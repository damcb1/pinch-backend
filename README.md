# Pinch — Backend

REST API for **Pinch**, a mobile-first web app to save, structure and search recipes from different sources (blogs, TikTok, Instagram, YouTube). The backend imports a recipe from a link (or pasted text), turns it into structured data (ingredients as `{qty, unit, name}`, time, servings, cuisine) and makes it searchable.

## Tech stack

- **Java 25**, **Spring Boot 4.1**
- **Spring Security + JWT** for authentication
- **Spring Data JPA** + **PostgreSQL**
- **Anthropic API** (Claude Haiku) to structure pasted text and normalize ingredients
- **Maven** (wrapper included)
- Tests: **JUnit 5**, **Mockito**, and integration tests on an in-memory **H2** database

## Requirements

- JDK 25
- PostgreSQL running locally (or reachable via `DB_URL`)
- An Anthropic API key (for the AI import features)
- No global Maven needed — use the included wrapper (`./mvnw`)

## Configuration

Secrets are read from environment variables via a `.env` file (never committed). Copy the example and fill it in:

```bash
cp .env.example .env
```

| Variable | Description | Example |
| --- | --- | --- |
| `DB_URL` | JDBC URL of the PostgreSQL database | `jdbc:postgresql://localhost:5432/pinch_db` |
| `DB_USER` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `your-password` |
| `JWT_SECRET` | Secret key used to sign JWTs (use a long, random value) | `a-long-random-secret` |
| `ANTHROPIC_API_KEY` | API key for the AI import layer | `sk-ant-...` |

Non-secret settings live in `src/main/resources/application.properties`, including the image upload config:

- `app.upload.path` — local folder where uploaded images are stored (default `uploads`)
- `app.upload.base-url` — public base URL used to build image URLs (default `http://localhost:8080`)

## Running the app

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. On startup Hibernate updates the schema (`ddl-auto=update`).

## Running the tests

```bash
./mvnw test
```

Unit tests use Mockito; integration tests use `@SpringBootTest` + MockMvc against an in-memory **H2** database, activated by the `test` profile (`src/test/resources/application-test.properties`). Tests never touch your real PostgreSQL database.

## API overview

All routes are prefixed with `/api`. Everything requires a `Authorization: Bearer <token>` header **except** `/api/auth/**` (public) and `GET /uploads/**` (public image reads).

### Auth — `/api/auth` (public)

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/register` | Create a user. Body: `{ email, password }`. Returns `201`. |
| `POST` | `/login` | Log in. Body: `{ email, password }`. Returns `200` with `{ token, email }`. |

### Users — `/api/users`

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/me` | Current user's email. |
| `DELETE` | `/me` | Delete own account, its recipes and its uploaded images. Returns `204`. |

### Recipes — `/api/recipes`

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/` | Create a manual recipe. Returns `201`. |
| `GET` | `/` | List/search own recipes. Query params: `q`, `cuisine`, `minTime`, `maxTime`, `origin`, `difficulty`. |
| `GET` | `/{id}` | Recipe detail. |
| `PUT` | `/{id}` | Update a recipe. |
| `DELETE` | `/{id}` | Delete a recipe and its uploaded image. Returns `204`. |
| `POST` | `/import` | Import from a URL (parses schema.org/Recipe JSON-LD). Body: `{ url }`. Returns a preview. |
| `POST` | `/import/text` | Import from pasted text (structured by the AI layer). Body: `{ text }`. Returns a preview. |
| `POST` | `/import/confirm` | Save a previewed/imported recipe. Returns `201`. |

### Files — `/api/files`

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/upload` | Upload an image (`multipart/form-data`, field `file`). Returns `201` with `{ fileName, contentType, size, url }`. |
| `GET` | `/uploads/**` | Public static access to uploaded images. |

## Data model

- **User** — `email` (unique), `password` (BCrypt), `role` (`USER`), and their recipes.
- **Recipe** — `title`, `sourceUrl`, `imageUrl`, `timeMinutes`, `servings`, `cuisine`, `difficulty` (`EASY` / `MEDIUM` / `HARD`), `origin` (`IMPORTED` / `MANUAL`), `sourcePlatform` (`WEB` / `MANUAL`), ordered `steps`, and `ingredients`.
- **Ingredient** — stored as `{ qty, unit, name }` (never as a plain string). This structure is what enables the search filters.

Deleting a user or a recipe also removes the associated uploaded image files from disk; images imported from external sites (external URLs) are left untouched.

## Project structure

```
src/main/java/com/example/pinchbackend
├── config        # Security and static-resource configuration
├── controller    # REST controllers
├── dto           # Request/response DTOs
├── entity        # JPA entities and enums
├── exception     # Custom exceptions + GlobalExceptionHandler
├── repository    # Spring Data JPA repositories
├── security      # JWT filter, user details, token utils
└── service       # Business logic
```
