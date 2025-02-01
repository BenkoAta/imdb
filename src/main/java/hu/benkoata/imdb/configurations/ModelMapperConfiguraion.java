package hu.benkoata.imdb.configurations;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfiguraion {
    @SuppressWarnings("unused")
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
