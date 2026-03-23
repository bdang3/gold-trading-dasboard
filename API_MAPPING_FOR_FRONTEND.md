# API MAPPING FOR FRONTEND

## Response shape
Success:
```json
{ "success": true, "data": ... }
```
Error:
```json
{ "success": false, "code": "...", "message": "...", "errors": [...] }
```

## List response shape
Paged list endpoints return:
```json
{
  "items": [...],
  "page": 0,
  "pageSize": 20,
  "totalItems": 100,
  "totalPages": 5
}
```

## Auth/session mapping
- `POST /api/v1/auth/register` (public)
- `POST /api/v1/auth/login` (public)
- `POST /api/v1/auth/refresh` (public)
- `POST /api/v1/auth/forgot-password` (public)
- `POST /api/v1/auth/reset-password` (public)
- `GET /api/v1/auth/me` (authenticated)
- `POST /api/v1/auth/logout` (authenticated)
- `POST /api/v1/auth/change-password` (authenticated)

Role guards:
- `/api/v1/admin/**` => ADMIN only
- user-area APIs => authenticated USER/ADMIN where applicable

## Admin pages/modules mapping
- `/admin` -> `GET /api/v1/admin/dashboard/summary`
  - typed data fields:
    - `totalUsers`, `totalMt5Accounts`
    - `pendingAccounts`, `processingAccounts`, `stoppedAccounts`, `failedAccounts`
    - `availablePorts`, `occupiedPorts`, `disabledPorts`
    - `recentAlertsCount`

- `/admin/users` -> `GET /api/v1/admin/users`
  - query: `page`, `pageSize`, `sortBy`, `sortOrder`, `search`
  - response: paged `items[]` of admin user list DTO

- `/admin/users/:id`:
  - `GET /api/v1/admin/users/{id}`
  - `PATCH /api/v1/admin/users/{id}`
  - `POST /api/v1/admin/users/{id}/suspend`
  - `POST /api/v1/admin/users/{id}/activate`

- `/admin/accounts` -> `GET /api/v1/admin/mt5-accounts`
  - query:
    - table: `page`, `pageSize`, `sortBy`, `sortOrder`, `search`
    - filters: `status`, `verificationStatus`, `strategyId`, `strategyCode`, `timeframe`, `riskRuleId`, `riskRuleCode`, `broker`, `portStatus`, `userId`
  - search behavior:
    - account number
    - broker
    - user full name/email (resolved to owner ids)
  - response: paged `items[]` of `Mt5AccountResponse`

- `/admin/accounts/:id`:
  - `GET /api/v1/admin/mt5-accounts/{id}`
  - `PATCH /api/v1/admin/mt5-accounts/{id}`
  - `POST /api/v1/admin/mt5-accounts/{id}/assign-port`
  - `POST /api/v1/admin/mt5-accounts/{id}/start`
  - `POST /api/v1/admin/mt5-accounts/{id}/stop`
  - `POST /api/v1/admin/mt5-accounts/{id}/reset`
  - `POST /api/v1/admin/mt5-accounts/{id}/release-port`
  - `POST /api/v1/admin/mt5-accounts/{id}/mark-failed`

- `/admin/ports` -> `GET /api/v1/admin/ports` (+ create/update endpoints when needed)
- `/admin/logs` -> `GET /api/v1/admin/logs` (paged)
- `/admin/process-logs` -> `GET /api/v1/admin/process-logs` (paged)

## User modules mapping (integration-relevant)
- `/app/accounts` + `/app/accounts/:id`:
  - `GET /api/v1/mt5-accounts/my`
  - `GET /api/v1/mt5-accounts/{id}`
  - `POST /api/v1/mt5-accounts`
  - `PATCH /api/v1/mt5-accounts/{id}`
  - lifecycle actions (`verify`, `submit`, `stop`, `delete`) per existing endpoints
- reports/trades:
  - `GET /api/v1/reports/my/summary` (typed summary DTO)
  - `GET /api/v1/reports/my/trades`
  - `GET /api/v1/trades/my`
- notifications:
  - `POST /api/v1/notifications/{id}/read` (owner-scoped)

## Contract notes
- Forgot password now returns generic success message and includes `demoResetToken` in response for MVP integration/testing.
- Report summary response is typed (`totalTrades, winRate, totalProfit, accounts`).
- Admin dashboard summary is fully typed (no `Map<String,Object>` contract).

## Enum/status notes for frontend
- `AccountStatus`: `PENDING | PROCESSING | STOPPED | FAILED`
- `VerificationStatus`: `UNVERIFIED | VERIFYING | VERIFIED | FAILED`
- `AdminActionState`: `PENDING_ADMIN | PROCESSING | PENDING_RECONFIGURE | RECONFIGURED`
- `PortStatus`: `AVAILABLE | OCCUPIED | DISABLED`
- `RoleType`: `USER | ADMIN`
- `UserStatus`: `ACTIVE | SUSPENDED | LOCKED`
