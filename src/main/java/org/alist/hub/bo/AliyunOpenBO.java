package org.alist.hub.bo;

import lombok.Data;
import org.alist.hub.utils.JsonUtil;

@Data
public class AliyunOpenBO implements Persistent {
    private String refresh_token;
    private String access_token;
    private Long expire;

    @Override
    public String getId() {
        return "myopentoken.txt";//用来加载自己的阿里云盘
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
