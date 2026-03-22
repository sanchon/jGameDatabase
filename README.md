# jGameDatabase

Aplicación web para **gestionar tu colección personal de videojuegos** y una **lista de deseados**, con búsqueda de títulos en **IGDB**, enlaces a **Metacritic**, precios a través de **GG.deals** (por Steam App ID), importación/exportación **CSV** y **notas en Markdown** en la página de detalle de cada juego.

---

## Funcionalidades

| Área | Descripción |
|------|-------------|
| **Inicio** | Punto de entrada a la aplicación. |
| **Mi colección** | Listado de juegos poseídos: filtros por estado, género y plataforma (se aplican al cambiar el desplegable), ordenación por título, año o nota, columnas de precio GG.deals cuando hay Steam App ID. |
| **Deseados** | Lista de juegos deseados; búsqueda de precios en GG.deals bajo demanda. |
| **Añadir juego** | Búsqueda en IGDB y formulario de alta; en deseados, búsqueda adicional en la **tienda Steam** para guardar el **Steam App ID** (necesario para precios). |
| **Detalle** | Vista `/games/detail/{id}`: portada, metadatos, enlaces a IGDB / Steam / Metacritic; **notas en Markdown** con edición integrada y vista renderizada. |
| **Editar** | Estado, valoración, notas, Steam App ID, etc. |
| **CSV** | Exportar e importar la colección (desde la vista de colección). |

Las credenciales de APIs externas y el perfil local se describen en la sección [Configuración](#configuración).

---

## Requisitos

- **JDK 21** (recomendado; el proyecto declara toolchain Java 21 en Gradle).
- **Git** (opcional, para clonar el repositorio).
- Conexión a Internet para IGDB, GG.deals y la API pública de búsqueda de Steam.

---

## Configuración

### Perfil `local` y secretos

Por defecto, `application.properties` activa `spring.profiles.active=local`. El fichero `src/main/resources/application-local.properties` está pensado para **credenciales locales** y está **ignorado por Git** (véase `.gitignore`).

1. Copia el ejemplo o crea `application-local.properties` en `src/main/resources/`.
2. Define al menos:

```properties
# IGDB (credenciales de la aplicación en Twitch Developer Console)
igdb.client-id=TU_CLIENT_ID
igdb.client-secret=TU_CLIENT_SECRET

# GG.deals (clave en tu cuenta / ajustes de API)
ggdeals.api-key=TU_API_KEY

# Opcional: región de precios (p. ej. es, eu, us, gb…)
ggdeals.region=es
```

### Variables de entorno (alternativa)

Puedes omitir `application-local.properties` y exportar:

- `IGDB_CLIENT_ID`, `IGDB_CLIENT_SECRET`
- `GGDEALS_API_KEY`
- `GGDEALS_REGION` (opcional; por defecto suele usarse `es` en `application.properties`)

### Base de datos

La aplicación usa **H2 en modo archivo** en `./data/jgamedatabase` (ruta relativa al directorio de trabajo al arrancar). El directorio `data/` está ignorado en Git. La primera ejecución crea los ficheros.

### Consola H2

Con la app en marcha, la consola H2 puede estar disponible según `application.properties` (`spring.h2.console.enabled=true`). La URL JDBC configurada es la del fichero anterior; usuario/contraseña por defecto en el proyecto: `sa` / `password` (solo entorno local).

---

## Instalación y arranque

### Clonar o copiar el proyecto

```bash
git clone <url-del-repositorio>
cd jGameDatabase
```

### Crear configuración local

Crea `src/main/resources/application-local.properties` con tus claves (sección anterior).

### Ejecutar con Gradle (recomendado)

**Linux / macOS:**

```bash
./gradlew bootRun
```

**Windows (PowerShell o CMD):**

```bat
gradlew.bat bootRun
```

La aplicación suele quedar disponible en **http://localhost:8080** (puerto por defecto de Spring Boot si no se cambia).

### Compilar sin arrancar

```bash
./gradlew build
```

El JAR ejecutable queda en `build/libs/` (nombre con versión del proyecto).

### Ejecutar el JAR

```bash
java -jar build/libs/jGameDatabase-0.0.1-SNAPSHOT.jar
```

(Ajusta el nombre del fichero si la versión cambia.)

---

## Desarrollo

- **Hot reload**: Spring DevTools está incluido como dependencia de desarrollo; recarga al cambiar código según configuración del IDE.
- **Gradle JVM**: si el IDE falla al sincronizar Gradle, asigna explícitamente **JDK 21** como JVM de Gradle.

---

## Docker y Docker Hub

El repositorio incluye un `Dockerfile` multi-etapa (JDK 21 → JRE, usuario no root, volumen para H2) y un `.dockerignore` para acelerar el contexto de build.

### Publicación automática con GitHub Actions

El workflow [`.github/workflows/docker-publish.yml`](.github/workflows/docker-publish.yml) construye la imagen y la sube a **Docker Hub** cuando:

- se hace **push** a las ramas `main` o `master`, o
- se publica un **tag** semántico `v*` (p. ej. `v1.2.0`), o
- se lanza manualmente desde la pestaña **Actions** → **Publish Docker image** → **Run workflow**.

**Secretos del repositorio** (en GitHub: *Settings → Secrets and variables → Actions*):

| Secreto | Descripción |
|---------|-------------|
| `DOCKERHUB_USERNAME` | Tu usuario de Docker Hub |
| `DOCKERHUB_TOKEN` | [Access Token](https://docs.docker.com/security/for-developers/access-tokens/) de Docker Hub (recomendado; no uses la contraseña principal si puedes evitarlo) |

La imagen se publica como **`<usuario>/jgamedatabase`** con etiquetas:

- `latest` (solo en la rama por defecto del repositorio),
- versión tipo `1.2.0` al empujar un tag `v1.2.0`,
- `sha-<corto>` con el commit.

Tras el primer push correcto, en Docker Hub aparecerá el repositorio de imagen correspondiente.

### Construcción y subida manual (opcional)

Si prefieres no usar CI:

```bash
docker login
docker build -t <tu_usuario>/jgamedatabase:latest .
docker push <tu_usuario>/jgamedatabase:latest
```

### Ejecutar el contenedor (servidor o local)

Cualquier usuario con Docker instalado (o en tu servidor Linux de producción) puede ejecutar la aplicación siguiendo estos pasos:

```bash
docker run -d \
  --name jgamedatabase \
  -p 8080:8080 \
  -v jgamedatabase-data:/app/data \
  -e IGDB_CLIENT_ID="tu_client_id_aqui" \
  -e IGDB_CLIENT_SECRET="tu_client_secret_aqui" \
  -e GGDEALS_API_KEY="tu_api_key_aqui" \
  --restart unless-stopped \
  <tu_usuario_docker>/jgamedatabase:latest
```

**Explicación de los parámetros principales:**
- `-d`: Ejecuta el contenedor en segundo plano (detached mode).
- `-p 8080:8080`: Mapea el puerto 8080 del servidor al puerto 8080 del contenedor. La aplicación será accesible en `http://localhost:8080` (o la IP del servidor).
- `-v jgamedatabase-data:/app/data`: Crea un **volumen persistente**. Esto es crítico para que la base de datos H2 no se borre si el contenedor se apaga o actualiza.
- `-e ...`: Variables de entorno necesarias para las integraciones de IGDB y GG.deals. Reemplaza con tus propias credenciales.
- `--restart unless-stopped`: Asegura que el contenedor arranque automáticamente si el servidor se reinicia o Docker se reinicia.

**Ver los logs (opcional):**
Si quieres comprobar que la aplicación ha arrancado correctamente, puedes ver los registros con:
```bash
docker logs -f jgamedatabase
```
