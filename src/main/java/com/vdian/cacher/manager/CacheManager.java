package com.vdian.cacher.manager;

import com.vdian.cacher.ICache;
import com.vdian.cacher.domain.BatchReadResult;
import com.vdian.cacher.exception.CacherException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jifang
 * @since 16/7/7.
 */
public class CacheManager {

    private static final Logger ROOT_LOGGER = LoggerFactory.getLogger(CacheManager.class);

    private static final Logger CACHER_LOGGER = LoggerFactory.getLogger("com.vdian.cacher");

    private Map<String, ICache> iCachePool;

    public CacheManager(final Map<String, ICache> caches) {
        iCachePool = createMapProxy(new ConcurrentHashMap<String, ICache>());
        iCachePool.putAll(caches);
    }

    /**
     * @param map
     * @return
     * @since 0.3.2
     */
    @SuppressWarnings("unchecked")
    private Map<String, ICache> createMapProxy(Map<String, ICache> map) {
        ProxyFactory factory = new CglibProxyFactory();
        Object proxyMap = factory.createInterceptorProxy(map, new Interceptor() {
            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                Object result = invocation.proceed();
                if (result == null
                        && StringUtils.equals(invocation.getMethod().getName(), "get")) {
                    String key = (String) invocation.getArguments()[0];
                    String msg = String.format("no ICache implementation named [%s], " +
                                    "please check the CacherAspect.caches param config correct",
                            key);
                    Throwable e = new CacherException(msg);
                    ROOT_LOGGER.error("wrong cache name {}", key, e);
                    CACHER_LOGGER.error("wrong cache name {}", key, e);
                    throw e;
                }

                return result;
            }
        }, new Class[]{Map.class});

        return (Map<String, ICache>) proxyMap;
    }

    public Object readSingle(String cache, String key) throws Exception {
        try {
            ICache cacheImpl = iCachePool.get(cache);
            return cacheImpl.read(key);
        } catch (Throwable e) {
            ROOT_LOGGER.error("read single cache failed, key: {} ", key, e);
            CACHER_LOGGER.error("read single cache failed, key: {} ", key, e);
            return null;
        }
    }

    public void writeSingle(String cache, String key, Object value, int expire) throws Exception {
        if (value != null) {
            try {
                ICache cacheImpl = iCachePool.get(cache);
                cacheImpl.write(key, value, expire);
            } catch (Throwable e) {
                ROOT_LOGGER.error("write single cache failed, key: {} ", key, e);
                CACHER_LOGGER.error("write single cache failed, key: {} ", key, e);
            }
        }
    }

    public BatchReadResult readBatch(String cache, Collection<String> keys) throws Exception {
        BatchReadResult batchReadResult;
        if (keys.isEmpty()) {
            batchReadResult = new BatchReadResult();
        } else {
            try {
                ICache cacheImpl = iCachePool.get(cache);

                Map<String, Object> fromCacheMap = cacheImpl.read(keys);

                // collect not nit keys, keep order when full hit
                Map<String, Object> hitValueMap = new LinkedHashMap<>();
                Set<String> notHitKeys = new LinkedHashSet<>();
                for (String key : keys) {
                    Object value = fromCacheMap.get(key);

                    if (value == null) {
                        notHitKeys.add(key);
                    } else {
                        hitValueMap.put(key, value);
                    }
                }

                batchReadResult = new BatchReadResult(hitValueMap, notHitKeys);
            } catch (Throwable e) {
                ROOT_LOGGER.error("read multi cache failed, keys: {}", keys, e);
                CACHER_LOGGER.error("read multi cache failed, keys: {}", keys, e);
                batchReadResult = new BatchReadResult();
            }
        }

        return batchReadResult;
    }

    public void writeBatch(String cache, Map<String, Object> keyValueMap, int expire) throws Exception {
        try {
            ICache cacheImpl = iCachePool.get(cache);
            cacheImpl.write(keyValueMap, expire);
        } catch (Exception e) {
            ROOT_LOGGER.error("write map multi cache failed, keys: {}", keyValueMap.keySet(), e);
            CACHER_LOGGER.error("write map multi cache failed, keys: {}", keyValueMap.keySet(), e);
        }
    }

    public void remove(String cache, String... keys) throws Exception {
        if (keys != null && keys.length != 0) {
            try {
                ICache cacheImpl = iCachePool.get(cache);
                cacheImpl.remove(keys);
            } catch (Throwable e) {
                ROOT_LOGGER.error("remove cache failed, keys: {}: ", keys, e);
                CACHER_LOGGER.error("remove cache failed, keys: {}: ", keys, e);
            }
        }
    }
}
