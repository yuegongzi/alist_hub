package org.alist.hub.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.bean.Query;
import org.alist.hub.model.Movie;
import org.alist.hub.repository.MovieRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movie")
@AllArgsConstructor
@Slf4j
public class MovieController {
    private final MovieRepository movieRepository;

    @GetMapping
    public Page<Movie> get(Movie movie, Query query) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains());
        return movieRepository.findAll(Example.of(movie, matcher), query.of(Movie.class));
    }

}
