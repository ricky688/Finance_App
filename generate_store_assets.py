import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT_DIR = "/home/ricky/Antigravity_project/finance_app/store_assets"
os.makedirs(OUTPUT_DIR, exist_ok=True)

FONT_TC_BOLD = "/usr/share/fonts/adobe-source-han-sans/SourceHanSans.ttc"
FONT_TC_MED = "/usr/share/fonts/adobe-source-han-sans/SourceHanSans.ttc"
FONT_EN_BOLD = "/usr/share/fonts/dejavu-sans-fonts/DejaVuSans-Bold.ttf"
FONT_EN_REG = "/usr/share/fonts/dejavu-sans-fonts/DejaVuSans.ttf"

# TTC Indices: 39 = HK Bold, 19 = HK Medium
INDEX_BOLD = 39
INDEX_MED = 19

# ==========================================
# 1. Refine Feature Graphic (1024 x 500)
# ==========================================
def create_feature_graphic():
    os.system("resvg -w 1024 -h 500 /home/ricky/Antigravity_project/finance_app/feature_graphic.svg /tmp/fg_base.png")
    img = Image.open("/tmp/fg_base.png").convert("RGBA")
    draw = ImageDraw.Draw(img)

    f_badge = ImageFont.truetype(FONT_TC_BOLD, 14, index=INDEX_BOLD)
    f_title = ImageFont.truetype(FONT_EN_BOLD, 62)
    f_sub = ImageFont.truetype(FONT_TC_MED, 21, index=INDEX_MED)
    f_pill = ImageFont.truetype(FONT_TC_BOLD, 15, index=INDEX_BOLD)
    f_tagline = ImageFont.truetype(FONT_EN_REG, 14)
    f_card_sub = ImageFont.truetype(FONT_EN_BOLD, 12)
    f_card_sm = ImageFont.truetype(FONT_EN_REG, 11)
    f_stat_l = ImageFont.truetype(FONT_EN_BOLD, 13)
    f_stat_r = ImageFont.truetype(FONT_EN_BOLD, 13)

    # Privacy Badge
    draw.text((115, 95), "100% 離線優先・極致隱私", font=f_badge, fill="#6EE7B7")

    # Title
    draw.text((75, 145), "VibeFinance", font=f_title, fill="#FFFFFF")

    # Subtitle
    draw.text((75, 230), "智能記帳・消費節奏熱力圖・八達通即時自動記錄", font=f_sub, fill="#E2E8F0")

    # Feature Pills (Clean text without emoji font clash)
    draw.text((105, 301), "即時通知記帳", font=f_pill, fill="#F1F5F9")
    draw.text((270, 301), "16週消費節奏", font=f_pill, fill="#F1F5F9")
    draw.text((435, 301), "每日動態預算", font=f_pill, fill="#F1F5F9")

    # English Tagline
    draw.text((75, 375), "Offline-first personal finance tracker with Material 3 Expressive motion", font=f_tagline, fill="#94A3B8")

    # Right Card Text
    draw.text((760, 100), "FINANCIAL RHYTHM", font=f_card_sub, fill="#94A3B8")
    draw.text((760, 118), "Active 16-Week Matrix", font=f_card_sm, fill="#64748B")

    draw.text((695, 375), "Smart Octopus", font=f_stat_l, fill="#34D399")
    draw.text((815, 375), "+ Auto-Logged", font=f_stat_r, fill="#F1F5F9")

    out_path = os.path.join(OUTPUT_DIR, "feature_graphic_1024x500.png")
    img.save(out_path, "PNG")
    print(f"Generated: {out_path}")


# ==========================================
# 2. Phone Screenshots (1080 x 1920, 9:16)
# ==========================================
def create_phone_screenshot(index, tag, title, subtitle, source_img_path):
    # Canvas: 1080 x 1920 (exactly 9:16)
    canvas = Image.new("RGBA", (1080, 1920), (10, 16, 29, 255)) # #0A101D
    draw = ImageDraw.Draw(canvas)

    # Ambient Top Emerald Glow
    glow = Image.new("RGBA", (1080, 700), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    gdraw.ellipse((-100, -200, 1180, 600), fill=(16, 185, 129, 50)) # Emerald ambient glow
    glow = glow.filter(ImageFilter.GaussianBlur(120))
    canvas.paste(glow, (0, 0), glow)

    # Subtle background gradient to bottom
    bottom_glow = Image.new("RGBA", (1080, 600), (0, 0, 0, 0))
    bgdraw = ImageDraw.Draw(bottom_glow)
    bgdraw.ellipse((100, 100, 980, 800), fill=(6, 182, 212, 25)) # Cyan accent
    bottom_glow = bottom_glow.filter(ImageFilter.GaussianBlur(150))
    canvas.paste(bottom_glow, (0, 1320), bottom_glow)

    # Fonts: bold and punchy
    f_tag = ImageFont.truetype(FONT_TC_BOLD, 26, index=INDEX_BOLD)
    f_title = ImageFont.truetype(FONT_TC_BOLD, 62, index=INDEX_BOLD)
    f_sub = ImageFont.truetype(FONT_TC_MED, 30, index=INDEX_MED)

    # 1. Category Tag Pill
    tag_w = draw.textlength(tag, font=f_tag) + 60
    tag_h = 56
    tag_x = (1080 - tag_w) / 2
    tag_y = 110
    draw.rounded_rectangle([tag_x, tag_y, tag_x + tag_w, tag_y + tag_h], radius=28, fill=(30, 41, 59, 200), outline=(51, 65, 85, 255), width=2)
    draw.text((tag_x + 30, tag_y + 11), tag, font=f_tag, fill=(110, 231, 183, 255))

    # 2. Main Title (Centered)
    title_w = draw.textlength(title, font=f_title)
    draw.text(((1080 - title_w) / 2, 195), title, font=f_title, fill=(255, 255, 255, 255))

    # 3. Subtitle (Centered)
    sub_w = draw.textlength(subtitle, font=f_sub)
    draw.text(((1080 - sub_w) / 2, 285), subtitle, font=f_sub, fill=(148, 163, 184, 255))

    # 4. Device Mockup Frame
    # Inside phone mockup dimensions: width = 800, height = 1500 (slight perspective/bleed at bottom)
    phone_w = 780
    phone_h = 1520
    phone_x = (1080 - phone_w) // 2
    phone_y = 380

    # Phone drop shadow
    shadow = Image.new("RGBA", (phone_w + 60, phone_h + 60), (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(shadow)
    sdraw.rounded_rectangle([20, 20, phone_w + 40, phone_h + 40], radius=56, fill=(0, 0, 0, 160))
    shadow = shadow.filter(ImageFilter.GaussianBlur(30))
    canvas.paste(shadow, (phone_x - 30, phone_y - 20), shadow)

    # Phone Outer Bezel
    draw.rounded_rectangle([phone_x, phone_y, phone_x + phone_w, phone_y + phone_h], radius=52, fill=(15, 23, 42, 255), outline=(51, 65, 85, 255), width=4)

    # Inner Screen Area
    screen_margin = 14
    inner_w = phone_w - screen_margin * 2
    inner_h = phone_h - screen_margin * 2
    inner_x = phone_x + screen_margin
    inner_y = phone_y + screen_margin

    # Load and scale screen capture
    src_img = Image.open(source_img_path).convert("RGBA")
    # Scale keeping aspect ratio to fill inner_w
    scale = inner_w / src_img.width
    scaled_h = int(src_img.height * scale)
    scaled_img = src_img.resize((inner_w, scaled_h), Image.Resampling.LANCZOS)

    # Crop to inner_h
    cropped_img = scaled_img.crop((0, 0, inner_w, inner_h))

    # Rounded mask for screen content
    mask = Image.new("L", (inner_w, inner_h), 0)
    mdraw = ImageDraw.Draw(mask)
    mdraw.rounded_rectangle([0, 0, inner_w, inner_h], radius=40, fill=255)

    canvas.paste(cropped_img, (inner_x, inner_y), mask)

    # Save final phone screenshot
    out_filename = f"screenshot_0{index}_{tag.split()[0]}.png"
    out_path = os.path.join(OUTPUT_DIR, out_filename)
    canvas.save(out_path, "PNG")
    print(f"Generated: {out_path} ({canvas.width}x{canvas.height})")


screenshots_data = [
    (
        1,
        "消費節奏",
        "消費節奏熱力矩陣",
        "16 週點陣視覺化・隨時掌握每日支出節奏",
        "/home/ricky/.gemini/antigravity/brain/6bf6d744-beca-45e1-8ae3-9166be8b8930/waydroid_teamwork_heatmap_centered.png"
    ),
    (
        2,
        "智慧記帳",
        "支付通知即時自動記帳",
        "八達通・信用卡・銀行推播免手動輸入",
        "/home/ricky/.gemini/antigravity/brain/6bf6d744-beca-45e1-8ae3-9166be8b8930/screen_mtr_logged.png"
    ),
    (
        3,
        "資產管理",
        "全方位多帳戶與資產總覽",
        "現金・銀行・信用卡・八達通餘額動態更新",
        "/home/ricky/.gemini/antigravity/brain/6bf6d744-beca-45e1-8ae3-9166be8b8930/screen_assets.png"
    ),
    (
        4,
        "固定支出",
        "週期訂閱與分期管理",
        "即時預警扣款排程・每月開銷一目了然",
        "/home/ricky/.gemini/antigravity/brain/6bf6d744-beca-45e1-8ae3-9166be8b8930/waydroid_teamwork_recurring_items.png"
    ),
    (
        5,
        "極致隱私",
        "100% 離線優先架構",
        "私有本機資料庫・無雲端伺服器・零廣告追蹤",
        "/home/ricky/.gemini/antigravity/brain/6bf6d744-beca-45e1-8ae3-9166be8b8930/screen_history_final.png"
    )
]

create_feature_graphic()
for item in screenshots_data:
    create_phone_screenshot(item[0], item[1], item[2], item[3], item[4])

print("All store assets successfully created in", OUTPUT_DIR)
