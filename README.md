# Applytrack

Multi-user web application for tracking, enriching, and analyzing job applications.

**Status:** early development (M1 - foundation).

## Stack

- Backend: Java 21, Spring Boot 4.1, Maven, PostgreSQL
- Frontend: Angular 21, TypeScript, SCSS

## Local development

1. `docker compose up -d` - starts PostgreSQL
2. `cd backend && ./mvnw spring-boot:run` - starts the backend
3. `cd frontend && npm install` - one-time, installs dependencies
4. `npm start` - starts the Angular dev server
5. Backend health: http://localhost:8080/actuator/health · Frontend: http://localhost:4200