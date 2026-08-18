# Contexto del Proyecto: jGameDatabase

Aplicación web (Spring Boot 3.2 / Java 21 / Thymeleaf / H2) para gestionar una colección
personal de videojuegos: colección, deseados, búsqueda IGDB, precios GG.deals, importación
por lotes, notas Markdown, CSV import/export y modo portable (jpackage) o Docker.

## Dónde encontrar cada cosa

No dupliques información aquí: mantenla en su documento de referencia y consúltalo.

* **Arquitectura técnica, paquetes, flujos internos, features y modo portable** →
  ver [`CLAUDE.md`](./CLAUDE.md) (documento vivo, actualízalo si tocas algo estructural).
* **Instalación, configuración, Docker, CI/CD y uso para el usuario final** →
  ver [`README.md`](./README.md).

## Reglas rápidas para agentes

* JDK 21 obligatorio (toolchain Gradle). Usar `./gradlew` / `gradlew.bat`.
* Antes de tocar la estructura de paquetes, servicios o endpoints, revisa y actualiza
  `CLAUDE.md` en el mismo cambio para que no quede desactualizado.
* Los secretos (IGDB, GG.deals) van en `application-local.properties` (git-ignored),
  variables de entorno, o se gestionan desde `/configuration` en runtime — nunca hardcodear.
* Evita crear documentación duplicada entre este archivo, `CLAUDE.md` y `README.md`.
