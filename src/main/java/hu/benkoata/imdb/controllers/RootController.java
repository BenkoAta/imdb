package hu.benkoata.imdb.controllers;

import hu.benkoata.imdb.ImdbApplication;
import hu.benkoata.imdb.services.ConfigFileReaderService;
import hu.benkoata.imdb.services.Logger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;
@SuppressWarnings("unused")
@RestController
@Slf4j
@Tag(name = "root operations")
@RequiredArgsConstructor
public class RootController {
    private final ConfigFileReaderService configFileReaderService;
    private static final String ROOT_TEMPLATE = "<title>%1$s</title>Webapp name: %1$s<br>Version: %2$s<br><a href=%3$s>swagger-UI</a>";
    //Todo @WebMvcTest tesztelés, hogy a megfelelő html-t adja vissza, mockolt ConfigFileReaderService használattal
    // minta:/home/benkoa/java-projects/pdcapi/pdcapi/src/test/java/hu/benkoata/pdcapi/controllers/ErpproxyControllerIT.java
    @GetMapping({"/","/api"})
    @Operation(summary = "get webapp name and version")
    @SecurityRequirements()
    public String getRoot(HttpServletRequest httpServletRequest,
                          @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        Properties props = configFileReaderService.read(ImdbApplication.class, "project.properties");
        return String.format(ROOT_TEMPLATE, props.getProperty("artifactId"), props.getProperty("version"),
                getSwaggerUrl(httpServletRequest));
    }

    private String getSwaggerUrl(HttpServletRequest request) {
        return String.format("%s://%s:%d%s/swagger-ui/index.html",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                request.getContextPath());
    }
}
