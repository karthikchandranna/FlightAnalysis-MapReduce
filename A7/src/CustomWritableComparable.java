/**
 * @classname: CustomWritableComparable
 * 
 * @author Ruinan,Naveen,Karthik,Sujith
 * @description This is used to implement the sort for the CustomWritable objects.
 *
 */

public class CustomWritableComparable implements Comparable{
	public String cityName;
	public int flightType;
	public long scheduleTime;
	public long actualTime;
	public int year;
	public int month;
	public int dayOfMonth;
	public int actualElapsedTime;
	public int distance;
	public int distanceGroup;
	public String carrier;
	
	public  CustomWritableComparable(){

	}
	public CustomWritableComparable(String cityName, int flightType, long scheduleTime, long actualTime, int year, int month, int dayOfMonth, int actualElapsedTime, int distance, int distanceGroup, String carrier) {
		this.cityName = cityName;
		this.flightType = flightType;
		this.scheduleTime = scheduleTime;
		this.actualTime = actualTime;
		this.year = year;
		this.month = month;
		this.dayOfMonth = dayOfMonth;
		this.actualElapsedTime = actualElapsedTime;
		this.distance = distance;
		this.distanceGroup = distanceGroup;
		this.carrier = carrier;
	}
	@Override
	public int compareTo(Object comparesTTS) {
		long scheduleTime=((CustomWritableComparable)comparesTTS).scheduleTime;
		//For Ascending order /1000 or too large for int 31536000 secs in a year
		return (int)(this.scheduleTime-scheduleTime);
	}
	@Override
	public String toString() {
		return cityName+","+flightType+","+scheduleTime+","+actualTime+","+year+","+month+
				dayOfMonth+","+actualElapsedTime+","+distance+","+distanceGroup;
	}
	
}
