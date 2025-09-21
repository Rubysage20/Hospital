# Hospital Management System – Enhancement 1

## Overview
This enhancement demonstrates **Software Design and Engineering** improvements for the Hospital Management System artifact.  
The goal was to make the system scalable, maintainable, and ready for database integration while also improving code quality and documentation.

## Key Enhancements
- **Encapsulation and Validation**
  - All domain classes (`Doctor`, `Patient`, `Employee`, `Person`) updated with private fields and validated setters.
  - `ValidationUtils` utility added to centralize input checks (non-null, non-blank, non-negative).
  - Immutable unique IDs added to `Person` and all subclasses.

- **Scalable Data Management**
  - Replaced fixed-size arrays with repository interfaces and in-memory implementations.
  - Designed repositories (`DoctorRepository`, `PatientRepository`, `EmployeeRepository`, `AppointmentRepository`) to be **storage-agnostic** and ready for MongoDB integration.

- **Console CRUD Menus**
  - `Clinic.java` enhanced with CRUD menus for Doctors, Patients, and Employees.
  - Appointment scaffolding added (`Appointment` class, repo, in-memory repo) for future algorithm and scheduling features.

- **Code Quality**
  - Added Javadoc and inline comments throughout the code.
  - Consistent formatting and naming.
  - Currency formatting for monetary outputs.

## What This Demonstrates
- Professional **software design and engineering** practices.
- Ability to refactor legacy array-based storage into scalable repositories.
- Preparation for future enhancements (algorithms, databases, front end).
- Clear, maintainable, and well-documented Java code.

## Next Steps
- Implement appointment conflict detection (Algorithms and Data Structures category).
- Connect repositories to MongoDB with Spring Data.
- Add a web-based front end for full-stack functionality.
- Upload finalized artifacts and demo videos to the ePortfolio.

## How to Run
1. Clone this repo and switch to the `Enhancement1` branch:
   ```bash
   git checkout Enhancement1
