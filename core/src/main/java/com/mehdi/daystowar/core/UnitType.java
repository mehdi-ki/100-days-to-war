package com.mehdi.daystowar.core;

public enum UnitType {
    INFANTRY("Infanterie", 5_000, 5, 5_000, 4, 8, 0, 40, false,
            "Günstig und verlässlich. Hält die Front und besetzt Gebiete."),
    ARTILLERY("Artillerie", 8_000, 20, 1_500, 6, 4, 0, 60, true,
            "Starker Fernkampf. Besonders wirksam gegen große Infanterieverbände."),
    TANK("Panzer", 18_000, 45, 2_000, 8, 5, 8, 80, true,
            "Schneller Durchbruch. Treibstoffmangel senkt die Kampfkraft."),
    LOGISTICS("Logistik", 6_000, 10, 1_000, 4, 3, 2, 40, true,
            "Verbessert Versorgung und Bewegung und senkt Treibstoffverbrauch.");

    private final String displayName;
    private final int cashCost;
    private final int steelCost;
    private final int manpowerCost;
    private final int trainingDays;
    private final int foodUse;
    private final int fuelUse;
    private final int work;
    private final boolean requiresMilitaryFactory;
    private final String role;

    UnitType(String displayName, int cashCost, int steelCost, int manpowerCost, int trainingDays,
             int foodUse, int fuelUse, int work, boolean requiresMilitaryFactory, String role) {
        this.displayName = displayName;
        this.cashCost = cashCost;
        this.steelCost = steelCost;
        this.manpowerCost = manpowerCost;
        this.trainingDays = trainingDays;
        this.foodUse = foodUse;
        this.fuelUse = fuelUse;
        this.work = work;
        this.requiresMilitaryFactory = requiresMilitaryFactory;
        this.role = role;
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

    public int manpowerCost() {
        return manpowerCost;
    }

    public int trainingDays() {
        return trainingDays;
    }

    public int foodUse() {
        return foodUse;
    }

    public int fuelUse() {
        return fuelUse;
    }

    public int work() {
        return work;
    }

    public boolean requiresMilitaryFactory() {
        return requiresMilitaryFactory;
    }

    public String role() {
        return role;
    }
}
