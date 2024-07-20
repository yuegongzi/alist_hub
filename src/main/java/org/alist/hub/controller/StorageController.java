package org.alist.hub.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.alist.hub.bean.Query;
import org.alist.hub.model.Storage;
import org.alist.hub.service.StorageService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage")
@AllArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @GetMapping
    public Page<Storage> get(Storage storage, Query query) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("mountPath", ExampleMatcher.GenericPropertyMatchers.contains());
        return storageService.findAll(Example.of(storage, matcher), query.of(Storage.class));
    }

    @PostMapping
    public void add(@RequestBody @Valid Storage storage) {
        storage.build();
        storageService.flush(storage);
    }
}
