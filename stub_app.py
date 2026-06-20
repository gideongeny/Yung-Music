import re

with open('app/src/main/kotlin/com/gideongeng/music/App.kt', 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('import com.gideongeng.music.lastfm.LastFM\\n', '')
text = re.sub(r'LastFM\\.initialize\\([\\s\\S]*?\\)', '', text)
text = re.sub(r'LastFM\\.sessionKey.*?\\n', '\\n', text)

with open('app/src/main/kotlin/com/gideongeng/music/App.kt', 'w', encoding='utf-8') as f:
    f.write(text)
