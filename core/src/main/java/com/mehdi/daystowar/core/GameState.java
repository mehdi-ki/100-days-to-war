package com.mehdi.daystowar.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class GameState {
    private final EnumMap<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);
    private final EnumMap<RegionId, List<BuildingType>> regionBuildings = new EnumMap<>(RegionId.class);
    private final List<ConstructionProject> construction = new ArrayList<>();
    private final List<ProductionOrder> production = new ArrayList<>();
    private final EnumMap<UnitType, Integer> army = new EnumMap<>(UnitType.class);
    private final EnumMap<UnitType, Integer> enemyUnits = new EnumMap<>(UnitType.class);
    private final List<String> history = new ArrayList<>();

    private int day = 1;
    private double stability = 75;
    private double morale = 76;
    private Policy policy = Policy.VOLUNTEER;
    private TaxLevel tax = TaxLevel.NORMAL;
    private int enemyMorale;
    private int enemyFood = 260;
    private int enemyFuel = 90;
    private int intelligence;
    private int recruitExperience;
    private int trainedExperience = 2;
    private int veteranExperience;
    private int experienceAge;
    private int industryModifierDays;
    private double industryModifier = 1;
    private EventDefinition currentEvent;
    private boolean eventResolved;
    private boolean finished;
    private WarReport report;

    GameState(Random random) {
        resources.put(ResourceType.CASH, 50_000);
        resources.put(ResourceType.FOOD, 250);
        resources.put(ResourceType.STEEL, 100);
        resources.put(ResourceType.FUEL, 100);
        resources.put(ResourceType.MANPOWER, 50_000);

        for (RegionId region : RegionId.values()) {
            regionBuildings.put(region, new ArrayList<>());
        }
        regionBuildings.get(RegionId.CAPITAL).add(BuildingType.BARRACKS);
        regionBuildings.get(RegionId.INDUSTRY).add(BuildingType.CIVIL);
        regionBuildings.get(RegionId.INDUSTRY).add(BuildingType.CIVIL);
        regionBuildings.get(RegionId.INDUSTRY).add(BuildingType.STEELWORK);
        regionBuildings.get(RegionId.RURAL).add(BuildingType.FARM);

        for (UnitType unit : UnitType.values()) {
            army.put(unit, 0);
            enemyUnits.put(unit, 0);
        }
        army.put(UnitType.INFANTRY, 2);
        enemyUnits.put(UnitType.INFANTRY, 2 + random.nextInt(2));
        enemyMorale = 67 + random.nextInt(9);
    }

    public int day() {
        return day;
    }

    public int resource(ResourceType type) {
        return resources.get(type);
    }

    public Map<ResourceType, Integer> resources() {
        return Collections.unmodifiableMap(resources);
    }

    public double stability() {
        return stability;
    }

    public double morale() {
        return morale;
    }

    public Policy policy() {
        return policy;
    }

    public TaxLevel tax() {
        return tax;
    }

    public Map<RegionId, List<BuildingType>> regionBuildings() {
        EnumMap<RegionId, List<BuildingType>> copy = new EnumMap<>(RegionId.class);
        regionBuildings.forEach((region, buildings) -> copy.put(region, List.copyOf(buildings)));
        return Collections.unmodifiableMap(copy);
    }

    public List<BuildingType> buildings(RegionId region) {
        return List.copyOf(regionBuildings.get(region));
    }

    public List<ConstructionProject> construction() {
        return Collections.unmodifiableList(construction);
    }

    public List<ProductionOrder> production() {
        return Collections.unmodifiableList(production);
    }

    public Map<UnitType, Integer> army() {
        return Collections.unmodifiableMap(army);
    }

    public int armyCount(UnitType type) {
        return army.get(type);
    }

    public Map<UnitType, Integer> enemyUnits() {
        return Collections.unmodifiableMap(enemyUnits);
    }

    public int enemyCount(UnitType type) {
        return enemyUnits.get(type);
    }

    public int enemyMorale() {
        return enemyMorale;
    }

    public int enemyFood() {
        return enemyFood;
    }

    public int enemyFuel() {
        return enemyFuel;
    }

    public int intelligence() {
        return intelligence;
    }

    public int recruitExperience() {
        return recruitExperience;
    }

    public int trainedExperience() {
        return trainedExperience;
    }

    public int veteranExperience() {
        return veteranExperience;
    }

    public EventDefinition currentEvent() {
        return currentEvent;
    }

    public boolean eventResolved() {
        return eventResolved;
    }

    public boolean finished() {
        return finished;
    }

    public WarReport report() {
        return report;
    }

    public List<String> history() {
        return Collections.unmodifiableList(history);
    }

    int countBuilding(BuildingType type) {
        return regionBuildings.values().stream()
                .mapToInt(buildings -> (int) buildings.stream().filter(type::equals).count())
                .sum();
    }

    int pendingForRegion(RegionId region) {
        return (int) construction.stream().filter(project -> project.region() == region).count();
    }

    int totalUnits(Map<UnitType, Integer> units) {
        return units.values().stream().mapToInt(Integer::intValue).sum();
    }

    int humanCount(Map<UnitType, Integer> units) {
        return units.get(UnitType.INFANTRY) * 5_000
                + units.get(UnitType.ARTILLERY) * 1_500
                + units.get(UnitType.TANK) * 2_000
                + units.get(UnitType.LOGISTICS) * 1_000;
    }

    void setCurrentEvent(EventDefinition event) {
        currentEvent = event;
        eventResolved = false;
    }

    void setEventResolved(boolean value) {
        eventResolved = value;
    }

    void setDay(int value) {
        day = value;
    }

    void setStability(double value) {
        stability = value;
    }

    void setMorale(double value) {
        morale = value;
    }

    void setPolicy(Policy value) {
        policy = value;
    }

    void setTax(TaxLevel value) {
        tax = value;
    }

    void setFinished(boolean value) {
        finished = value;
    }

    void setReport(WarReport value) {
        report = value;
    }

    void setIndustryModifierDays(int value) {
        industryModifierDays = value;
    }

    int industryModifierDays() {
        return industryModifierDays;
    }

    double industryModifier() {
        return industryModifier;
    }

    void setIndustryModifier(double value) {
        industryModifier = value;
    }

    int recruitExperienceAge() {
        return experienceAge;
    }

    void setRecruitExperienceAge(int value) {
        experienceAge = value;
    }

    void addResource(ResourceType type, int amount) {
        resources.compute(type, (key, value) -> value + amount);
    }

    void setResource(ResourceType type, int amount) {
        resources.put(type, amount);
    }

    void addBuilding(RegionId region, BuildingType type) {
        regionBuildings.get(region).add(type);
    }

    void addConstruction(ConstructionProject project) {
        construction.add(project);
    }

    void removeConstruction(ConstructionProject project) {
        construction.remove(project);
    }

    void addProduction(ProductionOrder order) {
        production.add(order);
    }

    void removeProduction(ProductionOrder order) {
        production.remove(order);
    }

    void addArmy(UnitType type, int amount) {
        army.compute(type, (key, value) -> value + amount);
    }

    void addEnemy(UnitType type, int amount) {
        enemyUnits.compute(type, (key, value) -> Math.max(0, value + amount));
    }

    void setEnemyMorale(int value) {
        enemyMorale = value;
    }

    void setEnemyFood(int value) {
        enemyFood = value;
    }

    void setEnemyFuel(int value) {
        enemyFuel = value;
    }

    void setIntelligence(int value) {
        intelligence = value;
    }

    void addRecruitExperience(int amount) {
        recruitExperience += amount;
    }

    void removeRecruitExperience(int amount) {
        recruitExperience = Math.max(0, recruitExperience - amount);
    }

    void addTrainedExperience(int amount) {
        trainedExperience += amount;
    }

    void addVeteranExperience(int amount) {
        veteranExperience += amount;
    }

    void addHistory(String message) {
        history.add(0, "Tag " + day + ": " + message);
        if (history.size() > 18) {
            history.remove(history.size() - 1);
        }
    }
}
