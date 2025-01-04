package hu.benkoata.imdb.controllers;

import hu.benkoata.imdb.dtos.*;
import hu.benkoata.imdb.services.AuthenticationService;
import hu.benkoata.imdb.services.Logger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 used source: <a href="https://medium.com/@tericcabrel/implement-jwt-authentication-in-a-spring-boot-3-application-5839e4fd8fac">...</a>
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication related endpoints")
@SuppressWarnings("unused")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping(value = "/users")
    @Operation(summary = "Create new user")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements()
    public CreateUserDto createUser(
            HttpServletRequest httpServletRequest,
            @RequestBody @Valid CreateUserCommand command,
            @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.POST_MAPPING, userDetails, command.toString());
        return authenticationService.createUser(httpServletRequest.getRequestURI(), command);
    }
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "find user by email")
    @SecurityRequirements()
    public UserDto getUser(HttpServletRequest httpServletRequest,
                         @RequestParam String email,
                         @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        return authenticationService.getUserDetails(httpServletRequest.getRequestURI(), email);
    }
    @GetMapping("/users/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get current user data")
    @SecurityRequirement(name = "BearerAuth")
    public UserDto getMe(HttpServletRequest httpServletRequest,
                         @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        return authenticationService.getUserDetails(userDetails);
    }
    @PutMapping("/users/{id}/email-verification-code")
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "Email address validation with a verification code")
    @SecurityRequirements()
    public void enableUserIfVerificationOk(
            HttpServletRequest httpServletRequest,
            @PathVariable long id,
            @RequestBody int verificationCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.PUT_MAPPING, userDetails, String.format("Verification code: %d", verificationCode));
        authenticationService.enableUserIfVerificationOk(httpServletRequest.getRequestURI(), id, verificationCode);
    }
    @PutMapping("/users/change-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "Change password")
    @SecurityRequirements()
    public void changePassword(
            HttpServletRequest httpServletRequest,
            @RequestBody ChangeCredentialsCommand changeCredentialsCommand,
            @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.PUT_MAPPING, userDetails, changeCredentialsCommand.toString());
        authenticationService.changePassword(httpServletRequest.getRequestURI(), changeCredentialsCommand);
    }
    @PostMapping("/credentials")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(method = "Login with credentials and get jwt token")
    @SecurityRequirements()
    public JwtTokenDto login(HttpServletRequest httpServletRequest,
                             @RequestBody CredentialsCommand credentials,
                             @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails, credentials.toString());
        return authenticationService.authenticate(httpServletRequest.getRequestURI(), credentials);
    }
}