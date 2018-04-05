package com.thetablock.thetaport.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private LocalTime lastPortTime;
    private boolean isEnabled;
    private int warpOffset; //nopte this is in seconds
    private boolean isTimed;
    private Runnable runnable;

    /*
    Noted input type for this command is Seconds, it is being parsed byt the command.
     */
    public PortData(String name, String linked, PortLoc floorPoint, PortLoc ceilPoint, PortLoc warpToPoint, boolean isEnabled, int timeBetweenPorts) {
        this.name = name;
        this.linked = linked;
        this.floorPoint = floorPoint;
        this.ceilPoint = ceilPoint;
        this.isEnabled = isEnabled;
        this.warpOffset = timeBetweenPorts;
        this.warpToPoint = warpToPoint;
        this.lastPortTime = LocalTime.now();
        isTimed = false;
    }

    public PortData() {
        this.lastPortTime = LocalTime.now();
    }

    @JsonIgnore
    private LocalTime getOffset(int amount) {
        return LocalTime.now().plusMinutes(warpOffset).minusSeconds(warpOffset / amount);
    }

    public String getName() {
        return name;
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
    public LocalTime getLastPortTime() {
        return lastPortTime;
    }

    @JsonIgnore
    public PortData setLastPortTime(LocalTime lastPortTime) {
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

    public boolean isTimed() {
        return isTimed;
    }

    public PortData setTimed(boolean timed) {
        isTimed = timed;
        return this;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public PortData setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    @Override
    public String toString() {
        return "PortData{" +
                "name='" + name + '\'' +
                ", linked='" + linked + '\'' +
                ", warpMessage='" + warpMessage + '\'' +
                ", arrivalMessage='" + arrivalMessage + '\'' +
                ", departureMessage='" + departureMessage + '\'' +
                ", floorPoint=" + floorPoint +
                ", ceilPoint=" + ceilPoint +
                ", isEnabled=" + isEnabled +
                ", warpOffset=" + warpOffset +
                ", isTimed=" + isTimed +
                ", warpToPoint=" + warpToPoint +
                '}';
    }

}
