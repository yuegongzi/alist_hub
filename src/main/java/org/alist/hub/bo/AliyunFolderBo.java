package org.alist.hub.bo;

import lombok.Data;

@Data
public class AliyunFolderBo implements Persistent {
    private String folder_id;
    private String drive_id;
    private String name;

    @Override
    public String getId() {
        return "temp_transfer_folder_id.txt"; //你的阿里网盘的转存目录的folder id
    }

    @Override
    public String getValue() {
        return this.folder_id;
    }

    @Override
    public String getFileValue() {
        return this.folder_id;
    }
}
