package com.vdian.cacher.domain;

import java.io.Serializable;

/**
 * @author jifang
 * @since 2017/1/11 下午2:03.
 */
public class SimpleUser implements Serializable {

    private static final long serialVersionUID = -403429791930316888L;

    private int id;

    private String address;

    public SimpleUser(int id, String address) {
        this.id = id;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
