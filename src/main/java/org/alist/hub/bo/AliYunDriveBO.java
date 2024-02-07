package org.alist.hub.bo;

import lombok.Data;
import org.alist.hub.util.JsonUtils;

@Data
public class AliYunDriveBO implements Persistent {
    private String refreshToken;
    private String accessToken;//用于签到
    private Long expiresIn;
    //用户信息 回显给前端展示
    private String nickName;
    private String avatar;
    private String userName;
    private String userId;
    @Override
    public String getId() {
        return "mytoken";//用来加载阿里分享，和自动签到
    }

    @Override
    public String getValue() {
        return JsonUtils.toJson(this);
    }

}
