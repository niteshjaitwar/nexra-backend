package com.nexra.hrms.nexra.modules.hrms.expense.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedExpenseUser(UUID userId, String email, String tenantCode, Set<String> roles) {
}

