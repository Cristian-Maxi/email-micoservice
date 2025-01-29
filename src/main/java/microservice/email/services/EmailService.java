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

//    private void generateOrderPdf(OrderCreatedEvent event) {
//        try (PdfWriter writer = new PdfWriter("order_" + event.getOrderId() + ".pdf")) {
//            PdfDocument pdf = new PdfDocument(writer);
//            Document document = new Document(pdf);
//
//            document.add(new Paragraph("Order Details").setFontSize(20));
//            document.add(new Paragraph("Order ID: " + event.getOrderId()));
//            document.add(new Paragraph("User ID: " + event.getUserId()));
//            document.add(new Paragraph("Items:"));
//
//            for (OrderCreatedEvent.OrderItemDetails item : event.getOrderItems()) {
//                document.add(new Paragraph("- " + item.getProductName() + " (ID: " + item.getProductId() + "), Quantity: " + item.getQuantity()));
//            }
//
//            document.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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

            for (OrderCreatedEvent.OrderItemDetails item : event.getOrderItems()) {
                table.addCell(String.valueOf(item.getProductId()));
                table.addCell(item.getName()); // Agregando nombre del producto
                table.addCell(item.getDescription());
                table.addCell("$" + String.format("%.2f", item.getPrice()));
                table.addCell(String.valueOf(item.getStock()));
                table.addCell(String.valueOf(item.getQuantity()));
            }

            document.add(table);
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
}
