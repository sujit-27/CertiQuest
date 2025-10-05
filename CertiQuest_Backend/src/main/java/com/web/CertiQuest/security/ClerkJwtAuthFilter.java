package com.web.CertiQuest.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Value("${clerk.issuer}")
    private String clerkIssuer;

    @Autowired
    private ClerkJwksProvider jwksProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // ===== Public endpoints that don't need authentication =====
        if (uri.startsWith("/api/v1.0/webhooks") ||   // Clerk webhooks
                uri.startsWith("/api/certificates/download") || // Certificate downloads
                uri.startsWith("/api/leaderboard") ||
                request.getMethod().equalsIgnoreCase("OPTIONS")) { // Preflight
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization header missing or invalid");
            return;
        }

        try {
            String token = authHeader.substring(7);
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length < 3) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT Token format");
                return;
            }

            // Decode JWT header to get "kid"
            String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode headerNode = mapper.readTree(headerJson);
            if (!headerNode.has("kid")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token header missing 'kid'");
                return;
            }

            String kid = headerNode.get("kid").asText();
            PublicKey publicKey = jwksProvider.getPublicKey(kid);

            // Parse JWT claims: LEGACY JJWT SYNTAX
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .setAllowedClockSkewSeconds(60)
                    .requireIssuer(clerkIssuer)
                    .build() // returns JwtParser
                    .parseClaimsJws(token) // parse here
                    .getBody();


            String clerkId = claims.getSubject();
            String role = claims.get("role", String.class);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            clerkId,
                            null,
                            Collections.singletonList(
                                    new SimpleGrantedAuthority(
                                            role != null ? "ROLE_" + role.toUpperCase() : "ROLE_USER"
                                    )
                            )
                    );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT Token: " + e.getMessage());
        }
    }
}