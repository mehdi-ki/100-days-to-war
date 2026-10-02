package com.mehdi.daystowar.core;

public enum Nation {
    NORVANE("Norvane"),
    ASTER("Aster"),
    VALERIA("Valeria"),
    DRAVIK("Dravik"),
    ELDORIA("Eldoria"),
    KARSEN("Karsen");

    private final String displayName;

    Nation(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
