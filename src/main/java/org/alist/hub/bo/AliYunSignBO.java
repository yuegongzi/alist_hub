package org.alist.hub.bo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.alist.hub.util.JsonUtils;

@Data
public class AliYunSignBO implements Persistent {
    private JsonNode result;

    @Override
    public String getId() {
        return "aliyun_drive_sign";
    }

    @Override
    public String getValue() {
        return JsonUtils.toJson(this);
    }

}
