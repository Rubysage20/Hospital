// Employee class (Child class of Person)
public class Employee extends Person {
		 double monthlyWorkingHours;
		 double baseSalary;
		 
		 public Employee(String firstName, String lastName, String address, double monthlyWorkingHours, double baseSalary){
			 super(firstName, lastName, address);
	          this.monthlyWorkingHours = monthlyWorkingHours;
	          this.baseSalary = baseSalary;
		}
		
		// Calculate employee paycheck
			public double calculatePaycheck() {
				if (monthlyWorkingHours > 165) {
					double overtimeHours = monthlyWorkingHours - 165;
					double overtimePay = (baseSalary / 165)* 1.5 * overtimeHours;
					return  baseSalary + overtimePay;
		    	} 
		    		return baseSalary;
		    	}	
			
		// getters
		public double getMonthlyWorkingHours() 
		{
			return monthlyWorkingHours;
		}
		public double getBaseSalary() {
			return baseSalary;
		}
		// setters
		public void setMonthlyWorkingHours (int monthlyWorkingHours) {
			this.monthlyWorkingHours = monthlyWorkingHours;
		}
		public void setBasesalary (double baseSalary) {
			this.baseSalary = baseSalary;
	}

	

}
