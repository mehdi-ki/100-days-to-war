package com.mehdi.daystowar.core;

import java.util.List;

public final class WarReport {
    private final boolean victory;
    private final int ratio;
    private final int ownMen;
    private final int enemyMen;
    private final int ownPower;
    private final int enemyPower;
    private final List<Factor> factors;
    private final List<String> reasons;

    public WarReport(boolean victory, int ratio, int ownMen, int enemyMen, int ownPower,
                     int enemyPower, List<Factor> factors, List<String> reasons) {
        this.victory = victory;
        this.ratio = ratio;
        this.ownMen = ownMen;
        this.enemyMen = enemyMen;
        this.ownPower = ownPower;
        this.enemyPower = enemyPower;
        this.factors = List.copyOf(factors);
        this.reasons = List.copyOf(reasons);
    }

    public boolean victory() {
        return victory;
    }

    public int ratio() {
        return ratio;
    }

    public int ownMen() {
        return ownMen;
    }

    public int enemyMen() {
        return enemyMen;
    }

    public int ownPower() {
        return ownPower;
    }

    public int enemyPower() {
        return enemyPower;
    }

    public List<Factor> factors() {
        return factors;
    }

    public List<String> reasons() {
        return reasons;
    }

    public record Factor(String name, String value, boolean positive) {
    }
}
