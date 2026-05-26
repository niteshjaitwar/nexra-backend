package com.nexra.hrms.nexra.modules.hrms.onboarding.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedOnboardingUser(UUID userId, String email, String tenantCode, Set<String> roles) {
}
