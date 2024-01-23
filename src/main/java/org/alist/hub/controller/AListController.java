package org.alist.hub.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.service.AListService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/alist")
@AllArgsConstructor
public class AListController {
    private final AListService aListService;

    @PostMapping("/start")
    public boolean start() {
        return aListService.startAList();
    }

    @PostMapping("/stop")
    public boolean stop() {
        return aListService.stopAList();
    }

}
