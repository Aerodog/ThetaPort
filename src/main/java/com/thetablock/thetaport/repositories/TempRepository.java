package com.thetablock.thetaport.repositories;

import com.google.common.collect.ImmutableMap;
import com.thetablock.thetaport.entities.PortState;
import com.thetablock.thetaport.entities.Core;
import com.thetablock.thetaport.enums.EnumPortState;
import org.apache.commons.cli.CommandLine;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TempRepository {

    boolean addActivePlayer(String zoneName, Player player, EnumPortState enumPortState);

    Map<UUID, PortState> tempWarpMaps();

    List<PortState> getActivePlayers(String warpName);

    void removePlayers(String name, List<Player> uuidList);

    void removePlayer(UUID uuid);

    boolean hasPlayer(UUID uuid);

    PortState getPlayer(UUID uuid);

    Core getTempPort(UUID uuid);

    void addTempPort(UUID uuid, Core core);

    void invalidateTempPort(UUID uuid);

    boolean hasTempPlayer(UUID uniqueId);

    org.apache.commons.cli.CommandLine getUserArgs(UUID uniqueId);

    void addCommandArgs(UUID uuid, CommandLine cmdLine);

    void addSelected(UUID uuid, String selected);

    ImmutableMap<UUID, String> getSelectedMap();
}
