package org.alist.hub.bo;

import lombok.Data;
import org.alist.hub.utils.JsonUtil;

@Data
public class AliyunDriveBO implements Persistent {
    private String refresh_token;
    private String access_token;
    private Integer expire_in;

    @Override
    public String getId() {
        return "mytoken.txt";//用来加载阿里分享，和自动签到
    }

    @Override
    public String getValue() {
        return JsonUtil.toJson(this);
    }

    @Override
    public String getFileValue() {
        return this.refresh_token;
    }
}
