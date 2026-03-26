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

Application runs on **http://localhost:8080**. H2 console at **http://localhost:8080/h2-console** (dev only).

## Architecture Overview

This is a **Spring Boot 3.2.3 / Java 21** personal game collection manager using Thymeleaf (server-side rendering), H2 file-based database, and Spring Data JPA.

### Layer Structure

```
xyz.sanchon.jgamedatabase
├── controller/    # MVC controllers (HomeController, GameController, ConfigurationController)
├── model/         # JPA entities (Game, Genre, Platform, AppConfiguration)
├── repository/    # Spring Data JPA repositories
├── service/       # Business logic + external API clients
├── dto/           # API response DTOs (IGDB, GG.deals, Steam)
└── bootstrap/     # DataInitializer — seeds DB with sample data on first run
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
- **Dev:** `./data/jgamedatabase` — H2 console enabled
- **Prod (Docker):** `/app/data/jgamedatabase` — H2 console disabled, Spring profile `prod`

Schema is auto-managed via `spring.jpa.hibernate.ddl-auto=update`.

### Key Features

- **CSV import/export** via `CsvService` (Apache Commons CSV)
- **Markdown notes** per game rendered via CommonMark, sanitized with JSoup (XSS protection)
- **IGDB token caching** with expiration in `IgdbService`
- **Sample data** auto-loaded by `DataInitializer` if DB is empty

### Docker

Multi-stage Dockerfile (JDK 21 build → JRE runtime, non-root user). GitHub Actions publishes to Docker Hub on push to `master` or `v*` tags.
