package org.alist.hub.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 阿里云盘信息
 */
@Data
public class DriveInfo {
    // 用户ID
    @JsonProperty("user_id")
    private String userId;

    // 姓名
    @JsonProperty("user_name")
    private String userName;

    // 头像
    @JsonProperty("avatar")
    private String avatar;

    // 默认驱动器ID
    @JsonProperty("default_drive_id")
    private String defaultDriveId;

    // 资源驱动器ID
    @JsonProperty("resource_drive_id")
    private String resourceDriveId;

    // 备份驱动器ID
    @JsonProperty("backup_drive_id")
    private String backupDriveId;

}
