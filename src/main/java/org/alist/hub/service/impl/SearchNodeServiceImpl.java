package org.alist.hub.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alist.hub.api.AListClient;
import org.alist.hub.bean.FileSystem;
import org.alist.hub.model.SearchNode;
import org.alist.hub.repository.SearchNodeRepository;
import org.alist.hub.service.SearchNodeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class SearchNodeServiceImpl implements SearchNodeService {
    private final SearchNodeRepository searchNodeRepository;
    private final AListClient aListClient;

    @Override
    public void build() {
        execute("/");
    }

    private void execute(String path) {
        List<FileSystem> list = aListClient.fs(path);
        list.forEach(fileSystem -> {
            SearchNode searchNode = new SearchNode();
            searchNode.setName(fileSystem.getName());
            searchNode.setParent(path);
            searchNode.setDir(fileSystem.isDir());
            searchNode.setSize(fileSystem.getSize());
            searchNodeRepository.saveAndFlush(searchNode);
            try {
                if (fileSystem.isDir()) {
                    Thread.sleep(500);
                    execute(path + fileSystem.getName() + "/");
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

    }
}
