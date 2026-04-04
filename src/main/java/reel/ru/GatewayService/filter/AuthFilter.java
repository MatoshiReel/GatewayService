package reel.ru.GatewayService.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Component
public class AuthFilter implements GatewayFilterFactory<AuthFilter.Config> {
    private final PublicKey publicKey;

    public AuthFilter(@Value("${RSA_JWT_PUBLIC_KEY}") String base64PublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey));
        this.publicKey = keyFactory.generatePublic(keySpec);
    }

    @Override
    public GatewayFilter apply(AuthFilter.Config config) {
        return (exchange, chain) -> {
            List<String> values = exchange.getRequest().getHeaders().get("Authorization");
            if(values == null || values.isEmpty()) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            String token = values.getFirst().replaceAll("Bearer ", "");
            try {
                JwtParser parser = Jwts.parser().verifyWith(publicKey).build();
                Claims claims = parser.parseSignedClaims(token).getPayload();
                String accountId = claims.getSubject();
                String accountRole = (String) claims.get("role");
                ServerWebExchange mutatedExchange = exchange.mutate().request(exchange.getRequest().mutate().header("X-Account-Id", accountId).header("X-Role", accountRole).build()).build();
                return chain.filter(mutatedExchange);
            } catch (JwtException e) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        };
    }

    @Override
    public Config newConfig() {
        return new Config("AuthFilter");
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Setter
    @Getter
    public static class Config {

        public Config(String name){
            this.name = name;
        }
        private String name;

    }
}
