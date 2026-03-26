package com.nexra.hrms.nexra.modules.auth.repository;

import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.entity.UserProductAccess;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides persistence operations for user product access grant records.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface UserProductAccessRepository extends JpaRepository<UserProductAccess, UUID> {

    /**
     * Finds all product access grants for a given user.
     *
     * @param user user account
     * @return list of product access grants
     */
    List<UserProductAccess> findByUser(UserAccount user);

    /**
     * Finds a specific product access grant for a given user and product.
     *
     * @param user user account
     * @param product product type
     * @return optional product access grant
     */
    Optional<UserProductAccess> findByUserAndProduct(UserAccount user, ProductType product);

    /**
     * Checks whether a product access grant exists for a given user and product.
     *
     * @param user user account
     * @param product product type
     * @return true when access grant exists
     */
    boolean existsByUserAndProduct(UserAccount user, ProductType product);

    /**
     * Deletes a specific product access grant for a given user and product.
     *
     * @param user user account
     * @param product product type
     */
    void deleteByUserAndProduct(UserAccount user, ProductType product);
}
