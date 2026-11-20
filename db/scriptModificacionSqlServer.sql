-- =====================================================================
--  SIGA - Taller 2: Dashboard con CRUD  |  SCRIPT DE MODIFICACIÓN (SQL Server)
--  Agrega a 'dbsigalogin' las tablas funcionario, alumno, area y materia
--  del diagrama dbsiga2022.png. Ejecutar DESPUÉS de scriptSqlServer.sql,
--  en SSMS (botón Ejecutar) o con:
--      sqlcmd -S .\SQLEXPRESS -E -i scriptModificacionSqlServer.sql
--  Las explicaciones de cada ajuste están en scriptModificacionMysql.sql
-- =====================================================================
USE dbsigalogin;
GO

-- 1) Borrar las tablas nuevas si ya existían (primero las "hijas")
DROP TABLE IF EXISTS dbo.materia;
DROP TABLE IF EXISTS dbo.area;
DROP TABLE IF EXISTS dbo.alumno;
DROP TABLE IF EXISTS dbo.funcionario;
GO

-- 2) funcionario (1 a 1 con usuario por el documento)
CREATE TABLE dbo.funcionario (
    docFuncionario        NVARCHAR(20)   NOT NULL,
    nomFuncionario        NVARCHAR(120)  NOT NULL,
    mailFuncionario       NVARCHAR(120)  NOT NULL,
    profFuncionario       NVARCHAR(150)  NULL,
    direcFuncionario      NVARCHAR(150)  NULL,
    teleFuncionario       NVARCHAR(20)   NULL,
    telemovilFuncionario  NVARCHAR(20)   NULL,
    estadoFuncionario     NVARCHAR(10)   NOT NULL CONSTRAINT dfFuncionarioEstado DEFAULT N'Activo',
    cargoFuncionario      NVARCHAR(30)   NOT NULL,
    sedeFuncionario       NVARCHAR(40)   NULL,
    CONSTRAINT pkFuncionario        PRIMARY KEY (docFuncionario),
    CONSTRAINT ukFuncionarioCorreo  UNIQUE (mailFuncionario),
    CONSTRAINT ckFuncionarioEstado  CHECK (estadoFuncionario IN (N'Activo', N'Inactivo')),
    CONSTRAINT fkFuncionarioUsuario FOREIGN KEY (docFuncionario)
        REFERENCES dbo.usuario (documento) ON DELETE NO ACTION ON UPDATE CASCADE
);
GO

-- 3) alumno
CREATE TABLE dbo.alumno (
    docAlumno       NVARCHAR(20)   NOT NULL,
    nomAlumno       NVARCHAR(120)  NOT NULL,
    mailAlumno      NVARCHAR(120)  NULL,
    direccAlumno    NVARCHAR(150)  NULL,
    teleAlumno      NVARCHAR(20)   NULL,
    estadoAlumno    NVARCHAR(10)   NOT NULL CONSTRAINT dfAlumnoEstado DEFAULT N'Activo',
    sexoAlumno      NVARCHAR(10)   NOT NULL,
    fechanacAlumno  DATE           NULL,
    CONSTRAINT pkAlumno        PRIMARY KEY (docAlumno),
    CONSTRAINT ckAlumnoEstado  CHECK (estadoAlumno IN (N'Activo', N'Inactivo')),
    CONSTRAINT ckAlumnoSexo    CHECK (sexoAlumno IN (N'Femenino', N'Masculino', N'Otro'))
);
GO

-- 4) area
CREATE TABLE dbo.area (
    codArea     INT IDENTITY(1,1) NOT NULL,
    nomArea     NVARCHAR(120)     NOT NULL,
    estadoArea  NVARCHAR(10)      NOT NULL CONSTRAINT dfAreaEstado DEFAULT N'Activo',
    CONSTRAINT pkArea        PRIMARY KEY (codArea),
    CONSTRAINT ukAreaNombre  UNIQUE (nomArea),
    CONSTRAINT ckAreaEstado  CHECK (estadoArea IN (N'Activo', N'Inactivo'))
);
GO

-- 5) materia
CREATE TABLE dbo.materia (
    codMateria         INT IDENTITY(1,1) NOT NULL,
    nomMateria         NVARCHAR(120)     NOT NULL,
    codArea            INT               NOT NULL,
    cicloMateria       NVARCHAR(20)      NOT NULL,
    estadoMateria      NVARCHAR(10)      NOT NULL CONSTRAINT dfMateriaEstado DEFAULT N'Activo',
    ordenMateria       INT               NOT NULL CONSTRAINT dfMateriaOrden DEFAULT 1,
    hSemanalesMateria  TINYINT           NULL,
    CONSTRAINT pkMateria        PRIMARY KEY (codMateria),
    CONSTRAINT ukMateriaNombre  UNIQUE (nomMateria),
    CONSTRAINT ckMateriaEstado  CHECK (estadoMateria IN (N'Activo', N'Inactivo')),
    CONSTRAINT ckMateriaHoras   CHECK (hSemanalesMateria IS NULL OR hSemanalesMateria BETWEEN 1 AND 40),
    CONSTRAINT fkMateriaArea    FOREIGN KEY (codArea)
        REFERENCES dbo.area (codArea) ON DELETE NO ACTION ON UPDATE CASCADE
);
GO
CREATE INDEX ixMateriaArea ON dbo.materia (codArea);
GO

-- 6) Usuario COORDINADOR de prueba (clave: Admin123*)
DELETE FROM dbo.usuario WHERE correo = N'coordinador@siga.edu.co' OR documento = N'1000000002';
INSERT INTO dbo.usuario (nombreCompleto, documento, correo, claveHash, tipoUsuario)
VALUES (N'Coordinador Académico', N'1000000002', N'coordinador@siga.edu.co',
        N'$2a$10$KxEd1CBaugbBR/w.8ixFcu7Nwd2.698UqFRy8eZS3IIrB.5elko/O', N'Coordinador');
GO

-- 7) Datos de ejemplo
INSERT INTO dbo.funcionario (docFuncionario, nomFuncionario, mailFuncionario, profFuncionario,
                             direcFuncionario, teleFuncionario, telemovilFuncionario,
                             estadoFuncionario, cargoFuncionario, sedeFuncionario)
SELECT documento, nombreCompleto, correo, N'Ingeniero de sistemas', N'Carrera 14 # 15-10',
       N'6018760492', N'3001234567', N'Activo', tipoUsuario, N'Principal'
FROM dbo.usuario
WHERE documento IN (N'1000000001', N'1000000002');
GO

SET IDENTITY_INSERT dbo.area ON;   -- permite escribir el código a mano
INSERT INTO dbo.area (codArea, nomArea, estadoArea) VALUES
 (1, N'Ciencias Naturales y Educación Ambiental', N'Activo'),
 (2, N'Ciencias Sociales, Historia, Geografía, Constitución Política y Democracia', N'Activo'),
 (3, N'Matemáticas y Geometría', N'Activo'),
 (4, N'Humanidades Lengua Castellana e Idiomas', N'Activo'),
 (5, N'Educación Física, Recreación y Deportes', N'Activo'),
 (6, N'Comportamiento Escolar', N'Activo'),
 (7, N'Tecnología e Informática', N'Activo'),
 (8, N'Educación Ética y Valores Humanos', N'Activo'),
 (9, N'Educación Artística', N'Activo'),
 (10, N'Educación Religiosa', N'Activo');
SET IDENTITY_INSERT dbo.area OFF;
GO

SET IDENTITY_INSERT dbo.materia ON;
INSERT INTO dbo.materia (codMateria, nomMateria, codArea, cicloMateria, estadoMateria, ordenMateria, hSemanalesMateria) VALUES
 (1, N'Biología', 1, N'3-4', N'Activo', 10, 3),
 (2, N'Química', 1, N'5-6', N'Activo', 11, 3),
 (3, N'Ciencias Sociales, Historia y Geografía', 2, N'1-2-3-4', N'Activo', 13, 3),
 (4, N'Constitución Política y Democracia', 2, N'3-4-5-6', N'Activo', 15, 1),
 (5, N'Filosofía', 2, N'5-6', N'Activo', 14, 2),
 (6, N'Ética', 8, N'1-2-3-4-5-6', N'Activo', 19, 1),
 (7, N'Religión', 10, N'3-4-5-6', N'Activo', 20, 1),
 (8, N'Matemáticas', 3, N'1-2-3-4-5-6', N'Activo', 25, 3),
 (9, N'Física', 1, N'5-6', N'Activo', 12, 3),
 (10, N'Informática', 7, N'3-4-5-6', N'Activo', 26, 3),
 (11, N'Educación Física, Recreación y Deporte', 5, N'3-4-5-6', N'Activo', 22, 1),
 (12, N'Educación Artística', 9, N'3-4-5-6', N'Activo', 21, 1),
 (13, N'Cívica', 2, N'3-4-5-6', N'Activo', 17, 1),
 (14, N'Español', 4, N'1-2-3-4-5-6', N'Activo', 23, 3),
 (15, N'Inglés', 4, N'3-4-5-6', N'Activo', 24, 2),
 (16, N'Convivencia', 6, N'1-2-3-4-5-6', N'Activo', 27, NULL),
 (17, N'Economía', 2, N'5-6', N'Activo', 16, 1),
 (18, N'Ciencias Naturales', 1, N'1-2', N'Activo', 9, 4),
 (19, N'Cátedra de la Paz', 2, N'1-2-3-4-5-6', N'Activo', 18, 1);
SET IDENTITY_INSERT dbo.materia OFF;
GO

INSERT INTO dbo.alumno (docAlumno, nomAlumno, mailAlumno, direccAlumno, teleAlumno, estadoAlumno, sexoAlumno, fechanacAlumno) VALUES
 (N'1011000001', N'GÓMEZ RUIZ LAURA VALENTINA', N'laura.gomez@correo.com', N'Calle 10 # 5-20', N'3104567890', N'Activo', N'Femenino', '2010-03-14'),
 (N'1011000002', N'PÉREZ DÍAZ JUAN DAVID', N'juan.perez@correo.com', N'Carrera 8 # 12-40', N'3115678901', N'Activo', N'Masculino', '2009-07-22'),
 (N'1011000003', N'MARTÍNEZ ROJAS SOFÍA', N'sofia.martinez@correo.com', N'Calle 3 # 9-15', N'3126789012', N'Activo', N'Femenino', '2011-11-02'),
 (N'1011000004', N'RODRÍGUEZ LEÓN ANDRÉS FELIPE', N'andres.rodriguez@correo.com', N'Diagonal 6 # 2-30', N'3137890123', N'Inactivo', N'Masculino', '2008-01-30'),
 (N'1011000005', N'CASTRO NIÑO MARÍA JOSÉ', N'maria.castro@correo.com', N'Transversal 4 # 7-11', N'3148901234', N'Activo', N'Femenino', '2010-09-09');
GO

-- 8) Permisos: usuarioSiga es db_datareader + db_datawriter, ya cubre las tablas nuevas.
SELECT N'funcionario' AS tabla, COUNT(*) AS registros FROM dbo.funcionario
UNION ALL SELECT N'alumno',  COUNT(*) FROM dbo.alumno
UNION ALL SELECT N'area',    COUNT(*) FROM dbo.area
UNION ALL SELECT N'materia', COUNT(*) FROM dbo.materia
UNION ALL SELECT N'usuario', COUNT(*) FROM dbo.usuario;
GO
