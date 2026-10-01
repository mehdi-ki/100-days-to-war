package com.mehdi.daystowar.core;

import java.util.List;

public record EventDefinition(String id, String title, String description, List<Choice> choices) {
    public EventDefinition {
        choices = List.copyOf(choices);
    }

    public record Choice(String label, String consequence) {
    }
}
