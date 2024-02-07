package org.alist.hub.bo;

import lombok.Data;
import org.alist.hub.util.JsonUtils;

@Data
public class PikPakBo implements Persistent {
    private String username;
    private String password;

    @Override
    public String getId() {
        return "pikpak";
    }

    @Override
    public String getValue() {
        return JsonUtils.toJson(this);
    }

}
