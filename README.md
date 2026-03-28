# jGameDatabase

Web application to **manage your personal video game collection** and a **wishlist**, with title search on **IGDB**, links to **Metacritic**, prices via **GG.deals** (by Steam App ID), **CSV** import/export, and **Markdown notes** on each game's detail page.

---

## Installation options

Choose the one that best fits your case:

| Option | Requires | Ideal for |
|--------|----------|-----------|
| [Portable executable](#1-portable-executable-recommended-for-end-users) | Nothing | Using on your PC without installing anything |
| [Docker](#2-docker) | Docker | Servers or those already using containers |
| [From source code](#3-from-source-code-for-developers) | JDK 21 + Git | Development and customization |

### 1. Portable executable (recommended for end users)

Download the ZIP for your system from the [Releases](../../releases/latest) page, unzip it, and run:

| System | Executable |
|--------|-----------|
| Windows | `jGameDatabase\jGameDatabase.exe` |
| Linux   | `jGameDatabase/bin/jGameDatabase` |
| macOS   | `jGameDatabase.app` (double click) |

**No need to have Java installed.** The JRE is included in the ZIP.

Data is stored in `~/.jgamedatabase/data/` (user folder).
After starting, open **http://localhost:8080** in your browser.

### 2. Docker

The most convenient option if you already have Docker. No need to clone the repository.

**With Docker Compose** (recommended): create a `.env` file with your credentials and run:

```env
IGDB_CLIENT_ID=your_client_id
IGDB_CLIENT_SECRET=your_client_secret
GGDEALS_API_KEY=your_api_key
```

```bash
docker compose -f docker/docker-compose.yml up -d
```

**Or directly with `docker run`:**

```bash
docker run -d \
  --name jgamedatabase \
  -p 8080:8080 \
  -v jgamedatabase-data:/app/data \
  -e IGDB_CLIENT_ID="your_client_id" \
  -e IGDB_CLIENT_SECRET="your_client_secret" \
  -e GGDEALS_API_KEY="your_api_key" \
  --restart unless-stopped \
  sanchon/jgamedatabase:latest
```

Available at **http://localhost:8080**. Credentials can also be entered from the UI at `/configuration` after the first startup.

### 3. From source code (for developers)

Requires **JDK 21** and **Git**.

```bash
git clone <repository-url>
cd jGameDatabase
./gradlew bootRun          # Linux / macOS
gradlew.bat bootRun        # Windows
```

Available at **http://localhost:8080**. See the [Configuration](#configuration) section to add API credentials.

---

## Features

| Area | Description |
|------|-------------|
| **Home** | Application entry point with shortcuts to collection, wishlist, and add game. |
| **My Collection** | List of owned games: filters by status, genre, and platform (applied when changing the dropdown), sortable by title, year, or rating. The title is a direct link to the detail page. |
| **Wishlist** | List of desired games; on-demand price search on GG.deals. |
| **Add game** | Search on IGDB and entry form. The platforms dropdown is automatically populated with the platforms IGDB reports for that game; if any does not exist in the local database, it is created when loading the form. For wishlist items, an additional search on the **Steam store** to save the **Steam App ID** (required for prices). |
| **Detail** | View `/games/detail/{id}`: cover, metadata, links to IGDB / Metacritic; **Markdown notes** with inline editing and rendered view. **Status can be changed directly** from the detail page without leaving it. Includes delete button with confirmation. |
| **Game statuses** | Canonical statuses: *Not started*, *Playing*, *Completed*, *Abandoned*. Each status has its own color and icon in the lists and detail. The automatic migration on startup converts any legacy text (e.g. `Completado`, `Playing`, `Backlog`) to the corresponding canonical status. |
| **Configuration** | Accessible from the navigation bar on all pages (`/configuration`). Manages **IGDB** and **GG.deals** credentials. Includes option to **reset the database** and to **enable/disable the H2 console** (shows the link and credentials when active). |
| **CSV Backups** | Generates a CSV on the server (`/app/backups` in Docker, `~/.jgamedatabase/backups` in portable, `./backups` in dev) from `/configuration`, with a timestamped name. Different from browser export. |
| **CSV** | Export and import **all** games (collection and wishlist) from the collection view. Exported/imported fields include: `id`, `titulo`, `año`, `plataforma`, `genero`, `estado`, `rating`, `igdb_id`, `steam_app_id`, `igdb_slug`, `portada_url`, `notas`, and `wishlist`. On import, platforms and genres that do not exist in the database are created automatically. |

External API credentials can be configured via `application-local.properties`, environment variables, or directly from the UI on the [Configuration](#configuration) page.

---

## Requirements

- **JDK 21** (recommended; the project declares a Java 21 toolchain in Gradle).
- **Git** (optional, to clone the repository).
- Internet connection for IGDB, GG.deals, and the public Steam search API.

---

## Configuration

### External API credentials

There are three ways to configure credentials, in order of precedence:

1. **Configuration page** (`/configuration`): the application itself allows entering them from the UI and persists them in the database. This is the most convenient option, especially in Docker.
2. **`application-local.properties`**: file ignored by Git, recommended for local development.
3. **Environment variables**: useful for CI/CD or containers when you don't want to touch files.

### `local` profile and secrets

By default, `application.properties` activates `spring.profiles.active=local`. The file `src/main/resources/application-local.properties` is intended for **local credentials** and is **ignored by Git** (see `.gitignore`).

1. Copy the example or create `application-local.properties` in `src/main/resources/`.
2. Define at least:

```properties
# IGDB (application credentials from the Twitch Developer Console)
igdb.client-id=YOUR_CLIENT_ID
igdb.client-secret=YOUR_CLIENT_SECRET

# GG.deals (key from your account / API settings)
ggdeals.api-key=YOUR_API_KEY

# Optional: price region (e.g. es, eu, us, gb…)
ggdeals.region=es
```

### Environment variables (alternative)

You can skip `application-local.properties` and export:

- `IGDB_CLIENT_ID`, `IGDB_CLIENT_SECRET`
- `GGDEALS_API_KEY`
- `GGDEALS_REGION` (optional; defaults to `es` in `application.properties`)

### Database

The application uses **H2 in file mode**. The file is stored in different paths depending on the environment:

| Environment | Spring profile | H2 file path |
|-------------|---------------|--------------|
| Local (Gradle / IntelliJ) | `local` | `./data/jgamedatabase` (relative to working directory) |
| Docker | `prod` | `/app/data/jgamedatabase` (inside the container volume) |

Each environment accesses its own independent file, with no locking conflicts between the local and Docker instances. The `data/` directory is ignored in Git. The first run creates the files.

> **Sample data**: if the database is empty on startup, the application automatically inserts 3 sample games (Zelda: Breath of the Wild, Elden Ring, Hollow Knight) along with 4 platforms (PlayStation 5, Xbox Series X, Nintendo Switch, PC) and 4 genres (RPG, Action, Adventure, Platformer).

### CSV Backups

The Configuration page allows generating CSV backups on the server. The path varies by environment:

| Environment | Backup path |
|-------------|-------------|
| Local (Gradle) | `./backups/` |
| Portable | `~/.jgamedatabase/backups/` |
| Docker | `/app/backups/` (mapped to the host volume `../backups`) |

Files are named `backup_YYYYMMDD_HHmmss.csv`. They are independent of the browser export and serve as server-side backups.

### H2 Console

The H2 console can be enabled and disabled from the **Configuration** page without needing to restart. When active, the page itself shows the link and credentials:

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:file:./data/jgamedatabase` (local) / `jdbc:h2:file:/app/data/jgamedatabase` (Docker) |
| User | `sa` |
| Password | `password` |

### Connecting from IntelliJ Database plugin (or another external client)

H2 in file mode only allows one JVM at a time. To inspect the database you have two options:

**Option A — With the app running:** use the built-in web console at **http://localhost:8080/h2-console** (see previous section).

**Option B — With the app stopped:** connect from IntelliJ with the absolute path:

| Field | Value |
|-------|-------|
| Driver | H2 |
| URL | `jdbc:h2:file:C:/Users/<your_user>/IdeaProjects/jGameDatabase/data/jgamedatabase` |
| User | `sa` |
| Password | `password` |

---

## Installation and startup

### Clone or copy the project

```bash
git clone <repository-url>
cd jGameDatabase
```

### Create local configuration

Create `src/main/resources/application-local.properties` with your keys (see previous section).

### Run with Gradle (recommended)

**Linux / macOS:**

```bash
./gradlew bootRun
```

**Windows (PowerShell or CMD):**

```bat
gradlew.bat bootRun
```

The application is available at **http://localhost:8080**.

### Build without running

```bash
./gradlew build
```

The executable JAR is placed in `build/libs/` (name includes the project version).

### Run the JAR

```bash
java -jar build/libs/jGameDatabase-1.1.3.jar
```

(Adjust the filename if the version changes.)

---

## Development

- **Hot reload**: Spring DevTools is included as a development dependency; it reloads on code changes according to IDE configuration.
- **Gradle JVM**: if the IDE fails to sync Gradle, explicitly assign **JDK 21** as the Gradle JVM.
- **H2 inspection**: while the app is running use the web console (`/h2-console`); if the app is stopped you can open the file directly from IntelliJ (see [Connecting from IntelliJ](#connecting-from-intellij-database-plugin-or-another-external-client) section).

---

## Docker and Docker Hub

Docker files are in the **`docker/`** directory:

| File | Description |
|------|-------------|
| `docker/Dockerfile` | Multi-stage build (JDK 21 → JRE, non-root user, volume for H2) |
| `docker/docker-compose.yml` | Production-ready composition |
| `.dockerignore` | In the root (the build context is always the project root) |

### Automatic publishing with GitHub Actions

The workflow [`.github/workflows/docker-publish.yml`](.github/workflows/docker-publish.yml) builds the image and pushes it to **Docker Hub** when:

- a semantic **tag** `v*` is published (e.g. `v1.2.0`), or
- triggered manually from the **Actions** tab → **Publish Docker image** → **Run workflow**.

**Repository secrets** (on GitHub: *Settings → Secrets and variables → Actions*):

| Secret | Description |
|--------|-------------|
| `DOCKERHUB_USERNAME` | Your Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub [Access Token](https://docs.docker.com/security/for-developers/access-tokens/) (recommended; avoid using your main password if possible) |

The image is published as **`<username>/jgamedatabase`** with tags:

- `latest` (only on the repository's default branch),
- version like `1.2.0` when pushing a tag `v1.2.0`,
- `sha-<short>` with the commit.

After the first successful push, the corresponding image repository will appear on Docker Hub.

### Manual build and push (optional)

If you prefer not to use CI:

```bash
docker login
docker build -f docker/Dockerfile -t <your_username>/jgamedatabase:latest .
docker push <your_username>/jgamedatabase:latest
```

### Run with Docker Compose (recommended)

The `docker/docker-compose.yml` file uses the image published on Docker Hub and mounts the data volume at `./data` (relative to the project root). Create a `.env` file in the root with the credentials:

```env
IGDB_CLIENT_ID=your_client_id
IGDB_CLIENT_SECRET=your_client_secret
GGDEALS_API_KEY=your_api_key
```

And then:

```bash
docker compose -f docker/docker-compose.yml up -d
```

To stop it:

```bash
docker compose -f docker/docker-compose.yml down
```

### Run the container directly (alternative)

Any user with Docker installed (or on your Linux production server) can run the application with these steps:

```bash
docker run -d \
  --name jgamedatabase \
  -p 8080:8080 \
  -v jgamedatabase-data:/app/data \
  -e IGDB_CLIENT_ID="your_client_id_here" \
  -e IGDB_CLIENT_SECRET="your_client_secret_here" \
  -e GGDEALS_API_KEY="your_api_key_here" \
  --restart unless-stopped \
  <your_docker_username>/jgamedatabase:latest
```

**Main parameters explained:**
- `-d`: Runs the container in the background (detached mode).
- `-p 8080:8080`: Maps port 8080 of the server to port 8080 of the container. The application will be accessible at `http://localhost:8080` (or the server's IP).
- `-v jgamedatabase-data:/app/data`: Creates a **persistent volume**. This is critical to prevent the H2 database from being deleted if the container shuts down or is updated.
  > **Note:** with `docker run`, CSV backups are not mounted outside the container. Use `docker-compose.yml` to have the `backups` volume persisted on the host.
- `-e ...`: Environment variables required for IGDB and GG.deals integrations. Replace with your own credentials.
- `--restart unless-stopped`: Ensures the container starts automatically if the server restarts or Docker restarts.

**View logs (optional):**
If you want to verify the application has started correctly, you can view the logs with:
```bash
docker logs -f jgamedatabase
```
