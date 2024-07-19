package org.alist.hub.service;

import org.alist.hub.model.User;

import java.util.Optional;

public interface UserService extends GenericService<User, Integer> {
    Optional<User> findByUsername(String name);
}
