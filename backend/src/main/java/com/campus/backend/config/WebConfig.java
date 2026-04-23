package com.campus.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 
 * 不再显式注册任何资源处理器！
 * 
 * 原因：显式注册 /** 资源映射会抢占 Controller 的 /api/** 路由，
 * 导致 "No static resource api/categories" 500错误。
 * 
 * Spring Boot 默认行为已足够：
 * - classpath:/static/ 下的文件自动映射到根路径
 * - 静态资源优先级低于 @RequestMapping Controller
 * - SPA fallback 由 SpaFallbackController 处理
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
}