package Analysis

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.evaluation.ClusteringEvaluator
import org.apache.spark.ml.{Pipeline, PipelineModel}
import Schema.Schemas
import StartApplication.Configurations


class ModelTraining {
  
  def setLearningDataPath(newLearningDataPath: String){
    Configurations.setLearningDataPath(newLearningDataPath)
  }
  
    def start() {
      
      val spark = SparkSession
        .builder()
        .appName("Offline Analysis")
        .master("local[2]")
        .getOrCreate()
      // For implicit conversions like converting RDDs to DataFrames
      import spark.implicits._
      spark.sparkContext.setLogLevel("ERROR")
            
      import spark.implicits._
      val schema = new Schemas()
      val path = Configurations.learningDataPath
      val df = spark.read.schema(schema.schema).json(path)
     
      val generalData = df.select($"*").as("generalDF")
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
      
      
        val rawFeatureData= generalData
        .groupBy($"sys_country") 
        .agg(
             avg("main_temp").alias("avg"),
             max("main_temp").alias("max"),
             min("main_temp").alias("min"),
             avg("main_pressure").alias("pressure"),
             avg("main_humidity").alias("humidity"),
             avg("clouds_cloudiness").alias("clouds")
           )
      rawFeatureData.show()
//      generalData.select($"main_temp", $"main_pressure", $"main_humidity", $"clouds_cloudiness").show()      
//      val rawFeatureData = generalData.select($"main_temp", $"main_pressure", $"main_humidity", $"clouds_cloudiness")
      
      val assembler = new VectorAssembler()
        .setInputCols(Array("avg", "max", "min", "pressure","humidity","clouds"))
        .setOutputCol("features")
      
      val features = assembler.transform(rawFeatureData)
      println("Assembled columns to vector column 'features'")
      features.select("features").show(false)
      
      // Cluster the data into two classes using KMeans
      val numClusters = 2
      val numIterations = 20
      // kmeans model with 8 clusters

         // Trains a k-means model.
    val kmeans = new KMeans()
      .setK(2)
      .setSeed(1L)
      .setMaxIter(numIterations)
        
    val pipeline = new Pipeline()
      .setStages(Array(kmeans))
  
    val pipelineModel = pipeline.fit(features)
    println("results written to: "+Configurations.trainedModelPath)
    pipelineModel.write.overwrite.save(Configurations.trainedModelPath)    
    
    }
    
}