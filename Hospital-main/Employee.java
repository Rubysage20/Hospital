/**
 * Name: Valerie Dawson
 * Enhancement (CS-499, Software Design/Engineering):
 * - Encapsulation + validation
 * - Paycheck = hours × hourly rate (baseSalary treated as hourly)
 * - Guard against absurd hours (0–400)
 */
public class Employee extends Person {

    private int monthlyWorkingHours;   // realistic: 0–400
    private double baseSalary;         // hourly rate

    public Employee(String firstName, String lastName, String address,
                    int monthlyWorkingHours, double baseSalary) {
        super(firstName, lastName, address);
        setMonthlyWorkingHours(monthlyWorkingHours);
        this.baseSalary = ValidationUtils.requireNonNegative(baseSalary, "baseSalary");
    }
    
 // Rehydrate
    public Employee(String id, String first, String last, String addr,
                    int monthlyHours, double hourlyRate) {
        super(id, first, last, addr);
        this.monthlyWorkingHours = monthlyHours;
        this.baseSalary = hourlyRate;
    }

    // getters
    public int getMonthlyWorkingHours() { return monthlyWorkingHours; }
    public double getBaseSalary() { return baseSalary; }

    // setters with validation
    public void setMonthlyWorkingHours(int hours) {
        if (hours < 0 || hours > 400) {
            throw new IllegalArgumentException("monthlyWorkingHours out of reasonable range (0–400)");
        }
        this.monthlyWorkingHours = hours;
    }
    public void setBaseSalary(double base) {
        this.baseSalary = ValidationUtils.requireNonNegative(base, "baseSalary");
    }

    /** Hourly × Hours. */
    public double calculatePaycheck() { return monthlyWorkingHours * baseSalary; }

    @Override public String toString() {
        return "Employee{" +
               "uniqueId='" + getUniqueId() + '\'' +
               ", name='" + getFirstName() + " " + getLastName() + '\'' +
               ", hours=" + monthlyWorkingHours +
               ", hourlyRate=" + baseSalary +
               ", paycheck=" + calculatePaycheck() +
               '}';
    }
}
