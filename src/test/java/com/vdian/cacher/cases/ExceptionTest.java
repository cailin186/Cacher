package com.vdian.cacher.cases;

import com.google.common.collect.Lists;
import com.vdian.cacher.cases.base.TestBase;
import com.vdian.cacher.domain.User;
import com.vdian.cacher.exception.CacherException;
import com.vdian.cacher.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author jifang
 * @since 2016/11/30 下午2:20.
 */
public class ExceptionTest extends TestBase {

    @Autowired
    private UserService service;

    @Test
    public void test1() {
        service.noParam();
    }

    @Test
    public void test2() {
        service.noCacheKey(new Object());
    }

    @Test(expected = CacherException.class)
    public void test3() {
        service.wrongMultiParam(new Object());
    }

    @Test(expected = CacherException.class)
    public void test4() {
        Map<Integer, Object> map = service.wrongIdentifier(Lists.newArrayList(1, 2));
    }

    @Test(expected = CacherException.class)
    public void test41() {
        List<User> map = service.wrongCollectionReturn(Lists.newArrayList(1, 2));
    }

    @Test(expected = NullPointerException.class)
    public void test50() {
        List<User> users = service.correctIdentifier(null);
    }

    @Test
    public void test51() {
        service.correctIdentifier(Collections.<Integer>emptyList());
        service.correctIdentifier(Collections.<Integer>emptyList());
    }

    @Test
    public void test5() {
        service.correctIdentifier(Lists.newArrayList(1, 2));
        List<User> users = service.correctIdentifier(Lists.newArrayList(1, 2, 3, 4, 5, 6));
        System.out.println(users);
    }
}
