package org.alist.hub.bo;

import lombok.Data;
import org.alist.hub.util.JsonUtils;

@Data
public class QuarkBO implements Persistent {
    private String cookie;

    @Override
    public String getId() {
        return "quark";
    }

    @Override
    public String getValue() {
        return JsonUtils.toJson(this);
    }
}
