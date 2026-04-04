package reel.ru.GatewayService.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminFilter implements GatewayFilterFactory<AdminFilter.Config> {

    @Override
    public GatewayFilter apply(AdminFilter.Config config) {
        return (exchange, chain) -> {
            List<String> values = exchange.getRequest().getHeaders().get("X-Role");
            if(values == null || values.isEmpty()) {
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return exchange.getResponse().setComplete();
            } else {
                String role = values.getFirst();
                if(role.equalsIgnoreCase("ADMIN")) {
                    return chain.filter(exchange);
                } else {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
        };
    }

    @Override
    public AdminFilter.Config newConfig() {
        return new AdminFilter.Config("AdminFilter");
    }

    @Override
    public Class<AdminFilter.Config> getConfigClass() {
        return AdminFilter.Config.class;
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
