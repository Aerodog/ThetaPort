package com.thetablock.thetaport.repositories;


import com.thetablock.thetaport.entities.PortData;

import java.util.List;
import java.util.Map;

public interface PortDataRepository {
    boolean warpDataExists(String warpData);

    boolean createNewWarp(PortData warp, boolean save);

    boolean deleteWarp(String warp);

    PortData getWarp(String warpName);

    List<PortData> getWarpData(int page);

    Map<String, PortData> getWarpData();

    void softUpdate(PortData portData);

    void update(PortData... firstPoint);

    int loadAll();
}
