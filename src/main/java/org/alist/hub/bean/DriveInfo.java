package org.alist.hub.bean;

import lombok.Data;

/**
 * 阿里云盘信息
 */
@Data
public class DriveInfo {
    // 用户ID
    private String user_id;
    // 姓名
    private String user_name;
    // 头像
    private String avatar;
    // 默认驱动器ID
    private String default_drive_id;
    // 资源驱动器ID
    private String resource_drive_id;
    // 备份驱动器ID
    private String backup_drive_id;

}
