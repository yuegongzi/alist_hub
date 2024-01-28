package org.alist.hub.bo;

import lombok.Data;
import org.alist.hub.utils.JsonUtil;

@Data
public class AliYunFolderBO implements Persistent {
    private String folderId;
    private String driveId;
    private String name;

    @Override
    public String getId() {
        return "temp_transfer_folder_id"; //你的阿里网盘的转存目录的folder id
    }

    @Override
    public String getValue() {
        return JsonUtil.toJson(this);
    }

}
