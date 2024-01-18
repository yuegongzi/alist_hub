package org.alist.hub.repository;

import org.alist.hub.model.Meta;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "meta", path = "meta")
public interface MetaRepository extends CrudRepository<Meta, Integer>,PagingAndSortingRepository<Meta, Integer> {
}
