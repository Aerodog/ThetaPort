package com.thetablock.thetaport.services;

import com.google.common.base.Joiner;
import com.thetablock.thetaport.entities.PortData;
import com.thetablock.thetaport.entities.PortLoc;
import com.thetablock.thetaport.entities.PortState;
import com.thetablock.thetaport.enums.EnumPortState;
import com.thetablock.thetaport.repositories.PortDataRepository;
import com.thetablock.thetaport.repositories.TimerRepository;
import com.thetablock.thetaport.utils.cmdManager.Option;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

import static com.thetablock.thetaport.enums.EnumPortState.JUST_WARPED;
import static com.thetablock.thetaport.enums.EnumPortState.WAITING_WARP;
import static java.time.temporal.ChronoUnit.SECONDS;

public final class EventServices {
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(12);

    static final ConcurrentHashMap<UUID, Location> ASYNC_PLAYER_LOCATIONS = new ConcurrentHashMap<>();

    private final PortDataRepository portDataRepository;
    private final TimerRepository timerRepository;

    @Inject
    public EventServices(PortDataRepository portDataRepository, TimerRepository timerRepository) {
        this.portDataRepository = portDataRepository;
        this.timerRepository = timerRepository;
    }

    private void checkIfPlayerLeavesPortZone(Player player) {
        PortLoc portLoc = new PortLoc(player.getLocation());
        for (PortState portState : timerRepository.tempWarpMaps().values()) {
            PortData portData = portDataRepository.getWarp(portState.getWarpName());
            portData.getFloorPoint().add(3, 3, 3);
            portData.getCeilPoint().add(3, 3, 3);

            if (!isWithinBounds(portData.getFloorPoint(), portData.getCeilPoint(), player.getLocation())) {
                timerRepository.removePlayer(player.getUniqueId());
            }
        }
    }

    private void checkIfPlayerEntersPortZone(Player player) {
        final LocalDateTime currentTime = LocalDateTime.now();
        Optional<PortData> portDataOption =
                portDataRepository.getWarpData().values().stream()
                        .filter(pt -> null != pt.getLinked() && !pt.getLinked().isEmpty())
                        .filter(pt -> isWithinBounds(pt.getFloorPoint(), pt.getCeilPoint(), player.getLocation()))
                        .filter(pt -> {
                            if (ChronoUnit.SECONDS.between(LocalDateTime.now(), pt.getLastPortTime().plusSeconds(pt.getWarpOffset())) <= 2) {
                                try {
                                    Thread.sleep(2);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            PortState portState = timerRepository.getPlayer(player.getUniqueId());

                            if (null != portState) {
                                if (portState.getEnumPortState().equals(JUST_WARPED) || portState.getEnumPortState().equals(WAITING_WARP)) {
                                    return false;
                                }
                            }
                            return true;
                        }).findFirst();

        portDataOption.ifPresent(pt -> {
            timerRepository.addActivePlayer(pt.getName(), player, EnumPortState.JUST_WARPED);
            System.out.println(timerRepository.toString());
            if (pt.getWarpOffset() > 0) {
                String timeLeft = getTimeRemaining(LocalDateTime.now(), pt.getLastPortTime().plusSeconds(pt.getWarpOffset()));
                System.out.println(timeLeft);
                String temp = pt.getDepartureMessage();
                temp = temp.replace("&t", timeLeft + " ");
                temp = temp.replace("&", "§");

                player.sendMessage(temp);
            }
        });
    }

    public synchronized String getTimeRemaining(LocalDateTime start, LocalDateTime end) {
        String output = "";
        //LocalTime startPoint = start.toLocalTime();
        //LocalTime endPoint = end.toLocalTime();
        Duration duration = start.isBefore(end) ? Duration.between(start, end) : Duration.between(end, start);

        if (duration.toDays() > 0L) {
            output = output.concat(duration.toDays() + "d ");
            duration = duration.minusDays(duration.toDays());
        }
        if (duration.toHours() > 0L) {
            output = output.concat(duration.toHours() + "h ");
            duration = duration.minusHours(duration.toHours());
        }
        if (duration.toMinutes() > 0L) {
            output = output.concat(duration.toMinutes() + "m ");
            duration = duration.minusMinutes(duration.toMinutes());
        }
        if (!duration.isZero()) {
            output = output.concat(duration.getSeconds() + "s");
        }
        return output;
    }


    private String parseTime(LocalTime nextExecutionTime) {

        ///
        final LocalDateTime now = LocalDateTime.now();
        long hours = ChronoUnit.HOURS.between(now, nextExecutionTime);
        long minutes = ChronoUnit.MINUTES.between(now, nextExecutionTime);
        long seconds = ChronoUnit.SECONDS.between(now, nextExecutionTime);

        String output = "";
        if (hours > 0) {
            output += hours + " hours ";
        }
        if (minutes > 0) {
            output += minutes + " minutes ";
        }

        if (seconds > 0) {
            output += seconds + " seconds ";
        }

        return output;
    }


    public void startListenerEvents(Plugin plugin) {
        portDataRepository.loadAll();
        buildRunnable();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
                ASYNC_PLAYER_LOCATIONS.put(pl.getUniqueId(), pl.getLocation());
            }
        }, 3, 3L);

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            ASYNC_PLAYER_LOCATIONS.forEach((k, v) -> {
                Player player = Bukkit.getPlayer(k);


                checkIfPlayerEntersPortZone(player);

            });
        }, 1000L, 1000L, TimeUnit.MILLISECONDS);


        scheduledExecutorService.scheduleAtFixedRate(() -> {
            ASYNC_PLAYER_LOCATIONS.forEach((k, v) -> {
                Player player = Bukkit.getPlayer(k);
                checkIfPlayerLeavesPortZone(player);
            });
        }, 1000L, 1000L, TimeUnit.MILLISECONDS);

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Iterator<Map.Entry<UUID, Location>> it = ASYNC_PLAYER_LOCATIONS.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Location> entry = it.next();
                Player player = Bukkit.getPlayer(entry.getKey());

                if (player == null) {
                    it.remove();
                    continue;
                }
            }
        }, 200L, 200L, TimeUnit.MILLISECONDS);
    }

    private void loopRunnableState(PortData portData) {
        for (PortState portState : timerRepository.tempWarpMaps().values()) {
            if (portState.getWarpName().equals(portData.getName())) {
                if (portState.getEnumPortState().equals(WAITING_WARP)) {
                    System.out.println(timerRepository.toString());
                    timerRepository.addActivePlayer(portData.getLinked(), portState.getPlayer(), JUST_WARPED);
                    //Test
                    portState = timerRepository.getPlayer(portState.getPlayer().getUniqueId());
                    portState.getPlayer().teleport(portData.getWarpToPoint().getLocation());

                    //sets the current state forward

                    portData.setLastPortTime(LocalDateTime.now());
                    portDataRepository.softUpdate(portData);
                    System.out.println("Last port time: " + portData.toString());

                    if (null != portData.getArrivalMessage() && !portData.getArrivalMessage().isEmpty()) {
                        String message = portData.getArrivalMessage();
                        message = message.replace("&", "§");
                        portState.getPlayer().sendMessage(message);
                    }

                }
            }
        }
    }

    private void buildRunnable() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                portDataRepository.getWarpData().values().stream()
                        .filter(pt -> pt.getLinked() != null && !pt.getLinked().isEmpty())
                        .forEach(pt -> {
                            if (pt.getWarpOffset() > 0) {
                                final long timeRemaining = SECONDS.between(LocalDateTime.now(), pt.getLastPortTime().plusSeconds(pt.getWarpOffset()));
                                if (timeRemaining <= 0) {
                                    pt.setLastPortTime(LocalDateTime.now());
                                    portDataRepository.softUpdate(pt);
                                    if (timerRepository.getActivePlayers(pt.getName()).size() > 0) {
                                        port(pt.getName(), pt.getLinked(), pt.getWarpToPoint(), pt.getArrivalMessage());
                                    }
                                }
                            }


                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000L, 1000L, TimeUnit.MILLISECONDS);
    }

    private void port(String portName, String linked, PortLoc portLoc, String message) {
        timerRepository.tempWarpMaps().values().forEach(portState -> {
            if (portState.getWarpName().equalsIgnoreCase(portName)) {

                if (null != portState.getEnumPortState()) {
                    System.out.println("port state " + portState.getEnumPortState());
                    if (portState.getEnumPortState().equals(WAITING_WARP)) {
                        return;
                    }
                }
                timerRepository.addActivePlayer(linked, portState.getPlayer(), JUST_WARPED);
                portState.getPlayer().teleport(portLoc.getLocation());
                portState.getPlayer().sendMessage(message.replace("&", "§"));
            }
        });
    }

    private boolean isWithinBounds(PortLoc lower, PortLoc upper, Location loc) {
        double x1 = Math.min(lower.getX(), upper.getX());
        double y1 = Math.min(lower.getY(), upper.getY());
        double z1 = Math.min(lower.getZ(), upper.getZ());
        double x2 = Math.max(lower.getX(), upper.getX());
        double y2 = Math.max(lower.getY(), upper.getY());
        double z2 = Math.max(lower.getZ(), upper.getZ());

        return loc.getBlockX() >= x1 && loc.getBlockX() <= x2
                && loc.getBlockY() >= y1 && loc.getBlockY() <= y2
                && loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2;
    }
}
