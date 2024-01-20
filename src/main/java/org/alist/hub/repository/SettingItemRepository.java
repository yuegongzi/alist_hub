package org.alist.hub.repository;

import org.alist.hub.model.SettingItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingItemRepository extends CrudRepository<SettingItem, Integer>, PagingAndSortingRepository<SettingItem, Integer> {
    SettingItem findByKey(String key);
}
