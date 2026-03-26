package com.nexra.hrms.nexra.modules.auth.service.impl;

import com.nexra.hrms.nexra.modules.auth.dto.request.GrantProductAccessRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.ProductAccessResponse;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.entity.UserProductAccess;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import com.nexra.hrms.nexra.modules.auth.exception.BusinessException;
import com.nexra.hrms.nexra.modules.auth.exception.ResourceNotFoundException;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserProductAccessRepository;
import com.nexra.hrms.nexra.modules.auth.service.ProductAccessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages product-level access lifecycle for tenant users across HRMS and CRM products.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductAccessServiceImpl implements ProductAccessService {

    private final UserAccountRepository userAccountRepository;
    private final UserProductAccessRepository userProductAccessRepository;
    private final ModelMapper modelMapper;

    /**
     * Returns all product access grants for a user.
     *
     * @param userId user account identifier
     * @return list of product access responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductAccessResponse> getProductAccess(final UUID userId) {
        log.info("ProductAccessServiceImpl() - getProductAccess() - Fetching product access, userId={}", userId);
        UserAccount user = resolveUser(userId);
        return userProductAccessRepository.findByUser(user).stream()
            .map(access -> {
                ProductAccessResponse response = modelMapper.map(access, ProductAccessResponse.class);
                response.setUserId(user.getId());
                return response;
            })
            .toList();
    }

    /**
     * Grants a user access to a specific product with an assigned role.
     *
     * @param userId target user account identifier
     * @param request product and role assignment payload
     * @param grantedByUserId identifier of the admin performing the grant
     * @return created product access response
     */
    @Override
    @Transactional
    public ProductAccessResponse grantAccess(
        final UUID userId,
        final GrantProductAccessRequest request,
        final UUID grantedByUserId
    ) {
        log.info("ProductAccessServiceImpl() - grantAccess() - Granting product access, userId={}, product={}, role={}",
            userId, request.product(), request.productRole());
        UserAccount user = resolveUser(userId);

        if (userProductAccessRepository.existsByUserAndProduct(user, request.product())) {
            throw new BusinessException("User already has access to product: " + request.product().name());
        }

        UserProductAccess access = new UserProductAccess();
        access.setUser(user);
        access.setProduct(request.product());
        access.setProductRole(request.productRole());
        access.setGrantedAt(Instant.now());
        access.setGrantedBy(grantedByUserId != null ? grantedByUserId.toString() : null);

        UserProductAccess saved = userProductAccessRepository.save(access);
        log.info("ProductAccessServiceImpl() - grantAccess() - Product access granted, userId={}, product={}",
            userId, request.product());

        ProductAccessResponse response = modelMapper.map(saved, ProductAccessResponse.class);
        response.setUserId(userId);
        return response;
    }

    /**
     * Revokes a user's access to a specific product.
     *
     * @param userId target user account identifier
     * @param product product type to revoke
     */
    @Override
    @Transactional
    public void revokeAccess(final UUID userId, final ProductType product) {
        log.info("ProductAccessServiceImpl() - revokeAccess() - Revoking product access, userId={}, product={}",
            userId, product);
        UserAccount user = resolveUser(userId);

        if (!userProductAccessRepository.existsByUserAndProduct(user, product)) {
            throw new ResourceNotFoundException("No access grant found for userId=" + userId + " and product=" + product.name());
        }

        userProductAccessRepository.deleteByUserAndProduct(user, product);
        log.info("ProductAccessServiceImpl() - revokeAccess() - Product access revoked, userId={}, product={}",
            userId, product);
    }

    /**
     * Resolves a user account by identifier or throws resource not found.
     *
     * @param userId user account identifier
     * @return resolved user account
     */
    private UserAccount resolveUser(final UUID userId) {
        return userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found for id: " + userId));
    }
}
