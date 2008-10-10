package airldm2.core;


	/**
	 * 
	 * @author Matthew Miller
	 * 
	 * In this implementation of Sufficient Statistics they will be represented by 
	 * a number - in this case a double value.  
	 *
	 */
	public interface ISufficentStatistic {
		
		/**
		 * 
		 * @param value = the Value of the statistic.
		 */
		public void setValue(Double value);
		
		
		/**
		 * 
		 * @return the value of the statistic in string form.
		 */
		public Double getValue();

	}

