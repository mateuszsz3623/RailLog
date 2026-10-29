# Notatki — RailLog (TaktRail)

> Prowadzi Architekt w OpenCode. Stan projektu, decyzje, TODO, gdzie są dostępy. Bez haseł i sekretów.

## Stan projektu (2026-10-29)

- Aplikacja: **RailLog 2.3.0** (versionCode 9) — pakiet 2.3 scalony z `feature/pakiet-2-3`: plan służb z ICS, numer służby, wymiar czasu pracy (baza v5), podgląd pociągu na żywo z API PLK, marka NIVEMI w README i opisach wydań.
- **Repo jest PUBLICZNE od 28.10** (wcześniej prywatne). Skutek: README nie może już mówić o prywatnym repo — poprawione 29.10 (D.4, commit 3f0fc76): przywrócona plakietka „Pobierz APK" i sekcja „📲 Pobierz aktualną wersję" (release/latest + alternatywa raw z gałęzi `apk-latest`).
- **Decyzja właściciela (29.10): klucz API PLK w publicznym APK zostaje** — nie usuwamy wydań i nie generujemy nowego klucza. Ryzyko przyjęte świadomie; przy kolejnej zmianie podejścia trzeba: nowy klucz, sekret `PLK_API_KEY`, usunięcie wydania i publikacja nowego APK.
- **Plan `.ai/plan.md` (redesign „KABINA", 2.2.0) jest w całości wykonany** — nowego planu dla 2.3 nie ma, praca 2.3 nie ma wpisów w logu poza D.4.
- Wydania APK buduje GitHub Actions (`.github/workflows/build-apk.yml`) i publikuje w GitHub Releases (wydanie „latest"); główne pliki wydania nazywają się RailLog-release.apk / RailLog-debug.apk. Stan 29.10: wydanie „latest" = 2.3.0, oba APK obecne, gałąź `apk-latest` = 2.3.0.
- Reguły pracy agentów (z Nordtherm, dostosowane do Androida) scalone do `main`.
- Środowisko: JAVA_HOME wskazuje `C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot` (jedyny JDK na dysku) — OK od 28.10. Lokalnie działa też assembleRelease (keystore lokalny dostępny).

## Incydent bazy v4 (28.10)

- Wydanie 2.1.0 crashowało na telefonie właściciela: baza na telefonie miała schemat v4 po lokalnym, niezcommitowanym buildzie z 26.10 (eksperymenty Antigravity). Naprawione w 2.1.1 migracją downgradu MIGRATION_4_3 (dane zachowane). Wersja bazy w kodzie zostaje 3; kolejna zmiana schematu = wersja 5.
- Gałąź lokalna `backup-dzisiejszy-stan` = backup eksperymentów z 26.10 (ikony semaforowe); nie scalać.

## Decyzje

- **Reguły pracy agentów**: jak w Nordtherm, ale sprawdzenie to `.\gradlew assembleDebug` + `.\gradlew testDebugUnitTest`; kontrola przed publikacją = pełny build + instalacja APK na telefonie właściciela; bez emulatora i telefonu na co dzień.
- **Klucz PLK w publicznym repo — zostaje (decyzja właściciela 29.10)**; świadomie nie usuwamy wydań i nie rotujemy klucza.
- **Tryb Kodera**: automatyczny — Architekt sam woła Kodera w tle, modelem **GLM-5.3-Flash** (`zai-coding-plan/glm-5.3-flash`, wliczony w plan Z.AI).
- **Scalenie do `main` = publikacja nowego APK** w GitHub Releases. Wersjonowany tag `v*` tylko za wyraźną zgodą właściciela.
- Architekt nie widzi obrazów wprost — zrzuty przegłada narzędziami wizyjnymi MCP (zai-mcp-server), gdy trzeba.
- Właściciel jest początkujący: tłumaczyć prosto, po polsku.
- **Repo prywatne od 28.10.** Klucz API PLK w sekrecie GitHuba `PLK_API_KEY` i lokalnie w `~/.gradle/gradle.properties` (`RAILLOG_PLK_API_KEY`); wbudowywany w każdy build (bez warunku na widoczność repo — decyzja właściciela). Przy upublicznieniu repo APK z wydań zawierają klucz (wtedy usunąć wydania i wygenerować nowy klucz).
- Baza Room w wersji 5 (plan służb + numer służby); kopia zapasowa Google włączona.

## TODO

- Właściciel ręcznie na GitHubie: usunąć stare wydanie „TaktRail v2.0.0" (Releases → Delete) i ustawić opis repozytorium (About) + ewentualnie tematy (topics).

## Dostępy

- Repo: https://github.com/mateuszsz3623/RailLog (origin).
- Podpis release: keystore w GitHub Secrets (KEYSTORE_BASE64 + hasła). Lokalne `raillog-release.jks` i `KEYSTORE_BASE64.txt` są w `.gitignore` — nigdy nie trafiają do repo.
