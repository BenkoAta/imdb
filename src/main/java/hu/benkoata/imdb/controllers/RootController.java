package hu.benkoata.imdb.controllers;

import hu.benkoata.imdb.ImdbApplication;
import hu.benkoata.imdb.services.ConfigFile;
import hu.benkoata.imdb.services.Logger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;
@SuppressWarnings("unused")
@RestController
@Slf4j
@Tag(name = "root operations")
@RequiredArgsConstructor
@RequestMapping("/api")
public class RootController {
    @GetMapping("/")
    @Operation(summary = "get webapp name and version")
    public String getRoot(HttpServletRequest httpServletRequest,
                          @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        ConfigFile cf = new ConfigFile(ImdbApplication.class, "project.properties");
        Properties props = cf.read();
        return String.format("%s %s", props.getProperty("artifactId"), props.getProperty("version"));
    }
}
