package com.mehdi.daystowar.core;

public record ActionResult(boolean success, String message) {
    public static ActionResult ok(String message) {
        return new ActionResult(true, message);
    }

    public static ActionResult rejected(String message) {
        return new ActionResult(false, message);
    }
}
