package com.fxtreasury.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class RoleAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<RoleAuthGatewayFilterFactory.Config> {

    // Minimum 256-bit secret key for HMAC-SHA
    private final SecretKey key;

    public RoleAuthGatewayFilterFactory(@Value("${app.jwt.secret:EnterpriseFxTreasuryPlatformSecretKeyMustBe32BytesLong!}") String secret) {
        super(Config.class);
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static class Config {
        private String requiredRole;
        public String getRequiredRole() { return requiredRole; }
        public void setRequiredRole(String requiredRole) { this.requiredRole = requiredRole; }
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("requiredRole");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(this.key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String userRole = claims.get("role", String.class);
                if (userRole == null || !userRole.equalsIgnoreCase(config.getRequiredRole())) {
                    return onError(exchange, HttpStatus.FORBIDDEN);
                }

                return chain.filter(exchange);
            } catch (Exception e) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}