# Mi Portafolio

## Cómo ejecutarlo (no necesitas configurar nada)

La aplicación funciona **sin base de datos en la nube**. Si no encuentra
credenciales de Supabase, crea sola una base de datos local en tu equipo
y guarda ahí tus usuarios y archivos. Solo tienes que compilar y desplegar:

```bash
mvn clean package
```

Luego despliega `target/MiPortafolio.war` en Tomcat 11 (o usa el Dockerfile
incluido) y abre la app en el navegador. Entra a `/registro.jsp` y crea tu
cuenta: ya debería funcionar.

Puedes confirmar el estado en `/estado`. Si dice **Modo: Local**, todo está
bien; tus datos quedan guardados en:

```
<tu carpeta de usuario>/miportafolio-datos/
├── portafolio.mv.db   ← base de datos (usuarios y tareas)
└── archivos/          ← los archivos que subes
```

> Si borras esa carpeta, se borra tu cuenta y tus tareas. Para hacer una
> copia de seguridad, basta con copiarla.

## Opcional: publicarlo en internet con Supabase

Solo necesitas esto si quieres que tu portafolio esté en línea y accesible
desde cualquier lugar. Mientras no lo configures, la app seguirá en modo
local sin problemas.

1. Crea un proyecto gratuito en [supabase.com](https://supabase.com).
2. En tu proyecto, click en **Connect** (arriba) y elige la pestaña
   **Session pooler** (NO "Direct connection"). Esto es importante: el host
   de conexión directa de Supabase solo funciona por IPv6, y la mayoría de
   hosting gratuitos (Render, Railway, etc.) no tienen salida IPv6, así que
   la conexión falla con un mensaje genérico como
   *"El intento de conexión falló"* aunque la contraseña esté bien.
   Copia de ahí:
   - **Host**: algo como `aws-0-us-east-1.pooler.supabase.com`
   - **Port**: `5432`
   - **User**: algo como `postgres.abcdefghijklmno` (con un punto y el
     identificador de tu proyecto — no es solo `postgres`)
   - **Password**: la que pusiste al crear el proyecto (se puede resetear en
     **Project Settings → Database**)
3. En **Project Settings → API**, copia la **Project URL** y la
   **service_role key**.
4. En **Storage**, crea un bucket llamado `portafolio-archivos` y márcalo
   como público.
5. Copia `src/main/resources/application.properties.example` a
   `src/main/resources/application.properties` (ese archivo ya tiene tus
   credenciales reales si seguiste esta guía con ayuda, y está excluido de
   git en `.gitignore` para que nunca se suba por accidente a GitHub).
6. Vuelve a compilar y abre `/estado`: debe decir **Modo: Supabase (nube)**.

**Si vas a desplegar en Render, Railway u otro hosting:** no subas
`application.properties` con tu contraseña real. En su lugar, define estas
variables de entorno directamente en el panel del hosting (sección
"Environment"): `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`,
`SUPABASE_URL`, `SUPABASE_KEY`, `SUPABASE_BUCKET`. Tienen prioridad sobre
el archivo, así que ni siquiera necesitas subirlo.

Ten en cuenta que los datos de la base local **no se copian solos** a
Supabase: al cambiar de modo empiezas con un portafolio vacío.

---

Aplicación web Java (Maven, Servlets + JSP) para el portafolio de dos cursos:

- **Taller de Aplicaciones 1**
- **Algoritmo y Estructura de Datos**

Cada curso tiene **4 unidades**, y cada unidad **4 semanas** (16 semanas por curso).
Las tareas se suben a la semana que les corresponde.

Los archivos se guardan en **Supabase Storage** y los datos (usuarios, tareas,
visitas) en la base Postgres de **Supabase**.

Identidad visual basada en el azul institucional UPLA `#0066B1`.

## Estructura

```
MiPortafolio/
├── pom.xml
├── Dockerfile
├── README.md
├── .gitignore
└── src/main/
    ├── java/com/miportafolio/
    │   ├── config/      -> conexión a BD y Supabase Storage
    │   ├── controller/  -> Servlets (login, registro, subir, descargar...)
    │   ├── dao/         -> acceso a datos (UsuarioDAO, ArchivoDAO)
    │   ├── model/       -> POJOs (Usuario, Archivo)
    │   └── service/     -> lógica de negocio (AuthService, StorageService)
    ├── resources/
    │   └── application.properties
    └── webapp/
        ├── index.jsp, login.jsp, registro.jsp, dashboard.jsp
        ├── css/styles.css
        ├── js/main.js
        ├── semanas/ unidades/ videos/   (carpetas de referencia, los archivos
        │                                 reales viven en Supabase Storage)
        └── WEB-INF/web.xml
```

## 1. Configurar Supabase

### Base de datos

**No necesitas correr nada a mano.** Al arrancar la aplicación, la clase
`DatabaseInitializer` crea las tablas automáticamente si no existen.

Si prefieres crearlas tú desde el SQL Editor de Supabase, este es el esquema:

```sql
CREATE TABLE IF NOT EXISTS usuarios (
    id              SERIAL PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    email           VARCHAR(150) UNIQUE NOT NULL,
    password_hash   TEXT NOT NULL,
    fecha_registro  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS archivos (
    id                SERIAL PRIMARY KEY,
    usuario_id        INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
    titulo            VARCHAR(200),
    nombre_original   VARCHAR(255) NOT NULL,
    nombre_almacenado VARCHAR(255) NOT NULL,
    curso             VARCHAR(80)  NOT NULL,
    unidad            INTEGER      NOT NULL CHECK (unidad BETWEEN 1 AND 4),
    semana            INTEGER      NOT NULL CHECK (semana BETWEEN 1 AND 4),
    url               TEXT NOT NULL,
    tipo_mime         VARCHAR(120),
    tamano            BIGINT,
    fecha_subida      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS visitas (
    id      SERIAL PRIMARY KEY,
    ip      VARCHAR(60),
    pagina  VARCHAR(100),
    fecha   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_archivos_ubicacion ON archivos (curso, unidad, semana);
```

### Storage

1. En Supabase → **Storage**, crea un bucket llamado `portafolio-archivos`
   (o el nombre que prefieras) y márcalo como **público** si quieres que los
   archivos sean accesibles directamente por URL.
2. Copia la **Project URL** y la **service_role key** (Settings → API).

## 2. Configurar credenciales

Edita `src/main/resources/application.properties` **o**, mejor, define
variables de entorno (tienen prioridad y evitan subir claves al repositorio):

| Variable          | Descripción                                  |
|-------------------|-----------------------------------------------|
| `DB_HOST`         | Host de la base de datos de Supabase          |
| `DB_PORT`         | Puerto (por defecto `5432`)                   |
| `DB_NAME`         | Nombre de la base (por defecto `postgres`)    |
| `DB_USER`         | Usuario de la base                            |
| `DB_PASSWORD`     | Contraseña de la base                         |
| `SUPABASE_URL`    | URL del proyecto Supabase                     |
| `SUPABASE_KEY`    | `service_role key` de Supabase                |
| `SUPABASE_BUCKET` | Nombre del bucket de Storage                  |

## 3. Compilar y ejecutar

### Opción A: Maven + Tomcat local

```bash
mvn clean package
# copia target/MiPortafolio.war a la carpeta webapps/ de tu Tomcat 11
```

### Opción B: Docker

```bash
docker build -t mi-portafolio .
docker run -p 8080:8080 \
  -e DB_HOST=xxxx.supabase.co \
  -e DB_PORT=5432 \
  -e DB_NAME=postgres \
  -e DB_USER=postgres \
  -e DB_PASSWORD=tu_clave \
  -e SUPABASE_URL=https://xxxx.supabase.co \
  -e SUPABASE_KEY=tu_service_role_key \
  -e SUPABASE_BUCKET=portafolio-archivos \
  mi-portafolio
```

Luego abre `http://localhost:8080`.

## 4. Cómo se usa

1. Entra a `/registro.jsp` y crea tu cuenta.
2. Inicia sesión: tu nombre aparece en la barra superior en todas las páginas.
3. En **Subir tareas** eliges curso, unidad (1-4), semana (1-4), un título
   opcional y el archivo.
4. En el portafolio público (`index.jsp`) cualquiera puede elegir un curso,
   abrir una unidad y ver las tareas de cada semana, con botones para verlas
   o descargarlas.

### Rutas

| Ruta            | Qué hace                                            |
|-----------------|-----------------------------------------------------|
| `/`             | Portafolio público                                  |
| `/registro`     | Crear cuenta                                        |
| `/login`        | Iniciar sesión                                      |
| `/logout`       | Cerrar sesión                                       |
| `/dashboard.jsp`| Panel privado para subir y borrar tareas            |
| `/subir`        | Recibe el archivo (POST)                            |
| `/archivos`     | JSON del curso con sus unidades y semanas           |
| `/visualizar`   | Abre el archivo en el navegador                     |
| `/descargar`    | Descarga el archivo                                 |
| `/eliminar`     | Borra una tarea (solo su dueño)                     |
| `/estado`       | **Diagnóstico**: revisa la conexión a BD y Supabase |

## Si algo falla

Abre **`/estado`** en el navegador. Esa página te dice exactamente qué está mal:
conexión a la base de datos, tablas creadas, URL y clave de Supabase, y bucket.

El error más común al registrarse es que `application.properties` sigue con los
valores de ejemplo (`xxxxxxxx`, `CAMBIA_ESTA_CLAVE`). La aplicación ahora muestra
el mensaje real del error en vez de un genérico "error del servidor".

## Notas

- Pensado para **Tomcat 11** (Jakarta EE 11: Servlet 6.1 + JSP 4.0) y **Java 21**.
  Usa el namespace `jakarta.*`, **no** `javax.*`.
- También corre en **Tomcat 10.1** sin cambios de código: solo baja
  `jakarta.servlet-api` a `6.0.0`, `jakarta.servlet.jsp-api` a `3.1.0`,
  la versión de `web.xml` a `6.0` y el compilador a 17.
- Si tu JDK es 17 en lugar de 21, cambia `maven.compiler.source/target` a `17`
  en el `pom.xml`; todo lo demás funciona igual (Tomcat 11 acepta Java 17+).
- Los `.jsp` usan pequeños *scriptlets* para mantener el proyecto simple; en un
  proyecto más grande se recomendaría JSTL/EL o una capa de servicio adicional.
- Cambia las claves de ejemplo en `application.properties` antes de usarlo en
  producción, o mejor, usa siempre variables de entorno.
