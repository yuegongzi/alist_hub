package org.alist.hub.service;

import java.util.Map;

public interface AliYunDriveService {
    /**
     * 授权 返回二维码状态
     */
    String authorize(Map<String, Object> params);
}
