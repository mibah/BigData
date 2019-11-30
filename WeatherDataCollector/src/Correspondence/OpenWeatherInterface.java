package Correspondence;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class OpenWeatherInterface {
	
	private static final String API_URL = "http://api.openweathermap.org";
	private static final String VERSION = "2.5";
	private static final String PREFIX = "data";
	private static final String CURRENT = "weather";
	private static final String APPID_VALUE = "20E4A9D2683AF0D314233138B30920B7";
	
	private static final String URL_SEPARATOR = "/";
	private static final String ARGS_SEPARATOR = "&";
	private static final String KEYVALUE_SEPARATOR = "=";
	private static final String QUERY_SEPARATOR = "?";
	
	private static final String APPID = "appid";
	private static final String UNITS = "units";
	private static final String UNITS_TYPE = "metric";
	
	private final HttpClient httpClient;
	
	public OpenWeatherInterface() {
		this.httpClient = HttpClients.createDefault();
	}
	
	private static final String assembleEndpoint(String query) {
		StringBuilder apiCall = new StringBuilder();
		apiCall.append(API_URL);
		apiCall.append(URL_SEPARATOR);
		apiCall.append(PREFIX);
		apiCall.append(URL_SEPARATOR);
		apiCall.append(VERSION);
		apiCall.append(URL_SEPARATOR);
		apiCall.append(CURRENT);
		apiCall.append(QUERY_SEPARATOR);
		apiCall.append(APPID);
		apiCall.append(KEYVALUE_SEPARATOR);
		apiCall.append(APPID_VALUE);
		apiCall.append(ARGS_SEPARATOR);
		apiCall.append(UNITS);
		apiCall.append(KEYVALUE_SEPARATOR);
		apiCall.append(UNITS_TYPE);
		apiCall.append(ARGS_SEPARATOR);
		apiCall.append(query);
		return apiCall.toString();
	}
	
	private static final String assembleCurrentWeatherEndpoint(
			String query) {
		return assembleEndpoint(query);
	}
	
	private static String assembleQueryByCityID(int cityID) {
		StringBuilder templateBuilder = new StringBuilder();
		templateBuilder.append("id=" + cityID);		
		String query = templateBuilder.toString();
		return query;
	}


	public String getCurrentWeatherEndpointByCityID(int cityId) {
		String query = assembleQueryByCityID(cityId);
		return assembleCurrentWeatherEndpoint(query);
	}
	
	public String getCurrentWeatherByCityID(int cityId) {
		String endpoint = getCurrentWeatherEndpointByCityID(cityId);
		return executeHttpGet(endpoint);
	}
	
	private String executeHttpGet(String endpoint) {
		HttpGet httpGet = new HttpGet(endpoint);
		String content = null;
		
		try {
			CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet);
			
			try {
				HttpEntity entity = response.getEntity();
				content = EntityUtils.toString(entity);
				EntityUtils.consume(entity);
			} finally {
				response.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}
	
}
