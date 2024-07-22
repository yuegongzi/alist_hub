package org.alist.hub.client;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
public class HttpUtil {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))//连接超时 客户端与服务器之间建立连接的超时时间
            .build();

    /**
     * 发送HTTP请求并获取响应。
     *
     * @param request HTTP请求对象，包含了请求的细节，如URL、方法、头部和请求体。
     * @return HTTP响应对象，包含了响应的状态码和主体。
     * <p>
     * 此方法尝试发送一个HTTP请求并处理可能的异常。如果请求成功发送，
     * 它将返回包含响应状态码和主体的响应对象。如果发生异常，它将记录错误
     * 信息并返回一个包含500状态码和空主体的响应对象。
     */
    private static Response sendRequest(HttpRequest request) {
        try {
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return Response.of(httpResponse);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // 表示内部服务器错误
            throw new RuntimeException("Internal Server Error");
        }
    }


    /**
     * 发送一个GET请求。
     * <p>
     * 此方法通过给定的Payload对象构建一个HTTP GET请求，并发送该请求以获取响应。
     * Payload对象包含了请求的URI、可选的请求头信息。
     *
     * @param payload 包含请求URI和可选请求头的信息。
     * @return 返回发送请求后的响应对象。
     */
    public static Response get(Payload payload) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .header("Content-Type", "application/json")
                .uri(payload.getUri());

        if (payload.getHeaders() != null) {
            payload.getHeaders().forEach(requestBuilder::header);
        }
        HttpRequest request = requestBuilder.build();
        return sendRequest(request);
    }


    /**
     * 使用POST方法发送请求。
     * <p>
     * 该方法通过构建一个请求来发送POST请求。请求的具体内容由Payload参数决定。
     * 主要用于需要向服务器提交数据的场景，如表单提交、上传文件等。
     *
     * @param payload 请求的负载数据，包含了请求的具体内容。
     * @return 返回处理请求后的响应对象，包含服务器返回的所有信息。
     */
    public static Response post(Payload payload) {
        return buildRequest(payload);
    }

    /**
     * 根据给定的Payload构建并发送HTTP请求。
     * <p>
     * 此方法用于组装HTTP请求的所有必要部分，包括请求体、URI和头部信息。
     * 根据Payload中是否包含表单数据，动态设置Content-Type头部。
     * 最后，通过调用sendRequest方法发送构建好的HTTP请求。
     *
     * @param payload 包含HTTP请求细节的Payload对象，如请求体、URI和头部信息。
     * @return 返回发送HTTP请求后的响应对象。
     */
    private static Response buildRequest(Payload payload) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(payload.getBodyString()))
                .uri(payload.getUri());
        if (payload.getHeaders() != null) {
            payload.getHeaders().forEach(requestBuilder::header);
        }
        HttpRequest request = requestBuilder.build();
        return sendRequest(request);
    }


    /**
     * 使用PUT方法发送请求。
     * <p>
     * 该方法通过构建一个请求来处理给定的有效载荷，并将其封装在一个响应对象中返回。
     * 主要用于更新已有资源。
     *
     * @param payload 请求的有效载荷，包含请求所需的数据。
     * @return 包含服务器响应的Response对象。
     */
    public static Response put(Payload payload) {
        return buildRequest(payload);
    }

    /**
     * 发送一个DELETE请求来删除资源。
     * <p>
     * 此方法构建一个DELETE类型的HTTP请求，使用提供的Payload中的URI和可选的头部信息。
     * 它首先创建一个HttpRequest的构建器，然后设置请求类型为DELETE，并指定请求的URI。
     * 如果Payload中包含头部信息，它们将被添加到请求中。
     * 最后，构建并发送请求，返回响应。
     *
     * @param payload 包含请求URI和可选头部信息的数据载体。
     * @return 返回处理请求后的响应对象。
     */
    public static Response delete(Payload payload) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .DELETE()
                .uri(payload.getUri());

        if (payload.getHeaders() != null) {
            payload.getHeaders().forEach(requestBuilder::header);
        }

        HttpRequest request = requestBuilder.build();

        return sendRequest(request);
    }


    /**
     * 下载文件。
     * 该方法通过发送HTTP GET请求来下载指定URI的文件，并将其保存到目标路径。
     *
     * @param payload    包含下载文件的URI和可选的HTTP头部信息的载体对象。
     * @param targetPath 文件保存的目标路径。
     * @throws RuntimeException 如果下载或保存文件过程中发生错误。
     */
    public static void downloadFile(Payload payload, String targetPath) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(payload.getUri())
                .timeout(Duration.ofHours(1));
        if (payload.getHeaders() != null) {
            payload.getHeaders().forEach(requestBuilder::header);
        }
        HttpRequest request = requestBuilder.build();
        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream in = response.body()) {
                // 从响应的输入流中复制数据到目标文件
                Files.copy(in, Path.of(targetPath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to write chunk to file", e);
        }
    }

    public static byte[] download(Payload payload) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(payload.getUri())
                .timeout(Duration.ofHours(1));
        if (payload.getHeaders() != null) {
            payload.getHeaders().forEach(requestBuilder::header);
        }
        HttpRequest request = requestBuilder.build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to write chunk to file", e);
        }
    }

}