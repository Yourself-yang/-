package com.supplychain.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret:SupplyChainSecretKey2026ForGraduationProject!!}")
    private String secret;

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/actuator",
            "/v3/api-docs",
            "/swagger",
            "/webjars",
            "/favicon.ico");

    public JwtAuthFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单放行
        if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "未登录或Token缺失");
        }
        String token = authHeader.substring(7);

        // Redis 黑名单
        return redisTemplate.hasKey("token:blacklist:" + token)
                .defaultIfEmpty(false)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return unauthorized(exchange, "Token已失效，请重新登录");
                    }
                    try {
                        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
                        String userId = claims.getSubject();
                        String username = claims.get("username", String.class);
                        String role = claims.get("role", String.class);
                        // 将用户信息写入请求头，传递到下游服务
                        ServerHttpRequest mutated = request.mutate()
                                .header("X-User-Id", userId)
                                .header("X-Username", username)
                                .header("X-Role", role)
                                .build();
                        return chain.filter(exchange.mutate().request(mutated).build());
                    } catch (Exception e) {
                        log.warn("JWT校验失败: {}", e.getMessage());
                        return unauthorized(exchange, "Token无效或已过期");
                    }
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body;
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 401);
            result.put("message", msg);
            result.put("data", null);
            body = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            body = "{\"code\":401,\"message\":\"" + msg + "\"}";
        }
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
