# 100 Days to War

Ein eigenständiges Linux-Strategiespiel über die letzten 100 Tage vor dem Krieg.

Der aktuelle Prototyp ist ein 10-Tage-Spielstand. Der Java-Port bildet zuerst den vorhandenen Spielkern als testbare Simulation ab und erhält danach die vollständige libGDX-Oberfläche, Datenmodelle und die 100-Tage-Kampagne.

## Technischer Stand

- Java 25 LTS als Zielplattform
- libGDX 1.14.2
- libGDX LWJGL3-Backend für Linux/Desktop
- Gradle 9.8.0 als Build-System
- Kernsimulation unabhängig vom Rendering
- Linux-AppImage mit gebündelter Laufzeit als Release-Ziel

Die ausführliche Entscheidung steht in [`docs/TECH_STACK.md`](docs/TECH_STACK.md).

## Lokal starten

Voraussetzung für die Entwicklung ist ein JDK 25 und Gradle 9.8.0 oder neuer:

```bash
gradle :lwjgl3:run
```

Der Kern kann unabhängig von libGDX geprüft werden:

```bash
./scripts/check-core.sh
```

## AppImage

Das Release-Skript erzeugt das Zielartefakt:

```bash
./scripts/build-appimage.sh
```

Erwarteter Dateiname:

```text
100-Days-to-War-v0.2.0-x86_64.AppImage
```

## Prototyp

Der ursprüngliche HTML-Prototyp wurde vor der Portierung geprüft und dient lokal als Referenz für Balancing und Spielregeln. Die öffentliche Repository-Version enthält den portierten Java-Spielkern; die private Quelldatei des Prototyps wird nicht automatisch veröffentlicht.
