package org.alist.hub.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class NoticeDTO {
    @NotEmpty(message = "请填写推送Key")
    private String pushKey;
}
