package hu.benkoata.imdb.configurations;

import hu.benkoata.imdb.services.MailSenderService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "spring.mail")
@Getter
@Setter
@SuppressWarnings("unused")
public class EmailConfig {
    private String host;
    private int port;
    private String from;
    private String username;
    private String password;
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }
    @Bean
    public MailSenderService mailSenderService(JavaMailSender javaMailSender, MessageSource messageSource) {
        return new MailSenderService(javaMailSender, from, messageSource);
    }
}
