package com.vdian.cacher.support.cache;

import com.vdian.cacher.ICache;
import com.vdian.cacher.IObjectSerializer;
import com.vdian.cacher.enums.Expire;
import com.vdian.cacher.support.serialize.Hessian2Serializer;
import com.vdian.cacher.utils.CacherUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import javax.annotation.PreDestroy;
import java.util.*;

/**
 * @author jifang
 * @since 2016/12/12 下午3:06.
 */
public class RedisCache implements ICache {

    private IObjectSerializer serializer;

    private JedisPool pool;

    public RedisCache(String host, int port) {
        this(host, port, 8, 10);
    }

    public RedisCache(String host, int port, int maxTotal, int waitMillis) {
        this(host, port, maxTotal, waitMillis, new Hessian2Serializer());
    }

    public RedisCache(String host, int port, int maxTotal, int waitMillis, IObjectSerializer serializer) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxWaitMillis(waitMillis);

        pool = new JedisPool(config, host, port);
        this.serializer = serializer;
    }

    @Override
    public Object read(String key) {

        Jedis client = null;
        try {
            client = pool.getResource();
            byte[] bytes = client.get(key.getBytes());

            return serializer.deserialize(bytes);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void write(String key, Object value, long expire) {
        byte[] bytesValue = serializer.serialize(value);

        Jedis client = null;
        try {
            client = pool.getResource();
            if (expire == Expire.FOREVER) {
                client.set(key.getBytes(), bytesValue);
            } else {
                client.setex(key.getBytes(), (int) expire, bytesValue);
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public Map<String, Object> read(Collection<String> keys) {
        byte[][] keysArr = CacherUtils.toByteArray(keys);

        Jedis client = null;
        try {
            client = pool.getResource();
            List<byte[]> byteValues = client.mget(keysArr);
            Map<String, Object> valueMap;
            if (byteValues != null && !byteValues.isEmpty()) {
                valueMap = new HashMap<>(byteValues.size());

                int i = 0;
                for (String key : keys) {
                    byte[] byteValue = byteValues.get(i++);
                    if (byteValue != null && byteValue.length != 0) {
                        Object value = serializer.deserialize(byteValue);

                        valueMap.put(key, value);
                    }
                }
            } else {
                valueMap = Collections.emptyMap();
            }

            return valueMap;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void write(Map<String, Object> keyValueMap, long expire) {
        Map<byte[], byte[]> keyValueBytes = CacherUtils.mapSerialize(keyValueMap, serializer);

        Jedis client = null;
        try {
            client = pool.getResource();

            if (expire == Expire.FOREVER) {
                byte[][] array = new byte[keyValueBytes.size() * 2][];
                int index = 0;
                for (Map.Entry<byte[], byte[]> entry : keyValueBytes.entrySet()) {
                    array[index++] = entry.getKey();
                    array[index++] = entry.getValue();
                }

                client.mset(array);
            } else {
                Pipeline pipeline = client.pipelined();
                for (Map.Entry<byte[], byte[]> entry : keyValueBytes.entrySet()) {
                    pipeline.setex(entry.getKey(), (int) expire, entry.getValue());
                }
                pipeline.sync();
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void remove(String... keys) {
        Jedis client = null;
        try {
            client = pool.getResource();
            client.del(keys);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @PreDestroy
    public void tearDown() {
        if (pool != null && !pool.isClosed()) {
            pool.destroy();
        }
    }
}
