package org.alist.hub.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SettingDTO {
    @NotEmpty(message = "请填写refresh_token")
    private String refresh_token;
    @NotEmpty(message = "请填写open_token")
    private String open_token;
    @NotEmpty(message = "请填填写文件ID")
    private String transfer_folder_id;
}
