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

    /**
     * Buildings have three readable levels. The first level is the existing
     * building; levels two and three are paid upgrades from the map popup.
     */
    public int maxLevel() {
        return 3;
    }

    public int upgradeCashCost(int currentLevel) {
        return switch (this) {
            case CIVIL -> currentLevel == 1 ? 6_000 : 10_000;
            case MILITARY -> currentLevel == 1 ? 8_000 : 14_000;
            case FARM -> currentLevel == 1 ? 5_000 : 9_000;
            case STEELWORK -> currentLevel == 1 ? 6_500 : 11_000;
            case REFINERY -> currentLevel == 1 ? 7_000 : 12_000;
            case BARRACKS -> currentLevel == 1 ? 4_500 : 8_000;
            case FORT -> currentLevel == 1 ? 7_500 : 13_000;
            case INTEL -> currentLevel == 1 ? 8_000 : 14_000;
        };
    }

    public int upgradeSteelCost(int currentLevel) {
        return switch (this) {
            case CIVIL -> currentLevel == 1 ? 12 : 20;
            case MILITARY -> currentLevel == 1 ? 18 : 30;
            case FARM -> currentLevel == 1 ? 15 : 25;
            case STEELWORK -> currentLevel == 1 ? 20 : 32;
            case REFINERY -> currentLevel == 1 ? 22 : 36;
            case BARRACKS -> currentLevel == 1 ? 10 : 18;
            case FORT -> currentLevel == 1 ? 24 : 40;
            case INTEL -> currentLevel == 1 ? 16 : 28;
        };
    }

    public String upgradeRequirement(int currentLevel) {
        if (currentLevel >= 2) {
            return "Mindestens eine zivile Fabrik im Land";
        }
        return "Gebäude vorhanden";
    }

    public String effectAtLevel(int level) {
        int safeLevel = Math.max(1, Math.min(maxLevel(), level));
        return switch (this) {
            case FARM -> "+" + safeLevel * 30 + " Nahrung/Tag";
            case STEELWORK -> "+" + safeLevel * 15 + " Stahl/Tag";
            case REFINERY -> "+" + safeLevel * 12 + " Treibstoff/Tag";
            case CIVIL -> "+" + safeLevel * 600 + " Geld/Tag";
            case MILITARY -> "+" + safeLevel * 10 + " Militärpunkte/Tag";
            case BARRACKS -> "+" + safeLevel * 500 + " Ausbildungskapazität";
            case FORT -> "+" + safeLevel * 15 + " % Verteidigung";
            case INTEL -> "+" + safeLevel * 12 + " % Informationsqualität";
        };
    }
}
