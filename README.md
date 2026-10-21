# OnlineBookStore

A modern online bookstore web application. Customers browse the catalog, manage a
shopping cart, and place orders; sellers manage the catalog from an admin area.

## Stack

- Java 17, Spring Boot 3 (Spring MVC, Spring Security, Spring Data JPA, Bean Validation)
- Thymeleaf server-rendered UI (Bootstrap 5 via WebJars)
- PostgreSQL 16 (Flyway-managed schema and seed data)
- Maven

## Features

**Customers**
- Register (email + password, terms acceptance enforced) and log in
- Browse the book catalog with live stock badges (Out of Stock / Only N left / Trending)
- Search books by title or author
- Session-based shopping cart with per-book quantity controls, capped by available stock
- Checkout with order summary and a simulated payment (no card data is collected)
- Receipt page after each order, with a personal order history

**Sellers (admin)**
- Inventory overview with stock badges
- Add books (server-generated UUID barcode), edit name/author/price/quantity, remove books
- Sales history: every order with customer, items, and totals

## Quickstart

Prerequisites: JDK 17+ and Maven 3.9+ (or use the Dockerfile).

**Option A — PostgreSQL via Docker:**

```bash
docker compose up -d db
mvn spring-boot:run
```

**Option B — no Docker (embedded H2, file-based):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Then open http://localhost:8080

### Demo accounts (seeded on first start)

| Email | Password | Role |
|---|---|---|
| `admin@bookstore.local` | `admin123` | Seller |
| `demo@bookstore.local` | `demo123` | Customer |

These accounts are for local development only — change or remove them before any real deployment.

### Configuration

Environment variables override the database connection:

- `DB_URL` (default `jdbc:postgresql://localhost:5432/onlinebookstore`)
- `DB_USER` / `DB_PASSWORD` (default `bookstore` / `bookstore`)
- `PORT` (default `8080`)

## Repository layout

```
src/main/java/com/onlinebookstore/
  config/        security, demo data seeding
  controller/    web controllers (customer, admin, errors)
  dto/           form and receipt objects
  exception/     domain exceptions
  model/         JPA entities and session cart
  repository/    Spring Data repositories
  service/       business logic (incl. transactional stock purchase)
src/main/resources/
  db/migration/  Flyway schema + seed data
  templates/     Thymeleaf templates
  static/        CSS
```

## Building

```bash
mvn clean package          # produces target/onlinebookstore.jar
java -jar target/onlinebookstore.jar
```

Or build and run the Docker image (expects a reachable database via `DB_URL`):

```bash
docker build -t onlinebookstore .
docker run -p 8080:8080 -e DB_URL=jdbc:postgresql://host.docker.internal:5432/onlinebookstore onlinebookstore
```
