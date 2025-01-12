package hu.benkoata.imdb.services;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@TestConfiguration
public class MailSenderServiceITConfiguration {
    @SuppressWarnings("unused")
    @Bean
    HandlerExceptionResolver handlerExceptionResolver() {
        return (request, response, handler, ex) -> new ModelAndView();
    }
}
