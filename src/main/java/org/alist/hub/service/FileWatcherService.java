package org.alist.hub.service;

public interface FileWatcherService {
    void watch(String path, String folderName);

    void merge(String id);
}
