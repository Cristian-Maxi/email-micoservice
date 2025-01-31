package microservice.email.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.mail.MessagingException;

import jakarta.mail.internet.MimeMessage;
import microservice.email.config.RabbitMQConfig;
import microservice.email.utils.OrderCreatedEvent;
import microservice.email.utils.ProductUpdatedEvent;
import microservice.email.utils.UserRegisteredEvent;
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
        generateOrderPdf(event);
        sendEmailWithPdf(event);
    }

    private void generateOrderPdf(OrderCreatedEvent event) {
        try (PdfWriter writer = new PdfWriter("order_" + event.getOrderId() + ".pdf")) {
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Order Details").setFontSize(20));
            document.add(new Paragraph("Order ID: " + event.getOrderId()));
            document.add(new Paragraph("User ID: " + event.getUserId()));
            document.add(new Paragraph("Customer Email: " + event.getEmail()));
            document.add(new Paragraph("\nOrder Items:").setFontSize(16));

            Table table = new Table(6);
            table.addHeaderCell("Product ID");
            table.addHeaderCell("Product Name");
            table.addHeaderCell("Description");
            table.addHeaderCell("Price");
            table.addHeaderCell("Stock");
            table.addHeaderCell("Quantity");

            double totalAmount = 0.0;

            for (OrderCreatedEvent.OrderItemDetails item : event.getOrderItems()) {
                double subtotal = item.getPrice() * item.getQuantity();
                totalAmount += subtotal;

                table.addCell(String.valueOf(item.getProductId()));
                table.addCell(item.getName());
                table.addCell(item.getDescription());
                table.addCell("$" + String.format("%.2f", item.getPrice()));
                table.addCell(String.valueOf(item.getStock()));
                table.addCell(String.valueOf(item.getQuantity()));
            }

            document.add(table);

            document.add(new Paragraph("\nTotal Amount: $" + String.format("%,.2f", totalAmount))
                    .setFontSize(14));

            document.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEmailWithPdf(OrderCreatedEvent event) {
        String recipientEmail = event.getEmail();
        String subject = "Order Confirmation - Order ID: " + event.getOrderId();
        String body = "Thank you for your order! Please find the details attached.";

        String pdfFilePath = "order_" + event.getOrderId() + ".pdf";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            //Se configura el destinatario
            helper.setTo("cristian@outlook");
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

    @RabbitListener(queues = RabbitMQConfig.USER_QUEUE)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        sendWelcomeEmail(event.getEmail(), event.getUsername());
    }

    private void sendWelcomeEmail(String email, String name) {
        String subject = "Welcome to Our Service!";
        String body = "Hi " + name + ",\n\nThank you for registering with us. We are excited to have you on board!\n\nBest regards,\nThe Team";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(body);

            mailSender.send(message);
            System.out.println("Welcome email sent to " + email);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error sending welcome email: " + e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PRODUCT_QUEUE)
    public void handleProductUpdatedEvent(ProductUpdatedEvent event) {
        String subject = "Inventario Actualizado: " + event.getProductName();
        String message = String.format(
                "El producto %s (ID: %d) ha cambiado su stock de %d a %d.",
                event.getProductName(), event.getProductId(), event.getOldStock(), event.getNewStock()
        );

        sendEmail("admin@empresa.com", subject, message);
        System.out.println("Notificación enviada al administrador: " + message);
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            mailSender.send(message);
            System.out.println("Correo enviado a " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }
    }
}
