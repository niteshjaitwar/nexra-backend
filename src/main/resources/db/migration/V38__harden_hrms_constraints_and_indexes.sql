ALTER TABLE onboarding_tasks
    ADD CONSTRAINT fk_onboarding_task_plan
        FOREIGN KEY (plan_id) REFERENCES onboarding_plans (plan_id);

ALTER TABLE recruitment_candidates
    ADD CONSTRAINT fk_recruitment_candidate_job
        FOREIGN KEY (job_id) REFERENCES recruitment_jobs (job_id);

ALTER TABLE recruitment_candidate_stage_history
    ADD CONSTRAINT fk_recruitment_stage_history_candidate
        FOREIGN KEY (candidate_id) REFERENCES recruitment_candidates (candidate_id);

CREATE UNIQUE INDEX ux_onboarding_plan_tenant_employee_name
    ON onboarding_plans (tenant_code, employee_id, plan_name);

CREATE INDEX ix_onboarding_tasks_plan_status
    ON onboarding_tasks (plan_id, status);

CREATE INDEX ix_performance_goals_tenant_employee_status
    ON performance_goals (tenant_code, employee_id, status);

CREATE UNIQUE INDEX ux_performance_review_tenant_employee_cycle
    ON performance_reviews (tenant_code, employee_id, review_cycle);

CREATE INDEX ix_recruitment_jobs_tenant_status
    ON recruitment_jobs (tenant_code, status);

CREATE INDEX ix_recruitment_candidates_tenant_job_stage
    ON recruitment_candidates (tenant_code, job_id, stage);

CREATE INDEX ix_recruitment_stage_history_candidate
    ON recruitment_candidate_stage_history (candidate_id);

CREATE INDEX ix_expense_claims_tenant_employee_status_submitted
    ON ex_claims (tenant_code, employee_id, status, claim_date);
