package reel.ru.GatewayService.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class DemoGatewayFilter implements GatewayFilterFactory<DemoGatewayFilter.Config> {

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("====" + exchange.getRequest().getHeaders().get("X-Forwarded-For"));
            return chain.filter(exchange);
        };
    }

    @Override
    public Config newConfig() {
        return new Config("DemoGatewayFilter");
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