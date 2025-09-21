/**
 * Name: Valerie Dawson
 * Enhancement (CS-499, Software Design/Engineering):
 * - Encapsulation + validation
 * - visit() tracking + paycheck from visits
 * - Legacy names/ctor kept to avoid breaking Clinic.java
 */
public class Doctor extends Person {

    private String speciality;       // kept legacy name used by Clinic
    private double officeVisitFee;
    private int numVisitedPatients;

    // legacy payroll fields preserved (not used in paycheck)
    private int monthlyWorkingHours;
    private double baseSalary;

    public Doctor(String firstName, String lastName, String address,
                  String speciality, double officeVisitFee,
                  int monthlyWorkingHours, double baseSalary) {
        super(firstName, lastName, address);
        this.speciality = ValidationUtils.requireNonBlank(speciality, "speciality");
        this.officeVisitFee = ValidationUtils.requireNonNegative(officeVisitFee, "officeVisitFee");
        this.monthlyWorkingHours = ValidationUtils.requireNonNegative(monthlyWorkingHours, "monthlyWorkingHours");
        this.baseSalary = ValidationUtils.requireNonNegative(baseSalary, "baseSalary");
        this.numVisitedPatients = 0;
    }

    /** Increment when a visit completes (used by intake or Appointment.complete()). */
    public void visit() { this.numVisitedPatients++; }

    /** Simple pay rule: visits × fee (clear for portfolio/grading). */
    public double calculatePaycheck() { return officeVisitFee * numVisitedPatients; }

    // getters (legacy names preserved)
    public String getSpeciality() { return speciality; }
    public double getOfficeVisitFee() { return officeVisitFee; }
    public int getnumVisitedPatients() { return numVisitedPatients; }
    public int getMonthlyWorkingHours() { return monthlyWorkingHours; }
    public double getBaseSalary() { return baseSalary; }

    // setters with validation
    public void setSpeciality(String speciality) { this.speciality = ValidationUtils.requireNonBlank(speciality, "speciality"); }
    public void setOfficeVisitFee(double fee)    { this.officeVisitFee = ValidationUtils.requireNonNegative(fee, "officeVisitFee"); }
    public void setnumVisitedPatients(int c)     { this.numVisitedPatients = ValidationUtils.requireNonNegative(c, "numVisitedPatients"); }
    public void setMonthlyWorkingHours(int h)    { this.monthlyWorkingHours = ValidationUtils.requireNonNegative(h, "monthlyWorkingHours"); }
    public void setBaseSalary(double s)          { this.baseSalary = ValidationUtils.requireNonNegative(s, "baseSalary"); }

    @Override public String toString() {
        return "Doctor{" +
               "uniqueId='" + getUniqueId() + '\'' +
               ", name='" + getFirstName() + " " + getLastName() + '\'' +
               ", speciality='" + speciality + '\'' +
               ", fee=" + officeVisitFee +
               ", visited=" + numVisitedPatients +
               '}';
    }
}



