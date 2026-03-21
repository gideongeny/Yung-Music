import json
import os
import urllib.request

GITHUB_TOKEN = os.environ.get('GITHUB_TOKEN')
REPO = 'gideongeny/Yung-Music'
TAG = 'v13.0.0'
NAME = 'YungMusic v13.0.0'
BODY = '''### 🔥 Massive Playback & UX Update

*   **Fixed `IO_UNSPECIFIED (2000)` Error:** Completely overhauled the YouTube stream fetching logic. The app now bypasses NewPipe decryption requirements entirely by using direct, pre-signed stream URLs. Online playback is now lightning fast and ultra-reliable.
*   **Deep Visual Redesign:** Switched to a gorgeous deep-purple material theme. Rewrote all legacy metrolist icons with pure, beautiful Material 3 vectors.
*   **Typography Overhaul:** Enforced a unified `Sang-Serif` custom font across the entire application for a premium, polished look.
*   **Ads on Startup:** Integrated commercial monetization successfully without disrupting the core music experience.
*   **Bulletproof Updates:** Added a direct prominent download page to the internal About screen.

Thanks to everyone supporting YungMusic! 💜'''

def create_release():
    url = f'https://api.github.com/repos/{REPO}/releases'
    headers = {
        'Accept': 'application/vnd.github+json',
        'Authorization': f'Bearer {GITHUB_TOKEN}',
        'X-GitHub-Api-Version': '2022-11-28',
        'Content-Type': 'application/json'
    }
    data = json.dumps({
        'tag_name': TAG,
        'target_commitish': 'main',
        'name': NAME,
        'body': BODY,
        'draft': False,
        'prerelease': False,
        'generate_release_notes': False
    }).encode('utf-8')
    req = urllib.request.Request(url, data=data, headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            result = json.loads(response.read().decode())
            print(f"Created release: {result['id']}")
            return result['id']
    except Exception as e:
        print(f"Error creating release: {e}")
        return None

def upload_asset(release_id, file_path, name, content_type):
    url = f"https://uploads.github.com/repos/{REPO}/releases/{release_id}/assets?name={name}"
    headers = {
        'Accept': 'application/vnd.github+json',
        'Authorization': f'Bearer {GITHUB_TOKEN}',
        'X-GitHub-Api-Version': '2022-11-28',
        'Content-Type': content_type
    }
    with open(file_path, 'rb') as f:
        data = f.read()
    req = urllib.request.Request(url, data=data, headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            print(f"Uploaded {name}")
    except Exception as e:
        print(f"Error uploading {name}: {e}")

if not GITHUB_TOKEN:
    print("Error: GITHUB_TOKEN environment variable is missing.")
    # Exit cleanly if token is missing so we can ask the user
    exit(0)

release_id = create_release()
if release_id:
    upload_asset(release_id, r"c:\Users\mukht\Desktop\Android\Yung-Music-main\app\build\outputs\apk\universalFoss\release\app-universal-foss-release.apk", "YungMusic.apk", "application/vnd.android.package-archive")
    upload_asset(release_id, r"c:\Users\mukht\Desktop\Android\Yung-Music-main\app\build\outputs\bundle\universalFossRelease\app-universal-foss-release.aab", "YungMusic.aab", "application/octet-stream")
    print("Done!")
