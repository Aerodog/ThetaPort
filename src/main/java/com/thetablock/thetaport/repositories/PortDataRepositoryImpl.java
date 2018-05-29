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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.FileUtil;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Singleton
public class PortDataRepositoryImpl implements PortDataRepository {
    private File file = Bukkit.getPluginManager().getPlugin("ThetaPort").getDataFolder();
    private LoadingCache<String, PortData> warpDataMap = CacheBuilder.newBuilder()
           // .concurrencyLevel(4)
            .build(new CacheLoader<String, PortData>() {
                @Override
                public PortData load(String key) {
                    return getWarpData(key);
                }
            });

    public PortDataRepositoryImpl() {
        File tempFile = new File(file + "/ports");

        if (!tempFile.exists()) {
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != tempFile.listFiles()) {
            Arrays.stream(tempFile.listFiles())
                    .filter(s->!s.getAbsolutePath().contains(".ser"))
                    .forEach(f -> {
                //   PortData portData = gson.fromJson(f, PortData.class);
                    ObjectMapper mapper = new ObjectMapper();
                        try {
                    PortData portData = mapper.readValue(f, PortData.class);
                    portData.setLastPortTime(LocalDateTime.now());
                    Path path = Paths.get(file + "/ports/" + portData.getName() + ".ser");

                    if (Files.exists(path)) {
                       String content = new String(Files.readAllBytes(path));
                       portData.setRequiredItem(itemFrom64(content));
                    }
                    warpDataMap.put(portData.getName(), portData);
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                    System.out.println("An error has occurred while trying to load ports.");
                }
            });
            System.out.println("[INFO] ThetaPort has successfully loaded " + warpDataMap.size() + " ports.");
        }
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
            mapper.writeValue(new FileWriter(file + "/ports/" + portData.getName() + ".json"), portData);
            if (portData.getRequiredItem() != null) {
                Path path = Paths.get(file + "/ports/" + portData.getName() + ".ser");
                String serialized = serializeItem(portData.getRequiredItem());
                Files.write(path, Collections.singleton(serialized));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String serializeItem(ItemStack stack) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(stack);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        }
        catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    private static ItemStack itemFrom64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                return (ItemStack) dataInput.readObject();
            }
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    @Override
    public boolean warpDataExists(String warpData) {
        return warpDataMap.asMap().entrySet().stream()
                .anyMatch(m -> m.equals(warpData));
    }

    @Override
    public boolean createNewWarp(PortData warp, boolean save) {
        warpDataMap.put(warp.getName(), warp);
        System.out.println(warp);
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
    public void softUpdate(PortData portData) {
        warpDataMap.invalidate(portData.getName());
        warpDataMap.put(portData.getName(), portData);
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
