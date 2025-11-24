# 📘 Todo Service

This document provides **complete technical documentation** for the **Todo Service** project, including:
- Architecture
- Embedded diagram
- API reference
- Scheduler behavior
- Resilience4j usage
- Spring Cache
- Setup & Docker
- Postman collection
- cURL script

---

# 🧱 Architecture

![Architecture](./architecture-diagram.png)

```
Controller → Service → Repository (JPA/H2)
                 ↓
             Scheduler
                 ↓
               Cache
```

---

# 📦 Features

- Create, update, mark done/not-done todos
- Automatic overdue detection → sets `PAST_DUE`
- `PAST_DUE` items cannot be modified
- H2 in-memory DB
- Spring Cache for read optimization
- Resilience4j Circuit Breaker + Rate Limiter
- Docker support
- Unit + integration tests

---

# 🗄 Database Configuration (H2)

JDBC URL:
```
jdbc:h2:file:/data/todos
```

H2 console:
```
http://localhost:8081/h2-console/
```

---

# 🕒 Automatic Past-Due Scheduler

Runs every minute:
- Fetches all NOT_DONE and DONE todos
- If dueDatetime < now → mark as `PAST_DUE`
- Evicts caches

---

# ⚡ Resilience4j

### Circuit Breaker (`TodoService`)
```
slidingWindowSize: 20
failureRateThreshold: 50%
waitDurationInOpenState: 5s
```

### Rate Limiter (`TodoService`)
```
20 requests per second
```

Applied on service layer.

---

# 🧠 Caching

Cache Manager:
```
ConcurrentMapCacheManager
```

Caches:
- `todoById`
- `todoList`

Writes → evict caches.

---

# 🚀 Running the Application

### Build
```
./gradlew clean bootJar
```

### Start with Docker Compose
```
docker-compose up --build
```

### Alternative 
created a custom script called ```run-build.sh``` in the project root directory to run the application:

```bash
./run-build.sh
```

---

# 📡 API Reference (Base Path: `/api/v1/todos`)

## ➕ Create Todo
```
POST /api/v1/todos
```
Body:
```json
{
  "description": "Buy milk",
  "dueDatetime": "2025-12-01T10:00:00Z"
}
```

## ✏️ Update Description
```
PATCH /api/v1/todos/{id}/description
```

## ✅ Mark Done
```
POST /api/v1/todos/{id}/done
```

## ❎ Mark Not Done
```
POST /api/v1/todos/{id}/not-done
```

## 🔍 Get Todo by ID
```
GET /api/v1/todos/{id}
```

## 📃 List Todos
```
GET /api/v1/todos?all=true
```



# 🧪 Testing

```
./gradlew test
```

---

# 🧰 Postman Collection (Inline)

```json
{
  "info": {
    "name": "Todo Service",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create Todo",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type","value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"description\": \"Buy milk\",\n  \"dueDatetime\": \"2025-12-01T10:00:00Z\"\n}"
        },
        "url": "http://localhost:8081/api/v1/todos"
      }
    }
  ]
}
```

---

# 🏁 cURL Script (Inline)

```
#!/usr/bin/env bash

BASE_URL="http://localhost:8081/api/v1/todos"

echo "1) Create todo"
curl -X POST "$BASE_URL" -H "Content-Type: application/json" -d '{
  "description":"Buy milk",
  "dueDatetime":"2025-12-01T10:00:00Z"
}'
```

---
