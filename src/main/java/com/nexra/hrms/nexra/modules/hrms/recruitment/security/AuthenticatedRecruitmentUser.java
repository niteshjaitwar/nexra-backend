package com.nexra.hrms.nexra.modules.hrms.recruitment.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedRecruitmentUser(UUID userId, String email, String tenantCode, Set<String> roles) {
}
