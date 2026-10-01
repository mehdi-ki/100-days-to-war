package com.mehdi.daystowar.core;

public enum TaxLevel {
    LOW("Niedrig", 3_000, 0.00),
    NORMAL("Normal", 4_200, 0.00),
    HIGH("Hoch", 6_000, 0.65);

    private final String displayName;
    private final int income;
    private final double stabilityStress;

    TaxLevel(String displayName, int income, double stabilityStress) {
        this.displayName = displayName;
        this.income = income;
        this.stabilityStress = stabilityStress;
    }

    public String displayName() {
        return displayName;
    }

    public int income() {
        return income;
    }

    public double stabilityStress() {
        return stabilityStress;
    }
}
