package com.hyzs.dog.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.hyzs.dog.gateway.bo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author lihaibo
 * @date 2019/11/27
 */
@Slf4j
@Component
public class LogoutGatewayFilter implements GatewayFilter, Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = request.getHeaders();
        String authorization = headers.getFirst("authorization");
        if(StringUtils.isEmpty(authorization)){
            return chain.filter(exchange);
        }
        //user login timeout (only for dog)
        String userInfo = redisTemplate.opsForValue().get(authorization);
        if(StringUtils.isEmpty(userInfo)){
            ResponseVO vo = new ResponseVO();
            vo.setCode(5011);
            vo.setMsg("Login has expired or user not exist!");
            return reWriteResponse(vo,response);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 重写响应体
     * @param vo
     * @param response
     * @return
     */
    public Mono<Void>  reWriteResponse(ResponseVO vo,ServerHttpResponse response){
        byte[] bytes = JSONObject.toJSONString(vo).getBytes(StandardCharsets.UTF_8);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8);
        DataBuffer buffer=  response.bufferFactory().wrap(bytes);
        return response.writeWith(Flux.just(buffer));
    }
}