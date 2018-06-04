package com.thetablock.thetaport.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thetablock.thetaport.utils.cmdManager.Option;

import java.time.LocalDateTime;
import java.util.Optional;

public class PortData extends Core {
    private String linked;

    private String arrivalMessage;
    private String departureMessage;
    private String invalidItemMessage;
    //Port Data

    @JsonIgnore
    private LocalDateTime lastPortTime = LocalDateTime.now();

    public PortData() {
        super("", 0, "", null, null, null, null, null, null);
        lastPortTime = LocalDateTime.now();
    }

    public PortData(Core core) {
        super(core.getName(), core.getOffset(), core.getLinked(), core.getRequiredItem(),  core.getFloorPoint(), core.getCeilPoint(), core.getArrivalPoint(),
                core.getArrivalRedstone(), core.getDepartureRedstone());
        lastPortTime = LocalDateTime.now();
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

    public String getInvalidItemMessage() {
        return invalidItemMessage;
    }

    public PortData setInvalidItemMessage(String invalidItemMessage) {
        this.invalidItemMessage = invalidItemMessage;
        return this;
    }

    @Override
    public String toString() {
        return "PortData{" +
                "linked='" + linked + '\'' +
                ", arrivalMessage='" + arrivalMessage + '\'' +
                ", departureMessage='" + departureMessage + '\'' +
                ", invalidItemMessage='" + invalidItemMessage + '\'' +
                ", lastPortTime=" + lastPortTime +
                '}' +
                super.toString();
    }
}
