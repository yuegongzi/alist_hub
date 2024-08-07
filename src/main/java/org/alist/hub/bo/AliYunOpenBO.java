package org.alist.hub.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.alist.hub.util.JsonUtils;

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
    private String folderId;
    private String driveId;
    private String name;

    @Override
    public String getId() {
        return "myopentoken";//用来加载自己的阿里云盘
    }

    @Override
    public String getValue() {
        return JsonUtils.toJson(this);
    }

}
