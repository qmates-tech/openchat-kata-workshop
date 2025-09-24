# OpenChat Kata (TypeScript)

## API specification
- [swagger ui](http://localhost:3000/docs)
- [API yaml definition](./APIs.yaml)

## API overview (high level)
A quick, non-technical summary of the main HTTP endpoints.
See the [API yaml definition](./APIs.yaml) file for full details or use embedded [swagger ui](http://localhost:3000/docs).

- `POST` /users – Register a new user.
- `POST` /login – Authenticate with username and password; returns basic user info.
- `POST` /users/{userId}/timeline – Create a new post authored by the user.
- `GET` /users/{userId}/timeline – Read the user’s timeline.
  - Timeline shows only posts created by that user.
  - Ordered by post date/time descending (newest post first).
- `GET` /users/{userId}/wall – Read the user’s wall.
  - Wall shows posts from the user and from users they follow (i.e., a broader feed).
  - Ordered by post date/time descending (newest post first).

## Prerequisites
- Node.js 22.x (enforced via package.json engines)
- pnpm 10.x (enforced via package.json engines)

## Quick Start
- Install: `pnpm install`
- Build: `pnpm build`
- Run all tests: `pnpm test`
- Watch mode: `pnpm vitest`
- Coverage: `pnpm coverage`

## Project structure (high level)
- src/domain: core aggregates, entities, commands, and domain services
- src/application: handlers(use-cases) coordinating domain and repositories
- src/infrastructure: Fastify adapters, DB repositories, and wrappers
- tests/: integration and customer tests

## Database / Repositories
- Uses @databases/sqlite (pure JS). No native sqlite3 is required for tests.
- _Integration_ and _Component_ tests connect to the in-repo SQLite in-memory (no data is saved);
**an empty DB is created every time**.
- Application stores the _"production"_ database in `database/database.db` file;

## Testing notes and tips
- Test runner: Vitest 3.x (see vitest.config.mts: isolate=true, restoreMocks=true; reporters=verbose; coverage provider=v8 limited to src/**/*.ts).
- Current state: All tests are green.
- _Customer_ and _API_ tests use Fastify + supertest. The tests do NOT create servers in-process; **manual server start is required**.
