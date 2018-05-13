package com.thetablock.thetaport.enums;

public enum ToolNames {
    CREATE_TOOL("Create Tool"),
    INFO_TOOL("Info Tool");

    private String name;
    ToolNames(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
