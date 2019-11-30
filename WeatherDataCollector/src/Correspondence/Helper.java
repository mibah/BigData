package Correspondence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import DataModel.Value;
import DataModel.rawCityData;


public class Helper {

	public static List<String> listJsonFilesForFolder(final File folder) {
		List<String> list = new ArrayList<String>();
		
	    for (final File fileEntry : folder.listFiles()) {
	        if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".json")) {
	        	list.add( fileEntry.getName().trim() );
	        	System.out.println("added: "+fileEntry.getName());
	        }
	    }
	    return list;
	}
	
	
	public static rawCityData loadRawData1(String fileName) {
		Gson gson = new Gson();
		
		
		String fileHandler = Configuration.dataPath+fileName;
		Value in = null;
		rawCityData rawData = null;
		try {
			FileReader file = new FileReader(fileHandler);
			in = gson.fromJson(file, Value.class);
			rawData = gson.fromJson(in.getValue(), rawCityData.class);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rawData;
	}
	
	public static boolean fileEmpty(File file) {
		if (file.length() == 0) {
		      System.out.println("file empty");
		      return true;
		    } else {
		      System.out.println("file not empty");
		      return false;
		    }
	}
	public static rawCityData loadRawData(String fileName){
		
		String fileHandler = Configuration.dataPath+fileName;
		System.out.println("load country from: "+fileHandler);
		ObjectMapper objectMapper = new ObjectMapper();

		//objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		
		//prüft ob eines der files leer ist.
		File fileCodes = new File(fileHandler);
		if(fileEmpty(fileCodes)==true) { 
			return null;
		}
		
		//File fileCities = new File("/Users/localuser/Documents/flight/preprocessing/city.list2.json");
		//List<countryCodes> myObjects = null;
		Value in = null;
		rawCityData rawData = null;
		try {
			//myObjects = Arrays.asList(objectMapper.readValue(fileCodes, countryCodes[].class));
			in = objectMapper.readValue(fileCodes, Value.class);
			rawData = objectMapper.readValue(in.getValue(), rawCityData.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rawData;
				
	}
}
