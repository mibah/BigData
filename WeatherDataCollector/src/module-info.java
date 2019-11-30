module WeatherDataCollector {
	requires httpcore;
	requires httpclient;
	requires java.sql;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	requires gson;
	exports Correspondence to gson, com.fasterxml.jackson;
	opens Correspondence;
}