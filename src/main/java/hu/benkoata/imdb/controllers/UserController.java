package hu.benkoata.imdb.controllers;

import hu.benkoata.imdb.dtos.UserDto;
import hu.benkoata.imdb.services.Logger;
import hu.benkoata.imdb.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Tag(name = "user related operations")
@RequiredArgsConstructor
@RequestMapping("/api/users")
@SuppressWarnings("unused")
public class UserController {
    private final UserService userService;
    @GetMapping("/me")
    @Operation(summary = "get current user data")
    @SecurityRequirement(name = "BearerAuth")
    public UserDto getMe(HttpServletRequest httpServletRequest,
                         @AuthenticationPrincipal UserDetails userDetails) {
        Logger.logRequest(log::info, httpServletRequest, Logger.GET_MAPPING, userDetails);
        return userService.getUserDetails(userDetails);
    }
}
