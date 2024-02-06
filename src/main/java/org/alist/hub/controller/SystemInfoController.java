package org.alist.hub.controller;

import lombok.AllArgsConstructor;
import org.alist.hub.bo.XiaoYaBo;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.vo.SystemInfoVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/system/info")
@AllArgsConstructor
public class SystemInfoController {
    private final AppConfigService appConfigService;

    @GetMapping
    public SystemInfoVO read() {
        Optional<XiaoYaBo> bo = appConfigService.get(new XiaoYaBo(), XiaoYaBo.class);
        SystemInfoVO vo = SystemInfoVO.read();
        vo.setVersion(bo.map(XiaoYaBo::getVersion).orElse("0"));
        return vo;
    }
}
