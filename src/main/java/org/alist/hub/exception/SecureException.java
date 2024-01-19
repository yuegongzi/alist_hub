package org.alist.hub.exception;

import lombok.Getter;
import org.alist.hub.bean.ResultCode;

@Getter
public class SecureException extends RuntimeException {

    private final ResultCode resultCode;

    public SecureException(String message) {
        super(message);
        this.resultCode = ResultCode.UN_AUTHORIZED;
    }

    public SecureException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public SecureException(ResultCode resultCode, Throwable cause) {
        super(cause);
        this.resultCode = resultCode;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}