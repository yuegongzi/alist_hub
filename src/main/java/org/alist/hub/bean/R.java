package org.alist.hub.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * R类是一个泛型类，用于表示一个结果对象。
 * 该类包含了结果对象的code、data和message属性。
 * 可以通过构造函数初始化结果对象，
 * 也可以使用静态方法创建结果对象的实例。
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class R<T> implements Serializable {

    private int code;      // 结果代码
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;       // 结果数据
    private String message;   // 结果消息

    /**
     * 通过结果代码、结果消息构造结果对象。
     *
     * @param resultCode 结果代码
     */
    private R(ResultCode resultCode) {
        this(resultCode, null, resultCode.getMessage());
    }

    /**
     * 通过结果代码、结果消息构造结果对象。
     *
     * @param resultCode 结果代码
     * @param msg        结果消息
     */
    private R(ResultCode resultCode, String msg) {
        this(resultCode, null, msg);
    }

    /**
     * 通过结果代码、结果数据构造结果对象。
     *
     * @param resultCode 结果代码
     * @param data       结果数据
     */
    private R(ResultCode resultCode, T data) {
        this(resultCode, data, resultCode.getMessage());
    }

    /**
     * 通过结果代码、结果数据、结果消息构造结果对象。
     *
     * @param resultCode 结果代码
     * @param data       结果数据
     * @param msg        结果消息
     */
    private R(ResultCode resultCode, T data, String msg) {
        this(resultCode.getCode(), data, msg);
    }

    /**
     * 通过结果代码、结果数据、结果消息构造结果对象。
     *
     * @param code 结果代码
     * @param data 结果数据
     * @param msg  结果消息
     */
    private R(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.message = msg;
    }

    /**
     * 通过数据创建成功结果对象。
     *
     * @param data 数据
     * @return 成功结果对象
     */
    public static <T> R<T> data(T data) {
        return data(data, ResultCode.SUCCESS.getMessage());
    }

    /**
     * 通过数据和结果消息创建成功结果对象。
     *
     * @param data 数据
     * @param msg  结果消息
     * @return 成功结果对象
     */
    public static <T> R<T> data(T data, String msg) {
        return data(ResultCode.SUCCESS.code, data, msg);
    }

    /**
     * 通过结果代码、数据和结果消息创建成功结果对象。
     *
     * @param code 结果代码
     * @param data 数据
     * @param msg  结果消息
     * @return 成功结果对象
     */
    public static <T> R<T> data(int code, T data, String msg) {
        return new R<>(code, data, data == null ? "暂无承载数据" : msg);
    }

    /**
     * 创建成功结果对象。
     *
     * @param msg 成功结果消息
     * @return 成功结果对象
     */
    public static <T> R<T> success(String msg) {
        return new R<>(ResultCode.SUCCESS, msg);
    }

    /**
     * 创建成功结果对象。
     *
     * @param resultCode 成功结果代码
     * @return 成功结果对象
     */
    public static <T> R<T> success(ResultCode resultCode) {
        return new R<>(resultCode);
    }

    /**
     * 创建成功结果对象。
     *
     * @param resultCode 成功结果代码
     * @param msg        成功结果消息
     * @return 成功结果对象
     */
    public static <T> R<T> success(ResultCode resultCode, String msg) {
        return new R<>(resultCode, msg);
    }

    /**
     * 创建失败结果对象。
     *
     * @param msg 失败结果消息
     * @return 失败结果对象
     */
    public static <T> R<T> fail(String msg) {
        return new R<>(ResultCode.FAILURE, msg);
    }

    /**
     * 创建失败结果对象。
     *
     * @param code 失败结果代码
     * @param msg  失败结果消息
     * @return 失败结果对象
     */
    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, null, msg);
    }

    /**
     * 创建失败结果对象。
     *
     * @param resultCode 失败结果代码
     * @return 失败结果对象
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode);
    }

    /**
     * 创建失败结果对象。
     *
     * @param resultCode 失败结果代码
     * @param msg        失败结果消息
     * @return 失败结果对象
     */
    public static <T> R<T> fail(ResultCode resultCode, String msg) {
        return new R<>(resultCode, msg);
    }

    /**
     * 根据操作是否成功创建结果对象。
     *
     * @param flag 操作是否成功的标志
     * @return 结果对象
     */
    public static <T> R<T> status(boolean flag) {
        return flag ? success("成功") : fail("操作失败");
    }

    /**
     * 创建分页对象。
     *
     * @param page 分页对象
     * @return 分页结果对象
     */
    public static <T> Page<T> page(org.springframework.data.domain.Page<T> page) {
        Pagination pagination = new Pagination(page.getNumber(), page.getSize(), page.getTotalElements());
        return new Page<>(page.getContent(), pagination);
    }

}

