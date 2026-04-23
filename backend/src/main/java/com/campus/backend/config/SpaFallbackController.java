package com.campus.backend.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SPA (Single Page Application) Fallback控制器
 * 
 * Vue Router使用History模式时，直接访问 /login, /products 等路径
 * 需要服务端返回 index.html，由前端路由接管。
 * 
 * 优先级设为最低，确保不干扰：
 * - API Controller (/api/**)
 * - 静态资源 (/assets/**, /index.html 等，由Spring Boot默认处理)
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@RestController
public class SpaFallbackController {

    @RequestMapping(value = {"/{path:[^\\.]*}"})
    public Object fallback() {
        return "forward:/index.html";
    }
}