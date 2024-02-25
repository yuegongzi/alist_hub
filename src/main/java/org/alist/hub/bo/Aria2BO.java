package org.alist.hub.bo;

import lombok.Data;
import org.alist.hub.util.JsonUtils;

@Data
public class Aria2BO implements Persistent {
    private String url;
    private String secretKey;

    @Override
    public String getId() {
        return "aria2";
    }

    @Override
    public String getValue() {
        return JsonUtils.toJson(this);
    }
}
