//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hyzs.dog.gateway.filter;

import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CachedBodyOutputMessage implements ReactiveHttpOutputMessage {
    private final DataBufferFactory bufferFactory;
    private final HttpHeaders httpHeaders;
    private Flux<DataBuffer> body = Flux.error(new IllegalStateException("The body is not set. Did handling complete with success?"));

    CachedBodyOutputMessage(ServerWebExchange exchange, HttpHeaders httpHeaders) {
        this.bufferFactory = exchange.getResponse().bufferFactory();
        this.httpHeaders = httpHeaders;
    }

    @Override
    public void beforeCommit(Supplier<? extends Mono<Void>> action) {
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.httpHeaders;
    }

    @Override
    public DataBufferFactory bufferFactory() {
        return this.bufferFactory;
    }

    public Flux<DataBuffer> getBody() {
        return this.body;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        this.body = Flux.from(body);
        return Mono.empty();
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return this.writeWith(Flux.from(body).flatMap((p) -> {
            return p;
        }));
    }

    @Override
    public Mono<Void> setComplete() {
        return this.writeWith(Flux.empty());
    }
}
