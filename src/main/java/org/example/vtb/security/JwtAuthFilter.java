package org.example.vtb.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("Обработка запроса: {}", path);

        // ⬅️ исключаем только публичные маршруты
        if (path.equals("/api/auth/register") || path.equals("/api/auth/login")
                || (path.startsWith("/api/faq") && request.getMethod().equals("GET"))) {
            log.info("Публичный маршрут, пропускаем аутентификацию");
            filterChain.doFilter(request, response);
            return;
        }

        // Для всех остальных маршрутов проверяем токен
        final String authHeader = request.getHeader("Authorization");
        log.info("Заголовок Authorization: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Отсутствует или неверный формат токена");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail;
        try {
            userEmail = jwtService.extractEmail(jwt);
            log.info("Извлечен email из токена: {}", userEmail);
        } catch (Exception e) {
            log.error("Ошибка при извлечении email из токена", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, ((UserDetailsImpl) userDetails).getUser())) {
                log.info("Токен валиден, устанавливаем аутентификацию");
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn("Токен невалиден");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}