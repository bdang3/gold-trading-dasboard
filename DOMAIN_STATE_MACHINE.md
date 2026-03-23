# DOMAIN STATE MACHINE

## MT5 Account transitions
- `PENDING -> PROCESSING` (start success)
- `PENDING -> FAILED` (start/verify failure)
- `PROCESSING -> STOPPED` (stop success)
- `STOPPED -> PENDING` (reconfigure/reset)
- `* -> FAILED` (mark-failed)

Invalid:
- Modify strategy/timeframe/risk rule while `PROCESSING`
- Delete while `PROCESSING`
- Start when not `VERIFIED`

## Admin action transitions
- Submit => `PENDING_ADMIN`
- Start success => `PROCESSING`
- Stop => `PENDING_RECONFIGURE`
- Reset => `PENDING_ADMIN`

## Port transitions
- `AVAILABLE -> OCCUPIED` on assign
- `OCCUPIED -> AVAILABLE` on stop/release/start rollback
- `DISABLED` is non-assignable

## Side effects
- Start success/failure and stop emit audit log, process log, and notification.
- Start failure rolls back assigned port.
