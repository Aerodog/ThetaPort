package com.thetablock.thetaport.services;

import com.thetablock.thetaport.entities.PortData;
import com.thetablock.thetaport.repositories.PortDataRepository;
import com.thetablock.thetaport.repositories.TimerRepositories;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.*;

public final class EventServices {
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final PortDataRepository portDataRepository;
    private final TimerRepositories timerRepositories;

    @Inject
    public EventServices(PortDataRepository portDataRepository, TimerRepositories timerRepositories) {
        this.portDataRepository = portDataRepository;
        this.timerRepositories = timerRepositories;
    }

    public void checkIfPlayerEntersPortZone(Player player, Location toPoint) {
        executorService.submit(() -> {
            portDataRepository.getWarpData().values().stream()
                    .filter(tp -> null != tp.getLinked() && tp.getLinked().isEmpty())
                    .filter(tp -> toPoint.getWorld().getName().equals(tp.getCeilPoint().getWorld()))
                    .filter(tp -> isWithinBounds(tp, toPoint) && !timerRepositories.getActivePlayers().containsEntry(tp.getName(), player))
                    .findFirst()
                    .ifPresent(tp -> {
                        if (tp.isEnabled()) {
                            timerRepositories.addActivePlayer(tp.getName(), player);
                        } else {
                            player.sendMessage("§4Warp is disabled.");
                        }
                    });
        });
    }


    public void checkIfPlayerLeavesPortZone(Player player, Location toPoint) {
        for (Map.Entry<String, Player> entries : timerRepositories.getActivePlayers().entries()) {
            if (entries.getValue().getUniqueId().equals(player.getUniqueId())) {
                PortData portData = portDataRepository.getWarp(entries.getKey());

                portData.getFloorPoint().add(3, 0, 3);
                portData.getCeilPoint().add(3, 0, 3);
                if (!isWithinBounds(portData, toPoint)) {
                    timerRepositories.removePlayer(portData.getName(), entries.getValue());
                }
            }
        }
    }

    public void startListenerEvents() {
        portDataRepository.loadAll();
        buildRunnable();
    }

    private ScheduledFuture buildRunnable() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
        return scheduledExecutorService.scheduleAtFixedRate(() -> {
            portDataRepository.getWarpData().values().stream()
                    .filter(wd ->
                            wd.getLastPortTime().plusSeconds(wd.getWarpOffset()).isBefore(LocalTime.now()) &&
                                    null != wd.getLinked() && !wd.getLinked().isEmpty())
                    .forEach(wd -> {
                        wd.setLastPortTime(LocalTime.now());
                        portDataRepository.update(wd);

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getWorld().getName().equals(wd.getFloorPoint().getWorld())) {
                                if (isWithinBounds(wd, player.getLocation())) {
                                    player.teleport(wd.getWarpToPoint().getLocation());
                                    player.sendMessage(wd.getArrivalMessage());
                                }
                            }
                        }
                    });
        }, 1000L, 1000L, TimeUnit.MILLISECONDS);
    }

    private boolean isWithinBounds(PortData warp, Location loc) {
        double x1 = Math.min(warp.getFloorPoint().getX(), warp.getCeilPoint().getX());
        double y1 = Math.min(warp.getFloorPoint().getY(), warp.getCeilPoint().getY());
        double z1 = Math.min(warp.getFloorPoint().getZ(), warp.getCeilPoint().getZ());
        double x2 = Math.max(warp.getFloorPoint().getX(), warp.getCeilPoint().getX());
        double y2 = Math.max(warp.getFloorPoint().getY(), warp.getCeilPoint().getY());
        double z2 = Math.max(warp.getFloorPoint().getZ(), warp.getCeilPoint().getZ());

        return loc.getBlockX() >= x1 && loc.getBlockX() <= x2
                && loc.getBlockY() >= y1 && loc.getBlockY() <= y2
                && loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2;
    }
//
//    private void registerNewWarp(PortData warpData) {
//
//        final int timer = warpData.getWarpOffset();
//        scheduledExecutorService.schedule(() -> {
//            timerRepositories.getActivePlayers(warpData.getName())
//                    .forEach(p -> {
//                        p.teleport(warpData.getWarpToPoint().getLocation());
//                        p.sendMessage(warpData.getArrivalMessage());
//                        sche
//                    });
//        }, warpData.getWarpOffset(), TimeUnit.SECONDS);
//    }

}
