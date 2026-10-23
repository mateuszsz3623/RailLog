import os
from PIL import Image, ImageDraw, ImageFilter

def process_logo():
    img_path = r'C:/Users/mszer/.gemini/antigravity/brain/b2df6288-a8bc-4574-bc72-67deea8c5f0c/.user_uploaded/media_1792792143849_1b3bf53c.jpg'
    im = Image.open(img_path).convert('RGBA')

    # Bounding box of the badge in original 1024x571:
    # x: 306 to 717 (width 411), y: 80 to 486 (height 406)
    # Center: cx = 511.5, cy = 283
    cx, cy = 511.5, 283
    half_w = 205.5
    half_h = 203.0
    
    # We want a high-precision square crop around the badge
    # Let's crop with 2px outer margin for smooth antialiased outer border
    pad = 2
    crop_box = (
        int(cx - half_w - pad),
        int(cy - half_h - pad),
        int(cx + half_w + pad),
        int(cy + half_h + pad)
    )
    badge_crop = im.crop(crop_box)
    bw, bh = badge_crop.size

    # Scale up using Lanczos for supersampled mask generation
    superscale = 4
    high_w = 512 * superscale
    high_h = 512 * superscale
    badge_high = badge_crop.resize((high_w, high_h), Image.Resampling.LANCZOS)

    # 1. High-precision squircle mask (with subtle corner curve matching the double chrome frame)
    mask_squircle = Image.new('L', (high_w, high_h), 0)
    draw_sq = ImageDraw.Draw(mask_squircle)
    # Radius is about 22% of dimension
    r_sq = int(high_w * 0.22)
    draw_sq.rounded_rectangle([0, 0, high_w - 1, high_h - 1], radius=r_sq, fill=255)

    # Composite squircle badge
    badge_squircle_high = Image.new('RGBA', (high_w, high_h), (0, 0, 0, 0))
    badge_squircle_high.paste(badge_high, (0, 0), mask_squircle)

    # 2. Master 512x512 Squircle Logo (for in-app and Play Store)
    master_512 = badge_squircle_high.resize((512, 512), Image.Resampling.LANCZOS)
    master_512.save('design/taktrail_icon_512.png', 'PNG')
    # Save directly as in-app logo drawable
    master_512.save('app/src/main/res/drawable/ic_taktrail_logo.png', 'PNG')

    # 3. High-precision circular mask for Round Launcher Icon
    mask_circle = Image.new('L', (high_w, high_h), 0)
    draw_cir = ImageDraw.Draw(mask_circle)
    margin = int(high_w * 0.02)
    draw_cir.ellipse([margin, margin, high_w - margin, high_h - margin], fill=255)

    # Circular badge on dark backing matching the artwork background (#131924)
    circle_high = Image.new('RGBA', (high_w, high_h), (19, 25, 36, 255))
    circle_high.paste(badge_high, (0, 0))
    
    round_final_high = Image.new('RGBA', (high_w, high_h), (0, 0, 0, 0))
    round_final_high.paste(circle_high, (0, 0), mask_circle)
    
    # Outer thin chrome rim on the circular icon
    draw_rim = ImageDraw.Draw(round_final_high)
    draw_rim.ellipse([margin, margin, high_w - margin, high_h - margin], outline=(180, 190, 205, 230), width=superscale * 3)

    master_round_512 = round_final_high.resize((512, 512), Image.Resampling.LANCZOS)
    master_round_512.save('design/taktrail_icon_round_512.png', 'PNG')

    # 4. Generate all legacy mipmap densities
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

        # Legacy squircle icon with 4% padding for drop-shadow breathing room
        icon_canvas = Image.new('RGBA', (sz, sz), (0, 0, 0, 0))
        content_sz = int(sz * 0.94)
        offset = (sz - content_sz) // 2
        scaled_badge = badge_squircle_high.resize((content_sz, content_sz), Image.Resampling.LANCZOS)
        icon_canvas.paste(scaled_badge, (offset, offset), scaled_badge)
        icon_canvas.save(os.path.join(folder_path, 'ic_launcher.png'), 'PNG')

        # Legacy round icon
        round_canvas = Image.new('RGBA', (sz, sz), (0, 0, 0, 0))
        scaled_round = round_final_high.resize((content_sz, content_sz), Image.Resampling.LANCZOS)
        round_canvas.paste(scaled_round, (offset, offset), scaled_round)
        round_canvas.save(os.path.join(folder_path, 'ic_launcher_round.png'), 'PNG')
        print(f'Generated {folder}: {sz}x{sz}')

    # 5. Android Adaptive Icon Foreground
    # Viewport is 108dp. Safe zone is central 66dp (diameter 66/108 = 61.1%).
    # We create a 432x432 foreground image where the badge fills the central safe zone (size 432 * 66 / 108 = 264px)
    # with smooth padding so NO system mask clips the double chrome frame!
    adaptive_size = 432
    safe_badge_size = int(adaptive_size * (68.0 / 108.0)) # ~272px
    offset_ad = (adaptive_size - safe_badge_size) // 2
    adaptive_fg = Image.new('RGBA', (adaptive_size, adaptive_size), (0, 0, 0, 0))
    scaled_badge_safe = badge_squircle_high.resize((safe_badge_size, safe_badge_size), Image.Resampling.LANCZOS)
    adaptive_fg.paste(scaled_badge_safe, (offset_ad, offset_ad), scaled_badge_safe)
    
    # Save adaptive foreground PNG in drawable
    adaptive_fg.save('app/src/main/res/drawable/ic_launcher_foreground.png', 'PNG')
    print('Generated adaptive foreground: 432x432')

    # Copy master previews to artifact directory for presentation
    artifact_dir = r'C:\Users\mszer\.gemini\antigravity\brain\b2df6288-a8bc-4574-bc72-67deea8c5f0c'
    master_512.save(os.path.join(artifact_dir, 'taktrail_icon_512.png'), 'PNG')
    master_round_512.save(os.path.join(artifact_dir, 'taktrail_icon_round_512.png'), 'PNG')
    print('Artifacts updated in brain folder')

if __name__ == '__main__':
    process_logo()
