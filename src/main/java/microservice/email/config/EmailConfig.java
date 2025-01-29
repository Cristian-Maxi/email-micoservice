package microservice.email.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

//        mailSender.setHost("sandbox.smtp.mailtrap.io");
//        mailSender.setPort(2525);
//        mailSender.setUsername("d0465e537dbdf9");
//        mailSender.setPassword("a18ff16637ca7d");

        //Se configura el remitente
        mailSender.setHost("sandbox.smtp.mailtrap.io");
        mailSender.setPort(2525);
        mailSender.setUsername("d0465e537dbdf9");
        mailSender.setPassword("a18ff16637ca7d");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
