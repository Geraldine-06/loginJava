package co.edu.siga.modelo;

import co.edu.siga.configuracion.ConexionBd;
import co.edu.siga.servicio.excepciones.ErrorReferenciaInvalida;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * FuncionarioModelo  -  Paquete MODELO (acceso a datos de "funcionario")  ·  Taller 2
 * Hereda de ConexionBd: aquí vive TODO el SQL de funcionarios.
 */
public class FuncionarioModelo extends ConexionBd {

    private static final String COLUMNAS_FUNCIONARIO = "docFuncionario, nomFuncionario, mailFuncionario, "
            + "profFuncionario, direcFuncionario, teleFuncionario, telemovilFuncionario, estadoFuncionario, "
            + "cargoFuncionario, sedeFuncionario";

    public List<Funcionario> listar() throws SQLException {
        return ejecutarConsulta("SELECT " + COLUMNAS_FUNCIONARIO + " FROM funcionario ORDER BY nomFuncionario")
                .stream().map(Funcionario::desdeMapa).toList();
    }

    public Funcionario buscarPorDocumento(String docFuncionario) throws SQLException {
        List<Map<String, Object>> filas = ejecutarConsulta(
                "SELECT " + COLUMNAS_FUNCIONARIO + " FROM funcionario WHERE docFuncionario = ?", docFuncionario);
        return filas.isEmpty() ? null : Funcionario.desdeMapa(filas.get(0));
    }

    public boolean existeCorreoDeOtro(String mailFuncionario, String docFuncionario) throws SQLException {
        return !ejecutarConsulta(
                "SELECT docFuncionario FROM funcionario WHERE mailFuncionario = ? AND docFuncionario <> ?",
                mailFuncionario, docFuncionario).isEmpty();
    }

    public boolean insertar(Funcionario funcionario) throws SQLException {
        try {
            return ejecutarComando(
                    "INSERT INTO funcionario (" + COLUMNAS_FUNCIONARIO + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    funcionario.getDocFuncionario(), funcionario.getNomFuncionario(), funcionario.getMailFuncionario(),
                    funcionario.getProfFuncionario(), funcionario.getDirecFuncionario(), funcionario.getTeleFuncionario(),
                    funcionario.getTelemovilFuncionario(), funcionario.getEstadoFuncionario(),
                    funcionario.getCargoFuncionario(), funcionario.getSedeFuncionario()) == 1;
        } catch (ErrorReferenciaInvalida errorReferencia) {
            throw new ErrorReferenciaInvalida("Ese documento no tiene usuario. Créelo primero en "
                    + "'Gestión de Usuarios'.", errorReferencia);
        }
    }

    /** El documento (llave primaria) no se cambia aquí: se usa para ubicar el registro. */
    public int actualizar(Funcionario funcionario) throws SQLException {
        return ejecutarComando(
                "UPDATE funcionario SET nomFuncionario = ?, mailFuncionario = ?, profFuncionario = ?, "
                        + "direcFuncionario = ?, teleFuncionario = ?, telemovilFuncionario = ?, estadoFuncionario = ?, "
                        + "cargoFuncionario = ?, sedeFuncionario = ? WHERE docFuncionario = ?",
                funcionario.getNomFuncionario(), funcionario.getMailFuncionario(), funcionario.getProfFuncionario(),
                funcionario.getDirecFuncionario(), funcionario.getTeleFuncionario(), funcionario.getTelemovilFuncionario(),
                funcionario.getEstadoFuncionario(), funcionario.getCargoFuncionario(), funcionario.getSedeFuncionario(),
                funcionario.getDocFuncionario());
    }

    public boolean eliminar(String docFuncionario) throws SQLException {
        return ejecutarComando("DELETE FROM funcionario WHERE docFuncionario = ?", docFuncionario) == 1;
    }
}
