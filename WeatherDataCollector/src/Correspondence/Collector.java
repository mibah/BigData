package Correspondence;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import DataModel.City;
import DataModel.Country;
import DataModel.Europe;
import DataModel.rawCityData;

public class Collector {


	public static void main(String[] args) {
		Configuration.dataPath = args[0];
		Configuration.numberOfCalls = Integer.parseInt( args[1] );
		
		OpenWeatherInterface api = new OpenWeatherInterface();
		Helper helper = new Helper();
		System.out.println(Configuration.dataPath);
		List<String> fileList = Helper.listJsonFilesForFolder(new File(Configuration.dataPath ));
		//-1 da das erste part file leer ist.
		Country[] countries = new Country[fileList.size()-1];
		int countriesFilled = 0;
		
		for (String countryFilePath: fileList) {
			//load a country
			rawCityData rawCitiesPerCountry = helper.loadRawData(countryFilePath);
			if (rawCitiesPerCountry == null) continue; 
			//init country
			Country newCountry = new Country();
			newCountry.setName(rawCitiesPerCountry.getCountryName());
			int listLength = rawCitiesPerCountry.getCityName().length;
			String[] cityNames = rawCitiesPerCountry.getCityName();
			String[] cityIds = rawCitiesPerCountry.getCityID();
			City[] cities = new City[listLength];
			//init cities per country
			for (int i = 0; i < listLength; i++) {
				City newCity = new City();
				newCity.setName(cityNames[i]);
				newCity.setId(Integer.parseInt(cityIds[i]));
				cities[i] = newCity;
			}
			newCountry.setList(cities);
			countries[countriesFilled] = newCountry;
			countriesFilled = countriesFilled + 1;
		}
		int cityCount = 0;
		for (int i = 0; i < countries.length; i++) {
			if (countries[i].getList() == null) continue;
			City[] cities = countries[i].getList();
			cityCount = cityCount + cities.length;
		}
		System.out.println("cityCount "+cityCount);
		Europe europe = new Europe(countries);
				
		try {
			collectCurrentWeather(europe,api, Configuration.numberOfCalls);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void collectSingleCityByID(int id, OpenWeatherInterface api) throws IOException {
		
		String outFile = Configuration.westernEuropWeatherFile;
		try {
			Files.createFile(Paths.get(outFile));
			try {
				Files.write(
						Paths.get(outFile), 
						("[").getBytes(), 
						StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileAlreadyExistsException e1) {
			System.out.println("file already exists");
		}
		
				String jsonWeather = api.getCurrentWeatherByCityID(id);

				if (jsonWeather != null) {
					try {
						Files.write(
								Paths.get(outFile), 
								(jsonWeather + "\n").getBytes(), 
								StandardOpenOption.APPEND);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.err.println("no response for city");
				}
				
			}
	
	private static void collectCurrentWeather(
			Europe europe, OpenWeatherInterface api, int roundsOfCalls) throws IOException {
		
		String outFile = Configuration.westernEuropWeatherFile;

		try {
			Files.createFile(Paths.get(outFile));
			try {
				Files.write(
						Paths.get(outFile), 
						("[").getBytes(), 
						StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileAlreadyExistsException e1) {
			System.out.println("file already exists");
		}
		
		int ttlCountries = europe.getCountries().length;
		
		for (int round = 1; round <= roundsOfCalls; round++) {
			System.out.println("round: "+round);
			int processedCountries = 0;
			int sentCalls = 0;
			for (Country country : europe.getCountries()) {
				System.out.println("country: " + country.getName() + "\n");
				
				int ttlCities = country.getList().length;
				int processedCities = 0;
				
				for (City city : country.getList()) {
					int cityId = city.getId();
					System.out.println("city: " + city.getName());
					
					String jsonWeather = api.getCurrentWeatherByCityID(cityId);
					sentCalls = sentCalls + 1;
					if (sentCalls == 60) {
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						sentCalls = 0;
					}
					
					if (jsonWeather != null) {
						try {
							if(
									processedCountries == (ttlCountries -1) 
									&& processedCities == (ttlCities -1)
									&& round == roundsOfCalls
									) {
								Files.write(
										Paths.get(outFile), 
										(jsonWeather + "]").getBytes(), 
										StandardOpenOption.APPEND);
							} else {
								Files.write(
										Paths.get(outFile), 
										(jsonWeather + ",\n").getBytes(), 
										StandardOpenOption.APPEND);							
							}

						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						System.err.println("no response for city");
					}
					processedCities = processedCities + 1;
				}
				processedCountries = processedCountries + 1;
			}
		}

	}
	
}
