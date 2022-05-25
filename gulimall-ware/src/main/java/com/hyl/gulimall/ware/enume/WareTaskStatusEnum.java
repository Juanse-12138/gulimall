package com.hyl.gulimall.ware.enume;

public enum WareTaskStatusEnum {
    Locked(1,""),
    UNLocked(2,"");

    private Integer code;
    private String msg;
    WareTaskStatusEnum(Integer code,String msg) {
        this.code = code;
        this.msg = msg;
    }
    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
