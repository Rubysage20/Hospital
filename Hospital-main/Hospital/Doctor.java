// Doctor class ( child class of person)
class Doctor extends Person {

			private String speciality;
			private double officeVisitFee;
			private int numVisitedPatients;
			
			public Doctor(String firstName, String lastName, String address, String speciality, double officeVisitFee, int monthlyWorkingHours, double BaseSalary) {
				super(firstName, lastName, address);
				this.speciality = speciality;
				this.officeVisitFee = officeVisitFee;
				this.numVisitedPatients = 0;
			}
			// method to add patients and increment number of visited patients 
			public void visit() {
				numVisitedPatients++;
			}
			// calculate doctor's paycheck
			public double calculatePaycheck() {
				return numVisitedPatients * officeVisitFee;
			}
			
			// getters
			public int getnumVisitedPatients () {
				return numVisitedPatients;
			}
			public double getOfficeVisitFee () {
				return officeVisitFee;
			}
			public String getSpeciality() {
				return speciality;
			}
			
			// setters 
			public void setnumVisitedPatients(int numVisitedPatients) {
				this.numVisitedPatients = numVisitedPatients;
			}
			public void setOfficeVisitFee(double officeVisitFee) {
				this.officeVisitFee = officeVisitFee;
			}
			public void setSpeciality(String speciality){
				this.speciality = speciality;
			}
			

	}


