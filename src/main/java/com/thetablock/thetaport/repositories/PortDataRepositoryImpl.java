package com.thetablock.thetaport.repositories;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.google.inject.Singleton;
import com.thetablock.thetaport.entities.PortData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Singleton
public class PortDataRepositoryImpl implements PortDataRepository {
    private File file = Bukkit.getPluginManager().getPlugin("ThetaPort").getDataFolder();
    private LoadingCache<String, PortData> warpDataMap = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .build(new CacheLoader<String, PortData>() {
                @Override
                public PortData load(String key) {
                    return getWarpData(key);
                }
            });

    public PortDataRepositoryImpl() {
        File tempFile = new File(file + "/warps");
        Arrays.stream(tempFile.listFiles()).forEach(f -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                PortData portData = mapper.readValue(f, PortData.class);
                this.warpDataMap.put(f.getName(), portData);
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        });
    }

    private Multimap<String, Player> activeTransit = ArrayListMultimap.create();

    private PortData getWarpData(String warpName) {
        if (!warpName.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            File tempFile = new File(file + "/warps/" + warpName + ".json");

            if (tempFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    PortData portData = mapper.readValue(tempFile, PortData.class);
                    return portData;
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
        return null;
    }

    private boolean saveData(PortData portData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // gson.toJson(portData.getCeilPoint(), new FileWriter( file + "/warps/" + portData.getName() + ".json"));
            mapper.writeValue(new FileWriter(file + "/warps/" + portData.getName() + ".json"), portData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean warpDataExists(String warpData) {
        return warpDataMap.asMap().entrySet().stream()
                .anyMatch(m -> m.equals(warpData));
    }

    @Override
    public boolean createNewWarp(PortData warp, boolean save) {
        warpDataMap.put(warp.getName(), warp);
        if (save) {
            saveData(warp);
        }
        return true;
    }

    @Override
    //TODO Remove From File.
    public boolean deleteWarp(String warp) {
        warpDataMap.invalidate(warp);
        File fileToDelete = new File(file + "/warps/" + warp + ".json");
        if (fileToDelete.isFile()) {
            fileToDelete.delete();
            return false;
        }
        return true;
    }

    /**
     * @param warpName
     * @return {@link PortData}
     */
    @Override
    public PortData getWarp(String warpName) {
        try {
            return warpDataMap.get(warpName);
        } catch (ExecutionException | CacheLoader.InvalidCacheLoadException e) {
            return null;
        }
    }

    @Override
    public List<PortData> getWarpData(int page) {
        int offset = (page - 1) * 5;
        return warpDataMap.asMap().entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .limit(5)
                .skip(offset)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, PortData> getWarpData() {
        if (warpDataMap.asMap().isEmpty()) {
            return new HashMap<>();
        }
        return warpDataMap.asMap();
    }

    @Override
    public void update(PortData... firstPoint) {
        for (PortData portData : firstPoint) {
            try {
                Files.deleteIfExists(new File(file + "/" + portData.getName() + ".json").toPath());
                this.warpDataMap.invalidate(portData.getName());
                this.saveData(portData);
                this.warpDataMap.put(portData.getName(), portData);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public int loadAll() {
        return 0;
    }
}
