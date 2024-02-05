package org.alist.hub.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileWatcherDTO {
    @NotNull(message = "请填写存储挂载点")
    private Long storageId;
    @NotEmpty(message = "请选择挂载路径")
    private String path;
    @NotEmpty(message = "请填写转存文件夹名称")
    private String folderName;
}
