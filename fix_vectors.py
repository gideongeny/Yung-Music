import os

drawable_dir = r"c:\Users\mukht\Desktop\Android\Yung-Music-main\app\src\main\res\drawable"

def wrap_path(path_data):
    return f'''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24.0"
    android:viewportHeight="24.0"
    android:tint="?attr/colorControlNormal">
  <path
      android:fillColor="@android:color/white"
      android:pathData="{path_data}"/>
</vector>'''

icons = {
    # Fix the parsing glitch by using spaces and commas rigorously
    "skip_previous.xml": "M6,6 h2 v12 H6 V6 z M9.5,12 L18,18 V6 L9.5,12 z",
    "ic_skip_previous.xml": "M6,6 h2 v12 H6 V6 z M9.5,12 L18,18 V6 L9.5,12 z",
    "ic_widget_skip_previous.xml": "M6,6 h2 v12 H6 V6 z M9.5,12 L18,18 V6 L9.5,12 z",
    
    "skip_next.xml": "M6,18 L14.5,12 L6,6 V18 z M16,6 V18 h2 V6 H16 z",
    "ic_skip_next.xml": "M6,18 L14.5,12 L6,6 V18 z M16,6 V18 h2 V6 H16 z",
    "ic_widget_skip_next.xml": "M6,18 L14.5,12 L6,6 V18 z M16,6 V18 h2 V6 H16 z",
    
    # Bottom Row Icons the user highlighted
    "lyrics.xml": "M21,3 H3 C1.9,3 1,3.9 1,5 V19 C1,20.1 1.9,21 3,21 H21 C22.1,21 23,20.1 23,19 V5 C23,3.9 22.1,3 21,3 Z M21,19 H3 V5 H21 V19 Z M7,15 H17 V17 H7 V15 Z M7,11 H17 V13 H7 V11 Z M7,7 H17 V9 H7 V7 Z",
    
    "bedtime.xml": "M12.29,3.03 C11.39,2.84 10.45,2.75 9.5,2.75 C4.25,2.75 0.15,6.85 0.15,12.1 C0.15,17.35 4.25,21.45 9.5,21.45 C13.78,21.45 17.47,18.66 18.55,14.77 C14.65,15.5 10.63,12.83 9.42,8.96 C8.62,6.38 9.31,4.02 12.29,3.03 Z",
    
    "fullscreen.xml": "M21,11 V3 h-8 l3.29,3.29 l-10,10 L3,13 v8 h8 l-3.29,-3.29 l10,-10 z",
    "shortcut_explore.xml": "M21,11 V3 h-8 l3.29,3.29 l-10,10 L3,13 v8 h8 l-3.29,-3.29 l10,-10 z",
    
    "tune.xml": "M3,17 v2 h6 v-2 H3 z M3,5 v2 h10 V5 H3 z M13,21 v-2 h8 v-2 h-8 v-2 h-2 v6 H13 z M7,9 v2 H3 v2 h4 v2 h2 V9 H7 z M21,13 v-2 H11 v2 H21 z M15,9 h2 V7 h4 V5 h-4 V3 h-2 V9 z",
    "sliders.xml": "M3,17 v2 h6 v-2 H3 z M3,5 v2 h10 V5 H3 z M13,21 v-2 h8 v-2 h-8 v-2 h-2 v6 H13 z M7,9 v2 H3 v2 h4 v2 h2 V9 H7 z M21,13 v-2 H11 v2 H21 z M15,9 h2 V7 h4 V5 h-4 V3 h-2 V9 z",
    "equalizer.xml": "M3,17 v2 h6 v-2 H3 z M3,5 v2 h10 V5 H3 z M13,21 v-2 h8 v-2 h-8 v-2 h-2 v6 H13 z M7,9 v2 H3 v2 h4 v2 h2 V9 H7 z M21,13 v-2 H11 v2 H21 z M15,9 h2 V7 h4 V5 h-4 V3 h-2 V9 z",
    
    "sync.xml": "M12,4 V1 L8,5 l4,4 V6 c3.31,0 6,2.69 6,6 c0,1.01 -0.25,1.97 -0.7,2.8 l1.46,1.46 C19.54,15.03 20,13.57 20,12 C20,7.58 16.42,4 12,4 z M12,18 c-3.31,0 -6,-2.69 -6,-6 c0,-1.01 0.25,-1.97 0.7,-2.8 L5.24,7.74 C4.46,8.97 4,10.43 4,12 c0,4.42 3.58,8 8,8 v3 l4,-4 l-4,-4 V18 z",
    "repeat.xml": "M12,4 V1 L8,5 l4,4 V6 c3.31,0 6,2.69 6,6 c0,1.01 -0.25,1.97 -0.7,2.8 l1.46,1.46 C19.54,15.03 20,13.57 20,12 C20,7.58 16.42,4 12,4 z M12,18 c-3.31,0 -6,-2.69 -6,-6 c0,-1.01 0.25,-1.97 0.7,-2.8 L5.24,7.74 C4.46,8.97 4,10.43 4,12 c0,4.42 3.58,8 8,8 v3 l4,-4 l-4,-4 V18 z"
}

for icon_name, path in icons.items():
    file_path = os.path.join(drawable_dir, icon_name)
    if os.path.exists(file_path):
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(wrap_path(path))
        print(f"Overwrote {icon_name}")
