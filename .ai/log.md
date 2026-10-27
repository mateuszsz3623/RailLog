# Log — dziennik zatwierdzonych zadań

> Dopisuje Architekt, jeden wpis na końcu pliku. Czyta Claude przy recenzji końcowej.
> Format: `- [data] [gałąź] [zadanie] — [zrobione / częściowo / blokada] [commit] [notka]`

## Wpisy

- 2026-10-27 `feature/reguly-agentow` — Reguły pracy agentów AI (AGENTS.md, CLAUDE.md, .opencode, .gitignore) — zrobione — 43cf794 — skopiowane z Nordtherm, dostosowane do Androida (Gradle, GitHub Releases, kontrola przez build + telefon zamiast przeglądarki). Ustalony tryb Kodera: automatyczny, model GLM-5.3-Flash.
- 2026-10-27 `feature/nowe-logo` — 1.1 Nowe logo aplikacji i ikony APK (OPCJA B z semaforem) — zrobione — 97acf7c — skrypt design/generate_semaphore_icons.py na bazie process_user_logo.py (auto-detekcja plakietki z pominięciem napisu, zmierzony promień zaokrąglenia 18,1%, skala ×1.02, geometryczny self-check); 14 PNG zregenerowanych (masters 512, logo w aplikacji, foreground 432, mipmapy 48–192); źródło design/ikony_warianty.jpg; weryfikacja wizyjna Architekta OK (rogi czyste, brak ramki tła). 2 poprawki: (1) resztki tła JPG w rogach → zmierzony promień + przykrycie marginesu detekcji, (2) self-check kolorowy niewykonalny (panel plakietki ≈ kolor tła) → geometryczny. Uwagi: skrypt wymaga numpy (na laptopie ew. pip install numpy); systemowy JAVA_HOME wskazuje usunięty JDK — Koder omija w sesji.
- 2026-10-27 `feature/nowe-logo` — 2.1 Wersja 2.1.0 / versionCode 5 + .gitignore paczki — zrobione — ff2f822 — build.gradle.kts podbity, RailLog_Kompletna_Paczka/ i .zip wykluczone z repo (źródło grafiki bezpiecznie w design/ikony_warianty.jpg).
- 2026-10-28 `feature/nowe-logo` — Kontrola przed publikacją (K.1 + K.2) — zrobione — 8db9eba — K.1: assembleDebug OK, testDebugUnitTest 27/27 OK; K.2: właściciel zdecydował sprawdzić nowe wydanie bezpośrednio z GitHub Releases po scaleniu (zamiast instalacji debug APK). JAVA_HOME naprawiony w systemie (jedyny JDK: jdk-17.0.21.21-hotspot).
- 2026-10-28 `main` — Scalenie feature/nowe-logo i publikacja TaktRail 2.1.0 — scalenie — merge do main — GitHub Actions buduje i publikuje APK 2.1.0 w wydaniu „latest"; plan nowego logo wykonany w całości (1.1, 2.1, K.1, K.2).
