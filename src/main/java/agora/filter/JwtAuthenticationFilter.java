package agora.filter;

import agora.exceptions.AgoraAuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import agora.security.JwtHandler;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Причина отказа в токене; её отдаёт клиенту JwtAuthenticationEntryPoint
    public static final String AUTH_ERROR_ATTRIBUTE = "agora.auth.error";

    private final JwtHandler jwtHandler;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorization.substring(7);

            if (!jwtHandler.isAccessToken(token)) {
                throw new AgoraAuthException("Invalid token type. Access token required.");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String userName = jwtHandler.getUsernameFromToken(token);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userName);

                UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.authenticated(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception ex) {
            // Невалидный токен не обрывает запрос: permitAll-эндпоинты пройдут,
            // защищённые получат 401 от JwtAuthenticationEntryPoint
            SecurityContextHolder.clearContext();
            request.setAttribute(AUTH_ERROR_ATTRIBUTE, ex.getMessage());
            log.debug("JWT отклонён для {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
