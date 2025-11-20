package co.edu.uptc.RespuestaAutomatica.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret:mySecretKeyForJWTTokenGenerationAndValidationPurposesOnly123456789}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration:1200}")
    private long accessTokenExpirationSeconds;

    @Value("${app.jwt.refresh-token-expiration:86400}")
    private long refreshTokenExpirationSeconds;

    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";
    private static final String AUTH_SCHEME = "Bearer";

    public String generateAccessToken(String username, String email, String role) {
        return generateToken(username, email, ACCESS_TOKEN, accessTokenExpirationSeconds, role);
    }

    public String generateRefreshToken(String username, String email, String role) {
        return generateToken(username, email, REFRESH_TOKEN, refreshTokenExpirationSeconds, role);
    }

    private String generateToken(String username, String email, String tokenType, long expirationSeconds, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationSeconds * 1000);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .claim("email", email)
                .claim("tokenType", tokenType)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error al obtener el username del token: ", e);
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            logger.error("Error al obtener el role del token: ", e);
            return null;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("email", String.class);
        } catch (Exception e) {
            logger.error("Error al obtener el email del token: ", e);
            return null;
        }
    }

    public boolean validateAccessToken(String token) {
        return validateTokenType(token, ACCESS_TOKEN);
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenType(token, REFRESH_TOKEN);
    }

    private boolean validateTokenType(String token, String expectedType) {
        try {
            Claims claims = extractAllClaims(token);
            return expectedType.equals(claims.get("tokenType"));
        } catch (Exception e) {
            logger.error("Error validando el tipo de token: ", e);
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return buildParser()
                .parseClaimsJws(token)
                .getBody();
    }

    private JwtParser buildParser() {
        Key signingKey = getSigningKey();
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build();
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenWellFormed(String token) {
        try {
            JwtParser jwtParser = buildParser();
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expirado: ", e);
            return true;
        } catch (Exception e) {
            logger.error("Token no está bien formado: ", e);
            return false;
        }
    }

    public boolean isTokenMalformed(String token) {
        return !isTokenWellFormed(token);
    }

    public String getAuthScheme() { return AUTH_SCHEME; }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.error("JWT no válido: {}", e.getMessage());
        }
        return false;
    }
}
