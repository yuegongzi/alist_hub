package org.alist.hub.service.impl;

import org.alist.hub.model.Movie;
import org.alist.hub.repository.MovieRepository;
import org.alist.hub.service.MovieService;
import org.springframework.stereotype.Service;

@Service
public class MovieServiceImpl extends GenericServiceImpl<Movie, Long> implements MovieService {

    public MovieServiceImpl(MovieRepository repository) {
        super(repository);
    }
}
