package com.thetablock.thetaport.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.geometry.Point3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class PortLoc extends Point3D {
    private String world;
    private double yaw;
    private double pitch;

    public PortLoc(String world, Double x, Double y, Double z) {
        super(x,y,z);
        this.world = world;
        this.yaw = 0;
        this.pitch = 0;
    }

    public PortLoc(String world, Double x, Double y, Double z, double yaw, double pitch) {
        super(x,y,z);
        this.world = world;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public PortLoc() {
        super(0, 0, 0);
    }

    @JsonIgnore
    public PortLoc(Location location) {
        super(location.getX(), location.getY(), location.getZ());
        world = location.getWorld().getName();
    }

    @JsonIgnore
    public boolean compareWarpLoc(PortLoc loc) {
        double x1 = Math.min(this.getX(), loc.getX());
        double y1 = Math.min(this.getY(), loc.getY());
        double z1 = Math.min(this.getZ(), loc.getZ());
        double x2 = Math.min(this.getX(), loc.getX());
        double y2 = Math.min(this.getY(), loc.getY());
        double z2 = Math.min(this.getZ(), loc.getZ());

        Double minX = this.getX() > loc.getX() ? this.getX() : loc.getX();
        Double maxX = this.getX() > loc.getX() ? loc.getX() : this.getX();
        Double minY = this.getY() > loc.getY() ? this.getY() : loc.getY();
        Double maxY = this.getY() > loc.getY() ? loc.getY() : this.getY();
        Double minZ = this.getZ() > loc.getZ() ? this.getZ() : loc.getZ();
        Double maxZ = this.getY() > loc.getY() ? loc.getY() : this.getY();


        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    @JsonIgnore
    public PortLoc getCenterPointFrom(PortLoc portLoc) {
        Double midX = (portLoc.getX() + this.getX()) / 2;
        Double midZ = (portLoc.getZ() + this.getZ()) / 2;
        Double midY = (portLoc.getY() > this.getY()) ? this.getY() : portLoc.getY();
        return new PortLoc(world, midX, midY, midZ);
    }

    @JsonIgnore
    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), getX(), getY(), getZ());
    }

    public String getWorld() {
        return world;
    }

    public boolean isWithinBounds(PortLoc lower, PortLoc upper) {
        double x1 = Math.min(lower.getX(), upper.getX());
        double y1 = Math.min(lower.getY(), upper.getY());
        double z1 = Math.min(lower.getZ(), upper.getZ());
        double x2 = Math.max(lower.getX(), upper.getX());
        double y2 = Math.max(lower.getY(), upper.getY());
        double z2 = Math.max(lower.getZ(), upper.getZ());

        return this.getX() >= x1 && this.getX() <= x2
                && getY() >= y1 && getY() <= y2
                && getZ() >= z1 && getZ() <= z2;
    }
}
