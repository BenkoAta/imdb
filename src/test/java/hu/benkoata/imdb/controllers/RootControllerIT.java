package hu.benkoata.imdb.controllers;

import hu.benkoata.imdb.ImdbApplication;
import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.ConfigFileReaderService;
import hu.benkoata.imdb.services.security.JwtService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RootController.class)
//@ContextConfiguration(classes = {SecurityConfig.class, ApplicationAuthenticationConfiguration.class, JwtAuthenticationFilter.class})
@ContextConfiguration(classes = DisableSecurityConfig.class)
class RootControllerIT {
    public static final String ARTFACT = "imdb";
    public static final String VERSION = "mockedVersion";
    @MockitoBean
    ConfigFileReaderService configFileReaderService;
    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    UserRepository userRepository;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private MockMvc mockMvc;

//    @BeforeEach
//    public void setup() {
//        mockMvc = MockMvcBuilders
//                .webAppContextSetup(context)
//                .apply(springSecurity())
//                .build();
//    }

    @ParameterizedTest()
    @ValueSource(strings = {"/", "/api"})
    @WithMockUser("a@b.c")
    void testGetRoot(String path) throws Exception {
        mockConfigFileReaderService(configFileReaderService);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(path)
                )
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        assertThat(response)
                .contains(String.format("Webapp name: %s", ARTFACT))
                .contains(String.format("Version: %s", VERSION));
        verify(configFileReaderService, times(1)).read(ImdbApplication.class, "project.properties");
    }

    private void mockConfigFileReaderService(ConfigFileReaderService service) {
        Properties mockedProps = new Properties();
        mockedProps.put("artifactId", ARTFACT);
        mockedProps.put("version", VERSION);
        when(service.read(ImdbApplication.class, "project.properties"))
                .thenReturn(mockedProps);
    }
}