package DataModel;

public class Europe {

	private final Country[] countries;
	
	public Europe(Country[] countries) {
		this.countries = countries;
	}
	
	/**
	 * @return the countries
	 */
	public Country[] getCountries() {
		return countries;
	}
}
