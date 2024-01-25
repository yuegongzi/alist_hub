package org.alist.hub.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PasswordDTO {
    @NotEmpty(message = "请填写当前密码")
    private String password;
    @NotEmpty(message = "请填写新密码")
    private String newPassword;
    @NotEmpty(message = "请填写确认密码")
    private String confirmPassword;
}
