package com.thetablock.thetaport.repositories;

import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public interface TimerRepositories {

    boolean addActivePlayers(String zoneName, List<Player> playerList);

    boolean addActivePlayer(String zoneName, Player player);

    Collection<Player> getActivePlayers(String warpName);

    Collection<Player> removePlayers(String name, List<Player> players);

    Multimap<String, Player> getActivePlayers();

    void removePlayer(String name, Player v);

    void registerTask(String taskName, ScheduledFuture scheduledFuture);

    boolean stopTask(String taskName);
}
