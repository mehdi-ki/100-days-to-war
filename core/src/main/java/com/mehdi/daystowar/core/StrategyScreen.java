package com.mehdi.daystowar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class StrategyScreen extends InputAdapter implements Screen {
    private static final float WORLD_WIDTH = 1_280;
    private static final float WORLD_HEIGHT = 720;
    private static final Color BACKGROUND = new Color(0.035f, 0.055f, 0.045f, 1f);
    private static final Color PANEL = new Color(0.075f, 0.105f, 0.085f, 1f);
    private static final Color PANEL_ALT = new Color(0.055f, 0.08f, 0.065f, 1f);
    private static final Color LINE = new Color(0.18f, 0.25f, 0.20f, 1f);
    private static final Color TEXT = new Color(0.85f, 0.9f, 0.85f, 1f);
    private static final Color MUTED = new Color(0.52f, 0.62f, 0.55f, 1f);
    private static final Color ACCENT = new Color(0.78f, 0.93f, 0.45f, 1f);
    private static final Color WARNING = new Color(0.94f, 0.66f, 0.34f, 1f);

    private final WorldSimulation simulation;
    private final Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final List<Button> buttons = new ArrayList<>();
    private String notice = "";
    private float noticeTime;

    public StrategyScreen(WorldSimulation simulation) {
        this.simulation = simulation;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        noticeTime = Math.max(0, noticeTime - delta);
        viewport.apply();
        Gdx.gl.glClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        buttons.clear();

        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeType.Filled);
        shapes.setColor(BACKGROUND);
        shapes.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        shapes.end();

        drawPanels();
        drawButtons();
        drawText();
    }

    private void drawPanels() {
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeType.Filled);
        panel(24, 24, 1_232, 72, PANEL);
        panel(24, 116, 530, 560, PANEL);
        panel(568, 116, 368, 560, PANEL);
        panel(950, 116, 306, 560, PANEL);
        shapes.end();

        shapes.begin(ShapeType.Line);
        shapes.setColor(LINE);
        border(24, 24, 1_232, 72);
        border(24, 116, 530, 560);
        border(568, 116, 368, 560);
        border(950, 116, 306, 560);
        shapes.end();
    }

    private void drawButtons() {
        GameState state = simulation.state();
        EventDefinition event = state.currentEvent();
        if (!state.finished() && event != null && !state.eventResolved()) {
            drawButton(new Rectangle(48, 184, 482, 62), event.choices().get(0).label(), ACCENT,
                    () -> execute(simulation.chooseEvent(0)));
            drawButton(new Rectangle(48, 112 + 8, 482, 62), event.choices().get(1).label(),
                    new Color(0.18f, 0.25f, 0.20f, 1f), () -> execute(simulation.chooseEvent(1)));
        }

        if (!state.finished()) {
            drawButton(new Rectangle(594, 570, 316, 42), "Festung an der Grenze bauen", WARNING,
                    () -> execute(simulation.build(BuildingType.FORT, RegionId.BORDER)));
            drawButton(new Rectangle(594, 518, 316, 42), "Militärfabrik errichten", WARNING,
                    () -> execute(simulation.build(BuildingType.MILITARY, RegionId.INDUSTRY)));
            drawButton(new Rectangle(594, 466, 316, 42), "Infanterie ausbilden", ACCENT,
                    () -> execute(simulation.train(UnitType.INFANTRY)));
            drawButton(new Rectangle(594, 414, 316, 42), "20 Stahl kaufen · 9.000 Geld", MUTED,
                    () -> execute(simulation.trade(TradeType.BUY_STEEL)));
            drawButton(new Rectangle(594, 362, 316, 42), "20 Treibstoff kaufen · 6.000 Geld", MUTED,
                    () -> execute(simulation.trade(TradeType.BUY_FUEL)));
            drawButton(new Rectangle(594, 310, 316, 42), "Geheimdienstoperation", MUTED,
                    () -> execute(simulation.intelligenceAction()));
            drawButton(new Rectangle(594, 52, 316, 48), state.eventResolved()
                    ? (state.day() == 10 ? "Kriegstest starten" : "Tag beenden")
                    : "Ereignis zuerst entscheiden", ACCENT,
                    () -> execute(simulation.endDay()));
        }
    }

    private void drawText() {
        GameState state = simulation.state();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        text("100 DAYS TO WAR", 42, 70, 22, ACCENT);
        text("Strategische Vorbereitung", 42, 45, 12, MUTED);
        text("TAG " + String.format(Locale.ROOT, "%02d", state.day()) + " / 10", 360, 67, 18, TEXT);
        text("Stabilität " + percent(state.stability()) + "   Moral " + percent(state.morale()), 360, 43, 12, MUTED);
        text("WELTLAGE", 970, 650, 11, ACCENT);
        text("TAGESEREIGNIS", 48, 650, 11, ACCENT);
        text("OPERATIONSZENTRALE", 594, 650, 11, ACCENT);
        text("RESSOURCEN & FRONT", 976, 650, 11, ACCENT);

        EventDefinition event = state.currentEvent();
        if (state.finished()) {
            drawReport(state);
        } else if (event != null) {
            text(event.title(), 48, 614, 21, TEXT);
            drawWrapped(event.description(), 48, 582, 460, 16, MUTED);
            if (state.eventResolved()) {
                text("Entscheidung notiert. Du kannst den Tag abschließen.", 48, 278, 13, ACCENT);
            } else {
                text(event.choices().get(0).consequence(), 60, 220, 11, MUTED);
                text(event.choices().get(1).consequence(), 60, 148, 11, MUTED);
            }
        }

        text("Bau, Produktion und Handel", 594, 618, 18, TEXT);
        text("Der Kern ist unabhängig vom Rendering testbar.", 594, 594, 12, MUTED);
        text("Laufende Projekte: " + state.construction().size(), 594, 276, 12, TEXT);
        text("Ausbildungsaufträge: " + state.production().size(), 594, 252, 12, TEXT);
        text("Bilanz / Tag: " + signed(simulation.dailyFlow(ResourceType.CASH)) + " Geld · "
                + signed(simulation.dailyFlow(ResourceType.FOOD)) + " Nahrung", 594, 226, 12,
                simulation.dailyFlow(ResourceType.CASH) >= 0 ? ACCENT : WARNING);

        drawResourceLines(state);
        text("Eigene Verbände", 976, 374, 12, ACCENT);
        for (UnitType type : UnitType.values()) {
            text(type.displayName() + ": " + state.armyCount(type), 976, 350 - type.ordinal() * 22, 12, TEXT);
        }
        text("Gegnerische Schätzung", 976, 242, 12, ACCENT);
        text("Infanterie: " + state.enemyCount(UnitType.INFANTRY), 976, 218, 12, TEXT);
        text("Moral: " + percent(state.enemyMorale()), 976, 196, 12, TEXT);
        text("Aufklärung: " + simulation.enemyEstimateQuality() + " %", 976, 174, 12, MUTED);

        if (noticeTime > 0) {
            text(notice, 42, 112, 12, notice.startsWith("OK") ? ACCENT : WARNING);
        }
        batch.end();
    }

    private void drawReport(GameState state) {
        WarReport report = state.report();
        text(report.victory() ? "SIEG — LAND VERTEIDIGT" : "NIEDERLAGE — FRONT DURCHBROCHEN",
                48, 610, 24, report.victory() ? ACCENT : WARNING);
        text("Kampfkraft-Verhältnis: " + report.ratio() + " %", 48, 574, 16, TEXT);
        text("Eigene Truppen: " + format(report.ownMen()) + " Mann", 48, 540, 13, TEXT);
        text("Gegner: " + format(report.enemyMen()) + " Mann", 48, 516, 13, TEXT);
        text("Eigene Kampfkraft: " + format(report.ownPower()), 48, 492, 13, TEXT);
        text("Gegnerische Kampfkraft: " + format(report.enemyPower()), 48, 468, 13, TEXT);
        text("Entscheidende Faktoren", 48, 420, 14, ACCENT);
        int y = 396;
        for (WarReport.Factor factor : report.factors()) {
            text(factor.name() + ": " + factor.value(), 48, y, 12, factor.positive() ? ACCENT : WARNING);
            y -= 22;
        }
        text("Die vollständige Nachbesprechung folgt im nächsten UI-Schritt.", 48, 210, 12, MUTED);
    }

    private void drawResourceLines(GameState state) {
        int y = 610;
        for (ResourceType type : ResourceType.values()) {
            text(type.displayName(), 976, y, 12, MUTED);
            text(format(state.resource(type)), 1_170, y, 13, TEXT);
            text(signed(simulation.dailyFlow(type)) + "/Tag", 976, y - 17, 10,
                    simulation.dailyFlow(type) >= 0 ? ACCENT : WARNING);
            y -= 62;
        }
    }

    private void execute(ActionResult result) {
        notice = (result.success() ? "OK: " : "Hinweis: ") + result.message();
        noticeTime = 4;
    }

    private void drawButton(Rectangle rectangle, String label, Color color, Runnable action) {
        buttons.add(new Button(rectangle, action));
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeType.Filled);
        shapes.setColor(new Color(color.r, color.g, color.b, .18f));
        shapes.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        shapes.end();
        shapes.begin(ShapeType.Line);
        shapes.setColor(color);
        shapes.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        shapes.end();
        batch.begin();
        text(label, rectangle.x + 12, rectangle.y + rectangle.height / 2 + 5, 12, color);
        batch.end();
    }

    private void panel(float x, float y, float width, float height, Color color) {
        shapes.setColor(color);
        shapes.rect(x, y, width, height);
    }

    private void border(float x, float y, float width, float height) {
        shapes.rect(x, y, width, height);
    }

    private void text(String value, float x, float y, float size, Color color) {
        font.getData().setScale(size / 15f);
        font.setColor(color);
        font.draw(batch, value, x, y);
    }

    private void drawWrapped(String value, float x, float y, int maxCharacters, float lineHeight, Color color) {
        for (String line : wrap(value, maxCharacters)) {
            text(line, x, y, 12, color);
            y -= lineHeight;
        }
    }

    private static List<String> wrap(String value, int maxCharacters) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : value.split(" ")) {
            if (current.length() + word.length() + 1 > maxCharacters && !current.isEmpty()) {
                lines.add(current.toString());
                current.setLength(0);
            }
            if (!current.isEmpty()) {
                current.append(' ');
            }
            current.append(word);
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private static String format(int value) {
        return String.format(Locale.GERMANY, "%,d", value);
    }

    private static String signed(int value) {
        return (value >= 0 ? "+" : "") + format(value);
    }

    private static String percent(double value) {
        return Math.round(value) + " %";
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector2 world = viewport.unproject(new Vector2(screenX, screenY));
        for (int index = buttons.size() - 1; index >= 0; index--) {
            Button candidate = buttons.get(index);
            if (candidate.bounds().contains(world.x, world.y)) {
                candidate.action().run();
                return true;
            }
        }
        return true;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }

    private record Button(Rectangle bounds, Runnable action) {
    }
}
