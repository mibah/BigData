###Introduction###
This documentation gives an overview of the application and describes how to compile and run the solution

###Compiling and Running###
First, the source code is saved in the folder /src. Since the application contains 2 seperate applications, one for data collection and one for analysis each has to be compiled and run seperately.
Each project is a separate maven project. Thus and for instance, as it was done in this project, eclipse or any other IDE can be used by using their appropriate functionality for importing maven projects. Thus, compiling and running the projects follows from using the IDE.
Second, each project is already attached to this folder as a runnable jar. The next steps described in this documentation show how to use these *.jars for the required task.

###Requirements###
The following list shows the requirements for the application that is described by this documentation.

1 Connection and data collection (+ understanding the example)  2 Create the data collection for online and historic analysis
3 Create the stream (online) metric computation implementation
4 Create the batch( historic) metric computation implementation
5 Compare the online statistics with the offline computed statistics  6 Study options for making predictions about 3 statistics and make predictions 7 Create a visualization and prepare the demo (GUI, batch, excel....)

###Application description / Specification###
The application is analyzing weather data from https://openweathermap.org/. The application uses Java, Scala and Matlab as programming languages. From this general perspective Java is used for requirement 1 – 2, scala for 3 – 6 and visualization is done in Matlab. Going more into detail this work uses Spark Structured Streaming within steps 3 – 6 as a big data compute platform. Visualization is done in Matlab. More details on each requirement are given next within a brief overview over the package and class structure of the programm.

###Package and Class Structure for the analysis tool###

#Schema
+Schemas+ describes the schemata that are necessary in order to load and process collected weather data within the application.

#Preprocessing
+CountryBuilder+ is used in a preprocessing step once for assembling a list of countries for which data was collected by means of various sql statements

#Streaming / Dataset#
Data were streamed on 5 days in a way that each day data were streamed in a different but consequent time window for 30 minutes. On the last day data was streamed for the whole period that were covered before in a partially manner. This streamed data was then used within the application to simulate a streaming data source by setting the source of the used framework to stream from a predefined folder that acts as the streaming input source. More in detail, every minute 60 calls were sent – limit of the api – and since – as described in the section Analysis – 120 cities were in the focus of the investigation every 2 minutes all relevant cities were queried.

#Analysis
+OnlineAnalysis+ contains all analytical processes that work with or on streaming data. More concrete, 120 cities from all over western Europe with a population having at least 105500 inhabitants are used for calculation. Calculating metrics was done on the aggregate of these cities along their countries. Thus, analyzed countries with relevant cities are Germany, Netherlands, Switzerland, Austria, France, Belgium, Luxembourg. Late data is handled by a sliding window of size 10 and moving rate of 5. The metrics per country are the average degree in Celsius, the maximum degree, the minimum degree and the difference between the maximum and minimum value within a given period of time. Additionally, the comparison of online and offline results was done in a way that offline results were loaded and compared to each batch in a way that for each country offline values for the selected metrics existed. Then, the difference per time window of each metric were calculated so that one is able to identify for instance the window with the biggest difference. Furthermore, for prediction each country was mapped along dimensions’ average temperature, maximum temperature, minimum temperature, pressure, humidity and cloudiness into a feature space where consequently k means was used in order to classify countries and show their similarity to each other along these selected dimensions. In order to do these a pre learned k means model was loaded and used to classify data from each batch. The country selection was done since the selected api is bound in terms of 60 api calls per minute. More details on the streaming aspect is given in the chapter streaming.

+OfflineAnalysis+ contains calculation on the whole dataset as described in the Streaming / Dataset section. For this the same metrics as in the section OnlineAnalysis are calculated (avg degree, min and max degree, difference in degree per country). These metrics are then written to disk such that the OnlineAnalysis part can fetch the data for comparison with the online stream.

+ModelTraining+ is used in order to train the selected prediction model, k means, to get trained on the whole dataset as described in the Streaming / Dataset section. The datamodel is then written to disk such that the OnlineAnalysis part can fetch that model for comparison with the online stream.

###Results###
Results are written as *.csv files to disk such that they can be reused by any other framework or tool for further analytical purposes such as visualization, clustering, etc.

###Visualization###
Visualization is done by printing the results of the online and offline analysis to disk. Afterwards any tool might be used for visualization. In this case Matlab is used since it allows for convenient postprocessing of the data and applying various other machine learning models not well suited for big data scenarios. However, since streaming is only simulated after a certain amount of time – when the stream reached its end – the results are displayed using plain scatter plots in order to plot the average degree of each country in comparison to each other. From this, it can be seen which country is the hottest or coldest in a fixed point of time. Visualization could be extended that it might be generated on demand when new data arrives.

###Package and Class Structure for the data collection tool###
#DataModel contains the definition of the entities that are streamed. 
+Collector contains the main functionality for calling accordingly to the api limit
+Configuration contains configurations
+OpenWeatherInterface generates the url for calling the api since each api call has to be unique for a specific dataset (in this case a city - note, a country is simple a set of cities)
+Helper contains auxiliary functions.

#Correspondence contains the functionality for downloading weather data from the openweatherapi (for more information on the api see https://openweathermap.org/api)

###How to use###
1. Use the CountryBuilder in order to create a list of relevant cities that you want to gather weather data for
Then create a folder where you put the folder preprocessing in.
2. Use the DataCollector by passing it two arguments. First, the output from step one and second the number of calls you want to perform for each city. Example
java -jar DataCollector.jar "/Users/localuser/Documents/flight/preprocessing/cityListPerCountry/" 2
This data is preassumed and stored in the folder preprocessing
3. Once data is collected it should be stored in the /data folder within the project folder
4. Now analysis can start by first performing offline analysis on the data by calling for example:
java -jar Analysis.jar /Users/localuser/Documents/testFolder off /Users/localuser/Documents/testFolder/data/westernEuropeWeather0.json
where the Analyisis.jar is the application, the first parameter the basepath of the project, the second indicates that offline analysis is performed and the last is the parameter to the dataset that shall be used for offline analysis.
Analogously training has to be started by calling the same *.jar file with the same parameters but instead of "off" the parameter "training" should be used:
java -jar Analysis.jar /Users/localuser/Documents/testFolder training /Users/localuser/Documents/testFolder/data/westernEuropeWeather0.json
By this a k means model is training on the training dataset. This model is then used by the online analysis part. Then, in the last step the same *.jar file should be called with the same parameters but the keyword "on" should be used, still passing the same parameters for the basepath and the trained model:
java -jar Analysis.jar /Users/localuser/Documents/testFolder on /Users/localuser/Documents/testFolder/data/westernEuropeWeather0.json
5. All results from offline and online analysis can be found in the subfolder /results/collection as *.csv files.
6. Visualization can be now done with the data from step 5. But before /results/comprise.sh has to be run. This is just comprising the results produced by spark in a nice format so that there exist one *.csv file per query that contains the results.
Images for avg, max and min temperature for instance can be created by using the plotMe.m MATLAB function in the way the example is showing it:
AT = readtable('/Users/localuser/Documents/flight/results/collection/onlineAnalysisResults/AT/summary.csv');
BE = readtable('/Users/localuser/Documents/flight/results/collection/onlineAnalysisResults/BE/summary.csv');
CH = readtable('/Users/localuser/Documents/flight/results/collection/onlineAnalysisResults/CH/summary.csv');
DE = readtable('/Users/localuser/Documents/flight/results/collection/onlineAnalysisResults/DE/summary.csv');
FR = readtable('/Users/localuser/Documents/flight/results/collection/onlineAnalysisResults/FR/summary.csv');
LU = readtable('/Users/localuser/Documents/flight/results/collection/onlineAnalysisResults/LU/summary.csv');
NL = readtable('/Users/localuser/Documents/flight/results/collection/onlineAnalysisResults/NL/summary.csv');
>> plotMe(AT)

Where the example loads data for all countries that have been summarized before be means of the comprise.sh file. Consequently, a scatter plot is created by the function plotMe. The folder /img shows the scatter plots for the avg temperatur in celsius for each country for the period of time the that is spanned by the smallest and larges timestamp of the data. The concrete timestamps are mapped to a linear sequence of integers and can be seen in the summary file in a plain format.

HINT: The image in the repo shows the filestructure of the project folder
