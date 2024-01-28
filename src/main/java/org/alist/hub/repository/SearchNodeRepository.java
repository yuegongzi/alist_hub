package org.alist.hub.repository;

import org.alist.hub.model.SearchNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchNodeRepository extends CrudRepository<SearchNode, Long>, PagingAndSortingRepository<SearchNode, Long>, JpaRepository<SearchNode, Long> {
}
