package agora.security;

import agora.exceptions.AgoraAuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtHandler {

    @Value(value = "${jwt.secret.key}")
    private String secretKey;

    @Value(value = "${access.life.time}")
    private Long accessLifeTime;

    @Value(value = "${refresh.life.time}")
    private Long refreshLifeTime;

    public JwtHandler() {}

    public String generateAccessToken(String username, Long userId, String avatarUrl) {
        long validity = "stat-bot".equalsIgnoreCase(username) ? 31_536_000_000L : accessLifeTime;

        return this.buildToken(username, validity, Map.of(
                "id", userId,
                "avatarUrl", avatarUrl != null ? avatarUrl : "",
                "type", "access"
        ));
    }

    public String generateRefreshToken(String username, Long userId) {
        return this.buildToken(username, refreshLifeTime, Map.of(
                "id", userId,
                "type", "refresh"
        ));
    }

    private String buildToken(String subject, Long validityduration, Map<String, Object> claims) {
        Date now = new Date();

        Date expiredAt = new Date(now.getTime() + validityduration);

        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiredAt)
                .signWith(this.getSignInkey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(this.getSignInkey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        // Достаем как Object, а потом превращаем в String
        Object id = claims.get("id");
        return id != null ? String.valueOf(id) : null;
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInkey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            throw new AgoraAuthException("Token expired");
        } catch (MalformedJwtException ex) {
            throw new AgoraAuthException("Token invalid");
        } catch (SignatureException ex) {
            throw new AgoraAuthException("Token Signature Incorrect");
        } catch (IllegalArgumentException ex) {
            throw new AgoraAuthException("Token is null or empty");
        }
    }

    public Claims getValidClaims(String token, String expectedType) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(this.getSignInkey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenType = claims.get("type", String.class);
            if (!expectedType.equals(tokenType)) {
                log.warn("Security alert: Attempt to use {} token as {} token", tokenType, expectedType);
                throw new AgoraAuthException(String.format("Invalid token type. %s token required.", expectedType));
            }

            return claims;
        } catch (ExpiredJwtException ex) {
            throw new AgoraAuthException("Token expired");
        } catch (MalformedJwtException ex) {
            throw new AgoraAuthException("Token invalid");
        } catch (SignatureException ex) {
            throw new AgoraAuthException("Token Signature Incorrect");
        } catch (IllegalArgumentException ex) {
            throw new AgoraAuthException("Token is null or empty");
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(this.getSignInkey()).build().parse(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("Token expired");
        } catch (MalformedJwtException ex) {
            log.warn("Token invalid");
        } catch (SignatureException ex) {
            log.warn("Token Signature Incorrect");
        } catch (IllegalArgumentException ex) {
            log.warn("Token is null or empty");
        }
        return false;
    }

    public boolean isAccessToken(String token) {
        String type = extractAllClaims(token).get("type", String.class);
        return "access".equals(type);
    }

    private Key getSignInkey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
