import os

file_path = r"c:\Users\mukht\Desktop\Android\Yung-Music-main\app\src\main\kotlin\com\gideongeng\music\ui\theme\Type.kt"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Fix the broken top element
broken_text = """// Force professional system SansSerif to prevent inheritance of device cursive/handwriting fonts
// which degrade the modern visual aesthetic of Yung Music.
        fontFamily = FontFamily.SansSerif,"""

fixed_top_text = """// Force professional system SansSerif to prevent inheritance of device cursive/handwriting fonts
// which degrade the modern visual aesthetic of Yung Music."""

content = content.replace(broken_text, fixed_top_text)

# Replace all FontFamily.Default with FontFamily.SansSerif
content = content.replace("FontFamily.Default", "FontFamily.SansSerif")

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Type.kt fixed and updated.")
