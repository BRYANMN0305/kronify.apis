package co.com.kronifyapis.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

/**
 * Servicio para enviar correos electronicos.
 *
 * Centraliza el envío por Gmail SMTP. En desarrollo se puede apagar con
 * MAIL_ENABLED=false o redirigir todos los correos a MAIL_TEST_RECIPIENT.
 */

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    @Value("\${app.frontend-url}") private val frontendUrl: String,
    @Value("\${app.mail.enabled:true}") private val mailEnabled: Boolean,
    @Value("\${app.mail.test-recipient:}") private val testRecipient: String
) {
    private val log = org.slf4j.LoggerFactory.getLogger(javaClass)

    /**
     * Envia un correo de invitacion a un empleado con un link
     * para que acepte unirse al negocio.
     */
    fun sendInvitationEmail(to: String, token: String, businessName: String) {
        val link = "$frontendUrl/invitacion/aceptar?token=$token"

        sendHtmlEmail(
            to = to,
            subject = "Te invitaron a unirte a un negocio en Kronify",
            templateName = "invitation-email",
            variables = mapOf(
                "link" to link,
                "businessName" to businessName
            )
        )
    }

    /**
     * Envia un correo HTML procesando una plantilla de Thymeleaf.
     *
     * @throws org.springframework.mail.MailException si el servidor SMTP falla.
     */
    fun sendHtmlEmail(
        to: String,
        subject: String,
        templateName: String,
        variables: Map<String, Any?>
    ) {
        val recipient = resolveRecipient(to)
        if (recipient == null) {
            log.debug("Correo '{}' omitido: envío deshabilitado o destinatario vacío", templateName)
            return
        }

        val context = Context().apply {
            variables.forEach { (name, value) -> setVariable(name, value) }
        }
        val html = templateEngine.process(templateName, context)

        val mimeMessage = mailSender.createMimeMessage()
        MimeMessageHelper(mimeMessage, true, "UTF-8").apply {
            setTo(recipient)
            setSubject(subject)
            setText(html, true)
        }
        mailSender.send(mimeMessage)
        log.info("Correo '{}' enviado correctamente", templateName)
    }

    /**
     * Devuelve la direccion a la que realmente se enviara el correo.
     *
     * Si MAIL_ENABLED es false devuelve null. Si hay un destinatario de
     * pruebas configurado, ese reemplaza al destinatario real.
     */
    fun resolveRecipient(to: String?): String? {
        if (!mailEnabled) return null

        val original = to?.trim() ?: return null
        if (original.isEmpty()) return null

        val override = testRecipient.trim()
        if (override.isNotEmpty()) return override

        return original.takeIf { it.contains('@') && !it.any(Char::isWhitespace) }
    }

    /**
     * Indica si los envíos de correo están habilitados.
     */
    fun isMailEnabled(): Boolean = mailEnabled
}
