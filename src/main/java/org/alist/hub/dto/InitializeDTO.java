package org.alist.hub.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class InitializeDTO {
    @NotEmpty(message = "请填写refresh_token")
    private String drive_refresh_token;
    @NotEmpty(message = "请填写open_token")
    private String open_refresh_token;
    @NotEmpty(message = "请填写密码")
    private String password;
}
