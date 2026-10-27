# Plan — Nowe logo TaktRail i ikona APK (OPCJA B z semaforem)

Autor: Architekt OpenCode
Data: 2026-10-27
Gałąź: `feature/nowe-logo` (od aktualnego `main`)
- **Tryb Kodera**: automatyczny (model GLM-5.3-Flash — stała decyzja z `.ai/notatki.md`)

## Cel

Wdrożenie nowego logo aplikacji i ikony APK z grafiki `RailLog_Kompletna_Paczka/ikony.jpg` — wybrana **OPCJA B** (prawy górny kwadrant: realistyczny przód pociągu z reflektorami + semafor z zielonym światłem). Logo trafia do ekranów aplikacji (Dashboard, Ustawienia) i do ikony launchera, w stylu jak dotychczas: **plakietka wycentrowana na ciemnym tle**.

## Decyzje

- **OPCJA B** = prawy górny kwadrant `ikony.jpg` (potwierdzone podglądem wizyjnym) — wybór właściciela spośród wariantów A–D.
- **Styl ikony**: plakietka wycentrowana na ciemnym tle aplikacji (jak obecna ikona) — decyzja właściciela. Cała grafika widoczna, semafor nieucięty.
- **Struktura ikon zostaje bez zmian** — manifest, XML-e adaptive icon i warstwy jak dotychczas:
  - tło: `@drawable/ic_launcher_background.xml` (wektor, ciemny granat),
  - foreground: `@drawable/ic_launcher_foreground.png` (432×432, plakietka w strefie bezpieczeństwa 68/108),
  - monochrome: `@drawable/ic_launcher_monochrome.xml` (ikony tematyczne Android 13+) — bez zmian,
  - stare ikony rasterowe: `mipmap-*/ic_launcher.png` + `ic_launcher_round.png` (48–192 px).
- **Paczka z Antigravity nie jest używana wprost**: jej `skrypt_kadrujacy.py` zapisuje te same PNG-i 48–192 px jako warstwy adaptive icon (poprawnie: warstwa to 108 dp → 432 px — inaczej ikona byłaby rozmyta) i XML-e z paczki wyrzucają warstwę monochrome. Bazujemy zamiast tego na sprawdzonym `design/process_user_logo.py` (nim wygenerowano obecne ikony).
- **Źródło grafiki w repo**: kopia `ikony.jpg` jako `design/ikony_warianty.jpg` (źródło prawdy dla PC i laptopa).
- **Wersja**: 2.0.0 → **2.1.0**, versionCode 4 → **5**.
- **Paczka lokalnie**: folder `RailLog_Kompletna_Paczka/` i `.zip` zostają na dysku poza repo (dopisać do `.gitignore`).

## Poza zakresem

- Zmiany w kodzie ekranów i logice aplikacji (poza podmianą plików graficznych).
- Opcje A, C, D z grafiki (pozostają w źródle `design/ikony_warianty.jpg`).
- Tag wersjonowany `v*` (tylko na wyraźną prośbę właściciela).
- Publikacja poza GitHub Releases (scalenie do `main` = nowy APK w wydaniu „latest" — jak zwykle po słowie „scal").

## Etapy i zadania

### Etap 1 — Grafika

- [ ] **1.1 Skrypt generujący + wszystkie pliki PNG nowego logo**
  Skopiować `RailLog_Kompletna_Paczka/ikony.jpg` → `design/ikony_warianty.jpg`; napisać `design/generate_semaphore_icons.py` na bazie `design/process_user_logo.py` (kadrowanie: prawy górny kwadrant → wykrycie plakietki na jednolitym tle, z pominięciem napisu „OPCJA B" nad nią → dalszy pipeline jak dotychczas). Uruchomić i nadpisać: `design/taktrail_icon_512.png`, `design/taktrail_icon_round_512.png`, `drawable/ic_taktrail_logo.png` (512), `drawable/ic_launcher_foreground.png` (432), `mipmap-*/ic_launcher.png` + `ic_launcher_round.png` (48/72/96/144/192).
  Sprawdzenie Architekta: podgląd masterów narzędziem wizyjnym (poprawne kadrowanie OPCJI B, semafor nieucięty, brak ramek tła na rogach).

### Etap 2 — Porządki

- [ ] **2.1 Wersja aplikacji + .gitignore**
  `app/build.gradle.kts`: `versionCode = 5`, `versionName = "2.1.0"`. `.gitignore`: dopisać `RailLog_Kompletna_Paczka/` i `RailLog_Kompletna_Paczka.zip`.

### Etap 3 — Kontrola przed publikacją

- [ ] **K.1 Pełny build** (Koder): `.\gradlew assembleDebug` + `.\gradlew testDebugUnitTest`.
- [ ] **K.2 Test na telefonie** (właściciel): instalacja APK debug (`app/build/outputs/apk/debug/app-debug.apk`); sprawdzić nową ikonę w launcherze (zwykła, okrągła, tematyczna) i logo w Dashboard oraz Ustawieniach.

## Drobne zmiany po drodze

(brak — dopisywane jako D.1, D.2… gdy się pojawią)
