package com.nexra.hrms.nexra.modules.auth.dto.response;

import com.nexra.hrms.nexra.modules.auth.enums.AccountType;
import com.nexra.hrms.nexra.modules.auth.enums.UserRole;
import com.nexra.hrms.nexra.modules.auth.enums.UserStatus;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines UserProfileResponse component.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Getter
@Setter
public class UserProfileResponse {

    private UUID id;
    private String tenantCode;
    private String email;
    private String firstName;
    private String lastName;
    private boolean emailVerified;
    private boolean mfaEnabled;
    private AccountType accountType;
    private UserStatus status;
    private Set<UserRole> roles;
}
