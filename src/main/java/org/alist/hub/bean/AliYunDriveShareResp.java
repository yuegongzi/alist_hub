package org.alist.hub.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AliYunDriveShareResp {
    private List<ShareFile> items;

    @Data
    public static class ShareFile {

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

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;

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

}
