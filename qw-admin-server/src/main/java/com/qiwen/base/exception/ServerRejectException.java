package com.qiwen.base.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 服务器拒绝访问异常
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ServerRejectException extends RuntimeException {
    public ServerRejectException() {
    }

    public ServerRejectException(String message) {
        super(message);
    }

    public ServerRejectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerRejectException(Throwable cause) {
        super(cause);
    }

    public ServerRejectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
