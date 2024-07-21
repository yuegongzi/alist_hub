package org.alist.hub.service;


import org.alist.hub.model.Storage;

import java.util.List;

public interface StorageService extends GenericService<Storage, Long> {
    void resetStorage();

    void removeExpire();

    /**
     * 刷新数据
     *
     * @param storage 存储
     */
    void flush(Storage storage);

    List<Storage> findAllByDriver(String driver);

    List<Storage> findAllByIdGreaterThan(Long id);
}
