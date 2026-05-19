# Lesson 1: Campus Feature (Vertical Slice)

We have successfully implemented the **Campus Feature** to establish the standard Spring Boot architecture pattern.

## What We Built

| Layer | File | Role |
| :--- | :--- | :--- |
| **Entity** | `Campus.java` | Defines the `campuses` table (id, name). |
| **Repository** | `CampusRepository.java` | Interface for DB operations (save, findAll). |
| **Service** | `CampusService.java` | Business logic (transaction management). |
| **Controller** | `CampusController.java` | API Endpoints (`/api/campuses`). |

## How to Verify

### 1. Check Infrastructure
Ensure all 4 containers are running:
```bash
docker-compose ps
# Should see: ssabre_backend, ssabre_postgres, ssabre_redis, ssabre_nginx
```

### 2. Test API Endpoints

**Create a Campus (POST)**
```bash
# Returns the new ID (e.g., 1)
curl -X POST "http://localhost/api/campuses?name=Seoul"
```

**List Campuses (GET)**
Open in Browser: [http://localhost/api/campuses](http://localhost/api/campuses)
```json
[
  {
    "id": 1,
    "name": "Seoul"
  }
]
```

## Next Steps
This pattern (Entity -> Repo -> Service -> Controller) is universal.
For the next feature (e.g., `Members`), you will repeat these exact same steps, just adding more fields and relationships.
