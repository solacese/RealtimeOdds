package cacheproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;

class PublishColEvtHandler implements JCSMPStreamingPublishCorrelatingEventHandler {
	protected static final Logger logger = LoggerFactory.getLogger(PublishColEvtHandler.class);

	public void responseReceived(String key) {
		// Not invoked for Direct Messaging
		logger.debug("Producer received response for msg: {}", key);
	}

	public void handleError(String key, JCSMPException e, long timestamp) {
		logger.error("Producer received error for msg: {}@{} - {}", key, timestamp, e);
	}

	@Override
	public void handleErrorEx(Object key, JCSMPException e, long timestamp) {
		logger.error("[Ex]Message response (rejected) received for {}, error was {}", key, e.getMessage());
	}

	@Override
	public void responseReceivedEx(Object key) {
		logger.debug("[Ex]Message response (accepted) received for: " + key);
	}
}
