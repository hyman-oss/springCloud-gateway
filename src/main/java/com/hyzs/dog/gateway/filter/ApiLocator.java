package com.hyzs.dog.gateway.filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 转发路由配置
 * @author Hua-cloud
 */
@Slf4j
@Component
public class ApiLocator {

    @Autowired
    private ValidateGatewayFilter validateGatewayFilter;

    @Autowired
    private LogoutGatewayFilter logoutGatewayFilter;

    /**
     * 验签路由转发
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator signRouter(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        RouteLocatorBuilder.Builder serviceProvider = routes
            .route("business",
                r -> r
                    .path("/dog-business/**")
                    .filters(f -> {
                        f.filter(validateGatewayFilter);
                        f.filter(logoutGatewayFilter);
                        f.hystrix(config -> {
                            config.setName("hystrixCommand");
                            config.setFallbackUri("forward:/fallback");
                        });
                        return f;
                    })
                    .uri("lb://dog-business"));
        return serviceProvider.build();
    }

    /**
     * 常规路由转发
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator normalRouter(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        RouteLocatorBuilder.Builder serviceProvider = routes
                .route("file",r -> r
                        .path("/file-upload-service/**")
                        .filters(f -> {
                            f.hystrix(config -> {
                                config.setName("hystrixCommand");
                                config.setFallbackUri("forward:/fallback");
                            });
                            return f;
                        })
                        .uri("lb://FILE-UPLOAD-SERVICE"));
        return serviceProvider.build();
    }

}
