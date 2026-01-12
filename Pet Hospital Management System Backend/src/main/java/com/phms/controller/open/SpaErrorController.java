package com.phms.controller.open;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SPA 刷新路由兼容：
 * - 如果后端已经部署了 Vue 构建产物（static/index.html），则将 /error forward 到 index.html 交给前端路由处理
 * - 如果未部署（开发期），则重定向到 Vite dev server，避免 Whitelabel Error Page
 */
@Controller
public class SpaErrorController implements ErrorController {

    private final ResourceLoader resourceLoader;

    public SpaErrorController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @RequestMapping(value = "/error", produces = MediaType.TEXT_HTML_VALUE)
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 仅处理浏览器页面请求（Accept: text/html）
        String accept = request.getHeader("Accept");
        if (accept == null || !accept.contains("text/html")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            try {
                if (!response.isCommitted()) {
                    response.getWriter().write("{\"result\":\"fail\",\"message\":\"Not Found\"}");
                }
            } catch (IllegalStateException e) {
                // 如果输出流已经被使用，使用输出流写入
                response.getOutputStream().write("{\"result\":\"fail\",\"message\":\"Not Found\"}".getBytes("UTF-8"));
            }
            return;
        }

        // 生产：存在 static/index.html -> forward 给 Vue
        Resource spaIndex = resourceLoader.getResource("classpath:/static/index.html");
        if (spaIndex.exists()) {
            try {
                request.getRequestDispatcher("/index.html").forward(request, response);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        // 开发：未部署 SPA -> 跳转到 Vite dev server（按需改端口）
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String target = "http://localhost:5173" + uri + (query == null ? "" : ("?" + query));
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", target);
    }

    /**
     * 兼容旧版本 Spring Boot（需要实现 getErrorPath）。
     * 新版本已废弃此方法，但保留不会影响运行。
     */
    public String getErrorPath() {
        return "/error";
    }
}


