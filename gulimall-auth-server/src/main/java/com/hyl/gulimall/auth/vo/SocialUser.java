package com.hyl.gulimall.auth.vo;

import lombok.Data;

/**
 * @author hyl_marco
 * @data 2022/5/13 - 22:29
 */
@Data
public class SocialUser {
    private String access_token;
    private String remind_in;
    private Long expires_in;
    private String uid;
    private String isRealName;

}
