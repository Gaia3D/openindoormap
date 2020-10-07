package io.openindoormap.config;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.openindoormap.support.LogMessageSupport;

import java.io.IOException;

/**
 * APIController Exception 처리
 *
 * @author Cheon JeongDae
 */
@RestControllerAdvice(basePackages = {"io.openindoormap.api"})
public class APIControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> error(WebRequest request, DataAccessException e) {
        String errorCode = "db.exception";
        String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        if (message == null) {
            if (e.getCause() != null && e.getCause().toString().length() > 10) {
                message = e.getCause().toString();
            } else {
                message = e.toString();
            }
        }

        LogMessageSupport.printMessage(e, "@@ DataAccessException. message = {}", message);

        return handleExceptionInternal(e, errorCode, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> error(WebRequest request, RuntimeException e) {
        String errorCode = "runtime.exception";
        String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        if (message == null) {
            if (e.getCause() != null && e.getCause().toString().length() > 10) {
                message = e.getCause().toString();
            } else {
                message = e.toString();
            }
        }

        LogMessageSupport.printMessage(e, "@@ RuntimeException. message = {}", message);

        return handleExceptionInternal(e, errorCode, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> error(WebRequest request, IOException e) {
        String errorCode = "io.exception";
        String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        if (message == null) {
            if (e.getCause() != null && e.getCause().toString().length() > 10) {
                message = e.getCause().toString();
            } else {
                message = e.toString();
            }
        }

        LogMessageSupport.printMessage(e, "@@ IOException. message = {}", message);

        return handleExceptionInternal(e, errorCode, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> error(WebRequest request, Exception e) {
        String errorCode = "unknown.exception";
        String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        if (message == null) {
            if (e.getCause() != null && e.getCause().toString().length() > 10) {
                message = e.getCause().toString();
            } else {
                message = e.toString();
            }
        }

        LogMessageSupport.printMessage(e, "@@ Exception. message = {}", message);

        return handleExceptionInternal(e, errorCode, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
