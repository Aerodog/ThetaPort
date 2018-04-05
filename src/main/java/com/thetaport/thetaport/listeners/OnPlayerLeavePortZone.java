package com.thetablock.thetaport.listeners;

import com.thetablock.thetaport.entities.PortData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OnPlayerLeavePortZone extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private PortData portData;
    private Player player;

    public OnPlayerLeavePortZone(Player player, PortData portData) {
        super(true);
        this.portData = portData;
        this.player = player;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public final PortData getPortData() {
        return portData;
    }

    public final Player getPlayer() {
        return player;
    }
}
