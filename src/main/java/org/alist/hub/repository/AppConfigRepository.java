package org.alist.hub.repository;

import org.alist.hub.model.AppConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AppConfigRepository extends CrudRepository<AppConfig, Integer>, PagingAndSortingRepository<AppConfig, Integer> {
    AppConfig findByLabelAndGroup(String label, Integer Group);
}
