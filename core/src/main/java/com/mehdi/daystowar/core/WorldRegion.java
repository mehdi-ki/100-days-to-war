package com.mehdi.daystowar.core;

/**
 * The first strategic map of Veyra: six states with five readable regions each.
 * Rendering coordinates deliberately stay in the UI layer; this class only
 * describes simulation data that can later be loaded from JSON.
 */
public enum WorldRegion {
    NORVANE_CAPITAL("Norrholm", Nation.NORVANE, Terrain.PLAINS, 2_400_000, 91, "Verwaltung · Handel", 3, true, RegionId.CAPITAL),
    NORVANE_FROSTPASS("Frostpass", Nation.NORVANE, Terrain.MOUNTAINS, 900_000, 55, "Eisen · Kohle", 2, true, RegionId.BORDER),
    NORVANE_SKELD_FOREST("Skeld Forest", Nation.NORVANE, Terrain.FOREST, 1_200_000, 68, "Holz · Nahrung", 1, true, RegionId.RURAL),
    NORVANE_HELIX("Helix District", Nation.NORVANE, Terrain.PLAINS, 1_000_000, 74, "Technologie · Geheimdienst", 1, true, RegionId.INDUSTRY),
    NORVANE_KALDVIK("Kaldvik", Nation.NORVANE, Terrain.PASS, 650_000, 46, "Logistik · Eisenbahn", 2, true, RegionId.BORDER),

    ASTER_CAPITAL("Astera", Nation.ASTER, Terrain.PLAINS, 2_100_000, 84, "Handel · Stahl", 2, true, RegionId.CAPITAL),
    ASTER_WESTPORT("Westport", Nation.ASTER, Terrain.COAST, 1_300_000, 70, "Hafen · Fisch", 0, true, RegionId.CAPITAL),
    ASTER_FOUNDRY_COAST("Foundry Coast", Nation.ASTER, Terrain.COAST, 1_600_000, 76, "Zivilindustrie · Handel", 1, true, RegionId.INDUSTRY),
    ASTER_SILVER_HILLS("Silver Hills", Nation.ASTER, Terrain.MOUNTAINS, 1_000_000, 57, "Stahl · Erz", 1, false, RegionId.INDUSTRY),
    ASTER_RIVERGATE("Rivergate", Nation.ASTER, Terrain.PASS, 500_000, 62, "Bahn · Flussübergang", 3, true, RegionId.BORDER),

    VALERIA_CAPITAL("Valen", Nation.VALERIA, Terrain.PLAINS, 2_400_000, 91, "Regierung · Handel", 3, true, RegionId.CAPITAL),
    VALERIA_IRONVALE("Ironvale", Nation.VALERIA, Terrain.MOUNTAINS, 1_400_000, 72, "Stahl · Militärindustrie", 1, true, RegionId.INDUSTRY),
    VALERIA_EASTMARCH("Eastmarch", Nation.VALERIA, Terrain.PLAINS, 1_300_000, 66, "Grenze · Panzerkorridor", 1, true, RegionId.BORDER),
    VALERIA_NORTHWATCH("Northwatch", Nation.VALERIA, Terrain.MOUNTAINS, 800_000, 52, "Gebirgspass · Verteidigung", 2, true, RegionId.BORDER),
    VALERIA_GREEN_BASIN("Green Basin", Nation.VALERIA, Terrain.PLAINS, 1_700_000, 61, "Getreide · Nahrung", 0, true, RegionId.RURAL),

    DRAVIK_CAPITAL("Drazna", Nation.DRAVIK, Terrain.PLAINS, 2_200_000, 88, "Verwaltung · Rüstung", 3, true, RegionId.CAPITAL),
    DRAVIK_REDWORKS("Redworks", Nation.DRAVIK, Terrain.PLAINS, 1_600_000, 76, "Stahl · Waffen", 1, true, RegionId.INDUSTRY),
    DRAVIK_IRON_STEPPE("Iron Steppe", Nation.DRAVIK, Terrain.PLAINS, 1_800_000, 70, "Größte Stahlregion", 1, true, RegionId.INDUSTRY),
    DRAVIK_VARKESH("Varkesh", Nation.DRAVIK, Terrain.PLAINS, 1_000_000, 51, "Grenze · Aufmarsch", 2, true, RegionId.BORDER),
    DRAVIK_SOUTHERN_CORRIDOR("Southern Corridor", Nation.DRAVIK, Terrain.PASS, 600_000, 49, "Verbindung nach Karsen", 3, true, RegionId.BORDER),

    ELDORIA_CAPITAL("Elda", Nation.ELDORIA, Terrain.PLAINS, 1_800_000, 81, "Handel · Verwaltung", 2, true, RegionId.CAPITAL),
    ELDORIA_GOLDEN_FIELDS("Golden Fields", Nation.ELDORIA, Terrain.PLAINS, 2_100_000, 67, "Größte Agrarregion", 0, true, RegionId.RURAL),
    ELDORIA_RIVERLANDS("Riverlands", Nation.ELDORIA, Terrain.PLAINS, 1_700_000, 60, "Bevölkerung · Landwirtschaft", 1, true, RegionId.RURAL),
    ELDORIA_SOUTHWATCH("Southwatch", Nation.ELDORIA, Terrain.MOUNTAINS, 800_000, 47, "Hügelland · Verteidigung", 1, false, RegionId.BORDER),
    ELDORIA_ORCHARD_COAST("Orchard Coast", Nation.ELDORIA, Terrain.COAST, 1_200_000, 64, "Landwirtschaft · Küstenhandel", 0, true, RegionId.RURAL),

    KARSEN_CAPITAL("Karza", Nation.KARSEN, Terrain.PLAINS, 2_000_000, 83, "Banken · Handel", 2, true, RegionId.CAPITAL),
    KARSEN_BLACK_WELLS("Black Wells", Nation.KARSEN, Terrain.DESERT, 600_000, 35, "Größtes Ölfeld", 1, false, RegionId.INDUSTRY),
    KARSEN_SUN_BASIN("Sun Basin", Nation.KARSEN, Terrain.DESERT, 900_000, 51, "Öl · Salz", 0, true, RegionId.INDUSTRY),
    KARSEN_REFINERY_COAST("Refinery Coast", Nation.KARSEN, Terrain.COAST, 1_100_000, 64, "Raffinerien · Häfen", 1, true, RegionId.CAPITAL),
    KARSEN_NORTHERN_PIPELINE("Northern Pipeline", Nation.KARSEN, Terrain.DESERT, 550_000, 40, "Energietransport · Engpass", 3, true, RegionId.BORDER);

    private final String displayName;
    private final Nation nation;
    private final Terrain terrain;
    private final int population;
    private final int infrastructure;
    private final String resources;
    private final int fortressLevel;
    private final boolean railway;
    private final RegionId operationalRegion;

    WorldRegion(String displayName, Nation nation, Terrain terrain, int population, int infrastructure,
                String resources, int fortressLevel, boolean railway, RegionId operationalRegion) {
        this.displayName = displayName;
        this.nation = nation;
        this.terrain = terrain;
        this.population = population;
        this.infrastructure = infrastructure;
        this.resources = resources;
        this.fortressLevel = fortressLevel;
        this.railway = railway;
        this.operationalRegion = operationalRegion;
    }

    public String displayName() {
        return displayName;
    }

    public Nation nation() {
        return nation;
    }

    public Terrain terrain() {
        return terrain;
    }

    public int population() {
        return population;
    }

    public int infrastructure() {
        return infrastructure;
    }

    public String resources() {
        return resources;
    }

    public int fortressLevel() {
        return fortressLevel;
    }

    public boolean railway() {
        return railway;
    }

    public RegionId operationalRegion() {
        return operationalRegion;
    }
}
