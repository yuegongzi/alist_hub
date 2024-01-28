package org.alist.hub.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FileSystem {
    private String name;
    private Long size;
    @JsonProperty("is_dir")
    private boolean isDir;
    private String modified;
    private Integer type;
}
