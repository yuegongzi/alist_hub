package org.alist.hub.repository;

import org.alist.hub.model.SearchNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchNodeRepository extends CrudRepository<SearchNode, Long>, PagingAndSortingRepository<SearchNode, Long>, JpaRepository<SearchNode, Long> {

    int deleteByType(Integer type);

    @Query("SELECT e FROM SearchNode e WHERE (:keyword IS NULL OR e.name LIKE CONCAT('%', :keyword, '%' ))")
    Page<SearchNode> findAllByKeyword(String keyword, Pageable pageable);
}
