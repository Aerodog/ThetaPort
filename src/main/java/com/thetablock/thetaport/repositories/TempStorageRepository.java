package com.thetablock.thetaport.repositories;

import com.thetablock.thetaport.entities.PortLoc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface  TempStorageRepository {
    int getCount(UUID uuid);

    int addEntry(UUID uuid, PortLoc portLoc);

    boolean invalidate(UUID uuid);

    List<PortLoc> getTempPoints(UUID uuid);

    List<PortLoc> getTempPoints();

    void removeEntries(UUID uuid);

    Map<UUID, String> getTempDisabledList();

    boolean put(UUID uuid, String warpName);

    boolean deleteTempWarp(UUID uuid);

    String getTempDisabledPort(UUID uuid);
}
