package com.mehdi.daystowar.core;

public enum ResourceType {
    CASH("Staatskasse"),
    FOOD("Nahrung"),
    STEEL("Stahl"),
    FUEL("Treibstoff"),
    MANPOWER("Mannstärke");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
