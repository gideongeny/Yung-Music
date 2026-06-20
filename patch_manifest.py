import re

with open('app/src/main/AndroidManifest.xml', 'r', encoding='utf-8') as f:
    text = f.read()

wifi_perms = '''    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />'''

text = text.replace('    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />', wifi_perms)

with open('app/src/main/AndroidManifest.xml', 'w', encoding='utf-8') as f:
    f.write(text)
