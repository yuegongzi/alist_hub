package org.alist.hub.bean;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "成功"),
    FAILURE(HttpServletResponse.SC_BAD_REQUEST, "业务异常"),
    UN_AUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "请求未授权"),
    CLIENT_UN_AUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "客户端请求未授权"),
    NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, "404 没找到请求"),
    MSG_NOT_READABLE(HttpServletResponse.SC_BAD_REQUEST, "消息不能读取"),
    METHOD_NOT_SUPPORTED(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "不支持当前请求方法"),
    MEDIA_TYPE_NOT_SUPPORTED(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "不支持当前媒体类型"),
    REQ_REJECT(HttpServletResponse.SC_FORBIDDEN, "请求被拒绝"),
    INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器异常"),
    PARAM_MISS(HttpServletResponse.SC_BAD_REQUEST, "缺少必要的请求参数"),
    PARAM_TYPE_ERROR(HttpServletResponse.SC_BAD_REQUEST, "请求参数类型错误"),
    PARAM_BIND_ERROR(HttpServletResponse.SC_BAD_REQUEST, "请求参数绑定错误"),
    PARAM_VALID_ERROR(HttpServletResponse.SC_BAD_REQUEST, "参数校验失败"),
    ILLEGAL_SIGNATURE(HttpServletResponse.SC_NOT_ACCEPTABLE, "签名非法");
    final int code;
    final String message;

}
