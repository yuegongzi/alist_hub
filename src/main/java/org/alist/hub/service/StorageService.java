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

    void deleteByIdLessThanAndDisabled(Long id, boolean disabled);

    void deleteByIdLessThan(Long id);

    // 更新driver
    void updateDriver(String oldDriver, String driver);

    List<Storage> findAllByIdGreaterThan(Long id);
}
