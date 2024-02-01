package org.alist.hub.bean;

import lombok.Data;

import java.util.List;

@Data
public class FileSystemResp {
    private List<FileSystem> content;
    private Integer total;
}
