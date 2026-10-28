# Plan — Redesign „KABINA": kokpit maszynisty (cała aplikacja)

Autor: Architekt OpenCode
Data: 2026-10-28
Gałąź: `feature/redesign-kabina` (od aktualnego `main`)
- **Tryb Kodera**: automatyczny (stała decyzja z `.ai/notatki.md`)

## Cel

Przebudowa wyglądu całej aplikacji w duchu **„KABINA"** — kokpitu nowoczesnej lokomotywy: bento-grid na Starcie, wielka typografia liczb (monospace jak na wyświetlaczu maszyny), karty bez ramek na poziomach tonalnych, ciemny motyw jako wiodący. Interfejs czytany rzutem oka, „narzędzie zawodowe", nie gadżet. Bez zmian w logice, danych i przepływie zapisu.

## Decyzje

- **Zakres: cała aplikacja** — wszystkie ekrany (Dashboard, Statystyki, Historia, Szczegóły, Formularz, Kalendarz, Ustawienia) + nawigacja + design system (wybór właściciela).
- **Ciemny motyw wiodący** — ciemny jest już domyślny (ThemePreferences: `DARK`); zostaje. Jasny pozostaje pełnoprawny: żadnych hardcodowanych kolorów — wszystko przez `MaterialTheme.colorScheme` (dziś w kodzie są sztywne `Color(0xFFCBD5E1)` itp. — do usunięcia).
- **Wbudowane fonty (offline, w APK)**: **Inter** (tekst: Regular/Medium/SemiBold/Bold) + **JetBrains Mono** (liczby, etykiety techniczne: Regular/Medium/SemiBold/Bold). Pliki TTF do `app/src/main/res/font/`, rodziny XML, licencje OFL w `THIRD_PARTY.md` w repo.
- **Karty bez ramek** (dark) — hierarchia przez poziomy tonalne i światło; ramki zostają tylko jako cienki wariant w light. Spójna skala promieni: 12 / 16 / 20 dp.
- **Wielka typografia liczb**: nowa skala w `Type.kt` — `displayLarge` 44 sp / `displayMedium` 34 sp (mono) na godziny i KPI.
- **Pasek postępu miesiąca** (norma vs stan) jako cienka linia-wskaźnik na Dashboardzie i w Statystykach; jeśli w danych nie ma wyraźnej „normy", pasek pokazuje tempo miesiąca (liczba służb vs przepracowane dni) — bez zmian w logice ani nowej konfiguracji.
- **Wersja**: 2.1.1 → **2.2.0**, versionCode 6 → **7**.
- Logo/ikona aplikacji **bez zmian** (wdrożone w planie „nowe logo").

## Poza zakresem

- Kierunek 1 „TAKT" (animacje, mikrointerakcje, shared-element transitions) — możliwy jako osobny plan później.
- Kierunek 3 „EXPRES" (quick-add, inteligentne domyślne, zmiany przepływu zapisu, wyszukiwarka).
- Zmiany w logice, bazie danych, eksporcie/backupie, ViewModels (wyjątek: nic — czysta warstwa UI).
- Nowe funkcje i nowe ekrany.
- Tag wersjonowany `v*` (tylko na wyraźną prośbę).

## Etapy i zadania

### Etap 1 — Fundament: fonty i design system

- [x] **1.1 Wbudowane fonty + nowa typografia**
  Dodać TTF (Inter: Regular/Medium/SemiBold/Bold; JetBrains Mono: Regular/Medium/SemiBold/Bold) do `app/src/main/res/font/` + rodziny XML (`inter.xml`, `jetbrains_mono.xml`) + `THIRD_PARTY.md` z licencjami OFL. Przepiąć `Type.kt` na nowe rodziny; nowa skala: `displayLarge` 44 sp, `displayMedium` 34 sp (JetBrains Mono, SemiBold), reszta skalowana spójnie; etykiety techniczne (`labelMedium`/`labelSmall`) na JetBrains Mono.
  Pliki: `res/font/*`, `Type.kt`, `THIRD_PARTY.md` (nowy).
- [x] **1.2 Komponenty kokpitu**
  W `RailLogComponents.kt`: nowy `BentoCard` (bez ramek w dark, tonalny, promień 20 dp; w light cienka ramka) i `CockpitNumber` (wielka liczba mono + jednostka + podpis, kolory z motywu). `TechnicalCard` na bezramkowy wariant (zachować opcjonalny border), usunąć hardcodowane kolory light (`Color(0xFFCBD5E1)` i podobne → `colorScheme.outline`/`outlineVariant`), `QuickChip`/`StatusBadge`/przyciski na nowej typografii i promieniach.
  Pliki: `RailLogComponents.kt` (+ dotknięte ekrany tylko na tyle, ile wymaga zmiana sygnatur).

### Etap 2 — Nawigacja

- [x] **2.1 Dolny pasek „kokpitowy"**
  W `RailLogNavHost.kt`: wyższy pasek (64 dp), aktywna pozycja jako zaokrąglona pigułka za ikoną (proporcje kapsuły), bez sztywnej linii granicznej w dark (cień/ton tylko), etykiety w JetBrains Mono. Usunąć hardcodowane `Color(0xFFDC2626)`/`Color(0xFF475569)` → motyw.
  Pliki: `RailLogNavHost.kt`.

### Etap 3 — Dashboard (kokpit)

- [x] **3.1 Bento-grid na Starcie**
  `DashboardScreen.kt`: duży kafel „dziś" (data, status dnia: służba dzisiejsza/wolne), KPI jako `CockpitNumber` w `BentoCard`, cienki pasek postępu miesiąca, hero CTA „NOWA SŁUŻBA", chipsy ostatnich pojazdów, karta ostatniej służby bez ramki, dopracowany empty state. Nagłówki sekcji na mono.
  Pliki: `DashboardScreen.kt`.

### Etap 4 — Statystyki

- [x] **4.1 Kokpit liczb**
  `StatisticsScreen.kt`: wielkie liczby okresu (godziny, służby, nadgodziny) w `CockpitNumber`, ranking serii jako lista z cienkimi paskami proporcji (bez ramek), przełącznik okresu jako tonalny element.
  Pliki: `StatisticsScreen.kt`.

### Etap 5 — Pozostałe ekrany

- [x] **5.1 Historia służb**
  `HistoryScreen.kt`: karty bez ramek na tonach, filtry jako chipsy w nowym stylu, podsumowanie miesiąca w mono.
- [x] **5.2 Szczegóły służby**
  `DutyDetailScreen.kt`: układ kokpitu — wielkie godziny na górze, trasa jako pionowa oś czasu, badge'y statusów, akcje na dole.
- [ ] **5.3 Formularz służby**
  `DutyFormScreen.kt`: sekcje jako tonalne kafle, spójne pola i przyciski, nagłówki mono, bez zmian w walidacji i logice zapisu.
- [ ] **5.4 Kalendarz i Ustawienia**
  `CalendarScreen.kt`, `SettingsScreen.kt`: dopasowanie do nowego języka (kafle, typografia, przełącznik motywu w nowym stylu; dark dalej domyślny).

### Etap 6 — Porządki

- [ ] **6.1 Wersja aplikacji**
  `app/build.gradle.kts`: `versionCode = 7`, `versionName = "2.2.0"`.

### Etap 7 — Kontrola przed publikacją

- [ ] **K.1 Pełny build** (Koder): `.\gradlew assembleDebug` + `.\gradlew testDebugUnitTest`.
- [ ] **K.2 Test na telefonie** (właściciel): sprawdzenie wydania z GitHub Releases po scaleniu (jak w poprzednim planie); ewentualne uwagi załatwiamy jako K.x / D.x.

## Drobne zmiany po drodze

(brak — dopisywane jako D.1, D.2… gdy się pojawią)
