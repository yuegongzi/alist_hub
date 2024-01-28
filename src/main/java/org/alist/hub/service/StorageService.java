package org.alist.hub.service;


import org.alist.hub.model.Storage;

public interface StorageService {
    void updateAliYunDrive();

    void setAliAddition(Storage... storages);

    void setPikPakAddition(Storage... storages);

    void removeAll();

    boolean getMyAli();

    void updateMyAli(boolean disabled);

    void updatePikPak();
}
