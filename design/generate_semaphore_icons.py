"""Generuje komplet ikon aplikacji TaktRail z grafiki 'OPCJA B'.

Zrodlo: design/ikony_warianty.jpg (4 warianty w siatce 2x2 na wspolnym
ciemnogranatowym tle). Wybieramy prawy gorny kwadrant (OPCJA B) i automatycznie
wykrywamy plakiete (najwiekszy ciagly pas pikseli roznych od tla kwadrantu,
co pomija mniejszy napis 'OPCJA X' nad plakieta).

Pipeline bazuje na design/process_user_logo.py:
- kadrowanie plakietki z marginesem ~3 px,
- supersampling x4 + maska squircle o promieniu ZMIERZONYM z geometrii plakietki
  (R ~ (szerokosc_plakietki - dlugosc_gornej_krawedzi) / 2, clamp 15-30%),
- plakietka skalowana ~1.02-1.03x, aby margines detekcji wypal POZA maska,
- SELF-CHECK geometryczny mastera 512 (rogi poza maska przezroczyste,
  pas krawedziowy w pelni wypelniony; RuntimeError przy porazce),
- master 512 i logo w aplikacji,
- ikona okragla na ciemnym podkladzie RGB(19,25,36) z cienka jasna obwodka,
- mipmapy 48/72/96/144/192 px (plakietka na 94% rozmiaru, przezroczyste tlo),
- foreground adaptive icon 432x432 (plakietka w strefie bezpieczenstwa).

Uruchomienie z katalogu glownego projektu:  python design/generate_semaphore_icons.py
Skrypt zapisuje wylacznie wewnatrz katalogu projektu.
"""
import os

import numpy as np
from PIL import Image, ImageDraw

SOURCE = 'design/ikony_warianty.jpg'  # prawy gorny kwadrant = OPCJA B
DIFF_THRESHOLD = 25   # prog roznicy piksela od koloru tla kwadrantu
MIN_ROW_PIXELS = 3    # minimalna liczba pikseli maski, by wiersz/kolumna byl 'niepusty'
DETECT_MARGIN = 3     # margines wokol wykrytego bboxa (px)
SQUARE_TOLERANCE = 0.1  # dopuszczalne odchylenie stosunku bokow bboxa od 1.0

RADIUS_MIN = 0.15       # dolny clamp promienia maski squircle (uamek szerokosci)
RADIUS_MAX = 0.30       # gorny clamp promienia maski squircle
BADGE_SCALE_MIN = 1.02  # minimalne powiekszenie plakietki (margines poza maska)
BADGE_SCALE_MAX = 1.03  # maksymalne powiekszenie plakietki (nie obcinac za duzo)
SELF_CHECK_ALPHA = 8    # piksel 'nieprzezroczysty' gdy alpha > ta wartosc
SELF_CHECK_BAND = 4     # szerokosc pasa krawedziowego mastera 512 (px)
SELF_CHECK_MARGIN = 2   # zapas na miekki brzeg LANCZOS przy krzywej squircle (px)


def detect_badge(im):
    """Zwraca kadrowana plakiete z prawego gornego kwadrantu obrazu."""
    w, h = im.size
    quad = im.crop((w // 2, 0, w, h // 2))
    arr = np.asarray(quad).astype(int)

    bg = arr[5, 5, :3].copy()  # jednolite tlo kwadrantu z rogu (RGB)
    print(f'Tlo kwadrantu (probka z rogu): RGB({bg[0]}, {bg[1]}, {bg[2]})')

    diff = np.abs(arr[:, :, :3] - bg).max(axis=2)
    mask = diff > DIFF_THRESHOLD

    row_counts = mask.sum(axis=1)
    bands = []  # ciagle pasy wierszy z pikselami roznic od tla
    start = None
    for y, count in enumerate(row_counts):
        if count >= MIN_ROW_PIXELS and start is None:
            start = y
        elif count < MIN_ROW_PIXELS and start is not None:
            bands.append((start, y - 1))
            start = None
    if start is not None:
        bands.append((start, len(row_counts) - 1))
    if not bands:
        raise RuntimeError('Nie znaleziono plakietki w prawym gornym kwadrancie.')

    # Najwiekszy pas = plakietka; mniejsze pasy (np. napis 'OPCJA B') pomijamy.
    ys, ye = max(bands, key=lambda b: b[1] - b[0])
    print(f'Pasy wierszy: {bands} -> wybieram najwiekszy: rows {ys}..{ye}')

    col_counts = mask[ys:ye + 1].sum(axis=0)
    cols = np.where(col_counts >= MIN_ROW_PIXELS)[0]
    xs, xe = int(cols.min()), int(cols.max())

    qw, qh = quad.size
    box = (
        max(0, xs - DETECT_MARGIN),
        max(0, ys - DETECT_MARGIN),
        min(qw - 1, xe + DETECT_MARGIN),
        min(qh - 1, ye + DETECT_MARGIN),
    )
    bw = box[2] - box[0] + 1
    bh = box[3] - box[1] + 1
    ratio = bw / bh
    print(f'Wykryty bbox plakietki: x={box[0]}..{box[2]}, y={box[1]}..{box[3]} '
          f'(wymiary {bw}x{bh}, stosunek bokow {ratio:.3f})')
    if not (1.0 - SQUARE_TOLERANCE <= ratio <= 1.0 + SQUARE_TOLERANCE):
        print(f'UWAGA: bbox nie jest kwadratowy (stosunek {ratio:.3f} poza 0.9-1.1)!')

    # Rzeczywisty promien zaokraglenia plakietki:
    # R ~ (szerokosc_plakietki - dlugosc_gornej_prostej_krawedzi) / 2.
    badge_w = xe - xs + 1  # szerokosc plakietki bez marginesu detekcji
    top_edge = int(mask[ys, xs:xe + 1].sum())
    r_measured = (badge_w - top_edge) / 2.0 / badge_w
    radius_frac = min(RADIUS_MAX, max(RADIUS_MIN, r_measured))
    print(f'Diagnoza geometrii: gorna prosta krawedz = {top_edge} px przy szerokosci '
          f'{badge_w} px -> promien zaokraglenia ~{100 * r_measured:.1f}% '
          f'(clamp 15-30% -> uzywam {100 * radius_frac:.1f}%)')

    return quad.crop(box), ratio, radius_frac


def build_squircle(badge_high, high_w, high_h, radius_frac):
    """Plakietka przycieta maska squircle o danym promieniu (hi-res)."""
    mask_squircle = Image.new('L', (high_w, high_h), 0)
    draw_sq = ImageDraw.Draw(mask_squircle)
    r_sq = int(high_w * radius_frac)
    draw_sq.rounded_rectangle([0, 0, high_w - 1, high_h - 1], radius=r_sq, fill=255)
    squircle = Image.new('RGBA', (high_w, high_h), (0, 0, 0, 0))
    squircle.paste(badge_high, (0, 0), mask_squircle)
    return squircle


def self_check_master(path, radius_frac):
    """Geometryczny self-check mastera 512 (POPRAWKA 2 do zadania 1.1).

    Check celowo NIE porownuje kolorow z tlem zrodlowym: panel plakietki
    RGB(2,20,40) jest niemal identyczny z tlem RGB(3,20,38), wiec kryterium
    kolorowe z definicji nie moze przejsc. Weryfikujemy geometrie maski:
    a) ROGI: piksel jest poza krzywa squircle (zaokraglony prostokat o
       promieniu r = 512 * radius_frac), gdy dx = max(0, r-x, x-(511-r)),
       dy = max(0, r-y, y-(511-r)) i dx^2+dy^2 > r^2. Piksele poza krzywa
       z zapasem SELF_CHECK_MARGIN px (miekki brzeg LANCZOS) musza byc
       przezroczyste; naruszenia zliczamy w czterech strefach naroznych r x r.
    b) KRAWEDZIE: w pasach prostych krawedzi (SELF_CHECK_BAND px od kazdej
       krawedzi, wiersze/kolumny od r do 511-r, bez lukow naroznych) kazdy
       piksel musi miec alpha = 255.
    Zwraca (ok, corner_bad, edge_bad).
    """
    m = np.asarray(Image.open(path).convert('RGBA')).astype(int)
    h = m.shape[0]
    r = int(h * radius_frac)
    alpha = m[:, :, 3]
    xs = np.arange(h)
    dx = np.maximum(0, np.maximum(r - xs, xs - (h - 1 - r)))
    dy = np.maximum(0, np.maximum(r - xs, xs - (h - 1 - r)))  # obraz kwadratowy
    r_tol = r + SELF_CHECK_MARGIN
    outside = dx[None, :] ** 2 + dy[:, None] ** 2 > r_tol * r_tol
    opaque = alpha > SELF_CHECK_ALPHA

    corner_bad = 0
    corner_zones = (
        (slice(0, r), slice(0, r)),
        (slice(0, r), slice(h - r, h)),
        (slice(h - r, h), slice(0, r)),
        (slice(h - r, h), slice(h - r, h)),
    )
    for ysl, xsl in corner_zones:
        corner_bad += int((outside[ysl, xsl] & opaque[ysl, xsl]).sum())

    b = SELF_CHECK_BAND
    edge_bands = (alpha[:b, r:h - r], alpha[h - b:, r:h - r],
                  alpha[r:h - r, :b], alpha[r:h - r, h - b:])
    edge_bad = sum(int((band != 255).sum()) for band in edge_bands)
    return corner_bad == 0 and edge_bad == 0, corner_bad, edge_bad


def process_logo():
    if not os.path.exists(SOURCE):
        raise FileNotFoundError(f'Brak pliku zrodlowego: {SOURCE}')
    im = Image.open(SOURCE).convert('RGB').convert('RGBA')

    badge_crop, ratio, radius_frac = detect_badge(im)
    if not (1.0 - SQUARE_TOLERANCE <= ratio <= 1.0 + SQUARE_TOLERANCE):
        print('PRZERYWAM: wykryty bbox nie jest w miare kwadratowy - '
              'sprawdz zrodlo zamiast zgadywac poprawki.')
        return

    superscale = 4
    high_w = 512 * superscale
    high_h = 512 * superscale
    badge_high = badge_crop.resize((high_w, high_h), Image.Resampling.LANCZOS)

    # Margines detekcji musi wypasc POZA maske: plakietke skalujemy tak, aby
    # pas tla o szerokosci DETECT_MARGIN znalazl sie poza kwadratem 512 (hi-res).
    margin_high = DETECT_MARGIN * high_w / badge_crop.size[0]
    needed_scale = high_w / (high_w - 2.0 * margin_high)
    scale_fix = min(BADGE_SCALE_MAX, max(BADGE_SCALE_MIN, needed_scale))
    grown_w = int(round(high_w * scale_fix))
    grown = badge_high.resize((grown_w, grown_w), Image.Resampling.LANCZOS)
    off = (grown_w - high_w) // 2
    badge_high = grown.crop((off, off, off + high_w, off + high_w))
    print(f'Margines detekcji w hi-res: {margin_high:.1f} px na strone -> '
          f'skaluje plakietke x{scale_fix:.4f} (wystarczyloby x{needed_scale:.4f}), '
          f'centrujac kadrow {high_w}x{high_w}')

    # Maska squircle o zmierzonym promieniu, zapis mastera 512 i SELF-CHECK.
    badge_squircle_high = build_squircle(badge_high, high_w, high_h, radius_frac)
    master_512 = badge_squircle_high.resize((512, 512), Image.Resampling.LANCZOS)
    master_512.save('design/taktrail_icon_512.png', 'PNG')
    master_512.save('app/src/main/res/drawable/ic_taktrail_logo.png', 'PNG')
    print('Generated design/taktrail_icon_512.png: 512x512')
    print('Generated app/src/main/res/drawable/ic_taktrail_logo.png: 512x512')

    ok, corner_bad, edge_bad = self_check_master(
        'design/taktrail_icon_512.png', radius_frac)
    print(f'SELF-CHECK: rogi poza masce przezroczyste = '
          f'{"OK" if corner_bad == 0 else "NIEOK"} ({corner_bad} px), '
          f'krawedzie wypelnione = {"OK" if edge_bad == 0 else "NIEOK"} '
          f'({edge_bad} px)')
    if not ok:
        raise RuntimeError(
            'Self-check geometryczny mastera 512 nie przeszedl: '
            f'piksele nieprzezroczyste poza maska (rogi) = {corner_bad} px, '
            f'piksele niedomkniete w pasie krawedziowym = {edge_bad} px. '
            'Przerywam bez generowania dalszych plikow - sprawdz geometrie '
            'zrodla zamiast zgadywac poprawki.')

    # 3. Ikona okragla na ciemnym podkladzie RGB(19,25,36) z cienka obwodka
    mask_circle = Image.new('L', (high_w, high_h), 0)
    draw_cir = ImageDraw.Draw(mask_circle)
    margin = int(high_w * 0.02)
    draw_cir.ellipse([margin, margin, high_w - margin, high_h - margin], fill=255)

    circle_high = Image.new('RGBA', (high_w, high_h), (19, 25, 36, 255))
    circle_high.paste(badge_high, (0, 0))

    round_final_high = Image.new('RGBA', (high_w, high_h), (0, 0, 0, 0))
    round_final_high.paste(circle_high, (0, 0), mask_circle)

    draw_rim = ImageDraw.Draw(round_final_high)
    draw_rim.ellipse([margin, margin, high_w - margin, high_h - margin],
                     outline=(180, 190, 205, 230), width=superscale * 3)

    master_round_512 = round_final_high.resize((512, 512), Image.Resampling.LANCZOS)
    master_round_512.save('design/taktrail_icon_round_512.png', 'PNG')
    print('Generated design/taktrail_icon_round_512.png: 512x512')

    # 4. Stare mipmapy (48/72/96/144/192 px)
    densities = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192,
    }
    base_res = 'app/src/main/res'
    for folder, sz in densities.items():
        folder_path = os.path.join(base_res, folder)
        os.makedirs(folder_path, exist_ok=True)

        icon_canvas = Image.new('RGBA', (sz, sz), (0, 0, 0, 0))
        content_sz = int(sz * 0.94)
        offset = (sz - content_sz) // 2
        scaled_badge = badge_squircle_high.resize((content_sz, content_sz), Image.Resampling.LANCZOS)
        icon_canvas.paste(scaled_badge, (offset, offset), scaled_badge)
        icon_canvas.save(os.path.join(folder_path, 'ic_launcher.png'), 'PNG')

        round_canvas = Image.new('RGBA', (sz, sz), (0, 0, 0, 0))
        scaled_round = round_final_high.resize((content_sz, content_sz), Image.Resampling.LANCZOS)
        round_canvas.paste(scaled_round, (offset, offset), scaled_round)
        round_canvas.save(os.path.join(folder_path, 'ic_launcher_round.png'), 'PNG')
        print(f'Generated {folder}: {sz}x{sz} (ic_launcher + ic_launcher_round)')

    # 5. Foreground adaptive icon 432x432 (plakietka w strefie bezpieczenstwa)
    adaptive_size = 432
    safe_badge_size = int(adaptive_size * (68.0 / 108.0))  # 272 px
    offset_ad = (adaptive_size - safe_badge_size) // 2
    adaptive_fg = Image.new('RGBA', (adaptive_size, adaptive_size), (0, 0, 0, 0))
    scaled_badge_safe = badge_squircle_high.resize((safe_badge_size, safe_badge_size), Image.Resampling.LANCZOS)
    adaptive_fg.paste(scaled_badge_safe, (offset_ad, offset_ad), scaled_badge_safe)
    adaptive_fg.save('app/src/main/res/drawable/ic_launcher_foreground.png', 'PNG')
    print('Generated app/src/main/res/drawable/ic_launcher_foreground.png: 432x432 '
          f'(plakietka {safe_badge_size}x{safe_badge_size})')


if __name__ == '__main__':
    process_logo()
