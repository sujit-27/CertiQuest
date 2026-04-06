package com.web.CertiQuest.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClerkJwksProvider {

    @Value("${clerk.jwks-url}")
    private String jwksUrl;

    // Thread-safe cache
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

    private long lastFetchTime = 0;

    // 5 minutes cache TTL
    private static final long CACHE_TTL = 300000;

    public PublicKey getPublicKey(String kid) throws Exception {

        // Try cache first
        if (keyCache.containsKey(kid) &&
                System.currentTimeMillis() - lastFetchTime < CACHE_TTL) {
            return keyCache.get(kid);
        }

        // Refresh keys
        refreshKeys();

        PublicKey key = keyCache.get(kid);

        // If still not found → force refresh again
        if (key == null) {
            refreshKeys();
            key = keyCache.get(kid);
        }

        if (key == null) {
            throw new RuntimeException("Public key not found for kid: " + kid);
        }

        return key;
    }

    // Thread-safe refresh
    private synchronized void refreshKeys() throws Exception {

        // Clear old cache
        keyCache.clear();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jwks = mapper.readTree(new URL(jwksUrl));
        JsonNode keys = jwks.get("keys");

        for (JsonNode keyNode : keys) {
            String kid = keyNode.get("kid").asText();
            String kty = keyNode.get("kty").asText();
            String alg = keyNode.get("alg").asText();

            if ("RSA".equals(kty) && "RS256".equals(alg)) {
                String n = keyNode.get("n").asText();
                String e = keyNode.get("e").asText();

                PublicKey publicKey = createPublicKey(n, e);
                keyCache.put(kid, publicKey);
            }
        }

        lastFetchTime = System.currentTimeMillis();
    }

    private PublicKey createPublicKey(String modulus, String exponent) throws Exception {

        byte[] modulusBytes = Base64.getUrlDecoder().decode(modulus);
        byte[] exponentBytes = Base64.getUrlDecoder().decode(exponent);

        BigInteger modulusBigInt = new BigInteger(1, modulusBytes);
        BigInteger exponentBigInt = new BigInteger(1, exponentBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulusBigInt, exponentBigInt);
        KeyFactory factory = KeyFactory.getInstance("RSA");

        return factory.generatePublic(spec);
    }
}
