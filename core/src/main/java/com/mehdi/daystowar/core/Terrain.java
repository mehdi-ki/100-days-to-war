package com.mehdi.daystowar.core;

public enum Terrain {
    MOUNTAINS("Gebirge"),
    FOREST("Wald"),
    PLAINS("Ebenen"),
    COAST("Küste"),
    DESERT("Wüste"),
    PASS("Engpass");

    private final String displayName;

    Terrain(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
