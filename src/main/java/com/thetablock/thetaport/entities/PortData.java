package com.thetablock.thetaport.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class PortData {
    private String name;
    private String linked;
    private String warpMessage;
    private String arrivalMessage;
    private String departureMessage;
    private PortLoc floorPoint;
    private PortLoc ceilPoint;
    private PortLoc warpToPoint;
    @JsonIgnore
    private LocalDateTime lastPortTime;
    private boolean isEnabled;
    private int warpOffset; //nopte this is in seconds

    public PortData(String name, String linked, String warpMessage, String arrivalMessage, String departureMessage, PortLoc floorPoint, PortLoc ceilPoint, PortLoc warpToPoint, LocalDateTime lastPortTime, boolean isEnabled, int warpOffset) {
        this.name = name;
        this.linked = linked;
        this.warpMessage = warpMessage;
        this.arrivalMessage = arrivalMessage;
        this.departureMessage = departureMessage;
        this.floorPoint = floorPoint;
        this.ceilPoint = ceilPoint;
        this.warpToPoint = warpToPoint;
        this.lastPortTime = lastPortTime;
        this.isEnabled = isEnabled;
        this.warpOffset = warpOffset;
    }

    public PortData() {

    }

    @JsonIgnore
    private LocalDateTime getOffset(int amount) {
        return LocalDateTime.now().plusMinutes(warpOffset).minusSeconds(warpOffset / amount);
    }

    public String getName() {
        return !name.isEmpty() ? name : "DERP";
    }

    public PortData setName(String name) {
        this.name = name;
        return this;
    }

    public String getLinked() {
        return linked;
    }

    public PortData setLinked(String linked) {
        this.linked = linked;
        return this;
    }

    public String getWarpMessage() {
        return warpMessage;
    }

    public PortData setWarpMessage(String warpMessage) {
        this.warpMessage = warpMessage;
        return this;
    }

    public String getArrivalMessage() {
        return arrivalMessage;
    }

    public PortData setArrivalMessage(String arrivalMessage) {
        this.arrivalMessage = arrivalMessage;
        return this;
    }

    public String getDepartureMessage() {
        return departureMessage;
    }

    public PortData setDepartureMessage(String departureMessage) {
        this.departureMessage = departureMessage;
        return this;
    }

    public PortLoc getFloorPoint() {
        return floorPoint;
    }

    public PortData setFloorPoint(PortLoc floorPoint) {
        this.floorPoint = floorPoint;
        return this;
    }

    public PortLoc getCeilPoint() {
        return ceilPoint;
    }

    public PortData setCeilPoint(PortLoc ceilPoint) {
        this.ceilPoint = ceilPoint;
        return this;
    }

    public PortLoc getWarpToPoint() {
        return warpToPoint;
    }

    public PortData setWarpToPoint(PortLoc warpToPoint) {
        this.warpToPoint = warpToPoint;
        return this;
    }

    @JsonIgnore
    public LocalDateTime getLastPortTime() {
        return lastPortTime;
    }

    @JsonIgnore
    public PortData setLastPortTime(LocalDateTime lastPortTime) {
        this.lastPortTime = lastPortTime;
        return this;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public PortData setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    public int getWarpOffset() {
        return warpOffset;
    }

    public PortData setWarpOffset(int warpOffset) {
        this.warpOffset = warpOffset;
        return this;
    }
}
