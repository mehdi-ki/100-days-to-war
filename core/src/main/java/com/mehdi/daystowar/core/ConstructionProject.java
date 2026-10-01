package com.mehdi.daystowar.core;

public final class ConstructionProject {
    private final BuildingType type;
    private final RegionId region;
    private int remainingDays;

    public ConstructionProject(BuildingType type, RegionId region) {
        this.type = type;
        this.region = region;
        this.remainingDays = type.buildDays();
    }

    public BuildingType type() {
        return type;
    }

    public RegionId region() {
        return region;
    }

    public int remainingDays() {
        return remainingDays;
    }

    void advanceDay() {
        remainingDays--;
    }

    public int progressPercent() {
        return Math.min(100, Math.max(0,
                Math.round((1f - (float) remainingDays / type.buildDays()) * 100f)));
    }
}
