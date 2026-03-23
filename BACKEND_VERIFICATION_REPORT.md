# BACKEND VERIFICATION REPORT

## Scope
Verification and targeted completion for frontend integration blockers in this pass:
- typed admin dashboard summary contract
- admin MT5 account list server-driven behavior (pagination/filter/sort/search)
- frontend mapping/fix documentation

## Verified and present (already in code)
- Auth route hardening in `SecurityConfig`:
  - public only: register/login/refresh/forgot-password/reset-password
  - private: `GET /auth/me`, `POST /auth/logout`, `POST /auth/change-password`
  - `/api/v1/admin/**` restricted to ADMIN.
- Password reset flow:
  - `PasswordResetToken` entity + Flyway migration/table
  - hashed token storage, expiry validation, single-use semantics
  - reset password updates hash and invalidates token.
- Notification ownership safety:
  - mark-read uses owner-scoped lookup (`findByIdAndUserId`), preventing ID guessing.
- MT5 lifecycle/port lifecycle policies:
  - `Mt5AccountLifecyclePolicy` and `PortLifecyclePolicy` are present and enforced.
- AES-GCM MT5 password encryption:
  - random IV per encryption in crypto service
  - encrypted MT5 password stored at rest
  - raw MT5 password not exposed in API response DTOs.

## Implemented in this pass
- Replaced map-based admin dashboard summary response with typed DTO:
  - `AdminDashboardSummaryResponse`
  - `DashboardService.summary()` now returns typed DTO directly.
- Completed admin MT5 list query contract:
  - added `AdminMt5AccountsQueryRequest`
  - endpoint now accepts and forwards:
    - `page`, `pageSize`, `sortBy`, `sortOrder`, `search`
    - `status`, `verificationStatus`, `strategyId`, `strategyCode`
    - `timeframe`, `riskRuleId`, `riskRuleCode`, `broker`, `portStatus`, `userId`
  - implemented DB-backed filtering with `Specification` + pageable sorting.
  - added practical search across account number, broker, user full name/email (user-id assisted).
- Added/updated repositories to support filters:
  - `MT5AccountRepository` now supports `JpaSpecificationExecutor`
  - `PortMasterRepository.findByStatus(...)`
  - strategy/risk-rule code lookups for code-based filters.
- Added integration tests for new contract behavior:
  - `AdminContractsIT`:
    - dashboard typed structure assertion
    - mt5 list pagination/filter/sort/search behavior assertions.

## Still incomplete
- None for the requested blockers in this pass.
- Environment limitation: local Java runtime here does not support Java 21 compilation (`release version 21 not supported`), so test execution must be validated in Java 21 + Docker-enabled CI/dev environment.
