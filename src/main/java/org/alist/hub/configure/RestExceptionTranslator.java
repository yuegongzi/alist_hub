package org.alist.hub.configure;

import jakarta.servlet.Servlet;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.R;
import org.alist.hub.bean.ResultCode;
import org.alist.hub.exception.SecureException;
import org.alist.hub.exception.ServiceException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * 全局异常处理，处理可预见的异常，Order 排序优先级高
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@RestControllerAdvice
public class RestExceptionTranslator {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleError(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数{}", e.getParameterName());
        String message = String.format("缺少必要的请求参数: %s", e.getParameterName());
        return R.fail(ResultCode.PARAM_MISS, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleError(MethodArgumentTypeMismatchException e) {
        log.warn("请求参数格式错误{}", e.getMessage());
        String message = String.format("请求参数格式错误: %s", e.getName());
        return R.fail(ResultCode.PARAM_TYPE_ERROR, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<?> handleError(MethodArgumentNotValidException e) {
        log.warn("参数验证失败{}", e.getMessage());
        return handleError(e.getBindingResult());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleError(BindException e) {
        log.warn("参数绑定失败{}", e.getMessage());
        return handleError(e.getBindingResult());
    }

    private R<?> handleError(BindingResult result) {
        List<FieldError> errors = result.getFieldErrors();
        List<String> message = new ArrayList<>();
        for (final var error : errors) {
            message.add(String.format("%s(%s)", error.getDefaultMessage(), error.getField()));
        }
        return R.fail(ResultCode.PARAM_BIND_ERROR, StringUtils.collectionToDelimitedString(message, ","));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleError(ConstraintViolationException e) {
        log.warn("参数验证失败 {}", e.getMessage());
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        List<String> list = new ArrayList<>();
        for (final var constraint : violations) {
            String message = constraint.getMessage();
            String propertyPath = constraint.getPropertyPath().toString().split("\\.")[2];
            list.add(String.format("%s:%s", propertyPath, message));
        }
        String message = StringUtils.collectionToDelimitedString(list, ",");
        return R.fail(ResultCode.PARAM_VALID_ERROR, message);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<?> handleError(NoResourceFoundException e) {
        log.error("404没找到请求:{}", e.getMessage());
        return R.fail(ResultCode.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleError(HttpMessageNotReadableException e) {
        log.error("消息不能读取:{}", e.getMessage());
        return R.fail(ResultCode.MSG_NOT_READABLE, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public R<?> handleError(HttpRequestMethodNotSupportedException e) {
        log.error("不支持当前请求方法:{}", e.getMessage());
        return R.fail(ResultCode.METHOD_NOT_SUPPORTED, e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public R<?> handleError(HttpMediaTypeNotSupportedException e) {
        log.error("不支持当前媒体类型:{}", e.getMessage());
        return R.fail(ResultCode.MEDIA_TYPE_NOT_SUPPORTED, e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public R<?> handleError(HttpMediaTypeNotAcceptableException e) {
        String message = e.getMessage() + " " + StringUtils.collectionToDelimitedString(e.getSupportedMediaTypes(), ",");
        log.error("不接受的媒体类型:{}", message);
        return R.fail(ResultCode.MEDIA_TYPE_NOT_SUPPORTED, message);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.OK)
    public R<?> handleError(Throwable e) {
        log.error("服务器异常", e);
        return R.fail(ResultCode.INTERNAL_SERVER_ERROR, ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<?> handleError(ServiceException e) {
        log.error("业务异常", e);
        return R.fail(e.getResultCode(), e.getMessage());
    }

    @ExceptionHandler(SecureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleError(SecureException e) {
        log.error("认证异常", e);
        return R.fail(e.getResultCode(), e.getMessage());
    }
}
