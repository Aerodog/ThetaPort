package com.thetablock.thetaport.repositories;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.google.inject.Singleton;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.*;

@Singleton
public class TimerRepositoryImpl implements TimerRepositories {
    private Multimap<String, Player> activeWarpList = ArrayListMultimap.create();
    private Map<String, ScheduledFuture> registeredTasks = new HashMap<>();

    @Override
    public boolean addActivePlayers(String zoneName, List<Player> playerList) {
        playerList.removeAll(activeWarpList.get(zoneName));
        return activeWarpList.putAll(zoneName, playerList);
    }

    @Override
    public boolean addActivePlayer(String zoneName, Player player) {
       return activeWarpList.put(zoneName, player);
    }

    @Override
    public Collection<Player> getActivePlayers(String warpName) {
        return activeWarpList.get(warpName);
    }

    @Override
    public Collection<Player> removePlayers(String name, List<Player> players) {
        return activeWarpList.removeAll(players);
    }

    @Override
    public Multimap<String, Player> getActivePlayers() {
        return activeWarpList;
    }

    @Override
    public void removePlayer(String name, Player v) {
        activeWarpList.remove(name, v);
    }

    @Override
    public void registerTask(String taskName, ScheduledFuture scheduledFuture) {
        registeredTasks.put(taskName, scheduledFuture);
    }

    @Override
    public boolean stopTask(String taskName) {
        return registeredTasks.get(taskName).cancel(true);
    }

}
