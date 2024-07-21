package org.alist.hub.repository;

import org.alist.hub.model.SearchNode;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SearchNodeRepository extends AListHubRepository<SearchNode, Long> {

    @Modifying
    @Query("DELETE SearchNode s  WHERE s.type = :type")
    void deleteByType(Integer type);

    @Modifying
    @Query("DELETE FROM SearchNode s WHERE s.id NOT IN (SELECT min(id) AS id FROM SearchNode GROUP BY  parent,name)")
    void removeDuplicate();
}
