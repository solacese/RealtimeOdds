package cacheproxy;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.XMLMessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheMessageListener implements XMLMessageListener {
	protected static final Logger logger = LoggerFactory.getLogger(CacheMessageListener.class);
	
	private Proxy proxy = null;
	public CacheMessageListener(Proxy _proxy) {
		super();
		proxy = _proxy;
	}
	
	@Override
	public void onReceive(BytesXMLMessage msg) {
		String recvTopic = msg.getDestination().getName();
		// check if this is a cache request
		if (recvTopic.equals("cacheproxy/request")){
			proxy.onCacheRequest(msg);
		} else if(msg.isCacheMessage()) {
			proxy.onCachedMessage(msg);
		} else {
			proxy.onFullMatchMessage(msg);
		}
	}

	@Override
	public void onException(JCSMPException e) {
		System.out.printf("Consumer received exception: %s%n", e);
	}	
}
