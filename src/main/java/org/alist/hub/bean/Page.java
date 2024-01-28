package org.alist.hub.bean;

import lombok.Data;

import java.util.List;

@Data
public class Page<T> {
    /**
     * 页面响应码
     */
    private int code;
    /**
     * 数据列表
     */
    private List<T> data;
    /**
     * 响应消息
     */
    private String message;

    private long current;
    private long pageSize;
    private long total;
    private boolean success;

    public Page(List<T> data, long current, long pageSize, long total) {
        this.data = data;
        this.current = current;
        this.pageSize = pageSize;
        this.total = total;
        this.code = ResultCode.SUCCESS.getCode();
        this.success = true;
        this.message = ResultCode.SUCCESS.getMessage();
    }

    /**
     * 构造方法
     *
     * @param message 响应消息
     */
    public Page(String message) {
        this.message = message;
        this.code = ResultCode.FAILURE.getCode();
        this.success = false;
    }
}
