package hu.benkoata.imdb.services.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class JwtService {
    public static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    private final String secretKey;
    @Getter
    private final long jwtExpirationSecs;

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, new Date(System.currentTimeMillis()));
    }
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return generateToken(extraClaims, userDetails, new Date(System.currentTimeMillis()));
    }
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, Date referenceDate) {
        return buildToken(extraClaims, userDetails, referenceDate, jwtExpirationSecs);
    }
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            Date referenceDate,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(referenceDate)
                .setExpiration(new Date(referenceDate.getTime() + expiration * 1000))
                .signWith(getSignInKey(), SIGNATURE_ALGORITHM)
                .compact();
    }
    public String extractUsername(String token, Date referenceDate) {
        return extractClaim(token, Claims::getSubject, referenceDate);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, Date referenceDate) {
        final Claims claims = extractAllClaims(token, referenceDate);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token, Date referenceDate) {
        return getParser(referenceDate)
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private JwtParserBuilder getParser(Date referenceDate) {
        JwtParserBuilder result = Jwts
                .parserBuilder();
        if (referenceDate != null) {
            result.setClock(new JwtTimeMachine(referenceDate));
        }
        return result;
    }
    public boolean isTokenValid(String token, UserDetails userDetails, Date referenceDate) {
        final String username = extractUsername(token, referenceDate);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, referenceDate);
    }

    private boolean isTokenExpired(String token, Date referenceDate) {
        Date now = referenceDate != null ? referenceDate : new Date();
        return extractExpiration(token, referenceDate).before(now);
    }

    private Date extractExpiration(String token, Date referenceDate) {
        return extractClaim(token, Claims::getExpiration, referenceDate);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
