package org.alist.hub.bean;

import lombok.Data;

@Data
public class UserClaims {
    private String username;
    private Long pwd_ts;
    private Long exp;
    private Long nbf;
    private Long iat;

    public boolean isExpired() {
        return System.currentTimeMillis() > exp * 1000;
    }
}
