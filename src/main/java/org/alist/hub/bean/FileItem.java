package org.alist.hub.bean;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileItem {

    @JsonProperty("drive_id")
    private String driveId;

    @JsonProperty("file_id")
    private String fileId;

    @JsonProperty("parent_file_id")
    private String parentFileId;

    private String name;

    private int size;

    @JsonProperty("file_extension")
    private String fileExtension;

    @JsonProperty("content_hash")
    private String contentHash;

    private String category;

    @JsonProperty("type")
    private String type;

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("url")
    private String url;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;

    @JsonProperty("play_cursor")
    private String playCursor;


}
