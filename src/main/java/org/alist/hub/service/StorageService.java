package org.alist.hub.service;


import org.alist.hub.model.Storage;

public interface StorageService {
    void resetStorage();

    void removeExpire();

    /**
     * 刷新数据
     *
     * @param storage 存储
     */
    void flush(Storage storage);

}
