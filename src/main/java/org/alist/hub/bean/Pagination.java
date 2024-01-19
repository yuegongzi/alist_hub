package org.alist.hub.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 适配前端ant design table分页信息
 */
@Data
@AllArgsConstructor
public class Pagination {
    private long current;
    private long pageSize;
    private long total;
}
