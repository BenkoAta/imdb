package hu.benkoata.imdb.configurations;

import hu.benkoata.imdb.services.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) {
        final String authHeader = request.getHeader("Authorization");
        try {
            if (authHeader == null) {
                filterChain.doFilter(request, response);
            } else {
                final String jwt = getJwtTokenFromHeader(authHeader);
                final String username = jwtService.extractUsername(jwt, null);
                if (username != null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(jwt, userDetails, null)) {
                        SecurityContextHolder.getContext().setAuthentication(buildAuthToken(userDetails, request));
                    }
                }
                filterChain.doFilter(request, response);
            }
        } catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }

    private String getJwtTokenFromHeader(String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            throw new AuthenticationServiceException(
                    "Not possible to use authentication: " + authHeader.substring(0, 6) + "!");
        }
        return authHeader.substring(7);
    }

    private UsernamePasswordAuthenticationToken buildAuthToken(UserDetails userDetails,
                                                               HttpServletRequest request) {
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        result.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return result;
    }
}
