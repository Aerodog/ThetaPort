package com.thetablock.thetaport.repositories;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;

import com.google.inject.Singleton;
import com.thetablock.thetaport.entities.PortState;
import com.thetablock.thetaport.enums.EnumPortState;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Singleton
public class TimerRepositoryImpl implements TimerRepository {
    private Map<UUID, PortState> playerPortState = new HashMap<>();
    private Map<String, ScheduledFuture> registeredTasks = new HashMap<>();
    private Cache<UUID, String> tempDisableWarp = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MILLISECONDS)
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
    public boolean stopTask(String taskName) {
        return registeredTasks.get(taskName).cancel(true);
    }

    @Override
    public void addTempDisabled(UUID uuid, String warpName) {
        tempDisableWarp.put(uuid, warpName);
    }

    @Override
    public boolean invalidate(UUID uuid) {
        tempDisableWarp.invalidate(uuid);
        return false;
    }

    @Override
    public boolean isDisabled(UUID uuid) {
        return tempDisableWarp.asMap().containsKey(uuid);
    }

    @Override
    public boolean hasPlayer(UUID uuid) {
        return playerPortState.containsKey(uuid);
    }

    @Override
    public PortState getPlayer(UUID uuid) {
        return playerPortState.get(uuid);
    }


}
