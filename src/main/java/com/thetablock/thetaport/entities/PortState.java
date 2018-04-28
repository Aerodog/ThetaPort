package com.thetablock.thetaport.entities;

import com.thetablock.thetaport.enums.EnumPortState;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PortState {
    private Player player;
    private String warpName;
    private EnumPortState enumPortState;

    public PortState(Player player, String warpName, EnumPortState enumPortState) {
        this.player = player;
        this.warpName = warpName;
        this.enumPortState = enumPortState;
    }

    public String getWarpName() {
        return warpName;
    }

    public Player getPlayer() {
        return player;
    }

    public EnumPortState getEnumPortState() {
        return enumPortState;
    }

    @Override
    public String toString() {
        return "PortState{" +
                "player=" + player +
                ", warpName='" + warpName + '\'' +
                ", enumPortState=" + enumPortState +
                '}';
    }
}
