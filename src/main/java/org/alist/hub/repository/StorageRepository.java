package org.alist.hub.repository;

import org.alist.hub.model.Storage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface StorageRepository extends CrudRepository<Storage, Long>, PagingAndSortingRepository<Storage, Long>, JpaRepository<Storage, Long> {

    List<Storage> findAllByDriver(String driver);

    @Query("SELECT e FROM Storage e WHERE (:mountPath IS NULL OR e.mountPath LIKE CONCAT('%', :mountPath, '%'))")
    Page<Storage> findAllByMountPathContaining(String mountPath, Pageable pageable);

    Optional<Storage> findByMountPath(String mount_path);

    int deleteByIdLessThanAndDisabled(Long id, boolean disabled);
    int deleteByIdLessThan(Long id);

    // 更新driver
    @Modifying
    @Query("UPDATE Storage s SET s.driver = :driver WHERE s.driver = :oldDriver")
    int updateDriver(String oldDriver, String driver);

    List<Storage> findAllByIdGreaterThan(Long id);
}
