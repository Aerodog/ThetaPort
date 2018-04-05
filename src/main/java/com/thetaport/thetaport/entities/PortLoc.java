package com.thetablock.thetaport.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.geometry.Point3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class PortLoc extends Point3D {
    private String world;
//    private Double x;
//    private Double y;
//    private Double z;

    public PortLoc(String world, Double x, Double y, Double z) {
        super(x,y,z);
        this.world = world;
    }

    public PortLoc() {
        super(0, 0, 0);
    }

    @JsonIgnore
    public PortLoc(Location location) {
        super(location.getX(), location.getY(), location.getZ());
//        this.x = location.getX();
//        this.y = location.getY();
//        this.z = location.getZ();
        world = location.getWorld().getName();
    }

    @JsonIgnore
    public PortLoc compareWarpLoc(PortLoc loc) {
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
                && loc.getZ() >= minZ && loc.getZ() <= maxZ ? loc : this;
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
}
