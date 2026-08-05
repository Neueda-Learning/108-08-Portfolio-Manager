# Fund Manager Feature — Change Log & Setup Notes

This document summarizes everything added/changed to introduce the fund manager perspective,
and lists the manual steps required for existing (non-fresh) **MySQL** deployments.

## Summary

Fund managers can now log in with a separate account, see every customer (owner) account with a
performance snapshot, add/remove customer accounts, and drill into any customer's dashboard and
holdings to view charts and manage their portfolio — using the same look and feel as the existing
customer-facing pages. Existing owner-facing pages, routes, and behavior are unchanged.

Seeded fund manager login: **username `fundmanager`, password `FundManager123!`** (override via
`MANAGER_USERNAME` / `MANAGER_PASSWORD` env vars, same pattern as the existing `OWNER_USERNAME` /
`OWNER_PASSWORD`).

### Update (follow-up changes)

After the initial rollout, three additional changes were made:

1. **Customer profile fields — `name` and `email`.** Customers now have `name` and `email` in
   addition to `username`. The fund manager provides both when creating a customer, they're shown
   in the customer list, on the customer's dashboard (as viewed by the fund manager), and the
   owner sees their own name/email on their own dashboard.
2. **Biometric / WebAuthn login removed entirely.** All WebAuthn code (backend service, crypto/CBOR
   helpers, credential entity/repository, frontend `webauthn.js` helper, sidebar "Enable
   Biometrics" button, login page "Sign in with biometrics" button) has been deleted. Login is now
   password-only.
3. **Customers can see their fund manager's username.** Each customer (`User`) now has a
   `managedBy` reference to the fund manager who created them; `/api/auth/me` returns
   `managerUsername`, and it's displayed in the owner's sidebar.

## Why a data-model change was required

The app previously had **no ownership concept** at all — `Portfolio` had no link to a `User`, and
every endpoint operated on a single global set of data (the app assumed exactly one customer ever
existed). To let a fund manager manage *multiple, isolated* customers, every portfolio/holding now
belongs to a specific owner (`User`). This is a foundational, necessary change — it does not alter
the UI/behavior of the existing owner pages, since a single pre-existing owner still sees exactly
the same data as before.

## Backend changes

| File | Change |
|---|---|
| `model/Role.java` | **New.** Enum: `OWNER`, `FUND_MANAGER`. |
| `model/User.java` | Added `role` column (defaults to `OWNER`); new constructor accepting a role. Added `name`, `email`, and a self-referencing `managedBy` (`User`, nullable `ManyToOne`, `managed_by_id` FK) pointing at the fund manager who created the account. |
| `model/Portfolio.java` | Added `owner` (`User`, required `ManyToOne`, `owner_id` FK column); new constructor accepting an owner. |
| `repository/UserRepository.java` | Added `findByRole`, `existsByUsername`. |
| `repository/PortfolioRepository.java` | Added `findByOwnerId`, `findByIdAndOwnerId`. |
| `repository/HoldingRepository.java` | Added `findByPortfolio_Owner_Id`, `findByIdAndPortfolio_Owner_Id`. |
| `service/PortfolioService.java` | All methods now scoped by `ownerId`/`owner` (multi-tenant isolation). Added `deleteAllForOwner`. |
| `service/HoldingService.java` | All methods now scoped by `ownerId` (multi-tenant isolation). |
| `service/HoldingImportService.java` | CSV/image import now scoped to the importing user. |
| `service/InsightsService.java` | AI summary now scoped to the current user's holdings. |
| `service/UserService.java` | Added `getCurrentUser(Authentication)`, `getCurrentUserId`, `listCustomers`, `createCustomer(username, password, name, email, managedBy)`, `getCustomerById`, `deleteUser`. Removed `getSoleUser()`/`hasBiometricCredential()` (only used by the now-removed WebAuthn flows). |
| `service/CustomerService.java` | **New.** Fund-manager-facing service: list customers with performance snapshot, get a single customer's profile, create/delete customers, fetch a customer's aggregate holdings. |
| `controller/PortfolioController.java`, `HoldingController.java`, `HoldingsController.java`, `NewsController.java`, `InsightsController.java` | Each endpoint now resolves the authenticated user (`Authentication`) and scopes data to them — same URLs/behavior as before for a single owner. |
| `controller/ManagerController.java` | **New.** `/api/manager/**`: list/create/delete customers, get a single customer's profile (`GET /customers/{id}`), and view/add/update/delete a specific customer's holdings + history, restricted to `FUND_MANAGER` role. |
| `dto/AuthResponse.java` | Added `role` field; removed `biometricEnabled` (WebAuthn removed). |
| `dto/CustomerRequest.java` | **New.** Now requires `username`, `password`, `name`, `email`. |
| `dto/CustomerSummary.java` | **New.** Now includes `name` and `email` alongside `username`. |
| `security/JwtService.java` | Token now embeds a `role` claim (`generateToken(subject, role)`, `extractRole`). |
| `security/JwtAuthFilter.java` | Grants `ROLE_OWNER` / `ROLE_FUND_MANAGER` authority based on the JWT's role claim. |
| `controller/AuthController.java` | Login / `/me` now return the account's `role`, `name`, `email`, and (for customers) `managerUsername`. All WebAuthn endpoints removed. |
| `config/SecurityConfig.java` | `/api/manager/**` restricted to `hasRole("FUND_MANAGER")`; `/api/portfolios/**` & `/api/holdings/**` restricted to `hasRole("OWNER")`. |
| `config/DataSeeder.java` | Seeds the `fundmanager` account (role `FUND_MANAGER`) first, then the `owner` account with `managedBy` set to the seeded fund manager, and `name`/`email` defaults for both. Seeding logic keyed by username existence instead of total row count. |
| `application.properties` / `application-prod.properties` | Added `app.manager.username` / `app.manager.password` (env: `MANAGER_USERNAME` / `MANAGER_PASSWORD`). Removed all `app.webauthn.*` properties. |
| `model/WebauthnCredential.java`, `repository/WebauthnCredentialRepository.java`, `security/webauthn/**` (`WebAuthnService`, `WebAuthnChallengeStore`, `WebAuthnCrypto`, `CborReader`) | **Deleted.** Biometric login removed entirely. |

## Frontend changes (all additive — existing owner pages/routes untouched)

| File | Change |
|---|---|
| `context/AuthContext.jsx` | Now also tracks `role` / exposes `isFundManager`, plus `name`, `email`, `managerUsername` fetched from `/api/auth/me`. Removed `biometricEnabled`. |
| `pages/LoginPage.jsx` | Redirects to `/manager` for fund managers, `/` for owners (was always `/`). Removed the "Sign in with biometrics" button/flow. |
| `components/common/ProtectedRoute.jsx` | Redirects a fund manager away from owner pages to `/manager`. |
| `components/common/ManagerRoute.jsx` | **New.** Route guard mirroring `ProtectedRoute`, but requires the `FUND_MANAGER` role. |
| `components/common/ManagerSidebar.jsx` | **New.** Same look/structure as `Sidebar.jsx`, with a "Customers" nav item instead of Dashboard/Holdings. |
| `components/common/Sidebar.jsx` | Removed the "Enable Biometrics" button/flow; now shows the logged-in owner's fund manager username (`Fund Manager: <username>`) when set. |
| `services/managerService.js` | **New.** Wraps the `/api/manager/**` endpoints, including `getCustomer(id)` for a single customer's profile. |
| `services/authService.js` | Removed all WebAuthn registration/login helper calls. |
| `config/api.js` | Added manager endpoint paths. Removed all `webauthn*` endpoint paths. |
| `utils/webauthn.js` | **Deleted.** |
| `pages/ManagerCustomersPage.jsx` | **New/Updated.** List/add/remove customers, using the same `Card`/`DataTable`/`Modal`/`ConfirmDialog` components and CSS classes as existing pages. The "Add Customer" form and list/table now include `name` and `email`. |
| `pages/ManagerCustomerDashboardPage.jsx` | **New/Updated.** Mirrors `DashboardPage`'s layout (stat cards, trend chart, allocation pie chart, performance bar chart) for a selected customer; the header now shows the customer's name, username, and email. |
| `pages/ManagerCustomerHoldingsPage.jsx` | **New.** Mirrors `HoldingsPage`'s layout (add/edit/delete holdings table) for a selected customer. |
| `pages/DashboardPage.jsx` | Owner's own dashboard subtitle now shows their `name`/`email`. |
| `App.jsx` | Added `/manager`, `/manager/customers/:customerId`, `/manager/customers/:customerId/holdings` routes and role-based sidebar selection. Existing routes unchanged. |

### Known limitations

Biometric/WebAuthn login has been removed entirely, so the previous WebAuthn-related limitation
(resolving the correct account when multiple `OWNER` accounts exist) no longer applies.

## What you need to do for MySQL (production) deployments

Dev (`H2`) uses `spring.jpa.hibernate.ddl-auto=create-drop`, so the schema is rebuilt fresh on
every restart — **no action needed there**.

Production (`application-prod.properties`) uses `spring.jpa.hibernate.ddl-auto=update`, which can
safely *add new tables/columns* to an **empty** database, but **cannot** safely add the two new
`NOT NULL` columns below to a MySQL database that already has data, because MySQL rejects adding a
`NOT NULL` column with no default to a non-empty table. If your MySQL database is brand new
(no existing rows), you can skip straight to starting the app — Hibernate will create everything
correctly. **If you already have existing data**, run this once against your MySQL database
*before* starting the updated app:

```sql
-- 1. Add the role column to existing users, defaulting existing accounts to OWNER
ALTER TABLE app_users
  ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'OWNER';

-- 2. Add the new profile columns for customers (nullable so existing rows aren't rejected)
ALTER TABLE app_users
  ADD COLUMN name VARCHAR(255) NULL,
  ADD COLUMN email VARCHAR(255) NULL,
  ADD COLUMN managed_by_id BIGINT NULL;

-- 3. Backfill a reasonable default name/email for any existing accounts (adjust as you like)
UPDATE app_users
  SET name = COALESCE(name, username),
      email = COALESCE(email, CONCAT(username, '@PortfolioM.local'))
  WHERE name IS NULL OR email IS NULL;

-- 4. Add the self-referencing FK for managed_by_id (fund manager who created the account)
ALTER TABLE app_users
  ADD CONSTRAINT fk_app_users_managed_by FOREIGN KEY (managed_by_id) REFERENCES app_users(id);

-- 5. Add the new owner relationship to portfolios (nullable first, so existing rows aren't rejected)
ALTER TABLE portfolios
  ADD COLUMN owner_id BIGINT NULL;

-- 6. Backfill existing portfolios to the current single owner account
--    (replace 'owner' with your actual OWNER_USERNAME if you changed it)
UPDATE portfolios
  SET owner_id = (SELECT id FROM app_users WHERE username = 'owner' LIMIT 1)
  WHERE owner_id IS NULL;

-- 7. Now that every row has a value, make the column required and add the FK
ALTER TABLE portfolios
  MODIFY COLUMN owner_id BIGINT NOT NULL;

ALTER TABLE portfolios
  ADD CONSTRAINT fk_portfolios_owner FOREIGN KEY (owner_id) REFERENCES app_users(id);
```

If you are picking up this feature for the **first time** on a database that already had the
`role`/`owner_id` columns from an earlier deployment of the fund manager feature (i.e. you only
need the `name`/`email`/`managed_by_id` additions), just run steps 2–4 above.

After that, starting the app will automatically seed the `fundmanager` account (Hibernate/
`DataSeeder` only insert what's missing — they won't touch your existing data).

**Recommended:** change the seeded fund manager password immediately in production via the
`MANAGER_PASSWORD` environment variable, the same way `OWNER_PASSWORD` is already handled.

**Also:** if you had `WEBAUTHN_RP_ID` / `WEBAUTHN_RP_NAME` / `WEBAUTHN_ORIGIN` environment
variables set for this app, they are no longer used and can be removed — biometric login has been
deleted. If you previously had a `webauthn_credentials` table, it's no longer read/written by the
app and can be dropped once you've confirmed you don't need the data:

```sql
DROP TABLE IF EXISTS webauthn_credentials;
```

## New API endpoints (fund manager only, requires `FUND_MANAGER` role)

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/manager/customers` | List all customers with a performance snapshot (includes `name`, `email`) |
| POST | `/api/manager/customers` | Create a new customer account (`username`, `password`, `name`, `email`) |
| GET | `/api/manager/customers/{id}` | Get a single customer's profile + performance snapshot |
| DELETE | `/api/manager/customers/{id}` | Remove a customer and all of their portfolios/holdings |
| GET | `/api/manager/customers/{id}/holdings` | Get a customer's aggregate holdings performance |
| POST | `/api/manager/customers/{id}/holdings` | Add a holding to a customer's portfolio |
| PUT | `/api/manager/customers/{id}/holdings/{holdingId}` | Update a customer's holding |
| DELETE | `/api/manager/customers/{id}/holdings/{holdingId}` | Remove a customer's holding |
| GET | `/api/manager/customers/{id}/holdings/history?range=` | Get a customer's portfolio value over time |

`GET /api/auth/me` (any authenticated user) now also returns `name`, `email`, and
`managerUsername` (the username of the fund manager who created the account, or `null` if none).

## Verification performed

- Backend compiles cleanly (`mvnw compile`).
- Frontend builds cleanly (`npm run build`).
- Manual smoke test (H2, fresh boot):
  - `owner` / `fundmanager` both log in and receive the correct `role`.
  - Owner endpoints reject the fund manager token (403); manager endpoints reject the owner token (403).
  - Fund manager can list customers, create a new customer with `name`/`email`, add a holding for
    them, and the new customer's login only sees their own holding (isolated from the seeded
    owner's 8 holdings).
  - `GET /api/manager/customers/{id}` returns the created customer's `name`/`email`.
  - `GET /api/auth/me` for both the seeded owner and a newly created customer returns
    `managerUsername: "fundmanager"`.
  - Deleting a customer via the manager API removes their portfolios/holdings and the account.
  - Removed WebAuthn endpoints (e.g. `/api/auth/webauthn/login/options`) no longer resolve to a
    working handler.
