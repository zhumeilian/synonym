package com.opendatasoft.redis;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import redis.clients.jedis.JedisPubSub;

public class AddTermRedisPubSub extends JedisPubSub {

	public static ESLogger logger = Loggers.getLogger("synonyms-redis-msg");

	@Override
	public void onMessage(String channel, String message) {
		logger.debug("channel:" + channel + " and message:" + message,
				new Object[0]);
		FileUtils.append(message);
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		logger.debug("pattern:" + pattern + " and channel:" + channel
				+ " and message:" + message);
		onMessage(channel, message);
	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {
		logger.info("psubscribe pattern:" + pattern
				+ " and subscribedChannels:" + subscribedChannels);

	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {
		logger.info("punsubscribe pattern:" + pattern
				+ " and subscribedChannels:" + subscribedChannels);

	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		logger.info("subscribe channel:" + channel + " and subscribedChannels:"
				+ subscribedChannels);

	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {
		logger.info("unsubscribe channel:" + channel
				+ " and subscribedChannels:" + subscribedChannels);
	}

}
