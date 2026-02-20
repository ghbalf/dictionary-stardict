# StarDict Dictionary App for Android

An offline dictionary app that downloads and parses [StarDict](https://en.wikipedia.org/wiki/StarDict) dictionaries, providing fast word lookup with prefix search, fuzzy matching, history, favorites, and dark mode.

## Features

- **Download dictionaries** from ZIP, tar.gz, or tar.xz URLs
- **StarDict format support**: .ifo, .idx, .dict, .dict.dz (compressed), .syn (synonyms)
- **Fast prefix search** with debounced input and binary search on sorted index
- **Fuzzy search** fallback using Levenshtein distance
- **Multiple dictionaries** with per-dictionary enable/disable and filter chips
- **History** of looked-up words
- **Favorites** bookmarking
- **Definition rendering**: plain text, HTML, phonetic, XDXF
- **Dark/light/system theme** with Material 3 dynamic color
- **Dictionary manager**: add URLs, track download progress, edit/retry/delete failed entries
- **Offline**: all lookups work without network after download

## Tech Stack

- Kotlin + Jetpack Compose
- Material 3 with dynamic color
- Hilt dependency injection
- Room database
- DataStore Preferences
- WorkManager for downloads
- OkHttp
- dictzip-java for .dict.dz random access
- MVVM architecture

## Building

Requirements: Android SDK with API 35, JDK 17.

```bash
# Debug
./gradlew assembleDebug

# Release (requires release-keystore.jks in project root)
./gradlew assembleRelease
```

APK outputs:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

## Dictionary Sources

Add any StarDict dictionary packaged as a ZIP or tar.gz/tar.xz archive. The archive must contain at minimum `.ifo` and `.idx` files, plus `.dict` or `.dict.dz`.

Tested sources:

| Dictionary | Entries | URL |
|---|---|---|
| GCIDE (English) | 108,121 | `https://tovotu.de/data/stardict/gcide.zip` |
| Etymology (English) | 46,133 | `https://tovotu.de/data/stardict/etymonline.zip` |
| Georges Latin-German | 54,831 | `https://gitlab.com/koreader/stardict-dictionaries/uploads/6339585b68ac485bedb8ee67892cb974/georges_lat-de.tar.gz` |
| Georges German-Latin | 26,608 | `https://gitlab.com/koreader/stardict-dictionaries/uploads/a04de66c7376e436913ca288a3ca608b/georges_de-lat.tar.gz` |

More dictionaries: [FreeDict](https://freedict.org/downloads/), [WikDict](https://download.wikdict.com/dictionaries/stardict/), [KoReader list](https://github.com/koreader/koreader/blob/master/frontend/ui/data/dictionaries.lua)

## Project Structure

```
app/src/main/java/com/example/stardict/
├── StarDictApp.kt                  # Hilt application with WorkManager
├── MainActivity.kt                 # Single activity, hosts Compose NavHost
├── core/
│   ├── di/                         # Hilt modules (AppModule, RepositoryModule)
│   └── util/Resource.kt            # Loading/Success/Error sealed class
├── data/
│   ├── local/
│   │   ├── db/                     # Room database, entities, DAOs
│   │   ├── stardict/               # Binary format parsers
│   │   │   ├── IfoParser.kt        # .ifo metadata (key=value text)
│   │   │   ├── IdxParser.kt        # .idx binary index (memory-mapped)
│   │   │   ├── DictReader.kt       # .dict/.dict.dz article reader
│   │   │   ├── SynParser.kt        # .syn synonym file
│   │   │   └── StarDictIndex.kt    # In-memory index with binary search
│   │   └── preferences/            # DataStore for theme preference
│   ├── remote/DownloadService.kt   # OkHttp downloader with progress
│   └── repository/                 # Repository implementations
├── domain/
│   ├── model/                      # Domain models
│   ├── repository/                 # Repository interfaces
│   └── usecase/                    # Search, definition, download, favorite
├── ui/
│   ├── navigation/                 # Routes and NavHost
│   ├── theme/                      # Material 3 theme
│   ├── screen/                     # Search, Definition, DictManager,
│   │                               # History, Favorites, Settings
│   └── component/                  # WordListItem, DefinitionCard
└── worker/                         # WorkManager download & index workers
```

## StarDict Format

- **.ifo**: Plain text metadata. First line: `StarDict's dict ifo file`, then `key=value` pairs (`bookname`, `wordcount`, `idxfilesize`, `sametypesequence`, etc.)
- **.idx**: Binary sorted index. Each entry: null-terminated UTF-8 word + 4-byte big-endian offset + 4-byte big-endian size
- **.dict/.dict.dz**: Article data. With `sametypesequence`, type chars are implicit and the last field has no terminator. Without it, each field starts with a type byte (`m`=text, `h`=HTML, `t`=phonetic, `x`=XDXF)
- **.syn**: Optional synonyms. Each entry: null-terminated synonym + 4-byte index into .idx entry list

## Design Decisions

- **Memory-mapped .idx parsing** via `FileChannel.map()` for zero-copy reads
- **LRU cache of 3 dictionary indices** to bound memory usage
- **Random-access .dict reads** - only the requested article is read, never the full file
- **WorkManager** for downloads that survive process death
- **Debounced search (300ms)** with prefix binary search
- **minSdk 26** for `FileChannel.map` and modern APIs

## License

MIT
