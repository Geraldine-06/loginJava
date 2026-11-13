-- =====================================================================
--  SIGA - Módulo de Login  |  Script de base de datos para SQL Server
--  (SQL Server 2019 / 2022 / Express)
--  Ejecutar COMPLETO (F5) en SQL Server Management Studio (SSMS), en la
--  extensión "SQL Server (mssql)" de VS Code, o en la consola:
--      sqlcmd -S .\SQLEXPRESS -E -i scriptSqlServer.sql
--  Convención: tablas y columnas en camelCase y en español.
--
--  CORRECCIONES respecto a la versión anterior:
--   * Avisa si el servidor NO está en "Modo de autenticación mixto"
--     (sin él, el login 'usuarioSiga' se crea pero nunca podrá entrar).
--   * Cierra las sesiones abiertas de 'usuarioSiga' antes de borrar el
--     login (antes el DROP LOGIN fallaba si la app estaba corriendo).
--   * El login queda con base de datos predeterminada 'dbsigalogin'.
--   * Al final se verifican los permisos del usuario.
-- =====================================================================

-- 0) Verificar el modo de autenticación
USE master;
GO
IF SERVERPROPERTY('IsIntegratedSecurityOnly') = 1
    PRINT N'ADVERTENCIA: el servidor solo acepta autenticación de Windows. '
        + N'Active "SQL Server and Windows Authentication mode" '
        + N'(Propiedades del servidor > Seguridad) y reinicie el servicio; '
        + N'si no, usuarioSiga no podrá conectarse.';
GO

-- 1) Crear la base de datos
IF DB_ID('dbsigalogin') IS NOT NULL
BEGIN
    ALTER DATABASE dbsigalogin SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE dbsigalogin;
END
GO
CREATE DATABASE dbsigalogin;
GO
USE dbsigalogin;
GO

-- 2) Tabla de usuarios  (NVARCHAR = texto Unicode: tildes y ñ)
CREATE TABLE dbo.usuario (
    idUsuario       INT IDENTITY(1,1) NOT NULL,
    nombreCompleto  NVARCHAR(120)     NOT NULL,
    documento       NVARCHAR(20)      NOT NULL,
    correo          NVARCHAR(120)     NOT NULL,
    claveHash       NVARCHAR(255)     NOT NULL,   -- Hash BCrypt
    tipoUsuario     NVARCHAR(20)      NOT NULL,
    estado          BIT               NOT NULL CONSTRAINT dfUsuarioEstado DEFAULT 1,
    fechaCreacion   DATETIME2(0)      NOT NULL CONSTRAINT dfUsuarioFechaCreacion DEFAULT SYSDATETIME(),
    ultimoIngreso   DATETIME2(0)      NULL,
    CONSTRAINT pkUsuario           PRIMARY KEY (idUsuario),
    CONSTRAINT ukUsuarioDocumento  UNIQUE (documento),
    CONSTRAINT ukUsuarioCorreo     UNIQUE (correo),
    CONSTRAINT ckUsuarioTipo       CHECK (tipoUsuario IN (N'Administrador',N'Coordinador',N'Directivo',N'Secretario',N'Docente'))
);
GO

-- 3) Tabla de tokens de recuperación (se guarda el hash SHA-256 del token)
CREATE TABLE dbo.tokenRecuperacion (
    idToken          INT IDENTITY(1,1) NOT NULL,
    idUsuario        INT               NOT NULL,
    tokenHash        CHAR(64)          NOT NULL,
    fechaExpiracion  DATETIME2(0)      NOT NULL,
    usado            BIT               NOT NULL CONSTRAINT dfTokenUsado DEFAULT 0,
    fechaCreacion    DATETIME2(0)      NOT NULL CONSTRAINT dfTokenFechaCreacion DEFAULT SYSDATETIME(),
    CONSTRAINT pkTokenRecuperacion        PRIMARY KEY (idToken),
    CONSTRAINT ukTokenRecuperacionHash    UNIQUE (tokenHash),
    CONSTRAINT fkTokenRecuperacionUsuario FOREIGN KEY (idUsuario)
        REFERENCES dbo.usuario (idUsuario) ON DELETE CASCADE ON UPDATE CASCADE
);
GO
CREATE INDEX ixTokenRecuperacionUsuario ON dbo.tokenRecuperacion (idUsuario);
GO

-- 4) Usuario de prueba  (correo: admin@siga.edu.co  |  clave: Admin123*)
INSERT INTO dbo.usuario (nombreCompleto, documento, correo, claveHash, tipoUsuario)
VALUES (N'Administrador General', N'1000000001', N'admin@siga.edu.co',
        N'$2a$10$KxEd1CBaugbBR/w.8ixFcu7Nwd2.698UqFRy8eZS3IIrB.5elko/O', N'Administrador');
GO

-- 5) Inicio de sesión SQL exclusivo para la aplicación (permisos mínimos).
USE master;
GO
-- 5.1) Cerrar sesiones abiertas del login (si la app está corriendo)
DECLARE @sesion INT, @sql NVARCHAR(50);
DECLARE cursorSesiones CURSOR LOCAL FAST_FORWARD FOR
    SELECT session_id FROM sys.dm_exec_sessions WHERE login_name = 'usuarioSiga';
OPEN cursorSesiones;
FETCH NEXT FROM cursorSesiones INTO @sesion;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @sql = N'KILL ' + CAST(@sesion AS NVARCHAR(10));
    EXEC (@sql);
    FETCH NEXT FROM cursorSesiones INTO @sesion;
END
CLOSE cursorSesiones;
DEALLOCATE cursorSesiones;
GO
-- 5.2) Volver a crear el login
IF EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'usuarioSiga')
    DROP LOGIN usuarioSiga;
GO
CREATE LOGIN usuarioSiga
    WITH PASSWORD = 'CAMBIAR_ANTES_DE_EJECUTAR_EL_SCRIPT',
         DEFAULT_DATABASE = dbsigalogin,
         CHECK_POLICY = OFF;
GO
-- 5.3) Usuario dentro de la base de datos con lectura y escritura
USE dbsigalogin;
GO
CREATE USER usuarioSiga FOR LOGIN usuarioSiga;
ALTER ROLE db_datareader ADD MEMBER usuarioSiga;
ALTER ROLE db_datawriter ADD MEMBER usuarioSiga;
GO

-- 6) Verificación
--    Deben aparecer los roles db_datareader y db_datawriter.
SELECT usuarioBd.name AS usuario, rol.name AS rol
FROM sys.database_role_members AS miembro
JOIN sys.database_principals AS rol       ON rol.principal_id = miembro.role_principal_id
JOIN sys.database_principals AS usuarioBd ON usuarioBd.principal_id = miembro.member_principal_id
WHERE usuarioBd.name = 'usuarioSiga';

SELECT idUsuario, nombreCompleto, correo, tipoUsuario, estado FROM dbo.usuario;
GO
