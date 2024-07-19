package org.alist.hub.service.impl;

import org.alist.hub.model.Meta;
import org.alist.hub.repository.MetaRepository;
import org.alist.hub.service.MetaService;
import org.springframework.stereotype.Service;

@Service
public class MetaServiceImpl extends GenericServiceImpl<Meta, Integer> implements MetaService {
    private final MetaRepository repository;

    public MetaServiceImpl(MetaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public void updateHideLessThan(Integer id, String hide) {
        repository.updateHideLessThan(id, hide);
    }
}
