package com.zhongan.ifrs17agentjava.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException; // 👈 提取到这里

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 专门处理找不到静态资源的 404 异常，不再返回 500 错误码
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(NoResourceFoundException e) {
        // 过滤掉 favicon.ico 的烦人报错，只打印其他真实的 404
        if (e.getResourcePath() != null && !e.getResourcePath().contains("favicon.ico")) {
            System.err.println("⚠️ [404 找不到路径] " + e.getResourcePath());
        }
        return Map.of("code", 404, "message", "找不到请求的路径: " + e.getResourcePath());
    }

    // 拦截所有其他 Exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleException(Exception e) {
        System.err.println("❌ [系统异常] " + e.getMessage());

        // 如果是大模型连接超时
        if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
            return Map.of(
                    "code", 503,
                    "message", "本地大模型服务(Ollama)未启动，请检查后重试！"
            );
        }

        // 通用降级话术
        return Map.of(
                "code", 500,
                "message", "AI 助手正在开小差，请稍后再试。错误追踪码：" + System.currentTimeMillis()
        );
    }
}