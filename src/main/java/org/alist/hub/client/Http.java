package org.alist.hub.client;

import lombok.AllArgsConstructor;
import org.alist.hub.bean.Response;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@Component
@AllArgsConstructor
public class Http {

    private final WebClient webClient;

    // GET请求
    public Response get(Payload payload) {
        return Response.of(webClient.get()
                .uri(payload.getUri())
                .headers(h -> h.addAll(payload.getHeaders()))
                .retrieve()
                .toEntity(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                .block(Duration.ofSeconds(30)));
    }

    // POST请求
    public Response post(Payload payload) {
        return Response.of(webClient.post()
                .uri(payload.getUri())
                .bodyValue(payload.getBody())
                .headers(h -> h.addAll(payload.getHeaders()))
                .retrieve()
                .toEntity(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                .block(Duration.ofSeconds(30))
        );
    }

    // DELETE请求
    public Response delete(Payload payload) {
        return Response.of(webClient.delete()
                .uri(payload.getUri())
                .headers(h -> h.addAll(payload.getHeaders()))
                .retrieve()
                .toEntity(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                .block(Duration.ofSeconds(30)));
    }

    // PUT请求
    public Response put(Payload payload) {
        return Response.of(webClient.put()
                .uri(payload.getUri())
                .bodyValue(payload.getBody())
                .headers(h -> h.addAll(payload.getHeaders()))
                .retrieve()
                .toEntity(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                .block(Duration.ofSeconds(30)));
    }

    // 下载文件
    public Mono<Void> downloadFile(Payload payload, String targetPath) {
        Flux<DataBuffer> dataBuffer = webClient.get()
                .uri(payload.getUri())
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)));
        try {
            Path path = Paths.get(targetPath);
            DataBufferUtils.write(dataBuffer, path)
                    .block(Duration.ofSeconds(60));
            Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write chunk to file", e);
        }
        return Mono.empty().then();
    }
}
