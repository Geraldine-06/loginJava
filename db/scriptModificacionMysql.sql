-- =====================================================================
--  SIGA - Taller 2: Dashboard con CRUD  |  SCRIPT DE MODIFICACIÓN (MySQL 8.x)
--  (Compatible con MariaDB 10.4+ / XAMPP y con MySQL 8 de AMPPS)
--
--  ¿Qué hace?  Agrega a la base 'dbsigalogin' (creada en el taller 1 con
--  scriptMysql.sql) las tablas del diagrama dbsiga2022.png que usa el
--  dashboard:  funcionario, alumno, area y materia.
--  NO borra la tabla 'usuario' ni los tokens: el login sigue funcionando.
--
--  Ejecutar COMPLETO como root DESPUÉS de scriptMysql.sql:
--    AMPPS  (root / mysql):
--      "C:\Program Files\Ampps\mysql\bin\mysql.exe" -u root -p < scriptModificacionMysql.sql
--    XAMPP  (root sin clave):
--      C:\xampp\mysql\bin\mysql.exe -u root < scriptModificacionMysql.sql
--    o en phpMyAdmin / Workbench: pestaña SQL, pegar y ejecutar.
--
--  Nombres: los del diagrama pasados a camelCase
--    doc_funcionario -> docFuncionario,  H_semanales_materia -> hSemanalesMateria
--
--  Ajustes respecto al diagrama original (dbsiga2022):
--   * La relación usuario 1 --- 0..1 funcionario se conserva: el documento
--     del funcionario es FK al documento del usuario. Así un funcionario
--     solo existe si tiene cuenta para ingresar al sistema.
--   * ON DELETE RESTRICT (y no CASCADE): no se puede borrar un usuario que
--     es funcionario, ni un área que tiene materias. La aplicación captura
--     ese error y muestra un mensaje claro (manejo de excepciones).
--   * Estados con CHECK ('Activo','Inactivo'); fecha de nacimiento DATE;
--     horas semanales numéricas; códigos de área y materia AUTO_INCREMENT.
--  El script se puede ejecutar varias veces (borra y crea las 4 tablas).
-- =====================================================================

USE dbsigalogin;

-- 1) Borrar las tablas nuevas si ya existían (primero las "hijas")
DROP TABLE IF EXISTS materia;
DROP TABLE IF EXISTS area;
DROP TABLE IF EXISTS alumno;
DROP TABLE IF EXISTS funcionario;

-- 2) Tabla funcionario  (relación 1 a 1 con usuario por el documento)
CREATE TABLE funcionario (
  docFuncionario        VARCHAR(20)   NOT NULL,
  nomFuncionario        VARCHAR(120)  NOT NULL,
  mailFuncionario       VARCHAR(120)  NOT NULL,
  profFuncionario       VARCHAR(150)  NULL     COMMENT 'Profesión',
  direcFuncionario      VARCHAR(150)  NULL,
  teleFuncionario       VARCHAR(20)   NULL     COMMENT 'Teléfono fijo',
  telemovilFuncionario  VARCHAR(20)   NULL     COMMENT 'Celular',
  estadoFuncionario     VARCHAR(10)   NOT NULL DEFAULT 'Activo',
  cargoFuncionario      VARCHAR(30)   NOT NULL,
  sedeFuncionario       VARCHAR(40)   NULL,
  CONSTRAINT pkFuncionario        PRIMARY KEY (docFuncionario),
  CONSTRAINT ukFuncionarioCorreo  UNIQUE (mailFuncionario),
  CONSTRAINT ckFuncionarioEstado  CHECK (estadoFuncionario IN ('Activo','Inactivo')),
  CONSTRAINT fkFuncionarioUsuario FOREIGN KEY (docFuncionario)
      REFERENCES usuario (documento) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;

-- 3) Tabla alumno
CREATE TABLE alumno (
  docAlumno       VARCHAR(20)   NOT NULL,
  nomAlumno       VARCHAR(120)  NOT NULL,
  mailAlumno      VARCHAR(120)  NULL,
  direccAlumno    VARCHAR(150)  NULL,
  teleAlumno      VARCHAR(20)   NULL,
  estadoAlumno    VARCHAR(10)   NOT NULL DEFAULT 'Activo',
  sexoAlumno      VARCHAR(10)   NOT NULL,
  fechanacAlumno  DATE          NULL     COMMENT 'Fecha de nacimiento',
  CONSTRAINT pkAlumno        PRIMARY KEY (docAlumno),
  CONSTRAINT ckAlumnoEstado  CHECK (estadoAlumno IN ('Activo','Inactivo')),
  CONSTRAINT ckAlumnoSexo    CHECK (sexoAlumno IN ('Femenino','Masculino','Otro'))
) ENGINE = InnoDB;

-- 4) Tabla area
CREATE TABLE area (
  codArea     INT           NOT NULL AUTO_INCREMENT,
  nomArea     VARCHAR(120)  NOT NULL,
  estadoArea  VARCHAR(10)   NOT NULL DEFAULT 'Activo',
  CONSTRAINT pkArea        PRIMARY KEY (codArea),
  CONSTRAINT ukAreaNombre  UNIQUE (nomArea),
  CONSTRAINT ckAreaEstado  CHECK (estadoArea IN ('Activo','Inactivo'))
) ENGINE = InnoDB;

-- 5) Tabla materia  (muchas materias pertenecen a un área)
CREATE TABLE materia (
  codMateria         INT           NOT NULL AUTO_INCREMENT,
  nomMateria         VARCHAR(120)  NOT NULL,
  codArea            INT           NOT NULL,
  cicloMateria       VARCHAR(20)   NOT NULL COMMENT 'Ciclos donde se dicta, ej: 1-2-3',
  estadoMateria      VARCHAR(10)   NOT NULL DEFAULT 'Activo',
  ordenMateria       INT           NOT NULL DEFAULT 1 COMMENT 'Orden en el boletín',
  hSemanalesMateria  TINYINT       NULL     COMMENT 'Horas semanales',
  CONSTRAINT pkMateria          PRIMARY KEY (codMateria),
  CONSTRAINT ukMateriaNombre    UNIQUE (nomMateria),
  CONSTRAINT ckMateriaEstado    CHECK (estadoMateria IN ('Activo','Inactivo')),
  CONSTRAINT ckMateriaHoras     CHECK (hSemanalesMateria IS NULL OR hSemanalesMateria BETWEEN 1 AND 40),
  CONSTRAINT fkMateriaArea      FOREIGN KEY (codArea)
      REFERENCES area (codArea) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE INDEX ixMateriaArea ON materia (codArea);

-- 6) Usuario COORDINADOR de prueba (es el único perfil que puede crear,
--    editar y eliminar en el dashboard).  Clave: Admin123*
DELETE FROM usuario WHERE correo = 'coordinador@siga.edu.co' OR documento = '1000000002';
INSERT INTO usuario (nombreCompleto, documento, correo, claveHash, tipoUsuario)
VALUES ('Coordinador Académico', '1000000002', 'coordinador@siga.edu.co',
        '$2a$10$KxEd1CBaugbBR/w.8ixFcu7Nwd2.698UqFRy8eZS3IIrB.5elko/O', 'Coordinador');

-- 7) Datos de ejemplo
--    Funcionarios: uno por cada usuario de prueba (su documento debe existir en 'usuario')
INSERT INTO funcionario (docFuncionario, nomFuncionario, mailFuncionario, profFuncionario,
                         direcFuncionario, teleFuncionario, telemovilFuncionario,
                         estadoFuncionario, cargoFuncionario, sedeFuncionario)
SELECT documento, nombreCompleto, correo, 'Ingeniero de sistemas', 'Carrera 14 # 15-10',
       '6018760492', '3001234567', 'Activo', tipoUsuario, 'Principal'
FROM usuario
WHERE documento IN ('1000000001', '1000000002');

INSERT INTO area (codArea, nomArea, estadoArea) VALUES
 (1, 'Ciencias Naturales y Educación Ambiental', 'Activo'),
 (2, 'Ciencias Sociales, Historia, Geografía, Constitución Política y Democracia', 'Activo'),
 (3, 'Matemáticas y Geometría', 'Activo'),
 (4, 'Humanidades Lengua Castellana e Idiomas', 'Activo'),
 (5, 'Educación Física, Recreación y Deportes', 'Activo'),
 (6, 'Comportamiento Escolar', 'Activo'),
 (7, 'Tecnología e Informática', 'Activo'),
 (8, 'Educación Ética y Valores Humanos', 'Activo'),
 (9, 'Educación Artística', 'Activo'),
 (10, 'Educación Religiosa', 'Activo');

INSERT INTO materia (codMateria, nomMateria, codArea, cicloMateria, estadoMateria, ordenMateria, hSemanalesMateria) VALUES
 (1, 'Biología', 1, '3-4', 'Activo', 10, 3),
 (2, 'Química', 1, '5-6', 'Activo', 11, 3),
 (3, 'Ciencias Sociales, Historia y Geografía', 2, '1-2-3-4', 'Activo', 13, 3),
 (4, 'Constitución Política y Democracia', 2, '3-4-5-6', 'Activo', 15, 1),
 (5, 'Filosofía', 2, '5-6', 'Activo', 14, 2),
 (6, 'Ética', 8, '1-2-3-4-5-6', 'Activo', 19, 1),
 (7, 'Religión', 10, '3-4-5-6', 'Activo', 20, 1),
 (8, 'Matemáticas', 3, '1-2-3-4-5-6', 'Activo', 25, 3),
 (9, 'Física', 1, '5-6', 'Activo', 12, 3),
 (10, 'Informática', 7, '3-4-5-6', 'Activo', 26, 3),
 (11, 'Educación Física, Recreación y Deporte', 5, '3-4-5-6', 'Activo', 22, 1),
 (12, 'Educación Artística', 9, '3-4-5-6', 'Activo', 21, 1),
 (13, 'Cívica', 2, '3-4-5-6', 'Activo', 17, 1),
 (14, 'Español', 4, '1-2-3-4-5-6', 'Activo', 23, 3),
 (15, 'Inglés', 4, '3-4-5-6', 'Activo', 24, 2),
 (16, 'Convivencia', 6, '1-2-3-4-5-6', 'Activo', 27, NULL),
 (17, 'Economía', 2, '5-6', 'Activo', 16, 1),
 (18, 'Ciencias Naturales', 1, '1-2', 'Activo', 9, 4),
 (19, 'Cátedra de la Paz', 2, '1-2-3-4-5-6', 'Activo', 18, 1);

-- Alumnos de ejemplo (datos ficticios)
INSERT INTO alumno (docAlumno, nomAlumno, mailAlumno, direccAlumno, teleAlumno, estadoAlumno, sexoAlumno, fechanacAlumno) VALUES
 ('1011000001', 'GÓMEZ RUIZ LAURA VALENTINA', 'laura.gomez@correo.com', 'Calle 10 # 5-20', '3104567890', 'Activo', 'Femenino', '2010-03-14'),
 ('1011000002', 'PÉREZ DÍAZ JUAN DAVID', 'juan.perez@correo.com', 'Carrera 8 # 12-40', '3115678901', 'Activo', 'Masculino', '2009-07-22'),
 ('1011000003', 'MARTÍNEZ ROJAS SOFÍA', 'sofia.martinez@correo.com', 'Calle 3 # 9-15', '3126789012', 'Activo', 'Femenino', '2011-11-02'),
 ('1011000004', 'RODRÍGUEZ LEÓN ANDRÉS FELIPE', 'andres.rodriguez@correo.com', 'Diagonal 6 # 2-30', '3137890123', 'Inactivo', 'Masculino', '2008-01-30'),
 ('1011000005', 'CASTRO NIÑO MARÍA JOSÉ', 'maria.castro@correo.com', 'Transversal 4 # 7-11', '3148901234', 'Activo', 'Femenino', '2010-09-09');

-- 8) Verificación
--    'usuarioSiga' (creado en el taller 1) ya tiene permisos sobre dbsigalogin.*,
--    por eso las tablas nuevas no necesitan otro GRANT.
SELECT 'funcionario' AS tabla, COUNT(*) AS registros FROM funcionario
UNION ALL SELECT 'alumno',  COUNT(*) FROM alumno
UNION ALL SELECT 'area',    COUNT(*) FROM area
UNION ALL SELECT 'materia', COUNT(*) FROM materia
UNION ALL SELECT 'usuario', COUNT(*) FROM usuario;
