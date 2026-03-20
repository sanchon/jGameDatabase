# Contexto del Proyecto: jGameDatabase

## Descripción General
Aplicación web para la gestión de una colección personal de videojuegos. Permite listar, añadir, editar y eliminar juegos, gestionando relaciones con plataformas y géneros.

## Stack Tecnológico
*   **Lenguaje**: Java 21
*   **Framework**: Spring Boot 3.2.3
*   **Build Tool**: Gradle 8.7 (Wrapper configurado)
*   **Base de Datos**: H2 Database (En memoria para desarrollo)
*   **Persistencia**: Spring Data JPA (Hibernate)
*   **Frontend**: Thymeleaf (Motor de plantillas) + Bootstrap 5.3.0 (CSS Framework)

## Estructura del Proyecto
El proyecto sigue una arquitectura MVC estándar de Spring Boot:
*   `xyz.sanchon.jgamedatabase.model`: Entidades JPA (`Game`, `Platform`, `Genre`).
*   `xyz.sanchon.jgamedatabase.repository`: Interfaces `JpaRepository`.
*   `xyz.sanchon.jgamedatabase.controller`: Controladores Web (`HomeController`, `GameController`).
*   `xyz.sanchon.jgamedatabase.bootstrap`: Carga de datos iniciales (`DataInitializer`).

## Modelo de Datos
1.  **Game**: Entidad principal.
    *   Relación `ManyToOne` con `Platform`.
    *   Relación `ManyToOne` con `Genre`.
    *   Atributos: `title`, `releaseYear`, `status` (Playing, Completed, etc.), `rating`, `notes`, `igdbId`.
2.  **Platform**: Catálogo de plataformas (e.g., PS5, Switch).
3.  **Genre**: Catálogo de géneros (e.g., RPG, Action).

## Estado Actual de Desarrollo
*   [x] Configuración inicial del proyecto (Gradle, Spring Boot).
*   [x] Definición de Entidades y Repositorios.
*   [x] Carga de datos de prueba (`DataInitializer`) al arrancar.
*   [x] Pantalla de Inicio (`index.html`).
*   [x] Listado de Juegos (`games/list.html`).
*   [ ] Formulario para añadir nuevo juego (`games/create.html`).
*   [ ] Edición de juegos existentes.
*   [ ] Eliminación de juegos.

## Notas del Entorno (Importante)
*   **Problema Detectado**: Se ha detectado un conflicto de versiones con Gradle ejecutándose sobre una JVM muy reciente (posiblemente Java 26 / Class version 70), lo cual es incompatible con las versiones actuales de Gradle.
*   **Solución Requerida**: Configurar el IDE para usar una **Gradle JVM** compatible (Java 17 o Java 21).
*   **Configuración Gradle**: `sourceCompatibility` y `targetCompatibility` están fijados en Java 17 en el archivo `build.gradle`.
