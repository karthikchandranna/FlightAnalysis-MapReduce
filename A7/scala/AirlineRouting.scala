/**
 * @scalaFile: AirlineRouting
 *
 * @author: Ruinan, Naveen, Karthik, Sujith
 * @description: This program provides the best connection for each request based on missed connection ratio and duration of two hop routes.  
 *               Uses the spark sql to achieve the joins across tables. 
 */

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.{StructType,StructField,StringType};
import org.apache.spark.sql.functions.{concat, lit}
import org.apache.spark.sql.functions._

// user defined datatypes
val toInt    = udf[Int, String]( _.toInt)
val toDouble = udf[Double, String]( _.toDouble)

// creating new sql context
val sqlContext = new SQLContext(sc)
// creating new dataframe reader with csv format.
val dfReader = sqlContext.read
dfReader.format("com.databricks.spark.csv")

// defining the schema  and loading History dataset
val schemaHisString = "median precentage"
val schemaHis =
  StructType(
    schemaHisString.split(" ").map(fieldName => StructField(fieldName, StringType, true)))
dfReader.option("header", "false") 
dfReader.option("inferSchema", "true")
dfReader.schema(schemaHis)
val dfHistory = dfReader.load("s3://A7Naveen/history")

// defining the schema  and loading Test dataset
val schemaTestString = "year month day airportO airportM airportD elapsedTime flightNum1 flightNum2"
val schemaTest =
  StructType(
    schemaTestString.split(" ").map(fieldName => StructField(fieldName, StringType, true)))
dfReader.schema(schemaTest)
val dfTest = dfReader.load("s3://A7Naveen/test")

// Join the history and test dataset based on intermediate city to get the missedconnection ratio.
val dfTrainTest = dfTest.join(dfHistory, dfTest("airportM") === dfHistory("median"), "inner")

// defining the schema  and loading Request dataset
val schemaReqString = "year month dayOfMonth origin destination ignore"
val schemaReq =  StructType(schemaReqString.split(" ").map(fieldName => StructField(fieldName, StringType, true)))
dfReader.schema(schemaReq)
val dfRequest = dfReader.load("s3://A7Naveen/04req10k.csv")

// creating temp tables for requests and connections
dfRequest.registerTempTable("requests")
dfTrainTest.registerTempTable("connections")

// joining the requests and connections table based on date, origin and destination.
// This associates each request with available connections.
val dfReqCon = sqlContext.sql("SELECT c.year as year,c.month as month,c.day as day,r.origin as origin,r.destination as destination,elapsedTime as duration,flightNum1,flightNum2,median as intermediateCity,precentage as missedConnPercentage from requests r, connections c where r.year = c.year and r.month = c.month and r.dayOfMonth = c.day and r.origin = c.airportO and r.destination = c.airportD")

// Converting the columns of the above resultset to relevant datatypes.
val dfReqConType = dfReqCon.withColumn("year", toInt(dfReqCon("year"))).withColumn("month", toInt(dfReqCon("month"))).withColumn("day", toInt(dfReqCon("day"))).withColumn("origin", dfReqCon("origin")).withColumn("destination", dfReqCon("destination")).withColumn("duration", toInt(dfReqCon("duration"))).withColumn("flightNum1", toInt(dfReqCon("flightNum1"))).withColumn("flightNum2", toInt(dfReqCon("flightNum2"))).withColumn("intermediateCity", dfReqCon("intermediateCity")).withColumn("missedConnPercentage", toDouble(dfReqCon("missedConnPercentage")))

// This will pick the best connection for each request based on missed connection ratio and then duration.
val dfPreFinal = dfReqConType.groupBy("year","month","day","origin","destination").agg(min("missedConnPercentage"),min("duration"),min("flightNum1"),min("flightNum2"))

// Converting the columns of the above resultset to relevant datatypes.
val dfPreFinalType = dfPreFinal.withColumn("year", dfPreFinal("year")).withColumn("month", dfPreFinal("month")).withColumn("day", dfPreFinal("day")).withColumn("origin", dfPreFinal("origin")).withColumn("destination", dfPreFinal("destination")).withColumn("duration", dfPreFinal("min(duration)")).withColumn("flightNum1", dfPreFinal("min(flightNum1)")).withColumn("flightNum2", dfPreFinal("min(flightNum2)")).withColumn("missedConnPercentage", dfPreFinal("min(missedConnPercentage)"))

// dropping irrelavent columns from the above dataset.
val dfFinal = dfPreFinalType.drop("min(missedConnPercentage)").drop("missedConnPercentage").drop("min(flightNum1)").drop("min(flightNum2)").drop("min(duration)")

// defining the schema  and loading validation/missed dataset
val schemaMissedString = "year month day origin destination flightNum1 flightNum2"
val schemaMissed =
  StructType(
    schemaMissedString.split(" ").map(fieldName => StructField(fieldName, StringType, true)))
dfReader.schema(schemaMissed)
val dfMissed = dfReader.load("s3://A7Naveen/04missed.csv")

// Converting the columns of the above resultset to relevant datatypes.
val dfMissedType = dfMissed.withColumn("year", toInt(dfMissed("year"))).withColumn("month", toInt(dfMissed("month"))).withColumn("day", toInt(dfMissed("day"))).withColumn("origin", dfMissed("origin")).withColumn("destination", dfMissed("destination")).withColumn("flightNum1", toInt(dfMissed("flightNum1"))).withColumn("flightNum2", toInt(dfMissed("flightNum2")))

// creating temp tables for final and missed
dfFinal.registerTempTable("final")
dfMissedType.registerTempTable("missed")

// calculating the total duration of the proposed connections.
val sumDurationFinal = sqlContext.sql("select sum(duration) from final") 

// Count of the number of missed connections from our proposed connections.
val dfMissedCount = sqlContext.sql("select count(*) from final f, missed m where f.year = m.year and f.month = m.month and f.day = m.day and f.origin = m.origin and f.destination = m.destination and f.flightNum1 = m.flightNum1 and f.flightNum2 = m.flightNum2")

// converting and printing the final output.
val duration = sumDurationFinal.withColumn("duration",toInt(sumDurationFinal("_c0"))).drop("_c0")
val missedCount = dfMissedCount.withColumn("missedCount",toInt(dfMissedCount("_c0"))).drop("_c0")
var durationInt = duration.head().getInt(0)
var missedCountInt = missedCount.head().getInt(0)
var finalResult = (durationInt/60) + (100 * missedCountInt)
println("Duration in hours: "+(durationInt/60))
println("Number of missed connections: "+missedCountInt)
println("Total duration after penalty in hours: "+finalResult)

/**
* 32599 => Total duration in hours
*/

/**
* 525 => Number of missed connections
* 85099.9 total hours after a 100 hour penalty for each missed connection
*/
