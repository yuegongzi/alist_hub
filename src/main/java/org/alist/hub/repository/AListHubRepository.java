package org.alist.hub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface AListHubRepository<T, ID extends Serializable>
        extends JpaRepository<T, ID> {

}
