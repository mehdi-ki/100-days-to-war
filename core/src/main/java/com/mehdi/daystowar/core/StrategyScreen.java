package com.mehdi.daystowar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Veyra command screen. The map is an interactive atlas: map content is
 * rendered in world coordinates while the surrounding command UI stays fixed.
 * Simulation rules remain in {@link WorldSimulation}.
 */
public final class StrategyScreen extends InputAdapter implements Screen {
    private static final float WORLD_WIDTH = 1_440;
    private static final float WORLD_HEIGHT = 900;
    private static final Nation PLAYER_NATION = Nation.VALERIA;

    private static final float MAP_X = 340;
    private static final float MAP_Y = 84;
    private static final float MAP_WIDTH = 700;
    private static final float MAP_HEIGHT = 732;
    private static final float MAP_AREA_X = MAP_X + 24;
    private static final float MAP_AREA_Y = MAP_Y + 136;
    private static final float MAP_AREA_WIDTH = MAP_WIDTH - 48;
    private static final float MAP_AREA_HEIGHT = MAP_HEIGHT - 186;
    private static final float MAP_LEFT = 382;
    private static final float MAP_BOTTOM = 270;
    private static final float MAP_TILE_WIDTH = 64;
    private static final float MAP_TILE_HEIGHT = 56;
    private static final float MAP_STEP_X = 66;
    private static final float MAP_STEP_Y = 58;
    private static final float MAP_CENTER_X = MAP_AREA_X + MAP_AREA_WIDTH / 2f;
    private static final float MAP_CENTER_Y = MAP_AREA_Y + MAP_AREA_HEIGHT / 2f;
    private static final float MAP_CONTENT_LEFT = MAP_LEFT - 24;
    private static final float MAP_CONTENT_RIGHT = MAP_LEFT + 8 * MAP_STEP_X + MAP_TILE_WIDTH + 24;
    private static final float MAP_CONTENT_BOTTOM = MAP_BOTTOM - 24;
    private static final float MAP_CONTENT_TOP = MAP_BOTTOM + 7 * MAP_STEP_Y + MAP_TILE_HEIGHT + 24;
    private static final float FONT_BASE_SIZE = 24f;

    private static final Color BACKGROUND = new Color(0.025f, 0.045f, 0.07f, 1f);
    private static final Color PANEL = new Color(0.055f, 0.085f, 0.12f, 1f);
    private static final Color PANEL_ALT = new Color(0.075f, 0.11f, 0.15f, 1f);
    private static final Color LINE = new Color(0.17f, 0.27f, 0.34f, 1f);
    private static final Color SEA = new Color(0.025f, 0.18f, 0.26f, 1f);
    private static final Color RIVER = new Color(0.25f, 0.78f, 0.92f, 1f);
    private static final Color RAIL = new Color(0.86f, 0.72f, 0.35f, 1f);
    private static final Color TEXT = new Color(0.9f, 0.94f, 0.96f, 1f);
    private static final Color MUTED = new Color(0.58f, 0.68f, 0.74f, 1f);
    private static final Color ACCENT = new Color(0.72f, 0.91f, 0.56f, 1f);
    private static final Color WARNING = new Color(0.98f, 0.62f, 0.33f, 1f);
    private static final Color BUILDING = new Color(0.94f, 0.78f, 0.45f, 1f);
    private static final Color BUILDING_DARK = new Color(0.36f, 0.22f, 0.16f, 1f);
    private static final Color POPUP = new Color(0.055f, 0.075f, 0.1f, 1f);
    private static final Color ACCENT_SURFACE = new Color(0.13f, 0.28f, 0.17f, 1f);
    private static final Color WARNING_SURFACE = new Color(0.30f, 0.20f, 0.10f, 1f);
    private static final Color MUTED_SURFACE = new Color(0.10f, 0.15f, 0.19f, 1f);

    private final WorldSimulation simulation;
    private final Viewport viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final List<Button> buttons = new ArrayList<>();
    private final List<BuildingHitBox> buildingHitBoxes = new ArrayList<>();
    private final List<MapRegionView> mapRegions = createMapRegions();
    private final FreeTypeFontGenerator fontGenerator;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private WorldRegion selectedRegion = WorldRegion.VALERIA_CAPITAL;
    private OperationsTab operationsTab = OperationsTab.BUILD;
    private BuildingHitBox selectedBuilding;
    private boolean buildingPopupOpen;
    private String notice = "";
    private float noticeTime;
    private float animationTime;
    private float mapZoom = 1f;
    private float mapPanX;
    private float mapPanY;
    private boolean panningMap;
    private boolean mapDragMoved;
    private float lastDragX;
    private float lastDragY;

    public StrategyScreen(WorldSimulation simulation) {
        this.simulation = simulation;
        FileHandle fontFile = Gdx.files.internal("fonts/DejaVuSans.ttf");
        if (fontFile.exists()) {
            fontGenerator = new FreeTypeFontGenerator(fontFile);
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 24;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ÄÖÜäöüß…·↔→–—";
            parameter.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            parameter.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            font = fontGenerator.generateFont(parameter);
            font.setUseIntegerPositions(false);
        } else {
            fontGenerator = null;
            font = new BitmapFont();
        }
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
        buildingHitBoxes.clear();

        shapes.setProjectionMatrix(viewport.getCamera().combined);
        drawPanels();
        drawMapVisuals();
        drawButtons();
        drawText();
        if (buildingPopupOpen) {
            drawBuildingPopup();
        }
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
        shapes.rect(MAP_AREA_X, MAP_AREA_Y, MAP_AREA_WIDTH, MAP_AREA_HEIGHT);
        shapes.end();

        beginMapClip();
        shapes.begin(ShapeType.Filled);
        drawSeaTexture();
        for (MapRegionView view : mapRegions) {
            Color country = nationColor(view.region().nation());
            shapes.setColor(country.r, country.g, country.b, 0.93f);
            drawFilledRegion(transformedVertices(view));
            drawTerrainDecoration(view);
            drawStrategicLandmarks(view);
            drawAnimatedBuildings(view);
        }
        shapes.end();

        shapes.begin(ShapeType.Line);
        shapes.setColor(LINE);
        for (MapRegionView view : mapRegions) {
            shapes.setColor(view.region() == selectedRegion ? ACCENT : LINE);
            shapes.polygon(transformedVertices(view));
        }
        drawRiverAndRailways();
        shapes.setColor(WARNING);
        shapes.line(centreX(WorldRegion.VALERIA_EASTMARCH), centreY(WorldRegion.VALERIA_EASTMARCH),
                centreX(WorldRegion.DRAVIK_VARKESH), centreY(WorldRegion.DRAVIK_VARKESH));
        drawFrontMarkers();
        shapes.end();

        shapes.begin(ShapeType.Filled);
        drawMovingMarker(WorldRegion.ASTER_RIVERGATE, WorldRegion.VALERIA_CAPITAL, RAIL, 0.1f);
        drawMovingMarker(WorldRegion.VALERIA_GREEN_BASIN, WorldRegion.ELDORIA_RIVERLANDS, RIVER, 0.62f);
        shapes.end();
        endMapClip();
    }

    private void drawSeaTexture() {
        shapes.setColor(SEA);
        shapes.rect(MAP_AREA_X - 60, MAP_AREA_Y - 60, MAP_AREA_WIDTH + 120, MAP_AREA_HEIGHT + 120);
        shapes.setColor(0.06f, 0.34f, 0.43f, 0.32f);
        for (int index = 0; index < 18; index++) {
            float x = MAP_AREA_X - 40 + ((index * 83) % 730);
            float y = MAP_AREA_Y - 20 + ((index * 47) % 600);
            shapes.circle(x, y, 1.5f + (index % 3), 8);
        }
    }

    private void drawTerrainDecoration(MapRegionView view) {
        float scale = mapZoom;
        float x = mapPointX(view.bounds().x + 9);
        float y = mapPointY(view.bounds().y + view.bounds().height - 18);
        switch (view.region().terrain()) {
            case MOUNTAINS -> {
                shapes.setColor(0.86f, 0.91f, 0.94f, 0.75f);
                triangle(x, y, x + 9 * scale, y + 14 * scale, x + 18 * scale, y);
                triangle(x + 11 * scale, y, x + 20 * scale, y + 11 * scale, x + 30 * scale, y);
            }
            case FOREST -> {
                shapes.setColor(0.12f, 0.34f, 0.25f, 0.92f);
                shapes.circle(x + 8 * scale, y + 5 * scale, 6 * scale, 10);
                shapes.circle(x + 18 * scale, y + 6 * scale, 7 * scale, 10);
                shapes.circle(x + 29 * scale, y + 4 * scale, 5 * scale, 10);
            }
            case DESERT -> {
                shapes.setColor(0.95f, 0.72f, 0.36f, 0.72f);
                shapes.rect(x, y + 4 * scale, 22 * scale, 2 * scale);
                shapes.rect(x + 8 * scale, y, 18 * scale, 2 * scale);
            }
            case COAST -> {
                shapes.setColor(0.4f, 0.82f, 0.87f, 0.85f);
                shapes.circle(x + 8 * scale, y + 4 * scale, 4 * scale, 10);
                shapes.circle(x + 20 * scale, y + 5 * scale, 3 * scale, 10);
            }
            case PASS -> {
                shapes.setColor(0.75f, 0.75f, 0.67f, 0.82f);
                triangle(x, y, x + 11 * scale, y + 14 * scale, x + 22 * scale, y);
            }
            case PLAINS -> {
                shapes.setColor(0.84f, 0.86f, 0.48f, 0.8f);
                shapes.rect(x + 2 * scale, y + 4 * scale, 22 * scale, 2 * scale);
            }
        }
    }

    private void drawStrategicLandmarks(MapRegionView view) {
        float scale = mapZoom;
        float x = mapPointX(view.bounds().x + view.bounds().width * .52f);
        float y = mapPointY(view.bounds().y + view.bounds().height * .48f);
        String resources = view.region().resources();

        if (view.region().operationalRegion() == RegionId.CAPITAL) {
            shapes.setColor(0.98f, 0.88f, 0.48f, .98f);
            shapes.circle(x, y, 4.2f * scale, 8);
            shapes.setColor(0.24f, 0.16f, 0.12f, .9f);
            shapes.rect(x - 1.2f * scale, y - 8 * scale, 2.4f * scale, 16 * scale);
            shapes.rect(x - 8 * scale, y - 1.2f * scale, 16 * scale, 2.4f * scale);
        } else if (resources.contains("Öl") || resources.contains("Energie")) {
            shapes.setColor(0.17f, 0.15f, 0.13f, .95f);
            shapes.rect(x - 1.4f * scale, y - 10 * scale, 2.8f * scale, 20 * scale);
            shapes.rect(x - 7 * scale, y - 7 * scale, 14 * scale, 1.8f * scale);
            shapes.rect(x - 5 * scale, y + 4 * scale, 10 * scale, 1.8f * scale);
        } else if (resources.contains("Stahl") || resources.contains("Industrie")) {
            shapes.setColor(0.23f, 0.24f, 0.24f, .95f);
            shapes.rect(x - 8 * scale, y - 5 * scale, 4 * scale, 11 * scale);
            shapes.rect(x - 2 * scale, y - 8 * scale, 4 * scale, 14 * scale);
            shapes.rect(x + 4 * scale, y - 3 * scale, 4 * scale, 9 * scale);
        } else if (resources.contains("Nahrung") || resources.contains("Getreide")
                || resources.contains("Landwirtschaft")) {
            shapes.setColor(0.92f, 0.79f, 0.26f, .9f);
            for (int stripe = -2; stripe <= 2; stripe++) {
                shapes.rect(x + stripe * 4 * scale, y - 7 * scale, 1.5f * scale, 14 * scale);
            }
        }
    }

    private void drawAnimatedBuildings(MapRegionView view) {
        List<BuildingType> actualBuildings = simulation.state().buildings(view.region().operationalRegion());
        int buildingCount = view.region().nation() == PLAYER_NATION
                ? Math.max(1, Math.min(3, actualBuildings.size())) : 1;
        float scale = mapZoom;
        float baseX = mapPointX(view.bounds().x + view.bounds().width - 27);
        float baseY = mapPointY(view.bounds().y + 10);
        float bob = (float) Math.sin(animationTime * 2.2f + view.region().ordinal()) * 1.4f * scale;

        for (int index = 0; index < buildingCount; index++) {
            float x = baseX - index * 11 * scale;
            float y = baseY + bob + index * 1.5f * scale;
            BuildingType type = actualBuildings.isEmpty()
                    ? BuildingType.FORT : actualBuildings.get(index % actualBuildings.size());
            shapes.setColor(BUILDING);
            shapes.rect(x, y, 10 * scale, 8 * scale);
            shapes.setColor(BUILDING_DARK);
            triangle(x - scale, y + 8 * scale, x + 5 * scale, y + 14 * scale, x + 11 * scale, y + 8 * scale);
            shapes.setColor((Math.sin(animationTime * 3f + view.region().ordinal() + index) > -0.15f)
                    ? ACCENT : new Color(0.2f, 0.34f, 0.4f, 1f));
            shapes.rect(x + 2 * scale, y + 3 * scale, 2 * scale, 2 * scale);
            shapes.rect(x + 6 * scale, y + 3 * scale, 2 * scale, 2 * scale);
            if (view.region().nation() == PLAYER_NATION && !actualBuildings.isEmpty()) {
                buildingHitBoxes.add(new BuildingHitBox(view.region(), type,
                        new Rectangle(x - 4 * scale, y - 4 * scale, 18 * scale, 21 * scale)));
            }
        }

        float smoke = (animationTime * 7f + view.region().ordinal() * 1.7f) % 18f;
        shapes.setColor(0.85f, 0.9f, 0.92f, 0.34f);
        shapes.circle(baseX + 3 * scale + smoke * .18f * scale,
                baseY + 19 * scale + smoke * .22f * scale, 2.5f * scale, 10);
        shapes.circle(baseX + 8 * scale + smoke * .16f * scale,
                baseY + 23 * scale + smoke * .16f * scale, 2f * scale, 10);

        if (view.region().railway()) {
            shapes.setColor(RAIL);
            shapes.circle(mapPointX(view.bounds().x + 31), mapPointY(view.bounds().y + 8), 2.5f * scale, 10);
        }
    }

    private void drawFrontMarkers() {
        float progress = (animationTime * .6f) % 1f;
        float x = centreX(WorldRegion.VALERIA_EASTMARCH)
                + (centreX(WorldRegion.DRAVIK_VARKESH) - centreX(WorldRegion.VALERIA_EASTMARCH)) * progress;
        float y = centreY(WorldRegion.VALERIA_EASTMARCH)
                + (centreY(WorldRegion.DRAVIK_VARKESH) - centreY(WorldRegion.VALERIA_EASTMARCH)) * progress;
        shapes.setColor(WARNING);
        shapes.circle(x, y, 3.5f * mapZoom, 10);
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
        float progress = (animationTime * .16f + offset) % 1f;
        float x = centreX(from) + (centreX(to) - centreX(from)) * progress;
        float y = centreY(from) + (centreY(to) - centreY(from)) * progress;
        shapes.setColor(color);
        shapes.circle(x, y, 4 * mapZoom, 12);
    }

    private void drawButtons() {
        GameState state = simulation.state();
        EventDefinition event = state.currentEvent();
        if (buildingPopupOpen) {
            return;
        }

        if (!state.finished() && event != null && !state.eventResolved()) {
            drawChoiceButton(new Rectangle(48, 310, 252, 92), event.choices().get(0), ACCENT,
                    () -> execute(simulation.chooseEvent(0)));
            drawChoiceButton(new Rectangle(48, 202, 252, 92), event.choices().get(1), PANEL_ALT,
                    () -> execute(simulation.chooseEvent(1)));
        }

        drawButton(new Rectangle(MAP_X + MAP_WIDTH - 86, 784, 66, 24), "RESET", MUTED, this::resetMapView);

        if (!state.finished()) {
            drawOperationsControls();
            String endDay = state.eventResolved() ? (state.day() == 10 ? "KRIEGSTEST" : "TAG BEENDEN")
                    : "ENTSCHEIDUNG NÖTIG";
            drawButton(new Rectangle(1_252, 836, 150, 32), endDay, ACCENT,
                    () -> execute(simulation.endDay()));
        }
    }

    private void drawOperationsControls() {
        OperationsTab[] tabs = OperationsTab.values();
        for (int index = 0; index < tabs.length; index++) {
            OperationsTab tab = tabs[index];
            int row = index / 4;
            int column = index % 4;
            float x = 1_074 + column * 80;
            float y = 296 - row * 30;
            Color color = tab == operationsTab ? ACCENT : PANEL_ALT;
            drawButton(new Rectangle(x, y, 74, 24), tab.label(), color, () -> operationsTab = tab);
        }

        switch (operationsTab) {
            case BUILD -> {
                drawButton(new Rectangle(1_074, 174, 156, 32), "Festung bauen", WARNING,
                        () -> execute(buildSelected(BuildingType.FORT)));
                drawButton(new Rectangle(1_242, 174, 156, 32), "Militärfabrik", WARNING,
                        () -> execute(buildSelected(BuildingType.MILITARY)));
            }
            case MILITARY -> {
                drawButton(new Rectangle(1_074, 174, 324, 32), "Infanterie ausbilden", ACCENT,
                        () -> execute(simulation.train(UnitType.INFANTRY)));
                drawButton(new Rectangle(1_074, 132, 324, 32), "Artillerie ausbilden", MUTED,
                        () -> execute(simulation.train(UnitType.ARTILLERY)));
            }
            case TRADE -> {
                drawButton(new Rectangle(1_074, 174, 156, 32), "Stahl kaufen", MUTED,
                        () -> execute(simulation.trade(TradeType.BUY_STEEL)));
                drawButton(new Rectangle(1_242, 174, 156, 32), "Treibstoff kaufen", MUTED,
                        () -> execute(simulation.trade(TradeType.BUY_FUEL)));
                drawButton(new Rectangle(1_074, 132, 324, 32), "Nahrung verkaufen", PANEL_ALT,
                        () -> execute(simulation.trade(TradeType.SELL_FOOD)));
            }
            case INTELLIGENCE -> drawButton(new Rectangle(1_074, 174, 324, 32), "Aufklärung starten", ACCENT,
                    () -> execute(simulation.intelligenceAction()));
            case DIPLOMACY -> drawButton(new Rectangle(1_074, 174, 324, 32), "Beziehungsbericht", MUTED,
                    () -> showNotice("Diplomatiebericht: Valeria hält die Nachbarstaaten unter Beobachtung."));
            case LOGISTICS -> drawButton(new Rectangle(1_074, 174, 324, 32), "Versorgungsbericht", MUTED,
                    () -> showNotice("Logistik: Eisenbahn aktiv. Frontversorgung über Eastmarch wird berechnet."));
        }
    }

    private void drawChoiceButton(Rectangle rectangle, EventDefinition.Choice choice, Color color,
                                  Runnable action) {
        drawButtonFrame(rectangle, color, action);
        batch.begin();
        drawWrapped(choice.label(), rectangle.x + 12, rectangle.y + rectangle.height - 24,
                rectangle.width - 24, 14, 12, TEXT);
        drawWrapped(choice.consequence(), rectangle.x + 12, rectangle.y + 39,
                rectangle.width - 24, 13, 10, MUTED);
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
        Color surface = buttonSurface(color);
        shapes.setColor(surface);
        shapes.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        shapes.end();
        shapes.begin(ShapeType.Line);
        shapes.setColor(color);
        shapes.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        shapes.end();
    }

    private Color buttonSurface(Color borderColor) {
        if (borderColor == ACCENT) {
            return ACCENT_SURFACE;
        }
        if (borderColor == WARNING) {
            return WARNING_SURFACE;
        }
        if (borderColor == MUTED) {
            return MUTED_SURFACE;
        }
        return PANEL_ALT;
    }

    private void drawText() {
        GameState state = simulation.state();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        drawHeader(state);
        drawMapLabels();
        drawSituationText();
        drawRegionDetails(state);
        drawOperationsText(state);
        if (state.finished()) {
            drawReport(state);
        } else {
            drawEventText(state);
        }
        if (noticeTime > 0) {
            drawWrapped(notice, 48, 112, 244, 13, 11, notice.startsWith("OK") ? ACCENT : WARNING);
        }
        batch.end();
    }

    private void drawHeader(GameState state) {
        text("100 DAYS TO WAR", 48, 858, 20, ACCENT);
        text("VEYRA // ATLAS DER HUNDERT TAGE", 48, 840, 10, MUTED);
        text("TAG " + String.format(Locale.ROOT, "%02d", state.day()) + " / 10", 300, 858, 18, TEXT);
        text("Stabilität " + percent(state.stability()) + "  ·  Moral " + percent(state.morale()), 300, 840, 10, MUTED);
        text("WELTLAGE: " + worldStatus(state), 520, 858, 11,
                worldStatus(state).equals("kritisch") ? WARNING : ACCENT);
        drawWrapped("Veyr River  ·  Eastmarch-Krise  ·  Eisenbahnnetz aktiv",
                520, 840, 490, 10, 8, MUTED);

        for (int index = 0; index < 5; index++) {
            ResourceType type = ResourceType.values()[index];
            float x = 760 + index * 94;
            text(shortResource(type), x + 7, 858, 8, MUTED);
            text(format(state.resource(type)), x + 7, 843, 10, TEXT);
        }
    }

    private void drawMapLabels() {
        text("VEYRA // INTERAKTIVER ATLAS", 366, 792, 14, TEXT);
        text("30 Regionen · Zoom mit Scrollrad · Karte ziehen · Gebäude anklicken", 366, 773, 10, MUTED);

        // Region labels belong to the map viewport. Without this clip they
        // are drawn above the fixed side panels when the map is panned.
        batch.flush();
        beginMapClip();
        for (MapRegionView view : mapRegions) {
            String number = String.format(Locale.ROOT, "%02d", view.region().ordinal() + 1);
            Color labelColor = view.region() == selectedRegion ? TEXT : MUTED;
            text(number, mapPointX(view.labelX()), mapPointY(view.labelY()),
                    view.region() == selectedRegion ? 10 : 8, labelColor);
        }

        mapLabel("NORVANE", 584, 742, Nation.NORVANE);
        mapLabel("ASTER", 390, 626, Nation.ASTER);
        mapLabel("VALERIA", 590, 564, Nation.VALERIA);
        mapLabel("DRAVIK", 824, 626, Nation.DRAVIK);
        mapLabel("ELDORIA", 590, 366, Nation.ELDORIA);
        mapLabel("KARSEN", 590, 282, Nation.KARSEN);
        batch.flush();
        endMapClip();

        text("Fluss", 382, 224, 9, RIVER);
        text("Eisenbahn", 430, 224, 9, RAIL);
        text("Front", 515, 224, 9, WARNING);
        text("Scrollen: Zoom  ·  Ziehen: Pan  ·  Gebäude: Upgrade", 620, 224, 9, MUTED);
    }

    private void mapLabel(String value, float x, float y, Nation nation) {
        text(value, mapPointX(x), mapPointY(y), 9, nationColor(nation));
    }

    private void drawSituationText() {
        text("EASTMARCH-KRISE", 366, 190, 10, WARNING);
        text("Varkesh ↔ Eastmarch  ·  offene Ebene  ·  Panzerkorridor", 366, 171, 11, TEXT);
        text("Flussüberquerung: −20 % Angriff  ·  Eisenbahn verbessert Versorgung und Bewegung", 366, 151, 9, MUTED);
        text("Atlas-Modus aktiv: Gebäude pulsieren, Bahnmarker bewegen sich, Regionen bleiben anklickbar", 366, 112, 9, ACCENT);
    }

    private void drawEventText(GameState state) {
        EventDefinition event = state.currentEvent();
        text("TAGESLAGE", 48, 792, 11, ACCENT);
        if (event == null) {
            text("Keine neue Meldung", 48, 750, 16, TEXT);
        } else {
            drawWrapped(event.title(), 48, 752, 244, 20, 16, TEXT);
            drawWrapped(event.description(), 48, 700, 244, 16, 11, MUTED);
            text(state.eventResolved() ? "Entscheidung gespeichert" : "ENTSCHEIDUNG AUSWÄHLEN",
                    48, 442, 10, state.eventResolved() ? ACCENT : WARNING);
        }

        text("LETZTE MELDUNGEN", 48, 166, 10, ACCENT);
        int y = 144;
        for (String entry : state.history().stream().limit(3).toList()) {
            drawWrapped(entry, 48, y, 244, 13, 9, MUTED);
            y -= 30;
        }
    }

    private void drawRegionDetails(GameState state) {
        text("REGION", 1_074, 792, 10, ACCENT);
        text(selectedRegion.displayName(), 1_074, 758, 18, TEXT);
        drawWrapped(selectedRegion.nation().displayName() + "  ·  " + selectedRegion.terrain().displayName(),
                1_074, 737, 310, 12, 10, nationColor(selectedRegion.nation()));

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
                int level = state.buildingLevel(selectedRegion.operationalRegion(), building);
                text("• " + building.displayName() + "  L" + level, 1_074, y, 10, TEXT);
                y -= 21;
            }
        }
        if (!state.construction().isEmpty()) {
            ConstructionProject project = state.construction().get(0);
            text("Bau: " + project.type().displayName() + "  " + project.progressPercent() + "%",
                    1_074, 415, 9, WARNING);
        }

        text("OPERATIONSZENTRALE", 1_074, 350, 10, ACCENT);
        drawWrapped(selectedRegion.nation() == PLAYER_NATION
                        ? "Tabs öffnen die einzelnen Einsatzbereiche."
                        : "Nur Valeria kann in dieser Phase bauen.",
                1_074, 334, 310, 11, 9,
                selectedRegion.nation() == PLAYER_NATION ? MUTED : WARNING);
    }

    private void drawOperationsText(GameState state) {
        text(operationsTab.title(), 1_074, 236, 11, TEXT);
        switch (operationsTab) {
            case BUILD -> text("Bauort: " + selectedRegion.displayName(), 1_074, 218, 9, MUTED);
            case MILITARY -> drawWrapped("Ausbildung bindet Kapazität und Ressourcen.",
                    1_074, 218, 310, 11, 9, MUTED);
            case TRADE -> drawWrapped("Marktpreise: Stahl −9.000 · Treibstoff −6.000 Geld",
                    1_074, 218, 310, 11, 9, MUTED);
            case INTELLIGENCE -> drawWrapped("Operation kostet 6.500 Geld und erhöht die Datenqualität.",
                    1_074, 218, 310, 11, 9, MUTED);
            case DIPLOMACY -> drawWrapped("Beziehungen und Spannungen der sechs Staaten.",
                    1_074, 218, 310, 11, 9, MUTED);
            case LOGISTICS -> drawWrapped("Railway: "
                            + (selectedRegion.railway() ? "Versorgung gesichert" : "keine Bahnverbindung"),
                    1_074, 218, 310, 11, 9, MUTED);
        }
    }

    private void detailRow(String label, String value, int y) {
        text(label, 1_074, y, 9, MUTED);
        drawWrapped(value, 1_188, y, 200, 12, 9, TEXT);
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

    private void drawBuildingPopup() {
        if (selectedBuilding == null) {
            buildingPopupOpen = false;
            return;
        }
        GameState state = simulation.state();
        BuildingType type = selectedBuilding.type();
        RegionId region = selectedBuilding.region().operationalRegion();
        int currentLevel = state.buildingLevel(region, type);
        int targetLevel = Math.min(type.maxLevel(), currentLevel + 1);
        Rectangle popup = new Rectangle(430, 205, 580, 470);

        shapes.begin(ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, .68f);
        shapes.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        shapes.setColor(POPUP);
        shapes.rect(popup.x, popup.y, popup.width, popup.height);
        shapes.end();
        shapes.begin(ShapeType.Line);
        shapes.setColor(ACCENT);
        shapes.rect(popup.x, popup.y, popup.width, popup.height);
        shapes.end();

        batch.begin();
        text("GEBÄUDE-UPGRADE", popup.x + 28, popup.y + popup.height - 32, 11, ACCENT);
        text(type.displayName() + " · " + selectedBuilding.region().displayName(),
                popup.x + 28, popup.y + popup.height - 70, 21, TEXT);
        text("Aktuelle Stufe: " + currentLevel + " / " + type.maxLevel(),
                popup.x + 28, popup.y + popup.height - 108, 12, MUTED);
        text("Nächste Stufe: " + type.effectAtLevel(targetLevel),
                popup.x + 28, popup.y + popup.height - 140, 13, ACCENT);
        text("Voraussetzung: " + type.upgradeRequirement(currentLevel),
                popup.x + 28, popup.y + popup.height - 184, 11, TEXT);
        if (currentLevel < type.maxLevel()) {
            text("Kosten: " + format(type.upgradeCashCost(currentLevel)) + " Geld  ·  "
                    + type.upgradeSteelCost(currentLevel) + " Stahl",
                    popup.x + 28, popup.y + popup.height - 216, 11, WARNING);
            text("Nach dem Ausbau wird der Produktionsbonus beim nächsten Tageswechsel genutzt.",
                    popup.x + 28, popup.y + 120, 10, MUTED);
        } else {
            text("Dieses Gebäude arbeitet bereits auf der höchsten Stufe.",
                    popup.x + 28, popup.y + 120, 11, ACCENT);
        }
        batch.end();

        drawButton(new Rectangle(popup.x + 28, popup.y + 42, 244, 38),
                currentLevel >= type.maxLevel() ? "MAXIMALE STUFE" : "UPGRADE AUSFÜHREN", ACCENT,
                () -> execute(simulation.upgradeBuilding(type, region)));
        drawButton(new Rectangle(popup.x + popup.width - 180, popup.y + 42, 152, 38),
                "SCHLIESSEN", MUTED, () -> buildingPopupOpen = false);
    }

    private ActionResult buildSelected(BuildingType type) {
        if (selectedRegion.nation() != PLAYER_NATION) {
            return ActionResult.rejected("Bauaufträge sind nur in Valeria möglich.");
        }
        return simulation.build(type, selectedRegion.operationalRegion());
    }

    private void execute(ActionResult result) {
        showNotice((result.success() ? "OK: " : "Hinweis: ") + result.message());
    }

    private void showNotice(String message) {
        notice = message;
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
        font.getData().setScale(size / FONT_BASE_SIZE);
        font.setColor(color);
        font.draw(batch, value, x, y);
    }

    private void drawWrapped(String value, float x, float y, float maxWidth, float lineHeight,
                             float size, Color color) {
        font.getData().setScale(size / FONT_BASE_SIZE);
        for (String line : wrap(value, maxWidth)) {
            text(line, x, y, size, color);
            y -= lineHeight;
        }
    }

    private List<String> wrap(String value, float maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : value.split("\\s+")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (textWidth(candidate) > maxWidth && !current.isEmpty()) {
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

    private float textWidth(String value) {
        glyphLayout.setText(font, value);
        return glyphLayout.width;
    }

    private float centreX(WorldRegion region) {
        return mapRegions.stream().filter(view -> view.region() == region).findFirst()
                .map(view -> mapPointX(view.centerX())).orElse(MAP_LEFT);
    }

    private float centreY(WorldRegion region) {
        return mapRegions.stream().filter(view -> view.region() == region).findFirst()
                .map(view -> mapPointY(view.centerY())).orElse(MAP_BOTTOM);
    }

    private float mapPointX(float x) {
        return MAP_CENTER_X + (x - MAP_CENTER_X) * mapZoom + mapPanX;
    }

    private float mapPointY(float y) {
        return MAP_CENTER_Y + (y - MAP_CENTER_Y) * mapZoom + mapPanY;
    }

    private float inverseMapX(float x) {
        return MAP_CENTER_X + (x - MAP_CENTER_X - mapPanX) / mapZoom;
    }

    private float inverseMapY(float y) {
        return MAP_CENTER_Y + (y - MAP_CENTER_Y - mapPanY) / mapZoom;
    }

    private float[] transformedVertices(MapRegionView view) {
        float[] original = view.vertices();
        float[] transformed = new float[original.length];
        for (int index = 0; index < original.length; index += 2) {
            transformed[index] = mapPointX(original[index]);
            transformed[index + 1] = mapPointY(original[index + 1]);
        }
        return transformed;
    }

    private void lineBetween(WorldRegion from, WorldRegion to) {
        shapes.line(centreX(from), centreY(from), centreX(to), centreY(to));
    }

    private void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        shapes.triangle(x1, y1, x2, y2, x3, y3);
    }

    private void beginMapClip() {
        int screenX = Math.round(viewport.getScreenX() + MAP_AREA_X / WORLD_WIDTH * viewport.getScreenWidth());
        int screenY = Math.round(viewport.getScreenY() + MAP_AREA_Y / WORLD_HEIGHT * viewport.getScreenHeight());
        int screenWidth = Math.round(MAP_AREA_WIDTH / WORLD_WIDTH * viewport.getScreenWidth());
        int screenHeight = Math.round(MAP_AREA_HEIGHT / WORLD_HEIGHT * viewport.getScreenHeight());
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(screenX, screenY, screenWidth, screenHeight);
    }

    private void endMapClip() {
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
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
        float x = MAP_LEFT + column * MAP_STEP_X + (row % 2) * 2;
        float y = MAP_BOTTOM + row * MAP_STEP_Y + (column % 2) * 2;
        float width = MAP_TILE_WIDTH;
        float height = MAP_TILE_HEIGHT;
        float[] vertices = {
                x + 5, y + 8,
                x + 23, y,
                x + width - 7, y + 5,
                x + width, y + height - 12,
                x + width - 12, y + height,
                x + 10, y + height - 3,
                x, y + height - 17
        };
        Rectangle bounds = new Rectangle(x, y, width, height);
        regions.add(new MapRegionView(region, bounds, vertices, x + 27, y + 30));
    }

    private static Color nationColor(Nation nation) {
        return switch (nation) {
            case NORVANE -> new Color(0.42f, 0.69f, 0.84f, 1f);
            case ASTER -> new Color(0.47f, 0.35f, 0.72f, 1f);
            case VALERIA -> new Color(0.27f, 0.58f, 0.33f, 1f);
            case DRAVIK -> new Color(0.72f, 0.27f, 0.23f, 1f);
            case ELDORIA -> new Color(0.63f, 0.65f, 0.25f, 1f);
            case KARSEN -> new Color(0.78f, 0.51f, 0.25f, 1f);
        };
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

    private boolean mapContains(float x, float y) {
        return x >= MAP_AREA_X && x <= MAP_AREA_X + MAP_AREA_WIDTH
                && y >= MAP_AREA_Y && y <= MAP_AREA_Y + MAP_AREA_HEIGHT;
    }

    private void resetMapView() {
        mapZoom = 1f;
        mapPanX = 0;
        mapPanY = 0;
        showNotice("OK: Kartenansicht zurückgesetzt.");
    }

    private void clampMapPan() {
        float minPanX = MAP_AREA_X - MAP_CENTER_X - (MAP_CONTENT_RIGHT - MAP_CENTER_X) * mapZoom;
        float maxPanX = MAP_AREA_X + MAP_AREA_WIDTH - MAP_CENTER_X
                - (MAP_CONTENT_LEFT - MAP_CENTER_X) * mapZoom;
        float minPanY = MAP_AREA_Y - MAP_CENTER_Y - (MAP_CONTENT_TOP - MAP_CENTER_Y) * mapZoom;
        float maxPanY = MAP_AREA_Y + MAP_AREA_HEIGHT - MAP_CENTER_Y
                - (MAP_CONTENT_BOTTOM - MAP_CENTER_Y) * mapZoom;
        mapPanX = Math.max(minPanX, Math.min(maxPanX, mapPanX));
        mapPanY = Math.max(minPanY, Math.min(maxPanY, mapPanY));
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 world = viewport.unproject(new Vector2(screenX, screenY));
        if (buildingPopupOpen) {
            return true;
        }
        if (mapContains(world.x, world.y)) {
            panningMap = true;
            mapDragMoved = false;
            lastDragX = world.x;
            lastDragY = world.y;
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!panningMap || buildingPopupOpen) {
            return true;
        }
        Vector2 world = viewport.unproject(new Vector2(screenX, screenY));
        float deltaX = world.x - lastDragX;
        float deltaY = world.y - lastDragY;
        if (Math.abs(deltaX) + Math.abs(deltaY) > 1f) {
            mapDragMoved = true;
        }
        mapPanX += deltaX;
        mapPanY += deltaY;
        lastDragX = world.x;
        lastDragY = world.y;
        clampMapPan();
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector2 world = viewport.unproject(new Vector2(screenX, screenY));
        boolean dragged = mapDragMoved;
        panningMap = false;
        mapDragMoved = false;

        for (int index = buttons.size() - 1; index >= 0; index--) {
            Button candidate = buttons.get(index);
            if (candidate.bounds().contains(world.x, world.y)) {
                candidate.action().run();
                return true;
            }
        }
        if (buildingPopupOpen || dragged || !mapContains(world.x, world.y)) {
            return true;
        }

        float rawX = inverseMapX(world.x);
        float rawY = inverseMapY(world.y);
        for (int index = buildingHitBoxes.size() - 1; index >= 0; index--) {
            BuildingHitBox candidate = buildingHitBoxes.get(index);
            if (candidate.bounds().contains(world.x, world.y)) {
                selectedRegion = candidate.region();
                selectedBuilding = candidate;
                buildingPopupOpen = true;
                return true;
            }
        }
        for (int index = mapRegions.size() - 1; index >= 0; index--) {
            MapRegionView candidate = mapRegions.get(index);
            if (candidate.bounds().contains(rawX, rawY)) {
                selectedRegion = candidate.region();
                showNotice("OK: " + selectedRegion.displayName() + " ausgewählt.");
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        Vector2 world = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        if (!mapContains(world.x, world.y)) {
            return false;
        }
        float oldZoom = mapZoom;
        float rawX = inverseMapX(world.x);
        float rawY = inverseMapY(world.y);
        mapZoom = Math.max(.72f, Math.min(2.35f, mapZoom - amountY * .12f));
        mapPanX = world.x - MAP_CENTER_X - (rawX - MAP_CENTER_X) * mapZoom;
        mapPanY = world.y - MAP_CENTER_Y - (rawY - MAP_CENTER_Y) * mapZoom;
        clampMapPan();
        return oldZoom != mapZoom;
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
        if (fontGenerator != null) {
            fontGenerator.dispose();
        }
    }

    private enum OperationsTab {
        BUILD("BAU", "BAU & AUSBAU"),
        MILITARY("ARMEE", "ARMEE & AUSBILDUNG"),
        TRADE("HANDEL", "HANDEL & RESSOURCEN"),
        INTELLIGENCE("INTEL", "AUFKLÄRUNG"),
        DIPLOMACY("DIPLO", "DIPLOMATIE"),
        LOGISTICS("LOGISTIK", "LOGISTIK & VERSORGUNG");

        private final String label;
        private final String title;

        OperationsTab(String label, String title) {
            this.label = label;
            this.title = title;
        }

        private String label() {
            return label;
        }

        private String title() {
            return title;
        }
    }

    private record Button(Rectangle bounds, Runnable action) {
    }

    private record BuildingHitBox(WorldRegion region, BuildingType type, Rectangle bounds) {
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
