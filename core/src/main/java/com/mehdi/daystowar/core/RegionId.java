package com.mehdi.daystowar.core;

public enum RegionId {
    CAPITAL("Hauptstadt", "Regierung & Verwaltung", 8),
    INDUSTRY("Industrieregion", "Produktion & Stahl", 6),
    RURAL("Landwirtschaftsregion", "Versorgung & Nahrung", 4),
    BORDER("Grenzregion", "Front & Verteidigung", 4);

    private final String displayName;
    private final String description;
    private final int slots;

    RegionId(String displayName, String description, int slots) {
        this.displayName = displayName;
        this.description = description;
        this.slots = slots;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public int slots() {
        return slots;
    }
}
