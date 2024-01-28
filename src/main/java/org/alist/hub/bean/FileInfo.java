package org.alist.hub.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FileInfo {
    // 驱动ID
    @JsonProperty("drive_id")
    private String driveId;

    // 文件ID
    @JsonProperty("file_id")
    private String fileId;

    // 父文件ID
    @JsonProperty("parent_file_id")
    private String parentFileId;

    // 文件名
    @JsonProperty("file_name")
    private String fileName;
}
