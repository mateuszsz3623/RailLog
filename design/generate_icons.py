import os
import math
from PIL import Image, ImageDraw

def render_icon(size, is_round=False):
    scale = 8  # 8x supersampling for extreme antialiasing precision
    canvas_size = size * scale
    img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    def sx(val):
        return val * canvas_size / 108.0

    def sy(val):
        return val * canvas_size / 108.0

    center_x = canvas_size / 2.0
    center_y = canvas_size / 2.0

    # 1. Background
    if is_round:
        radius = canvas_size * 0.485
        draw.ellipse([center_x - radius, center_y - radius, center_x + radius, center_y + radius], fill="#0A0E14")
        draw.ellipse([center_x - radius, center_y - radius, center_x + radius, center_y + radius], outline="#263242", width=int(scale * 1.5))
    else:
        corner_r = canvas_size * 0.22
        draw.rounded_rectangle([0, 0, canvas_size, canvas_size], radius=corner_r, fill="#0A0E14")
        draw.rounded_rectangle([0, 0, canvas_size, canvas_size], radius=corner_r, outline="#263242", width=int(scale * 1.5))

    # Subtle technical railway guide tracks in background
    bg_track_color = "#151C28"
    grid_width = int(scale * 1.2)
    draw.line([(sx(36), 0), (sx(36), canvas_size)], fill=bg_track_color, width=grid_width)
    draw.line([(sx(72), 0), (sx(72), canvas_size)], fill=bg_track_color, width=grid_width)
    draw.line([(0, sy(28)), (canvas_size, sy(28))], fill=bg_track_color, width=grid_width)
    draw.line([(0, sy(80)), (canvas_size, sy(80))], fill=bg_track_color, width=grid_width)

    # 2. Cross-ties (Track Sleepers / Time Rhythms)
    tie_color = "#475569"
    tie_r = scale * 1.5
    # Upper tie (y=53.5 to 57.5)
    draw.rounded_rectangle([sx(41), sy(53.5), sx(67), sy(57.5)], radius=tie_r, fill=tie_color)
    # Lower tie (y=66.5 to 70.5)
    draw.rounded_rectangle([sx(41), sy(66.5), sx(67), sy(70.5)], radius=tie_r, fill=tie_color)

    # 3. Parallel Vertical Rails (Left & Right)
    rail_color = "#F8FAFC"
    rail_r = scale * 2.0
    # Left rail: x=45.5 to 51.5, y=38 to 76
    draw.rounded_rectangle([sx(45.5), sy(38), sx(51.5), sy(76)], radius=rail_r, fill=rail_color)
    # Right rail: x=56.5 to 62.5, y=38 to 76
    draw.rounded_rectangle([sx(56.5), sy(38), sx(62.5), sy(76)], radius=rail_r, fill=rail_color)

    # 4. Horizontal T-Head Wings (Left & Right)
    # Left wing: x=31 to 51.5, y=32 to 39
    draw.rounded_rectangle([sx(31), sy(32), sx(51.5), sy(39)], radius=scale * 3.5, fill=rail_color)
    # Right wing: x=56.5 to 77, y=32 to 39
    draw.rounded_rectangle([sx(56.5), sy(32), sx(77), sy(39)], radius=scale * 3.5, fill=rail_color)

    # 5. Central Griffin Red Railway Accent (Dynamic Takt Pulse)
    accent_color = "#E63946"
    accent_points = [
        (sx(54), sy(27)),    # Top point (apex)
        (sx(57), sy(33)),    # Top-right
        (sx(57), sy(43)),    # Bottom-right
        (sx(54), sy(48)),    # Bottom point (pointing along tracks)
        (sx(51), sy(43)),    # Bottom-left
        (sx(51), sy(33)),    # Top-left
    ]
    draw.polygon(accent_points, fill=accent_color)

    # 6. Illuminated Precision Core Dot
    dot_r = scale * 1.5
    draw.ellipse([sx(54) - dot_r, sy(35.5) - dot_r, sx(54) + dot_r, sy(35.5) + dot_r], fill="#FF8A95")

    # Downsample
    final_img = img.resize((size, size), Image.Resampling.LANCZOS)
    return final_img

def generate_svg():
    svg_content = '''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="512" height="512">
    <!-- TaktRail 2.0 Brand Icon Master Source -->
    <defs>
        <radialGradient id="bgGlow" cx="50%" cy="40%" r="60%">
            <stop offset="0%" stop-color="#161F2E" />
            <stop offset="100%" stop-color="#0A0E14" />
        </radialGradient>
        <filter id="subtleShadow" x="-10%" y="-10%" width="120%" height="120%">
            <feDropShadow dx="0" dy="2" stdDeviation="3" flood-color="#000000" flood-opacity="0.6"/>
        </filter>
    </defs>

    <!-- Background -->
    <rect width="108" height="108" rx="24" fill="url(#bgGlow)" stroke="#263242" stroke-width="1.5"/>

    <!-- Subtle Technical Grid Rails -->
    <g stroke="#1A2230" stroke-width="1">
        <line x1="36" y1="0" x2="36" y2="108" />
        <line x1="72" y1="0" x2="72" y2="108" />
        <line x1="0" y1="28" x2="108" y2="28" />
        <line x1="0" y1="80" x2="108" y2="80" />
    </g>

    <g filter="url(#subtleShadow)">
        <!-- 1. Cross-ties (Track Sleepers / Rhythmic Time Intervals) -->
        <rect x="41" y="53.5" width="26" height="4" rx="2" fill="#475569" />
        <rect x="41" y="66.5" width="26" height="4" rx="2" fill="#475569" />

        <!-- 2. Parallel Vertical Rails (Left & Right) -->
        <rect x="45.5" y="38" width="6" height="38" rx="2.5" fill="#F8FAFC" />
        <rect x="56.5" y="38" width="6" height="38" rx="2.5" fill="#F8FAFC" />

        <!-- 3. Horizontal T-Head Wings (Left & Right) -->
        <rect x="31" y="32" width="20.5" height="7" rx="3.5" fill="#F8FAFC" />
        <rect x="56.5" y="32" width="20.5" height="7" rx="3.5" fill="#F8FAFC" />

        <!-- 4. Central Griffin Red Railway Accent (Dynamic Takt Pulse) -->
        <polygon points="54,27 57,33 57,43 54,48 51,43 51,33" fill="#E63946" />

        <!-- 5. Illuminated Precision Core Dot -->
        <circle cx="54" cy="35.5" r="1.5" fill="#FF8A95" />
    </g>
</svg>'''
    return svg_content

def main():
    base_res = "app/src/main/res"
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    # Render legacy raster mipmaps
    for folder, size in densities.items():
        folder_path = os.path.join(base_res, folder)
        os.makedirs(folder_path, exist_ok=True)
        
        # Standard icon
        icon = render_icon(size, is_round=False)
        icon.save(os.path.join(folder_path, "ic_launcher.png"), "PNG")
        
        # Round icon
        round_icon = render_icon(size, is_round=True)
        round_icon.save(os.path.join(folder_path, "ic_launcher_round.png"), "PNG")
        print(f"Generated {folder}: {size}x{size}")

    # Generate 512x512 Master in design/
    os.makedirs("design", exist_ok=True)
    master_512 = render_icon(512, is_round=False)
    master_512.save("design/taktrail_icon_512.png", "PNG")
    
    master_round_512 = render_icon(512, is_round=True)
    master_round_512.save("design/taktrail_icon_round_512.png", "PNG")

    # Save master SVG
    svg_data = generate_svg()
    with open("design/taktrail_icon_master.svg", "w", encoding="utf-8") as f:
        f.write(svg_data)
        
    print("Master assets saved in design/")

if __name__ == "__main__":
    main()
