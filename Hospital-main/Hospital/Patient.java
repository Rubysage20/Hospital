// patient class ( child class of person)
public class Patient extends Person {

	private String primaryCarePhysician;
	private String lastVisitedDoctor;
	
	// Constructor
	public Patient (String firstName, String lastName, String address,String primaryCarePhysician) {
		super(firstName, lastName, address);
		this.primaryCarePhysician = primaryCarePhysician;
		this.lastVisitedDoctor ="";
	}
	// method to set the value of lastVisiteddoctor variable
	
	public void visit(String doctor) {
		lastVisitedDoctor = firstName;
		}
	// getters
	public String getPrimaryCarePhysician() {
		return primaryCarePhysician;
	}
	public String getLastVisitedDoctor () {
		return lastVisitedDoctor;
	}
	}


