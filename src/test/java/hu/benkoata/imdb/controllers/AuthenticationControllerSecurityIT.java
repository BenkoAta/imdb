package hu.benkoata.imdb.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.benkoata.imdb.configurations.ApplicationAuthenticationConfiguration;
import hu.benkoata.imdb.configurations.JwtServiceConfiguration;
import hu.benkoata.imdb.configurations.SecurityConfig;
import hu.benkoata.imdb.model.UserDetailDto;
import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.AuthenticationService;
import hu.benkoata.imdb.services.EndpointUsageLimiterService;
import hu.benkoata.imdb.services.MailSenderService;
import hu.benkoata.imdb.services.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static hu.benkoata.imdb.controllers.AuthenticationController.API_AUTH;
import static hu.benkoata.imdb.controllers.AuthenticationController.SECURITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(controllers = AuthenticationController.class)
@Import({SecurityConfig.class,
        ApplicationAuthenticationConfiguration.class,
        JwtServiceConfiguration.class})
class AuthenticationControllerSecurityIT {
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pwd";
    @MockitoBean
    AuthenticationService authenticationService;
    @MockitoBean
    UserDetailsService userDetailsService;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    MailSenderService mailSenderService;
    @MockitoBean
    EndpointUsageLimiterService limiterService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    JwtService jwtService;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testGetSecurityWithoutAuth() throws Exception {
        mockUserDetailService(userDetailsService);
        mockMvc.perform(MockMvcRequestBuilders.
                        get(API_AUTH + SECURITY)
                        .secure(true))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void testGetSecurityWithBasicAuth() throws Exception {
        mockUserDetailService(userDetailsService);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.
                        get(API_AUTH + SECURITY)
                        .with(httpBasic(USERNAME, PASSWORD))
                        .secure(true))
                .andExpect(status().isForbidden()).andReturn();
        HashMap<String, Object> response = mapper.readValue(result.getResponse().getContentAsString(), HashMap.class);
        response.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().toString())
                .forEach(log::debug);
        assertThat(response.get("detail").toString()).startsWith("Not possible to use authentication: Basic");
    }

    @Test
    void testGetSecurityWithJwtToken() throws Exception {
        mockUserDetailService(userDetailsService);
        Date now = Date.from(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()).toInstant());
        String token = jwtService.generateToken(USERNAME, now);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.
                        get(API_AUTH + SECURITY).secure(true)
                        .header("Authorization", String.format("Bearer %s", token)))
                .andExpect(status().isOk())
                .andReturn();
        ArrayList<String> response = mapper.readValue(result.getResponse().getContentAsString(), ArrayList.class);
        response.forEach(log::debug);
        assertThat(response).anyMatch(s -> s.contains("JwtAuthenticationFilter"));
    }
    @Test
    void testGetSecurityWithWrongJwtToken() throws Exception {
        mockUserDetailService(userDetailsService);
        Date now = Date.from(ZonedDateTime.of(LocalDateTime.now().minusDays(2), ZoneId.systemDefault()).toInstant());
        String token = jwtService.generateToken(USERNAME, now);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.
                        get(API_AUTH + SECURITY).secure(true)
                        .header("Authorization", String.format("Bearer %s", token)))
                .andExpect(status().isForbidden())
                .andReturn();
        HashMap<String, Object> response = mapper.readValue(result.getResponse().getContentAsString(), HashMap.class);
        response.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().toString())
                .forEach(log::debug);
    }

    private void mockUserDetailService(UserDetailsService userDetailsService) {
        UserDetailDto user = new UserDetailDto();
        user.setEmail(USERNAME);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setAccountLocked(false);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(user);
    }
}