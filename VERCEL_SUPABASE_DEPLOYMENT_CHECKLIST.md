# Vercel + Supabase Deployment Checklist

## Architecture Reminder
- Frontend can be deployed on Vercel (static hosting).
- Spring Boot backend should be deployed on a Java-capable host (Render, Railway, Azure App Service, VM, or container host).
- Supabase remains the PostgreSQL database (and optional Supabase Auth provider).

## 1. Backend Environment Variables
Set these on the backend host:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET
- JWT_EXPIRATION_MS
- PORT
- APP_CORS_ALLOWED_ORIGINS
- SPRING_PROFILES_ACTIVE
- JPA_DDL_AUTO
- JPA_SHOW_SQL
- SUPABASE_AUTH_ENABLED
- SUPABASE_URL
- SUPABASE_ANON_KEY
- SUPABASE_SERVICE_ROLE_KEY
- SUPABASE_TIMEOUT_MS

Recommended production values:
- SPRING_PROFILES_ACTIVE=prod
- JPA_DDL_AUTO=validate
- JPA_SHOW_SQL=false
- APP_CORS_ALLOWED_ORIGINS=https://<your-vercel-domain>

## 2. Frontend API Base URL Mapping
This frontend rewrites any fetch call that starts with /api/ through static/api-config.js.

You have two valid deployment options:

Option A: Direct backend URL in frontend runtime config
- Set static/env.js value:
  - window.LEAVEPAL_ENV.API_BASE_URL = "https://<your-backend-domain>";
- Keep frontend fetch calls as /api/... (already implemented).

Option B: Keep API_BASE_URL empty and use Vercel rewrite proxy
- Keep static/env.js as empty API base URL.
- Add a Vercel rewrite from /api/(.*) to https://<your-backend-domain>/api/$1.

## 3. Security Checklist
- Use a strong JWT_SECRET (at least 32 characters).
- Ensure backend runs with centralized JWT filter chain (implemented).
- Keep only auth bootstrap routes public:
  - /api/auth/login
  - /api/auth/forgot-password-request
  - /api/auth/reset-temporary-password
- Restrict CORS to deployed frontend origin only in production.

## 4. End-to-End Validation Steps
Run these in order after deployment:

1. Health and docs
- Open https://<backend-domain>/swagger-ui/index.html
- Confirm 200 and API docs load.

2. Auth flow
- POST /api/auth/login with valid credentials.
- Confirm JWT token is returned.

3. Protected route enforcement
- Call GET /api/notifications/my without Authorization header.
- Expected: 401 Unauthorized.
- Call same endpoint with Bearer token.
- Expected: 200.

4. Database connectivity
- Backend startup logs must not show localhost:5432 connection attempts.
- Backend startup logs must show successful datasource initialization.

5. Frontend to backend connectivity
- Login from deployed Vercel frontend.
- Confirm network requests resolve to either:
  - https://<your-backend-domain>/api/... (Option A), or
  - https://<your-vercel-domain>/api/... rewritten to backend (Option B).

## 5. Quick Local Preflight
Before deployment:

1. Create .env from .env.example and fill real values.
2. Start backend:
- PowerShell:
  - Set-Location c:\Leave-Management-System\Leavepal
  - Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
  - .\run-backend.ps1 -UseProdProfile
3. Verify startup has no datasource/JWT configuration errors.

## 6. Current Mapping Verification Status
Verified in this codebase:
- Backend property placeholders map to environment variables in src/main/resources/application.properties.
- Production profile overrides exist in src/main/resources/application-prod.properties.
- All static pages using /api calls include env.js and api-config.js.
- Static link/reference scan reports no missing files.
