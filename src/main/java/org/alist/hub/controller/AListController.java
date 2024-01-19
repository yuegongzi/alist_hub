package org.alist.hub.controller;

import lombok.AllArgsConstructor;
import org.alist.hub.utils.AListUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alist")
@AllArgsConstructor
public class AListController {

    @PostMapping("/start")
    public boolean start() {
        return AListUtil.start();
    }
}
