package org.alist.hub.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.alist.hub.bo.AliYunDriveBO;

@Data
public class AliYunDriveResp {
    @JsonProperty("pds_login_result")
    private AliYunDriveBO result;
}
