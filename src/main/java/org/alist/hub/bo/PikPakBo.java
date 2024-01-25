package org.alist.hub.bo;

import lombok.Data;
import org.alist.hub.utils.JsonUtil;

@Data
public class PikPakBo implements Persistent {
    private String username;
    private String password;

    @Override
    public String getId() {
        return "pikpak.txt";
    }

    @Override
    public String getValue() {
        return JsonUtil.toJson(this);
    }

    @Override
    public String getFileValue() {
        return '"' +
                this.username +
                '"' +
                ' ' +
                '"' +
                this.password +
                '"';
    }
}
