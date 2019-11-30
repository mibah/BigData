package Analysis

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType, FloatType, ArrayType, BooleanType, LongType, DoubleType}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row
import org.apache.spark.sql._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.PipelineModel
import Schema.Schemas
import StartApplication.Configurations
class OnlineAnalysis {

    
  def start() {
  
   val spark = SparkSession.builder.appName("Spark_Streaming").master("local[2]").getOrCreate();
   spark.sparkContext.setLogLevel("ERROR")
   spark.conf.set("spark.sql.streaming.checkpointLocation", Configurations.checkpointingPath)
    // this is used to implicitly convert an RDD to a DataFrame.
  import spark.implicits._

  //import schemas for data loading
  val schema = new Schemas()    
  
  //configure the input stream
  val weatherStream = spark
  .readStream
  .option("maxFilesPerTrigger", 1)
  .format("json")
  .schema(schema.schema)
  .json(Configurations.streamingSourcePath);
  
  //select the main data frame
  val generalData = weatherStream.select($"*").as("generalDF")
  .withColumn("longitude", $"generalDF.coord.lon")
  .withColumn("latitude", $"generalDF.coord.lat")
  .withColumn("weather_condition_id", $"generalDF.weather"(0).getField("id"))
  .withColumn("weather_group", $"generalDF.weather"(0).getField("main"))
  .withColumn("weather_description", $"generalDF.weather"(0).getField("description"))
  .withColumn("weather_icon", $"generalDF.weather"(0).getField("icon"))
  .withColumn("general_base", $"generalDF.base")
  .withColumn("main_temp", $"generalDF.main.temp")
  .withColumn("main_pressure", $"generalDF.main.pressure")
  .withColumn("main_humidity", $"generalDF.main.humidity")
  .withColumn("main_temp_min", $"generalDF.main.temp_min")
  .withColumn("main_temp_max", $"generalDF.main.temp_max")
  .withColumn("general_visibility", $"generalDF.visibility")
  .withColumn("wind_speed", $"generalDF.wind.speed")
  .withColumn("wind_direction_degree", $"generalDF.wind.deg")
  .withColumn("clouds_cloudiness", $"generalDF.clouds.all")
  .withColumn("general_date", from_unixtime($"generalDF.dt").cast(TimestampType))
  .withColumn("sys_type", $"generalDF.sys.type")
  .withColumn("sys_id", $"generalDF.sys.id")
  .withColumn("sys_country", $"generalDF.sys.country")
  .withColumn("sys_sunrise", from_unixtime($"generalDF.sys.sunrise").cast(TimestampType))
  .withColumn("sys_sunset", from_unixtime($"generalDF.sys.sunset").cast(TimestampType))
  .withColumn("general_timezone", $"generalDF.timezone")
  .withColumn("general_id", $"generalDF.id")
  .withColumn("general_name", $"generalDF.name")
  .withColumn("general_cod", $"generalDF.cod")
  .drop("coord").drop("weather").drop("base").drop("main").drop("visibility").drop("wind")
  .drop("clouds").drop("dt").drop("sys").drop("timezone").drop("id").drop("name").drop("cod")
  .filter($"sys_country".isNotNull)

  //metric calculation
  val weatherAllCountries = generalData
  .withWatermark("general_date", "10 minutes")
  .groupBy($"sys_country", window($"general_date", "10 minutes", "5 minutes")) 
   .agg(
       avg("main_temp").alias("on_avg"),
       max("main_temp").alias("on_max"),
       min("main_temp").alias("on_min")
       )
       
  //more metric calculation, adding another metric
  val onlineAnalysisResults = weatherAllCountries
  .select($"*", ($"on_max"-$"on_min").as("on_diff"))
  .withColumn("windowStart", from_unixtime(unix_timestamp($"window.start"), "yyyy-MM-dd' 'HH:mm:ss"))
  .withColumn("windowEnd", from_unixtime(unix_timestamp($"window.end"), "yyyy-MM-dd' 'HH:mm:ss"))
  .drop("window")
  
  val onlineAnalysisResultsDE = onlineAnalysisResults.select("*").filter($"sys_country" === "DE")
  val onlineAnalysisResultsNL = onlineAnalysisResults.select("*").filter($"sys_country" === "NL")
  val onlineAnalysisResultsCH = onlineAnalysisResults.select("*").filter($"sys_country" === "CH")
  val onlineAnalysisResultsAT = onlineAnalysisResults.select("*").filter($"sys_country" === "AT")
  val onlineAnalysisResultsFR = onlineAnalysisResults.select("*").filter($"sys_country" === "FR")
  val onlineAnalysisResultsBE = onlineAnalysisResults.select("*").filter($"sys_country" === "BE")
  val onlineAnalysisResultsLU = onlineAnalysisResults.select("*").filter($"sys_country" === "LU")
  
  //loading offline metric results for comparison
  val offlineWeatherPerCountryRaw = spark.read.option("charset", "UTF-8").schema(schema.offlineSchema).json("/Users/localuser/Documents/flight/postprocessing/offlineWeather.json")
  val offlineWeatherPerCountry = offlineWeatherPerCountryRaw
  .select(from_json($"value", schema.offlineContentSchema) as "results")
  .select($"results.*")
  .withColumn("off_country",$"sys_country")
  .drop("sys_country")
  .withColumn("off_avg", $"avg")
  .drop("avg")
  .withColumn("off_max", $"max")
  .drop("max")
  .withColumn("off_min", $"min")
  .drop("min")
  .withColumn("off_diff", $"(max - min)")
  .drop("(max - min)")
  offlineWeatherPerCountry.show()
  
  //comparison results, online versus offline
  val comparisonResults = onlineAnalysisResults
  .join(offlineWeatherPerCountry, $"sys_country" === $"off_country", "inner")
  .withColumn("avg_compare", $"on_avg"-$"off_avg")
  .withColumn("max_compare", $"on_max"-$"off_max")
  .withColumn("min_compare", $"on_min"-$"off_min")
  .withColumn("diff_compare", $"on_diff"-$"off_diff")
  .drop("off_avg").drop("off_max").drop("off_min")
  .drop("on_avg").drop("on_max").drop("on_min")
  .drop("off_country").drop("off_diff").drop("on_diff")
  
  //prediction: select data for prediction
  val rawFeatureData= generalData
  .withWatermark("general_date", "10 minutes")
  .groupBy($"sys_country", window($"general_date", "10 minutes", "5 minutes"))
  .agg(
     avg("main_temp").alias("avg"),
     max("main_temp").alias("max"),
     min("main_temp").alias("min"),
     avg("main_pressure").alias("pressure"),
     avg("main_humidity").alias("humidity"),
     avg("clouds_cloudiness").alias("clouds")
   )

  //prediction: create numeric features
  val assembler = new VectorAssembler()
    .setInputCols(Array("avg", "max", "min", "pressure","humidity","clouds"))
    .setOutputCol("features")
  val features = assembler.transform(rawFeatureData)
    
  //prediction: load a prelearned model that is used for prediction
  val modelPath = Configurations.trainedModelPath
  val model = PipelineModel.read.load(modelPath)
  
  // run the model on data that is arriving per batch
  val predictionResults = model.transform(features).select("sys_country", "window","prediction")
  .withColumn("windowStart", from_unixtime(unix_timestamp($"window.start"), "yyyy-MM-dd' 'HH:mm:ss"))
  .withColumn("windowEnd", from_unixtime(unix_timestamp($"window.end"), "yyyy-MM-dd' 'HH:mm:ss"))
  .drop("window")
  
  //starting the queries
  //online analysis results
  onlineAnalysisResultsDE
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.onlineAnalysisDePath)
  .start()
  
  onlineAnalysisResultsNL
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.onlineAnalysisNlPath)
  .start()
  
  onlineAnalysisResultsCH
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.onlineAnalysisChPath)
  .start()
  
  onlineAnalysisResultsAT
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.onlineAnalysisAtPath)
  .start()
  
  onlineAnalysisResultsFR
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.onlineAnalysisFrPath)
  .start()
  
  onlineAnalysisResultsBE
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.onlineAnalysisBePath)
  .start()
  
  onlineAnalysisResultsLU
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.onlineAnalysisLuPath)
  .start()
  
  onlineAnalysisResults
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.onlineAnalysisResultsPath)
  .start()
  
  //comparison results
  comparisonResults
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.comparisonResultsPath)
  .start()
  
  //general data
  generalData
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.generalDataPath)
  .start()
  
  //prediction results
  predictionResults
  .repartition(1)
  .writeStream
  .format("csv")
  .option("path", Configurations.predictionResultsPath)
  .start()
  
  spark.streams.awaitAnyTermination()  

  }  

  
}