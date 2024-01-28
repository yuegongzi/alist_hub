package org.alist.hub.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserClaims {
    // 用户名
    private String username;

    // 密码时间戳
    @JsonProperty("pwd_ts")
    private Long pwdTs;

    // 过期时间
    private Long exp;

    // 生效时间
    private Long nbf;

    // 签发时间
    private Long iat;

    public boolean isExpired() {
        return System.currentTimeMillis() > exp * 1000;
    }
}
