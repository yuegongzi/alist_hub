package org.alist.hub.annotation;

import jakarta.annotation.Nonnull;
import org.alist.hub.bean.R;
import org.alist.hub.utils.JsonUtil;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

@Order(0)
@RestControllerAdvice
@SuppressWarnings("all")
public class ResponseHandler implements ResponseBodyAdvice<Object> {


    @Override
    public boolean supports(MethodParameter returnType, @Nonnull Class converterType) {
        if (returnType.getDeclaringClass().isAnnotationPresent(IgnoreResponseHandler.class)) {
            return false;
        }
        return !Objects.requireNonNull(returnType.getMethod()).isAnnotationPresent(IgnoreResponseHandler.class);

    }

    @Override
    public Object beforeBodyWrite(Object body, @Nonnull MethodParameter returnType, @Nonnull MediaType selectedContentType, @Nonnull Class selectedConverterType, @Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response) {
        if (body == null) {
            return R.status(true);
        }
        if (body instanceof Boolean) {
            return R.status((Boolean) body);
        }
        if (body instanceof R<?>) {
            return body;
        }
        if (body instanceof String) {
            return JsonUtil.toJson(R.data(body));
        }
        if (body instanceof Page<?>) {
            return R.page((Page<?>) body);
        }
        return R.data(body);
    }

}
