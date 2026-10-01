package com.mehdi.daystowar.core;

public enum Policy {
    VOLUNTEER("Freiwilligenarmee", 0.00, 150, 0.00,
            "Keine Produktionsstrafe · niedriger Zulauf an Rekruten"),
    LIMITED("Begrenzte Wehrpflicht", 0.05, 650, 0.00,
            "-5 % Industrieproduktion · mehr verfügbare Rekruten"),
    GENERAL("Allgemeine Wehrpflicht", 0.15, 1_300, 0.45,
            "-15 % Industrieproduktion · mehr Rekruten, sinkende Stabilität");

    private final String displayName;
    private final double industryPenalty;
    private final int manpowerPerDay;
    private final double stabilityLossPerDay;
    private final String description;

    Policy(String displayName, double industryPenalty, int manpowerPerDay, double stabilityLossPerDay,
           String description) {
        this.displayName = displayName;
        this.industryPenalty = industryPenalty;
        this.manpowerPerDay = manpowerPerDay;
        this.stabilityLossPerDay = stabilityLossPerDay;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public double industryPenalty() {
        return industryPenalty;
    }

    public int manpowerPerDay() {
        return manpowerPerDay;
    }

    public double stabilityLossPerDay() {
        return stabilityLossPerDay;
    }

    public String description() {
        return description;
    }
}
