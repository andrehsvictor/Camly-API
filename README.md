<div align="center">

# 📸 Camly API 📸

  <img src="src/main/resources/static/logo.png" alt="Camly Logo" width="250" height="auto">
  
  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  [![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
  [![Build Status](https://img.shields.io/badge/build-passing-success.svg)](https://github.com/andrehsvictor/Camly-API)
  [![Code Coverage](https://img.shields.io/badge/coverage-80%25-green.svg)](https://github.com/andrehsvictor/Camly-API)
  
  <h2>A modern photo storage and sharing service API</h2>
  
  <p align="center">
    <b>
    <a href="#features">✨ Features</a> •
    <a href="#tech-stack">🛠️ Tech Stack</a> •
    <a href="#api-endpoints">🔌 API Endpoints</a> •
    <a href="#installation">⚙️ Installation</a> •
    <a href="#running">🚀 Running</a> •
    <a href="#testing">🧪 Testing</a> •
    <a href="#deployment">📦 Deployment</a> •
    <a href="#license">📝 License</a>
    </b>
  </p>

  <br>
  <p align="center">
    <i>Secure · Scalable · Modern</i>
  </p>
</div>

## 📋 Overview

Camly API is a robust Spring Boot application providing backend services for photo storage and sharing. It allows users to create accounts, upload photos, create and manage posts, like content, and follow other users. The application follows RESTful principles and includes comprehensive security features with JWT authentication.

<div align="center">
  <table>
    <tr>
      <td align="center"><b>🔒 Secure</b></td>
      <td>JWT-based authentication and authorization</td>
    </tr>
    <tr>
      <td align="center"><b>⚡ Fast</b></td>
      <td>Redis caching for high performance</td>
    </tr>
    <tr>
      <td align="center"><b>🔄 Scalable</b></td>
      <td>Docker containerization for easy deployment</td>
    </tr>
    <tr>
      <td align="center"><b>📊 Testable</b></td>
      <td>Comprehensive test suite with high coverage</td>
    </tr>
  </table>
</div>

## ✨ Features

- **User Management**
  - User registration and authentication
  - JWT-based authentication with access and refresh tokens
  - Email verification
  - Password reset functionality
  - User search and filtering
  - Follow/unfollow functionality

- **Post Management**
  - Create, read, update, and delete posts
  - Add captions to posts
  - Filter posts by caption text or username
  - Like/unlike posts
  - Engagement rate calculation

- **Image Handling**
  - Upload and store images securely
  - Comprehensive validation of image files
  - Integration with MinIO object storage

- **Social Features**
  - Follow other users
  - Like posts
  - View user profiles and posts
  - Track post engagement statistics

- **Caching**
  - Redis-based caching for improved performance

- **Documentation**
  - OpenAPI/Swagger documentation for all endpoints

## 🛠️ Tech Stack

- **Java 21** - Core programming language
- **Spring Boot** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **PostgreSQL** - Primary database
- **Redis** - Caching solution
- **MinIO** - Object storage for images
- **JWT** - Token-based authentication
- **OpenAPI 3** - API documentation
- **JUnit 5 & RestAssured** - Testing framework
- **Testcontainers** - Integration testing with containers
- **Docker** - Containerization
- **GitHub Actions** - CI/CD pipeline

## 🔌 API Endpoints

### 🔐 Authentication

- `POST /api/v1/account` - Create a new user account
- `POST /api/v1/account/send-action-email` - Send verification or password reset email
- `POST /api/v1/account/verify` - Verify email address
- `POST /api/v1/account/reset-password` - Reset password
- `POST /api/v1/token` - Get access and refresh tokens
- `POST /api/v1/token/refresh` - Refresh access token
- `POST /api/v1/token/revoke` - Revoke token

### 👥 Users

- `GET /api/v1/users` - Get all users with optional filtering
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}/followers` - Follow/unfollow a user

### 📱 Posts

- `POST /api/v1/posts` - Create a new post
- `GET /api/v1/posts` - Get all posts with filtering
- `GET /api/v1/posts/{id}` - Get post by ID
- `PUT /api/v1/posts/{id}` - Update a post
- `DELETE /api/v1/posts/{id}` - Delete a post
- `PUT /api/v1/posts/{id}/likes` - Like/unlike a post
- `GET /api/v1/posts/stats` - Get post statistics for the current user
- `GET /api/v1/users/{userId}/posts` - Get all posts for a specific user

### 🖼️ Images

- `POST /api/v1/images` - Upload an image

## ⚙️ Installation

### Prerequisites

- Java 21 JDK
- Maven
- Docker and Docker Compose (for local development)
- PostgreSQL
- Redis
- MinIO

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/andrehsvictor/Camly-API.git
   cd Camly-API
   ```

2. Generate RSA keys for JWT authentication:
   ```bash
   chmod +x rsa-keys.sh
   ./rsa-keys.sh
   ```

3. Configure the application:
   
   Create an `application.properties` or `application.yml` file with the following configuration (modify as needed):
   ```properties
   # Database
   spring.datasource.url=jdbc:postgresql://localhost:5432/camly
   spring.datasource.username=camly
   spring.datasource.password=yourpassword

   # Redis
   spring.data.redis.host=localhost
   spring.data.redis.port=6379

   # MinIO
   camly.minio.endpoint=http://localhost:9000
   camly.minio.admin.username=minio
   camly.minio.admin.password=minio123
   camly.minio.bucket.name=camly

   # Email (for verification and password reset)
   spring.mail.host=localhost
   spring.mail.port=1025
   ```

## 🚀 Running

### Using Maven

```bash
./mvnw spring-boot:run
```

### Using Docker

Build the Docker image:
```bash
docker build -t camly-api .
```

Run the container:
```bash
docker run -p 8080:8080 camly-api
```

## 🧪 Testing

### Running Unit Tests

```bash
./mvnw test
```

### Running Integration Tests

```bash
./mvnw failsafe:integration-test
```

Integration tests use Testcontainers to spin up PostgreSQL, Redis, MinIO, and MailHog containers automatically for testing.

## 📦 Deployment

The project includes a GitHub Actions workflow for CI/CD in `.github/workflows/main.yml`. It:

1. Builds the application
2. Runs unit and integration tests
3. Creates and pushes a Docker image to Docker Hub

To deploy:

1. Set up GitHub repository secrets:
   - `DOCKER_USERNAME`: Your Docker Hub username
   - `DOCKER_PASSWORD`: Your Docker Hub password

2. Push to the main branch or create a tag to trigger the workflow.

## 📚 API Documentation

When the application is running, OpenAPI documentation is available at:

- http://localhost:8080/swagger-ui.html

## 📝 License

This project is licensed under the [MIT License](LICENSE).

## 👨‍💻 Author

- **André Victor** - [andrehsvictor@gmail.com](mailto:andrehsvictor@gmail.com)
- GitHub: [https://github.com/andrehsvictor](https://github.com/andrehsvictor)