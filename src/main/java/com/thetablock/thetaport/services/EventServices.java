package com.thetablock.thetaport.services;

import com.thetablock.thetaport.entities.PortData;
import com.thetablock.thetaport.entities.PortLoc;
import com.thetablock.thetaport.entities.PortState;
import com.thetablock.thetaport.enums.EnumPortState;
import com.thetablock.thetaport.repositories.PortDataRepository;
import com.thetablock.thetaport.repositories.TempRepository;
import com.thetablock.thetaport.utils.ItemComparator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Lever;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

import static com.thetablock.thetaport.enums.EnumPortState.JUST_WARPED;
import static com.thetablock.thetaport.enums.EnumPortState.MISSING_REQUIRED_ITEM;
import static com.thetablock.thetaport.enums.EnumPortState.WAITING_WARP;
import static java.time.temporal.ChronoUnit.SECONDS;

public final class EventServices {
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(12);
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    static final ConcurrentHashMap<UUID, Location> ASYNC_PLAYER_LOCATIONS = new ConcurrentHashMap<>();

    private final PortDataRepository portDataRepository;
    private final TempRepository tempRepository;

    @Inject
    public EventServices(PortDataRepository portDataRepository, TempRepository tempRepository) {
        this.portDataRepository = portDataRepository;
        this.tempRepository = tempRepository;
    }

    private void checkIfPlayerLeavesPortZone(Player player) {
        PortLoc portLoc = new PortLoc(player.getLocation());
        for (PortState portState : tempRepository.tempWarpMaps().values()) {
            PortData portData = portDataRepository.getWarp(portState.getWarpName());
            portData.getFloorPoint().add(3, 3, 3);
            portData.getCeilPoint().add(3, 3, 3);

            if (!isWithinBounds(portData.getFloorPoint(), portData.getCeilPoint(), player.getLocation())) {
                tempRepository.removePlayer(player.getUniqueId());
            }
        }
    }

    private void checkIfPlayerEntersPortZone(Player player) throws InterruptedException {
        final PortLoc portLoc = new PortLoc(player.getLocation());
        portDataRepository.getWarpData().values().stream()
                .filter(pt->pt.getLinked() != null && !pt.getLinked().isEmpty())
                .filter(pt->isWithinBounds(pt.getFloorPoint(), pt.getCeilPoint(), player.getLocation()))
                .filter(pt -> {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PortState portState = tempRepository.getPlayer(player.getUniqueId());
                    return null == portState;
                })
                .filter(pt->{
                    if (null != pt.getRequiredItem()) {
                        Optional<ItemStack> stack = Arrays.stream(player.getInventory().getContents())
                                .filter(i -> ItemComparator.compare(i, pt.getRequiredItem()))
                                .findFirst();
                        if (stack.isPresent()) {
                            stack.ifPresent(i -> player.getInventory().remove(i));
                            return true;
                        }
                        tempRepository.addActivePlayer(pt.getName(), player, EnumPortState.MISSING_REQUIRED_ITEM);
                        player.sendMessage("§4You do not have the required ticket for this port.");
                        return false;
                    }
                    return true;
                })
                .forEach(pt -> {
                    if (pt.getOffset() > 0) {
                        if (ChronoUnit.SECONDS.between(pt.getLastPortTime(), LocalTime.now()) > 1) {
                            LocalDateTime nextToWarp = pt.getLastPortTime().plusSeconds(pt.getOffset());
                            String departureMessage = pt.getDepartureMessage().replace("&t", getTimeRemaining(LocalDateTime.now(), nextToWarp));
                            departureMessage = departureMessage.replace("&", "§");
                            player.sendMessage(departureMessage);
                        }
                    }
                    tempRepository.addActivePlayer(pt.getName(), player, EnumPortState.WAITING_WARP);
                });
    }

    private synchronized String getTimeRemaining(LocalDateTime start, LocalDateTime end) {
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

                try {
                    checkIfPlayerEntersPortZone(player);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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

    /**
     * This System will only  execute if the offset > 0;
     */
    private void buildRunnable() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                portDataRepository.getWarpData().values().stream()
                        .filter(pt -> {
                            boolean isLinked = pt.getLinked() != null && !pt.getLinked().isEmpty();
                            boolean hasOffsetAndPort;
                            if (pt.getOffset() > 0) {
                                hasOffsetAndPort = SECONDS.between(LocalDateTime.now(), pt.getLastPortTime().plusSeconds(pt.getOffset())) < 0;
                            } else {
                                hasOffsetAndPort = true;

                            }
                            return isLinked && hasOffsetAndPort;
                        })
                        .forEach(pt -> {
                            //If the port
                            if (pt.getOffset() > 0) {
                                pt.setLastPortTime(LocalDateTime.now());
                                portDataRepository.update(pt);
                                port(pt.getName(), pt.getLinked(), pt.getRequiredItem());
                            } else {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                port(pt.getName(), pt.getLinked(),  pt.getRequiredItem());
                            }
                            //executes redstone on warp
                            if (null != pt.getArrivalRedstone()) {
                                Lever lever = (Lever) pt.getArrivalRedstone().getLocation().getBlock();
                                executeRedstone(lever);
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000L, 1000L, TimeUnit.MILLISECONDS);
    }

    private void port(String portName, String linked, ItemStack requiredItem) {
        final PortData portToPoint = portDataRepository.getWarp(linked);
        tempRepository.tempWarpMaps().values().stream()
                .filter(Objects::nonNull)
                .filter(portState -> portState.getWarpName().equalsIgnoreCase(portName))
                .filter(pt -> {
                    if (pt.getEnumPortState() != null) {
                        return !pt.getEnumPortState().equals(MISSING_REQUIRED_ITEM);
                    }
                    return true;
                })
                .filter(pt -> {
                    if (pt.getEnumPortState() != null) {
                        return !pt.getEnumPortState().equals(JUST_WARPED);
                    }
                    return true;
                })
                .forEach(portState -> {
                    if (null != requiredItem) {
                        Optional<ItemStack> stack = Arrays.stream(portState.getPlayer().getInventory().getContents())
                                .filter(i->ItemComparator.compare(i, requiredItem))
                                .findFirst();
                        if (stack.isPresent()) {
                            stack.ifPresent(i->portState.getPlayer().getInventory().remove(i));
                        }
                    }
                    tempRepository.addActivePlayer(portToPoint.getName(), portState.getPlayer(), JUST_WARPED);
                    portState.getPlayer().teleport(portToPoint.getArrivalPoint().getLocation().add(0,1,0));
                    //TEST
                    System.out.println("arrival " + portToPoint.getName() + " " + portToPoint.getArrivalMessage());
                    if (null != portToPoint.getArrivalMessage() && !portToPoint.getArrivalMessage().isEmpty()) {
                        String message = portToPoint.getArrivalMessage().replace("&", "§");
                        portState.getPlayer().sendMessage(message);
                    }
                });
    }

    private void executeRedstone(Lever lever) {
        executorService.execute(() -> {
            lever.setPowered(true);
            try {
                Thread.sleep(3L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lever.setPowered(false);
        });
        lever.setPowered(true);
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
