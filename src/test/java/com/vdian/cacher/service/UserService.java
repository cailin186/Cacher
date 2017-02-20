package com.vdian.cacher.service;


import com.vdian.cacher.domain.User;

import java.util.List;
import java.util.Map;

/**
 * @author jifang
 * @since 16/7/19 下午6:24.
 */
public interface UserService {

    /**
     * multi
     ***/
    Map<Integer, User> returnMap(String app, List<Integer> ids, Object noKey);

    void multiInvalid(String apps, List<Integer> ids);

    List<User> returnList(List<Integer> ids, String name, Object non);

    void batchUpdateList(List<User> users);

    /***
     * single
     ****/
    User singleKey(int id, String name, Object non);

    void singleRemove(int id, String name, Object non);

    void updateUser(User user, String name, Object non);

    /**
     * ops
     */
    void noParam();

    void noCacheKey(Object o);

    void wrongMultiParam(Object o);

    Map<Integer, Object> wrongIdentifier(List<Integer> ids);

    List<User> wrongCollectionReturn(List<Integer> ids);

    List<User> correctIdentifier(List<Integer> ids);
}
