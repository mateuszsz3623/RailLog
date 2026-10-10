# RAILLOG 🚆
### Nowoczesny Dziennik Służb Maszynisty na system Android (v1.1.0)

[![Pobierz najnowsze APK](https://img.shields.io/badge/Pobierz_APK-RailLog_Najnowsza_Wersja-brightgreen?style=for-the-badge&logo=android)](https://github.com/mateuszsz3623/RailLog/releases/download/latest/RailLog-release.apk)
[![GitHub Release](https://img.shields.io/github/v/release/mateuszsz3623/RailLog?style=for-the-badge&color=red)](https://github.com/mateuszsz3623/RailLog/releases)
[![Build Status](https://img.shields.io/github/actions/workflow/status/mateuszsz3623/RailLog/build-apk.yml?branch=main&style=for-the-badge)](https://github.com/mateuszsz3623/RailLog/actions)

> 📲 **Pobierz najnowszą wersję na telefon:**  
> **[👉 Pobierz plik RailLog-release.apk (Najnowsza wersja)](https://github.com/mateuszsz3623/RailLog/releases/download/latest/RailLog-release.apk)**  
> *(Wszystkie oficjalne wydania i historia plików APK znajdują się w zakładce [Releases](https://github.com/mateuszsz3623/RailLog/releases))*

> **Elegancka, szybka i w 100% prywatna aplikacja dla maszynistów kolejowych do rejestrowania każdej służby.**  
> Twórca: **Mateusz Szerszeń**

---

## 🌟 Najważniejsze Cechy i Funkcjonalności

1. **Złota Zasada 20–30 sekund (Zero Friction):**
   - Zapisz całą służbę w kilkanaście sekund po zdaniu lokomotywy.
   - Tylko data jest wymagana (domyślnie dzisiejsza). Wszystkie pozostałe pola są opcjonalne.
   - Żadnych wieloekranowych kreatorów ani zbędnych kroków.

2. **Wieloodcinkowa Trasa (Legs) i Przypisanie Taboru:**
   - Służba może składać się z wielu kolejnych odcinków.
   - Każdy odcinek zawiera: stację początkową, docelową, numer pociągu oraz opcjonalnie własny pojazd trakcyjny.
   - Inteligentne łańcuchowanie stacji: nowy odcinek automatycznie podpowiada stację końcową poprzedniego.
   - Równorzędna prezentacja wszystkich pojazdów w podsumowaniu (`Pojazdy: ED160 • ET42`).

3. **Czas Służby i Ręczne Nadgodziny:**
   - Opcjonalne godziny rozpoczęcia i zakończenia z automatycznym wyliczaniem czasu trwania służby przechodzącej przez północ (np. 22:00 – 06:15).
   - Ręczne nadgodziny bez limitów z dokładnością do minuty (np. 17 h 07 min, 0 h 15 min).
   - Opcja „Cała służba w nadgodzinach”.

4. **Karta Znajomości Szlaku i Taboru:**
   - **Wyszukiwanie wg stacji**: pokazuje służby, w których stacja wystąpiła na początku lub końcu dowolnego odcinka, wraz z informacją o dacie ostatniego przejazdu i ile dni temu to było.
   - **Wyszukiwanie wg typu pojazdu**: grupuje historię wyłącznie po serii/typie (np. `ED160`, `ET42`), ignorując numery egzemplarzy.

5. **Miesięczne Statystyki i Filtry:**
   - Łatwe przełączanie między bieżącym, poprzednim a dowolnym historycznym miesiącem.
   - Suma godzin, nadgodzin i liczby przepracowanych służb.

6. **Eksport do Polskiego Excela i Pełny Backup:**
   - Eksport do CSV ze specjalnym nagłówkiem **UTF-8 z BOM** oraz separatorem `;`, co gwarantuje natychmiastowe poprawne wyświetlanie polskich znaków w programie Microsoft Excel.
   - Pełna kopia zapasowa JSON z opcją przywracania i scalania bazy.

7. **100% Offline i Prywatność:**
   - Brak logowania, kont, chmury, reklam i telemetrii. Wszystkie dane znajdują się wyłącznie w pamięci telefonu.

---

## 🎨 Design i Wygląd

- **Motyw Ciemny (Grafitowy):** Nowoczesny, głęboki grafit (`#0E1217`), subtelne obramowania i kolejowe akcenty czerwieni (`#E63946`).
- **Autorska ilustracja:** Dyskretna grafika lokomotywy spalinowej w nagłówku.
- **Wygoda:** Duże strefy dotykowe (min. 48–56 dp) umożliwiające wygodną obsługę jedną ręką.

---

## 🏗 Architektura Techniczna

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
- **Właściciel:** `CN=Mateusz Szerszen, O=RailLog, C=PL`

---

## 📜 Licencja

Projekt udostępniany na licencji [MIT](LICENSE).  
Copyright (c) 2026 Mateusz Szerszeń.
