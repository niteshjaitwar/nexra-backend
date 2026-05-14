package com.nexra.hrms.nexra.modules.hrms.onboarding.service;

/**
 * Alias kept for backward compatibility during migration.
 * All implementations and injections should use {@link IOnboardingService}.
 *
 * @author niteshjaitwar
 * @deprecated Use {@link IOnboardingService} directly.
 */
@Deprecated(since = "1.1", forRemoval = true)
public interface OnboardingService extends IOnboardingService {
    // Intentionally empty — all methods are defined in IOnboardingService.
}
