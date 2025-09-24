# OpenChat Kata (Scala)

## API specification
- [API yaml definition](./APIs.yaml)

## API overview (high level)
A quick, non-technical summary of the main HTTP endpoints.

- POST /users – Register a new user.
- POST /login – Authenticate with username and password; returns basic user info.
- POST /users/{userId}/timeline – Create a new post authored by the user.
- GET /users/{userId}/timeline – Read the user’s timeline.
  - Timeline shows only posts created by that user.
  - Ordered by post date/time descending (newest post first).

Default server bind: http://127.0.0.1:8080. You can override host/port via env vars OPENCHAT_HOST and OPENCHAT_PORT.

## Prerequisites
- Java 17+ (JDK)
- Scala 2.13.x (currently 2.13.16)
- sbt (latest 1.x)

## Quick Start (from repository root)
- Install dependencies: sbt will auto-resolve on first run
- Compile: `sbt compile`
- Run the HTTP server: `sbt run`
- Run all tests: `sbt test`
- Format code: `sbt scalafmtAll` (config at `.scalafmt.conf`)

## Project structure (high level)
- src/main/scala/openchat: domain, application, infrastructure
- src/test/scala/openchat: customer and integration tests, test utilities

## Database / Repositories
- Uses Doobie + SQLite for persistence in production.
- Application stores the database in `database/database.db` (created on first run).
- Schema is initialized automatically on startup.

## Configuration
- Provided by `openchat.infrastructure.config.AppConfig` with sensible defaults:
  - OPENCHAT_HOST (default: 127.0.0.1)
  - OPENCHAT_PORT (default: 8080)
  - OPENCHAT_DB_URL (default: jdbc:sqlite:database/database.db)
  - OPENCHAT_DB_DRIVER (default: org.sqlite.JDBC)
- These values are used by Main (HTTP) and AppDependencies (DB transactor).

## Testing notes
- Test framework: ScalaTest.
- Run full suite: `sbt test`.
- Run a single suite (example): `sbt "testOnly openchat.infrastructure.http.RoutesSpec"`.
