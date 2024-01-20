package org.alist.hub.api;

import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@AllArgsConstructor
public class Http {

    private final WebClient webClient;

    // GET请求
    public Mono<String> get(Payload payload) {
        return webClient.get()
                .uri(payload.getUri())
                .headers(h -> h.addAll(payload.getHeaders()))
                .retrieve()
                .bodyToMono(String.class);
    }

    // POST请求
    public Mono<String> post(Payload payload) {
        return webClient.post()
                .uri(payload.getUri())
                .bodyValue(payload.getBody())
                .headers(h -> h.addAll(payload.getHeaders()))
                .retrieve()
                .bodyToMono(String.class);
    }

    // DELETE请求
    public Mono<Void> delete(Payload payload) {
        return webClient.delete()
                .uri(payload.getUri())
                .headers(h -> h.addAll(payload.getHeaders()))
                .retrieve()
                .toBodilessEntity().then();
    }

    // PUT请求
    public Mono<String> put(Payload payload) {
        return webClient.put()
                .uri(payload.getUri())
                .bodyValue(payload.getBody())
                .headers(h -> h.addAll(payload.getHeaders()))
                .retrieve()
                .bodyToMono(String.class);
    }

    // 下载文件
    public Mono<Void> downloadFile(Payload payload, String targetPath) {
        Flux<DataBuffer> dataBuffer = webClient.get()
                .uri(payload.getUri())
                .retrieve()
                .bodyToFlux(DataBuffer.class);
        try {
            Path path = Paths.get(targetPath);
            DataBufferUtils.write(dataBuffer, path)
                    .block();
            Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write chunk to file", e);
        }
        return Mono.empty().then();
    }
}
