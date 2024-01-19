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
    /**
     * 分页信息
     */
    private Pagination pagination;

    /**
     * 构造方法
     *
     * @param data       数据列表
     * @param pagination 分页信息
     */
    public Page(List<T> data, Pagination pagination) {
        this.data = data;
        this.pagination = pagination;
        this.code = ResultCode.SUCCESS.getCode();
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
    }
}
