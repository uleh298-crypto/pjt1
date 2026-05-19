# System Architecture Suitability Analysis

This document analyzes the fit between the proposed system architecture (`architecture.md`) and the database schema (`ssabre.vuerd.json`).

## Summary
The proposed **Single EC2 + Spring Boot + Local Redis/PostgreSQL** architecture is **well-suited for an MVP (Minimum Viable Product)** or a pilot stage of the "SSABRE" platform. It provides a solid foundation for a relational data model and supports essential asynchronous features like notifications.

## Key Strengths

1.  **Relational Fit**:
    *   **Data Model**: The schema contains highly relational data (`Members`, `Classes`, `Studies`, `Teams`, `Portfolios`). PostgreSQL is the correct choice ensures data integrity across these relationships (e.g., referential integrity between `enrollment` and `classes`).
    *   **Complexity Handling**: Spring Boot (JPA/Hibernate) handles complex join operations and transaction management effectively, which is necessary for features like "retrieving all studies a member applies to".

2.  **Asynchronous Notification Support**:
    *   **Requirement**: The schema includes `messages`, `study_applications`, and `team_applications`. These interactiosn necessitate user notifications.
    *   **Architecture Fit**: The included `Redis` (Queue) -> `Worker` -> `FCM` flow is the industry-standard pattern for handling push notifications without blocking user requests (e.g., sending a DM returns 200 OK immediately, while the push notification is sent in the background).

3.  **Simplicity**:
    *   Hosting everything on a single EC2 instance simplifies deployment and debugging, allowing the team to focus on feature development rather than infrastructure management during the early stages.

## Considerations & Recommendations

While the architecture fits the core needs, consider the following potential gaps based on the schema's features:

### 1. File & Image Storage (Critical)
*   **Observation**: The schema includes `portfolios` and `posts`, which typically require image or file uploads (profile pictures, project PDFs, post images).
*   **Constraint (User-Defined)**: Must run on a **Single EC2 Instance**. Cloud Object Storage (S3) is not an option.
*   **Recommendation**:
    *   **Local File System with Docker Volumes**:
        *   Create a host directory (e.g., `/home/ubuntu/ssabre-data/uploads`).
        *   Mount this directory into the Spring Boot container (e.g., `-v /home/ubuntu/ssabre-data/uploads:/app/uploads`).
        *   **Serving Files**: Configure **Nginx** (the reverse proxy) to serve these static files directly. Map a path like `/uploads/` in Nginx to this physical directory. This avoids the overhead of passing file requests through the Spring Boot application.
    *   **Backup Strategy**: Since data is local, **regular backups are mandatory**. Use a cron job to periodically compress the `uploads` folder and the database dump, then copy them to a separate EBS volume or download them to a safe location.

### 2. Real-Time Messaging
*   **Observation**: The presence of `messages` and `message_rooms` suggests a Chat feature.
*   **Gap**: The current architecture relies on HTTP requests + FCM. This works for "Email-style" messaging but may feel sluggish for real-time chat.
*   **Recommendation**:
    *   If **instant** chat is a priority, consider utilizing **WebSockets** (STOMP) within Spring Boot.
    *   (Note: For an MVP, the current HTTP+FCM approach is acceptable and easier to implement).

### 3. Search Capabilities
*   **Observation**: Tables like `stacks` (`portfolio_stacks`), `studies`, and `posts` imply users will search for content (e.g., "Find usage of 'React' in portfolios").
*   **Recommendation**: PostgreSQL's generic `LIKE` or basic Full-Text Search is sufficient multiple thousands of rows. If the dataset grows significantly, you might eventually need dedicated search (Elasticsearch), but it is **not** needed yet.

### 4. Single Point of Failure
*   **Observation**: Local DB and Redis on the same EC2 instance.
*   **Risk**: If the EC2 instance fails, the entire service (including data access) goes down. Data on local disk must be backed up.
*   **Recommendation**: Ensure frequent **Automated Backups** of the PostgreSQL data and uploaded files. Copy these backups to a separate EBS volume, a different server, or download them regularly to a local machine to prevent total data loss in case of instance failure.

## Conclusion
**The architecture fits.** It is a pragmatic, standard approach for this type of application foundation. It supports the complex relationships defined in the Vuerd schema and correctly anticipates the need for background processing (notifications).

**Next Steps**:
1.  Verify where file uploads will be stored.
2.  Implement a backup strategy for the local PostgreSQL.
