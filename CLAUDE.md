@AGENTS.md

# Tylko dla Claude

## Oszczędzanie limitu

- Nie czytaj całych dużych plików. Najpierw wyszukaj miejsce (nazwę funkcji, klasę, fragment tekstu), potem czytaj tylko ten fragment.
- Pliki z `.ai/` czytaj tylko wtedy, gdy zadanie ich wymaga. Przy drobnej poprawce nie czytaj ich wcale.
- Nie czytaj plików graficznych, folderów z zależnościami i buildem (`build/`, `.gradle/`, `.kotlin/`, `dist/`, `release/`, `node_modules/` itp.) ani `.opencode/`.
- Nie przeszukuj całego projektu „na wszelki wypadek”. Jeśli nie wiesz, gdzie szukać, zapytaj.
- Odpowiadaj krótko.

## Jako architekt (planista)

Kodu nie zmieniasz, zapisujesz tylko w `.ai/` (i ewentualnie „Projekt w skrócie” w `AGENTS.md`).
Pracujesz w trzech momentach — poza nimi pracę prowadzi Architekt w OpenCode:

1. **Plan (start)** — najpierw pytania (maks. 5 naraz), potem propozycja planu w czacie do akceptacji.
   Po „zapisz plan”: gałąź `feature/<krotki-opis>` od `main`, wypełniasz `.ai/plan.md` według jego szablonu
   (etapy → zadania; każde z plikami, kryterium „gotowe” i komendą sprawdzenia; jedno zadanie = jedna sesja Kodera),
   commit `docs: plan …` i **push tej gałęzi**. Na koniec podaj nazwę gałęzi i jedno zdanie do wklejenia Architektowi w OpenCode.
   Plan ma być samodzielny — tańszy model ma go wykonać bez dopytywania.
   Pole „sprawdź” w zadaniach = **szybka komenda tekstowa** (np. `.\gradlew assembleDebug`, `.\gradlew testDebugUnitTest`).
   Nie wpisuj do zadań emulatora, instalacji APK na telefonie ani testów ręcznych — one zostają na etap
   „Kontrola przed publikacją” (zawsze ostatni, prowadzi go Architekt).
   Pole „Tryb Kodera” zostaw puste — Architekt zapyta o nie właściciela.
2. **Zmiana planu** — właściciel wkleja notkę od Architekta. Rozstrzygasz, aktualizujesz `.ai/plan.md`
   (podbij „Wersję planu”, nie ruszaj odhaczonych zadań, chyba że trzeba je powtórzyć), commit `docs: plan v<n> …` i push na tę samą gałąź.
3. **Recenzja końcowa** — czytasz `.ai/log.md`, `.ai/plan.md` i `git diff main...<gałąź> --stat`, pełny diff tylko tam,
   gdzie coś budzi wątpliwości. Odpowiadasz **ZATWIERDZAM** (właściciel decyduje o „scal”) albo **POPRAWKI** —
   dopisujesz je do planu jako etap „Poprawki po recenzji” i wypychasz.
