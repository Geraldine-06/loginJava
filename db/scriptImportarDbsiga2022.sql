-- =====================================================================
--  SIGA - Taller 2  |  IMPORTAR DATOS DE dbsiga2022 (OPCIONAL, MySQL)
--
--  Solo si en su servidor existe la base original 'dbsiga2022'
--  (la del diagrama dbsiga2022.png). Copia sus áreas, materias, alumnos
--  y funcionarios a 'dbsigalogin', limpiando los datos de paso:
--    * 'Activa'/'Inactiva'        -> 'Activo'/'Inactivo'
--    * fecha 0000-00-00           -> NULL
--    * horas semanales '---'      -> NULL
--    * cargo vacío                -> 'Docente'
--  Ejecutar DESPUÉS de scriptModificacionMysql.sql:
--      mysql -u root -p < scriptImportarDbsiga2022.sql
--
--  IMPORTANTE: en dbsiga2022 las claves están en TEXTO PLANO y no sirven
--  para el login (que usa BCrypt). A cada funcionario importado que no
--  tenga cuenta se le crea un usuario con la clave temporal  Admin123*
--  Cámbiela desde "Gestión de Usuarios" o con "¿Olvidó su contraseña?".
--  INSERT IGNORE: si un registro ya existe (misma llave o correo) se omite.
-- =====================================================================

USE dbsigalogin;

-- 1) Áreas (se conserva el código original)
INSERT IGNORE INTO area (codArea, nomArea, estadoArea)
SELECT a.cod_area,
       TRIM(a.nom_area),
       CASE WHEN a.estado_area LIKE 'Inactiv%' THEN 'Inactivo' ELSE 'Activo' END
FROM dbsiga2022.area a
WHERE a.nom_area IS NOT NULL;

-- 2) Materias (solo las que tienen un área válida)
INSERT IGNORE INTO materia (codMateria, nomMateria, codArea, cicloMateria, estadoMateria, ordenMateria, hSemanalesMateria)
SELECT m.cod_materia,
       TRIM(m.nom_materia),
       m.cod_area,
       COALESCE(NULLIF(TRIM(m.ciclo_materia), ''), '1'),
       CASE WHEN m.estado_materia LIKE 'Inactiv%' THEN 'Inactivo' ELSE 'Activo' END,
       COALESCE(m.orden_materia, 1),
       CASE WHEN m.H_semanales_materia REGEXP '^[0-9]+$' THEN CAST(m.H_semanales_materia AS UNSIGNED) END
FROM dbsiga2022.materia m
JOIN area ar ON ar.codArea = m.cod_area
WHERE m.nom_materia IS NOT NULL;

-- 3) Alumnos
INSERT IGNORE INTO alumno (docAlumno, nomAlumno, mailAlumno, direccAlumno, teleAlumno, estadoAlumno, sexoAlumno, fechanacAlumno)
SELECT al.doc_alumno,
       TRIM(al.nom_alumno),
       NULLIF(TRIM(al.mail_alumno), ''),
       NULLIF(TRIM(al.direcc_alumno), ''),
       LEFT(NULLIF(TRIM(al.tele_alumno), ''), 20),
       CASE WHEN al.estado_alumno = 'Inactivo' THEN 'Inactivo' ELSE 'Activo' END,
       CASE WHEN al.sexo_alumno IN ('Femenino','Masculino') THEN al.sexo_alumno ELSE 'Otro' END,
       CASE WHEN YEAR(al.fechanac_alumno) BETWEEN 1950 AND YEAR(CURDATE()) THEN DATE(al.fechanac_alumno) END
FROM dbsiga2022.alumno al
WHERE al.nom_alumno IS NOT NULL;

-- 4) Usuarios para los funcionarios que aún no tienen cuenta (clave temporal Admin123*)
INSERT IGNORE INTO usuario (nombreCompleto, documento, correo, claveHash, tipoUsuario)
SELECT TRIM(f.nom_funcionario),
       f.doc_funcionario,
       LOWER(TRIM(COALESCE(f.mail_funcionario, u.mail_usuario))),
       '$2a$10$KxEd1CBaugbBR/w.8ixFcu7Nwd2.698UqFRy8eZS3IIrB.5elko/O',
       CASE WHEN COALESCE(f.cargo_funcionario, u.Tipo_usuario) IN
                 ('Administrador','Coordinador','Directivo','Secretario','Docente')
            THEN COALESCE(f.cargo_funcionario, u.Tipo_usuario) ELSE 'Docente' END
FROM dbsiga2022.funcionario f
JOIN dbsiga2022.usuario u ON u.doc_usuario = f.doc_funcionario
WHERE COALESCE(f.mail_funcionario, u.mail_usuario) IS NOT NULL;

-- 5) Funcionarios (solo si su documento ya tiene usuario en dbsigalogin)
INSERT IGNORE INTO funcionario (docFuncionario, nomFuncionario, mailFuncionario, profFuncionario,
                                direcFuncionario, teleFuncionario, telemovilFuncionario,
                                estadoFuncionario, cargoFuncionario, sedeFuncionario)
SELECT f.doc_funcionario,
       TRIM(f.nom_funcionario),
       us.correo,
       LEFT(NULLIF(TRIM(f.prof_funcionario), ''), 150),
       LEFT(NULLIF(TRIM(f.direc_funcionario), ''), 150),
       NULLIF(TRIM(f.tele_funcionario), ''),
       NULLIF(TRIM(f.telemovil_funcionario), ''),
       CASE WHEN f.estado_funcionario = 'Inactivo' THEN 'Inactivo' ELSE 'Activo' END,
       COALESCE(NULLIF(TRIM(f.cargo_funcionario), ''), 'Docente'),
       NULLIF(TRIM(f.sede_funcionario), '')
FROM dbsiga2022.funcionario f
JOIN usuario us ON us.documento = f.doc_funcionario;

-- 6) Verificación
SELECT 'funcionario' AS tabla, COUNT(*) AS registros FROM funcionario
UNION ALL SELECT 'alumno',  COUNT(*) FROM alumno
UNION ALL SELECT 'area',    COUNT(*) FROM area
UNION ALL SELECT 'materia', COUNT(*) FROM materia
UNION ALL SELECT 'usuario', COUNT(*) FROM usuario;
