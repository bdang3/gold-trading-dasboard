# BACKEND IMPLEMENTATION PLAN

## Module breakdown
- `auth`: register/login/refresh/logout/forgot/reset/change-password + lockout + rate limiting.
- `users`: me/profile update.
- `plans`, `strategies`, `riskrules`, `ports`: admin master data.
- `mt5accounts`: user/admin account management with lifecycle rules.
- `botoperations` + `infrastructure.runtime`: operation abstraction + simulated runtime.
- `notifications`, `trades`, `reports`, `dashboard`: user/admin data views.
- `auditlogs`, `processlogs`: business and technical logs.

## Architecture decisions
- Modular monolith with layered per-module packaging.
- Scheduler + Redis-ready coordination selected over RabbitMQ for lower operational complexity in MVP.
- JWT access + hashed refresh tokens.
- Transactional MT5 + Port lifecycle operations.

## Implementation order
1. Frontend inspection and route/data contract extraction.
2. Spring Boot scaffold + Flyway schema + entities/repos.
3. Auth/security/profile.
4. Master-data APIs.
5. MT5 lifecycle + runtime simulation.
6. Notifications/trades/reports/logs/dashboard.
7. Docker, seed data, tests, docs.
