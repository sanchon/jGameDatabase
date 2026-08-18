# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Run the application (dev mode with hot reload)
./gradlew bootRun

# Build JAR
./gradlew build

# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "xyz.sanchon.jgamedatabase.JGameDatabaseApplicationTests"

# Clean build
./gradlew clean build
```

**Docker:**
```bash
docker compose -f docker/docker-compose.yml up -d
```

Application runs on **http://localhost:8080**. H2 console at **http://localhost:8080/h2-console** (toggled at runtime via `/configuration`).

## Architecture Overview

This is a **Spring Boot 3.2.3 / Java 21** personal game collection manager using Thymeleaf (server-side rendering), H2 file-based database, and Spring Data JPA.

### Layer Structure

```
xyz.sanchon.jgamedatabase
├── (root)         # JGameDatabaseApplication, StartupListener, PortableTrayManager (profile "portable")
├── controller/    # MVC controllers (HomeController, GameController, ConfigurationController,
│                  # ShutdownController [portable], PortableModelAdvice [portable, @ControllerAdvice])
├── model/         # JPA entities (Game, Genre, Platform, GameStatus, Store, AppConfiguration)
├── repository/    # Spring Data JPA repositories (incl. GameStatusRepository, StoreRepository)
├── service/       # Business logic + external API clients (incl. BackupService, BatchImportService)
├── config/        # H2ConsoleAccessFilter — runtime gate for /h2-console/*; WebMvcConfig
├── dto/           # API response DTOs (IGDB, GG.deals, Steam) + BatchGameEntry
└── bootstrap/     # DataInitializer (@Order 1) → StatusMigrationService (@Order 2) → StoreSeedService (@Order 3)
```

### External API Integrations

All credentials are stored in the `app_configuration` DB table and managed via `/configuration` UI. They can also be set via environment variables or `application-local.properties`.

| Service | Purpose | Auth | Service Class |
|---------|---------|------|---------------|
| **IGDB** (Twitch) | Game search & metadata | OAuth2 Client Credentials | `IgdbService` |
| **GG.deals** | Game prices by Steam App ID (batch, max 100) | API Key | `GgDealsService` |
| **Steam Store** | Steam App ID lookup for wishlist | Public | `SteamStoreSearchService` |

**Credential precedence:** DB (`app_configuration` table) → `application-local.properties` (git-ignored) → environment variables (`IGDB_CLIENT_ID`, `IGDB_CLIENT_SECRET`, `GGDEALS_API_KEY`, `GGDEALS_REGION`).

### Database

H2 file-based database:
- **Dev:** `./data/jgamedatabase` — H2 console enabled at startup
- **Prod (Docker):** `/app/data/jgamedatabase` — Spring profile `prod`; H2 console servlet is enabled but access is gated by `H2ConsoleAccessFilter` which checks a DB flag toggled from `/configuration`

Schema is auto-managed via `spring.jpa.hibernate.ddl-auto=update` (adds columns, never drops — safe for production upgrades).

**Backup paths** (`app.backup.dir` property):
- Dev: `./backups`
- Portable: `${user.home}/.jgamedatabase/backups`
- Docker: `/app/backups` (mounted volume `../backups:/app/backups` in docker-compose)

### Key Features

- **CSV import/export** via `CsvService` (Apache Commons CSV) — exports `status_id` FK name, imports by name lookup with fallback to legacy text
- **CSV backup to server** via `BackupService` — timestamped files saved to `app.backup.dir`, listed in `/configuration`
- **Batch import** (`/games/batch-upload`, `/games/batch/next`, `/games/batch/skip`): upload a CSV (columns `Nombre`/`Name`, `Géneros`, `Fecha de Lanzamiento`, `Plataformas`, `Fuentes`) via `BatchImportService.parseCsv` (strips UTF-8 BOM). Entries are queued in the **HTTP session** (`batchQueue` attribute); each `/batch/next` call pops one entry, searches IGDB by name, resolves `Platform`/`Store` by bidirectional substring match, auto-fetches Steam App ID, and redirects to `/games/create` pre-filled (`batchMode`, `batchRemaining` query params) so the user reviews/confirms each game one by one.
- **Formats/Stores catalog** (`Store` entity): seeded at startup by `StoreSeedService` (@Order 3) with `Formato físico`, `Steam`, `Rockstar`, `EA`, `Epic`, `GOG`, `Ubisoft`. Selected per game via the `store` dropdown on the create form; also resolved automatically during batch import from the `Fuentes` CSV column.
- **Markdown notes** per game rendered via CommonMark, sanitized with JSoup (XSS protection)
- **IGDB token caching** with expiration in `IgdbService`
- **Sample data** auto-loaded by `DataInitializer` (@Order 1) if DB is empty
- **Status normalization** via `StatusMigrationService` (@Order 2): seeds canonical statuses (`Sin empezar`, `Jugando`, `Terminado`, `Abandonado`) and migrates legacy text values (`Completado`, `Playing`, `Backlog`, etc.) to FK on every startup — idempotent
- **H2 console toggle** at runtime: `H2ConsoleAccessFilter` checks `AppConfigurationService.isH2ConsoleEnabled()` on every request to `/h2-console/*`, no restart needed
- **Shared navbar** via Thymeleaf fragment `fragments/navbar.html` — used by all templates
- **Move to collection**: `POST /games/move-to-collection/{id}` moves a wishlist game into the owned collection
- **Target price on wishlist**: `Game.targetPrice` (`Double`) lets users set a per-game price objective from the create form (wishlist only) or the detail page (`POST /games/detail/{id}/target-price`). The wishlist shows the target and, after fetching GG.deals prices, a "below/above target" badge comparing the best current price. Exported/imported as CSV column `target_price`.
- **Edit page removed**: game status is changed inline on the detail page (`POST /games/detail/{id}/status`); the old `templates/games/edit.html` (orphaned, unreferenced) has been deleted

### Portable mode (`portable` Spring profile)

Used for the jpackage-generated portable executable (`application-portable.properties`):

- **`PortableTrayManager`** (`@Profile("portable")`): on `WebServerInitializedEvent`, adds a **system tray icon** (Java AWT) with "Open in browser" / "Stop jGameDatabase" menu, shows a startup balloon notification, and auto-opens the default browser. Falls back to just opening the browser if `SystemTray` is unsupported (some Linux desktops).
- **`ShutdownController`** (`@Profile("portable")`): exposes `POST /shutdown`, used by the UI to gracefully stop the embedded server and exit the JVM.
- **`PortableModelAdvice`** (`@Profile("portable")`, `@ControllerAdvice`): injects `portableMode=true` into every view's model so templates can conditionally show/hide portable-only UI (e.g. the shutdown button).
- **`StartupListener`** (always active): prints a startup banner with the local URL to stdout regardless of profile.
- Data/backups stored under `${user.home}/.jgamedatabase/` (see `application-portable.properties`); H2 console and DevTools disabled; `spring.main.headless=false` required for AWT tray support.

### Docker

Multi-stage Dockerfile (JDK 21 build → JRE runtime, non-root user). GitHub Actions publishes to Docker Hub on push to `master` or `v*` tags.
