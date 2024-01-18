package org.alist.hub.repository;

import org.alist.hub.model.SettingItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "setting_item", path = "setting_item")
public interface SettingItemRepository extends CrudRepository<SettingItem, Integer>,PagingAndSortingRepository<SettingItem, Integer> {
}
