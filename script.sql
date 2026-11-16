-- Autos Colombia · Script de instalación (MySQL 8)
-- Crea la base de datos y las 4 tablas completas, tal como las usa Spring Boot.
-- Incluye la Iteración 2: usuarios.activo (baja lógica) y celdas.fuera_de_servicio.
-- Las 80 celdas (70 carro + 10 moto) las crea la aplicación sola al arrancar
-- por primera vez (ServicioParqueadero.inicializarCeldas).

CREATE DATABASE IF NOT EXISTS autos_colombia
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE autos_colombia;

CREATE TABLE IF NOT EXISTS usuarios (
  id_usuario  BIGINT       NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(120) NOT NULL,
  documento   VARCHAR(30)  NOT NULL,
  rol         VARCHAR(20)  NULL DEFAULT 'OPERADOR',
  activo      TINYINT(1)   NOT NULL DEFAULT 1,
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uk_usuarios_documento (documento)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vehiculos (
  id_vehiculo   BIGINT      NOT NULL AUTO_INCREMENT,
  placa         VARCHAR(10) NOT NULL,
  marca         VARCHAR(60) NOT NULL,
  color         VARCHAR(40) NOT NULL,
  tipo_vehiculo ENUM('CARRO','MOTO') NOT NULL,
  id_usuario    BIGINT      NOT NULL,
  PRIMARY KEY (id_vehiculo),
  UNIQUE KEY uk_vehiculos_placa (placa),
  CONSTRAINT fk_vehiculos_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS celdas (
  id_celda      BIGINT      NOT NULL AUTO_INCREMENT,
  codigo        VARCHAR(10) NOT NULL,
  tipo_vehiculo ENUM('CARRO','MOTO') NOT NULL,
  disponible    BOOLEAN     NOT NULL DEFAULT TRUE,
  fuera_de_servicio TINYINT(1) NOT NULL DEFAULT 0,
  vehiculo_id   BIGINT      NULL,
  PRIMARY KEY (id_celda),
  UNIQUE KEY uk_celdas_codigo (codigo),
  UNIQUE KEY uk_celdas_vehiculo (vehiculo_id),
  CONSTRAINT fk_celdas_vehiculo
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos (id_vehiculo)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ocupaciones (
  id_ocupacion        BIGINT   NOT NULL AUTO_INCREMENT,
  id_vehiculo         BIGINT   NOT NULL,
  id_celda            BIGINT   NOT NULL,
  fecha_hora_entrada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_hora_salida   DATETIME NULL,
  puerta_entrada      INT      NOT NULL,
  puerta_salida       INT      NULL,
  -- Columnas calculadas: guardan el id mientras la ocupación está ACTIVA
  -- (sin fecha de salida) y quedan en NULL cuando termina. Con el índice único
  -- se garantiza una sola ocupación activa por vehículo y por celda, aunque
  -- dos operarios registren al mismo tiempo.
  vehiculo_activo     BIGINT GENERATED ALWAYS AS
                        (IF(fecha_hora_salida IS NULL, id_vehiculo, NULL)) STORED,
  celda_activa        BIGINT GENERATED ALWAYS AS
                        (IF(fecha_hora_salida IS NULL, id_celda, NULL)) STORED,
  PRIMARY KEY (id_ocupacion),
  UNIQUE KEY uk_un_activo_por_vehiculo (vehiculo_activo),
  UNIQUE KEY uk_un_activo_por_celda (celda_activa),
  KEY idx_ocupaciones_entrada (fecha_hora_entrada),
  KEY idx_ocupaciones_salida (fecha_hora_salida),
  KEY idx_ocupaciones_vehiculo (id_vehiculo, fecha_hora_salida),
  CONSTRAINT fk_ocupaciones_vehiculo
    FOREIGN KEY (id_vehiculo) REFERENCES vehiculos (id_vehiculo),
  CONSTRAINT fk_ocupaciones_celda
    FOREIGN KEY (id_celda) REFERENCES celdas (id_celda),
  CONSTRAINT ck_puerta_entrada CHECK (puerta_entrada IN (1, 2)),
  CONSTRAINT ck_puerta_salida  CHECK (puerta_salida IS NULL OR puerta_salida IN (1, 2))
) ENGINE=InnoDB;