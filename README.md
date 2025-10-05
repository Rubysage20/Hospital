# Hospital Management System  
### Enhancement 3: Mongo Integration, Scheduling Logic, and Payroll Automation  
**Course:** CS-499 Computer Science Capstone  
**Category:** Software Design & Engineering | Algorithms & Data Structures | Databases  
**Author:** Valerie Dawson  

---

## Project Overview
Enhancement 3 builds upon the existing Hospital Management System by integrating MongoDB for persistent data storage, refining the appointment scheduling logic with priority-based algorithms, and updating payroll automation for accuracy.  
This milestone demonstrates the combination of **software design, algorithm optimization, and database management** within a unified backend system.

---

## Key Enhancements

### 1. MongoDB Integration
- Implemented full CRUD repositories for **doctors**, **patients**, **appointments**, and **employees**.  
- Added indexing for fast lookups on fields such as `doctorId` and `patientId`.  
- Used repository classes (`MongoDoctorRepository`, `MongoPatientRepository`, `MongoAppointmentRepository`) to manage persistence and data validation.  
- Connected to `gms_db` for real-world simulation of clinical record management.

### 2. Optimized Appointment Scheduling
- Created a **priority queue** system to manage urgent vs. normal appointments.  
- Added **conflict detection** logic to prevent double-booking for the same doctor.  
- Normalized all appointments to **1-hour intervals** for consistency.  
- Enhanced the `AppointmentScheduler` to handle doctor availability, automatic conflict resolution, and patient validation.  

### 3. Payroll and Reporting Updates
- Updated payroll summary logic to calculate doctor pay based on the **number of completed appointments** rather than static counters.  
- Implemented dynamic data retrieval from the `appointments` collection to ensure accurate monthly reporting.  
- Streamlined doctor and employee payroll summaries for real-time output and clarity.

---

## Algorithms and Data Structures
- **Priority Queue:** Used to handle appointment urgency and scheduling order.  
- **Hash Maps:** Improve lookup time for doctor and patient retrieval.  
- **Sorting Algorithms:** Organize appointment data for chronological reporting.  
- **Validation Utils:** Ensure data integrity before database transactions.

---

## Database Layer
- **Database:** MongoDB (`gms_db`)  
- **Collections:** `doctors`, `patients`, `appointments`, `employees`  
- **Indexes:** Created for quick access by ID and to prevent duplicate appointment times.  
- **Integration:** Uses `MongoConnection.db()` for streamlined connectivity.  

---

## Tools and Technologies
- **Java 17**  
- **MongoDB** (local or Atlas cluster)  
- **Eclipse IDE**  
- **Git & GitHub**  
- **Command-line (PowerShell/Bash)**  

---

## How to Run
1. Clone the repository  
   ```bash
   git clone https://github.com/Rubysage20/Hospital.git
