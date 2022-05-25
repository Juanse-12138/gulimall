package com.hyl.gulimall.member.exception;

public class UserExistException extends Exception {

    public UserExistException() {
        super("用户名已存在");
    }
}
