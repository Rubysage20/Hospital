# Hospital Management System - Enhancement 2: Algorithms and Databases

## 📌 Project Overview
The **Hospital Management System (HMS)** is a Java-based application designed to streamline hospital operations such as patient management, appointment scheduling, doctor assignments, and payroll.  

This branch, **Enhancement 2: Algorithms and Databases**, focuses on improving **efficiency and scalability** by optimizing algorithms for core processes and enhancing database interactions.

---

## 🚀 Key Enhancements

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

## 🛠️ Tech Stack
- **Language:** Java  
- **Database:** MySQL / Oracle (configurable)  
- **Tools:** JDBC, Git, GitHub  

---

## 📂 Project Structure
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
