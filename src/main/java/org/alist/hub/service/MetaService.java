package org.alist.hub.service;

import org.alist.hub.model.Meta;

public interface MetaService extends GenericService<Meta, Integer> {
    void updateHideLessThan(Integer id, String hide);
}
