from PIL import Image
import os

icon_path = r"c:/Users/mukht/Desktop/Android/Yung-Music-main/fastlane/metadata/android/en-US/images/icon.png"

def crop_icon(path):
    if not os.path.exists(path):
        print(f"File not found: {path}")
        return

    img = Image.open(path)
    img = img.convert("RGBA")
    
    # Get bounding box of non-transparent areas or non-white areas
    # In this case, the background is white, so we look for non-white pixels
    # or we can use the alpha channel if it's there.
    # The provided image has a white background.
    
    # Convert white (255, 255, 255) to transparent for easier cropping if needed,
    # but the image itself seems to have a square with rounded corners on white.
    
    # Let's find the content by looking for the 3D frame.
    # We'll crop to the edge of the gold/blue frame.
    
    datas = img.getdata()
    
    left = img.width
    top = img.height
    right = 0
    bottom = 0
    
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = img.getpixel((x, y))
            # Assume pixels that are NOT near-white are content
            if r < 240 or g < 240 or b < 240:
                left = min(left, x)
                top = min(top, y)
                right = max(right, x)
                bottom = max(bottom, y)
    
    # Add a tiny bit of padding (e.g. 10px) to not cut the edges
    padding = 20
    left = max(0, left - padding)
    top = max(0, top - padding)
    right = min(img.width, right + padding)
    bottom = min(img.height, bottom + padding)
    
    cropped = img.crop((left, top, right, bottom))
    
    # Save back as PNG
    cropped.save(path, "PNG")
    print(f"Icon cropped to {cropped.size} and saved to {path}")

if __name__ == "__main__":
    try:
        crop_icon(icon_path)
    except Exception as e:
        print(f"Error: {e}")
