import os
import shutil

root_dir = r"c:\Users\mukht\Desktop\Android\Yung-Music-main"
ignore_dirs = {'.git', 'build', '.gradle', '.idea', 'gradle'}

def replace_in_files():
    for root, dirs, files in os.walk(root_dir):
        dirs[:] = [d for d in dirs if d not in ignore_dirs and "build" not in root.split(os.sep)]
        for file in files:
            if not file.endswith(('.kt', '.java', '.xml', '.pro', '.kts', '.toml', '.gradle')):
                continue
            path = os.path.join(root, file)
            try:
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                if 'com.metrolist' in content:
                    new_content = content.replace('com.metrolist', 'com.gideongeng.music')
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    print(f"Updated {path}")
            except Exception as e:
                pass

def rename_directories():
    for root, dirs, files in os.walk(root_dir, topdown=False):
        for d in dirs:
            if d == 'metrolist' and os.path.basename(root) == 'com':
                old_dir = os.path.join(root, d)
                com_dir = root
                new_dir = os.path.join(com_dir, "gideongeng")
                if not os.path.exists(new_dir):
                    os.makedirs(new_dir)
                music_dir = os.path.join(new_dir, "music")
                if not os.path.exists(music_dir):
                    os.makedirs(music_dir)
                
                # move all contents of com/metrolist to com/gideongeng/music
                for item in os.listdir(old_dir):
                    s = os.path.join(old_dir, item)
                    d_dest = os.path.join(music_dir, item)
                    if not os.path.exists(d_dest):
                        shutil.move(s, d_dest)
                    else:
                        print(f"Skipping move for {s} as destination exists")
                try:
                    os.rmdir(old_dir)
                except:
                    pass
                print(f"Moved directory {old_dir} to {music_dir}")

replace_in_files()
rename_directories()
