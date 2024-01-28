package org.alist.hub.controller;

import lombok.AllArgsConstructor;
import org.alist.hub.service.SearchNodeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/search_node")
public class SearchNodeController {
    private final SearchNodeService searchNodeService;

    @PostMapping("/build")
    public void build() {
        new Thread(searchNodeService::build).start();
    }
}
