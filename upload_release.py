import requests
import json
import os
import sys

# ── Config ──────────────────────────────────────────────────────────────────
OWNER = "gideongeny"
REPO  = "Yung-Music"
TAG   = "v13.0.0-foss"
TITLE = "v13.0.0 – FOSS Builds (IzzyOnDroid)"
BODY  = """## YungMusic v13.0.0 – FOSS Builds

These builds were compiled specifically for open-source repositories like **IzzyOnDroid**.

| Variant | Description | Size |
|---------|-------------|------|
| **FOSS** | No Ads · No Firebase · No proprietary libs | ~8.8 MB |
| **GMS**  | Full features with Cast, Ads (Google services) | ~25 MB |

> ⚠️ The full **Play Store** release (41 MB) is separate and available on the [main v13.0.0 release](https://github.com/gideongeny/Yung-Music/releases).

### FOSS Compliance Changes
- ✅ AdMob & Firebase removed from FOSS flavor
- ✅ kuromoji-ipadic (large Japanese NLP binary) removed from FOSS flavor
- ✅ PNG assets optimized to WebP
- ✅ Signed with a proper release key (not debug)
"""

FOSS_APK = r"app\build\outputs\apk\universalFoss\release\app-universal-foss-release.apk"
GMS_APK  = r"app\build\outputs\apk\universalGms\release\app-universal-gms-release.apk"
GMS_AAB  = r"app\build\outputs\bundle\universalGmsRelease\app-universal-gms-release.aab"

# ── Token ────────────────────────────────────────────────────────────────────
TOKEN = os.environ.get("GITHUB_TOKEN", "")
if not TOKEN:
    print("ERROR: GITHUB_TOKEN environment variable not set.")
    print("Please run:  $env:GITHUB_TOKEN = 'your_token_here'  then re-run this script.")
    sys.exit(1)

HEADERS = {
    "Authorization": f"token {TOKEN}",
    "Accept": "application/vnd.github+json",
}

# ── Step 1 – Check if release already exists ─────────────────────────────────
print(f"Checking for existing release {TAG}...")
r = requests.get(f"https://api.github.com/repos/{OWNER}/{REPO}/releases/tags/{TAG}", headers=HEADERS)

if r.status_code == 200:
    release = r.json()
    release_id = release["id"]
    upload_url = release["upload_url"].split("{")[0]
    print(f"Release already exists (id={release_id}). Will add assets to it.")
else:
    # ── Step 2 – Create the release ──────────────────────────────────────────
    print("Creating new release...")
    payload = {
        "tag_name": TAG,
        "name": TITLE,
        "body": BODY,
        "draft": False,
        "prerelease": False,
    }
    r = requests.post(
        f"https://api.github.com/repos/{OWNER}/{REPO}/releases",
        headers=HEADERS,
        json=payload,
    )
    if r.status_code not in (200, 201):
        print(f"Failed to create release: {r.status_code} {r.text}")
        sys.exit(1)
    release = r.json()
    release_id = release["id"]
    upload_url = release["upload_url"].split("{")[0]
    print(f"Release created: {release['html_url']}")

# ── Step 3 – Upload APKs ──────────────────────────────────────────────────────
def upload_asset(path, name):
    print(f"Uploading {name} ({os.path.getsize(path) // 1024 // 1024} MB)...")
    with open(path, "rb") as f:
        data = f.read()
    r = requests.post(
        f"{upload_url}?name={name}",
        headers={**HEADERS, "Content-Type": "application/vnd.android.package-archive"},
        data=data,
    )
    if r.status_code in (200, 201):
        print(f"  ✅ Uploaded: {r.json()['browser_download_url']}")
    else:
        print(f"  ❌ Failed ({r.status_code}): {r.text[:300]}")

upload_asset(FOSS_APK, "app-universal-foss-release.apk")
upload_asset(GMS_APK,  "app-universal-gms-release.apk")
upload_asset(GMS_AAB,  "app-universal-gms-release.aab")

print(f"\nDone! View release at: https://github.com/{OWNER}/{REPO}/releases/tag/{TAG}")
