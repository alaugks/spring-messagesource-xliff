# Checkstyle / Spring Code Style

Stand: 2026-11-19, Branch `feature/code-style`.

Das Projekt folgt dem **Spring Code Style** (`spring-javaformat`) vollständig, ohne
Regeln abzuschalten. Geprüft wird per Maven.

## Konfiguration

| Datei | Zweck |
|---|---|
| `checkstyle.xml` | Bindet `io.spring.javaformat.checkstyle.SpringChecks` ein und setzt `headerFile`. |
| `header.txt` | Eigenes Header-Template (Apache 2.0, `Copyright André Laugks`, ohne Jahreszahl). |
| `pom.xml` | `maven-checkstyle-plugin` 3.6.0 (Checkstyle 10.18.2, `spring-javaformat-checkstyle` 0.0.48), `includeTestSourceDirectory=true`, `violationSeverity=warning`. Zusätzlich `spring-javaformat-maven-plugin` mit `validate` in der Phase `validate`. |

Hinweise:

- Die `src`-Pfade müssen **nicht** in `checkstyle.xml` stehen. Das Maven-Plugin prüft
  `src/main` und (wegen `includeTestSourceDirectory`) `src/test`.
- `headerFile` ist relativ zur Projektwurzel (`header.txt`). `${config_loc}` wird vom
  Maven-Plugin nicht gesetzt. Checkstyle muss daher aus der Projektwurzel laufen.
- `header.txt` ist Klartext (jede Zeile wird literal verglichen). Die Zeile
  `Copyright André Laugks` darf nicht auf einen Punkt enden.
- Das Checkstyle-Plugin hängt **nicht** am Build-Lifecycle, es läuft nur explizit.

## Befehle

```
mvn checkstyle:check                                                        # Regeln prüfen
mvn io.spring.javaformat:spring-javaformat-maven-plugin:0.0.48:apply        # Format automatisch anwenden
mvn test                                                                    # Tests
```

Stand jetzt: `checkstyle:check` ohne Verstöße, 252 Tests grün.

## Was getan wurde

Ausgangslage: 331 Verstöße. Behoben wurde so:

1. **Formatierung**: `spring-javaformat:apply` (44 Dateien umformatiert).
2. **Header**: SPDX-Header durch Apache-2.0-Header mit `Copyright André Laugks` ersetzt
   (43 Dateien, zwei Dateien hatten vorher keinen Header). Dafür `header.txt` +
   `headerFile`, da der Standard "the original author or authors" und eine Jahreszahl
   vorschreibt.
3. **Tests umbenannt**: 14 Klassen `*Test` → `*Tests` (Dateien und Klassennamen), da
   `SpringTestFileName` das verlangt.
4. **BDD-Mockito**: `Mockito.when` → `BDDMockito.given` in `XliffDocumentTests`.
5. **Imports**: Reihenfolge `java`, `javax`, Sonstige, `org.springframework`, dann statische
   Imports, jeweils durch Leerzeile getrennt.
6. **Kleinere Regeln**: Catch-Variable `ex`, Klammern bei Ternary und Lambda-Parametern,
   `@Nullable` direkt vor dem Typ, redundantes `public` entfernt (`@Override` bei
   `Xliff12Document.getUnits()`), `final` bei `InlineElementStrategyFactory`,
   `XliffDocumentParser` und `TestHelper` (mit privatem Konstruktor), Zeilenende in
   Test-`.properties`.
7. **Javadoc**:
   - `@author André Laugks` an allen 25 betroffenen Typen.
   - Javadoc an `private`-Methoden in normale `//`-Kommentare umgewandelt (keine Tags,
     Konvention für private Methoden). Damit entfallen die `@param`/`@return`-Pflichten
     und die Parse-Fehler bei XML-Elementen wie `<segment>`.

## Git-Stand

Nichts committet. Die 14 Testdateien sind im Index als **reine Umbenennung** gestaged
(alter Inhalt, neuer Dateiname und Klassenname), alle weiteren Änderungen liegen
ungestaged im Arbeitsverzeichnis. So lässt sich zuerst die Umbenennung committen
(`git log --follow` funktioniert dann), danach der Rest.

Noch untracked und beim Commit mitzunehmen: `checkstyle.xml`, `header.txt`,
`CHECKSTYLE.md`.

## Offen / später

- **Commit-Reihenfolge**: (1) Umbenennung der Tests, (2) Checkstyle-Konfiguration,
  (3) Formatierung und Code-Anpassungen. Je Schritt `mvn test` prüfen.
- **Build-Integration**: `maven-checkstyle-plugin` per `<execution>` an `validate`
  (oder `verify`) hängen, damit Verstöße den Build brechen. Danach in CI aufnehmen.
- **Public Javadoc**: Öffentliche Methoden wurden nicht angefasst. Neue öffentliche
  Methoden brauchen `@param`/`@return`, Typen `@author`.
- **Header neuer Dateien**: Neue Dateien brauchen den Header aus `header.txt`
  (Checkstyle meldet `SpringHeader`).
- **Dokumentation**: Kurzer Abschnitt für Contributors im README
  (`mvn spring-javaformat:apply`, `mvn checkstyle:check`) und ggf. Hinweis auf
  Spring-Style.
