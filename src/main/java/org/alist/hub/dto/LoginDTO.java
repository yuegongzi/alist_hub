package org.alist.hub.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginDTO {
    @NotEmpty(message = "请填写账号")
    private String username;
    @NotEmpty(message = "请填写密码")
    private String password;
}
