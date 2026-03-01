# 🔄 System Architecture & Flowcharts - SnipLink

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                         │
├─────────────────────────────────────────────────────────────┤
│   Browser (HTML5 + CSS3 + JavaScript + Chart.js)            │
│   • index.html  • dashboard.html  • login.html              │
│   • register.html  • Dark Theme UI with Glassmorphism       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ HTTP/HTTPS
                     │ REST API Calls
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
├─────────────────────────────────────────────────────────────┤
│                   Spring MVC Controllers                     │
│  ┌───────────────┬────────────┬─────────────┬────────────┐ │
│  │ AuthController│ URLController│ RedirectCtrl│ Analytics │ │
│  │               │            │             │ Controller │ │
│  └───────────────┴────────────┴─────────────┴────────────┘ │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ DTOs
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      SECURITY LAYER                          │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Spring Security + JWT Authentication Filter         │  │
│  │  • JwtTokenProvider  • JwtAuthenticationFilter       │  │
│  │  • BCrypt Password Encoder  • CORS Configuration     │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ Business Logic
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                           │
├─────────────────────────────────────────────────────────────┤
│  ┌────────────┬──────────┬──────────────┬──────────────┐   │
│  │AuthService │URLService│AnalyticsServ.│  AIService   │   │
│  │            │          │              │              │   │
│  └────────────┴──────────┴──────────────┴──────────────┘   │
│                    BulkImportService                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ JPA/Hibernate
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   PERSISTENCE LAYER                          │
├─────────────────────────────────────────────────────────────┤
│              Spring Data JPA Repositories                    │
│  ┌──────────────┬──────────────┬──────────────────────┐    │
│  │UserRepository│URLRepository │ClickLogRepository    │    │
│  └──────────────┴──────────────┴──────────────────────┘    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ JDBC
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATABASE LAYER                         │
├─────────────────────────────────────────────────────────────┤
│              PostgreSQL 12+ (Relational Database)            │
│       Tables: users, urls, click_logs                        │
└─────────────────────────────────────────────────────────────┘
```

---

## Authentication Flow

```
┌─────────┐                                           ┌──────────────┐
│ Client  │                                           │   Backend    │
└────┬────┘                                           └──────┬───────┘
     │                                                        │
     │  1. POST /api/auth/register or /api/auth/login       │
     │  { username, email, password }                        │
     │────────────────────────────────────────────────────► │
     │                                                        │
     │                            2. Validate Credentials    │
     │                               (BCrypt hash check)     │
     │                                                   ┌────┴────┐
     │                                                   │Database │
     │                                                   └────┬────┘
     │                                                        │
     │  3. Generate JWT Token                                │
     │  { token, username, email, role }                     │
     │ ◄──────────────────────────────────────────────────── │
     │                                                        │
     │  4. Store token in localStorage                       │
     │                                                        │
     │  5. All subsequent requests include token             │
     │  Authorization: Bearer <token>                        │
     │────────────────────────────────────────────────────► │
     │                                                        │
     │  6. JWT Filter validates token                        │
     │                                                        │
     │  7. Response                                          │
     │ ◄──────────────────────────────────────────────────── │
     │                                                        │
```

---

## URL Shortening Flow

```
┌────────┐              ┌──────────┐           ┌──────────┐          ┌──────────┐
│ User   │              │URLService│           │AIService │          │Database  │
└───┬────┘              └────┬─────┘           └────┬─────┘          └────┬─────┘
    │                        │                      │                      │
    │ 1. POST /api/shorten   │                      │                      │
    │ with URL & options     │                      │                      │
    │───────────────────────►│                      │                      │
    │                        │                      │                      │
    │                    2. Validate URL            │                      │
    │                        │                      │                      │
    │                    3. Check AI Safety         │                      │
    │                        │─────────────────────►│                      │
    │                        │                      │                      │
    │                        │  4. Safety Result    │                      │
    │                        │◄─────────────────────│                      │
    │                        │                      │                      │
    │                    5. Generate/Validate       │                      │
    │                       Short Code              │                      │
    │                        │                      │                      │
    │                    6. Check if short code     │                      │
    │                       already exists          │                      │
    │                        │──────────────────────────────────────────►  │
    │                        │                      │                      │
    │                        │  7. Exists? (Boolean)│                      │
    │                        │◄───────────────────────────────────────────│
    │                        │                      │                      │
    │                    8. If exists, regenerate   │                      │
    │                       (loop until unique)     │                      │
    │                        │                      │                      │
    │                    9. Hash password (if set)  │                      │
    │                        │                      │                      │
    │                   10. Save URL entity         │                      │
    │                        │──────────────────────────────────────────►  │
    │                        │                      │                      │
    │                   11. Saved                   │                      │
    │                        │◄───────────────────────────────────────────│
    │                        │                      │                      │
    │  12. Return shortened URL                     │                      │
    │◄───────────────────────│                      │                      │
    │                        │                      │                      │
```

---

## URL Redirect & Analytics Flow

```
┌────────┐         ┌────────────────┐      ┌────────────────┐     ┌──────────┐
│ User   │         │RedirectController│      │AnalyticsService│     │ Database │
└───┬────┘         └────────┬─────────┘      └────────┬───────┘     └────┬─────┘
    │                       │                         │                   │
    │  1. GET /{shortCode}  │                         │                   │
    │──────────────────────►│                         │                   │
    │                       │                         │                   │
    │                   2. Find URL by short code     │                   │
    │                       │─────────────────────────────────────────► │
    │                       │                         │                   │
    │                       │  3. URL Entity          │                   │
    │                       │◄────────────────────────────────────────── │
    │                       │                         │                   │
    │                   4. Check if active            │                   │
    │                   5. Check if expired           │                   │
    │                   6. Check if safe              │                   │
    │                   7. Validate password (if set) │                   │
    │                       │                         │                   │
    │                   8. Log click analytics        │                   │
    │                       │────────────────────────►│                   │
    │                       │                         │                   │
    │                       │   9. Parse User-Agent   │                   │
    │                       │      Extract IP, Device,│                   │
    │                       │      Browser            │                   │
    │                       │                         │                   │
    │                       │  10. Save ClickLog      │                   │
    │                       │                         │───────────────► │
    │                       │                         │                   │
    │                       │  11. Increment click    │                   │
    │                       │      count              │───────────────► │
    │                       │                         │                   │
    │  12. Redirect to original URL                   │                   │
    │◄──────────────────────│                         │                   │
    │                       │                         │                   │
```

---

## Dashboard Analytics Flow

```
┌────────┐         ┌──────────────────┐        ┌──────────┐
│ User   │         │AnalyticsController│        │ Database │
└───┬────┘         └─────────┬──────────┘        └────┬─────┘
    │                        │                        │
    │  1. GET /api/analytics/dashboard                │
    │  Authorization: Bearer <token>                  │
    │───────────────────────►│                        │
    │                        │                        │
    │                    2. Get user from JWT         │
    │                        │                        │
    │                    3. Query statistics          │
    │                        │                        │
    │                    4. Count total URLs          │
    │                        │────────────────────► │
    │                        │                        │
    │                    5. Sum total clicks          │
    │                        │────────────────────► │
    │                        │                        │
    │                    6. Count active URLs         │
    │                        │────────────────────► │
    │                        │                        │
    │                    7. Get top 5 URLs            │
    │                        │────────────────────► │
    │                        │                        │
    │                    8. Get last 7 days clicks    │
    │                        │────────────────────► │
    │                        │                        │
    │  9. Return DashboardStats                       │
    │     { totalLinks, totalClicks, activeLinks,     │
    │       topLinks[], dailyClicks{} }               │
    │◄───────────────────────│                        │
    │                        │                        │
    │  10. Render Chart.js visualization             │
    │                        │                        │
```

---

## Bulk Import Flow

```
┌────────┐       ┌──────────────┐      ┌───────────┐     ┌──────────┐
│ User   │       │BulkController│      │URLService │     │ Database │
└───┬────┘       └──────┬───────┘      └─────┬─────┘     └────┬─────┘
    │                   │                    │                  │
    │  1. POST /api/bulk/import              │                  │
    │     multipart/form-data                │                  │
    │     file: urls.csv                     │                  │
    │──────────────────►│                    │                  │
    │                   │                    │                  │
    │               2. Parse CSV             │                  │
    │                   │                    │                  │
    │               3. For each row:         │                  │
    │                   │                    │                  │
    │               4. Call shortenURL()     │                  │
    │                   │───────────────────►│                  │
    │                   │                    │                  │
    │                   │        5. Process (validate, AI check,│
    │                   │           save)    │──────────────► │
    │                   │                    │                  │
    │                   │    6. Success/Error│                  │
    │                   │◄───────────────────│                  │
    │                   │                    │                  │
    │               7. Collect results       │                  │
    │                   │                    │                  │
    │               8. Generate result CSV   │                  │
    │                   │                    │                  │
    │  9. Download result file               │                  │
    │     (success/failure for each URL)     │                  │
    │◄──────────────────│                    │                  │
    │                   │                    │                  │
```

---

## Security Flow

```
                    ┌─────────────────────────┐
                    │   Client Request        │
                    └───────────┬─────────────┘
                                │
                                ▼
                    ┌─────────────────────────┐
                    │  JwtAuthenticationFilter│
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │ Extract JWT from header │
                    │ Authorization: Bearer   │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │ Validate JWT Token      │
                    │ (JwtTokenProvider)      │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │  Valid?                 │
                    └───┬───────────────┬─────┘
                        │ YES           │ NO
                        │               │
            ┌───────────▼──────┐   ┌────▼────────────┐
            │ Load UserDetails │   │ Return 401      │
            │ from token       │   │ Unauthorized    │
            └───────────┬──────┘   └─────────────────┘
                        │
            ┌───────────▼──────────────────┐
            │ Set Authentication in        │
            │ SecurityContextHolder        │
            └───────────┬──────────────────┘
                        │
            ┌───────────▼──────────────────┐
            │ Check role-based permissions │
            │ (@PreAuthorize, etc.)        │
            └───────────┬──────────────────┘
                        │
            ┌───────────▼──────────────────┐
            │ Proceed to Controller        │
            └──────────────────────────────┘
```

---

## Component Interaction Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐            │
│  │ index.html  │  │dashboard.html│  │ login.html   │            │
│  │             │  │             │  │              │            │
│  │ + Chart.js  │  │ + Analytics │  │ + Auth Forms │            │
│  └─────────────┘  └─────────────┘  └──────────────┘            │
└────────────────────────┬────────────────────────────────────────┘
                         │ AJAX/Fetch API
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    CONTROLLERS                            │  │
│  │  Auth │ URL │ Redirect │ Analytics │ Bulk                 │  │
│  └───────┬──────────────────────────────────────────────────┘  │
│          │                                                       │
│  ┌───────▼──────────────────────────────────────────────────┐  │
│  │                      SERVICES                             │  │
│  │  Auth │ URL │ Analytics │ AI │ BulkImport                 │  │
│  └───────┬──────────────────────────────────────────────────┘  │
│          │                                                       │
│  ┌───────▼──────────────────────────────────────────────────┐  │
│  │                   REPOSITORIES                            │  │
│  │  User │ URL │ ClickLog (Spring Data JPA)                  │  │
│  └───────┬──────────────────────────────────────────────────┘  │
└──────────┼───────────────────────────────────────────────────┘
           │ Hibernate/JPA
           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      POSTGRESQL DATABASE                         │
│              users | urls | click_logs                           │
└─────────────────────────────────────────────────────────────────┘
```

---

**Last Updated**: March 2026
