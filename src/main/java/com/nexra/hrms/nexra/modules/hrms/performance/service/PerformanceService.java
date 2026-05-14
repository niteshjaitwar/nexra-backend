package com.nexra.hrms.nexra.modules.hrms.performance.service;

/**
 * Alias kept for backward compatibility during migration.
 * All implementations and injections should use {@link IPerformanceService}.
 *
 * @author niteshjaitwar
 * @deprecated Use {@link IPerformanceService} directly.
 */
@Deprecated(since = "1.1", forRemoval = true)
public interface PerformanceService extends IPerformanceService {
    // Intentionally empty — all methods are defined in IPerformanceService.
}
