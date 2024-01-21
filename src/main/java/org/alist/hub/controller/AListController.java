package org.alist.hub.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.dto.SettingDTO;
import org.alist.hub.service.AListService;
import org.alist.hub.service.AppConfigService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/alist")
@AllArgsConstructor
public class AListController {
    private final AppConfigService appConfigService;
    private final AListService aListService;

    @PostMapping("/start")
    public boolean start() {
        return aListService.startAList();
    }

    @PostMapping("/stop")
    public boolean stop() {
        return aListService.stopAList();
    }

    @PostMapping("/setting")
    @SneakyThrows
    public boolean setting(@RequestBody @Valid SettingDTO setting) {
        if (appConfigService.saveOrUpdate(setting)) {
            if (!appConfigService.isInitialized()) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        aListService.initialize();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }).start();
            }
            return true;
        }
        return false;
    }
}
