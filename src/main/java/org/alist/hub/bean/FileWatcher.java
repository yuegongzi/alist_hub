package org.alist.hub.bean;

import lombok.Data;

@Data
public class FileWatcher {
    private String path;
    private String folderName;
    private Long storageId;
    private String parentFileId;
    private String driveId;
    private String toDriveId;
    private String toFileId;
}
