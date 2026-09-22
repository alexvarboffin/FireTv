# Player browse scope (zap / in-player channel sheet)

## Problem

The TV player can switch channels without leaving the route (side sheet + CH±).
If siblings are loaded only from `channelId` (e.g. “same playlist as this channel”),
the zap list **ignores where the user came from**:

| User opened player from | Wrong siblings (old) | Expected siblings |
|-------------------------|----------------------|-------------------|
| Favorites | entire playlist of that channel | favorites only |
| Playlist “UK” | same playlist (OK by luck) | that playlist |
| Category “News” | playlist / category guess | News category |
| Search / All channels | huge or unrelated list | none (or explicit opt-in) |
| Direct stream URL | N/A | none |

Passing only `channelId` into `Routes.Player` loses browse context.

## Solution

Explicit **browse scope** travels with navigation:

```text
PlayerBrowseScope =
  Favorites
| Playlist(playlistId)
| Category(name)
| All          // opt-in; usually avoided (library size)
| None         // no sheet / CH±
| InferPlaylist // legacy fallback via playlist_channel_join
```

### Wire format

Query args on Player (and Details, so Play keeps scope):

- `scope` — `favorites` | `playlist` | `category` | `all` | `none` | `infer`
- `scopeKey` — playlist id or category name (when needed)

Example:

`player/42?scope=favorites&scopeKey=`  
`player/42?scope=playlist&scopeKey=7`  
`player/42?scope=category&scopeKey=News`

### Call sites

| Entry | Scope |
|-------|--------|
| Favorites | `Favorites` |
| Playlist channels / Xtream live | `Playlist(id)` |
| Category list | `Category(name)` |
| All channels | `None` |
| Search | `None` |
| Direct stream | `None` |
| Unknown / old links | `InferPlaylist` |

### Layers

| Layer | Type |
|-------|------|
| `app-compose` navigation | `PlayerBrowseScope` |
| `:core:presentation` VM | `PlayerBrowseScopeWire` (no app dependency) |
| Mapping | `PlayerBrowseScope.toWire()` |

`PlayerViewModel` loads `siblings` **only** from the wire scope.
Sheet + CH± operate on that list; category headers are a secondary grouping inside the scope.

## Files

- `navigation/PlayerBrowseScope.kt`
- `navigation/PlayerBrowseScopeMapping.kt`
- `navigation/Routes.kt` / `ChannelNavigation.kt`
- `core/.../player/PlayerViewModel.kt`
- TV/mobile Favorites, Playlist, Category, All, Search, Details, Player
- `TvNavHost` / `PhoneNavHost` — `navArgument` defaults for `scope` / `scopeKey`
