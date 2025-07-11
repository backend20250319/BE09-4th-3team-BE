package io.fundy.fundyserver.register.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${frontend.url}")
    private String frontendUrl;

    // 정적 리소스 매핑용 상수
//    private static final String PROFILE_IMAGE_PATH = "file:///C:/profile_images/";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(frontendUrl)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // 정적 리소스 매핑 추가 주석 처리
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    }
}
