package com.thetablock.thetaport.repositories;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.thetablock.thetaport.entities.PortLoc;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class TempStorageImpl implements TempStorageRepository {
    private Multimap<UUID, PortLoc> warpLocList = ArrayListMultimap.create();
    private Cache<UUID, String> tempDisableWarp = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();
    private List<UUID> overrideProtocols = new ArrayList<>();

    @Override
    public int getCount(UUID uuid) {
       return warpLocList.get(uuid).size();
    }

    @Override
    public int addEntry(UUID uuid, PortLoc point2D) {
        warpLocList.put(uuid, point2D);
        return 0;
    }

    @Override
    public boolean invalidate(UUID uuid) {
       warpLocList.removeAll(uuid);
        return true;
    }

    @Override
    public List<PortLoc> getTempPoints(UUID uuid) {
        return new ArrayList<>(warpLocList.get(uuid));
    }

    @Override
    public List<PortLoc> getTempPoints() {
        return new ArrayList<>(warpLocList.values());
    }

    @Override
    public void removeEntries(UUID uuid) {
        warpLocList.removeAll(uuid);
    }


    @Override
    public Cache<UUID, String> getTempDisabledList() {
        return tempDisableWarp;
    }

    @Override
    public boolean put(UUID uuid, String warpName) {
        tempDisableWarp.put(uuid, warpName);
        return true;
    }

    @Override
    public boolean deleteTempWarp(UUID uuid) {
        tempDisableWarp.invalidate(uuid);
        return true;
    }

    @Override
    public String getTempDisabledPort(UUID uuid) {
        return tempDisableWarp.getIfPresent(uuid);
    }
}
