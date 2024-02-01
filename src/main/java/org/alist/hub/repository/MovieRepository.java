package org.alist.hub.repository;

import org.alist.hub.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MovieRepository extends CrudRepository<Movie, Long>, PagingAndSortingRepository<Movie, Long>, JpaRepository<Movie, Long> {


}
