package DataModel;

public class Country {

	private String name;
	private City[] list;
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the list
	 */
	public City[] getList() {
		return list;
	}
	/**
	 * @param list the list to set
	 */
	public void setList(City[] list) {
		this.list = list;
	}
}
