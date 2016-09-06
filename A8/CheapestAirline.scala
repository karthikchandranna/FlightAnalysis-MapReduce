import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import scala.util.Try
import scala.collection.JavaConversions._
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.{StructType,StructField,StringType,DoubleType};

/*
 * <h1>Cheapest Airline Based on Linear Regression</h1>
 * The spark program runs a Linear Regression on the sanitized Flights data to find the cheapest airline and 
 * further calculates its weekly median price for all the years.
 *
 * @author: Karthik, Ruinan, Naveen and Sujith
 * @date: March 24, 2016
 */
object CheapestAirline {

	/**
    * @name: tryToInt
    * @description: converts the string to integer type
    * catches if any exception using try. 
    * @param: s String
    * @return: s Int
    */
	def tryToInt( s: String ) = Try(s.toInt).toOption

	/**
    * @name: tryToFloat
    * @description: converts the string to float type
    * catches if any exception using try. 
    * @param: s String
    * @return: s Float
    */
	def tryToFloat( s: String) = Try(s.toFloat).toOption

	/**
    * @name: roundUp
    * @description: converts the given double value to int, correspondin 
    * to its ceil value.
    * @param: d Double
    * @return: d Int
    */
	def roundUp(d: Double) = math.ceil(d).toInt


	/**
    * @name: main
    * @description: For a given value and airline data find the cheapeat airline carrier
    * based on given N value where N is the travel time using Linear Regression. Also 
    * find the weekly median price for the cheapeast airline carrier.
    * @param: 
    * 1. InputFolder - files containing airline data 
    * 2. OutputFolder - data containing weekly median price for airline carrier
    * which has been computed to be cheapest based on N value.
    * 3. N value
    * @return: file - data containing the weekly median price for the cheapest carrier for given N.
    */
	def main(args: Array[String]) {

		// get the arguments, set app name and config.
		val inputFolder = args(0);
		val outputFile = args(1);
		val N = args(2).toInt;		
		var conf = new SparkConf().setAppName("CheapestAirline")
		val sc = new SparkContext(conf)

		// sanitize all the records from the airline dataset.
		val allRecords = sc.textFile(inputFolder)
					.map { _.replaceAll("\"","").replaceAll(", ","|").split(",") }
					.filter (record => AirlineDetails.validate(record))

		// Build mapReduce for sanitized data in order to feed this to linear regression.
	    val cheapAirlineRecords = allRecords
	   				.map( record => 
					((record(0).toInt,		//YEAR
					record(8)),				//CARRIER
					(record(50).toFloat,	//ELAPSED_TIME
					record(109).toFloat)) 	//PRICE
					)			
					.groupByKey()

		// 	get the intercept and slope for each key value pair of cheapAirlineRecords			
		val coefficients = cheapAirlineRecords.map{ o =>
			val regression = new SimpleRegression();
			o._2.map{ x =>
				regression.addData(x._1, x._2)
				}
			(o._1, (regression.getIntercept(),regression.getSlope()))
		}

		// defining new sqlContext build table with year, carrier, value to find the cheapest airline.
		val sqlContext = new org.apache.spark.sql.SQLContext(sc)
		val schema = StructType(Array(StructField("year",StringType,true),StructField("carrier",StringType,true),StructField("value",DoubleType,true)))
		val rowCo = coefficients.map(r => Row((r._1._1.toString),
						(r._1._2.toString), 
						(r._2._1) + N*(r._2._2)))

		// create tables which are used to find the cheapest airline based on the coefficient value(slope and intercept) 
		// from linear regression. Select the cheapeast airline based on the lowest coefficients with highest count.
		val dfAirline = sqlContext.createDataFrame(rowCo, schema)
			dfAirline.registerTempTable("airline")
		val dfResult = sqlContext.sql("SELECT x.year, x.value, x.carrier from airline x , (SELECT p.year, min(value) AS min_value FROM airline p GROUP BY p.year) c where c.year = x.year and c.min_value = x.value")
			dfResult.registerTempTable("airlineFinal")
		val dfFinal = sqlContext.sql("select p.carrier from airlineFinal p group by p.carrier order by count(*) desc limit 1")
		val dfConverted = dfFinal.withColumn("c",dfFinal("carrier")).drop("carrier")
		var cheapestAirline = dfConverted.take(1)(0)(0).toString

		// finding the weekly data for the cheapest airline from the sanitized records
		val leastMedianRecords = allRecords
		 			.filter(record => record(8) == cheapestAirline)
	   				.map( record => 
					((record(0).toInt,								//YEAR
					AirlineDetails.getWeekOfYear(record(5))),		//WEEK_NUMBER
					(record(109).toFloat)))							//PRICE			
					.groupByKey()

		// for each weekly data find the median price
		val weeklyMedian = leastMedianRecords.map{ o =>
			val sortedPriceList = o._2.toList.sortWith(_ < _)
			var medianIndex = roundUp(sortedPriceList.length/2)
			var median = sortedPriceList(medianIndex)
			(o._1, (median))
		}

		// saving the output to file.
		val result = weeklyMedian.map{ case (key, value) => Array(cheapestAirline, key._1.toString, key._2.toString, value.toString).mkString(",")}
		result.saveAsTextFile(outputFile)
	}
}
