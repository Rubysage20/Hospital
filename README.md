# Hospital Management System - Enhancement 2: Algorithms and Databases

##  Project Overview
The **Hospital Management System (HMS)** is a Java-based application designed to streamline hospital operations such as patient management, appointment scheduling, doctor assignments, and payroll.  

This branch, **Enhancement 2: Algorithms and Databases**, focuses on improving **efficiency and scalability** by optimizing algorithms for core processes and enhancing database interactions.

---

##  Key Enhancements

### 🔹 Algorithms
- **Patient Search Optimization**  
  Implemented **hash maps** for constant-time lookup of patient records.  
- **Appointment Scheduling**  
  Added **priority queues** to schedule urgent/emergency appointments more efficiently.  
- **Payroll and Reports**  
  Improved **sorting algorithms** to generate organized payroll reports and staff scheduling outputs.

### 🔹 Database
- **Schema Refinement**  
  Updated schema design to reduce redundancy and improve relational integrity.  
- **Indexing**  
  Introduced database indexing on frequently queried fields to boost performance.  
- **Query Optimization**  
  Refactored SQL queries to reduce execution time for large datasets.  

---

##  Tech Stack
- **Language:** Java  
- **Database:** MySQL / Oracle (configurable)  
- **Tools:** JDBC, Git, GitHub  

---

## Project Structure
HospitalManagementSystem/
│── src/
│   ├── models/
│   │   ├── Patient.java
│   │   ├── Doctor.java
│   │   ├── Appointment.java
│   │   ├── Payroll.java
│   │   └── Department.java
│   │
│   ├── services/
│   │   ├── PatientService.java        # HashMap-based search
│   │   ├── AppointmentService.java    # PriorityQueue scheduling
│   │   ├── PayrollService.java        # Sorting for reporting
│   │   └── DatabaseService.java       # Database connection & queries
│   │
│   ├── utils/
│   │   └── InputValidator.java
│   │
│   └── Main.java                      # Entry point
│
│── database/
│   └── schema.sql                     # SQL file to set up DB
│
│── README.md
│── .gitignore

## Database Setup
1. Import the `schema.sql` file into MySQL or Oracle.
2. Update your database credentials in `DatabaseService.java`.
3. Run the program to connect and test database features.

## Testing the Enhancements
- **Search Test:** Add 1000+ patient records and test retrieval time.
- **Scheduling Test:** Create mixed-priority appointments and confirm urgent ones are scheduled first.
- **Payroll Test:** Generate payroll for 50+ employees and validate sorted output.

## Future Improvements
- Add predictive scheduling using machine learning.
- Integrate MongoDB for NoSQL scalability.
- Implement role-based authentication for database operations.
- Expand reporting with data visualization dashboards.

## Author
**Valerie Dawson**  
- Computer Science Major (SNHU, graduating Oct 2025)  
- Aspiring Software Engineer | AWS Cloud Practitioner Certified  

