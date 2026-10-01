package com.mehdi.daystowar.core;

import java.util.List;

public final class EventCatalog {
    private static final List<EventDefinition> EVENTS = List.of(
            event("harvest", "Schlechte Ernte",
                    "Trockenheit trifft die Landwirtschaft. Die Ernte fällt deutlich kleiner aus als erwartet.",
                    "Erntehilfen finanzieren", "-5.000 Geld · Stabilität bleibt erhalten",
                    "Sparen und durchhalten", "-25 Nahrung · -6 Stabilität"),
            event("accident", "Unfall im Stahlwerk",
                    "Ein Kessel fällt aus. Die Reparatur kostet Geld, aber ein langer Stillstand würde die Produktion treffen.",
                    "Sofort reparieren", "-4.000 Geld · Produktion läuft weiter",
                    "Reparatur verschieben", "-10 Stahl · -4 Stabilität"),
            event("mobilize", "Forderung nach Mobilisierung",
                    "Der Generalstab fordert mehr Soldaten an der Grenze. Die Wirtschaft warnt vor fehlenden Arbeitskräften.",
                    "Begrenzte Wehrpflicht ausrufen", "Wechsel auf begrenzte Wehrpflicht · +4 Moral",
                    "Forderung ablehnen", "-3 Moral · Wirtschaft bleibt geschützt"),
            event("blackmarket", "Angebot auf dem Schwarzmarkt",
                    "Ein Händler bietet sofort verfügbaren Stahl an. Herkunft und Qualität sind fragwürdig.",
                    "Lieferung annehmen", "-8.000 Geld · +30 Stahl",
                    "Angebot ablehnen", "Keine Kosten · Stabilität +2"),
            event("fueldeal", "Treibstofflieferung verfügbar",
                    "Ein neutraler Nachbarstaat kann einen kleinen Treibstoffvorrat liefern – gegen sofortige Zahlung.",
                    "Lieferung kaufen", "-10.000 Geld · +30 Treibstoff",
                    "Geld zurückhalten", "Keine Kosten · Angebot verfällt"),
            event("spy", "Spionagebericht",
                    "Ein Kontakt behauptet, der Gegner verlege Einheiten an die Grenze. Die Information könnte Gold wert sein.",
                    "Quelle bezahlen", "-6.000 Geld · Geheimdienstqualität steigt",
                    "Bericht ignorieren", "Keine Kosten · Bericht bleibt unbestätigt"),
            event("wages", "Arbeiter fordern höhere Löhne",
                    "In mehreren Betrieben drohen Streiks. Eine Einigung kostet Geld, sichert aber die Produktion.",
                    "Lohnerhöhung bewilligen", "-5.000 Geld · +7 Stabilität",
                    "Forderung zurückweisen", "-8 Stabilität · +2 Armeemoral"),
            event("refugees", "Flüchtlinge an der Grenze",
                    "Familien fliehen vor dem Konflikt. Ihre Aufnahme kostet Nahrung, könnte aber die Gesellschaft stärken.",
                    "Menschen aufnehmen", "-25 Nahrung · +5 Stabilität",
                    "Grenze schließen", "-2 Stabilität · keine Nahrungskosten"),
            event("budget", "Militär fordert mehr Budget",
                    "Die Armeeführung verlangt zusätzliche Mittel für Übungen und Ausrüstung.",
                    "Budget freigeben", "-9.000 Geld · +10 Armeemoral",
                    "Anfrage ablehnen", "-5 Armeemoral · Geld bleibt in der Kasse"),
            event("sabotage", "Verdacht auf Sabotage",
                    "Unbekannte wurden nahe einer Produktionsstätte gesehen. Eine Untersuchung kostet Zeit und Geld.",
                    "Sicherheitskräfte einsetzen", "-5.000 Geld · Geheimdienstqualität +1",
                    "Betrieb normal weiterführen", "Produktion bleibt · -4 Stabilität"),
            event("border", "Grenzzwischenfall",
                    "Ein Schusswechsel sorgt für Unruhe. Deine Reaktion beeinflusst, wie vorbereitet sich die Armee fühlt.",
                    "Verteidigung verstärken", "-6.000 Geld · +5 Armeemoral",
                    "Deeskalieren", "-2 Armeemoral · +3 Stabilität"),
            event("ration", "Vorräte werden knapp",
                    "Die Bevölkerung bemerkt steigende Preise. Die Regierung kann Vorräte freigeben oder den Markt arbeiten lassen.",
                    "Notreserven verteilen", "-35 Nahrung · +4 Stabilität",
                    "Marktpreise zulassen", "-5 Stabilität · keine Vorräte")
    );

    private EventCatalog() {
    }

    public static List<EventDefinition> all() {
        return EVENTS;
    }

    private static EventDefinition event(String id, String title, String description,
                                         String firstLabel, String firstConsequence,
                                         String secondLabel, String secondConsequence) {
        return new EventDefinition(id, title, description, List.of(
                new EventDefinition.Choice(firstLabel, firstConsequence),
                new EventDefinition.Choice(secondLabel, secondConsequence)));
    }
}
