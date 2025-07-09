package io.fundy.fundyserver.register.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${frontend.url}")  // application.yml에서 설정된 프론트엔드 URL을 읽어옴
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 엔드포인트에 대해
                .allowedOrigins(frontendUrl)  // application.yml에서 설정된 프론트엔드 URL을 사용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 허용할 HTTP 메서드
                .allowedHeaders("Content-Type", "Authorization")  // 허용할 헤더 설정
                .allowCredentials(true);  // 쿠키/인증 정보 허용 시 true
    }
    private static final String PROFILE_IMAGE_PATH = "file:///C:/profile_images/";
    // ★ 정적 리소스 매핑
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profile_images/**")
                .addResourceLocations(PROFILE_IMAGE_PATH);
    }
}