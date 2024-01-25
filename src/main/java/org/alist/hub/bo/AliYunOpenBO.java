package org.alist.hub.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.alist.hub.utils.JsonUtil;

@Data
public class AliYunOpenBO implements Persistent {
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private Long expiresIn;
    @JsonProperty("token_type")
    private String tokenType;

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
        return this.refreshToken;
    }
}
