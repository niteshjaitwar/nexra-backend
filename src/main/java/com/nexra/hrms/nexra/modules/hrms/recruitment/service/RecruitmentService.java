package com.nexra.hrms.nexra.modules.hrms.recruitment.service;

/**
 * Alias kept for backward compatibility during migration.
 * All implementations and injections should use {@link IRecruitmentService}.
 *
 * @author niteshjaitwar
 * @deprecated Use {@link IRecruitmentService} directly.
 */
@Deprecated(since = "1.1", forRemoval = true)
public interface RecruitmentService extends IRecruitmentService {
    // Intentionally empty — all methods are defined in IRecruitmentService.
}
