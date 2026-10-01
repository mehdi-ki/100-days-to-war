package com.mehdi.daystowar.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mehdi.daystowar.core.WarGame;

public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("100 Days to War");
        configuration.setWindowedMode(1_280, 720);
        configuration.setForegroundFPS(60);
        configuration.useVsync(true);
        configuration.setResizable(true);
        new Lwjgl3Application(new WarGame(), configuration);
    }
}
