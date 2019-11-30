package Preprocessing

import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession
import scala.reflect.io.Path.string2path
import StartApplication.Configurations

//Creates a List of 120 cities spread over western europe
object CountryBuilder {

  import scala.reflect.io.File

    def isDirectoryExists(directory: String): Boolean = {
    val dir = File(directory)
    if (dir.isDirectory) true else false
  }

  def isFileExists(fileName: String): Boolean = {
    val file = File(fileName)
    if (file.isFile && file.exists) true else false
  }

  def deleteFile(fileName: String): Unit = {
    val file = File(fileName)
    if (file.isFile && file.exists) {
      file.delete()
    }
  }

  def deleteDirectory(directory: String): Unit = {
    val dir = File(directory)
    if (dir.isDirectory && dir.exists) {
      dir.deleteRecursively()
    }
  }
  
  def main(args: Array[String]) {
   
  
  import org.apache.spark.sql.catalyst.encoders.RowEncoder
  import org.apache.spark.sql.types._
  import org.apache.spark.sql.Row
  import org.apache.spark.sql.streaming.{OutputMode, Trigger}
  

  val spark = SparkSession.builder.appName("Spark_Streaming").master("local[2]").getOrCreate();
   spark.sparkContext.setLogLevel("ERROR")
    // this is used to implicitly convert an RDD to a DataFrame.
  import spark.implicits._
  // Import Spark SQL data types and Row.
  import org.apache.spark.sql._
  
  val cityListTemp = spark.read.option("multiline",true).json(Configurations.basePath+"/preprocessing/city.list2.json")
  val countryCodes = spark.read.option("charset", "UTF-8").json(Configurations.basePath+"/preprocessing/2letter_country_codes.json")
  val euroCountriesTemp = spark.read.option("charset", "UTF-8").json(Configurations.basePath+"/preprocessing/euro-countries.json")
  val cityPop = spark.read.format("csv").option("header", "true").load(Configurations.basePath+"/preprocessing/worldcities.csv")
  println("cityPop")
  cityPop.show() 
  
  val cityPopNotNull = cityPop
  .filter($"population".isNotNull)
  .filter($"population" > Configurations.populationThreshold)
  println("cityPopNotNull")
  cityPopNotNull.show()  
    
  val cityList = cityListTemp.withColumn("CityName", $"name").drop("name")
  println("cityList")
  cityList.show()
  
  val euroCountries = euroCountriesTemp
  .withColumn("CodeName", $"code").drop("code")
  .withColumn("CountryName", $"name").drop("name")


  val countryCodesEurope = countryCodes.join(euroCountries, $"Code" === $"CodeName", "inner")
  countryCodesEurope.drop("CodeName").drop("CountryName")
  println("countryCodesEurope")
  countryCodesEurope.show()
     
  val relevantCountriesAllCities = countryCodesEurope.join(cityList, $"Code" === $"country", "inner")
  .withColumn("CountryCode", $"Code")
  .withColumn("CountryName", $"Name")
  .withColumn("CityCoords", $"coord")
  .withColumn("CityName", $"CityName")
  .withColumn("CityID", $"id")
  .drop("Code").drop("Name").drop("coord").drop("country").drop("id")
  .sort("CountryCode")
  
  val relevantCountriesRelevantCities = relevantCountriesAllCities
  .join(cityPopNotNull, $"CityName" === $"city", "inner")
  .dropDuplicates("CityName")
  .drop("city")
  .drop("city_ascii")
  .drop("lat")
  .drop("lng")
  .drop("country")
  .drop("iso2")
  .drop("iso3")
  .drop("admin_name")
  .drop("capital")
  .drop("id")
  println("relevantCountriesRelevantCities")
  relevantCountriesRelevantCities.show()
  
  val cityListPerCountry = relevantCountriesRelevantCities.groupBy($"CountryName")
  .agg(
      collect_list("CityName").as("CityName"), 
      collect_list("CityID").as("CityID"))
      
  val forDelete1 = Configurations.basePath+"/preprocessing/relevantCountriesRelevantCities"
  val forDelete2 = Configurations.basePath+"/preprocessing/cityListPerCountry"
  if(isDirectoryExists(forDelete1))
    deleteDirectory(forDelete1)
  
  if(isDirectoryExists(forDelete2))
    deleteDirectory(forDelete2)

  relevantCountriesRelevantCities.show()
  relevantCountriesRelevantCities.printSchema()
  relevantCountriesRelevantCities.toJSON.write.json(Configurations.basePath+"/preprocessing/relevantCountriesRelevantCities")
 
  cityListPerCountry.show()
  cityListPerCountry.printSchema()
  cityListPerCountry.toJSON.write.json(Configurations.basePath+"/preprocessing/cityListPerCountry")
   
  }  
  
  

  
}