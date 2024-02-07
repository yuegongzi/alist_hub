package org.alist.hub.bo;

import lombok.Data;
import org.alist.hub.util.JsonUtils;

@Data
public class NoticeBO implements Persistent {
    private boolean sign;
    private boolean update;
    private boolean transfer;
    private String pushKey;

    @Override
    public String getId() {
        return "notice";
    }

    @Override
    public String getValue() {
        return JsonUtils.toJson(this);
    }
}
