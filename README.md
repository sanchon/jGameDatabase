# jGameDatabase

Aplicación web para **gestionar tu colección personal de videojuegos** y una **lista de deseados**, con búsqueda de títulos en **IGDB**, enlaces a **Metacritic**, precios a través de **GG.deals** (por Steam App ID), importación/exportación **CSV** y **notas en Markdown** en la página de detalle de cada juego.

---

## Opciones de instalación

Elige la que mejor se adapte a tu caso:

| Opción | Requiere | Ideal para |
|--------|----------|------------|
| [Ejecutable portable](#1-ejecutable-portable-recomendado-para-usuarios-finales) | Nada | Usar en tu PC sin instalar nada |
| [Docker](#2-docker) | Docker | Servidores o quienes ya usen contenedores |
| [Desde el código fuente](#3-desde-el-código-fuente-para-desarrolladores) | JDK 21 + Git | Desarrollo y personalización |

### 1. Ejecutable portable (recomendado para usuarios finales)

Descarga el ZIP para tu sistema desde la página de [Releases](../../releases/latest), descomprímelo y ejecuta:

| Sistema | Ejecutable |
|---------|-----------|
| Windows | `jGameDatabase\jGameDatabase.exe` |
| Linux   | `jGameDatabase/bin/jGameDatabase` |
| macOS   | `jGameDatabase.app` (doble clic) |

**No es necesario tener Java instalado.** La JRE va incluida en el ZIP.

Los datos se guardan en `~/.jgamedatabase/data/` (carpeta de usuario).
Tras arrancar, abre **http://localhost:8080** en tu navegador.

### 2. Docker

La forma más cómoda si ya tienes Docker. No necesitas clonar el repositorio.

**Con Docker Compose** (recomendado): crea un fichero `.env` con tus credenciales y ejecuta:

```env
IGDB_CLIENT_ID=tu_client_id
IGDB_CLIENT_SECRET=tu_client_secret
GGDEALS_API_KEY=tu_api_key
```

```bash
docker compose -f docker/docker-compose.yml up -d
```

**O directamente con `docker run`:**

```bash
docker run -d \
  --name jgamedatabase \
  -p 8080:8080 \
  -v jgamedatabase-data:/app/data \
  -e IGDB_CLIENT_ID="tu_client_id" \
  -e IGDB_CLIENT_SECRET="tu_client_secret" \
  -e GGDEALS_API_KEY="tu_api_key" \
  --restart unless-stopped \
  <tu_usuario_docker>/jgamedatabase:latest
```

Disponible en **http://localhost:8080**. Las credenciales también se pueden introducir desde la UI en `/configuration` tras el primer arranque.

### 3. Desde el código fuente (para desarrolladores)

Requiere **JDK 21** y **Git**.

```bash
git clone <url-del-repositorio>
cd jGameDatabase
./gradlew bootRun          # Linux / macOS
gradlew.bat bootRun        # Windows
```

Disponible en **http://localhost:8080**. Consulta la sección [Configuración](#configuración) para añadir las credenciales de API.

---

## Funcionalidades

| Área | Descripción |
|------|-------------|
| **Inicio** | Punto de entrada a la aplicación con accesos directos a colección, deseados y añadir juego. |
| **Mi colección** | Listado de juegos poseídos: filtros por estado, género y plataforma (se aplican al cambiar el desplegable), ordenación por título, año o nota, columnas de precio GG.deals cuando hay Steam App ID. |
| **Deseados** | Lista de juegos deseados; búsqueda de precios en GG.deals bajo demanda. |
| **Añadir juego** | Búsqueda en IGDB y formulario de alta. El desplegable de plataformas se rellena automáticamente con las plataformas que IGDB reporta para ese juego; si alguna no existe en la base de datos local, se crea al cargar el formulario. En deseados, búsqueda adicional en la **tienda Steam** para guardar el **Steam App ID** (necesario para precios). |
| **Detalle** | Vista `/games/detail/{id}`: portada, metadatos, enlaces a IGDB / Steam / Metacritic; **notas en Markdown** con edición integrada y vista renderizada. |
| **Editar** | Estado, valoración, notas, Steam App ID, etc. |
| **Configuración** | Accesible desde la barra de navegación en todas las páginas (`/configuration`). Permite gestionar desde la UI las credenciales de **IGDB** (client ID y secret) y **GG.deals** (API key y región), como alternativa a `application-local.properties` o variables de entorno. Es la vía recomendada cuando se usa Docker. |
| **CSV** | Exportar e importar **todos** los juegos (colección y deseados) desde la vista de colección. Los campos exportados/importados incluyen: `id`, `titulo`, `año`, `plataforma`, `genero`, `estado`, `rating`, `igdb_id`, `steam_app_id`, `igdb_slug`, `portada_url`, `notas` y `wishlist`. Al importar, las plataformas y géneros que no existan en la base de datos se crean automáticamente. |

Las credenciales de APIs externas se pueden configurar mediante `application-local.properties`, variables de entorno, o directamente desde la UI en la página de [Configuración](#configuración).

---

## Requisitos

- **JDK 21** (recomendado; el proyecto declara toolchain Java 21 en Gradle).
- **Git** (opcional, para clonar el repositorio).
- Conexión a Internet para IGDB, GG.deals y la API pública de búsqueda de Steam.

---

## Configuración

### Credenciales de APIs externas

Hay tres formas de configurar las credenciales, en orden de precedencia:

1. **Página de Configuración** (`/configuration`): la propia aplicación permite introducirlas desde la UI y las persiste en la base de datos. Es la opción más cómoda, especialmente en Docker.
2. **`application-local.properties`**: fichero ignorado por Git, recomendado para desarrollo local.
3. **Variables de entorno**: útil para CI/CD o contenedores cuando no se quiere tocar ficheros.

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
- `GGDEALS_REGION` (opcional; por defecto `es` en `application.properties`)

### Base de datos

La aplicación usa **H2 en modo archivo**. El fichero se almacena en rutas distintas según el entorno:

| Entorno | Perfil Spring | Ruta del fichero H2 |
|---------|--------------|---------------------|
| Local (Gradle / IntelliJ) | `local` | `./data/jgamedatabase` (relativo al directorio de trabajo) |
| Docker | `prod` | `/app/data/jgamedatabase` (dentro del volumen del contenedor) |

Cada entorno accede a su propio fichero independiente, sin conflictos de bloqueo entre la instancia local y la de Docker. El directorio `data/` está ignorado en Git. La primera ejecución crea los ficheros.

> **Datos de muestra**: si la base de datos está vacía al arrancar, la aplicación inserta automáticamente 3 juegos de ejemplo (Zelda: Breath of the Wild, Elden Ring, Hollow Knight) junto con 4 plataformas (PlayStation 5, Xbox Series X, Nintendo Switch, PC) y 4 géneros (RPG, Action, Adventure, Platformer).

### Consola H2

Con la app en marcha, accesible en **http://localhost:8080/h2-console**:

| Campo | Valor |
|-------|-------|
| JDBC URL | `jdbc:h2:file:./data/jgamedatabase` |
| Usuario | `sa` |
| Contraseña | `password` |

### Conexión desde IntelliJ Database plugin (u otro cliente externo)

H2 en modo archivo solo admite una JVM a la vez. Para inspeccionar la base de datos tienes dos opciones:

**Opción A — Con la app en marcha:** usa la consola web integrada en **http://localhost:8080/h2-console** (ver sección anterior).

**Opción B — Con la app parada:** conéctate desde IntelliJ con ruta absoluta:

| Campo | Valor |
|-------|-------|
| Driver | H2 |
| URL | `jdbc:h2:file:C:/Users/<tu_usuario>/IdeaProjects/jGameDatabase/data/jgamedatabase` |
| Usuario | `sa` |
| Contraseña | `password` |

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

La aplicación queda disponible en **http://localhost:8080**.

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
- **Inspección de H2**: mientras la app está corriendo usa la consola web (`/h2-console`); si la app está parada puedes abrir el fichero directamente desde IntelliJ (ver sección [Conexión desde IntelliJ](#conexión-desde-intellij-database-plugin-u-otro-cliente-externo)).

---

## Docker y Docker Hub

Los ficheros Docker están en el directorio **`docker/`**:

| Fichero | Descripción |
|---------|-------------|
| `docker/Dockerfile` | Build multi-etapa (JDK 21 → JRE, usuario no root, volumen para H2) |
| `docker/docker-compose.yml` | Composición lista para producción |
| `.dockerignore` | En el root (el contexto de build es siempre el root del proyecto) |

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
docker build -f docker/Dockerfile -t <tu_usuario>/jgamedatabase:latest .
docker push <tu_usuario>/jgamedatabase:latest
```

### Ejecutar con Docker Compose (recomendado)

El fichero `docker/docker-compose.yml` usa la imagen publicada en Docker Hub y monta el volumen de datos en `./data` (relativo al root del proyecto). Crea un fichero `.env` en el root con las credenciales:

```env
IGDB_CLIENT_ID=tu_client_id
IGDB_CLIENT_SECRET=tu_client_secret
GGDEALS_API_KEY=tu_api_key
```

Y luego:

```bash
docker compose -f docker/docker-compose.yml up -d
```

Para detenerlo:

```bash
docker compose -f docker/docker-compose.yml down
```

### Ejecutar el contenedor directamente (alternativa)

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
