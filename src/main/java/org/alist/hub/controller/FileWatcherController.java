package org.alist.hub.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.FileWatcher;
import org.alist.hub.dto.FileWatcherDTO;
import org.alist.hub.model.AppConfig;
import org.alist.hub.service.AppConfigService;
import org.alist.hub.service.FileWatcherService;
import org.alist.hub.util.JsonUtils;
import org.alist.hub.vo.FileWatcherVO;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/file/watcher")
@AllArgsConstructor
public class FileWatcherController {
    private final FileWatcherService fileWatcherService;
    private final AppConfigService appConfigService;

    @PostMapping
    public void post(@RequestBody @Valid FileWatcherDTO fileWatcher) {
        fileWatcherService.watch(fileWatcher.getPath(), fileWatcher.getFolderName());
    }

    @GetMapping
    public List<FileWatcherVO> get() {
        AppConfig appConfig = new AppConfig();
        appConfig.setGroup(Constants.WATCHER_GROUP);
        return toVO(appConfigService.findAll(Example.of(appConfig)));
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") String id) {
        appConfigService.deleteById(id);
    }

    private List<FileWatcherVO> toVO(List<AppConfig> appConfigs) {
        List<FileWatcherVO> list = new ArrayList<>();
        appConfigs.forEach(appConfig -> {
            FileWatcherVO fileWatcherVO = new FileWatcherVO();
            Optional<FileWatcher> watcher = JsonUtils.readValue(appConfig.getValue(), FileWatcher.class);
            if (watcher.isPresent()) {
                fileWatcherVO.setPath(watcher.get().getPath());
                fileWatcherVO.setId(appConfig.getId());
                fileWatcherVO.setFolderName(watcher.get().getFolderName());
                list.add(fileWatcherVO);
            }
        });
        return list;
    }
}
