package org.alist.hub.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SpaceInfo {
    @JsonProperty("total_size")
    private long totalSize;
    @JsonProperty("used_size")
    private long usedSize;
}
