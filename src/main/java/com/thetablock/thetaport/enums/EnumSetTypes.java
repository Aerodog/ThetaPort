package com.thetablock.thetaport.enums;

import com.thetablock.thetaport.entities.PortLoc;

public enum EnumSetTypes {
    OFFSET(Integer.class),
    NAME(String.class),
    LINK(String.class),
    FLOOR(PortLoc.class),
    CEIL(PortLoc.class);

    private Class clazz;

    <T>EnumSetTypes(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Class getClassType() {
        return clazz;
    }
}
