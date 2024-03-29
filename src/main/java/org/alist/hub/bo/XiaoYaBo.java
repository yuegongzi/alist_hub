package org.alist.hub.bo;

import lombok.Data;
import org.alist.hub.util.JsonUtils;

import java.util.Date;

@Data
public class XiaoYaBo implements Persistent {
    private String version;
    private Date updateTime;

    @Override
    public String getId() {
        return "xiaoya";
    }

    @Override
    public String getValue() {
        return JsonUtils.toJson(this);
    }

}
