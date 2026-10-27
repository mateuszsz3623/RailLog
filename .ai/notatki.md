# Notatki — RailLog (TaktRail)

> Prowadzi Architekt w OpenCode. Stan projektu, decyzje, TODO, gdzie są dostępy. Bez haseł i sekretów.

## Stan projektu (2026-10-28)

- Aplikacja stabilna: TaktRail 2.1.0 (versionCode 5) — nowe logo i ikona APK (OPCJA B z semaforem). Wydania APK buduje GitHub Actions (`.github/workflows/build-apk.yml`) i publikuje w GitHub Releases (wydanie „latest").
- Reguły pracy agentów (z Nordtherm, dostosowane do Androida) scalone do `main`.
- Środowisko: JAVA_HOME wskazuje `C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot` (jedyny JDK na dysku) — OK od 28.10.

## Decyzje

- **Reguły pracy agentów**: jak w Nordtherm, ale sprawdzenie to `.\gradlew assembleDebug` + `.\gradlew testDebugUnitTest`; kontrola przed publikacją = pełny build + instalacja APK na telefonie właściciela; bez emulatora i telefonu na co dzień.
- **Tryb Kodera**: automatyczny — Architekt sam woła Kodera w tle, modelem **GLM-5.3-Flash** (`zai-coding-plan/glm-5.3-flash`, wliczony w plan Z.AI).
- **Scalenie do `main` = publikacja nowego APK** w GitHub Releases. Wersjonowany tag `v*` tylko za wyraźną zgodą właściciela.
- Architekt nie widzi obrazów wprost — zrzuty przegłada narzędziami wizyjnymi MCP (zai-mcp-server), gdy trzeba.
- Właściciel jest początkujący: tłumaczyć prosto, po polsku.

## TODO

- (brak)

## Dostępy

- Repo: https://github.com/mateuszsz3623/RailLog (origin).
- Podpis release: keystore w GitHub Secrets (KEYSTORE_BASE64 + hasła). Lokalne `raillog-release.jks` i `KEYSTORE_BASE64.txt` są w `.gitignore` — nigdy nie trafiają do repo.
