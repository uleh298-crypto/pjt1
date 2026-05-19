# System Architecture Setup Guide

This guide explains **how your system works** and **how to set it up**.
It is designed to help you understand the role of each component (Spring Boot, Redis, PostgreSQL, Nginx) so you can develop with confidence.

## 1. The Big Picture: How Data Flows

We are building a **Single EC2 Instance** architecture. Imagine your EC2 server as a single house where all these workers live together.

```mermaid
graph TD
    User([User's Browser / App])
    
    subgraph EC2_Server [Your EC2 Server]
        direction TB
        
        %% Entry Point
        Nginx[Nginx (The Doorman)]
        
        %% Applications
        SpringBoot[Spring Boot (The Brain)]
        
        %% Data Stores
        Redis[(Redis - The Sticky Note)]
        Postgres[(PostgreSQL - The Filing Cabinet)]
        
        %% File Storage
        HardDrive[Local Hard Drive (Uploads)]
    end

    %% Interactions
    User -->|Requests URL| Nginx
    
    %% Nginx Routing
    Nginx -->|/api/...| SpringBoot
    Nginx -->|/uploads/...| HardDrive
    
    %% Spring Boot Logic
    SpringBoot -->|Save User/Study data| Postgres
    SpringBoot -->|Quick Task: 'Send Notification'| Redis
    
    %% Background Worker (Inside Spring Boot)
    Worker((Worker Thread))
    Worker -.->|Check for Tasks| Redis
```

## 2. Component Roles (Why do we need them?)

### A. Nginx (The Traffic Controller)
*   **Role**: It is the first thing users hit.
*   **Why**:
    *   **Security**: Hides your internal ports (8080).
    *   **Speed**: Serves images (uploads) directly from the hard drive **extremely fast**. It doesn't bother the Java server for simple files.
    *   **Routing**: Sends API requests (`/api/login`) to Spring Boot and file requests (`/uploads/profile.jpg`) to the disk.

### B. Spring Boot (The Brain)
*   **Role**: Your main application logic.
*   **What it does for you**:
    *   **API**: Receives JSON requests from the frontend.
    *   **Business Logic**: "Can this user join this study group?"
    *   **Database Access**: Talks to PostgreSQL to save/load data.
    *   **Producer**: When a DM is sent, it *quickly* puts a "Send Push Notification" note into Redis.
    *   **Consumer (Worker)**: A separate background thread wakes up, reads the note from Redis, and actually sends the notification to Google/FCM.

### C. PostgreSQL (The Long-Term Memory)
*   **Role**: Your main database.
*   **Why**: It stores complex relationships reliably.
    *   *Example*: "Find all students enrolled in the 'Java Study' class."
    *   This data is **critical** and must be backed up.

### D. Redis ( The Short-Term Memory / Inbox)
*   **Role**: A super-fast, in-memory key-value store.
*   **Why we use it here**: **Task Queue**.
*   **Scenario (Sending a DM)**:
    1.  User sends "Hello".
    2.  Spring Boot saves "Hello" to Postgres (Permanent).
    3.  Spring Boot puts "Notify User B" into Redis (Temporary Task).
    4.  Spring Boot replies "Message Sent" to User A (Instant success).
    5.  *...Milliseconds later...*
    6.  Worker sees "Notify User B" in Redis and talks to Google/FCM.
    *   **Benefit**: User A never waits for Google. If Google is down, the task stays in Redis to try again later.

## 3. Project Directory Structure
To support this, we will organize your project like this:

```
S14P11D103/
├── backend/                # Your Spring Boot Code
│   ├── src/main/java/...   # Java Code
│   └── Dockerfile          # Instructions to run backend
├── nginx/                  # Nginx Config
│   └── nginx.conf          # Rules for routing traffic
├── docker-compose.yml      # The "Master Plan" to run everything
└── .env                    # Secrets (DB passwords) - NEVER COMMIT THIS
```

## 4. How to Run It (The Standard Workflow)

We will use **Docker Compose**. It acts like a conductor, starting all 4 components at once.

**1. Define the System (`docker-compose.yml`)**
We write a file that says:
*   "Start Postgres 15"
*   "Start Redis 7"
*   "Build my Java App and connect it to Postgres & Redis"
*   "Start Nginx and open port 80 to the world"

**2. The Command**
You just type:
```bash
docker-compose up -d --build
```
*   `up`: Start everything.
*   `-d`: Detached mode (run in background).
*   `--build`: Recompile my Java code if I changed it.

**3. Development Workflow**
*   **Coding**: You write Java code in `backend/`.
*   **Testing**: You run the app locally (or in Docker).
*   **Deploying**: specific to your single EC2, you will likely `git pull` on the server and run the docker command again.

## 5. Next Steps for You
Since you are new to Spring Boot:
1.  **Don't worry about the complex infrastructure yet.** I will write the `docker-compose.yml` for you.
2.  **Focus on the Java Code**: You will write "Controllers" (API endpoints) and "Services" (Logic).
3.  **Redis Usage**: You won't write raw Redis commands. You will use a library (Spring Data Redis) that makes it look like simple Java method calls.

---
**Ready?** I can now generate the `docker-compose.yml` and `nginx.conf` so you have a working "skeleton" system.
