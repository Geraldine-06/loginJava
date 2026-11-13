package co.edu.siga.servicio;
 
import co.edu.siga.configuracion.Configuracion;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
 
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
 
/**
 * CorreoServicio  -  Paquete SERVICIO
 * Envía el correo de recuperación con Jakarta Mail (SMTP).
 * CORREO_MODO=consola -> solo imprime el enlace en la consola de Tomcat.
 */
public final class CorreoServicio {
 
    private static final Logger registro = Logger.getLogger(CorreoServicio.class.getName());
 
    private CorreoServicio() { }
 
    public static String construirEnlaceRecuperacion(String tokenPlano) {
        return Configuracion.urlBaseAplicacion() + "/restablecer.html?token=" + tokenPlano;
    }
 
    public static boolean enviarCorreoRecuperacion(String correoDestino, String nombreDestino, String tokenPlano) {
        String enlaceRecuperacion = construirEnlaceRecuperacion(tokenPlano);
 
        if ("consola".equals(Configuracion.correoModo())) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println(" [MODO CONSOLA] Correo de recuperación para: " + correoDestino);
            System.out.println(" Enlace: " + enlaceRecuperacion);
            System.out.println("=".repeat(70) + "\n");
            return true;
        }
 
        try {
            String cuerpoHtml = leerPlantilla()
                    .replace("{{nombre}}", escaparHtml(nombreDestino))
                    .replace("{{enlace}}", enlaceRecuperacion)
                    .replace("{{minutos}}", String.valueOf(Configuracion.tokenMinutosExpiracion()));
 
            Properties propiedadesSmtp = new Properties();
            propiedadesSmtp.put("mail.smtp.host", Configuracion.smtpServidor());
            propiedadesSmtp.put("mail.smtp.port", String.valueOf(Configuracion.smtpPuerto()));
            propiedadesSmtp.put("mail.smtp.auth", "true");
            if (Configuracion.smtpPuerto() == 465) {
                propiedadesSmtp.put("mail.smtp.ssl.enable", "true");          // SSL directo
            } else {
                propiedadesSmtp.put("mail.smtp.starttls.enable", "true");     // 587: STARTTLS
            }
 
            Session sesionCorreo = Session.getInstance(propiedadesSmtp, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(Configuracion.smtpUsuario(), Configuracion.smtpClave());
                }
            });
 
            MimeBodyPart parteTexto = new MimeBodyPart();
            parteTexto.setText("Hola " + nombreDestino + ", abre este enlace para crear una nueva contraseña: "
                    + enlaceRecuperacion, "UTF-8");
            MimeBodyPart parteHtml = new MimeBodyPart();
            parteHtml.setContent(cuerpoHtml, "text/html; charset=UTF-8");
            MimeMultipart contenido = new MimeMultipart("alternative", parteTexto, parteHtml);
 
            MimeMessage mensaje = new MimeMessage(sesionCorreo);
            mensaje.setFrom(new InternetAddress(Configuracion.correoRemitente()));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
            mensaje.setSubject("SIGA - Recuperación de contraseña", "UTF-8");
            mensaje.setContent(contenido);
            Transport.send(mensaje);
            return true;
        } catch (MessagingException | IOException errorCorreo) {
            registro.log(Level.SEVERE, "No se pudo enviar el correo", errorCorreo);
            return false;
        }
    }
 
    private static String leerPlantilla() throws IOException {
        try (InputStream flujo = CorreoServicio.class.getClassLoader()
                .getResourceAsStream("plantillasCorreo/correoRecuperacion.html")) {
            if (flujo == null) {
                throw new IOException("No se encontró la plantilla de correo");
            }
            return new String(flujo.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
 
    private static String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
