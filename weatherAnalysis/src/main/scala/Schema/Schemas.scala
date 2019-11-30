package Schema

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType, FloatType, ArrayType}


class Schemas {
  
  
    val offlineContentSchema = StructType(
    List(
      StructField("sys_country", StringType, true),
      StructField("avg", StringType, true),
      StructField("max", StringType, true),
      StructField("min", StringType, true),
      StructField("(max - min)", StringType, true)
    )
  )
  
  val offlineSchema = StructType(
    List(
      StructField("value", StringType, true)
    )
  )
  
  
    val coordSchema = StructType(
    List(
      StructField("lon", StringType, true),
      StructField("lat", StringType, true)
    )
  )
  
    val weatherSchema = StructType(
    List(
      StructField("id", StringType, true),
      StructField("main", StringType, true),
      StructField("description", StringType, true),
      StructField("icon", StringType, true)
    )
  )

    val mainSchema = StructType(
    List(
      StructField("temp", FloatType, true),
      StructField("pressure", IntegerType, true),
      StructField("humidity", IntegerType, true),
      StructField("temp_min", StringType, true),
      StructField("temp_max", StringType, true)
    )
  )
  
      val windSchema = StructType(
    List(
      StructField("speed", StringType, true),
      StructField("deg", StringType, true)
    )
  )

  val cloudSchema = StructType(
    List(
      StructField("all", IntegerType, true)
    )
  )
  
      val sysSchema = StructType(
    List(
      StructField("type", StringType, true),
      StructField("id", StringType, true),
      StructField("country", StringType, true),
      StructField("sunrise", StringType, true),
      StructField("sunset", StringType, true)
    )
  )
  
    val schema = StructType(
    List(
      StructField("coord", coordSchema, true),
      StructField("weather", ArrayType(weatherSchema), true),
      StructField("base", StringType, true),
      StructField("main", mainSchema, true),
      StructField("visibility", StringType, true),
      StructField("wind", windSchema, true),
      StructField("clouds", cloudSchema, true),
      StructField("dt", StringType, true),
      StructField("sys", sysSchema, true),
      StructField("timezone", StringType, true),
      StructField("id", StringType, true),
      StructField("name", StringType, true),
      StructField("cod", StringType, true)
    )
  )
  
      val aimSchema = StructType(
    List(
      StructField("lon", StringType, true),
      StructField("lat", StringType, true),
      StructField("id", StringType, true),
      StructField("main", StringType, true),
      StructField("description", StringType, true),
      StructField("icon", StringType, true),
      StructField("base", StringType, true),
      StructField("temp", StringType, true),
      StructField("pressure", StringType, true),
      StructField("humidity", StringType, true),
      StructField("temp_min", StringType, true),
      StructField("temp_max", StringType, true),
      StructField("visibility", StringType, true),
      StructField("wind speed", StringType, true),
      StructField("wind deg", StringType, true),
      StructField("clouds", StringType, true),
      StructField("dt", StringType, true),
      StructField("type", StringType, true),
      StructField("id", StringType, true),
      StructField("country", StringType, true),
      StructField("sunrise", StringType, true),
      StructField("sunset", StringType, true),
      StructField("timezone", StringType, true),
      StructField("id", StringType, true),
      StructField("name", StringType, true),
      StructField("cod", StringType, true)
    )
  )
  }   
 