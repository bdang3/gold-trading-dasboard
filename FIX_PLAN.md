# FIX_PLAN

## Completed in this pass
- Replaced map-based admin dashboard summary contract with explicit DTO:
  - `AdminDashboardSummaryResponse`
  - `DashboardService.summary()` now returns typed contract directly.
- Completed admin MT5 account list server-table behavior:
  - added `AdminMt5AccountsQueryRequest`
  - implemented pageable + sortable + filterable + searchable query logic
  - filters now support:
    - `status`, `verificationStatus`, `strategyId`, `strategyCode`
    - `timeframe`, `riskRuleId`, `riskRuleCode`, `broker`, `portStatus`, `userId`
  - search supports:
    - account number
    - broker
    - owner full name/email (resolved to user ids)
- Added repository support required by filters:
  - mt5 specifications
  - strategy/risk-rule code lookup
  - port status lookup
- Added integration contract tests in `AdminContractsIT`:
  - dashboard typed/stable response fields
  - admin mt5 list paging/filter/sort/search expectations.

## Important environment note
- Current environment cannot compile Java 21 project (`release version 21 not supported`), so Maven test run cannot complete here.
- Test classes are structured for CI/dev Java 21 + Docker-enabled environments.

## UI integration blockers removed
- Admin dashboard now has a stable typed response for direct frontend mapping.
- Admin MT5 table endpoint now supports practical server-driven behavior for pagination, sorting, filtering, and search.
- Frontend mapping docs now reflect concrete query params and response contracts.

## Remaining optional improvements
- Add DB indexes for heavily queried MT5 filters/search columns for large datasets.
- Add explicit OpenAPI parameter docs/examples for admin MT5 list filters.
