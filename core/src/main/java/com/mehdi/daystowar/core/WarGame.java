package com.mehdi.daystowar.core;

import com.badlogic.gdx.Game;

public final class WarGame extends Game {
    @Override
    public void create() {
        setScreen(new StrategyScreen(new WorldSimulation()));
    }
}
