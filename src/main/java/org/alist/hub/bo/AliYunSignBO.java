package org.alist.hub.bo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.alist.hub.utils.JsonUtil;

@Data
public class AliYunSignBO implements Persistent {
    private JsonNode result;

    @Override
    public String getId() {
        return "aliyun_drive_sign";
    }

    @Override
    public String getValue() {
        return JsonUtil.toJson(this);
    }

}
