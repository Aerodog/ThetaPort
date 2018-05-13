package com.thetablock.thetaport.repositories;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Singleton;
import com.thetablock.thetaport.entities.PortState;
import com.thetablock.thetaport.entities.Core;
import com.thetablock.thetaport.enums.EnumPortState;
import org.apache.commons.cli.CommandLine;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class TempRepositoryImpl implements TempRepository {
    private Map<UUID, PortState> playerPortState = new HashMap<>();
    private Map<UUID, Core> tempPorts = new HashMap<>();
    private Cache<UUID, CommandLine> tempArgs = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Override
    public boolean addActivePlayer(String zoneName, Player player, EnumPortState enumPortState) {
       playerPortState.put(player.getUniqueId(), new PortState(player, zoneName, enumPortState));
       return true;
    }

    @Override
    public Map<UUID, PortState> tempWarpMaps() {
        return playerPortState;
    }

    @Override
    public List<PortState> getActivePlayers(String warpName) {
        return playerPortState.values().stream()
                .filter(m->m.getWarpName().equalsIgnoreCase(warpName))
                .collect(Collectors.toList());
    }

    @Override
    public void removePlayers(String name, List<Player> uuidList) {
        uuidList.forEach(playerPortState::remove);
    }

    @Override
    public void removePlayer(UUID uuid) {
        playerPortState.remove(uuid);
    }


    @Override
    public boolean hasPlayer(UUID uuid) {
        return playerPortState.containsKey(uuid);
    }

    @Override
    public PortState getPlayer(UUID uuid) {
        return playerPortState.get(uuid);
    }

    @Override
    public Core getTempPort(UUID uuid) {
        return tempPorts.get(uuid);
    }

    @Override
    public void addTempPort(UUID uuid, Core core) {
        tempPorts.put(uuid, core);
    }

    @Override
    public void invalidateTempPort(UUID uuid) {
        tempPorts.remove(uuid);
    }

    @Override
    public boolean hasTempPlayer(UUID uniqueId) {
        return tempPorts.containsKey(uniqueId);
    }

    @Override
    public CommandLine getUserArgs(UUID uniqueId) {
        return tempArgs.getIfPresent(uniqueId);
    }

    @Override
    public void addCommandArgs(UUID uuid, CommandLine cmdLine) {
        tempArgs.put(uuid, cmdLine);
    }


}
