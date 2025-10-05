import java.util.Scanner;
public class Clinic {

	public static void main(String[] args) {
		// Create instance of doctors and employees

	}
	Scanner scanner = new Scanner(System.in);

    // create array
	Doctor [] doctors = new Doctor [3]; // 3 Doctors
	Patient [] patients = new Patient [100]; // Assuming a maximum of 100 patients
	Employee [] employees = new Employee [2]; // 2 Employees
	int doctorCount = 0;
	int patientCount = 0;{
	

	// Create array objects
	
    doctors[0] = new Doctor("John", "Doe", "123 Main St", "Obstetrician", 500.0, 5000, 100.0);
    doctors[1] = new Doctor("Jane", "Smith", "456 Elm St", "Pediatrician", 600.0, 6000, 150.0);
    doctors[2] = new Doctor("Bob", "Johnson", "789 Oak St", "General Practitioner", 700.0, 7000, 200.0);
    
    
    employees[0] = new Employee("Mike", "Jones", "789 Oak St", 1400, 5000.0);
    employees[1] = new Employee("Sarah", "Williams", "456 Elm St", 2350, 5500.0);
	
    
    // user prompt continously
    boolean continueInput = true;
    while(continueInput) {
    	System.out.print("Enter patient's last name:");
    	String lastName = scanner.next();
    	boolean isNewPatient = true;
    	int existingPatientIndex = 0;
    	
    	// Check is the patient is new 
    	for(int i=0; i < patientCount; i++) {
    		if (patients[i].getLastName().equals(lastName)) {
    			isNewPatient = false;
    			existingPatientIndex = i;
    			break;
    		}
    	}
    
    	
    	if (isNewPatient) {
    		System.out.print("Enter patient's first name:");
    				String firstName = scanner.next();
    				System.out.print("Enter patient's address");
    				String address = scanner.next();
    				System.out.print(" Primary care Physician:");
    				String pcp = scanner.next();
    				
    		patients[patientCount] = new Patient(firstName, lastName, address, pcp); 
    		System.out.println("Patient found in records");
    	}
    	// Prompt the user to continue entering patients
    	System.out.print("Do you have more patients ? (yes/no)");
    	String response = scanner.next();
    	if (response.equalsIgnoreCase("no")){
    		continueInput = false;
    	}
    }

// Perform visit and calculate 

	for (int i=0; i < patientCount; i++) {
		Patient currentPatient = patients[i]; 
		Doctor primaryCarePhysician = null;
			
		// Find the patients pcp
		for (int j = 0; j < doctorCount; j++) {
			if (doctors[j].getLastName().equals(currentPatient.getPrimaryCarePhysician())){
				
				primaryCarePhysician = doctors[j];
				break;
			}
		}
	

		// Check if the patient wants to visit their PCP or a different doctor
		if(currentPatient.getLastVisitedDoctor().equals(currentPatient.getPrimaryCarePhysician())) {
			if (primaryCarePhysician != null) {
				primaryCarePhysician.visit();
			}
		}else {
			Doctor specialistDoctor = null;
			for(int j= 0; j < doctorCount; j++) {
				if(doctors[j].getLastName().equals(currentPatient.getLastVisitedDoctor().split("")[1])) {
					specialistDoctor = doctors[j];
					break;
				}
			}
		}
		// print paychecks 
		for (int d = 0; d < doctorCount; d++) {
			Doctor currentDoctor = doctors[i];
				  System.out.println("Doctor Information:" + currentDoctor.firstName + "" + currentDoctor.lastName);
    			  System.out.println("Speciality: " + currentDoctor.getSpeciality());
    			  System.out.println("Office Visit Fee: $" + currentDoctor.getOfficeVisitFee());
    			  System.out.println("Number of Visited Patients:" + currentDoctor.getnumVisitedPatients());
    			  System.out.println("Paycheck: $" + currentDoctor.calculatePaycheck());
    			  
    			 Employee currentEmployee = employees[i];
    			  System.out.println("-----------------");
    			  System.out.println("Employee Information:" + currentEmployee.firstName + "" +currentEmployee.lastName);
    			  System.out.println("Salary Rate:" + currentEmployee.baseSalary);
    			  System.out.println("Hours:" + currentEmployee.monthlyWorkingHours);
    			  System.out.println("Paycheck: $" + currentEmployee.calculatePaycheck());
    			}
		}
	scanner.close();
	}
	}




