package com.thetablock.thetaport.listeners;

import com.thetablock.thetaport.entities.PortData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OnPlayerEnterPortZone extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private PortData portData;
    private Player player;


    public OnPlayerEnterPortZone(Player player, PortData portData) {
        this.player = player;
        this.portData = portData;
    }

    public static HandlerList getHandlerList() {
            return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }

    public PortData getPortData() {
        return portData;
    }

    public Player getPlayer() {
        return player;
    }
}
