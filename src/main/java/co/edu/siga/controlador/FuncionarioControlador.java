package co.edu.siga.controlador;

import co.edu.siga.modelo.Funcionario;
import co.edu.siga.modelo.FuncionarioModelo;
import co.edu.siga.modelo.UsuarioModelo;
import co.edu.siga.servicio.ValidadorDatos;
import co.edu.siga.servicio.excepciones.ErrorConflicto;
import co.edu.siga.servicio.excepciones.ErrorNoEncontrado;
import co.edu.siga.servicio.excepciones.ErrorValidacion;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * FuncionarioControlador  -  Paquete CONTROLADOR (Servlet)  ·  Taller 2
 * Rutas (prefijo /api/funcionarios): GET "", GET "/{doc}", POST "", PUT "/{doc}", DELETE "/{doc}"
 * Consultar: cualquier perfil con sesión.  Crear/editar/eliminar: solo Coordinador.
 */
@WebServlet(name = "FuncionarioControlador", urlPatterns = {"/api/funcionarios/*"})
public class FuncionarioControlador extends ControladorBase {

    private Funcionario buscarFuncionarioOFallar(FuncionarioModelo funcionarioModelo, String docFuncionario)
            throws SQLException {
        Funcionario funcionario = funcionarioModelo.buscarPorDocumento(docFuncionario);
        if (funcionario == null) {
            throw new ErrorNoEncontrado("No existe el funcionario con documento " + docFuncionario + ".");
        }
        return funcionario;
    }

    @Override
    protected void listar(HttpServletRequest peticion, HttpServletResponse respuesta) throws SQLException, IOException {
        List<Funcionario> funcionarios = new FuncionarioModelo().listar();
        RespuestaJson.responder(respuesta, true, funcionarios.size() + " funcionarios encontrados.",
                funcionarios.stream().map(Funcionario::aMapa).toList(), 200);
    }

    @Override
    protected void consultar(HttpServletRequest peticion, HttpServletResponse respuesta, String docFuncionario)
            throws SQLException, IOException {
        Funcionario funcionario = buscarFuncionarioOFallar(new FuncionarioModelo(), docFuncionario);
        RespuestaJson.responder(respuesta, true, "Funcionario consultado.", funcionario.aMapa(), 200);
    }

    @Override
    protected void crear(HttpServletRequest peticion, HttpServletResponse respuesta) throws SQLException, IOException {
        Map<String, Object> datos = RespuestaJson.leerDatos(peticion);
        List<String> errores = ValidadorDatos.validarFuncionario(datos);
        if (!errores.isEmpty()) {
            throw new ErrorValidacion(errores);
        }

        FuncionarioModelo funcionarioModelo = new FuncionarioModelo();
        String docFuncionario = (String) datos.get("docFuncionario");
        if (!new UsuarioModelo().existeDocumento(docFuncionario)) {
            throw new ErrorValidacion(List.of("Ese documento no tiene usuario. Créelo primero en 'Gestión de Usuarios'."));
        }
        if (funcionarioModelo.buscarPorDocumento(docFuncionario) != null) {
            throw new ErrorConflicto("Ese usuario ya tiene ficha de funcionario.");
        }
        if (funcionarioModelo.existeCorreoDeOtro((String) datos.get("mailFuncionario"), docFuncionario)) {
            throw new ErrorConflicto("Otro funcionario ya tiene ese correo.");
        }

        funcionarioModelo.insertar(Funcionario.desdeMapa(datos));
        RespuestaJson.responder(respuesta, true, "Funcionario " + datos.get("nomFuncionario") + " creado.", null, 201);
    }

    @Override
    protected void actualizar(HttpServletRequest peticion, HttpServletResponse respuesta, String docFuncionario)
            throws SQLException, IOException {
        FuncionarioModelo funcionarioModelo = new FuncionarioModelo();
        buscarFuncionarioOFallar(funcionarioModelo, docFuncionario);

        Map<String, Object> datos = RespuestaJson.leerDatos(peticion);
        datos.put("docFuncionario", docFuncionario);              // la llave viene de la URL, no del formulario
        List<String> errores = ValidadorDatos.validarFuncionario(datos);
        if (!errores.isEmpty()) {
            throw new ErrorValidacion(errores);
        }
        if (funcionarioModelo.existeCorreoDeOtro((String) datos.get("mailFuncionario"), docFuncionario)) {
            throw new ErrorConflicto("Otro funcionario ya tiene ese correo.");
        }

        funcionarioModelo.actualizar(Funcionario.desdeMapa(datos));
        RespuestaJson.responder(respuesta, true, "Funcionario actualizado.", null, 200);
    }

    @Override
    protected void eliminar(HttpServletRequest peticion, HttpServletResponse respuesta, String docFuncionario)
            throws SQLException, IOException {
        FuncionarioModelo funcionarioModelo = new FuncionarioModelo();
        Funcionario funcionario = buscarFuncionarioOFallar(funcionarioModelo, docFuncionario);
        funcionarioModelo.eliminar(docFuncionario);
        RespuestaJson.responder(respuesta, true,
                "Funcionario " + funcionario.getNomFuncionario() + " eliminado (su usuario se conserva).", null, 200);
    }
}
