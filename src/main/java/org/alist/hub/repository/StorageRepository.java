package org.alist.hub.repository;

import org.alist.hub.model.Storage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface StorageRepository extends AListHubRepository<Storage, Long> {

    List<Storage> findAllByDriver(String driver);


    void deleteByIdLessThanAndDisabled(Long id, boolean disabled);

    void deleteByIdLessThan(Long id);

    // 更新driver
    @Modifying
    @Query("UPDATE Storage s SET s.driver = :driver WHERE s.driver = :oldDriver")
    void updateDriver(String oldDriver, String driver);

    List<Storage> findAllByIdGreaterThan(Long id);
}
