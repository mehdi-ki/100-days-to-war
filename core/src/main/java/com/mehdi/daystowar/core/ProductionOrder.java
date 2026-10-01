package com.mehdi.daystowar.core;

public final class ProductionOrder {
    private final UnitType type;
    private int remainingDays;
    private double progress;

    public ProductionOrder(UnitType type) {
        this.type = type;
        this.remainingDays = type.trainingDays();
    }

    public UnitType type() {
        return type;
    }

    public int remainingDays() {
        return remainingDays;
    }

    public double progress() {
        return progress;
    }

    void advanceInfantryDay() {
        remainingDays--;
    }

    void addWork(double amount) {
        progress += amount;
    }

    public int progressPercent() {
        double value = type == UnitType.INFANTRY
                ? (1d - (double) remainingDays / type.trainingDays()) * 100d
                : progress / type.work() * 100d;
        return (int) Math.min(100, Math.max(0, Math.round(value)));
    }

    boolean completed() {
        return type == UnitType.INFANTRY ? remainingDays <= 0 : progress >= type.work();
    }
}
