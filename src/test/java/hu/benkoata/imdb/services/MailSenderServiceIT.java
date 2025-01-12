package hu.benkoata.imdb.services;

import hu.benkoata.imdb.ImdbApplication;
import hu.benkoata.imdb.configurations.EmailConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {
        ImdbApplication.class,
        EmailConfig.class,
        MailSenderServiceITConfiguration.class
})
class MailSenderServiceIT {
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    MessageSource messageSource;

    @Test
    void testSend() {
        MailSenderService mailSenderService = new MailSenderService(javaMailSender, "noreply@localhost", messageSource);
        mailSenderService.sendSimpleEmail("a@b.com", "teszt", "tesztüzenet");
        assertTrue(true);
    }
}