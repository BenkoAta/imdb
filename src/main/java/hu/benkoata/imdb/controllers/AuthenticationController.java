package hu.benkoata.imdb.controllers;

import hu.benkoata.imdb.dtos.CreateUserCommand;
import hu.benkoata.imdb.dtos.CreateUserDto;
import hu.benkoata.imdb.dtos.CredentialsCommand;
import hu.benkoata.imdb.dtos.JwtTokenDto;
import hu.benkoata.imdb.services.AuthenticationService;
import hu.benkoata.imdb.services.Logger;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@SuppressWarnings("unused")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements()
    public CreateUserDto createUser(
            HttpServletRequest httpServletRequest,
            @RequestBody @Valid CreateUserCommand command,
            @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.POST_MAPPING, userDetails, command.toString());
        return authenticationService.createUser(httpServletRequest.getRequestURI(), command);
    }

    //https://medium.com/@tericcabrel/implement-jwt-authentication-in-a-spring-boot-3-application-5839e4fd8fac
    @PostMapping("/credentials")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements()
    public JwtTokenDto login(HttpServletRequest httpServletRequest,
                             @RequestBody CredentialsCommand credentials,
                             @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails, credentials.toString());
        return authenticationService.authenticate(httpServletRequest.getRequestURI(), credentials);
    }
}