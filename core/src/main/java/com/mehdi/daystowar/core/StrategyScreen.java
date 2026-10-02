package com.mehdi.daystowar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The readable Veyra command screen. The simulation stays in WorldSimulation;
 * this class only renders and routes player input to it.
 */
public final class StrategyScreen extends InputAdapter implements Screen {
    private static final float WORLD_WIDTH = 1_440;
    private static final float WORLD_HEIGHT = 900;
    private static final Nation PLAYER_NATION = Nation.VALERIA;

    private static final float MAP_X = 340;
    private static final float MAP_Y = 84;
    private static final float MAP_WIDTH = 700;
    private static final float MAP_HEIGHT = 732;
    private static final float MAP_LEFT = 382;
    private static final float MAP_BOTTOM = 270;
    private static final float MAP_TILE_WIDTH = 62;
    private static final float MAP_TILE_HEIGHT = 54;
    private static final float MAP_STEP_X = 66;
    private static final float MAP_STEP_Y = 58;

    private static final Color BACKGROUND = new Color(0.025f, 0.045f, 0.07f, 1f);
    private static final Color PANEL = new Color(0.055f, 0.085f, 0.12f, 1f);
    private static final Color PANEL_ALT = new Color(0.075f, 0.11f, 0.15f, 1f);
    private static final Color LINE = new Color(0.17f, 0.27f, 0.34f, 1f);
    private static final Color SEA = new Color(0.035f, 0.13f, 0.18f, 1f);
    private static final Color RIVER = new Color(0.22f, 0.67f, 0.82f, 1f);
    private static final Color RAIL = new Color(0.78f, 0.68f, 0.37f, 1f);
    private static final Color TEXT = new Color(0.88f, 0.93f, 0.96f, 1f);
    private static final Color MUTED = new Color(0.55f, 0.66f, 0.72f, 1f);
    private static final Color ACCENT = new Color(0.63f, 0.9f, 0.96f, 1f);
    private static final Color WARNING = new Color(0.98f, 0.62f, 0.33f, 1f);
    private static final Color BUILDING = new Color(0.89f, 0.76f, 0.46f, 1f);
    private static final Color BUILDING_DARK = new Color(0.43f, 0.29f, 0.2f, 1f);

    private final WorldSimulation simulation;
    private final Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final List<Button> buttons = new ArrayList<>();
    private final List<MapRegionView> mapRegions = createMapRegions();

    private WorldRegion selectedRegion = WorldRegion.VALERIA_CAPITAL;
    private String notice = "";
    private float noticeTime;
    private float animationTime;

    public StrategyScreen(WorldSimulation simulation) {
        this.simulation = simulation;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        animationTime += delta;
        noticeTime = Math.max(0, noticeTime - delta);
        viewport.apply();
        Gdx.gl.glClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        buttons.clear();

        shapes.setProjectionMatrix(viewport.getCamera().combined);
        drawPanels();
        drawMapVisuals();
        drawButtons();
        drawText();
    }

    private void drawPanels() {
        shapes.begin(ShapeType.Filled);
        shapes.setColor(BACKGROUND);
        shapes.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        panel(24, 828, 1_392, 48, PANEL_ALT);
        panel(24, 84, 300, 732, PANEL);
        panel(MAP_X, MAP_Y, MAP_WIDTH, MAP_HEIGHT, PANEL);
        panel(1_056, 84, 360, 732, PANEL);

        for (int index = 0; index < 5; index++) {
            float x = 760 + index * 94;
            shapes.setColor(PANEL);
            shapes.rect(x, 836, 88, 32);
        }
        shapes.end();

        shapes.begin(ShapeType.Line);
        shapes.setColor(LINE);
        border(24, 828, 1_392, 48);
        border(24, 84, 300, 732);
        border(MAP_X, MAP_Y, MAP_WIDTH, MAP_HEIGHT);
        border(1_056, 84, 360, 732);
        shapes.end();
    }

    private void drawMapVisuals() {
        shapes.begin(ShapeType.Filled);
        shapes.setColor(SEA);
        shapes.rect(MAP_X + 24, MAP_Y + 136, MAP_WIDTH - 48, MAP_HEIGHT - 186);

        for (MapRegionView view : mapRegions) {
            Color country = nationColor(view.region().nation());
            shapes.setColor(country.r, country.g, country.b, 0.9f);
            drawFilledRegion(view.vertices());
            drawTerrainDecoration(view);
            drawAnimatedBuildings(view);
        }
        shapes.end();

        shapes.begin(ShapeType.Line);
        shapes.setColor(LINE);
        shapes.rect(MAP_X + 24, MAP_Y + 136, MAP_WIDTH - 48, MAP_HEIGHT - 186);
        for (MapRegionView view : mapRegions) {
            shapes.setColor(view.region() == selectedRegion ? ACCENT : LINE);
            shapes.polygon(view.vertices());
        }
        drawRiverAndRailways();
        shapes.setColor(ACCENT);
        shapes.line(centreX(WorldRegion.VALERIA_EASTMARCH), centreY(WorldRegion.VALERIA_EASTMARCH),
                centreX(WorldRegion.DRAVIK_VARKESH), centreY(WorldRegion.DRAVIK_VARKESH));
        shapes.end();

        shapes.begin(ShapeType.Filled);
        drawMovingMarker(WorldRegion.ASTER_RIVERGATE, WorldRegion.VALERIA_CAPITAL, RAIL, 0.1f);
        drawMovingMarker(WorldRegion.VALERIA_GREEN_BASIN, WorldRegion.ELDORIA_RIVERLANDS, RIVER, 0.62f);
        shapes.end();
    }

    private void drawTerrainDecoration(MapRegionView view) {
        float x = view.bounds().x + 10;
        float y = view.bounds().y + view.bounds().height - 17;
        switch (view.region().terrain()) {
            case MOUNTAINS -> {
                shapes.setColor(0.82f, 0.88f, 0.9f, 0.75f);
                shapes.triangle(x, y, x + 9, y + 13, x + 18, y);
                shapes.triangle(x + 11, y, x + 20, y + 10, x + 29, y);
            }
            case FOREST -> {
                shapes.setColor(0.19f, 0.48f, 0.38f, 0.85f);
                shapes.circle(x + 8, y + 5, 6, 10);
                shapes.circle(x + 18, y + 6, 7, 10);
                shapes.circle(x + 28, y + 4, 5, 10);
            }
            case DESERT -> {
                shapes.setColor(0.93f, 0.73f, 0.38f, 0.72f);
                shapes.rect(x, y + 4, 22, 2);
                shapes.rect(x + 8, y, 18, 2);
            }
            case COAST -> {
                shapes.setColor(0.32f, 0.75f, 0.85f, 0.85f);
                shapes.circle(x + 8, y + 4, 4, 10);
                shapes.circle(x + 19, y + 5, 3, 10);
            }
            case PASS -> {
                shapes.setColor(0.72f, 0.72f, 0.65f, 0.8f);
                shapes.triangle(x, y, x + 11, y + 14, x + 22, y);
            }
            case PLAINS -> {
                shapes.setColor(0.76f, 0.82f, 0.44f, 0.72f);
                shapes.rect(x + 2, y + 4, 22, 2);
            }
        }
    }

    private void drawAnimatedBuildings(MapRegionView view) {
        int buildingCount = view.region().nation() == PLAYER_NATION
                ? Math.max(1, Math.min(3, simulation.state().buildings(view.region().operationalRegion()).size()))
                : 1;
        float baseX = view.bounds().x + view.bounds().width - 27;
        float baseY = view.bounds().y + 10;
        float bob = (float) Math.sin(animationTime * 2.2f + view.region().ordinal()) * 1.4f;

        for (int index = 0; index < buildingCount; index++) {
            float x = baseX - index * 11;
            float y = baseY + bob + index * 1.5f;
            shapes.setColor(BUILDING);
            shapes.rect(x, y, 10, 8);
            shapes.setColor(BUILDING_DARK);
            shapes.triangle(x - 1, y + 8, x + 5, y + 14, x + 11, y + 8);
            boolean lightOn = Math.sin(animationTime * 3f + view.region().ordinal() + index) > -0.15f;
            if (lightOn) {
                shapes.setColor(ACCENT);
            } else {
                shapes.setColor(0.2f, 0.34f, 0.4f, 1f);
            }
            shapes.rect(x + 2, y + 3, 2, 2);
            shapes.rect(x + 6, y + 3, 2, 2);
        }

        float smoke = (animationTime * 7f + view.region().ordinal() * 1.7f) % 18f;
        shapes.setColor(0.78f, 0.86f, 0.88f, 0.32f);
        shapes.circle(baseX + 3 + smoke * .18f, baseY + 19 + smoke * .22f, 2.5f, 10);
        shapes.circle(baseX + 8 + smoke * .16f, baseY + 23 + smoke * .16f, 2f, 10);

        if (view.region().railway()) {
            shapes.setColor(RAIL);
            shapes.circle(view.bounds().x + 31, view.bounds().y + 8, 2.5f, 10);
        }
    }

    private void drawFilledRegion(float[] vertices) {
        for (int index = 2; index < vertices.length - 2; index += 2) {
            shapes.triangle(vertices[0], vertices[1], vertices[index], vertices[index + 1],
                    vertices[index + 2], vertices[index + 3]);
        }
    }

    private void drawRiverAndRailways() {
        shapes.setColor(RIVER);
        lineBetween(WorldRegion.NORVANE_FROSTPASS, WorldRegion.VALERIA_NORTHWATCH);
        lineBetween(WorldRegion.VALERIA_NORTHWATCH, WorldRegion.VALERIA_GREEN_BASIN);
        lineBetween(WorldRegion.VALERIA_GREEN_BASIN, WorldRegion.ELDORIA_RIVERLANDS);

        shapes.setColor(RAIL);
        lineBetween(WorldRegion.ASTER_RIVERGATE, WorldRegion.VALERIA_CAPITAL);
        lineBetween(WorldRegion.VALERIA_CAPITAL, WorldRegion.VALERIA_IRONVALE);
        lineBetween(WorldRegion.VALERIA_IRONVALE, WorldRegion.VALERIA_EASTMARCH);
        lineBetween(WorldRegion.VALERIA_EASTMARCH, WorldRegion.DRAVIK_VARKESH);
        lineBetween(WorldRegion.VALERIA_GREEN_BASIN, WorldRegion.ELDORIA_RIVERLANDS);
    }

    private void drawMovingMarker(WorldRegion from, WorldRegion to, Color color, float offset) {
        float progress = (animationTime * 0.16f + offset) % 1f;
        float x = centreX(from) + (centreX(to) - centreX(from)) * progress;
        float y = centreY(from) + (centreY(to) - centreY(from)) * progress;
        shapes.setColor(color);
        shapes.circle(x, y, 4, 12);
    }

    private void drawButtons() {
        GameState state = simulation.state();
        EventDefinition event = state.currentEvent();

        if (!state.finished() && event != null && !state.eventResolved()) {
            drawChoiceButton(new Rectangle(48, 310, 252, 92), event.choices().get(0), ACCENT,
                    () -> execute(simulation.chooseEvent(0)));
            drawChoiceButton(new Rectangle(48, 202, 252, 92), event.choices().get(1), PANEL_ALT,
                    () -> execute(simulation.chooseEvent(1)));
        }

        if (!state.finished()) {
            drawButton(new Rectangle(1_074, 226, 160, 34), "Festung bauen", WARNING,
                    () -> execute(buildSelected(BuildingType.FORT)));
            drawButton(new Rectangle(1_244, 226, 154, 34), "Militärfabrik", WARNING,
                    () -> execute(buildSelected(BuildingType.MILITARY)));
            drawButton(new Rectangle(1_074, 184, 160, 34), "Infanterie", ACCENT,
                    () -> execute(simulation.train(UnitType.INFANTRY)));
            drawButton(new Rectangle(1_244, 184, 154, 34), "Stahl kaufen", MUTED,
                    () -> execute(simulation.trade(TradeType.BUY_STEEL)));
            drawButton(new Rectangle(1_074, 142, 160, 34), "Treibstoff", MUTED,
                    () -> execute(simulation.trade(TradeType.BUY_FUEL)));
            drawButton(new Rectangle(1_244, 142, 154, 34), "Aufklärung", MUTED,
                    () -> execute(simulation.intelligenceAction()));

            String endDay = state.eventResolved() ? (state.day() == 10 ? "KRIEGSTEST" : "TAG BEENDEN")
                    : "ENTSCHEIDUNG NÖTIG";
            drawButton(new Rectangle(1_252, 836, 150, 32), endDay, ACCENT,
                    () -> execute(simulation.endDay()));
        }
    }

    private void drawChoiceButton(Rectangle rectangle, EventDefinition.Choice choice, Color color,
                                  Runnable action) {
        drawButtonFrame(rectangle, color, action);
        batch.begin();
        drawWrapped(choice.label(), rectangle.x + 12, rectangle.y + rectangle.height - 24,
                30, 14, 12, TEXT);
        drawWrapped(choice.consequence(), rectangle.x + 12, rectangle.y + 39,
                33, 13, 10, MUTED);
        batch.end();
    }

    private void drawButton(Rectangle rectangle, String label, Color color, Runnable action) {
        drawButtonFrame(rectangle, color, action);
        batch.begin();
        text(label, rectangle.x + 10, rectangle.y + rectangle.height / 2 + 4, 10, TEXT);
        batch.end();
    }

    private void drawButtonFrame(Rectangle rectangle, Color color, Runnable action) {
        buttons.add(new Button(rectangle, action));
        shapes.begin(ShapeType.Filled);
        shapes.setColor(color.r, color.g, color.b, 0.16f);
        shapes.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        shapes.end();
        shapes.begin(ShapeType.Line);
        shapes.setColor(color);
        shapes.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        shapes.end();
    }

    private void drawText() {
        GameState state = simulation.state();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        drawHeader(state);
        drawMapLabels();
        drawSituationText();
        drawRegionDetails(state);

        if (state.finished()) {
            drawReport(state);
        } else {
            drawEventText(state);
        }

        if (noticeTime > 0) {
            text(notice, 48, 112, 11, notice.startsWith("OK") ? ACCENT : WARNING);
        }
        batch.end();
    }

    private void drawHeader(GameState state) {
        text("100 DAYS TO WAR", 48, 858, 20, ACCENT);
        text("VEYRA // FLEXIBLE DEFENSE", 48, 840, 10, MUTED);
        text("TAG " + String.format(Locale.ROOT, "%02d", state.day()) + " / 10", 300, 858, 18, TEXT);
        text("Stabilität " + percent(state.stability()) + "  ·  Moral " + percent(state.morale()), 300, 840, 10, MUTED);
        text("WELTLAGE: " + worldStatus(state), 520, 858, 11, worldStatus(state).equals("kritisch") ? WARNING : ACCENT);
        text("Veyr River  ·  Eastmarch-Krise  ·  Eisenbahnnetz aktiv", 520, 840, 9, MUTED);

        for (int index = 0; index < 5; index++) {
            ResourceType type = ResourceType.values()[index];
            float x = 760 + index * 94;
            text(shortResource(type), x + 7, 858, 8, MUTED);
            text(format(state.resource(type)), x + 7, 843, 10, TEXT);
        }
    }

    private void drawMapLabels() {
        text("VEYRA // STRATEGISCHE KARTE", 366, 792, 14, TEXT);
        text("30 Regionen · sechs Staaten · Regionen anklicken", 366, 773, 10, MUTED);

        for (MapRegionView view : mapRegions) {
            String number = String.format(Locale.ROOT, "%02d", view.region().ordinal() + 1);
            Color labelColor = view.region() == selectedRegion ? TEXT : MUTED;
            text(number, view.labelX(), view.labelY(), view.region() == selectedRegion ? 10 : 8, labelColor);
        }

        text("NORVANE", 584, 742, 9, nationColor(Nation.NORVANE));
        text("ASTER", 390, 626, 9, nationColor(Nation.ASTER));
        text("VALERIA", 590, 564, 9, nationColor(Nation.VALERIA));
        text("DRAVIK", 824, 626, 9, nationColor(Nation.DRAVIK));
        text("ELDORIA", 590, 366, 9, nationColor(Nation.ELDORIA));
        text("KARSEN", 590, 282, 9, nationColor(Nation.KARSEN));

        text("Fluss", 382, 224, 9, RIVER);
        text("Eisenbahn", 430, 224, 9, RAIL);
        text("Front", 515, 224, 9, WARNING);
        text("Nummer wählen → Details rechts", 620, 224, 9, MUTED);
    }

    private void drawSituationText() {
        text("EASTMARCH-KRISE", 366, 190, 10, WARNING);
        text("Varkesh ↔ Eastmarch  ·  offene Ebene  ·  Panzerkorridor", 366, 171, 11, TEXT);
        text("Flussüberquerung: −20 % Angriff  ·  Eisenbahn verbessert Versorgung und Bewegung", 366, 151, 9, MUTED);
        text("Gebäude pulsieren live: Fenster, Rauch, Bahnverkehr und Baustellen", 366, 112, 9, ACCENT);
    }

    private void drawEventText(GameState state) {
        EventDefinition event = state.currentEvent();
        text("TAGESLAGE", 48, 792, 11, ACCENT);
        if (event == null) {
            text("Keine neue Meldung", 48, 750, 16, TEXT);
        } else {
            drawWrapped(event.title(), 48, 752, 31, 20, 16, TEXT);
            drawWrapped(event.description(), 48, 700, 35, 16, 11, MUTED);
            text(state.eventResolved() ? "Entscheidung gespeichert" : "ENTSCHEIDUNG AUSWÄHLEN",
                    48, 442, 10, state.eventResolved() ? ACCENT : WARNING);
        }

        text("LETZTE MELDUNGEN", 48, 166, 10, ACCENT);
        int y = 144;
        for (String entry : state.history().stream().limit(3).toList()) {
            drawWrapped(entry, 48, y, 34, 13, 9, MUTED);
            y -= 30;
        }
    }

    private void drawRegionDetails(GameState state) {
        text("REGION", 1_074, 792, 10, ACCENT);
        text(selectedRegion.displayName(), 1_074, 758, 18, TEXT);
        text(selectedRegion.nation().displayName() + "  ·  " + selectedRegion.terrain().displayName(),
                1_074, 737, 10, nationColor(selectedRegion.nation()));

        detailRow("Bevölkerung", formatMillions(selectedRegion.population()), 698);
        detailRow("Infrastruktur", selectedRegion.infrastructure() + " / 100", 672);
        detailRow("Ressourcen", selectedRegion.resources(), 646);
        detailRow("Festung", "Stufe " + selectedRegion.fortressLevel(), 620);
        detailRow("Eisenbahn", selectedRegion.railway() ? "angeschlossen" : "nicht angeschlossen", 594);
        detailRow("Bauplätze", selectedRegion.operationalRegion().slots() + " strategische Plätze", 568);

        text("AKTIVE GEBÄUDE", 1_074, 520, 10, ACCENT);
        List<BuildingType> buildings = state.buildings(selectedRegion.operationalRegion());
        if (buildings.isEmpty()) {
            text("Keine aktiven Gebäude", 1_074, 494, 10, MUTED);
        } else {
            int y = 494;
            for (BuildingType building : buildings.stream().limit(3).toList()) {
                text("• " + building.displayName(), 1_074, y, 10, TEXT);
                y -= 21;
            }
        }
        if (!state.construction().isEmpty()) {
            ConstructionProject project = state.construction().get(0);
            text("Bau: " + project.type().displayName() + "  " + project.progressPercent() + "%",
                    1_074, 415, 9, WARNING);
        }

        text("OPERATIONSZENTRALE", 1_074, 316, 10, ACCENT);
        text(selectedRegion.nation() == PLAYER_NATION
                ? "Bauaktionen wirken in der ausgewählten Region."
                : "Nur Valeria kann in dieser Phase bauen.", 1_074, 294, 9,
                selectedRegion.nation() == PLAYER_NATION ? MUTED : WARNING);
    }

    private void detailRow(String label, String value, int y) {
        text(label, 1_074, y, 9, MUTED);
        drawWrapped(value, 1_188, y, 18, 12, 9, TEXT);
    }

    private void drawReport(GameState state) {
        WarReport report = state.report();
        text("KRIEGSBERICHT", 48, 752, 11, ACCENT);
        text(report.victory() ? "SIEG — VALERIA HÄLT" : "NIEDERLAGE — FRONT DURCHBROCHEN",
                48, 714, 15, report.victory() ? ACCENT : WARNING);
        text("Kampfkraft-Verhältnis: " + report.ratio() + " %", 48, 680, 11, TEXT);
        text("Eigene Truppen: " + format(report.ownMen()), 48, 654, 10, TEXT);
        text("Gegner: " + format(report.enemyMen()), 48, 632, 10, TEXT);
        text("Entscheidende Faktoren", 48, 590, 10, ACCENT);
        int y = 566;
        for (WarReport.Factor factor : report.factors()) {
            text(factor.name() + ": " + factor.value(), 48, y, 9, factor.positive() ? ACCENT : WARNING);
            y -= 19;
        }
    }

    private ActionResult buildSelected(BuildingType type) {
        if (selectedRegion.nation() != PLAYER_NATION) {
            return ActionResult.rejected("Bauaufträge sind nur in Valeria möglich.");
        }
        return simulation.build(type, selectedRegion.operationalRegion());
    }

    private void execute(ActionResult result) {
        notice = (result.success() ? "OK: " : "Hinweis: ") + result.message();
        noticeTime = 4;
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

    private void drawWrapped(String value, float x, float y, int maxCharacters, float lineHeight,
                             float size, Color color) {
        for (String line : wrap(value, maxCharacters)) {
            text(line, x, y, size, color);
            y -= lineHeight;
        }
    }

    private static List<String> wrap(String value, int maxCharacters) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : value.split("\\s+")) {
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

    private float centreX(WorldRegion region) {
        return mapRegions.stream().filter(view -> view.region() == region).findFirst()
                .map(MapRegionView::centerX).orElse(MAP_LEFT);
    }

    private float centreY(WorldRegion region) {
        return mapRegions.stream().filter(view -> view.region() == region).findFirst()
                .map(MapRegionView::centerY).orElse(MAP_BOTTOM);
    }

    private static List<MapRegionView> createMapRegions() {
        List<MapRegionView> regions = new ArrayList<>();
        add(regions, WorldRegion.NORVANE_CAPITAL, 3, 7);
        add(regions, WorldRegion.NORVANE_FROSTPASS, 4, 7);
        add(regions, WorldRegion.NORVANE_SKELD_FOREST, 2, 6);
        add(regions, WorldRegion.NORVANE_HELIX, 3, 6);
        add(regions, WorldRegion.NORVANE_KALDVIK, 4, 6);

        add(regions, WorldRegion.ASTER_CAPITAL, 0, 5);
        add(regions, WorldRegion.ASTER_WESTPORT, 1, 5);
        add(regions, WorldRegion.ASTER_FOUNDRY_COAST, 0, 6);
        add(regions, WorldRegion.ASTER_SILVER_HILLS, 1, 6);
        add(regions, WorldRegion.ASTER_RIVERGATE, 2, 5);

        add(regions, WorldRegion.VALERIA_CAPITAL, 3, 5);
        add(regions, WorldRegion.VALERIA_IRONVALE, 4, 5);
        add(regions, WorldRegion.VALERIA_EASTMARCH, 5, 4);
        add(regions, WorldRegion.VALERIA_NORTHWATCH, 3, 4);
        add(regions, WorldRegion.VALERIA_GREEN_BASIN, 4, 4);

        add(regions, WorldRegion.DRAVIK_CAPITAL, 6, 5);
        add(regions, WorldRegion.DRAVIK_REDWORKS, 7, 5);
        add(regions, WorldRegion.DRAVIK_IRON_STEPPE, 6, 6);
        add(regions, WorldRegion.DRAVIK_VARKESH, 6, 4);
        add(regions, WorldRegion.DRAVIK_SOUTHERN_CORRIDOR, 8, 4);

        add(regions, WorldRegion.ELDORIA_CAPITAL, 3, 2);
        add(regions, WorldRegion.ELDORIA_GOLDEN_FIELDS, 4, 2);
        add(regions, WorldRegion.ELDORIA_RIVERLANDS, 3, 1);
        add(regions, WorldRegion.ELDORIA_SOUTHWATCH, 4, 1);
        add(regions, WorldRegion.ELDORIA_ORCHARD_COAST, 5, 1);

        add(regions, WorldRegion.KARSEN_CAPITAL, 2, 0);
        add(regions, WorldRegion.KARSEN_BLACK_WELLS, 3, 0);
        add(regions, WorldRegion.KARSEN_SUN_BASIN, 4, 0);
        add(regions, WorldRegion.KARSEN_REFINERY_COAST, 5, 0);
        add(regions, WorldRegion.KARSEN_NORTHERN_PIPELINE, 6, 0);
        return List.copyOf(regions);
    }

    private static void add(List<MapRegionView> regions, WorldRegion region, int column, int row) {
        float x = MAP_LEFT + column * MAP_STEP_X;
        float y = MAP_BOTTOM + row * MAP_STEP_Y;
        float width = MAP_TILE_WIDTH;
        float height = MAP_TILE_HEIGHT;
        float[] vertices = {
                x + 5, y + 7,
                x + 25, y,
                x + width - 6, y + 4,
                x + width, y + height - 11,
                x + width - 12, y + height,
                x + 9, y + height - 3,
                x, y + height - 17
        };
        Rectangle bounds = new Rectangle(x, y, width, height);
        regions.add(new MapRegionView(region, bounds, vertices, x + 27, y + 30));
    }

    private static Color nationColor(Nation nation) {
        return switch (nation) {
            case NORVANE -> new Color(0.26f, 0.66f, 0.78f, 1f);
            case ASTER -> new Color(0.68f, 0.48f, 0.82f, 1f);
            case VALERIA -> new Color(0.22f, 0.55f, 0.78f, 1f);
            case DRAVIK -> new Color(0.78f, 0.29f, 0.25f, 1f);
            case ELDORIA -> new Color(0.29f, 0.64f, 0.43f, 1f);
            case KARSEN -> new Color(0.78f, 0.58f, 0.28f, 1f);
        };
    }

    private void lineBetween(WorldRegion from, WorldRegion to) {
        shapes.line(centreX(from), centreY(from), centreX(to), centreY(to));
    }

    private static String shortResource(ResourceType type) {
        return switch (type) {
            case CASH -> "GELD";
            case FOOD -> "NAHRUNG";
            case STEEL -> "STAHL";
            case FUEL -> "TREIBSTOFF";
            case MANPOWER -> "MANN";
        };
    }

    private static String format(int value) {
        return String.format(Locale.GERMANY, "%,d", value);
    }

    private static String formatMillions(int population) {
        return String.format(Locale.GERMANY, "%.1f Mio.", population / 1_000_000d);
    }

    private static String percent(double value) {
        return Math.round(value) + " %";
    }

    private static String worldStatus(GameState state) {
        if (state.day() >= 9) {
            return "kritisch";
        }
        if (state.day() >= 5) {
            return "angespannt";
        }
        return "stabil";
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
        for (int index = mapRegions.size() - 1; index >= 0; index--) {
            MapRegionView candidate = mapRegions.get(index);
            if (candidate.bounds().contains(world.x, world.y)) {
                selectedRegion = candidate.region();
                notice = "OK: " + selectedRegion.displayName() + " ausgewählt.";
                noticeTime = 2.5f;
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

    private record MapRegionView(WorldRegion region, Rectangle bounds, float[] vertices,
                                 float labelX, float labelY) {
        private float centerX() {
            return bounds.x + bounds.width / 2f;
        }

        private float centerY() {
            return bounds.y + bounds.height / 2f;
        }
    }
}
