package org.alist.hub.repository;

import org.alist.hub.model.SearchNode;

import java.util.List;

public interface SearchNodeRepository extends AListHubRepository<SearchNode, Long> {

    void deleteByType(Integer type);

    List<SearchNode> findByNameAndParent(String name, String parent);
}
