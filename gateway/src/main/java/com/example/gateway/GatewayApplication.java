package com.example.gateway;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class GatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, args);
  }

  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
        .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
        .timeLimiterConfig(
            TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build())
        .build());
  }

  @Bean
  KeyResolver userKeyResolver() {
    return exchange -> Mono.just("1");
  }

  @Bean
  public RouteLocator routes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("modify_request_body", r -> r.path("/post/**")
            .filters(f -> f.modifyRequestBody(
                String.class, Msg.class, MediaType.APPLICATION_JSON_VALUE,
                (exchange, s) -> Mono.just(new Msg(s.toUpperCase()))))
            .uri("https://httpbin.org"))
        .build();
  }

  static class Msg {

    String msg;

    public Msg() {
    }

    public Msg(String msg) {
      this.msg = msg;
    }

    public String getMsg() {
      return msg;
    }

    public void setMsg(String msg) {
    }
  }
}

record Health(String status) {

}

@RestController
@RequestMapping("/fallback")
class GatewayFallback {

  @GetMapping("/mockHealth")
  public Health getMockHealth() {
    return new Health("UP");
  }
}
