package org.example.vtb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Разрешаем запросы с фронтенда
        config.addAllowedOrigin("https://vtb-forum.onrender.com"); // URL вашего Nuxt приложения

        // Разрешаем все HTTP методы
        config.addAllowedMethod("*");

        // Разрешаем все заголовки
        config.addAllowedHeader("*");

        // Разрешаем передачу куки и заголовков авторизации
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
