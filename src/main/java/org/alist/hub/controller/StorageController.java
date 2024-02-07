package org.alist.hub.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.alist.hub.bean.Query;
import org.alist.hub.model.Storage;
import org.alist.hub.repository.StorageRepository;
import org.alist.hub.service.StorageService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping
    public Page<Storage> get(Storage storage, Query query) {
        return storageRepository.findAll(Example.of(storage), query.of(Storage.class));
    }

    @PostMapping
    public void add(@RequestBody @Valid Storage storage) {
        Optional<Storage> temp = storageRepository.findByMountPath(storage.getMountPath());
        temp.ifPresent(value -> storage.setId(value.getId()));
        storage.build();
        storage.setDisabled(false);
        storageService.flush(storage);
    }
}
