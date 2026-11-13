-- =====================================================================
--  SIGA - Módulo de Login  |  Script de base de datos para MySQL 8.x
--  (Compatible con MariaDB 10.4+ / XAMPP)
--  Ejecutar COMPLETO como root en phpMyAdmin (pestaña SQL), MySQL
--  Workbench o la consola:
--      mysql -u root -p < scriptMysql.sql
--  En XAMPP (root sin clave):
--      C:\xampp\mysql\bin\mysql.exe -u root < scriptMysql.sql
--  Convención: tablas y columnas en camelCase y en español.
--
--  CORRECCIONES respecto a la versión anterior:
--   * Paso 0: repara las tablas internas de permisos. En XAMPP/MariaDB
--     suelen dañarse (apagado brusco) y el GRANT fallaba con
--     "#1034 Index for table 'db' is corrupt", dejando al usuario
--     'usuarioSiga' SIN permisos -> la app respondía "error interno".
--   * El usuario de la aplicación se crea para 'localhost' y '127.0.0.1'
--     (según la configuración del servidor, la conexión TCP puede llegar
--     con cualquiera de los dos nombres).
--   * Al final se verifican los permisos con SHOW GRANTS.
-- =====================================================================

-- 0) Reparar las tablas de permisos (si están sanas responde "OK";
--    en MySQL 8 son InnoDB y solo muestra una nota informativa).
REPAIR TABLE mysql.db;
REPAIR TABLE mysql.tables_priv;
REPAIR TABLE mysql.columns_priv;
REPAIR TABLE mysql.procs_priv;
REPAIR TABLE mysql.proxies_priv;
FLUSH PRIVILEGES;

-- 1) Crear la base de datos con soporte completo para tildes, ñ y emojis
DROP DATABASE IF EXISTS dbsigalogin;
CREATE DATABASE dbsigalogin
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE dbsigalogin;

-- 2) Tabla de usuarios
CREATE TABLE usuario (
  idUsuario       INT           NOT NULL AUTO_INCREMENT,
  nombreCompleto  VARCHAR(120)  NOT NULL,
  documento       VARCHAR(20)   NOT NULL,
  correo          VARCHAR(120)  NOT NULL,
  claveHash       VARCHAR(255)  NOT NULL COMMENT 'Hash BCrypt, nunca la clave en texto plano',
  tipoUsuario     VARCHAR(20)   NOT NULL,
  estado          TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1 = activo, 0 = inactivo',
  fechaCreacion   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ultimoIngreso   DATETIME      NULL,
  CONSTRAINT pkUsuario            PRIMARY KEY (idUsuario),
  CONSTRAINT ukUsuarioDocumento   UNIQUE (documento),
  CONSTRAINT ukUsuarioCorreo      UNIQUE (correo),
  CONSTRAINT ckUsuarioTipo        CHECK (tipoUsuario IN ('Administrador','Coordinador','Directivo','Secretario','Docente'))
) ENGINE = InnoDB;

-- 3) Tabla de tokens de recuperación de contraseña
--    Se guarda el HASH SHA-256 del token, no el token. Si alguien roba la
--    base de datos no puede usar los enlaces enviados por correo.
CREATE TABLE tokenRecuperacion (
  idToken          INT        NOT NULL AUTO_INCREMENT,
  idUsuario        INT        NOT NULL,
  tokenHash        CHAR(64)   NOT NULL,
  fechaExpiracion  DATETIME   NOT NULL,
  usado            TINYINT(1) NOT NULL DEFAULT 0,
  fechaCreacion    DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pkTokenRecuperacion        PRIMARY KEY (idToken),
  CONSTRAINT ukTokenRecuperacionHash    UNIQUE (tokenHash),
  CONSTRAINT fkTokenRecuperacionUsuario FOREIGN KEY (idUsuario)
      REFERENCES usuario (idUsuario) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE INDEX ixTokenRecuperacionUsuario ON tokenRecuperacion (idUsuario);

-- 4) Usuario de prueba  (correo: admin@siga.edu.co  |  clave: Admin123*)
INSERT INTO usuario (nombreCompleto, documento, correo, claveHash, tipoUsuario)
VALUES ('Administrador General', '1000000001', 'admin@siga.edu.co',
        '$2a$10$KxEd1CBaugbBR/w.8ixFcu7Nwd2.698UqFRy8eZS3IIrB.5elko/O', 'Administrador');

-- 5) Usuario exclusivo para la aplicación, con permisos mínimos.
--    Así el backend NO se conecta como root.
DROP USER IF EXISTS 'usuarioSiga'@'localhost';
DROP USER IF EXISTS 'usuarioSiga'@'127.0.0.1';
CREATE USER 'usuarioSiga'@'localhost' IDENTIFIED BY 'CAMBIAR_ANTES_DE_EJECUTAR_EL_SCRIPT';
CREATE USER 'usuarioSiga'@'127.0.0.1' IDENTIFIED BY 'CAMBIAR_ANTES_DE_EJECUTAR_EL_SCRIPT';
GRANT SELECT, INSERT, UPDATE, DELETE ON dbsigalogin.* TO 'usuarioSiga'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON dbsigalogin.* TO 'usuarioSiga'@'127.0.0.1';
FLUSH PRIVILEGES;

-- 6) Verificación
--    Debe aparecer: GRANT SELECT, INSERT, UPDATE, DELETE ON `dbsigalogin`.* ...
--    Si solo aparece "GRANT USAGE ON *.*", el GRANT no se aplicó.
SHOW GRANTS FOR 'usuarioSiga'@'localhost';
SELECT idUsuario, nombreCompleto, correo, tipoUsuario, estado FROM usuario;
