package com.thetablock.thetaport.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class Core {
    private String name;
    private int offset;
    private String linked;
    private ItemStack requiredItem;
    private PortLoc floorPoint;
    private PortLoc ceilPoint;
    private PortLoc arrivalPoint;
    private PortLoc arrivalRedstone;
    private PortLoc departureRedstone;

    @JsonIgnore
    public Core(String name, int offset, String linked, ItemStack requiredItem, PortLoc floorPoint, PortLoc ceilPoint, PortLoc arrivalPoint, PortLoc arrivalRedstone, PortLoc departureRedstone) {
        this.name = name;
        this.offset = offset;
        this.linked = linked;
        this.requiredItem = requiredItem;
        this.floorPoint = floorPoint;
        this.ceilPoint = ceilPoint;
        this.arrivalPoint = arrivalPoint;
        this.arrivalRedstone = arrivalRedstone;
        this.departureRedstone = departureRedstone;
    }

    public Core(String name, int offset, String linked, PortLoc floorPoint, PortLoc ceilPoint, PortLoc arrivalPoint, PortLoc arrivalRedstone, PortLoc departureRedstone) {
        this.name = name;
        this.offset = offset;
        this.linked = linked;
        this.requiredItem = null;
        this.floorPoint = floorPoint;
        this.ceilPoint = ceilPoint;
        this.arrivalPoint = arrivalPoint;
        this.arrivalRedstone = arrivalRedstone;
        this.departureRedstone = departureRedstone;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    public String getLinked() {
        return linked;
    }

    @JsonIgnore
    public ItemStack getCoreItem() {
        return requiredItem;
    }

    public ItemStack getRequiredItem() {
        return requiredItem;
    }

    public PortLoc getFloorPoint() {
        return floorPoint;
    }

    public void setArrivalPoint(PortLoc point) {
        this.arrivalPoint = point;
    }

    public PortLoc getCeilPoint() {
        return ceilPoint;
    }

    public PortLoc getArrivalPoint() {
        return arrivalPoint;
    }

    public Core setFloorPoint(PortLoc floorPoint) {
        this.floorPoint = floorPoint;
        return this;
    }

    public Core setCeilPoint(PortLoc ceilPoint) {
        this.ceilPoint = ceilPoint;
        return this;
    }

    public Core setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public PortLoc getArrivalRedstone() {
        return arrivalRedstone;
    }

    public PortLoc getDepartureRedstone() {
        return departureRedstone;
    }

    @JsonIgnore
    public Core setRequiredItem(ItemStack requiredItem) {
        this.requiredItem = requiredItem;
        return this;
    }

    public Core setArrivalRedstone(PortLoc arrivalRedstone) {
        this.arrivalRedstone = arrivalRedstone;
        return this;
    }

    public Core setDepartureRedstone(PortLoc departureRedstone) {
        this.departureRedstone = departureRedstone;
        return this;
    }
}
