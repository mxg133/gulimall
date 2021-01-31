package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author 孟享广
 * @date 2021-01-31 2:52 下午
 * @description
 */
@Data
@ToString
public class SocialUser {

    /**
     * 用户授权的唯一票据，用于调用微博的开放接口，同时也是第三方应用验证微博用户登录的唯一票据，第三方应用应该用该票据和自己应用内的用户建立唯一影射关系，来识别登录状态，不能使用本返回值里的UID字段来做登录识别。
     */
    private String access_token;
    //access_token的生命周期，单位是秒数。
    private String remind_in;
    //access_token的生命周期（该参数即将废弃，开发者请使用expires_in）。
    private long expires_in;
    //授权用户的UID，本字段只是为了方便开发者，减少一次user/show接口调用而返回的，第三方应用不能用此字段作为用户登录状态的识别，只有access_token才是用户授权的唯一票据。
    private String uid;//相当于微博账号 作为注册gulimall的账号 同一个人的uid是固定的
    private String isRealName;
}
