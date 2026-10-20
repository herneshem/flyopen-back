# FlyOpen Backend

Backend de **FlyOpen**, desarrollado con Java y Spring Boot. Este proyecto contiene la lógica del servidor y la conexión con la base de datos PostgreSQL.

## Tecnologías utilizadas

- **Java 21**
- **Spring Boot**
- **Spring Web** — desarrollo de servicios web y API REST.
- **Spring Data JPA** — acceso y persistencia de datos.
- **Hibernate** — ORM para la comunicación con la base de datos.
- **PostgreSQL** — sistema de gestión de bases de datos relacional.
- **Spring Security** — seguridad y autenticación.
- **JWT (JSON Web Token)** — autenticación basada en tokens.
- **Maven** — gestión de dependencias y construcción del proyecto.
- **Jenkins** — automatización del proceso de integración continua mediante `Jenkinsfile`.

## Requisitos previos

Antes de ejecutar el proyecto, necesitas:

- Java JDK 21.
- PostgreSQL instalado y en ejecución.
- Una base de datos llamada `flyopen`.
- Git para clonar el repositorio.

## Instalación

Clona el repositorio:

```bash
git clone https://github.com/herneshem/flyopen-back.git
```

Entra en el directorio del proyecto:

```bash
cd flyopen-back
```

## Configuración de la base de datos

El backend utiliza PostgreSQL. Para el entorno de desarrollo local, la configuración de conexión es:

| Parámetro | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `5432` |
| Base de datos | `flyopen` |
| Usuario | Configurado mediante variable de entorno |
| Contraseña | Configurada mediante variable de entorno |

Crea la base de datos si todavía no existe:

```sql
CREATE DATABASE flyopen;
```

### Variables de entorno

Configura las siguientes variables antes de ejecutar la aplicación:

| Variable | Descripción |
|---|---|
| `DB_URL` | URL de conexión JDBC a PostgreSQL |
| `DB_USERNAME` | Usuario de PostgreSQL |
| `DB_PASSWORD` | Contraseña de PostgreSQL |
| `JWT_SECRET` | Clave secreta utilizada para firmar los tokens JWT |
| `JWT_EXPIRATION` | Tiempo de expiración del token, en milisegundos, si así está implementado |

Ejemplo de URL para desarrollo local:

```text
jdbc:postgresql://localhost:5432/flyopen
```

**Importante:** configura los valores reales en las variables de entorno de tu equipo. No publiques contraseñas, tokens ni claves JWT en el repositorio.

Comprueba que `src/main/resources/application.properties` utiliza estas variables de entorno antes de ejecutar el proyecto.

## Ejecución

El repositorio incluye Maven Wrapper, por lo que puedes ejecutar el proyecto sin instalar Maven globalmente.

### Windows

```bash
mvnw.cmd spring-boot:run
```

### Linux o macOS

```bash
./mvnw spring-boot:run
```

También puedes abrir el proyecto en IntelliJ IDEA, configurar las variables de entorno en la configuración de ejecución de Spring Boot y ejecutar la clase principal de la aplicación.

## Compilación y pruebas

Para compilar el proyecto:

```bash
mvnw.cmd clean package
```

Para ejecutar las pruebas:

```bash
mvnw.cmd test
```

En Linux o macOS, sustituye `mvnw.cmd` por `./mvnw`.

## Persistencia de datos

El proyecto utiliza Spring Data JPA e Hibernate para gestionar la persistencia de datos en PostgreSQL.

La configuración de Hibernate puede actualizar el esquema de la base de datos automáticamente durante el desarrollo. Para producción, conviene revisar esta configuración y utilizar migraciones controladas.

## Seguridad

- Mantener las credenciales fuera del código fuente.
- Utilizar variables de entorno para la configuración sensible.
- Generar una clave JWT segura y privada para cada entorno.
- No compartir archivos de configuración que contengan secretos.
- Revisar los permisos de la base de datos y la configuración de Spring Security antes del despliegue.

Si una contraseña o clave secreta ya se ha publicado en GitHub, debe considerarse expuesta y sustituirse.

## Integración continua

El repositorio contiene un archivo `Jenkinsfile` para definir un proceso de integración continua. La configuración concreta de las etapas depende del contenido de ese archivo y del entorno de Jenkins.

## Estado del proyecto

Proyecto en desarrollo. La documentación de endpoints, ejemplos de peticiones y procedimientos de despliegue se ampliará conforme se implementen y verifiquen las funcionalidades.

## Autor

**Hernest**

GitHub: [@herneshem](https://github.com/herneshem)

## Licencia

No se ha especificado una licencia. Añade una si deseas establecer formalmente las condiciones de uso y distribución del proyecto.
