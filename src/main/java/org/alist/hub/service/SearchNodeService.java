package org.alist.hub.service;

import org.alist.hub.model.SearchNode;

public interface SearchNodeService extends GenericService<SearchNode, Long> {

    void build();

    void update();

}
