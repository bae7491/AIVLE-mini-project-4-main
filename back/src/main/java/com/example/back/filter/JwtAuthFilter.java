package com.example.back.filter;

import com.example.back.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    @SuppressWarnings("null")
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {  
            filterChain.doFilter(request, response);                   
            return;                                                     
        }

        String token = authHeader.substring(7);

        try {
            jwtUtil.validateToken(token);
            String userId = jwtUtil.getUserId(token);
            log.info("[JwtAuthFilter] 추출된 userId={}", userId);

            request.setAttribute("userId", userId);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of()); // 수정

            SecurityContextHolder.getContext().setAuthentication(authentication);   // 수정
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"유효하지 않은 토큰입니다.\"}");
            return;
        }

        log.info("필터에서 JWT검사 완료");
        filterChain.doFilter(request, response);
    }
}
