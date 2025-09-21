/**
 * Name: Valerie Dawson
 *
 * Clinic (Console Runner)
 * -----------------------
 * Enhancement (CS-499, Software Design/Engineering):
 * - Replaced arrays with repository interfaces (in-memory impls now; Mongo later).
 * - Added CRUD menus for Doctors, Patients, and Employees.
 * - Clean, commented I/O helpers and currency formatting.
 * - Storage-agnostic design: swap repo implementations without touching menu logic.
 */

import java.util.Scanner;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
public class Clinic {

    /** Single Scanner instance for the console app. */
    private static final Scanner scanner = new Scanner(System.in);

    // Repositories (swap these for Mongo-backed implementations later).
    private static final DoctorRepository doctorRepo     = new InMemoryDoctorRepository();
    private static final PatientRepository patientRepo   = new InMemoryPatientRepository();
    private static final EmployeeRepository employeeRepo = new InMemoryEmployeeRepository();

    public static void main(String[] args) {

        // ---- Seed Doctors (same names as earlier) ----
        doctorRepo.save(new Doctor("John", "Doe", "123 Main St", "Obstetrician", 500.0, 0, 0.0));
        doctorRepo.save(new Doctor("Jane", "Smith", "456 Elm St", "Pediatrician", 600.0, 0, 0.0));
        doctorRepo.save(new Doctor("Bob",  "Johnson","789 Oak St", "General Practitioner", 700.0, 0, 0.0));

        // ---- Seed Employees (realistic hourly) ----
        employeeRepo.save(new Employee("Mike",  "Jones",   "789 Oak St", 160, 35.00));
        employeeRepo.save(new Employee("Sarah", "Williams","456 Elm St", 168, 42.00));

        // ---- Optional Patient Intake (same behavior as before, repo-backed) ----
        intakePatientsAndApplyVisits();

        // ---- Main CRUD Menu Loop ----
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Select an option: ").trim();
            System.out.println();

            try {
                switch (choice) {
                    // Doctors
                    case "1": createDoctor(); break;
                    case "2": listDoctors(); break;
                    case "3": updateDoctor(); break;
                    case "4": deleteDoctor(); break;

                    // Patients
                    case "5": createPatient(); break;
                    case "6": listPatients(); break;
                    case "7": updatePatient(); break;
                    case "8": deletePatient(); break;

                    // Employees
                    case "9":  createEmployee(); break;
                    case "10": listEmployees(); break;
                    case "11": updateEmployee(); break;
                    case "12": deleteEmployee(); break;

                    // Summaries / Exit
                    case "13": showPayrollSummary(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception ex) {
                System.out.println("⚠️  " + ex.getMessage());
            }
            System.out.println();
        }

        scanner.close();
        System.out.println("Goodbye!");
    }

    // =========================================================================================
    // Patient intake (parity with your earlier flow) + visit logic
    // =========================================================================================
    private static void intakePatientsAndApplyVisits() {
        System.out.println();
        System.out.println("=== Patient Intake (press Enter to skip) ===");
        String maybeSkip = readLine("Press Enter to begin or type 'skip' to go to menu: ").trim();
        if (!maybeSkip.equalsIgnoreCase("skip")) {
            boolean continueInput = true;
            while (continueInput) {
                String lastName = readLine("Enter patient's last name: ").trim();
                if (lastName.isEmpty()) {
                    System.out.println("Skipping empty entry...");
                    continue;
                }

                boolean exists = patientRepo.findByLastName(lastName).isPresent();
                if (!exists) {
                    String firstName = readLine("Enter patient's first name: ").trim();
                    String address   = readLine("Enter patient's address: ").trim();
                    String pcpLast   = readLine("Primary care physician last name: ").trim();

                    Patient p = new Patient(firstName, lastName, address, pcpLast);
                    patientRepo.save(p);
                    System.out.println("✅ New patient added.");
                } else {
                    System.out.println("ℹ️  Patient found in records.");
                }

                String response = readLine("Do you have more patients? (yes/no): ").trim();
                if (response.equalsIgnoreCase("no")) continueInput = false;
            }

            // Apply the same visit logic as your original version (PCP or lastVisited specialist)
            for (Patient currentPatient : patientRepo.findAll()) {
                Doctor pcp = doctorRepo.findByLastName(currentPatient.getPrimaryCarePhysician()).orElse(null);
                String lastVisited = currentPatient.getLastVisitedDoctor();

                if (lastVisited == null || lastVisited.isEmpty()
                        || lastVisited.equalsIgnoreCase(currentPatient.getPrimaryCarePhysician())) {
                    if (pcp != null) pcp.visit();
                } else {
                    Doctor specialist = doctorRepo.findByLastName(lastVisited).orElse(null);
                    if (specialist != null) specialist.visit();
                }
            }

            System.out.println();
            System.out.println("=== Intake complete ===");
            listDoctors(); // quick feedback
            System.out.println();
        }
    }

    // =========================================================================================
    // DOCTOR CRUD
    // =========================================================================================
    private static void createDoctor() {
        System.out.println("=== Create Doctor ===");
        String first = readLine("First Name: ");
        String last  = readLine("Last Name: ");
        String addr  = readLine("Address: ");
        String spec  = readLine("Specialty: ");
        double fee   = readDouble("Office Visit Fee: ");

        Doctor d = new Doctor(first, last, addr, spec, fee, 0, 0.0);
        doctorRepo.save(d);

        System.out.println("✅ Created doctor with ID: " + d.getUniqueId());
        System.out.println();
        listDoctors();
    }

    private static void listDoctors() {
        System.out.println("=== Doctors ===");
        var list = doctorRepo.findAll();
        if (list.isEmpty()) { System.out.println("(none)"); return; }
        for (Doctor doc : list) {
            System.out.println("---------------");
            System.out.println("ID: " + doc.getUniqueId());
            System.out.println("Name: " + safe(doc.getFirstName()) + " " + safe(doc.getLastName()));
            System.out.println("Specialty: " + doc.getSpeciality());
            System.out.println("Office Visit Fee: " + money(doc.getOfficeVisitFee()));
            System.out.println("Visited Patients: " + doc.getnumVisitedPatients());
            System.out.println("Paycheck: " + money(doc.calculatePaycheck()));
        }
    }

    private static void updateDoctor() {
        System.out.println("=== Update Doctor ===");
        String id = readLine("Enter Doctor ID to update: ").trim();
        Optional<Doctor> opt = doctorRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("Doctor not found."); return; }

        Doctor d = opt.get();
        System.out.println("Editing: " + d.getFirstName() + " " + d.getLastName() + " (" + d.getSpeciality() + ")");

        String first = readLine("First Name [" + d.getFirstName() + "]: ").trim();
        String last  = readLine("Last Name  [" + d.getLastName()  + "]: ").trim();
        String addr  = readLine("Address    [" + d.getAddress()   + "]: ").trim();
        String spec  = readLine("Specialty  [" + d.getSpeciality()+ "]: ").trim();
        String feeS  = readLine("Office Visit Fee [" + money(d.getOfficeVisitFee()) + "]: ").trim();

        if (!first.isEmpty()) d.setFirstName(first);
        if (!last.isEmpty())  d.setLastName(last);
        if (!addr.isEmpty())  d.setAddress(addr);
        if (!spec.isEmpty())  d.setSpeciality(spec);
        if (!feeS.isEmpty())  d.setOfficeVisitFee(Double.parseDouble(feeS));

        doctorRepo.save(d);
        System.out.println("✅ Doctor updated.");
        System.out.println();
        listDoctors();
    }

    private static void deleteDoctor() {
        System.out.println("=== Delete Doctor ===");
        String id = readLine("Enter Doctor ID to delete: ").trim();
        Optional<Doctor> opt = doctorRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("Doctor not found."); return; }

        Doctor target = opt.get();
        doctorRepo.deleteById(id);
        System.out.println("🗑️  Deleted doctor: " + target.getFirstName() + " " + target.getLastName());

        System.out.println();
        listDoctors();
    }

    // =========================================================================================
    // PATIENT CRUD
    // =========================================================================================
    private static void createPatient() {
        System.out.println("=== Create Patient ===");
        String first = readLine("First Name: ");
        String last  = readLine("Last Name: ");
        String addr  = readLine("Address: ");
        String pcp   = readLine("PCP Last Name: ");

        Patient p = new Patient(first, last, addr, pcp);
        patientRepo.save(p);

        System.out.println("✅ Created patient with ID: " + p.getUniqueId());
        System.out.println();
        listPatients();
    }

    private static void listPatients() {
        System.out.println("=== Patients ===");
        var list = patientRepo.findAll();
        if (list.isEmpty()) { System.out.println("(none)"); return; }
        for (Patient p : list) {
            System.out.println("---------------");
            System.out.println("ID: " + p.getUniqueId());
            System.out.println("Name: " + p.getFirstName() + " " + p.getLastName());
            System.out.println("Address: " + p.getAddress());
            System.out.println("PCP Last Name: " + safe(p.getPrimaryCarePhysician()));
            System.out.println("Last Visited Doctor: " + safe(p.getLastVisitedDoctor()));
            System.out.println("Last Visited At: " + (p.getLastVisitedAt() == null ? "N/A" : p.getLastVisitedAt().toString()));
        }
    }

    private static void updatePatient() {
        System.out.println("=== Update Patient ===");
        String id = readLine("Enter Patient ID to update: ").trim();
        Optional<Patient> opt = patientRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("Patient not found."); return; }

        Patient p = opt.get();
        System.out.println("Editing: " + p.getFirstName() + " " + p.getLastName());

        String first = readLine("First Name [" + p.getFirstName() + "]: ").trim();
        String last  = readLine("Last Name  [" + p.getLastName()  + "]: ").trim();
        String addr  = readLine("Address    [" + p.getAddress()   + "]: ").trim();
        String pcp   = readLine("PCP Last Name [" + p.getPrimaryCarePhysician() + "]: ").trim();

        if (!first.isEmpty()) p.setFirstName(first);
        if (!last.isEmpty())  p.setLastName(last);
        if (!addr.isEmpty())  p.setAddress(addr);
        if (!pcp.isEmpty())   p.setPrimaryCarePhysician(pcp);

        patientRepo.save(p);
        System.out.println("✅ Patient updated.");
        System.out.println();
        listPatients();
    }

    private static void deletePatient() {
        System.out.println("=== Delete Patient ===");
        String id = readLine("Enter Patient ID to delete: ").trim();
        Optional<Patient> opt = patientRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("Patient not found."); return; }

        Patient target = opt.get();
        patientRepo.deleteById(id);
        System.out.println("🗑️  Deleted patient: " + target.getFirstName() + " " + target.getLastName());

        System.out.println();
        listPatients();
    }

    // =========================================================================================
    // EMPLOYEE CRUD
    // =========================================================================================
    private static void createEmployee() {
        System.out.println("=== Create Employee ===");
        String first = readLine("First Name: ");
        String last  = readLine("Last Name: ");
        String addr  = readLine("Address: ");
        int hours    = readInt("Monthly Working Hours (0–400): ", 0, 400);
        double rate  = readDouble("Hourly Rate: ");

        Employee e = new Employee(first, last, addr, hours, rate);
        employeeRepo.save(e);

        System.out.println("✅ Created employee with ID: " + e.getUniqueId());
        System.out.println();
        listEmployees();
    }

    private static void listEmployees() {
        System.out.println("=== Employees ===");
        var list = employeeRepo.findAll();
        if (list.isEmpty()) { System.out.println("(none)"); return; }
        for (Employee emp : list) {
            System.out.println("---------------");
            System.out.println("ID: " + emp.getUniqueId());
            System.out.println("Name: " + emp.getFirstName() + " " + emp.getLastName());
            System.out.println("Address: " + emp.getAddress());
            System.out.println("Hours: " + emp.getMonthlyWorkingHours());
            System.out.println("Hourly Rate: " + money(emp.getBaseSalary()));
            System.out.println("Paycheck: " + money(emp.calculatePaycheck()));
        }
    }

    private static void updateEmployee() {
        System.out.println("=== Update Employee ===");
        String id = readLine("Enter Employee ID to update: ").trim();
        Optional<Employee> opt = employeeRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("Employee not found."); return; }

        Employee e = opt.get();
        System.out.println("Editing: " + e.getFirstName() + " " + e.getLastName());

        String first = readLine("First Name [" + e.getFirstName() + "]: ").trim();
        String last  = readLine("Last Name  [" + e.getLastName()  + "]: ").trim();
        String addr  = readLine("Address    [" + e.getAddress()   + "]: ").trim();
        String hours = readLine("Monthly Working Hours [" + e.getMonthlyWorkingHours() + "]: ").trim();
        String rateS = readLine("Hourly Rate [" + money(e.getBaseSalary()) + "]: ").trim();

        if (!first.isEmpty()) e.setFirstName(first);
        if (!last.isEmpty())  e.setLastName(last);
        if (!addr.isEmpty())  e.setAddress(addr);
        if (!hours.isEmpty()) e.setMonthlyWorkingHours(Integer.parseInt(hours));
        if (!rateS.isEmpty()) e.setBaseSalary(Double.parseDouble(rateS));

        employeeRepo.save(e);
        System.out.println("✅ Employee updated.");
        System.out.println();
        listEmployees();
    }

    private static void deleteEmployee() {
        System.out.println("=== Delete Employee ===");
        String id = readLine("Enter Employee ID to delete: ").trim();
        Optional<Employee> opt = employeeRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("Employee not found."); return; }

        Employee target = opt.get();
        employeeRepo.deleteById(id);
        System.out.println("🗑️  Deleted employee: " + target.getFirstName() + " " + target.getLastName());

        System.out.println();
        listEmployees();
    }

    // =========================================================================================
    // Summaries
    // =========================================================================================
    private static void showPayrollSummary() {
        System.out.println("=== Payroll Summary ===");
        System.out.println("-- Doctors --");
        double totalDoc = 0.0;
        for (Doctor doc : doctorRepo.findAll()) {
            double pay = doc.calculatePaycheck();
            totalDoc += pay;
            System.out.printf("%s %s: visits=%d, pay=%s%n",
                    safe(doc.getFirstName()), safe(doc.getLastName()),
                    doc.getnumVisitedPatients(), money(pay));
        }

        System.out.println("-- Employees --");
        double totalEmp = 0.0;
        for (Employee emp : employeeRepo.findAll()) {
            double pay = emp.calculatePaycheck();
            totalEmp += pay;
            System.out.printf("%s %s: hours=%d, rate=%s, pay=%s%n",
                    safe(emp.getFirstName()), safe(emp.getLastName()),
                    emp.getMonthlyWorkingHours(), money(emp.getBaseSalary()), money(pay));
        }

        System.out.println("-------------------------");
        System.out.println("TOTAL Doctor Payroll:   " + money(totalDoc));
        System.out.println("TOTAL Employee Payroll: " + money(totalEmp));
        System.out.println("GRAND TOTAL:            " + money(totalDoc + totalEmp));
    }

    // =========================================================================================
    // I/O helpers
    // =========================================================================================
    private static void printMenu() {
        System.out.println("====================================");
        System.out.println("            CLINIC  MENU            ");
        System.out.println("====================================");
        System.out.println("1)  Create Doctor");
        System.out.println("2)  List Doctors");
        System.out.println("3)  Update Doctor");
        System.out.println("4)  Delete Doctor");
        System.out.println("5)  Create Patient");
        System.out.println("6)  List Patients");
        System.out.println("7)  Update Patient");
        System.out.println("8)  Delete Patient");
        System.out.println("9)  Create Employee");
        System.out.println("10) List Employees");
        System.out.println("11) Update Employee");
        System.out.println("12) Delete Employee");
        System.out.println("13) Show Payroll Summary");
        System.out.println("0)  Exit");
        System.out.println("====================================");
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                String s = readLine(prompt).trim();
                return Double.parseDouble(s);
            } catch (NumberFormatException nfe) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                String s = readLine(prompt).trim();
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    System.out.printf("Please enter a value between %d and %d.%n", min, max);
                    continue;
                }
                return v;
            } catch (NumberFormatException nfe) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static String money(double v) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(v);
    }

    private static String safe(String s) { return s == null ? "" : s; }
}

