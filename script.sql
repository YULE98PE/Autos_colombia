-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema autos_colombia
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema autos_colombia
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `autos_colombia` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `autos_colombia` ;

-- -----------------------------------------------------
-- Table `autos_colombia`.`usuarios`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `autos_colombia`.`usuarios` (
  `id_usuario` BIGINT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(120) NOT NULL,
  `documento` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE INDEX `documento` (`documento` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `autos_colombia`.`vehiculos`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `autos_colombia`.`vehiculos` (
  `id_vehiculo` BIGINT NOT NULL AUTO_INCREMENT,
  `placa` VARCHAR(10) NOT NULL,
  `marca` VARCHAR(60) NOT NULL,
  `color` VARCHAR(40) NOT NULL,
  `tipo_vehiculo` ENUM('CARRO', 'MOTO') NOT NULL,
  `id_usuario` BIGINT NOT NULL,
  PRIMARY KEY (`id_vehiculo`),
  UNIQUE INDEX `placa` (`placa` ASC) VISIBLE,
  INDEX `fk_vehiculo_usuario` (`id_usuario` ASC) VISIBLE,
  CONSTRAINT `fk_vehiculo_usuario`
    FOREIGN KEY (`id_usuario`)
    REFERENCES `autos_colombia`.`usuarios` (`id_usuario`))
ENGINE = InnoDB
AUTO_INCREMENT = 5
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `autos_colombia`.`celdas`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `autos_colombia`.`celdas` (
  `id_celda` BIGINT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(10) NOT NULL,
  `tipo_vehiculo` ENUM('CARRO', 'MOTO') NOT NULL,
  `disponible` TINYINT(1) NOT NULL DEFAULT '1',
  `vehiculo_id` BIGINT NULL DEFAULT NULL,
  PRIMARY KEY (`id_celda`),
  UNIQUE INDEX `codigo` (`codigo` ASC) VISIBLE,
  UNIQUE INDEX `UK_3tkh8y7c1uvwg3sl762wi8pg4` (`vehiculo_id` ASC) VISIBLE,
  CONSTRAINT `FK3plsa3qclnr01ejyp3x3cal4v`
    FOREIGN KEY (`vehiculo_id`)
    REFERENCES `autos_colombia`.`vehiculos` (`id_vehiculo`))
ENGINE = InnoDB
AUTO_INCREMENT = 143
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `autos_colombia`.`ocupaciones`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `autos_colombia`.`ocupaciones` (
  `id_ocupacion` BIGINT NOT NULL AUTO_INCREMENT,
  `id_vehiculo` BIGINT NOT NULL,
  `id_celda` BIGINT NOT NULL,
  `fecha_hora_entrada` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_hora_salida` DATETIME NULL DEFAULT NULL,
  `puerta_entrada` INT NOT NULL,
  `puerta_salida` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id_ocupacion`),
  INDEX `idx_ocupacion_vehiculo` (`id_vehiculo` ASC) VISIBLE,
  INDEX `idx_ocupacion_celda` (`id_celda` ASC) VISIBLE,
  INDEX `idx_ocupacion_salida` (`fecha_hora_salida` ASC) VISIBLE,
  CONSTRAINT `fk_ocupacion_celda`
    FOREIGN KEY (`id_celda`)
    REFERENCES `autos_colombia`.`celdas` (`id_celda`),
  CONSTRAINT `fk_ocupacion_vehiculo`
    FOREIGN KEY (`id_vehiculo`)
    REFERENCES `autos_colombia`.`vehiculos` (`id_vehiculo`))
ENGINE = InnoDB
AUTO_INCREMENT = 12
DEF