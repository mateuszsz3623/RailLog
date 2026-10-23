# TAKTRAIL 🚆
### Nowoczesny Dziennik Służb Maszynisty na system Android (v2.0.0)

[![Pobierz najnowsze APK](https://img.shields.io/badge/Pobierz_APK-TaktRail_Najnowsza_Wersja-brightgreen?style=for-the-badge&logo=android)](https://github.com/mateuszsz3623/RailLog/releases/download/latest/TaktRail-release.apk)
[![GitHub Release](https://img.shields.io/github/v/release/mateuszsz3623/RailLog?style=for-the-badge&color=red)](https://github.com/mateuszsz3623/RailLog/releases)
[![Build Status](https://img.shields.io/github/actions/workflow/status/mateuszsz3623/RailLog/build-apk.yml?branch=main&style=for-the-badge)](https://github.com/mateuszsz3623/RailLog/actions)

> 📲 **Pobierz najnowszą wersję na telefon:**  
> **[👉 Pobierz plik TaktRail-release.apk (Najnowsza wersja produkcyjna)](https://github.com/mateuszsz3623/RailLog/releases/download/latest/TaktRail-release.apk)**  
> *(Wszystkie oficjalne wydania i historia plików APK znajdują się w zakładce [Releases](https://github.com/mateuszsz3623/RailLog/releases))*

> **TaktRail** (będący bezpośrednią ewolucją projektu RailLog) to elegancka, dopracowana w najdrobniejszych szczegółach i w 100% prywatna aplikacja dla maszynistów kolejowych do rejestrowania każdej służby.  
> Filozofia projektowa: **Premium Fintech × Technical Precision × Railway Identity**.  
> Twórca: **Mateusz Szerszeń**

---

## 🌟 Najważniejsze Cechy TaktRail 2.0

1. **Nowa Tożsamość i Autorska Ikona Launcher:**
   - Geometryczny, autorski znak marki łączący literę **T**, dwie równoległe szyny oraz precyzyjny wskaźnik rytmu czasu (Takt) z rubinowym akcentem Griffin Red.
   - Pełne wsparcie dla Android Adaptive Icon (z bezpieczną strefą safe-zone, warstwą tła, pierwszego planu oraz wersją monochromatyczną dla motywów tematycznych Android 13+).
   - Dedykowane zasoby wektorowe oraz zoptymalizowane wersje rastrowe mipmap dla wszystkich gęstości ekranu (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi).

2. **Pojazdy Trakcyjne Przypisywane Wyłącznie do Odcinków:**
   - Usunięto globalny wybór pojazdu z poziomu całej służby — tabor przypisywany jest precyzyjnie do poszczególnych odcinków trasy.
   - Szybkie chipy podpowiedzi najczęstszych serii (`EU160`, `EU200`, `EP09`, `ED250`, `ET42`, `EN57`) oraz ostatnio prowadzonych pojazdów bezpośrednio w kafelkach odcinków.
   - 100% zgodności wstecznej z historycznymi wpisami.

3. **Dwa Dopracowane Motywy Wizualne z Trwałym Zapamiętywaniem:**
   - **Motyw Ciemny (Carbon Graphite):** Głębokie grafity (`#0A0E14`), techniczne obramowania, wysoki kontrast w kabinie maszynisty w nocy.
   - **Motyw Jasny (Technical Precision):** Czysta stalowa biel (`#F8FAFC`), wysoka czytelność w pełnym słońcu.
   - Opcja automatycznego dopasowania do motywu systemu Android.

4. **Złota Zasada 20–30 sekund (Zero Friction):**
   - Zapisz całą służbę w kilkanaście sekund po zdaniu lokomotywy.
   - Tylko data jest wymagana (domyślnie dzisiejsza). Wszystkie pozostałe pola są opcjonalne.

5. **Wieloodcinkowa Trasa (Legs) i Inteligentne Łańcuchowanie:**
   - Służba może składać się z wielu kolejnych etapów.
   - Każdy odcinek zawiera: stację początkową, docelową, numer pociągu oraz własny pojazd trakcyjny.
   - Nowy odcinek automatycznie podpowiada stację końcową poprzedniego.

6. **Czas Służby i Ręczne Nadgodziny:**
   - Opcjonalne godziny rozpoczęcia i zakończenia z automatycznym wyliczaniem czasu trwania służby przechodzącej przez północ (np. 22:00 – 06:15).
   - Ręczne nadgodziny bez limitów z dokładnością do minuty (np. +17 h 07 min, +0 h 15 min).
   - Opcja „Cała służba w nadgodzinach”.

7. **Karta Znajomości Szlaku i Taboru:**
   - **Wyszukiwanie wg stacji**: pokazuje służby ze stacją na początku lub końcu odcinka, z informacją o dacie ostatniego przejazdu i liczbie dni temu.
   - **Wyszukiwanie wg typu pojazdu**: grupuje historię wg serii/typu taboru.

8. **Miesięczne Statystyki i Filtry:**
   - Przełączanie między bieżącym, poprzednim a historycznymi miesiącami.
   - Suma godzin, nadgodzin i liczby przepracowanych służb.

9. **Eksport do Polskiego Excela i Pełny Backup:**
   - Eksport CSV ze specjalnym nagłówkiem **UTF-8 z BOM** oraz separatorem `;`, co gwarantuje natychmiastowe poprawne wyświetlanie polskich znaków w Microsoft Excel.
   - Pełna kopia zapasowa JSON z opcją przywracania i bezpiecznego scalania bazy danych.

10. **100% Offline i Pełna Prywatność:**
    - Zero logowania, kont, chmury, reklam i telemetrii. Wszystkie dane znajdują się wyłącznie w pamięci telefonu.

---

## 🏗 Architektura Techniczna

- **Identyfikator aplikacji:** `pl.raillog.app` (stabilny identyfikator zachowujący ciągłość instalacji)
- **Język:** Kotlin 2.0+ (JVM 17)
- **UI:** Jetpack Compose + Material 3
- **Baza danych:** Room Database (SQLite, v3) z migracją schema
- **Architektura:** MVVM / Clean Architecture (core, data, domain, presentation)
- **Serializacja:** Kotlinx Serialization

---

## 🚀 Jak zbudować aplikację

### 1. Wersja Debug (do testów):
```powershell
.\gradlew assembleDebug
```
Plik wynikowy: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Wersja Release (podpisana certyfikatem produkcyjnym):
```powershell
.\gradlew assembleRelease
```
Plik wynikowy: `app/build/outputs/apk/release/app-release.apk`

---

## 📄 Certyfikat Podpisu APK

Aplikacja w wersji produkcyjnej jest podpisywana certyfikatem RSA 2048-bit:
- **Alias:** `raillog`
- **Ważność:** do 2054 roku
- **SHA-256 Fingerprint:** `C8:98:E7:40:2F:F0:D7:39:57:56:FA:FD:94:27:3A:52:80:B4:C0:CC:E1:53:68:1D:E5:52:B2:D9:9A:15:E4:00`
- **Właściciel:** `CN=Mateusz Szerszen, OU=RailLog Development, O=RailLog, L=Bydgoszcz, ST=Kujawsko-Pomorskie, C=PL`

Umożliwia to bezpośrednią, bezproblemową aktualizację istniejącej instalacji na telefonie bez konieczności odinstalowywania aplikacji i bez ryzyka utraty danych.

---

## 📜 Licencja

Projekt udostępniany na licencji [MIT](LICENSE).  
Copyright (c) 2026 Mateusz Szerszeń.
