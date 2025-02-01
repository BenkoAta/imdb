package hu.benkoata.imdb.configurations;

import hu.benkoata.imdb.model.UserDetailDto;
import hu.benkoata.imdb.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SuppressWarnings("unused")
@Configuration
public class ApplicationAuthenticationConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
//@Bean
//public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
//    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//    authProvider.setUserDetailsService(userDetailsService);
//    authProvider.setPasswordEncoder(passwordEncoder());
//    return new ProviderManager(List.of(authProvider));
//}
    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository, ModelMapper modelMapper) {
        return username -> modelMapper.map(
                userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", username))),
                UserDetailDto.class);
    }

    @Bean
    public DaoAuthenticationProvider getAuthenticationProvider(UserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}
