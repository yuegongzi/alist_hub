package org.alist.hub.bean;

import lombok.Data;

@Data
public class FileInfo {
    // 驱动ID
    private String drive_id;
    // 文件ID
    private String file_id;
    // 父文件ID
    private String parent_file_id;
    // 文件名
    private String file_name;

}
