package com.thetablock.thetaport.repositories;

import com.google.common.collect.Multimap;
import com.thetablock.thetaport.entities.PortState;
import com.thetablock.thetaport.enums.EnumPortState;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TimerRepository {

    boolean addActivePlayer(String zoneName, Player player, EnumPortState enumPortState);

    Map<UUID, PortState> tempWarpMaps();

    List<PortState> getActivePlayers(String warpName);

    void removePlayers(String name, List<Player> uuidList);


    void removePlayer(UUID uuid);

    boolean stopTask(String taskName);

    void addTempDisabled(UUID uuid, String warpName);

    boolean invalidate(UUID uuid);

    boolean isDisabled(UUID uuid);

    boolean hasPlayer(UUID uuid);

    PortState getPlayer(UUID uuid);
}
