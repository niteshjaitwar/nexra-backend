package com.nexra.hrms.nexra.modules.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nexra.hrms.nexra.common.web.SecurityHeadersCustomizer;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.cert.Certificate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures Spring Authorization Server infrastructure and endpoint security chain.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
@RequiredArgsConstructor
public class AuthorizationServerConfig {

    private final ResourceLoader resourceLoader;

    /**
     * Configures high-priority security chain for authorization server endpoints.
     *
     * @param http HttpSecurity builder
     * @return configured authorization server filter chain
     * @throws Exception when security setup fails
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(final HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http
            .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
            .with(authorizationServerConfigurer, Customizer.withDefaults())
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
        SecurityHeadersCustomizer.apply(http);
        return http.build();
    }

    /**
     * Creates JDBC-backed registered client repository.
     *
     * @param jdbcTemplate JDBC template
     * @return registered client repository
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(final JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * Creates JDBC-backed OAuth2 authorization service.
     *
     * @param jdbcTemplate JDBC template
     * @param registeredClientRepository registered client repository
     * @return authorization service
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(
        final JdbcTemplate jdbcTemplate,
        final RegisteredClientRepository registeredClientRepository
    ) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * Creates JDBC-backed authorization consent service.
     *
     * @param jdbcTemplate JDBC template
     * @param registeredClientRepository registered client repository
     * @return authorization consent service
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(
        final JdbcTemplate jdbcTemplate,
        final RegisteredClientRepository registeredClientRepository
    ) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * Creates ephemeral RSA key source for token signing.
     *
     * @return JWK source
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource(final AuthProperties authProperties) {
        RSAKey rsaKey = authProperties.getOauth2().isEphemeralKeyEnabled()
            ? generateRsa()
            : loadRsaFromKeystore(authProperties);
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, context) -> jwkSelector.select(jwkSet);
    }

    /**
     * Creates authorization server settings with configured issuer.
     *
     * @param authProperties auth property container
     * @return authorization server settings
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings(final AuthProperties authProperties) {
        return AuthorizationServerSettings.builder()
            .issuer(authProperties.getOauth2().getIssuer())
            .build();
    }

    /**
     * Generates in-memory RSA key pair for signing JWK exposure.
     *
     * @return RSA JWK
     */
    private RSAKey generateRsa() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to create RSA key for authorization server", exception);
        }
    }

    /**
     * Loads RSA key pair from configured PKCS12 keystore for stable token signing across restarts.
     *
     * @param authProperties auth property container
     * @return RSA JWK loaded from keystore
     */
    private RSAKey loadRsaFromKeystore(final AuthProperties authProperties) {
        AuthProperties.Oauth2 oauth2 = authProperties.getOauth2();
        String location = oauth2.getKeystoreLocation();
        String storePassword = oauth2.getKeystorePassword();
        String alias = oauth2.getKeystoreKeyAlias();
        String keyPassword = oauth2.getKeystoreKeyPassword();

        if (isBlank(location) || isBlank(storePassword) || isBlank(alias) || isBlank(keyPassword)) {
            throw new IllegalStateException("OAuth2 keystore configuration is incomplete.");
        }

        try {
            Resource resource = resourceLoader.getResource(location);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (var inputStream = resource.getInputStream()) {
                keyStore.load(inputStream, storePassword.toCharArray());
            }

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword.toCharArray());
            Certificate certificate = keyStore.getCertificate(alias);
            if (privateKey == null || certificate == null) {
                throw new IllegalStateException("Keystore alias not found or missing key material: " + alias);
            }

            RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
            return new RSAKey.Builder(publicKey)
                .privateKey(rsaPrivateKey)
                .keyID(alias)
                .build();
        } catch (IOException | java.security.GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to load OAuth2 signing key from keystore.", exception);
        }
    }

    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
