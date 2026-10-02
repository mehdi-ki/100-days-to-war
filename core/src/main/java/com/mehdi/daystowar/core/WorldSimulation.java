package com.mehdi.daystowar.core;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public final class WorldSimulation {
    private final Random random;
    private final GameState state;
    private final List<String> recentEventIds = new ArrayList<>();

    public WorldSimulation() {
        this(new Random());
    }

    public WorldSimulation(long seed) {
        this(new Random(seed));
    }

    private WorldSimulation(Random random) {
        this.random = random;
        this.state = new GameState(random);
        setNextEvent();
        state.addHistory("Der 10-Tage-Vorbereitungsplan beginnt.");
    }

    public GameState state() {
        return state;
    }

    public int dailyFlow(ResourceType resource) {
        return dailyFlows().get(resource);
    }

    public ActionResult chooseEvent(int choice) {
        if (state.finished()) {
            return ActionResult.rejected("Der Kriegstest ist bereits abgeschlossen.");
        }
        if (state.eventResolved()) {
            return ActionResult.rejected("Das Tagesereignis wurde bereits entschieden.");
        }
        if (state.currentEvent() == null || choice < 0 || choice >= state.currentEvent().choices().size()) {
            return ActionResult.rejected("Diese Ereignisentscheidung ist nicht verfügbar.");
        }
        if (!eventCostAffordable(state.currentEvent().id(), choice)) {
            return ActionResult.rejected("Dafür reicht die Staatskasse nicht.");
        }

        applyEvent(state.currentEvent().id(), choice);
        state.setStability(clamp(state.stability(), 0, 100));
        state.setMorale(clamp(state.morale(), 0, 100));
        state.setEventResolved(true);
        state.addHistory("Ereignis „" + state.currentEvent().title() + "“: "
                + state.currentEvent().choices().get(choice).label() + ".");
        return ActionResult.ok("Entscheidung notiert. Du kannst den Tag abschließen.");
    }

    public ActionResult build(BuildingType type, RegionId region) {
        if (state.finished()) {
            return ActionResult.rejected("Der Kriegstest ist bereits abgeschlossen.");
        }
        int buildCapacity = Math.max(1, state.countBuilding(BuildingType.CIVIL));
        if (state.construction().size() >= buildCapacity) {
            return ActionResult.rejected("Alle Baukapazitäten sind belegt.");
        }
        if (state.buildings(region).size() + state.pendingForRegion(region) >= region.slots()) {
            return ActionResult.rejected("Diese Region hat keine freien Bauplätze mehr.");
        }
        if (!canAfford(type.cashCost(), type.steelCost(), 0)) {
            return ActionResult.rejected("Dafür fehlen Geld oder Stahl.");
        }

        state.addResource(ResourceType.CASH, -type.cashCost());
        state.addResource(ResourceType.STEEL, -type.steelCost());
        state.addConstruction(new ConstructionProject(type, region));
        state.addHistory(type.displayName() + " in " + region.displayName() + " in Auftrag gegeben.");
        return ActionResult.ok(type.displayName() + " wurde in Auftrag gegeben.");
    }

    public ActionResult upgradeBuilding(BuildingType type, RegionId region) {
        if (state.finished()) {
            return ActionResult.rejected("Der Kriegstest ist bereits abgeschlossen.");
        }
        int currentLevel = state.buildingLevel(region, type);
        if (currentLevel <= 0) {
            return ActionResult.rejected("Dieses Gebäude ist in der Region noch nicht vorhanden.");
        }
        if (currentLevel >= type.maxLevel()) {
            return ActionResult.rejected("Dieses Gebäude ist bereits auf der höchsten Stufe.");
        }
        if (currentLevel >= 2 && state.countBuilding(BuildingType.CIVIL) < 1) {
            return ActionResult.rejected("Ab Stufe 3 wird mindestens eine zivile Fabrik benötigt.");
        }

        int cashCost = type.upgradeCashCost(currentLevel);
        int steelCost = type.upgradeSteelCost(currentLevel);
        if (!canAfford(cashCost, steelCost, 0)) {
            return ActionResult.rejected("Für dieses Upgrade fehlen Geld oder Stahl.");
        }

        state.addResource(ResourceType.CASH, -cashCost);
        state.addResource(ResourceType.STEEL, -steelCost);
        state.upgradeBuilding(region, type);
        int targetLevel = currentLevel + 1;
        state.addHistory(type.displayName() + " in " + region.displayName()
                + " auf Stufe " + targetLevel + " verbessert.");
        return ActionResult.ok(type.displayName() + " ist jetzt Stufe " + targetLevel + ". "
                + type.effectAtLevel(targetLevel));
    }

    public ActionResult train(UnitType type) {
        if (state.finished()) {
            return ActionResult.rejected("Der Kriegstest ist bereits abgeschlossen.");
        }
        if (type.requiresMilitaryFactory() && state.countBuilding(BuildingType.MILITARY) < 1) {
            return ActionResult.rejected("Für diese Ausrüstung brauchst du eine Militärfabrik.");
        }
        int trainingCapacity = Math.max(1, state.countBuilding(BuildingType.BARRACKS));
        if (state.production().size() >= trainingCapacity) {
            return ActionResult.rejected("Alle Ausbildungskapazitäten sind belegt.");
        }
        if (!canAfford(type.cashCost(), type.steelCost(), type.manpowerCost())) {
            return ActionResult.rejected("Dafür fehlen Ressourcen oder Mannstärke.");
        }

        state.addResource(ResourceType.CASH, -type.cashCost());
        state.addResource(ResourceType.STEEL, -type.steelCost());
        state.addResource(ResourceType.MANPOWER, -type.manpowerCost());
        state.addProduction(new ProductionOrder(type));
        state.addHistory(type.displayName() + " zur Ausbildung eingeteilt.");
        return ActionResult.ok(type.displayName() + " wurde zur Ausbildung eingeteilt.");
    }

    public ActionResult setTax(TaxLevel tax) {
        if (state.finished()) {
            return ActionResult.rejected("Der Kriegstest ist bereits abgeschlossen.");
        }
        state.setTax(tax);
        state.addHistory("Steuersatz auf " + tax.displayName() + " gestellt.");
        return ActionResult.ok("Steuersatz aktualisiert.");
    }

    public ActionResult setPolicy(Policy policy) {
        if (state.finished()) {
            return ActionResult.rejected("Der Kriegstest ist bereits abgeschlossen.");
        }
        if (policy == state.policy()) {
            return ActionResult.rejected("Diese Wehrpflicht ist bereits aktiv.");
        }
        Policy old = state.policy();
        state.setPolicy(policy);
        if (policy == Policy.GENERAL) {
            state.setStability(state.stability() - 2);
        } else if (policy == Policy.VOLUNTEER && old == Policy.GENERAL) {
            state.setStability(state.stability() + 2);
        }
        state.addHistory("Wehrpflicht geändert: " + policy.displayName() + ".");
        return ActionResult.ok("Wehrpflicht geändert.");
    }

    public ActionResult trade(TradeType trade) {
        if (state.finished()) {
            return ActionResult.rejected("Der Kriegstest ist bereits abgeschlossen.");
        }
        switch (trade) {
            case SELL_FOOD -> {
                if (state.resource(ResourceType.FOOD) < 65) {
                    return ActionResult.rejected("Du brauchst mindestens 65 Nahrung, um 50 zu verkaufen.");
                }
                state.addResource(ResourceType.FOOD, -50);
                state.addResource(ResourceType.CASH, 5_000);
                state.addHistory("50 Nahrung auf dem Markt verkauft.");
            }
            case BUY_STEEL -> {
                if (state.resource(ResourceType.CASH) < 9_000) {
                    return ActionResult.rejected("Nicht genug Geld für Stahl.");
                }
                state.addResource(ResourceType.CASH, -9_000);
                state.addResource(ResourceType.STEEL, 20);
                state.addHistory("20 Stahl gekauft.");
            }
            case BUY_FUEL -> {
                if (state.resource(ResourceType.CASH) < 6_000) {
                    return ActionResult.rejected("Nicht genug Geld für Treibstoff.");
                }
                state.addResource(ResourceType.CASH, -6_000);
                state.addResource(ResourceType.FUEL, 20);
                state.addHistory("20 Treibstoff gekauft.");
            }
            default -> throw new IllegalStateException("Unbekannter Handel: " + trade);
        }
        return ActionResult.ok("Handel abgeschlossen.");
    }

    public ActionResult intelligenceAction() {
        if (state.finished()) {
            return ActionResult.rejected("Der Kriegstest ist bereits abgeschlossen.");
        }
        if (state.resource(ResourceType.CASH) < 6_500) {
            return ActionResult.rejected("Nicht genug Geld für die Geheimdienstoperation.");
        }
        if (state.intelligence() >= 3) {
            return ActionResult.rejected("Die Aufklärung ist bereits auf dem aktuellen Maximum.");
        }
        state.addResource(ResourceType.CASH, -6_500);
        state.setIntelligence(Math.min(3, state.intelligence() + 1));
        state.addHistory("Geheimdienstoperation verbessert die Einschätzung des Gegners.");
        return ActionResult.ok("Aufklärung verbessert.");
    }

    public ActionResult endDay() {
        if (state.finished()) {
            return ActionResult.rejected("Der Kriegstest ist bereits abgeschlossen.");
        }
        if (!state.eventResolved()) {
            return ActionResult.rejected("Entscheide zuerst über das Tagesereignis.");
        }

        dailyUpdate();
        if (state.day() >= 10) {
            runWarTest();
            return ActionResult.ok("Der Kriegstest wurde gestartet.");
        }
        state.setDay(state.day() + 1);
        setNextEvent();
        state.addHistory("Morgenbericht für Tag " + state.day() + " liegt vor.");
        return ActionResult.ok("Tag " + state.day() + " beginnt.");
    }

    public WarReport runWarTest() {
        if (state.report() != null) {
            return state.report();
        }

        int ownMen = state.humanCount(state.army());
        int enemyMen = state.humanCount(state.enemyUnits());
        int ownRaw = weightedBase(state.army(), true);
        int enemyRaw = weightedBase(state.enemyUnits(), false);
        int foodNeed = 22 + state.totalUnits(state.army()) * 11;
        double foodSupply = clamp((double) state.resource(ResourceType.FOOD) / Math.max(1, foodNeed), .3, 1);
        int mechanizedCount = state.armyCount(UnitType.TANK) + state.armyCount(UnitType.LOGISTICS);
        int fuelNeed = state.armyCount(UnitType.TANK) * 25 + state.armyCount(UnitType.LOGISTICS) * 10;
        double fuelSupply = fuelNeed == 0
                ? 1
                : clamp((double) state.resource(ResourceType.FUEL) / Math.max(1, fuelNeed), .12, 1);
        double logistics = state.armyCount(UnitType.LOGISTICS) > 0 ? 1.2 : 1;
        double ownSupply = (.58 + .42 * foodSupply) * logistics;
        double fuelFactor = 1 - (mechanizedCount > 0 ? Math.max(0, 1 - fuelSupply) * .55 : 0);
        double fortFactor = 1 + Math.min(.45, state.buildingPower(BuildingType.FORT) * .15);
        double moraleFactor = .68 + state.morale() / 250;
        double stabilityFactor = .82 + state.stability() / 550;
        double experienceFactor = experienceFactor();
        int ownPower = (int) Math.round(ownRaw * ownSupply * fuelFactor * fortFactor
                * moraleFactor * stabilityFactor * experienceFactor);

        double enemySupply = clamp((double) state.enemyFood()
                / (20 + state.totalUnits(state.enemyUnits()) * 10), .52, .82);
        double enemyFuel = state.enemyCount(UnitType.TANK) == 0
                ? 1
                : clamp((double) state.enemyFuel() / (state.enemyCount(UnitType.TANK) * 22 + 1), .35, .95);
        double enemyPowerFactor = 1 - (state.enemyCount(UnitType.TANK) > 0 ? 1 - enemyFuel : 0) * .42;
        int enemyPower = (int) Math.round(enemyRaw * enemySupply * enemyPowerFactor
                * (.8 + state.enemyMorale() / 300d) * (.91 + random.nextDouble() * .17));
        int ratio = (int) Math.round((double) ownPower / Math.max(1, enemyPower) * 100);
        boolean victory = ratio >= 92;
        int quality = enemyEstimateQuality();

        List<String> reasons = new ArrayList<>();
        if (state.buildingPower(BuildingType.FORT) > 0) {
            reasons.add("Grenzbefestigungen steigerten die eigene Verteidigung um "
                    + Math.round((fortFactor - 1) * 100) + " %.");
        } else {
            reasons.add("Ohne Festungen verteidigte die Armee nur mit ihrer Grundstärke.");
        }
        if (foodSupply > .75) {
            reasons.add("Nahrungsvorräte hielten die Truppen versorgt.");
        } else {
            reasons.add("Zu geringe Nahrungsvorräte schwächten die Versorgung im entscheidenden Moment.");
        }
        if (state.armyCount(UnitType.TANK) > 0 && fuelSupply < .7) {
            reasons.add("Treibstoffmangel nahm den Panzern einen Teil ihrer Mobilität.");
        } else if (state.resource(ResourceType.FUEL) < 30) {
            reasons.add("Die knappen Treibstoffreserven ließen kaum Spielraum für längere Operationen.");
        } else {
            reasons.add("Die Treibstoffreserve war für die vorhandenen mechanisierten Einheiten ausreichend.");
        }
        if (state.morale() >= 75) {
            reasons.add("Die Armeemoral blieb hoch und gab den Truppen zusätzlichen Rückhalt.");
        } else {
            reasons.add("Niedrige Armeemoral senkte die Kampfkraft.");
        }
        if (state.intelligence() > 0 || state.buildingPower(BuildingType.INTEL) > 0) {
            reasons.add("Aufklärung verringerte das Risiko, von der gegnerischen Stärke überrascht zu werden.");
        } else {
            reasons.add("Unzureichende Aufklärung ließ die Führung mit unsicheren Gegnerdaten planen.");
        }
        if (experienceFactor >= 1.05) {
            reasons.add("Ausgebildete und erfahrene Einheiten steigerten die Kampfkraft.");
        } else if (experienceFactor < 1) {
            reasons.add("Unerfahrene Rekruten kämpften mit einem Abschlag.");
        }
        if (state.armyCount(UnitType.INFANTRY) >= 4) {
            reasons.add("Zusätzliche Infanterie half, die Front zu halten.");
        } else {
            reasons.add("Die Infanterie war zahlenmäßig knapp.");
        }

        List<WarReport.Factor> factors = List.of(
                new WarReport.Factor("Versorgung", Math.round(ownSupply * 100) + " %", ownSupply > .75),
                new WarReport.Factor("Treibstoffbereitschaft", Math.round(fuelSupply * 100) + " %", fuelSupply > .65),
                new WarReport.Factor("Armeemoral", Math.round(state.morale()) + " %", state.morale() >= 70),
                new WarReport.Factor("Festungsbonus", "+" + Math.round((fortFactor - 1) * 100) + " %",
                        state.buildingPower(BuildingType.FORT) > 0),
                new WarReport.Factor("Erfahrung", Math.round(experienceFactor * 100) + " %", experienceFactor >= 1),
                new WarReport.Factor("Geheimdienst", quality + " % Genauigkeit", quality > 55)
        );
        WarReport report = new WarReport(victory, ratio, ownMen, enemyMen, ownPower, enemyPower, factors, reasons);
        state.setReport(report);
        state.setFinished(true);
        state.addHistory("Der Kriegstest endete mit: " + (victory ? "SIEG" : "NIEDERLAGE") + ".");
        return report;
    }

    public int enemyEstimateQuality() {
        int quality = 28 + state.intelligence() * 18 + state.buildingPower(BuildingType.INTEL) * 12;
        return Math.min(95, quality);
    }

    private void setNextEvent() {
        List<EventDefinition> candidates = EventCatalog.all().stream()
                .filter(event -> !recentEventIds.subList(Math.max(0, recentEventIds.size() - 3), recentEventIds.size())
                        .contains(event.id()))
                .toList();
        EventDefinition event = candidates.isEmpty()
                ? EventCatalog.all().get(random.nextInt(EventCatalog.all().size()))
                : candidates.get(random.nextInt(candidates.size()));
        state.setCurrentEvent(event);
        recentEventIds.add(event.id());
        if (recentEventIds.size() > 6) {
            recentEventIds.remove(0);
        }
    }

    private boolean canAfford(int cash, int steel, int manpower) {
        return state.resource(ResourceType.CASH) >= cash
                && state.resource(ResourceType.STEEL) >= steel
                && state.resource(ResourceType.MANPOWER) >= manpower;
    }

    private boolean eventCostAffordable(String id, int choice) {
        if (choice != 0) {
            return true;
        }
        int cost = switch (id) {
            case "harvest" -> 5_000;
            case "accident" -> 4_000;
            case "blackmarket" -> 8_000;
            case "fueldeal" -> 10_000;
            case "spy" -> 6_000;
            case "wages" -> 5_000;
            case "budget" -> 9_000;
            case "sabotage" -> 5_000;
            case "border" -> 6_000;
            default -> 0;
        };
        return state.resource(ResourceType.CASH) >= cost;
    }

    private void applyEvent(String id, int choice) {
        switch (id) {
            case "harvest" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.CASH, -5_000);
                } else {
                    state.addResource(ResourceType.FOOD, -25);
                    state.setStability(state.stability() - 6);
                }
            }
            case "accident" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.CASH, -4_000);
                } else {
                    state.addResource(ResourceType.STEEL, -10);
                    state.setStability(state.stability() - 4);
                }
            }
            case "mobilize" -> {
                if (choice == 0) {
                    state.setPolicy(Policy.LIMITED);
                    state.setMorale(state.morale() + 4);
                } else {
                    state.setMorale(state.morale() - 3);
                }
            }
            case "blackmarket" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.CASH, -8_000);
                    state.addResource(ResourceType.STEEL, 30);
                } else {
                    state.setStability(state.stability() + 2);
                }
            }
            case "fueldeal" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.CASH, -10_000);
                    state.addResource(ResourceType.FUEL, 30);
                }
            }
            case "spy" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.CASH, -6_000);
                    state.setIntelligence(Math.min(3, state.intelligence() + 1));
                }
            }
            case "wages" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.CASH, -5_000);
                    state.setStability(state.stability() + 7);
                } else {
                    state.setStability(state.stability() - 8);
                    state.setMorale(state.morale() + 2);
                }
            }
            case "refugees" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.FOOD, -25);
                    state.setStability(state.stability() + 5);
                } else {
                    state.setStability(state.stability() - 2);
                }
            }
            case "budget" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.CASH, -9_000);
                    state.setMorale(state.morale() + 10);
                } else {
                    state.setMorale(state.morale() - 5);
                }
            }
            case "sabotage" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.CASH, -5_000);
                    state.setIntelligence(Math.min(3, state.intelligence() + 1));
                } else {
                    state.setStability(state.stability() - 4);
                }
            }
            case "border" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.CASH, -6_000);
                    state.setMorale(state.morale() + 5);
                } else {
                    state.setMorale(state.morale() - 2);
                    state.setStability(state.stability() + 3);
                }
            }
            case "ration" -> {
                if (choice == 0) {
                    state.addResource(ResourceType.FOOD, -35);
                    state.setStability(state.stability() + 4);
                } else {
                    state.setStability(state.stability() - 5);
                }
            }
            default -> throw new IllegalStateException("Unbekanntes Ereignis: " + id);
        }
    }

    private EnumMap<ResourceType, Integer> dailyFlows() {
        EnumMap<ResourceType, Integer> flows = new EnumMap<>(ResourceType.class);
        double industry = 1 - state.policy().industryPenalty();
        double stabilityFactor = state.stability() < 35 ? .78 : 1;
        double modifier = state.industryModifierDays() > 0 ? state.industryModifier() : 1;
        double industryFactor = industry * stabilityFactor * modifier;
        int civil = state.buildingPower(BuildingType.CIVIL);
        int military = state.buildingPower(BuildingType.MILITARY);
        int refinery = state.buildingPower(BuildingType.REFINERY);
        int intel = state.buildingPower(BuildingType.INTEL);
        int income = state.tax().income() + civil * 600;
        int expense = 1_900 + military * 250 + refinery * 180 + intel * 120
                + (state.policy() == Policy.GENERAL ? 500 : state.policy() == Policy.LIMITED ? 200 : 0);
        flows.put(ResourceType.CASH, income - expense);
        flows.put(ResourceType.FOOD, (int) Math.round(state.buildingPower(BuildingType.FARM) * 30 * industryFactor
                - foodUse()));
        flows.put(ResourceType.STEEL, (int) Math.round(state.buildingPower(BuildingType.STEELWORK) * 15 * industryFactor));
        flows.put(ResourceType.FUEL, (int) Math.round(state.buildingPower(BuildingType.REFINERY) * 12 * industryFactor
                - fuelUse()));
        flows.put(ResourceType.MANPOWER, state.policy().manpowerPerDay());
        return flows;
    }

    private int foodUse() {
        return 18 + state.armyCount(UnitType.INFANTRY) * UnitType.INFANTRY.foodUse()
                + state.armyCount(UnitType.ARTILLERY) * UnitType.ARTILLERY.foodUse()
                + state.armyCount(UnitType.TANK) * UnitType.TANK.foodUse()
                + state.armyCount(UnitType.LOGISTICS) * UnitType.LOGISTICS.foodUse();
    }

    private int fuelUse() {
        return state.armyCount(UnitType.TANK) * UnitType.TANK.fuelUse()
                + state.armyCount(UnitType.LOGISTICS) * UnitType.LOGISTICS.fuelUse();
    }

    private void dailyUpdate() {
        EnumMap<ResourceType, Integer> flows = dailyFlows();
        state.addResource(ResourceType.CASH, flows.get(ResourceType.CASH));
        if (state.resource(ResourceType.CASH) < 0) {
            state.setStability(state.stability() - 3);
            state.addHistory("Die Staatskasse ist überzogen. Stabilität sinkt.");
        }

        state.addResource(ResourceType.FOOD, flows.get(ResourceType.FOOD));
        if (state.resource(ResourceType.FOOD) < 0) {
            state.setResource(ResourceType.FOOD, 0);
            state.setStability(state.stability() - 7);
            state.setMorale(state.morale() - 5);
            state.addHistory("Nahrungsmangel belastet Bevölkerung und Armee.");
        }
        state.addResource(ResourceType.STEEL, flows.get(ResourceType.STEEL));
        state.addResource(ResourceType.FUEL, flows.get(ResourceType.FUEL));
        if (state.resource(ResourceType.FUEL) < 0) {
            state.setResource(ResourceType.FUEL, 0);
            state.setMorale(state.morale() - 3);
            state.addHistory("Treibstoffmangel bremst die Armee.");
        }
        state.addResource(ResourceType.MANPOWER, flows.get(ResourceType.MANPOWER));

        state.setStability(state.stability() - state.tax().stabilityStress() - state.policy().stabilityLossPerDay());
        state.setMorale(state.morale() + (state.resource(ResourceType.FOOD) > 100 ? .3 : -1));
        state.setStability(clamp(state.stability(), 0, 100));
        state.setMorale(clamp(state.morale(), 0, 100));
        completeProjects();
        completeProduction();
        advanceExperience();
        advanceEnemy();
        if (state.industryModifierDays() > 0) {
            state.setIndustryModifierDays(state.industryModifierDays() - 1);
        }
    }

    private void completeProjects() {
        for (ConstructionProject project : new ArrayList<>(state.construction())) {
            project.advanceDay();
            if (project.remainingDays() <= 0) {
                state.addBuilding(project.region(), project.type());
                state.removeConstruction(project);
                state.addHistory(project.type().displayName() + " in " + project.region().displayName()
                        + " fertiggestellt.");
            }
        }
    }

    private void completeProduction() {
        int factories = state.buildingPower(BuildingType.MILITARY);
        long activeIndustrialOrders = state.production().stream()
                .filter(order -> order.type() != UnitType.INFANTRY)
                .count();
        List<ProductionOrder> completed = new ArrayList<>();
        for (ProductionOrder order : new ArrayList<>(state.production())) {
            if (order.type() == UnitType.INFANTRY) {
                order.advanceInfantryDay();
            } else if (factories > 0) {
                order.addWork(Math.max(1, factories * 10d / Math.max(1, activeIndustrialOrders)));
            }
            if (order.completed()) {
                state.addArmy(order.type(), 1);
                state.addRecruitExperience(1);
                completed.add(order);
                state.addHistory(order.type().displayName() + " ist einsatzbereit.");
            }
        }
        completed.forEach(state::removeProduction);
    }

    private void advanceExperience() {
        if (state.recruitExperience() > 0) {
            state.setRecruitExperienceAge(state.recruitExperienceAge() + 1);
            if (state.recruitExperienceAge() >= 3) {
                state.removeRecruitExperience(1);
                state.addTrainedExperience(1);
                state.setRecruitExperienceAge(0);
            }
        }
    }

    private void advanceEnemy() {
        state.setEnemyFood(Math.max(0, state.enemyFood() + 26 - (18 + state.totalUnits(state.enemyUnits()) * 7)));
        state.setEnemyFuel(Math.max(0, state.enemyFuel() + 6 - state.enemyCount(UnitType.TANK) * 7
                - state.enemyCount(UnitType.LOGISTICS) * 2));
        if (state.day() % 3 == 0 && random.nextDouble() < .55) {
            state.addEnemy(UnitType.INFANTRY, 1);
        }
        if (state.day() >= 5 && state.day() % 4 == 1 && random.nextDouble() < .25) {
            state.addEnemy(UnitType.ARTILLERY, 1);
        }
        if (state.day() >= 8 && state.day() % 4 == 0 && random.nextDouble() < .28) {
            state.addEnemy(UnitType.TANK, 1);
        }
        state.setEnemyMorale((int) clamp(state.enemyMorale() + (random.nextBoolean() ? 1 : -1), 55, 85));
    }

    private double experienceFactor() {
        int total = state.totalUnits(state.army());
        if (total == 0) {
            return 1;
        }
        return (state.recruitExperience() * .9 + state.trainedExperience()
                + state.veteranExperience() * 1.15) / total;
    }

    private int weightedBase(java.util.Map<UnitType, Integer> units, boolean own) {
        if (own) {
            return units.get(UnitType.INFANTRY) * 110
                    + units.get(UnitType.ARTILLERY) * 132
                    + units.get(UnitType.TANK) * 205
                    + units.get(UnitType.LOGISTICS) * 28;
        }
        return units.get(UnitType.INFANTRY) * 100
                + units.get(UnitType.ARTILLERY) * 145
                + units.get(UnitType.TANK) * 225
                + units.get(UnitType.LOGISTICS) * 28;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
