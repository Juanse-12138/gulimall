package com.hyl.common.constant;

/**
 * @author hyl_marco
 * @data 2022/3/26 - 14:18
 */
public class WareConstant {
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        BUYING(2,"正在采购"),
        FINISHED(3,"已完成"),
        ERRORED(4,"采购失败");

        private int code;
        private String msg;

        PurchaseStatusEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
