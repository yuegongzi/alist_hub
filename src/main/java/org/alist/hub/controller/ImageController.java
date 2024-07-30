package org.alist.hub.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.annotation.IgnoreResponseHandler;
import org.alist.hub.bean.Constants;
import org.alist.hub.client.Http;
import org.alist.hub.client.Payload;
import org.alist.hub.model.Movie;
import org.alist.hub.service.MovieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/open/movie")
@AllArgsConstructor
@Slf4j
public class ImageController {
    private final MovieService movieService;

    @GetMapping("/{id}/image")
    @IgnoreResponseHandler
    public void image(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            Optional<Movie> optionalMovie = movieService.findById(id);
            if (optionalMovie.isPresent()) {
                Payload payload = Payload.create(optionalMovie.get().getImageUrl()).addHeader("User-Agent", Constants.USER_AGENT);
                byte[] imageBytes = Http.download(payload);
                if (imageBytes != null) {
                    response.setContentType("image/webp");
                    response.getOutputStream().write(imageBytes);
                }
            }
        } catch (Exception e) {
            log.error("下载图片出错");
        }

    }
}
