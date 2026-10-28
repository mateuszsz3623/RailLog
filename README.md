# RailLog 🚆

### Twój dziennik służb — prosty, szybki, zawsze pod ręką

[![Build Status](https://img.shields.io/github/actions/workflow/status/mateuszsz3623/RailLog/build-apk.yml?branch=main&style=for-the-badge)](https://github.com/mateuszsz3623/RailLog/actions)

**Aktualna wersja: 2.3.0**

---

## Dla kogo

Dla maszynistów, którzy chcą szybko zapisać służbę po zjeździe: trasę, pociągi, pojazdy, godziny i nadgodziny — bez kont, bez logowania, bez zbędnych kliknięć.

## Co potrafi

- **Dziennik służb z odcinkami** — stacje, numery pociągów, seria i numer pojazdu na każdym odcinku, z podpowiedziami z historii.
- **Godziny i nadgodziny** — czas liczony automatycznie (także przez północ), nadgodziny co do minuty. Godziny możesz wpisać skrótem, np. `600` albo `6.15`.
- **Plan służb z pliku .ics** — jeśli pracodawca udostępnia plan, wczytaj go. Na ekranie Start zobaczysz najbliższą służbę, a jednym kliknięciem „Zapisz z planu" wypełnisz formularz. Plan to tylko podpowiedź — wszystko możesz zmienić.
- **Statystyki i wymiar czasu pracy** — podsumowanie miesiąca, plan kontra wymiar z Kodeksu pracy i to, co faktycznie zapisałeś.
- **Podgląd pociągu na żywo** — wpisz numer pociągu IC i sprawdź status, opóźnienie oraz trasę stacja po stacji (dane PKP Polskie Linie Kolejowe S.A.; odświeżanie ręczne).
- **Kalendarz, historia i wyszukiwarka** — znajdziesz służbę po stacji, pociągu, pojeździe czy uwagach.
- **Eksport i kopie zapasowe** — CSV pod polski Excel, kopia JSON z przywracaniem, automatyczna kopia awaryjna przed każdym nadpisaniem danych.
- **Szkic formularza** — jeśli telefon zamknie aplikację w trakcie wpisywania, nic nie przepada.

## Instalacja i aktualizacja

Repozytorium jest **prywatne**:

1. Na telefonie zaloguj się na GitHubie w przeglądarce.
2. Wejdź w **Releases** i pobierz plik `RailLog-release.apk`.
3. Otwórz go i zezwól na instalację z nieznanych źródeł, gdy system o to poprosi.

Aktualizacja instaluje się **ponad starą wersję** — dane zostają. Wersja debug to osobna aplikacja „RailLog Debug", która instaluje się obok i niczego nie nadpisuje.

## Prywatność

Bez kont, reklam i trackerów. Dane są na telefonie; kopia zapasowa Androida może je przechować na Twoim koncie Google. Podgląd pociągów łączy się z API PLK tylko, gdy o to poprosisz.

## Dla deweloperów

Kotlin, Jetpack Compose, Material 3, Room, MVVM.

```powershell
.\gradlew assembleDebug
.\gradlew testDebugUnitTest
.\gradlew lintDebug
```

Klucz API PLK podajesz lokalnie jako właściwość Gradle `RAILLOG_PLK_API_KEY` (np. w `~/.gradle/gradle.properties`), w CI jako sekret `PLK_API_KEY`. Bez klucza podgląd pociągów jest ukryty. Push na `main` buduje i publikuje APK w wydaniu „latest".

## Autor

**NIVEMI — Mateusz Szerszeń**

## Licencja

[MIT](LICENSE) · Copyright (c) 2026 Mateusz Szerszeń.
