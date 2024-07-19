package org.alist.hub.controller;

import lombok.AllArgsConstructor;
import org.alist.hub.bean.Query;
import org.alist.hub.model.SearchNode;
import org.alist.hub.repository.Condition;
import org.alist.hub.service.SearchNodeService;
import org.alist.hub.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/search_node")
public class SearchNodeController {
    private final SearchNodeService searchNodeService;

    @GetMapping
    public Page<SearchNode> get(@RequestParam("s") String s, Query query) {
        List<Condition> conditions = Collections.singletonList(
                Condition.of((root, cb) -> cb.like(root.get("name"), "%" + s + "%"), StringUtils.hasText(s))
        );
        return searchNodeService.findAll(SearchNode.class, conditions, query.of(SearchNode.class));
    }
}
