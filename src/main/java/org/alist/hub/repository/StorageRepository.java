package org.alist.hub.repository;

import org.alist.hub.model.Storage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends CrudRepository<Storage, Integer>, PagingAndSortingRepository<Storage, Integer> {
}
