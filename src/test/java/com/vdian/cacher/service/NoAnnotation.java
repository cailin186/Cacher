package com.vdian.cacher.service;

import com.vdian.cacher.ICache;
import com.vdian.cacher.domain.SimpleUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

/**
 * @author jifang
 * @since 2016/12/2 上午11:01.
 */
@Component
public class NoAnnotation implements Serializable {

    @Qualifier("redisCache")
    @Autowired
    private ICache cache;

    public SimpleUser getFromDBOrHttp(int id, String address) {
        // 0. compose cache key
        String key = genCacheKey(id, address);

        // 1. read cache
        SimpleUser user = (SimpleUser) cache.read(key);
        if (user == null) {
            // 2. select db or http
            user = new SimpleUser(id, address);

            // 3. write cache
            cache.write(key, user, 1000);
        }

        return user;
    }

    public List<SimpleUser> getFromDBOrHttp(List<Integer> ids, String address) {

        // 0. compose cache key
        List<String> keys = new ArrayList<>(ids.size());
        for (int id : ids) {
            keys.add(genCacheKey(id, address));
        }

        // 2. read cache
        Map<String, Object> fromCache = cache.read(keys);
        List<SimpleUser> users = new ArrayList<>(ids.size());

        // 3. remain not in cache
        for (Iterator<Integer> iter = ids.iterator(); iter.hasNext(); ) {
            int id = iter.next();
            String key = genCacheKey(id, address);
            SimpleUser user = (SimpleUser) fromCache.get(key);

            // in cache
            if (user != null) {
                users.add(user);
                iter.remove();
            }
        }

        // 4. select db or http
        Map<String, Object> needSaveCache = new HashMap<>(ids.size());
        for (int id : ids) {
            SimpleUser user = new SimpleUser(id, address);
            String key = genCacheKey(id, address);
            needSaveCache.put(key, user);
            users.add(user);
        }

        // 5. write cache
        if (!needSaveCache.isEmpty()) {
            cache.write(needSaveCache, 1000);
        }

        return users;
    }

    private String genCacheKey(int id, String address) {
        return String.format("id:%s-address:%s", id, address);
    }
    
}
