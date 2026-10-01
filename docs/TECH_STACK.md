# Technische Entscheidung

## Entscheidung

Für **100 Days to War** wird **Java + libGDX + Gradle** eingesetzt. Das Desktop-Backend ist das aktuelle libGDX-LWJGL3-Backend.

| Bereich | Entscheidung |
| --- | --- |
| Hauptsprache | Java 25 LTS |
| Framework | libGDX 1.14.2 |
| Desktop-Backend | libGDX LWJGL3 |
| Build-System | Gradle 9.8 |
| Zielplattform | Linux Desktop x86_64 |
| Releaseformat | AppImage mit gebündelter Laufzeit |

## Warum Java?

Der gelieferte 10-Tage-Prototyp ist eine einzelne HTML/JavaScript-Datei. Er ist eine wichtige spielbare Referenz für Regeln und Balancing, aber keine tragfähige Kernarchitektur für die geplante Desktop-Version. Die Regeln werden deshalb nicht mit JavaScript im Spielkern vermischt, sondern in eine testbare Java-Simulation überführt.

Java ist für dieses Projekt langfristig passend, weil:

- die Simulation aus vielen klar trennbaren Systemen besteht,
- objektorientierte Modelle für Länder, Regionen, Gebäude, Einheiten und Ereignisse sinnvoll sind,
- Gameplay, Simulation, Daten und Rendering sauber getrennt werden können,
- libGDX ein ausgereiftes 2D-Framework mit Linux-Desktop-Unterstützung bietet,
- Gradle reproduzierbare Builds und CI-Builds ermöglicht,
- Java 25 zusammen mit der AppImage-Laufzeit ausgeliefert werden kann.

Eine Python-Migration würde keinen Vorteil bringen: Der vorhandene Prototyp ist nicht in Python aufgebaut, und ein paralleler Java/Python-Kern würde die Architektur unnötig verkomplizieren.

## Architektur

```text
core/
  Simulation, Spielzustand, Ereignisse, Ressourcen, Gebäude, Einheiten, Kriegstest

lwjgl3/
  Linux/Desktop-Launcher und libGDX-Oberfläche

data/
  spätere datengetriebene Länder-, Regionen-, Ereignis- und Diplomatiemodelle

prototype/
  unveränderte HTML-Referenz des 10-Tage-Prototyps
```

Der `core`-Teil kennt kein Rendering. Er kann deshalb in Tests und später auch in Headless-Simulationen ausgeführt werden. Die Oberfläche sendet nur Spieleraktionen an die Simulation und rendert den aktuellen Zustand.

## Build und Laufzeit

Die Java-Version ist zentral im Root-Build festgelegt. Der CI-Build verwendet ein JDK 25. Nutzer des fertigen Releases benötigen kein separat installiertes Java: Das AppImage wird mit `jpackage` als App-Image mit eigener Runtime vorbereitet und anschließend als `100-Days-to-War-v0.2.0-x86_64.AppImage` verpackt.

Lokal für Entwickler:

```bash
gradle :lwjgl3:run
```

Release:

```bash
./scripts/build-appimage.sh
```

## Drittanbieter-Abhängigkeiten

- `com.badlogicgames.gdx:gdx:1.14.2`
- `com.badlogicgames.gdx:gdx-backend-lwjgl3:1.14.2`
- `com.badlogicgames.gdx:gdx-platform:1.14.2:natives-desktop`
- JUnit Jupiter 5.12.2 für Simulationstests
- Gradle 9.8 für Build und Packaging
- `jpackage` aus dem JDK und `appimagetool` für das Linux-AppImage

## Portierungsumfang des Prototyps

Der erste Java-Stand übernimmt die vorhandene Spielidee und die zentrale Schleife:

1. Morgenbericht und Ressourcenbilanz
2. Tagesereignis mit zwei Entscheidungen
3. Bauaufträge für Regionen
4. Einheitenproduktion und Versorgung
5. Politik, Steuern, Handel und Geheimdienst
6. Gegnerentwicklung
7. Kriegstest mit Bericht über die entscheidenden Faktoren

Als nächstes folgen die 100-Tage-Kampagne, persistente Spielstände, datengetriebene Inhalte, erweiterte Diplomatie und die vollständige UI.
