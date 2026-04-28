package com.campus.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new SpaPathResourceResolver());
    }

    private static class SpaPathResourceResolver extends PathResourceResolver {
        @Override
        public Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource resource = location.createRelative(resourcePath);
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            if (!resourcePath.startsWith("api/") &&
                !resourcePath.startsWith("v2/") &&
                !resourcePath.startsWith("v3/") &&
                !resourcePath.contains(".") &&
                !resourcePath.startsWith("ws")) {
                return new ClassPathResource("/static/index.html");
            }
            return null;
        }
    }
}
