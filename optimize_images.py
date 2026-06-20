import os
from PIL import Image

def optimize_images(directory):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".png") and ("wrapped_playlist" in file or "about_logo" in file or "launcher" in file):
                file_path = os.path.join(root, file)
                original_size = os.path.getsize(file_path)
                if original_size > 50000:  # Only optimize files > 50KB
                    print(f"Optimizing {file_path} ({original_size // 1024} KB)")
                    try:
                        img = Image.open(file_path)
                        # Convert to WebP
                        webp_path = file_path.replace(".png", ".webp")
                        img.save(webp_path, "WEBP", quality=85)
                        new_size = os.path.getsize(webp_path)
                        print(f"  -> Created {webp_path} ({new_size // 1024} KB)")
                        
                        # Note: We should update XMLs to point to .webp, but Android 10+ handles .webp in drawable folders fine if we rename.
                        # Wait, it's safer to just overwrite the PNG with a HEAVILY optimized PNG if possible, 
                        # or just leave the .webp and delete the .png. 
                        # But then I need to update all resource references.
                        # For now, I'll just save back as PNG with higher compression if Pillow supports it.
                        img.save(file_path, "PNG", optimize=True)
                        after_size = os.path.getsize(file_path)
                        print(f"  -> Optimized PNG size: {after_size // 1024} KB")
                    except Exception as e:
                        print(f"  Error optimizing {file}: {e}")

if __name__ == "__main__":
    res_dir = r"c:\Users\mukht\Desktop\Android\Yung-Music-main\app\src\main\res"
    # Ensure Pillow is installed: pip install Pillow
    try:
        optimize_images(res_dir)
    except ImportError:
        print("Pillow not installed. Skipping image optimization.")
