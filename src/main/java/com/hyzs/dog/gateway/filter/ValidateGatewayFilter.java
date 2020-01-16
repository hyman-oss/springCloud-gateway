package com.hyzs.dog.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.hyzs.dog.gateway.bo.ResponseVO;
import com.hyzs.dog.gateway.bo.VerifyBO;
import com.hyzs.dog.gateway.util.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author lihaibo
 * @date 2019/11/27
 */
@Slf4j
@Component
public class ValidateGatewayFilter implements GatewayFilter, Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put("startTime", System.currentTimeMillis());
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String authorization = headers.getFirst("authorization");
        String signValue= headers.getFirst("sign");
        VerifyBO rb = new VerifyBO();
        rb.setMethod(request.getMethodValue().toLowerCase());
        rb.setUri(request.getURI().getPath());
        rb.setTimestamp(headers.getFirst("timestamp"));
        rb.setNonce(headers.getFirst("nonce"));
        rb.setAuthorization(authorization);
        //POST请求 获取body 并重写request
        if (request.getMethod().equals(HttpMethod.POST)) {
            ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
            MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
            Mono<String> modifiedBody = serverRequest.bodyToMono(String.class).flatMap(body -> {
                //仅支持JSON
                if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType) || MediaType.APPLICATION_JSON_UTF8.isCompatibleWith(mediaType)) {
                    rb.setRequestData(body);
                    String sign = MD5Utils.encrypt(MD5Utils.encrypt(rb.toString()));
                    if(sign.equals(signValue)){
                        return Mono.just(body);
                    }
                }
                return Mono.error(new RuntimeException("verify signature failed!"));
            });
            BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
            HttpHeaders newHeader = new HttpHeaders();
            newHeader.putAll(exchange.getRequest().getHeaders());
            newHeader.remove("Content-Length");
            CachedBodyOutputMessage myOutputMessage = new CachedBodyOutputMessage(exchange, newHeader);
            return bodyInserter.insert(myOutputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
                ServerHttpRequest decorator = this.decorate(exchange, newHeader, myOutputMessage);
                return returnMono(chain, exchange.mutate().request(decorator).build());
            }));
        }
        //Get请求
        else if(request.getMethod().equals(HttpMethod.GET)){
            MultiValueMap queryParams = request.getQueryParams();
            rb.setQueryString(queryParams.toSingleValueMap().toString().replace(",","&").
                    replace("{","").replace("}","").replace(" ",""));
            String sign = MD5Utils.encrypt(MD5Utils.encrypt(rb.toString()));
            if(!sign.equals(signValue)){
                throw new RuntimeException("verify signature failed!");
            }
        }
        return returnMono(chain,exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 后处理
     * @param chain
     * @param exchange
     * @return
     */
    private Mono<Void> returnMono(GatewayFilterChain chain,ServerWebExchange exchange){
        return chain.filter(exchange).then(Mono.fromRunnable(()->{
            Long startTime = exchange.getAttribute("startTime");
            String userToken = exchange.getRequest().getHeaders().getFirst("authorization");
            if(userToken != null){
                redisTemplate.expire(userToken,30, TimeUnit.MINUTES);
            }
            if (startTime != null){
                long executeTime = (System.currentTimeMillis() - startTime);
                log.info("耗时：{}ms" , executeTime);
                log.info("状态码：{}" , Objects.requireNonNull(exchange.getResponse().getStatusCode()).value());
            }
        }));
    }

    /**
     * 请求构造器
     * @param exchange
     * @param header
     * @param outputMessage
     * @return
     */
    ServerHttpRequestDecorator decorate(ServerWebExchange exchange, HttpHeaders header, CachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = header.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                if (contentLength > 0L) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set("Transfer-Encoding", "chunked");
                }
                return httpHeaders;
            }
            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
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