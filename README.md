# URL Shortener - Multi-Language Microservices

A distributed URL shortener system demonstrating microservices architecture with multiple programming languages and technologies.

## 🎯 Overview

This project implements a URL shortener service using microservices architecture with two main components:
- **Backend Service** (Kotlin/Spring Boot) - Core URL shortening functionality
- **Analytics Service** (Python/FastAPI) - Real-time analytics and monitoring dashboard

### Key Features

- ✅ Create shortened URLs from long URLs
- ✅ Fast URL redirection with click tracking
- ✅ Real-time analytics dashboard
- ✅ RESTful API with comprehensive endpoints
- ✅ PostgreSQL database with optimized queries
- ✅ Docker containerization
- ✅ Multi-language microservices demonstration

## 🏗️ Architecture

![UML Diagram](https://www.plantuml.com/plantuml/png/bP5FRnCn4CNl_XIZN6WELS8VfAe7r3GK4L74qat49HVZ7JPMlUFLU1OH8RuxNc_ehYCanCqpVk_dUPwR89gbQvmAZvPQpRe1HYEQQeVmKm7SQlDOi--eMdld6RuSh63VzrmX3xrR5qNgbr9Y9Tfs3_TG7ZmB4ZAKno3Om8fDxpJICS95QPlMsMyMgxbIp-3kXo2JTc2SHPAWnXbcwzI2LTk66UW0GntBJBMAxgDO4s2tIRAdncwJwQh64cozTmCjrQdivrXxHWZ8twt139z5sbF_AKZLdYu-U76M5cNBbkhvdegekOQhvTKouQFloVRNSVTK3iz5Ld6KWwV5EnraLMmciENpFDB9CWL-z6J5zqD4qATMh8_YURADLtt4jrhqLnrGLQS3p0eVf6OiFz__UVBrgvSladUy6hw724tF3cfS0rnUlic2mZMytswBnSDTkLMJFIJXUOYJM8sB-0TlrFq7Rlo33AQoPw9m6lWSVNv_WrHrZViD)

## 🛠️ Prerequisites

### Required Software
- **Docker** 20.10+ and **Docker Compose** 2.0+
- **Java** 17+ (for local development)
- **Python** 3.11+ (for local development)
- **PostgreSQL** 13+ (if running locally)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd url-shortener
```

### 2. Start with Docker Compose
```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

### 3. Verify Services
```bash
# Check if all containers are running
docker-compose ps

# Backend service health
curl http://localhost:8080/links

# Analytics service health
curl http://localhost:8001/api/analytics/summary
```

### 4. Access Applications
- **Backend API**: http://localhost:8080
- **Analytics Dashboard**: http://localhost:8001

## 🔧 Services

### Backend Service (Kotlin/Spring Boot)
**Location**: `src/url-shortener-backend/`
**Port**: 8080
**Technology Stack**:
- Kotlin 1.9+
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL Driver
- MapStruct for mapping
- Docker

**Key Features**:
- URL shortening with unique code generation
- Click tracking and analytics
- RESTful API endpoints
- Database persistence with JPA
- Exception handling
- Swagger documentation

### Analytics Service (Python/FastAPI)
**Location**: `src/analyzer/`
**Port**: 8001
**Technology Stack**:
- Python 3.11
- FastAPI
- AsyncPG (PostgreSQL async driver)
- Jinja2 Templates
- Bootstrap 5 UI
- Docker

**Key Features**:
- Real-time analytics dashboard
- Interactive charts and statistics
- Domain-based analytics
- Recent links monitoring
- RESTful API for data access

## 📚 API Documentation

### Backend Service Endpoints

#### Create Short Link
```bash
curl -X POST http://localhost:8080/links \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.example.com/very/long/url/that/needs/shortening"}'

# Response:
# {
#   "shortCode": "AbC123Xy",
#   "url": "http://localhost:8080/AbC123Xy",
#   "originalUrl": "https://www.example.com/very/long/url/that/needs/shortening",
#   "expiresAt": null,
#   "createdAt": "2025-01-15T10:30:45.123",
#   "isActive": true
# }
```

#### Redirect Short Link (Browser)
```bash
# This will redirect to the original URL
curl -L http://localhost:8080/AbC123Xy
```

#### Get Link Information
```bash
curl http://localhost:8080/links/AbC123Xy

# Response:
# {
#   "shortCode": "AbC123Xy",
#   "url": "http://localhost:8080/AbC123Xy",
#   "originalUrl": "https://www.example.com/very/long/url/that/needs/shortening",
#   "expiresAt": null,
#   "createdAt": "2025-01-15T10:30:45.123",
#   "isActive": true
# }
```

#### Get Link Statistics
```bash
curl http://localhost:8080/links/AbC123Xy/stats

# Response:
# {
#   "shortCode": "AbC123Xy",
#   "clicks": 42,
#   "uniqueVisitors": 28,
#   "lastAccessedAt": "2025-01-15T14:22:10.456",
#   "isActive": true
# }
```

#### Delete Link
```bash
curl -X DELETE http://localhost:8080/links/AbC123Xy

# Response: 204 No Content
```

### Analytics Service Endpoints

#### Get Summary Statistics
```bash
curl http://localhost:8001/api/analytics/summary

# Response:
# {
#   "data": {
#     "total_links": 1250,
#     "active_links": 1180,
#     "total_clicks": 45230,
#     "total_unique_visitors": 28540,
#     "avg_clicks_per_link": 36.18,
#     "max_clicks": 892,
#     "unused_links": 70,
#     "unique_domains": 245
#   }
# }
```

#### Get Links Created by Days
```bash
curl "http://localhost:8001/api/analytics/links-by-days?days=7"

# Response:
# {
#   "data": [
#     {"date": "2025-01-09", "count": 12},
#     {"date": "2025-01-10", "count": 18},
#     {"date": "2025-01-11", "count": 15},
#     {"date": "2025-01-12", "count": 22},
#     {"date": "2025-01-13", "count": 19},
#     {"date": "2025-01-14", "count": 25},
#     {"date": "2025-01-15", "count": 8}
#   ]
# }
```

#### Get Top Domains
```bash
curl "http://localhost:8001/api/analytics/top-domains?limit=3"

# Response:
# {
#   "data": [
#     {
#       "domain": "github.com",
#       "url_count": 45,
#       "total_clicks": 1250,
#       "total_visitors": 890,
#       "avg_clicks": 27.8
#     },
#     {
#       "domain": "stackoverflow.com",
#       "url_count": 32,
#       "total_clicks": 980,
#       "total_visitors": 654,
#       "avg_clicks": 30.6
#     },
#     {
#       "domain": "medium.com",
#       "url_count": 28,
#       "total_clicks": 750,
#       "total_visitors": 532,
#       "avg_clicks": 26.8
#     }
#   ]
# }
```

#### Get Recent Links
```bash
curl "http://localhost:8001/api/analytics/recent-links?limit=5&sort_by=clicks&sort_order=desc"

# Response:
# {
#   "data": [
#     {
#       "short_code": "AbC123Xy",
#       "original_url": "https://github.com/example/repo",
#       "clicks": 156,
#       "unique_visitors": 89,
#       "created_at": "2025-01-15T10:30:45.123",
#       "last_accessed_at": "2025-01-15T14:22:10.456",
#       "is_active": true
#     }
#   ]
# }
```

## 💻 Development

### Backend Service Development

```bash
cd src/url-shortener-backend

# Build
./gradlew build

# Run locally (requires PostgreSQL)
./gradlew bootRun

# Run tests
./gradlew test

# Generate fat JAR
./gradlew bootJar
```

### Analytics Service Development

```bash
cd src/analyzer

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run locally
uvicorn main:app --host 0.0.0.0 --port 8001 --reload

# Run with environment variables
DB_HOST=localhost DB_USER=url_user DB_PASSWORD=url_password uvicorn main:app --reload
```

### Development Database Setup

```bash
# Start only PostgreSQL
docker-compose up db

# Or use local PostgreSQL installation
sudo apt install postgresql  # Ubuntu
```

### Logs

```bash
# View all logs
docker-compose logs

# Follow logs for specific service
docker-compose logs -f backend
docker-compose logs -f analytics
docker-compose logs -f db

# Backend application logs
docker-compose exec backend tail -f /app/logs/application.log
```

---

**Happy URL Shortening! 🔗**