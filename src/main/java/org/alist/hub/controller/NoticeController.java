package org.alist.hub.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.alist.hub.bean.ResultCode;
import org.alist.hub.bo.NoticeBO;
import org.alist.hub.dto.NoticeDTO;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.external.BarkClient;
import org.alist.hub.service.AppConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    private final AppConfigService appConfigService;
    private final BarkClient barkClient;

    @GetMapping
    public NoticeBO get() {
        Optional<NoticeBO> noticeBO = appConfigService.get(new NoticeBO(), NoticeBO.class);
        if (noticeBO.isEmpty()) {
            NoticeBO bo = new NoticeBO();
            appConfigService.saveOrUpdate(bo);
            return bo;
        }
        return noticeBO.get();
    }

    @PostMapping
    public void add(@RequestBody @Valid NoticeDTO notice) {
        Optional<NoticeBO> bo = appConfigService.get(new NoticeBO(), NoticeBO.class);
        NoticeBO noticeBO = new NoticeBO();
        if (bo.isPresent()) {
            noticeBO = bo.get();
        }
        noticeBO.setPushKey(notice.getPushKey());
        appConfigService.saveOrUpdate(noticeBO);
        barkClient.send(notice.getPushKey(), "通知设置成功", "测试消息通道是否通畅");
    }

    @PutMapping
    public void update(@RequestBody NoticeBO notice) {
        Optional<NoticeBO> bo = appConfigService.get(new NoticeBO(), NoticeBO.class);
        if (bo.isEmpty()) {
            throw new ServiceException(ResultCode.NOT_FOUND);
        }
        appConfigService.saveOrUpdate(notice);
        barkClient.send(notice.getPushKey(), "通知更新成功", "测试消息通道是否通畅");
    }
}
