package com.nexra.hrms.nexra.modules.auth.service.impl.oauth;

import com.nexra.hrms.nexra.modules.auth.dto.request.OAuthClientCreateRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.OAuthClientResponse;
import com.nexra.hrms.nexra.modules.auth.exception.BusinessException;
import com.nexra.hrms.nexra.modules.auth.service.oauth.OAuthClientService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements OAuth2 client registration and retrieval using Spring Authorization Server repositories.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthClientServiceImpl implements OAuthClientService {

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
   // private final ModelMapper modelMapper;

    /**
     * Registers new OAuth client with authorization code, refresh token and client credentials grants.
     *
     * @param request registration payload
     * @return persisted client metadata
     */
    @Override
    @Transactional
    public OAuthClientResponse registerClient(final OAuthClientCreateRequest request) {
        log.info("OAuthClientServiceImpl() - registerClient() - Registering OAuth client, clientId={}", request.clientId());
        if (registeredClientRepository.findByClientId(request.clientId()) != null) {
            throw new BusinessException("OAuth client id already exists.");
        }

        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(request.clientId())
            .clientIdIssuedAt(Instant.now())
            .clientSecret(passwordEncoder.encode(request.clientSecret()))
            .clientName(request.clientName())
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri(request.redirectUri())
            .scope(OidcScopes.OPENID)
            .scopes(scopes -> scopes.addAll(request.scopes()))
            .clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(true).build())
            .tokenSettings(TokenSettings.builder().reuseRefreshTokens(false).build())
            .build();

        registeredClientRepository.save(client);
        return toResponse(client);
    }

    /**
     * Lists OAuth clients from persisted authorization server table.
     *
     * @return client metadata list
     */
    @Override
    @Transactional(readOnly = true)
    public List<OAuthClientResponse> listClients() {
        log.info("OAuthClientServiceImpl() - listClients() - Fetching OAuth clients list");
        String sql = "SELECT id, client_id, client_name, redirect_uris, client_id_issued_at, scopes FROM oauth2_registered_client";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OAuthClientResponse response = new OAuthClientResponse();
            response.setId(rs.getString("id"));
            response.setClientId(rs.getString("client_id"));
            response.setClientName(rs.getString("client_name"));
            response.setRedirectUri(rs.getString("redirect_uris"));
            response.setClientIdIssuedAt(rs.getTimestamp("client_id_issued_at").toInstant());
            response.setScopes(Set.of(rs.getString("scopes").split(",")));
            return response;
        });
    }

    /**
     * Maps registered client domain object into API response payload.
     *
     * @param client registered client entity
     * @return client response payload
     */
    private OAuthClientResponse toResponse(final RegisteredClient client) {
        OAuthClientResponse response = new OAuthClientResponse();
        response.setId(client.getId());
        response.setClientId(client.getClientId());
        response.setClientName(client.getClientName());
        response.setClientIdIssuedAt(client.getClientIdIssuedAt());
        response.setRedirectUri(client.getRedirectUris().stream().findFirst().orElse(""));
        response.setScopes(client.getScopes());
        return response;
    }
}
