package microservice.email.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import jakarta.mail.MessagingException;

import jakarta.mail.internet.MimeMessage;
import microservice.email.config.RabbitMQConfig;
import microservice.email.utils.OrderCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        // Generar el PDF con los detalles del pedido
        generateOrderPdf(event);

        // Enviar el email con el PDF adjunto
        sendEmailWithPdf(event);
    }

    private void generateOrderPdf(OrderCreatedEvent event) {
        try (PdfWriter writer = new PdfWriter("order_" + event.getOrderId() + ".pdf")) {
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Order Details").setFontSize(20));
            document.add(new Paragraph("Order ID: " + event.getOrderId()));
            document.add(new Paragraph("User ID: " + event.getUserId()));
            document.add(new Paragraph("Items:"));

            for (OrderCreatedEvent.OrderItemDetails item : event.getOrderItems()) {
                document.add(new Paragraph("- Product ID: " + item.getProductId() + ", Quantity: " + item.getQuantity()));
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEmailWithPdf(OrderCreatedEvent event) {
        String recipientEmail = event.getEmail();
        String subject = "Order Confirmation - Order ID: " + event.getOrderId();
        String body = "Thank you for your order! Please find the details attached.";

        String pdfFilePath = "order_" + event.getOrderId() + ".pdf"; // Ruta al archivo PDF generado

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            //Se configura el destinatario
            helper.setTo("testeando432@gmail.com");
            helper.setSubject(subject);
            helper.setText(body);

            File pdfFile = new File(pdfFilePath);
            helper.addAttachment(pdfFile.getName(), pdfFile);

            mailSender.send(message);
            System.out.println("Correo enviado exitosamente a " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }
    }
}
