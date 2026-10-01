package com.mehdi.daystowar.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class WorldSimulationTest {
    @Test
    void startsWithThePrototypeBaseline() {
        WorldSimulation simulation = new WorldSimulation(7L);
        GameState state = simulation.state();

        assertEquals(1, state.day());
        assertEquals(50_000, state.resource(ResourceType.CASH));
        assertEquals(2, state.armyCount(UnitType.INFANTRY));
        assertNotNull(state.currentEvent());
        assertFalse(state.eventResolved());
    }

    @Test
    void planningActionsConsumeResourcesAndAdvanceTheDay() {
        WorldSimulation simulation = new WorldSimulation(11L);

        assertTrue(simulation.build(BuildingType.FORT, RegionId.BORDER).success());
        assertTrue(simulation.train(UnitType.INFANTRY).success());
        assertEquals(29_000, simulation.state().resource(ResourceType.CASH));
        assertTrue(simulation.chooseEvent(1).success());
        assertTrue(simulation.endDay().success());
        assertEquals(2, simulation.state().day());
        assertFalse(simulation.state().finished());
    }

    @Test
    void tenDayLoopProducesAWarReport() {
        WorldSimulation simulation = new WorldSimulation(42L);

        for (int day = 1; day <= 10; day++) {
            assertTrue(simulation.chooseEvent(1).success());
            assertTrue(simulation.endDay().success());
        }

        assertTrue(simulation.state().finished());
        assertNotNull(simulation.state().report());
        assertTrue(simulation.state().report().ratio() > 0);
    }
}
