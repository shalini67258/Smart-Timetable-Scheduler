# 🗓️ Smart Timetable Scheduler

<p align="center">
  <img src="https://img.shields.io/badge/Backend-Java%20Spring%20Boot%203.x-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Database-PostgreSQL-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Frontend-Thymeleaf%20%2B%20JS-orange?style=for-the-badge&logo=html5" alt="Thymeleaf"/>
  <img src="https://img.shields.io/badge/Architecture-MVC%20%2F%20Secured-blueviolet?style=for-the-badge" alt="MVC"/>
</p>

---

## 🚀 The Ultimate Project Vision
**Smart Timetable Scheduler** is a high-performance, enterprise-grade academic scheduling platform designed to eliminate institutional planning stress. Driven by an intelligent **Multi-Variable Constraint Solver Engine**, the application seamlessly matches classrooms, faculty availability, and structural syllabus limits to construct 100% conflict-free master grids instantly. 

No more double-booked professors, overlapping sections, or impossible classroom collisions. It completely bridges automated speed with manual administrative precision.

---

## 🔥 Core Killer Features

### 🧠 1. Intelligent Hybrid Matrix Generator
* **One-Click Automated Compiler:** Reads configuration parameters dynamically from the database (start time, period durations, breaks, working days) and fills the entire schedule automatically using a multi-dimensional matching array.
* **Granular Manual Overrides:** Administrators have total authority. You can manually assign, edit, or lock specific slots. The automated generation pipeline instantly respects these manual blocks, routing all auto-assignments completely *around* them.

### 🛡️ 2. Zero-Stress Conflict Isolation Architecture
The platform operates under absolute mathematical rules to prevent operational human error:
* **Faculty Isolation:** A teacher cannot be assigned to two different classes or sections during the same slot anywhere in the institution.
* **Classroom Isolation:** A lecture hall or laboratory cannot be double-booked. The engine tracks active space allocations across all branches simultaneously.
* **Branch-Year Mapping Protection:** Ensures that curriculum guidelines are perfectly maintained for every academic division.

### 👥 3. Identity-Based Session Routing (Personalized Dashboards)
Users do not look through giant, confusing master charts. The system uses an **authenticated session-filtering routing layout**:
* **👨‍🎓 Students Portal:** Upon logging in, the system reads the student's profile group and automatically renders **only** the timetable belonging to their specific **Year, Branch, and Section** (e.g., *2nd Year - CSE-A*).
* **👩‍🏫 Faculty Portal:** Professors log into a private interface that filters the global matrix database to present **only** their personal weekly teaching tracks, room assignments, and free intervals.
* **👑 Admin Console:** Central nervous system used to register classrooms, enroll subjects, map faculty variables, adjust institutional constraints, and execute the master scheduler.

### 📝 4. Whitelisted Institutional Registration
* Guardrail registration logic that prevents random public accounts from signing up. 
* Faculty and staff accounts can only complete registration if their specific identifiers match pre-loaded database pools authorized by the Administrator.

---

## 🧬 System Architecture & Data Flow

```text
  ┌────────────────────────────────────────────────────────┐
  │                 Administrative Input                   │
  │  (Classrooms, Faculty Pools, Year-Subject Curriculum)   │
  └───────────────────────────┬────────────────────────────┘
                              ▼
  ┌────────────────────────────────────────────────────────┐
  │            Conflict Ingestion Control Engine           │
  │     [1. Lock Manual Slots] -> [2. Isolate Resources]   │
  └───────────────────────────┬────────────────────────────┘
                              ▼
  ┌────────────────────────────────────────────────────────┐
  │         Multi-Layer Allocation Matrix Solver           │
  │  Checks: Is Teacher Free? | Is Room Free? | Match Slot  │
  └───────────────────────────┬────────────────────────────┘
                              ▼
  ┌────────────────────────────────────────────────────────┐
  │                 PostgreSQL Persistence                 │
  │      Verifies ACID Compliance & Commits Base Rows      │
  └───────────────────────────┬────────────────────────────┘
                              ▼
         ┌────────────────────┼────────────────────┐
         ▼                    ▼                    ▼
  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
  │ Admin Portal │     │Faculty view  │     │ Student View │
  │ (Full Matrix)│     │(My Lectures) │     │ (My Section) │
  └──────────────┘     └──────────────┘     └──────────────┘
