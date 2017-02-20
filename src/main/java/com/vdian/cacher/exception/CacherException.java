package com.vdian.cacher.exception;

/**
 * @author jifang
 * @since 16/7/18 下午4:07.
 */
public class CacherException extends RuntimeException {

    public CacherException(String message) {
        super(message);
    }

    public CacherException(Throwable cause) {
        super(cause);
    }
}
