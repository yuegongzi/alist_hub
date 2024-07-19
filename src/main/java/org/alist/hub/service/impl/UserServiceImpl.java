package org.alist.hub.service.impl;

import org.alist.hub.model.User;
import org.alist.hub.repository.UserRepository;
import org.alist.hub.service.UserService;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl extends GenericServiceImpl<User, Integer> implements UserService {
    public UserServiceImpl(UserRepository repository) {
        super(repository);
    }

    @Override
    public Optional<User> findByUsername(String name) {
        User user = new User();
        user.setUsername(name);
        return findOne(Example.of(user));
    }
}
