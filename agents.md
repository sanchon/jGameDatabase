# Contexto del Proyecto: jGameDatabase

## Descripción general
Aplicación web para gestionar una colección personal de videojuegos: catálogo en colección, lista de deseados, búsqueda vía IGDB, precios vía GG.deals (Steam), exportación/importación CSV, notas en Markdown en la ficha de detalle y enlaces externos (Metacritic, IGDB, Steam).

## Stack tecnológico
* **Lenguaje**: Java 21 (toolchain en Gradle)
* **Framework**: Spring Boot 3.2.x
* **Build**: Gradle (wrapper incluido)
* **Base de datos**: H2 en archivo (`./data/jgamedatabase`), consola H2 habilitada en desarrollo
* **Persistencia**: Spring Data JPA (Hibernate)
* **Cliente HTTP**: WebClient (WebFlux) para IGDB, GG.deals y búsqueda en la tienda Steam
* **Frontend**: Thymeleaf + Bootstrap 5
* **Markdown**: commonmark-java + Jsoup (HTML saneado en notas)

## Estructura del proyecto (MVC)
* `xyz.sanchon.jgamedatabase.model`: entidades JPA (`Game`, `Platform`, `Genre`).
* `xyz.sanchon.jgamedatabase.repository`: repositorios Spring Data.
* `xyz.sanchon.jgamedatabase.controller`: `HomeController`, `GameController` (rutas bajo `/games`, detalle, wishlist, APIs auxiliares).
* `xyz.sanchon.jgamedatabase.service`: lógica (IGDB, GG.deals, Steam store search, CSV, Markdown).
* `xyz.sanchon.jgamedatabase.dto`: DTOs de APIs externas.
* `xyz.sanchon.jgamedatabase.bootstrap`: datos iniciales opcionales (`DataInitializer`).
* `src/main/resources/templates`: vistas Thymeleaf.
* Perfiles: `application.properties` + `application-local.properties` (secretos; ver `.gitignore`).

## Modelo de datos (resumen)
* **Game**: título, año, plataforma, género, estado (colección), `wishlist`, valoración, notas (Markdown), `igdbId`, `igdbSlug`, `coverUrl`, `steamAppId`.
* **Platform** / **Genre**: catálogos reutilizables.

## Integraciones externas
* **IGDB** (vía Twitch OAuth client credentials): búsqueda de juegos; opcionalmente datos auxiliares.
* **Steam Store Search** (`store.steampowered.com/api/storesearch/`): localizar Steam App ID al añadir a deseados.
* **GG.deals** (`api.gg.deals/v1/prices/by-steam-app-id/`): precios por lista de Steam App IDs y región configurable.

## Estado de desarrollo (referencia)
* CRUD de juegos, colección vs wishlist, filtros en listado (auto-submit), ordenación conservando filtros.
* Búsqueda IGDB, alta desde resultados, detalle `/games/detail/{id}`, edición de notas Markdown en detalle.
* Precios GG.deals en colección y en deseados (bajo demanda en wishlist).
* Import/export CSV.

## Entorno y Gradle
* Usar una **JDK compatible con el wrapper de Gradle del proyecto** (p. ej. **Java 21** alineado con `build.gradle`).
* Si el IDE usa una JVM demasiado nueva para Gradle, ajustar **Gradle JVM** en el IDE o usar `./gradlew` desde terminal con JDK 21.

## CI / Docker Hub
* Workflow **GitHub Actions** (`.github/workflows/docker-publish.yml`): construye la imagen con el `Dockerfile` y la publica en Docker Hub al hacer push a `main`/`master`, al etiquetar `v*` o manualmente. Requiere secretos `DOCKERHUB_USERNAME` y `DOCKERHUB_TOKEN` en el repositorio.

## Documentación de usuario
* Instalación, configuración, arranque y despliegue Docker: ver **README.md** en la raíz del repositorio.
