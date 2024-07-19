package org.alist.hub.repository;

import org.alist.hub.model.Meta;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface MetaRepository extends AListHubRepository<Meta, Integer> {
    @Modifying
    @Query("UPDATE Meta s SET s.hide = :hide WHERE s.id < :id")
    void updateHideLessThan(Integer id, String hide);
}
