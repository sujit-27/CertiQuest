package com.web.CertiQuest.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

@Component
public class ClerkJwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ClerkJwtAuthFilter.class);

    @Value("${clerk.issuer}")
    private String clerkIssuer;

    @Autowired
    private ClerkJwksProvider jwksProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip certain endpoints
        if (path.contains("/webhooks") || path.contains("/leaderboard") ||
                path.contains("/download") || path.equals("/api/quiz/create") ||
                path.equals("/users/points")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            // Decode JWT header to get 'kid'
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                logger.warn("Invalid JWT token format");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT Token format");
                return;
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode headerNode = mapper.readTree(headerJson);

            if (!headerNode.has("kid")) {
                logger.warn("JWT header missing 'kid'");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token header is missing 'kid'");
                return;
            }

            String kid = headerNode.get("kid").asText();
            PublicKey publicKey = jwksProvider.getPublicKey(kid);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .requireIssuer(clerkIssuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String clerkId = claims.getSubject();
            if (clerkId == null || clerkId.isEmpty()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "JWT token subject is missing");
                return;
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            clerkId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);

        } catch (SignatureException e) {
            logger.error("JWT signature verification failed", e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT Signature");
        } catch (Exception e) {
            logger.error("JWT validation error", e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT Token: " + e.getMessage());
        }
    }
}
