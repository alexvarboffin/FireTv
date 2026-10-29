# Ultimate.TV: IPTV Player — release listing draft (tracks/subtitles)
APPROVED 2026-10-29 by user (texts as drafted). NOT published yet. Fetched live 2026-10-29 (Play en-US = only locale shown, hl=ru serves the same English text; RuStore ru).
Additive only: title, short description and all existing text/keywords unchanged.

## Google Play en-US — full description (INSERT)
Position: new feature line right after "Custom Player Replacement: ... personalized viewing experience. 🎞️" and before "Ultimate HD TV Streaming offers ...".

```diff
  🔎 Custom Player Replacement: Replace the standard video player with your preferred option for a personalized viewing experience. 🎞️
+ Audio Tracks & Subtitles: Choose the audio track and subtitles right in the player on multi-audio streams. Switch the audio language, pick a subtitle language or turn subtitles off, on phones, tablets and Android TV. 🔊💬
  Ultimate HD TV Streaming offers a comprehensive TV and movie experience right at your fingertips. ...
```
Insert: 219 chars. Live full ~3290 chars (web-flattened; Console count slightly higher with line breaks) -> ~3510 after insert, under 4000.
Keywords added: audio track, subtitles, multi-audio, audio language, Android TV.

## RuStore (ru) — full description (INSERT, no emoji, no brand name in insert; if the name is ever written in ru text use "Ultimate-TV")
Position: after "Перемотка: возвращайтесь к нужному моменту потока." and before "Совместимость: ...".

```diff
  Перемотка: возвращайтесь к нужному моменту потока.
+ Аудиодорожки и субтитры: выбирайте звуковую дорожку и субтитры прямо в плеере, если в потоке их несколько. Переключайте язык звука, включайте субтитры на нужном языке или отключайте их на телефоне, планшете и Android TV.
  Совместимость: Android 7.0 и выше, ...
```
Insert: 220 chars. §6.4 check: no emoji/decorative symbols, no superlatives, no third-party brands, feature present in the build.

## What's new
en (266/500):
New: choose the audio track and subtitles in the player (multi-audio streams, subtitle language or off). Android TV: new settings panel (Menu button) with player, audio and subtitles; key hints on screen; Left opens channels and TV guide. Bug fixes and improvements.

ru (285/500, RuStore-safe, no emoji):
Новое: выбор аудиодорожки и субтитров в плеере (несколько звуковых дорожек, язык субтитров или выключить). Android TV: новая панель настроек (кнопка Menu) с выбором плеера, звука и субтитров, подсказки по кнопкам пульта, влево открывает каналы и телепрограмму. Исправления и улучшения.

## Notes (do not publish)
- Track switching is implemented via the Exo-based tracks controller; confirm it also works on the other engines before claiming "all players" (the draft does not claim that).
- Play live text contains piracy-flavoured claims ("10,000+ channels", "live sports") — unchanged here on purpose (ASO rule), but it is a policy risk worth a separate decision.
