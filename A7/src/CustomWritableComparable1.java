/**
 * @classname: CustomWritableComparable
 * 
 * @author Ruinan,Naveen, Karthik, Sujith
 * @description This is used to implement the sort for the CustomWritable objects.
 *
 */

public class CustomWritableComparable1 implements Comparable{
	public String airportO;
	public String airportD;
	public int flightType;
	public int flightNum;
	public long scheduleTime;
	public int year;
	public int month;
	public int dayOfMonth;
	public int ElapsedTime;
	
	public  CustomWritableComparable1(){

	}
	public CustomWritableComparable1(String airportO, String airportD, int flightType, int flightNum, long scheduleTime, int year, int month, int dayOfMonth, int ElapsedTime) {
		this.airportO = airportO;
		this.airportD = airportD;
		this.flightType = flightType;
		this.flightNum = flightNum;
		this.scheduleTime = scheduleTime;
		this.year = year;
		this.month = month;
		this.dayOfMonth = dayOfMonth;
		this.ElapsedTime = ElapsedTime;
	}
	@Override
	public int compareTo(Object comparesTTS) {
		long scheduleTime=((CustomWritableComparable1)comparesTTS).scheduleTime;
		//For Ascending order /1000 or too large for int 31536000 secs in a year
		return (int)(this.scheduleTime-scheduleTime);
	}
	@Override
	public String toString() {
		return airportO+","+airportD+","+flightType+","+scheduleTime+","+year+","+month+
				dayOfMonth;
	}
	
}
