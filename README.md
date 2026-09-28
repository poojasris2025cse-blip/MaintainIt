# Preventive Maintenance Scheduler for Equipment
**LEAP – Java and DBMS Project**

A complete, robust, full-stack backend system with an integrated Thymeleaf web portal for a workshop to track machinery, schedule preventive maintenance based on usage hours or calendar intervals, assign technicians, log equipment running hours, and eliminate unexpected machine breakdowns.

All data is stored and managed in **MySQL**, accessible via both **REST APIs (Postman)** and a **Clean Web Portal**.

---

## Table of Contents
1. [Project Title](#1-project-title)
2. [Problem Statement](#2-problem-statement)
3. [Project Objective](#3-project-objective)
4. [Key Features](#4-key-features)
5. [Technologies Used](#5-technologies-used)
6. [System Architecture](#6-system-architecture)
7. [Complete Application Flow](#7-complete-application-flow)
8. [Database Design & Schema](#8-database-design--schema)
9. [Entity Relationships](#9-entity-relationships)
10. [Project Structure](#10-project-structure)
11. [MySQL Database Setup](#11-mysql-database-setup)
12. [Spring Boot Configuration](#12-spring-boot-configuration)
13. [How to Run the Application](#13-how-to-run-the-application)
14. [Postman Testing & Automated Collection](#14-postman-testing--automated-collection)
15. [Complete REST API Endpoints](#15-complete-rest-api-endpoints)
16. [Sample JSON Requests & Responses](#16-sample-json-requests--responses)
17. [Business Rules Implementation](#17-business-rules-implementation)
18. [Global Exception & Error Handling](#18-global-exception--error-handling)
19. [MySQL Verification Commands](#19-mysql-verification-commands)
20. [Web Frontend Pages & Navigation](#20-web-frontend-pages--navigation)

---

## 1. Project Title
**Preventive Maintenance Scheduler for Equipment**

---

## 2. Problem Statement
In industrial and manufacturing workshops, equipment failure causes expensive production downtime, safety hazards, and reactive repair bills. Machine maintenance is frequently neglected until a catastrophic breakdown occurs. Workshops need an automated, reliable system to enforce proactive preventive maintenance based on actual runtime operating hours or elapsed calendar cycles.

---

## 3. Project Objective
Build a production-grade backend application with an integrated user-friendly dashboard to:
- Track machines, their operating hours, and maintenance parameters.
- Record incremental operational usage hours via logs.
- Detect when equipment has reached maintenance thresholds.
- Prevent duplicate active maintenance tasks for the same machine.
- Assign certified technicians and log maintenance completion with inspection notes.
- Automatically reset the machine's maintenance baseline upon task completion.

---

## 4. Key Features
- **Dual Maintenance Modes:**
  - **Usage-Based (`USAGE_HOURS`):** Triggers maintenance when `Current Usage >= Last Maintenance Usage + Interval Hours`.
  - **Calendar-Based (`CALENDAR_DAYS`):** Triggers maintenance when `Current Date >= Last Maintenance Date + Interval Days`.
- **Duplicate Prevention:** Prevents generating duplicate tasks if a machine already has an `OPEN` or `IN_PROGRESS` task. Returns `HTTP 409 CONFLICT`.
- **Maintenance Completion & Auto-Reset:** Updating a task to `COMPLETED` records completion notes and timestamps, and resets the machine's `lastMaintenanceUsageHours` and `lastMaintenanceDate`.
- **Overdue Detection:** Query `/api/machines/overdue` returns all machines needing immediate servicing.
- **Full REST API Suite:** Standard CRUD for Machines, Technicians, Usage Logs, and Maintenance Tasks.
- **Thymeleaf Frontend UI:** Integrated Dashboard, Machine Management, Technician Directory, Usage Logger, and Maintenance Operations.
- **Strict Data Validation & Clean JSON Errors:** Rejects invalid, negative, or duplicate references with descriptive error bodies and proper HTTP status codes.

---

## 5. Technologies Used
- **Backend Core:** Java 17 / 21 LTS
- **Framework:** Spring Boot 3.3.4
- **Web Layer:** Spring Web (REST Controllers & MVC)
- **Persistence Layer:** Spring Data JPA & Hibernate ORM
- **Database:** MySQL 8.0 (Physical Local Server on Port 3306)
- **Frontend Template Engine:** Thymeleaf with Vanilla CSS
- **API Testing:** Postman (Collection included: `leap_maintenance_postman_collection.json`)
- **Build Automation:** Apache Maven 3.9.x

---

## 6. System Architecture

### Write / Mutating Flow
```
User / Admin (Browser or Postman)
               │
               ▼
        REST Controller
               │
               ▼
         Service Layer
               │
               ▼
     Business Rules Validation
               │
               ▼
         JPA Repository
               │
               ▼
           Hibernate
               │
               ▼
          MySQL Server
               │
               ▼
     Database Tables (INSERT / UPDATE / DELETE)
```

### Read / Query Flow
```
         MySQL Server
               │
               ▼
           Hibernate
               │
               ▼
         JPA Repository
               │
               ▼
         Service Layer
               │
               ▼
        REST Controller
               │
               ▼
  JSON Response to Postman / HTML to Browser
```

---

## 7. Complete Application Flow

1. **Register Technician:** Add technician with specialization, phone, and unique email.
2. **Register Machine:** Define maintenance type (`USAGE_HOURS` or `CALENDAR_DAYS`), interval, and initial usage.
3. **Log Usage:** As the machine runs, post usage hours. The machine's `currentUsageHours` updates cumulatively in MySQL.
4. **Evaluate Schedule:** Calling `/api/maintenance/check/{machineId}` evaluates whether the threshold is reached.
5. **Generate Maintenance Task:** If due and no active task exists, creates a `MaintenanceTask` in `OPEN` status, flags the machine status as `MAINTENANCE`.
6. **Assign & Perform Maintenance:** Technician inspects and performs required servicing.
7. **Complete Maintenance:** Call `/api/maintenance/{id}/complete` with `technicianId` and `completionNotes`. The task is marked `COMPLETED`, `completedAt` is recorded, and the machine's `lastMaintenanceUsageHours` and `lastMaintenanceDate` are reset.
8. **Continue Monitoring:** The cycle repeats seamlessly for subsequent maintenance cycles.

---

## 8. Database Design & Schema

### Database Name
`leap_maintenance`

### Tables
1. **`machine`**
   - `id` (BIGINT, PK, AUTO_INCREMENT)
   - `machine_name` (VARCHAR(255), NOT NULL)
   - `machine_code` (VARCHAR(255), NOT NULL, UNIQUE)
   - `description` (TEXT)
   - `maintenance_type` (VARCHAR(50), NOT NULL) — `USAGE_HOURS` or `CALENDAR_DAYS`
   - `maintenance_interval_hours` (INT)
   - `maintenance_interval_days` (INT)
   - `current_usage_hours` (INT, NOT NULL, DEFAULT 0)
   - `last_maintenance_date` (DATE)
   - `last_maintenance_usage_hours` (INT, NOT NULL, DEFAULT 0)
   - `status` (VARCHAR(50), NOT NULL, DEFAULT 'ACTIVE') — `ACTIVE`, `INACTIVE`, `MAINTENANCE`
   - `created_at` (DATETIME, NOT NULL)

2. **`technician`**
   - `id` (BIGINT, PK, AUTO_INCREMENT)
   - `name` (VARCHAR(255), NOT NULL)
   - `email` (VARCHAR(255), NOT NULL, UNIQUE)
   - `phone` (VARCHAR(50))
   - `specialization` (VARCHAR(255))
   - `status` (VARCHAR(50), NOT NULL, DEFAULT 'ACTIVE') — `ACTIVE`, `INACTIVE`

3. **`usage_log`**
   - `id` (BIGINT, PK, AUTO_INCREMENT)
   - `machine_id` (BIGINT, FK -> `machine.id`, NOT NULL)
   - `usage_hours` (INT, NOT NULL)
   - `recorded_at` (DATETIME, NOT NULL)
   - `remarks` (TEXT)

4. **`maintenance_task`**
   - `id` (BIGINT, PK, AUTO_INCREMENT)
   - `machine_id` (BIGINT, FK -> `machine.id`, NOT NULL)
   - `technician_id` (BIGINT, FK -> `technician.id`, NULLABLE)
   - `task_type` (VARCHAR(100))
   - `description` (TEXT)
   - `status` (VARCHAR(50), NOT NULL, DEFAULT 'OPEN') — `OPEN`, `IN_PROGRESS`, `COMPLETED`, `OVERDUE`
   - `created_at` (DATETIME, NOT NULL)
   - `due_date` (DATE)
   - `due_usage_hours` (INT)
   - `completed_at` (DATETIME)
   - `completion_notes` (TEXT)

---

## 9. Entity Relationships

```
┌──────────────┐ 1        * ┌──────────────┐
│   Machine    │───────────<│   UsageLog   │
└──────────────┘            └──────────────┘
       │ 1
       │
       │ *
┌──────────────┐ *        1 ┌──────────────┐
│MaintenanceTask├───────────>│  Technician  │
└──────────────┘            └──────────────┘
```

- **One-to-Many:** `Machine` -> `UsageLog` (one machine has multiple usage logs).
- **One-to-Many:** `Machine` -> `MaintenanceTask` (one machine has multiple maintenance tasks).
- **Many-to-One:** `MaintenanceTask` -> `Technician` (one technician can handle multiple tasks).
- Circular serialization is prevented using `@JsonIgnore` on the child lists and `@ManyToOne` eager fetching on references.

---

## 10. Project Structure

```
MaintainIt/
├── pom.xml
├── leap_maintenance_postman_collection.json
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── leap/
│   │   │           └── maintenance/
│   │   │               ├── LeapMaintenanceApplication.java
│   │   │               ├── controller/
│   │   │               │   ├── MachineController.java
│   │   │               │   ├── TechnicianController.java
│   │   │               │   ├── UsageLogController.java
│   │   │               │   ├── MaintenanceTaskController.java
│   │   │               │   └── WebController.java
│   │   │               ├── service/
│   │   │               │   ├── MachineService.java
│   │   │               │   ├── TechnicianService.java
│   │   │               │   ├── UsageLogService.java
│   │   │               │   └── MaintenanceTaskService.java
│   │   │               ├── repository/
│   │   │               │   ├── MachineRepository.java
│   │   │               │   ├── TechnicianRepository.java
│   │   │               │   ├── UsageLogRepository.java
│   │   │               │   └── MaintenanceTaskRepository.java
│   │   │               ├── entity/
│   │   │               │   ├── Machine.java
│   │   │               │   ├── Technician.java
│   │   │               │   ├── UsageLog.java
│   │   │               │   └── MaintenanceTask.java
│   │   │               ├── dto/
│   │   │               │   ├── MachineRequest.java
│   │   │               │   ├── TechnicianRequest.java
│   │   │               │   ├── UsageLogRequest.java
│   │   │               │   ├── MaintenanceTaskRequest.java
│   │   │               │   └── CompleteMaintenanceRequest.java
│   │   │               └── exception/
│   │   │                   ├── ResourceNotFoundException.java
│   │   │                   ├── BusinessRuleException.java
│   │   │                   ├── ErrorResponse.java
│   │   │                   └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       │   └── css/
│   │       │       └── style.css
│   │       └── templates/
│   │           ├── fragments.html
│   │           ├── dashboard.html
│   │           ├── machines.html
│   │           ├── technicians.html
│   │           ├── usage-logs.html
│   │           └── maintenance.html
```

---

## 11. MySQL Database Setup

Log into MySQL command-line or MySQL Workbench:

```sql
-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS leap_maintenance;

-- Step 2: Switch to Database
USE leap_maintenance;

-- Tables will be automatically created on startup by Hibernate DDL auto!
```

---

## 12. Spring Boot Configuration
File: `src/main/resources/application.properties`

```properties
server.port=8080
spring.application.name=leap-maintenance

# Database connection
spring.datasource.url=jdbc:mysql://localhost:3306/leap_maintenance?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Pooja@123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Jackson configuration
spring.jackson.serialization.write-dates-as-timestamps=false
```

---

## 13. How to Run the Application

In the project root directory (`C:\Users\Pooja\Desktop\MaintainIt`):

```bash
# 1. Clean and compile
mvn clean compile

# 2. Start the Spring Boot application
mvn spring-boot:run
```

Once started, verify:
- **Web UI:** Open browser at `http://localhost:8080/dashboard`
- **REST APIs:** Use Postman against `http://localhost:8080/api/...`

---

## 14. Postman Testing & Automated Collection

A complete, pre-configured Postman collection is saved in the project root:
📁 **`leap_maintenance_postman_collection.json`**

### How to Import:
1. Open Postman.
2. Click **Import** (top left).
3. Drag and drop `leap_maintenance_postman_collection.json` or browse to `C:\Users\Pooja\Desktop\MaintainIt\leap_maintenance_postman_collection.json`.
4. Click **Import**.
5. All folders (**1. Technicians**, **2. Machines**, **3. Usage Logs**, **4. Maintenance Flow**, **5. Error Testing & Validations**) are ready to execute!

---

## 15. Complete REST API Endpoints

### Machine Endpoints
| HTTP Method | URL | Description | Expected Status |
|---|---|---|---|
| `POST` | `/api/machines` | Register a new machine | `201 CREATED` |
| `GET` | `/api/machines` | Retrieve all machines | `200 OK` |
| `GET` | `/api/machines/{id}` | Retrieve machine by ID | `200 OK` |
| `PUT` | `/api/machines/{id}` | Update machine details | `200 OK` |
| `DELETE` | `/api/machines/{id}` | Delete machine | `204 NO CONTENT` |
| `GET` | `/api/machines/overdue` | List machines overdue for maintenance | `200 OK` |
| `GET` | `/api/machines/{machineId}/maintenance-history` | List task history for a machine | `200 OK` |

### Technician Endpoints
| HTTP Method | URL | Description | Expected Status |
|---|---|---|---|
| `POST` | `/api/technicians` | Create technician | `201 CREATED` |
| `GET` | `/api/technicians` | List all technicians | `200 OK` |
| `GET` | `/api/technicians/{id}` | Get technician by ID | `200 OK` |
| `PUT` | `/api/technicians/{id}` | Update technician | `200 OK` |
| `DELETE` | `/api/technicians/{id}` | Delete technician | `204 NO CONTENT` |

### Usage Log Endpoints
| HTTP Method | URL | Description | Expected Status |
|---|---|---|---|
| `POST` | `/api/usage-logs` | Record hours run (updates machine) | `201 CREATED` |
| `GET` | `/api/usage-logs` | Get all usage logs | `200 OK` |
| `GET` | `/api/usage-logs/{id}` | Get log by ID | `200 OK` |
| `DELETE` | `/api/usage-logs/{id}` | Delete log | `204 NO CONTENT` |

### Maintenance Task Endpoints
| HTTP Method | URL | Description | Expected Status |
|---|---|---|---|
| `POST` | `/api/maintenance/check/{machineId}` | Check threshold & trigger task | `201 CREATED` |
| `PUT` | `/api/maintenance/{id}/complete` | Mark task completed & reset baseline | `200 OK` |
| `GET` | `/api/maintenance` | Get all maintenance tasks | `200 OK` |
| `GET` | `/api/maintenance/open` | Get open/in-progress tasks | `200 OK` |
| `GET` | `/api/maintenance/{id}` | Get task by ID | `200 OK` |
| `POST` | `/api/maintenance` | Create manual maintenance task | `201 CREATED` |
| `PUT` | `/api/maintenance/{id}` | Update task details | `200 OK` |
| `DELETE` | `/api/maintenance/{id}` | Delete task | `204 NO CONTENT` |

---

## 16. Sample JSON Requests & Responses

### 1. Create Technician
**POST** `/api/technicians`
```json
{
    "name": "Arun Kumar",
    "email": "arun@example.com",
    "phone": "9876543210",
    "specialization": "Mechanical Maintenance",
    "status": "ACTIVE"
}
```
**Response (201 CREATED):**
```json
{
    "id": 1,
    "name": "Arun Kumar",
    "email": "arun@example.com",
    "phone": "9876543210",
    "specialization": "Mechanical Maintenance",
    "status": "ACTIVE"
}
```

### 2. Create Machine
**POST** `/api/machines`
```json
{
    "machineName": "CNC Machine",
    "machineCode": "CNC-001",
    "description": "Workshop CNC machine",
    "maintenanceType": "USAGE_HOURS",
    "maintenanceIntervalHours": 100,
    "maintenanceIntervalDays": null,
    "currentUsageHours": 0,
    "status": "ACTIVE"
}
```
**Response (201 CREATED):**
```json
{
    "id": 1,
    "machineName": "CNC Machine",
    "machineCode": "CNC-001",
    "description": "Workshop CNC machine",
    "maintenanceType": "USAGE_HOURS",
    "maintenanceIntervalHours": 100,
    "maintenanceIntervalDays": null,
    "currentUsageHours": 0,
    "lastMaintenanceDate": null,
    "lastMaintenanceUsageHours": 0,
    "status": "ACTIVE",
    "createdAt": "2026-09-28T10:26:12"
}
```

### 3. Log Usage Hours
**POST** `/api/usage-logs`
```json
{
    "machineId": 1,
    "usageHours": 50,
    "remarks": "Normal operation"
}
```
**Response (201 CREATED):**
```json
{
    "id": 1,
    "machine": {
        "id": 1,
        "machineName": "CNC Machine",
        "currentUsageHours": 50
    },
    "usageHours": 50,
    "recordedAt": "2026-09-28T10:26:44",
    "remarks": "Normal operation"
}
```

### 4. Check & Generate Maintenance
**POST** `/api/maintenance/check/1`
**Response (201 CREATED):**
```json
{
    "id": 1,
    "machine": {
        "id": 1,
        "machineName": "CNC Machine",
        "currentUsageHours": 100,
        "status": "MAINTENANCE"
    },
    "technician": null,
    "taskType": "USAGE_HOURS",
    "description": "Preventive maintenance triggered for CNC Machine (USAGE_HOURS)",
    "status": "OPEN",
    "createdAt": "2026-09-28T10:27:11",
    "dueDate": null,
    "dueUsageHours": 100,
    "completedAt": null,
    "completionNotes": null
}
```

### 5. Complete Maintenance Task
**PUT** `/api/maintenance/1/complete`
```json
{
    "technicianId": 1,
    "completionNotes": "Machine inspected and lubricated."
}
```
**Response (200 OK):**
```json
{
    "id": 1,
    "machine": {
        "id": 1,
        "machineName": "CNC Machine",
        "currentUsageHours": 100,
        "lastMaintenanceDate": "2026-09-28",
        "lastMaintenanceUsageHours": 100,
        "status": "ACTIVE"
    },
    "technician": {
        "id": 1,
        "name": "Arun Kumar"
    },
    "taskType": "USAGE_HOURS",
    "status": "COMPLETED",
    "completedAt": "2026-09-28T10:27:30",
    "completionNotes": "Machine inspected and lubricated."
}
```

---

## 17. Business Rules Implementation

- **RULE 1 – Duplicate Maintenance Prevention:**
  If a machine already has an `OPEN` or `IN_PROGRESS` task, any subsequent call to generate maintenance throws `BusinessRuleException` returning `HTTP 409 CONFLICT`:
  ```json
  {
      "timestamp": "2026-09-28T10:27:24",
      "status": 409,
      "error": "Conflict",
      "message": "An open maintenance task already exists for this machine."
  }
  ```
- **RULE 2 – Usage-Based Maintenance:**
  Maintenance is due when `currentUsageHours >= lastMaintenanceUsageHours + maintenanceIntervalHours`.
- **RULE 3 – Calendar-Based Maintenance:**
  Maintenance is due when `today >= lastMaintenanceDate + maintenanceIntervalDays`.
- **RULE 4 – Maintenance Completion Baseline Reset:**
  Upon completion, the task status becomes `COMPLETED`, `completedAt` is recorded, and the machine's `lastMaintenanceUsageHours` is updated to current usage hours and `lastMaintenanceDate` to today's date.
- **RULE 5 – Completed Task Protection:**
  Attempting to complete an already `COMPLETED` task throws `BusinessRuleException` returning `HTTP 409 CONFLICT`:
  `"Maintenance task is already completed."`
- **RULE 6 – Foreign Key Integrity & Reference Validation:**
  Invalid machine ID or technician ID returns `HTTP 404 NOT FOUND`.
- **RULE 7 – Field Validation:**
  Negative intervals, negative usage hours, or blank names/emails are intercepted and return `HTTP 400 BAD REQUEST`. Unique constraints on machine codes and emails return `HTTP 409 CONFLICT`.

---

## 18. Global Exception & Error Handling
Handled centrally by `GlobalExceptionHandler` with `@RestControllerAdvice`. Stack traces are suppressed; callers receive standard JSON errors:

```json
{
    "timestamp": "2026-09-28T10:29:31",
    "status": 404,
    "error": "Not Found",
    "message": "Machine with ID 999 not found"
}
```

---

## 19. MySQL Verification Commands

After any operation, run in MySQL command-line or Workbench:

```sql
USE leap_maintenance;

-- View all registered machines
SELECT id, machine_code, machine_name, maintenance_type, current_usage_hours, last_maintenance_usage_hours, status FROM machine;

-- View all registered technicians
SELECT id, name, email, specialization, status FROM technician;

-- View all logged operational usage hours
SELECT id, machine_id, usage_hours, recorded_at, remarks FROM usage_log;

-- View all maintenance tasks with status and assigned technician
SELECT id, machine_id, technician_id, task_type, due_usage_hours, status, completed_at, completion_notes FROM maintenance_task;
```

---

## 20. Web Frontend Pages & Navigation

Access the integrated Web Portal at `http://localhost:8080/`:
1. **`/dashboard`** — Live KPI cards (Total Machines, Active Machines, Overdue Machines, Open Tasks, Technicians), Overdue equipment table with 1-click maintenance trigger, and active task status.
2. **`/web/machines`** — Complete machine table with Add/Edit forms, usage metrics, status badges, and 1-click interval evaluation.
3. **`/web/technicians`** — Technician directory, email/phone management, specialization tracker.
4. **`/web/usage-logs`** — Usage logger form with immediate machine cumulative hour updates and running log history.
5. **`/web/maintenance`** — Full maintenance dashboard: manual task scheduling, threshold evaluation, technician task completion form, and maintenance history log.
