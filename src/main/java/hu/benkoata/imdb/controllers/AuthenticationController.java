package hu.benkoata.imdb.controllers;

import hu.benkoata.imdb.dtos.*;
import hu.benkoata.imdb.exceptions.InvalidUserIdException;
import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.AuthenticationService;
import hu.benkoata.imdb.services.EndpointUsageLimiterService;
import hu.benkoata.imdb.services.Logger;
import hu.benkoata.imdb.services.MailSenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * used source: <a href="https://medium.com/@tericcabrel/implement-jwt-authentication-in-a-spring-boot-3-application-5839e4fd8fac">...</a>
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(AuthenticationController.API_AUTH)
@Tag(name = "Authentication related endpoints")
@SuppressWarnings("unused")
public class AuthenticationController {
    public static final String API_AUTH = "/api/auth";
    public static final String DELETE_VERIFICATION = "/users/{id}/delete-verification";
    public static final String EMAIL_VERIFICATION = "/users/{id}/email-verification";
    public static final String UNLOCK_VERIFICATION = "/users/{id}/unlock-verification";
    private static final String VERIFICATION_CODE_PARAM = "?verification-code={code}";
    public static final String SECURITY = "/security";
    public static final String USERS_ME = "/users/me";
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final MailSenderService mailSenderService;
    private final EndpointUsageLimiterService limiterService;
    private final Filter springSecurityFilterChain;

    @GetMapping(SECURITY)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get security filter chain.")
    @SecurityRequirement(name = "BearerAuth")
    public List<String> getSecurity(HttpServletRequest httpServletRequest,
                         @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        FilterChainProxy filterChainProxy = (FilterChainProxy) springSecurityFilterChain;
        List<SecurityFilterChain> list = filterChainProxy.getFilterChains();
        return list.stream()
                .flatMap(chain -> chain.getFilters().stream())
                .map(filter -> filter.getClass().toString())
                .toList();
    }
    //Todo webtestclienttel tesztelni, hogy valóban létrejön a user,
    // mockolt mailSenderService-vel az elküldött emailt tesztelni
    // és a limitálást tesztelni
    @PostMapping(value = "/users")
    @Operation(summary = "Create new user",
            description = "Create a user with a locked account and" +
                    " send an email containing a verification link to request email address verification.")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    public CreateUserDto createUser(
            HttpServletRequest httpServletRequest,
            @RequestBody @Valid CreateUserCommand command,
            @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.POST_MAPPING, userDetails, command.toString());
        limiterService.checkRegistrationLimit(httpServletRequest.getRemoteHost());
        Locale locale = new Locale(command.getPreferredLanguageId());
        return authenticationService.createUser(httpServletRequest.getRequestURI(),
                command,
                (userDto, verificationCode) -> sendEmailVerification(httpServletRequest, userDto, verificationCode, locale));
    }

    private void sendEmailVerification(HttpServletRequest httpServletRequest, UserDto userDto, Integer verificationCode, Locale locale) {
        mailSenderService.sendEmailValidification(
                httpServletRequest,
                userDto.getId(),
                verificationCode,
                userDto.getEmail(),
                API_AUTH + EMAIL_VERIFICATION + VERIFICATION_CODE_PARAM,
                locale
        );
    }

    //Todo webtestclienttel tesztelni, hogy visszadja-e a mentett usert.
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find user by email.")
    @SecurityRequirements
    public UserDto getUser(HttpServletRequest httpServletRequest,
                           @RequestParam String email,
                           @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        return authenticationService.getUserDetailsByEmail(httpServletRequest.getRequestURI(), email);
    }

    //Todo webtestclienttel teszteli, hogy jó-e
    @GetMapping(USERS_ME)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve the currently logged-in user's data.")
    @SecurityRequirement(name = "BearerAuth")
    public UserDto getMe(HttpServletRequest httpServletRequest,
                         @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        return authenticationService.getFullUserDetails(httpServletRequest.getRequestURI(), userDetails.getUsername());
    }
    //Todo webtestclienttel teszteli, hogy csak a saját user adatait adja vissza, másét nem
    @GetMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve user's data by id",
    description = "Only the currently logged-in user's data can be retrieved.")
    @SecurityRequirement(name = "BearerAuth")
    public UserDto getUserById(HttpServletRequest httpServletRequest,
                               @PathVariable long id,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        UserDto result = authenticationService.getFullUserDetails(httpServletRequest.getRequestURI(), userDetails.getUsername());
        if (id != result.getId()) {
            throw new InvalidUserIdException(httpServletRequest.getRequestURI());
        }
        return result;
    }
    //todo megfelelően preparált userrel webtestclienttel tesztelni,
    // hogy elküldi-e kódot email-ben (mockolt mailSenderService)
    @PutMapping("/users/{id}/unlock")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Request to unlock a user account",
            description = "Send an email containing a verification link to unlock the user's account.")
    @SecurityRequirements
    public void requestToUnlockUserById(HttpServletRequest httpServletRequest,
                            @PathVariable long id,
                            @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.PUT_MAPPING, userDetails);
        limiterService.resetUnlockLimit(id);
        authenticationService.unlockUser(httpServletRequest.getRequestURI(), id,
                (userDto, verificationCode) -> sendUnlockMail(httpServletRequest, userDto, verificationCode));
    }

    private void sendUnlockMail(HttpServletRequest httpServletRequest, UserDto userDto, Integer verificationCode) {
        mailSenderService.sendUnlockUser(
                httpServletRequest,
                userDto.getId(),
                verificationCode,
                userDto.getEmail(),
                API_AUTH + UNLOCK_VERIFICATION + VERIFICATION_CODE_PARAM,
                new Locale(userDto.getPreferredLanguage())
        );
    }

    //Todo különböző státuszú (létrehozott, email nem verifikált/verifikált) userek esetén webtestclienttel tesztelni,
    // hogy a megfelelő művelet történik (lockolás, emailküldés, törlés).
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Request to delete (or lock) a user by id")
    @SecurityRequirements
    public void requestToDeleteUserById(HttpServletRequest httpServletRequest,
                               @PathVariable long id,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.DELETE_MAPPING, userDetails);
        if (authenticationService.lockUserByIdOrSendEmail(httpServletRequest.getRequestURI(),
                id,
                (userDto, verificationCode) -> sendEmailDeleteVerification(httpServletRequest, userDto, verificationCode))) {
            authenticationService.deleteUserById(httpServletRequest.getRequestURI(), id);
        }
    }
    private void sendEmailDeleteVerification(HttpServletRequest httpServletRequest, UserDto userDto, Integer verificationCode) {
        limiterService.resetDeleteVerficationLimit(userDto.getId());
        mailSenderService.sendDeleteValidification(
                httpServletRequest,
                userDto.getId(),
                verificationCode,
                userDto.getEmail(),
                API_AUTH + DELETE_VERIFICATION + VERIFICATION_CODE_PARAM,
                new Locale(userDto.getPreferredLanguage())
        );
    }

    //Todo webtestclienttel tesztelni, hogy a megfelelően preparált user lockolása / törlése megtörténik-e
    // hibás próbálkozások limitálásának tesztelése
    @GetMapping(DELETE_VERIFICATION)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete verification using a verification code")
    @SecurityRequirements
    public String deleteUserIfVerificationOk(HttpServletRequest httpServletRequest,
                                             @PathVariable long id,
                                             @RequestParam("verification-code") int verificationCode,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        limiterService.checkDeleteVerificationLimit(httpServletRequest.getRequestURI(), id);
        authenticationService.lockUserById(httpServletRequest.getRequestURI(), id, verificationCode);
        authenticationService.deleteUserById(httpServletRequest.getRequestURI(), id, verificationCode);
        return "User has been locked/deleted!";
    }
    //Todo webtestclienttel tesztelni a preparált user verifikálását
    // hibás próbálkozások limitálását
    @GetMapping(EMAIL_VERIFICATION)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Email address verification using a verification code")
    @SecurityRequirements
    public String enableUserIfEmailVerificationOk(
            HttpServletRequest httpServletRequest,
            @PathVariable long id,
            @RequestParam("verification-code") int verificationCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        limiterService.checkEmailVerificationLimit(httpServletRequest.getRequestURI(), id);
        authenticationService.enableUserIfVerificationOk(httpServletRequest.getRequestURI(), id, verificationCode);
        return "Successful verification!";
    }
    //Todo webtestclienttel tesztelni a preparált user verifikálását
    // hibás próbálkozások limitálását
    @GetMapping(UNLOCK_VERIFICATION)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Unlock user verification using a verification code")
    @SecurityRequirements
    public String enableUserIfUnlockVerificationOk(
            HttpServletRequest httpServletRequest,
            @PathVariable long id,
            @RequestParam("verification-code") int verificationCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        limiterService.checkUnlockLimit(httpServletRequest.getRequestURI(), id);
        authenticationService.enableUserIfVerificationOk(httpServletRequest.getRequestURI(), id, verificationCode);
        return "Successful verification!";
    }

    //Todo webtestclienttel tesztelni, hogy elküldi-e az emailt
    // mockolt mailSenderService-vel
    @DeleteMapping("/users/{id}/password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reset the user's password for the next password change",
            description = "The sent code remains valid until the timeout (10 minutes) expires," +
                    " a new login occurs, or the password is changed.")
    @SecurityRequirements
    public void resetPassword(HttpServletRequest httpServletRequest,
                              @PathVariable long id,
                              @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.DELETE_MAPPING, userDetails);
        authenticationService.resetPasswordForNextChange(httpServletRequest.getRequestURI(), id, LocalDateTime.now(),
                (userDto, resetPasswordCode) -> sendResetPasswordEmail(httpServletRequest, userDto, resetPasswordCode));
    }

    private void sendResetPasswordEmail(HttpServletRequest httpServletRequest, UserDto userDto, Integer resetPasswordCode) {
        mailSenderService.sendResetPassword(
                httpServletRequest,
                resetPasswordCode,
                userDto.getEmail(),
                new Locale(userDto.getPreferredLanguage())
        );
    }

    //Todo webtestclienttel tesztelni, hogy a jelszóváltoztatás sikeres-e vagy hibás authentikáció esetén visszautasítja-e
    // kísérletek számának limitálása
    @PutMapping("/users/change-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Change the user's password",
            description = "Upon successful authentication with the provided credentials.")
    @SecurityRequirements
    public void changePassword(
            HttpServletRequest httpServletRequest,
            @RequestBody ChangeCredentialsCommand changeCredentialsCommand,
            @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.PUT_MAPPING, userDetails, changeCredentialsCommand.toString());
        authenticationService.changePassword(httpServletRequest.getRequestURI(), changeCredentialsCommand, LocalDateTime.now());
    }

    //Todo webtestclient tesztelés, kísérletek számának limitálása
    @PostMapping("/credentials")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Log in with credentials to obtain a JWT token.")
    @SecurityRequirements
    public JwtTokenDto login(HttpServletRequest httpServletRequest,
                             @RequestBody CredentialsCommand credentials,
                             @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails, credentials.toString());
        return authenticationService.authenticate(httpServletRequest.getRequestURI(), credentials);
    }
}