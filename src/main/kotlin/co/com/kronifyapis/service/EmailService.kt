package co.com.kronifyapis.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

/**
 * Servicio para enviar correos electronicos.
 */

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    @Value("\${app.frontend-url}") private val frontendUrl: String
) {
    /**
     * Envia un correo de invitacion a un empleado con un link
     * para que acepte unirse al negocio.
     */
    fun sendInvitationEmail(to: String, token: String, businessName: String) {
        val link = "$frontendUrl/invitacion/aceptar?token=$token"

        val context = Context().apply {
            setVariable("link", link)
            setVariable("businessName", businessName)
        }
        val html = templateEngine.process("invitation-email", context)

        val mimeMessage = mailSender.createMimeMessage()
        MimeMessageHelper(mimeMessage, true, "UTF-8").apply {
            setTo(to)
            setSubject("Te invitaron a unirte a un negocio en Kronify")
            setText(html, true)
        }
        mailSender.send(mimeMessage)
    }
}