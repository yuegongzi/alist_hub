package org.alist.hub.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.alist.hub.api.AListClient;
import org.alist.hub.bean.Query;
import org.alist.hub.exception.ServiceException;
import org.alist.hub.model.Storage;
import org.alist.hub.repository.StorageRepository;
import org.alist.hub.service.AListService;
import org.alist.hub.service.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/storage")
@AllArgsConstructor
public class StorageController {
    private final StorageRepository storageRepository;
    private final StorageService storageService;
    private final AListClient aListClient;
    private final AListService aListService;

    @GetMapping
    public Page<Storage> get(Storage storage, Query query) {
        return storageRepository.findAllByMountPathContaining(storage.getMountPath(), query.of(Storage.class));
    }

    @PostMapping
    public void add(@RequestBody @Valid Storage storage) {
        Optional<Storage> temp = storageRepository.findByMountPath(storage.getMountPath());
        if (temp.isPresent()) {
            throw new ServiceException("挂载路径已存在");
        }
        switch (storage.getDriver()) {
            case "AliyundriveShare2Open":
                storage.build();
                storage.setId(System.currentTimeMillis());
                storage.setDisabled(false);
                storageService.setAliAddition(storage);
                break;
            case "PikPakShare":
                storage.build();
                storage.setId(System.currentTimeMillis());
                storage.setDisabled(false);
                storageService.setPikPakAddition(storage);
                break;
            default:
                storage.build();
                storage.setId(System.currentTimeMillis());
                storage.setDisabled(false);
                storageRepository.save(storage);
                break;
        }

    }

    @GetMapping("/load")
    public void load() {
        aListService.stopAList();
        aListService.startAList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        aListClient.delete(id);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody Storage storage) {
        if (storage.isDisabled()) {
            aListClient.disable(id);
        } else {
            aListClient.enable(id);
        }
    }
}
