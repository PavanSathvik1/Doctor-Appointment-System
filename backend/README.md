# HMS Backend - Spring Boot API

The backend for the Hospital Management System is a robust, high-performance Spring Boot application designed with a focus on scalability and clean architecture.

## 🏗️ Architecture

The application follows a strict **Layered MVC Architecture**, organized for maximum maintainability:

- **`com.hms.controller`**: REST APIs organized by feature (admin, auth, appointment, doctor, etc.).
- **`com.hms.service`**: Core business logic implementations.
- **`com.hms.repository`**: Data access layer using Spring Data JPA and Elasticsearch.
- **`com.hms.entity`**: Persistent data models.
- **`com.hms.dto`**: Data Transfer Objects for API request/response consistency.
- **`com.hms.mapper`**: Component-based mapping between Entities and DTOs using MapStruct.
- **`com.hms.config`**: System configurations (Security, Redis, S3, Elasticsearch).
- **`com.hms.exception`**: Global error handling system.

---

## 🚀 Technical Highlights

### 1. Security & Auth
- **JWT Authentication**: Stateless authentication using secure HS256 signed tokens.
- **Refresh Token Pattern**: Extended sessions with secure refresh token rotation.
- **RBAC**: Role-Based Access Control (ADMIN, DOCTOR, PATIENT).

### 2. Search Engine
- **Elasticsearch Integration**: Synchronized doctor records for fast, fuzzy, and phonetic search capabilities.

### 3. File Processing
- **PDF Generation**: Dynamic prescription PDF generation using iText 8.
- **AWS S3**: Cloud storage for medical documents with time-limited presigned URLs for secure access.

### 4. Database Management
- **Flyway**: Automated database versioning and schema migrations.
- **Redis Caching**: High-speed caching for session data and frequent lookups.

---

## 🛠️ Development Setup

### Configuration
The application uses `src/main/resources/application.yml` for configuration. Key properties can be overridden via environment variables:
- `SPRING_DATASOURCE_URL`: MySQL connection string.
- `ELASTICSEARCH_HOST`: Host for ES cluster.
- `JWT_SECRET`: Secret key for token signing.

### Maven Commands
- **Build**: `mvn clean install`
- **Run**: `mvn spring-boot:run`
- **Tests**: `mvn test`

---

## 📊 API Documentation
The API follows standard REST principles:
- `POST /api/auth/login`: Authentication
- `POST /api/doctors/register`: Application for doctors
- `GET /api/doctors/search`: Elasticsearch-backed search
- `POST /api/appointments/book`: Appointment scheduling
- `POST /api/prescriptions/issue`: Prescription management
