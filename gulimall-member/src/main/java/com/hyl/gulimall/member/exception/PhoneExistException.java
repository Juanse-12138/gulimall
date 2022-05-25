package com.hyl.gulimall.member.exception;

public class PhoneExistException extends Exception{
    public PhoneExistException() {
        super("手机号已存在");
    }
}
