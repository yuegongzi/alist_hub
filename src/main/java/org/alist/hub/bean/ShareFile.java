package org.alist.hub.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShareFile {
    @JsonProperty("drive_id")
    private String driveId;

    @JsonProperty("domain_id")
    private String domainId;

    @JsonProperty("file_id")
    private String fileId;

    @JsonProperty("share_id")
    private String shareId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("file_extension")
    private String fileExtension;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("mime_extension")
    private String mimeExtension;

    @JsonProperty("size")
    private long size;

    @JsonProperty("parent_file_id")
    private String parentFileId;

    @JsonProperty("category")
    private String category;

    @JsonProperty("punish_flag")
    private int punishFlag;
}
