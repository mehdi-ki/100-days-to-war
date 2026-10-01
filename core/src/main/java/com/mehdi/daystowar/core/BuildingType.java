package com.mehdi.daystowar.core;

public enum BuildingType {
    CIVIL("Zivile Fabrik", 18_000, 30, 6, "+600 Geld/Tag · +1 Baukapazität"),
    MILITARY("Militärfabrik", 22_000, 50, 7, "+10 militärische Produktionspunkte/Tag"),
    FARM("Farm", 8_000, 10, 3, "+30 Nahrung/Tag"),
    STEELWORK("Stahlwerk", 14_000, 0, 5, "+15 Stahl/Tag"),
    REFINERY("Raffinerie", 20_000, 25, 6, "+12 Treibstoff/Tag"),
    BARRACKS("Kaserne", 12_000, 20, 4, "+500 Rekruten-Ausbildungskapazität/Tag"),
    FORT("Festung", 16_000, 45, 5, "+15 % Verteidigung"),
    INTEL("Geheimdienstzentrale", 25_000, 20, 7, "Verbessert Informationen über den Gegner");

    private final String displayName;
    private final int cashCost;
    private final int steelCost;
    private final int buildDays;
    private final String effectDescription;

    BuildingType(String displayName, int cashCost, int steelCost, int buildDays, String effectDescription) {
        this.displayName = displayName;
        this.cashCost = cashCost;
        this.steelCost = steelCost;
        this.buildDays = buildDays;
        this.effectDescription = effectDescription;
    }

    public String displayName() {
        return displayName;
    }

    public int cashCost() {
        return cashCost;
    }

    public int steelCost() {
        return steelCost;
    }

    public int buildDays() {
        return buildDays;
    }

    public String effectDescription() {
        return effectDescription;
    }
}
