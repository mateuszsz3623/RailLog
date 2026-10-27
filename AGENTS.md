# AGENTS.md — RAILLOG (TAKTRAIL)

Wspólne zasady dla wszystkich agentów AI (Claude, OpenCode, Antigravity i innych).
Plik ładuje się przy każdej sesji, więc ma być krótki.

## Projekt w skrócie

- **Co to jest**: TaktRail (ewolucja projektu RailLog) — aplikacja Android dla maszynistów kolejowych do rejestrowania służb, w 100% offline i prywatna.
- **Stack**: Kotlin 2.0+ (JVM 17) + Jetpack Compose + Material 3 + Room (SQLite) + MVVM/Clean Architecture + Kotlinx Serialization; build Gradle.
- **Jak zbudować**: `.\gradlew assembleDebug` — APK ląduje w `app/build/outputs/apk/debug/app-debug.apk`.
- **Jak sprawdzić, że działa**: `.\gradlew assembleDebug` oraz `.\gradlew testDebugUnitTest` (wynik bez błędów).
- **Gdzie to działa / wdrożenie**: GitHub Releases (`https://github.com/mateuszsz3623/RailLog/releases`). Push na `main` uruchamia GitHub Actions (`.github/workflows/build-apk.yml`), które budują APK i publikują je w wydaniu „latest" — **scalenie do `main` = publikacja nowej wersji APK do pobrania**. Podpis release bierze się z keystore w GitHub Secrets; pliki `raillog-release.jks` i `KEYSTORE_BASE64.txt` są tylko lokalnie i nigdy nie trafiają do repo.
- Źródłem prawdy jest repo na GitHubie (`origin/main`). Pracuję na PC i laptopie.

## Pliki pomocnicze (czytaj tylko, gdy są potrzebne)

- `.ai/plan.md`: plan większej pracy — etapy, zadania do odhaczenia, decyzje.
- `.ai/log.md`: dziennik zatwierdzonych zadań (dopisuje Architekt, czyta Claude przy recenzji końcowej).
- `.ai/notatki.md`: stan projektu, decyzje, TODO, gdzie są dostępy (bez haseł).
- `.ai/zadanie.md`, `.ai/raport.md`: bieżące zadanie dla Kodera i jego raport. Pliki lokalne, nie trafiają do repo.

**Koder czyta tylko `.ai/zadanie.md`** i pliki w nim wymienione — plan, log i notatki czyta Architekt.

## Role

- **Claude (planista, opcjonalnie)**: w aplikacji Claude pisze plan do `.ai/plan.md` i go wypycha; wraca tylko przy zmianie planu i na recenzję końcową. Kodu nie zmienia.
- **Architekt (OpenCode)**: nadzorca planu — zamienia zadania z planu na `.ai/zadanie.md`, przekazuje je Koderowi, sprawdza jego pracę, prowadzi log, robi git i raz, przed publikacją, kontrolę buildu i testu na telefonie. Kodu nie zmienia.
- **Koder (OpenCode) / wykonawca**: robi zmiany w kodzie, szybko sprawdza i oddaje raport. Git ma zablokowany (commit, push, scalenie).
- **Tryb Kodera** (Architekt pyta raz na początku planu): **automatyczny** — Architekt sam woła Kodera w tle, z tym samym modelem; **ręczny** — właściciel otwiera okno `/koder`, wybiera model i pisze „wykonaj zadanie”.
- W OpenCode rolę wyznacza wybrany agent. W Claude i innych narzędziach domyślnie jesteś wykonawcą; architektem jesteś, gdy właściciel tak napisze albo powie, że robotę wykona inny agent.

## Przepływ pracy

**Mała zmiana** (da się ją opisać jednym zdaniem): wykonawca robi ją od razu, bez planu.

**Większa praca:**
1. **Plan** — z Claude'em (on zapisuje i wypycha plan na gałąź roboczą) albo **bez Claude'a**: właściciel pisze Architektowi w OpenCode „zaplanuj sam" i Architekt zapisuje plan w tym samym formacie. Dalej wszystko działa tak samo.
2. **Pętla w OpenCode (samodzielna)** — Architekt zapisuje kolejne zadanie do `.ai/zadanie.md` → przekazuje je Koderowi (w trybie automatycznym sam, w ręcznym przez właściciela) → Koder wykonuje i raportuje → Architekt sprawdza (**ZATWIERDZAM** / **POPRAWKI**) → odhacza zadanie w planie, dopisuje wpis do `.ai/log.md`, robi commit i push gałęzi roboczej i bierze następne zadanie. Zatrzymuje się tylko przy zmianie planu, trzecich poprawkach tego samego zadania, blokadzie i przed scaleniem do `main`.
3. **Drobnostki po drodze** (tekst, nazwa, mała poprawka) — Architekt dopisuje je w planie w sekcji „Drobne zmiany po drodze" i załatwia sam, bez Claude'a.
4. **Kontrola przed publikacją** — raz, gdy wszystkie zadania są odhaczone: Architekt robi pełny build (`.\gradlew assembleDebug` + `.\gradlew testDebugUnitTest`) i podaje właścicielowi APK do zainstalowania na telefonie i sprawdzenia; poprawki zleca Koderowi.
5. **Do Claude'a wracamy tylko** (jeśli plan pisał Claude):
   - przy **zmianie planu** — czegoś nie da się zrobić jak w planie, trzeba zmienić „Decyzje" albo zakres,
   - na **recenzję końcową** — po kontroli przed publikacją (raz na plan, nie po każdym etapie).
   Architekt sam mówi, kiedy, i daje gotową notkę do wklejenia.

## Raport wykonawcy

Po każdym zadaniu zapisz go do `.ai/raport.md` (nadpisz poprzedni) i pokaż w jednym bloku kodu (maks. ~15 linii, bez diffów):

```
RAPORT
Zadanie: <etap / jedno zdanie>
Status: zrobione / częściowo / zablokowane
Pliki: <lista>
Co zrobiono: <2–5 punktów>
Sprawdzenie: <co uruchomiono i wynik>
Odchylenia, pytania, ryzyka: <brak albo lista>
Git: <gałąź, ostatni commit, czy wypchnięte>
```

## Zasady pracy

- Rób **dokładnie to, o co proszono**, z minimum zmian. Nie poprawiaj „przy okazji” rzeczy, o które nikt nie prosił. Jeśli coś zauważysz, wspomnij o tym jednym zdaniem.
- Nie zgaduj. Jeśli czegoś nie wiesz, zapytaj.
- Zanim powiesz „gotowe”, uruchom sprawdzenie z sekcji „Projekt w skrócie” i podaj wynik.
- **Bez telefonu i emulatora na co dzień**: instalacja APK na telefonie, emulator i testy ręczne tylko raz — w kontroli przed publikacją (prowadzi ją Architekt z właścicielem). Koder sprawdza wyłącznie szybkimi komendami tekstowymi (build, testy jednostkowe).
- Jeśli coś nie działa po 2 próbach naprawy, zatrzymaj się i opisz problem, zamiast próbować w kółko.
- **Bez sekretów**: żadnych haseł, tokenów, kluczy API ani danych osobowych w repo i w odpowiedziach. Keystore (`*.jks`, `KEYSTORE_BASE64.txt`) i `local.properties` są w `.gitignore` — tak ma zostać.
- Nic, co kosztuje pieniądze albo zmienia coś na produkcji (zakupy, płatne API, wydanie nowej wersji APK, tag `v*`), bez zgody właściciela.

## Git

1. **Start sesji**: `git fetch` i `git status`. Jeśli drzewo jest czyste, a gałąź jest za zdalną, zrób `git pull --ff-only`. Jeśli są niezapisane zmiany albo historia się rozjechała, niczego nie nadpisuj, tylko opisz sytuację prostymi słowami i zapytaj.
2. Każda zmiana na nowej gałęzi od aktualnego `main`: `feature/krotki-opis` albo `fix/krotki-opis`.
3. Commity: `feat: …`, `fix: …`, `docs: …`, `chore: …`.
4. **Push tylko po słowie „wypchnij”** (albo „zapisz i wypchnij”). Wcześniej pokaż krótkie podsumowanie zmian. Wyjątki: Claude jako planista po „zapisz plan” wypycha plan na gałąź roboczą; **Architekt w OpenCode wypycha gałąź roboczą sam po każdym zatwierdzonym zadaniu** (stała zgoda właściciela, bez pytania).
5. **Scalenie do `main` tylko po słowie „scal”.** Poza scaleniem nigdy nie pushuj prosto do `main`.
6. Przed końcem sesji przypomnij o niezapisanych i niewypchniętych zmianach.
7. W OpenCode git (commit, push, scalenie) robi **Architekt sam** — właściciel nie wpisuje komend ręcznie. Koder ma `commit`, `push` i `merge` zablokowane. Scalenie do `main` (= publikacja nowego APK w GitHub Releases) zawsze wymaga słowa „scal”.

## Komunikacja

- Po polsku, na ty, krótko i konkretnie. Nie wklejaj całych plików, tylko zmienione fragmenty.
- Właściciel jest początkujący. Nowe pojęcia i komendy wyjaśniaj w 1–2 zdaniach. Błędy tłumacz prosto: co się stało, dlaczego i jak to naprawić.
