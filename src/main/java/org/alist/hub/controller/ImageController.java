package org.alist.hub.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.annotation.IgnoreResponseHandler;
import org.alist.hub.bean.Constants;
import org.alist.hub.model.Movie;
import org.alist.hub.repository.MovieRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/open/movie")
@AllArgsConstructor
@Slf4j
public class ImageController {
    private final WebClient webClient;
    private final MovieRepository movieRepository;

    @GetMapping("/{id}/image")
    @IgnoreResponseHandler
    public void image(@PathVariable("id") Long id, HttpServletResponse response) {
        Optional<Movie> optionalMovie = movieRepository.findById(id);
        if (optionalMovie.isPresent()) {
            byte[] imageBytes = webClient.get()
                    .uri(optionalMovie.get().getImageUrl())
                    .header("User-Agent", Constants.USER_AGENT)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                    .block();

            if (imageBytes != null) {
                response.setContentType("image/webp");
                try {
                    response.getOutputStream().write(imageBytes);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
