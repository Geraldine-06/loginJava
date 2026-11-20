/* =====================================================================
   modulosDashboard.js  -  Taller 2 · Capa VISTA (cliente)
   CONFIGURACIÓN de los 5 módulos CRUD. Aquí NO hay lógica: solo se
   describe cada módulo (columnas de la tabla y campos del formulario).
   La clase CrudModulo (crudModulo.js) lee esta descripción y construye
   la tabla, el buscador, la paginación y las ventanas modales.
   ¿Agregar un campo?  ->  agregue un objeto en "campos" (y en el backend).

   Propiedades de un campo:
     nombre        nombre exacto que espera el backend (= columna de la tabla)
     etiqueta      texto visible            tipo  text | email | tel | date | number | password | select
     requerido     obligatorio              maximo / minimo  longitud o valor permitido
     patron        expresión regular        ayuda  texto pequeño bajo el campo
     opciones      lista fija para select   opcionesDesde  select que se llena con la API
     llave         es la llave primaria (no se puede cambiar al editar)
     valorInicial  valor sugerido al crear  completo  ocupa las dos columnas
   Propiedad del módulo "femenino: true": escribe "Nueva área" en vez de "Nuevo área".
   ===================================================================== */
const OPCIONES_ESTADO = ["Activo", "Inactivo"];
const OPCIONES_TIPO_USUARIO = ["Administrador", "Coordinador", "Directivo", "Secretario", "Docente"];
const PATRON_DOCUMENTO = "^\\d{5,15}$";
const PATRON_TELEFONO = "^[0-9+() -]{7,20}$";

const modulosDashboard = {
  usuarios: {
    titulo: "Gestión de Usuarios",
    descripcion: "Cuentas que pueden ingresar al sistema y su perfil de acceso.",
    singular: "usuario",
    icono: "icono-usuarios",
    recurso: "usuarios",
    llave: "idUsuario",
    campoNombre: "nombreCompleto",
    campoEstado: "estado",
    columnas: [
      { campo: "documento", titulo: "Documento" },
      { campo: "nombreCompleto", titulo: "Nombre", principal: true },
      { campo: "correo", titulo: "Correo" },
      { campo: "tipoUsuario", titulo: "Perfil", tipo: "insignia" },
      { campo: "estado", titulo: "Estado", tipo: "estado" },
      { campo: "ultimoIngreso", titulo: "Último ingreso", tipo: "fecha" }
    ],
    campos: [
      { nombre: "nombreCompleto", etiqueta: "Nombre completo", tipo: "text", requerido: true, minimo: 3, maximo: 120, completo: true },
      { nombre: "documento", etiqueta: "Documento", tipo: "text", requerido: true, patron: PATRON_DOCUMENTO, ayuda: "Solo números, 5 a 15 dígitos" },
      { nombre: "correo", etiqueta: "Correo", tipo: "email", requerido: true, maximo: 120 },
      { nombre: "tipoUsuario", etiqueta: "Tipo de usuario", tipo: "select", requerido: true, opciones: OPCIONES_TIPO_USUARIO },
      { nombre: "estado", etiqueta: "Estado", tipo: "select", requerido: true, opciones: OPCIONES_ESTADO, valorInicial: "Activo" },
      { nombre: "clave", etiqueta: "Contraseña", tipo: "password", requeridoAlCrear: true, completo: true,
        patron: "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        ayuda: "Mínimo 8 caracteres, una mayúscula, una minúscula y un número. Al editar, déjela vacía para no cambiarla." }
    ],
    detalle: [
      ["idUsuario", "Id"], ["nombreCompleto", "Nombre"], ["documento", "Documento"], ["correo", "Correo"],
      ["tipoUsuario", "Perfil"], ["estado", "Estado"], ["fechaCreacion", "Creado"], ["ultimoIngreso", "Último ingreso"]
    ]
  },

  funcionarios: {
    titulo: "Funcionarios",
    descripcion: "Ficha del personal. Cada funcionario corresponde a un usuario del sistema.",
    singular: "funcionario",
    icono: "icono-funcionarios",
    recurso: "funcionarios",
    llave: "docFuncionario",
    campoNombre: "nomFuncionario",
    campoEstado: "estadoFuncionario",
    columnas: [
      { campo: "docFuncionario", titulo: "Documento" },
      { campo: "nomFuncionario", titulo: "Nombre", principal: true },
      { campo: "cargoFuncionario", titulo: "Cargo", tipo: "insignia" },
      { campo: "mailFuncionario", titulo: "Correo" },
      { campo: "telemovilFuncionario", titulo: "Celular" },
      { campo: "sedeFuncionario", titulo: "Sede" },
      { campo: "estadoFuncionario", titulo: "Estado", tipo: "estado" }
    ],
    campos: [
      { nombre: "docFuncionario", etiqueta: "Usuario (documento)", tipo: "select", requerido: true, llave: true, completo: true,
        opcionesDesde: { recurso: "usuarios/sin-funcionario", valor: "documento", texto: (u) => `${u.documento} - ${u.nombreCompleto}` },
        autocompletar: { nomFuncionario: "nombreCompleto", mailFuncionario: "correo", cargoFuncionario: "tipoUsuario" },
        ayuda: "Solo aparecen los usuarios que aún no tienen ficha. Si no está, créelo en Gestión de Usuarios." },
      { nombre: "nomFuncionario", etiqueta: "Nombre completo", tipo: "text", requerido: true, minimo: 3, maximo: 120, completo: true },
      { nombre: "mailFuncionario", etiqueta: "Correo", tipo: "email", requerido: true, maximo: 120 },
      { nombre: "profFuncionario", etiqueta: "Profesión", tipo: "text", maximo: 150 },
      { nombre: "cargoFuncionario", etiqueta: "Cargo", tipo: "select", requerido: true, opciones: OPCIONES_TIPO_USUARIO },
      { nombre: "sedeFuncionario", etiqueta: "Sede", tipo: "text", maximo: 40 },
      { nombre: "teleFuncionario", etiqueta: "Teléfono fijo", tipo: "tel", patron: PATRON_TELEFONO },
      { nombre: "telemovilFuncionario", etiqueta: "Celular", tipo: "tel", patron: PATRON_TELEFONO },
      { nombre: "direcFuncionario", etiqueta: "Dirección", tipo: "text", maximo: 150 },
      { nombre: "estadoFuncionario", etiqueta: "Estado", tipo: "select", requerido: true, opciones: OPCIONES_ESTADO, valorInicial: "Activo" }
    ],
    detalle: [
      ["docFuncionario", "Documento"], ["nomFuncionario", "Nombre"], ["mailFuncionario", "Correo"],
      ["profFuncionario", "Profesión"], ["cargoFuncionario", "Cargo"], ["sedeFuncionario", "Sede"],
      ["teleFuncionario", "Teléfono"], ["telemovilFuncionario", "Celular"], ["direcFuncionario", "Dirección"],
      ["estadoFuncionario", "Estado"]
    ]
  },

  alumnos: {
    titulo: "Alumnos",
    descripcion: "Estudiantes registrados en la institución.",
    singular: "alumno",
    icono: "icono-alumnos",
    recurso: "alumnos",
    llave: "docAlumno",
    campoNombre: "nomAlumno",
    campoEstado: "estadoAlumno",
    columnas: [
      { campo: "docAlumno", titulo: "Documento" },
      { campo: "nomAlumno", titulo: "Nombre", principal: true },
      { campo: "sexoAlumno", titulo: "Sexo" },
      { campo: "fechanacAlumno", titulo: "Nacimiento", tipo: "fecha" },
      { campo: "teleAlumno", titulo: "Teléfono" },
      { campo: "estadoAlumno", titulo: "Estado", tipo: "estado" }
    ],
    campos: [
      { nombre: "docAlumno", etiqueta: "Documento", tipo: "text", requerido: true, llave: true, patron: PATRON_DOCUMENTO, ayuda: "Solo números, 5 a 15 dígitos" },
      { nombre: "sexoAlumno", etiqueta: "Sexo", tipo: "select", requerido: true, opciones: ["Femenino", "Masculino", "Otro"] },
      { nombre: "nomAlumno", etiqueta: "Nombre completo", tipo: "text", requerido: true, minimo: 3, maximo: 120, completo: true },
      { nombre: "mailAlumno", etiqueta: "Correo", tipo: "email", maximo: 120 },
      { nombre: "teleAlumno", etiqueta: "Teléfono", tipo: "tel", patron: PATRON_TELEFONO },
      { nombre: "direccAlumno", etiqueta: "Dirección", tipo: "text", maximo: 150, completo: true },
      { nombre: "fechanacAlumno", etiqueta: "Fecha de nacimiento", tipo: "date" },
      { nombre: "estadoAlumno", etiqueta: "Estado", tipo: "select", requerido: true, opciones: OPCIONES_ESTADO, valorInicial: "Activo" }
    ],
    detalle: [
      ["docAlumno", "Documento"], ["nomAlumno", "Nombre"], ["mailAlumno", "Correo"], ["teleAlumno", "Teléfono"],
      ["direccAlumno", "Dirección"], ["sexoAlumno", "Sexo"], ["fechanacAlumno", "Nacimiento"], ["estadoAlumno", "Estado"]
    ]
  },

  areas: {
    titulo: "Áreas",
    descripcion: "Áreas del plan de estudios. Agrupan las materias.",
    singular: "área",
    femenino: true,
    icono: "icono-areas",
    recurso: "areas",
    llave: "codArea",
    campoNombre: "nomArea",
    campoEstado: "estadoArea",
    columnas: [
      { campo: "codArea", titulo: "Código" },
      { campo: "nomArea", titulo: "Nombre del área", principal: true },
      { campo: "totalMaterias", titulo: "Materias" },
      { campo: "estadoArea", titulo: "Estado", tipo: "estado" }
    ],
    campos: [
      { nombre: "nomArea", etiqueta: "Nombre del área", tipo: "text", requerido: true, minimo: 3, maximo: 120, completo: true },
      { nombre: "estadoArea", etiqueta: "Estado", tipo: "select", requerido: true, opciones: OPCIONES_ESTADO, valorInicial: "Activo" }
    ],
    detalle: [["codArea", "Código"], ["nomArea", "Nombre"], ["totalMaterias", "Materias"], ["estadoArea", "Estado"]]
  },

  materias: {
    titulo: "Materias",
    descripcion: "Asignaturas del plan de estudios y el área a la que pertenecen.",
    singular: "materia",
    femenino: true,
    icono: "icono-materias",
    recurso: "materias",
    llave: "codMateria",
    campoNombre: "nomMateria",
    campoEstado: "estadoMateria",
    columnas: [
      { campo: "codMateria", titulo: "Código" },
      { campo: "nomMateria", titulo: "Materia", principal: true },
      { campo: "nomArea", titulo: "Área" },
      { campo: "cicloMateria", titulo: "Ciclos" },
      { campo: "hSemanalesMateria", titulo: "Horas/sem." },
      { campo: "ordenMateria", titulo: "Orden" },
      { campo: "estadoMateria", titulo: "Estado", tipo: "estado" }
    ],
    campos: [
      { nombre: "nomMateria", etiqueta: "Nombre de la materia", tipo: "text", requerido: true, minimo: 3, maximo: 120, completo: true },
      { nombre: "codArea", etiqueta: "Área", tipo: "select", requerido: true, completo: true,
        opcionesDesde: { recurso: "areas", valor: "codArea", texto: (a) => a.nomArea } },
      { nombre: "cicloMateria", etiqueta: "Ciclos", tipo: "text", requerido: true, maximo: 20, patron: "^\\d{1,2}(-\\d{1,2})*$", ayuda: "Ej: 1-2-3-4" },
      { nombre: "hSemanalesMateria", etiqueta: "Horas semanales", tipo: "number", minimo: 1, maximo: 40 },
      { nombre: "ordenMateria", etiqueta: "Orden en el boletín", tipo: "number", requerido: true, minimo: 1, maximo: 99 },
      { nombre: "estadoMateria", etiqueta: "Estado", tipo: "select", requerido: true, opciones: OPCIONES_ESTADO, valorInicial: "Activo" }
    ],
    detalle: [
      ["codMateria", "Código"], ["nomMateria", "Materia"], ["nomArea", "Área"], ["cicloMateria", "Ciclos"],
      ["hSemanalesMateria", "Horas semanales"], ["ordenMateria", "Orden"], ["estadoMateria", "Estado"]
    ]
  }
};
