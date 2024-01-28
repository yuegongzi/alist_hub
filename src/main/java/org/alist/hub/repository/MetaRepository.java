package org.alist.hub.repository;

import org.alist.hub.model.Meta;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MetaRepository extends CrudRepository<Meta, Integer>, PagingAndSortingRepository<Meta, Integer> {
    int deleteByIdGreaterThan(Integer id);

    @Modifying
    @Query("UPDATE Meta s SET s.hide = :hide WHERE s.id < :id")
    int updateHideLessThan(Integer id, String hide);
}
