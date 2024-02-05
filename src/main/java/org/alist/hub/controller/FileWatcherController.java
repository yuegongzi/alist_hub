package org.alist.hub.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.alist.hub.bean.Constants;
import org.alist.hub.bean.FileWatcher;
import org.alist.hub.dto.FileWatcherDTO;
import org.alist.hub.model.AppConfig;
import org.alist.hub.model.Storage;
import org.alist.hub.repository.AppConfigRepository;
import org.alist.hub.repository.StorageRepository;
import org.alist.hub.service.FileWatcherService;
import org.alist.hub.utils.JsonUtil;
import org.alist.hub.vo.FileWatcherVO;
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
    private final AppConfigRepository appConfigRepository;
    private final StorageRepository storageRepository;

    @PostMapping
    public void post(@RequestBody @Valid FileWatcherDTO fileWatcher) {
        fileWatcherService.watch(fileWatcher.getStorageId(), fileWatcher.getPath(), fileWatcher.getFolderName());
    }

    @GetMapping
    public List<FileWatcherVO> get() {
        return toVO(appConfigRepository.findAllByGroup(Constants.WATCHER_GROUP));
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") String id) {
        appConfigRepository.deleteById(id);
    }

    private List<FileWatcherVO> toVO(List<AppConfig> appConfigs) {
        List<FileWatcherVO> list = new ArrayList<>();
        appConfigs.forEach(appConfig -> {
            FileWatcherVO fileWatcherVO = new FileWatcherVO();
            Optional<FileWatcher> watcher = JsonUtil.readValue(appConfig.getValue(), FileWatcher.class);
            if (watcher.isPresent()) {
                fileWatcherVO.setFolderName(watcher.get().getFolderName());
                fileWatcherVO.setId(appConfig.getId());
                Optional<Storage> storage = storageRepository.findById(watcher.get().getStorageId());
                storage.ifPresent(s -> fileWatcherVO.setPath(s.getMountPath() + watcher.get().getPath()));
                list.add(fileWatcherVO);
            }
        });
        return list;
    }
}
