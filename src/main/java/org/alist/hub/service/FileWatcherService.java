package org.alist.hub.service;

public interface FileWatcherService {
    void watch(Long storageId, String path, String folderName);

    void merge(String id);
}
