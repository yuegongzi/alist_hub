package org.alist.hub.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class AccountDTO {
    /**
     * 类型
     */
    @NotEmpty(message = "请填写类型")
    private String type;
    /**
     * 其他参数
     */
    @NotNull(message = "请填写其他参数")
    private Map<String, Object> params;
}
