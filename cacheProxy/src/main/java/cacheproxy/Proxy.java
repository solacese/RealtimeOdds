package cacheproxy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.solacesystems.jcsmp.BytesMessage;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.CacheLiveDataAction;
import com.solacesystems.jcsmp.CacheRequestResult;
import com.solacesystems.jcsmp.CacheSession;
import com.solacesystems.jcsmp.CacheSessionProperties;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Proxy extends BaseApp {

	class CacheRequest {
		String  replyToTopic;
		boolean isReSend;

		public CacheRequest(String topic){
			replyToTopic = topic;						
			isReSend = false;
		}
		public CacheRequest(String topic, boolean _isReSend){
			replyToTopic = topic;						
			isReSend = _isReSend;
		}

	}

	protected static final Logger logger = LoggerFactory.getLogger(Proxy.class);

	protected CacheSession cacheSession = null;
	private XMLMessageProducer prod = null;
	private final AtomicLong atomRequestId = new AtomicLong();
	private Cache<Long, CacheRequest> requestId2Map;
	private LoadingCache<String, AtomicInteger> matchDeltaSequence;
	private final String fullMatchTopic = "t1/match/*/full";
	private CountDownLatch latch = new CountDownLatch(1);

	public Proxy() {
		super();
	}

	public static void main(final String[] args) {
		final Proxy app = new Proxy();
		app.run(args);
	}

	private void run(final String[] args) {

		conf.parse(args);
		final JCSMPProperties properties = buildJSCMPProperties();
		requestId2Map = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
		matchDeltaSequence = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
				.build(new CacheLoader<String, AtomicInteger>() {
					@Override
					public AtomicInteger load(final String key) throws Exception {
						return new AtomicInteger();
					}
				});
		try {
			session = JCSMPFactory.onlyInstance().createSession(properties);
			logger.debug("Start to connect session");
			session.connect();

			logger.debug("Create message consumer");
			final XMLMessageConsumer cons = session.getMessageConsumer(new CacheMessageListener(this));
			prod = session.getMessageProducer(new PublishColEvtHandler());

			// create cache session
			final CacheSessionProperties cacheProps = new CacheSessionProperties(conf.cacheName, conf.maxMsgs,
					conf.maxAge, conf.timeout);
			cacheSession = session.createCacheSession(cacheProps);

			logger.debug("Listen on the cache request topic: {}", conf.topic);
			final Topic topic = JCSMPFactory.onlyInstance().createTopic(conf.topic);
			// listen on the cache request topic
			session.addSubscription(topic);

			// listen on the FULL match messages to generate deltas
			final Topic matchTopic = JCSMPFactory.onlyInstance().createTopic(fullMatchTopic);
			session.addSubscription(matchTopic);

			logger.debug("Connected. Awaiting message...");
			cons.start();

			Helper.pressEnter("Press Ctrl+C to exit ...");
			latch.await();

		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				logger.debug("close Session");
				session.closeSession();
			}
		}
	}

	protected void onCacheRequest(final BytesXMLMessage msg) {
		final String jsonStr = new String(((BytesMessage) msg).getData());
		final JsonObject request = JsonParser.parseString(jsonStr).getAsJsonObject();

		// get interested topic
		String topic = request.get("topic").getAsString();
		final JsonElement eProtocol = request.get("protocol");
		if ((eProtocol != null) && (eProtocol.getAsString().equalsIgnoreCase("MQTT"))) {
			topic = topic.replace('+', '*');
			topic = topic.replace('#', '>');
		}
		// set reply-to-topic for later coming cached message
		final String replyToTopic = request.get("replyTo").getAsString();
		final boolean isReSend = request.get("isReSend").getAsBoolean();
		final long requestId = atomRequestId.incrementAndGet();

		// cache the PS+ cache request information so we'd know what to do
		// when the cached message comes
		requestId2Map.put(requestId, new CacheRequest(replyToTopic, isReSend));

		try {
			final Topic cacheTopic = JCSMPFactory.onlyInstance().createTopic(topic);
			final CacheRequestResult result = cacheSession.sendCacheRequest(requestId, cacheTopic, false,
					CacheLiveDataAction.FLOW_THRU);
			logger.debug("Cache Request NO:{} is {}, [{}] from [{}]", requestId, result, topic, replyToTopic);
		} catch (final Exception e) {
			logger.warn("Unable to send cache request: {}", e.getMessage());
		}
	}

	protected void onCachedMessage(final BytesXMLMessage msg) {
		try {
			final long requestId = msg.getCacheRequestId();
			final CacheRequest cq = requestId2Map.getIfPresent(requestId);
			if (null == cq) {
				// non existed cache request id, just discard it
				return;
			}

			final BytesMessage bMsg = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
			final byte[] msgData = ((BytesMessage) msg).getData();
			if (cq.isReSend){
				// so the client could know this is a 're-send' message
				final String jsonStr = new String(msgData);
				final JsonObject delta = JsonParser.parseString(jsonStr).getAsJsonObject();
				delta.addProperty("isReSend", cq.isReSend);
				bMsg.setData(delta.toString().getBytes());
			}else{
				bMsg.setData(msgData);
			}
			prod.send(bMsg, JCSMPFactory.onlyInstance().createTopic(cq.replyToTopic));
		} catch (final Exception e) {
			logger.warn("onCachedMessage: {}", e);
		}
	}

	// received a full match, generate the related delta message
	protected void onFullMatchMessage(final BytesXMLMessage msg) {
		try {
			final String deltaStr = buildDeltaJson(msg);

			final String topic = msg.getDestination().getName().replaceAll("full", "delta");
			final BytesMessage bMsg = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
			bMsg.setData(deltaStr.getBytes());
			prod.send(bMsg, JCSMPFactory.onlyInstance().createTopic(topic));
		} catch (final Exception e) {
			logger.warn("onFullMatchMessage: {}", e);
		}

	}

	private String buildDeltaJson(final BytesXMLMessage msg) {
		String deltaStr = null;
		try {
			// The logic of generating delta between two message totally depends on business 
			final String jsonStr = new String(((BytesMessage) msg).getData());
			final JsonObject match = JsonParser.parseString(jsonStr).getAsJsonObject();
			final JsonObject delta = JsonParser.parseString("{'isDelta': true}").getAsJsonObject();

			final JsonElement matchNum = match.get("matchNum");
			final JsonElement matchStatus = match.get("matchStatus");
			final JsonElement statuslastupdated = match.get("statuslastupdated");
			final JsonElement oddsH = match.get("hadodds").getAsJsonObject().get("H");
			final JsonElement oddsA = match.get("hadodds").getAsJsonObject().get("A");
			final JsonElement oddsD = match.get("hadodds").getAsJsonObject().get("D");

			final JsonObject hadodds = JsonParser.parseString("{}").getAsJsonObject();
			hadodds.add("H", oddsH);
			hadodds.add("A", oddsA);
			hadodds.add("D", oddsD);
			delta.add("matchNum", matchNum);
			delta.add("matchStatus", matchStatus);
			delta.add("statuslastupdated", statuslastupdated);
			delta.add("hadodds", hadodds);

			// maintain an increasing integer as a sequence of each Match
			final AtomicInteger sequence = matchDeltaSequence.get(matchNum.getAsString());
			delta.addProperty("sequenceInt", sequence.incrementAndGet());

			deltaStr = delta.toString();
		} catch (final Exception e) {
			logger.warn("buildDeltaJson: {}", e);
		}
		return deltaStr;

	}

}
