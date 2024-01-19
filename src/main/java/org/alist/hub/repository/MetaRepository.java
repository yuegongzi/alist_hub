package org.alist.hub.repository;

import org.alist.hub.model.Meta;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MetaRepository extends CrudRepository<Meta, Integer>, PagingAndSortingRepository<Meta, Integer> {
}
