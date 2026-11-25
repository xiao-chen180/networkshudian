
package com.example.bookstore.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", ex.getStatusCode());
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "用户名或密码错误");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        String message = ex.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            if (message.contains("account")) {
                response.put("message", "账号已存在");
            } else if (message.contains("username")) {
                response.put("message", "用户名已存在");
            } else if (message.contains("email")) {
                response.put("message", "邮箱已存在");
            } else {
                response.put("message", "数据已存在");
            }
        } else {
            response.put("message", "数据完整性错误");
        }
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("detail", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "用户不存在");
        response.put("status", HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        response.put("message", "注册失败，表单验证错误");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        ex.printStackTrace(); // 打印异常堆栈
        Map<String, Object> response = new HashMap<>();
        response.put("message", "服务器内部错误：" + ex.getMessage());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
