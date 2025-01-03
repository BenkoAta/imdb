package hu.benkoata.imdb;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ImdbApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImdbApplication.class, args);
	}
	@SuppressWarnings("unused")
	@Bean
	public ModelMapper getModelMapper() {
		return new ModelMapper();
	}
}
