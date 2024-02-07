package org.alist.hub.controller;

import lombok.AllArgsConstructor;
import org.alist.hub.bean.Query;
import org.alist.hub.model.SearchNode;
import org.alist.hub.repository.SearchNodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/search_node")
public class SearchNodeController {
    private final SearchNodeRepository searchNodeRepository;

    @GetMapping
    public Page<SearchNode> get(@RequestParam("s") String s, Query query) {
        return searchNodeRepository.findAllByKeyword(s, query.of(SearchNode.class));
    }
}
