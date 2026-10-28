# RailLog 🚆

### Prywatny dziennik służb maszynisty na Androida

[![Pobierz APK](https://img.shields.io/badge/Pobierz_APK-RailLog_Najnowsza_Wersja-brightgreen?style=for-the-badge&logo=android)](https://github.com/mateuszsz3623/RailLog/releases/download/latest/RailLog-release.apk)
[![Build Status](https://img.shields.io/github/actions/workflow/status/mateuszsz3623/RailLog/build-apk.yml?branch=main&style=for-the-badge)](https://github.com/mateuszsz3623/RailLog/actions)
[![GitHub Release](https://img.shields.io/github/v/release/mateuszsz3623/RailLog?style=for-the-badge&color=red)](https://github.com/mateuszsz3623/RailLog/releases)

RailLog to elegancka i dopracowana w najdrobniejszych szczegółach aplikacja dla maszynistów kolejowych do rejestrowania każdej służby — w 100% offline i prywatna.
Filozofia projektowa: **Premium Fintech × Technical Precision × Railway Identity**.

---

## 📲 Pobierz aktualną wersję

**Aktualna wersja: 2.1.1**

> ### 👉 [**Pobierz RailLog-release.apk**](https://github.com/mateuszsz3623/RailLog/releases/download/latest/RailLog-release.apk)
> *(najnowsza wersja produkcyjna, automatycznie budowana z gałęzi `main`)*

**Instalacja krok po kroku:**
1. Pobierz plik `RailLog-release.apk` na telefon (link powyżej albo z zakładki [Releases](https://github.com/mateuszsz3623/RailLog/releases)).
2. Otwórz pobrany plik i zezwól na instalację z nieznanych źródeł, gdy system o to poprosi.
3. Zainstaluj — gotowe.
4. **Aktualizacja ponad starą wersję** działa bezpośrednio: instalujesz nowy APK bez odinstalowywania i **wszystkie dane zostają**.

Wersja diagnostyczna (do testów): [RailLog-debug.apk](https://github.com/mateuszsz3623/RailLog/releases/download/latest/RailLog-debug.apk).

---

## 🌟 Funkcje

- **📝 Dziennik służb z odcinkami trasy** — służba może składać się z wielu kolejnych odcinków; każdy ma stację początkową i docelową oraz numer pociągu. Nowy odcinek automatycznie podpowiada stację końcową poprzedniego.
- **🚄 Pojazdy trakcyjne przypisane do odcinków** — seria (np. `EU200`, `EP09`) i numer pojazdu, z szybkimi podpowiedziami najczęstszych serii i ostatnio prowadzonych pojazdów.
- **⏱️ Czas trwania i nadgodziny** — opcjonalne godziny rozpoczęcia i zakończenia z automatycznym wyliczaniem czasu (również przez północ), ręczne nadgodziny z dokładnością do minuty oraz opcja „cała służba nadgodzinowa".
- **📊 Statystyki miesięczne** — liczba służb, bilans nadgodzin i przełączanie między bieżącym, poprzednimi i historycznymi miesiącami.
- **🌓 Motywy ciemny / jasny / systemowy** — dopracowany grafityowy motyw ciemny i czysty motyw jasny, z opcją automatycznego dopasowania do systemu.
- **📤 Eksport CSV pod polski Excel** — plik UTF-8 z BOM i separatorem `;`, więc polskie znaki od razu wyświetlają się poprawnie w Microsoft Excel.
- **💾 Pełny backup JSON z przywracaniem** — kopia całej bazy danych w jednym pliku, z bezpiecznym przywracaniem i scalaniem.
- **✈️ 100% offline** — żadnego internetu, kont ani logowania. Wszystkie dane zostają na telefonie.

---

## 🔒 Prywatność

RailLog **nie zbiera żadnych danych**:

- ❌ bez kont i logowania,
- ❌ bez chmury,
- ❌ bez reklam i telemetrii.

Twoje służby, trasy i pojazdy istnieją **wyłącznie na Twoim urządzeniu**. Nikt poza Tobą nie ma do nich dostępu.

---

## 🛠️ Technologie

- **Język:** Kotlin 2.0+ (JVM 17)
- **UI:** Jetpack Compose + Material 3
- **Baza danych:** Room (SQLite)
- **Architektura:** MVVM / Clean Architecture
- **Build:** Gradle
- **Identyfikator aplikacji:** `pl.raillog.app` (stabilny, zachowuje ciągłość instalacji)

---

## 🚀 Jak zbudować aplikację

**Wersja debug (do testów):**
```powershell
.\gradlew assembleDebug
```
Plik wynikowy: `app/build/outputs/apk/debug/app-debug.apk`

**Wersja release (podpisana certyfikatem produkcyjnym):**
```powershell
.\gradlew assembleRelease
```
Plik wynikowy: `app/build/outputs/apk/release/app-release.apk`

Push na gałąź `main` uruchamia automatyczny build (GitHub Actions), który publikuje gotowe APK w wydaniu [latest](https://github.com/mateuszsz3623/RailLog/releases/tag/latest).

---

## 📄 Certyfikat podpisu APK

Aplikacja w wersji produkcyjnej jest podpisywana certyfikatem RSA 2048-bit:
- **Alias:** `raillog`
- **Ważność:** do 2054 roku
- **Właściciel:** `CN=Mateusz Szerszen, OU=RailLog Development, O=RailLog, L=Bydgoszcz, ST=Kujawsko-Pomorskie, C=PL`

Dzięki temu aktualizacja to zwykła instalacja nowego APK **ponad starą wersję** — bez odinstalowywania i bez utraty danych.

---

## ✍️ Autor

**Mateusz Szerszeń**

---

## 📜 Licencja

Projekt udostępniany na licencji [MIT](LICENSE).
Copyright (c) 2026 Mateusz Szerszeń.
