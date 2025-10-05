// person class (Parent class)
class Person {
	protected String firstName;
	protected String lastName;
	protected String address;
	// constructor
	public Person(String firstName, String lastName, String address) {
	this.firstName = firstName;
	this.lastName = lastName;
	this.address = address;
}
	
		// Getters
			
		public String getFirstName() {
			return firstName;
		}
		public String getLastName() {
			return lastName;
		}
		public String getAddress() {
			return address;
		}
		// Setters 
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public void setLastName (String lastName) {
			this.lastName = lastName;
		}
		public void setAddress (String address ) {
			this.address = address;
		}
	

	}


