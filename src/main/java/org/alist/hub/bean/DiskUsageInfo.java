package org.alist.hub.bean;

import lombok.Data;

@Data
public class DiskUsageInfo {
    private String fileSystem;
    private long total;
    private long used;
    private long available;
    private double usagePercentage;
}
