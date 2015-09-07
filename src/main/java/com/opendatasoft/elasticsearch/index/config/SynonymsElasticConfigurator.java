package com.opendatasoft.elasticsearch.index.config;

import com.opendatasoft.redis.AddTermRedisPubSub;
import com.opendatasoft.redis.RedisPoolBuilder;
import com.opendatasoft.redis.RedisUtils;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SynonymsElasticConfigurator {
    public static ESLogger logger = Loggers.getLogger("synonyms-filter");
    private static boolean loaded = false;
    public static Environment environment;

    public static void init(Settings indexSettings, Settings settings) {
    	if (isLoaded()) {
			return;
		}
    	environment  =new Environment(indexSettings);
        initRedis(settings);
        setLoaded(true);
    }
    
    private static void initRedis(final Settings settings) {
		if(null==settings.get("redis.ip")){
			logger.info("没有找到redis相关配置!");
			return;
		}
		new Thread(new  Runnable() {
			@Override
			public void run() {
				RedisPoolBuilder redisPoolBuilder = new RedisPoolBuilder();
				int maxActive = settings.getAsInt("redis.pool.maxactive", redisPoolBuilder.getMaxActive());
				int maxIdle = settings.getAsInt("redis.pool.maxidle", redisPoolBuilder.getMaxIdle());
				int maxWait = settings.getAsInt("redis.pool.maxwait", redisPoolBuilder.getMaxWait());
				boolean testOnBorrow = settings.getAsBoolean("redis.pool.testonborrow", redisPoolBuilder.isTestOnBorrow());
				logger.debug("maxActive:"+maxActive+",maxIdle:"+maxIdle+",maxWait:"+maxWait+",testOnBorrow:"+testOnBorrow );
				
				String ipAndport = settings.get("redis.ip",redisPoolBuilder.getIpAddress());
				int port = settings.getAsInt("redis.port", redisPoolBuilder.getPort());
				String channel = settings.get("redis.channel","synonyms_term");
				logger.debug("ip:"+ipAndport+",port:"+port+",channel:"+channel);
				
				JedisPool pool = redisPoolBuilder.setMaxActive(maxActive).setMaxIdle(maxIdle).setMaxWait(maxWait).setTestOnBorrow(testOnBorrow)
				.setIpAddress(ipAndport).setPort(port).jedisPool();
				RedisUtils.setJedisPool(pool);
				final Jedis jedis = RedisUtils.getConnection();
				
				logger.debug("pool:"+(pool==null)+",jedis:"+(jedis==null));
				logger.info("redis守护线程准备完毕,ip:{},port:{},channel:{}",ipAndport,port,channel );
				jedis.subscribe(new AddTermRedisPubSub(), new String[]{channel});
				RedisUtils.closeConnection(jedis);
			}
		}).start();
		
	}

    public static boolean isLoaded() {
        return loaded;
    }

    public static void setLoaded(boolean loaded) {
    	SynonymsElasticConfigurator.loaded = loaded;
    }

}
