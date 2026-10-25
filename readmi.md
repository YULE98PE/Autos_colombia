<div align="center">

# 🚗 AUTOS COLOMBIA 🚗
### *Sistema de Gestión de Entradas y Salidas de Parqueadero*

---

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

</div>

---

## 📌 Descripción del Proyecto

Aplicación web para administrar el ingreso y la salida de vehículos de un parqueadero. Permite consultar la disponibilidad de celdas y ver cuáles están ocupadas desde una interfaz web.

---

## Tecnologías

- Java 17
- Spring Boot 3.2.4
- Spring Web y Spring Data JPA
- MySQL
- Maven

## Requisitos

- JDK 17
- Maven 3.6.3 o superior
- MySQL 8

## Configuración

1. Crea las tablas ejecutando [`script.sql`](script.sql) desde MySQL Workbench o
   desde el cliente MySQL. El script crea la base de datos `autos_colombia` y
   las tablas requeridas.
2. Configura la conexión en
   [`application.properties`](src/main/resources/application.properties):

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/autos_colombia
   spring.datasource.username=TU_USUARIO
   spring.datasource.password=TU_CONTRASENA
   ```

   Usa un usuario de MySQL con permisos sobre `autos_colombia`. No guardes
   contraseñas reales en archivos que vayas a compartir o subir al repositorio.
3. Verifica que MySQL esté iniciado y que la base de datos esté disponible.

Hibernate tiene `ddl-auto=update` configurado en las propiedades actuales:
actualiza el esquema al iniciar la aplicación. El script SQL permite crear las
tablas explícitamente antes del primer inicio.

## Ejecutar

### Opción 1: Desde el editor (VS Code / IDE)
Abre el archivo `AutosColombiaApplication.java` (ubicado en `src/main/java/...`) y haz clic en **Run** (o presiona `F5`).

Desde la raíz del proyecto:

```bash
mvn spring-boot:run
```

También puedes compilar un JAR y ejecutarlo:

```bash
mvn clean package
java -jar target/autos-colombia-1.0-SNAPSHOT.jar
```

La aplicación usa el puerto predeterminado de Spring Boot (`8080`). Abre
[http://localhost:8080](http://localhost:8080) para acceder a la interfaz.

Al iniciar, si la tabla `celdas` está vacía, se crean automáticamente 70 celdas
para carros (`C-01` a `C-70`) y 10 para motos (`M-01` a `M-10`).

## API

La API está bajo `/api`:

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/api/resumen` | Cantidad de celdas disponibles por tipo. |
| `GET` | `/api/celdas` | Lista las celdas y su ocupación actual. |
| `POST` | `/api/ingreso` | Registra el ingreso de un vehículo. |
| `POST` | `/api/salida/{codigo}` | Registra la salida del vehículo en esa celda. |

Ejemplo de cuerpo para `POST /api/ingreso`:

```json
{
  "placa": "ABC123",
  "marca": "Toyota",
  "color": "Gris",
  "tipo": "CARRO",
  "cedula": "123456789",
  "nombrePropietario": "Nombre Apellido",
  "puerta": "1"
}
```

`tipo` debe ser `CARRO` o `MOTO`, y `puerta` debe ser `1` o `2`. Para registrar
una salida, usa el código de la celda, por ejemplo `POST /api/salida/C-01`.