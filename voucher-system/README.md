# WiFi Access Voucher System

A small Spring Boot service that generates timed WiFi access vouchers and
tracks/expires client sessions, with a minimal web dashboard.

## Features

- Generate random, unique voucher codes for fixed durations: 10 min, 20 min,
  30 min, 1 hr, 1 hr 30 min, 2 hr, 3 hr, 5 hr.
- Vouchers are single-use by default: a code grants access starting the
  moment a client connects/redeems it, for exactly its assigned duration.
- A background scheduler sweeps active sessions every few seconds and
  expires (simulated-disconnects) any whose duration has elapsed.
- Dashboard grouped by time slot showing each code's status
  (Unused / Active with time remaining / Expired), with buttons to generate
  new codes per slot and to force-disconnect an active session.
- Router/captive-portal integration is simulated via `connect(code)` /
  `disconnect(code)` service calls — no real network hardware involved.

## Running

```bash
cd voucher-system
mvn spring-boot:run
```

Then open http://localhost:8080 for the dashboard. H2 console (if needed
for debugging) is available at http://localhost:8080/h2-console using JDBC
URL `jdbc:h2:mem:voucherdb`, user `sa`, empty password.

## Tests

```bash
mvn test
```

## REST API

| Method | Path                          | Description                                   |
|--------|-------------------------------|------------------------------------------------|
| GET    | `/api/durations`              | List available voucher duration slots          |
| GET    | `/api/vouchers`                | List all vouchers                              |
| GET    | `/api/vouchers/active`         | List currently connected (ACTIVE) vouchers     |
| GET    | `/api/vouchers/dashboard`      | Vouchers grouped by duration slot              |
| POST   | `/api/vouchers/generate?duration=ONE_HOUR` | Generate a new code for a slot   |
| POST   | `/api/vouchers/{code}/connect` | Simulate a client redeeming/connecting a code  |
| POST   | `/api/vouchers/{code}/disconnect` | Force-disconnect an active session          |

Duration slot keys: `TEN_MIN`, `TWENTY_MIN`, `THIRTY_MIN`, `ONE_HOUR`,
`ONE_HOUR_THIRTY_MIN`, `TWO_HOUR`, `THREE_HOUR`, `FIVE_HOUR`.

## Notes

- Storage is an in-memory H2 database (data resets on restart).
- Codes are single-use unless generated with `singleUse=false`.
