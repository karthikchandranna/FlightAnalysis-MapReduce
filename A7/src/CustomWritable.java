/**
 * @classname: CustomWritable
 *
 * @author Ruinan,Naveen, Karthik, Sujith
 * @description This is used to write custom type from mapper.
 *
 */
import org.apache.hadoop.io.*;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CustomWritable implements WritableComparable<CustomWritable>{
	private String cityName;
	private String airPortNameO;
	private String airPortNameD;
	private int flightNum;
	private int flightType;
	private long scheduleTime;
	private long actualTime;
	private int year;
	private int month;
	private int dayOfMonth;
	private int actualElapsedTime;
	private int cRSElapsedTime;
	private int distance;
	private int distanceGroup;
	private String carrier;
	public CustomWritable() {
		set(null, null, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null);
	}
	public CustomWritable(String cityName, int flightType, long scheduleTime, long actualTime, int year, int month, int dayOfMonth, int actualElapsedTime, int distance, int distanceGroup, String carrier) {
		set(cityName, "", "", 0, flightType, scheduleTime, actualTime, year, month, dayOfMonth, 0, actualElapsedTime, distance, distanceGroup, carrier);
	}
	public CustomWritable(String airportO, String airportD, int flightNum, int flightType, long scheduleTime, int year, int month, int dayOfMonth, int ElapsedTime) {
		set("", airportO, airportD, flightNum, flightType, scheduleTime, 0, year, month, dayOfMonth, ElapsedTime, 0, 0, 0, "");
	}
	public void set(String cityName, String airPortNameO, String airPortNameD, int flightNum, int flightType, long scheduleTime, long actualTime, int year, int month, int dayOfMonth, int cRSElapsedTime, int actualElapsedTime, int distance, int distanceGroup, String carrier) {
		this.cityName = cityName;
		this.airPortNameO = airPortNameO;
		this.airPortNameD = airPortNameD;
		this.flightNum = flightNum;
		this.flightType = flightType;
		this.scheduleTime = scheduleTime;
		this.cRSElapsedTime = cRSElapsedTime;
		this.actualTime = actualTime;
		this.year = year;
		this.month = month;
		this.dayOfMonth = dayOfMonth;
		this.actualElapsedTime = actualElapsedTime;
		this.distance = distance;
		this.distanceGroup = distanceGroup;
		this.carrier = carrier;

	}
	public String getCityName() {
		return cityName;
	}
	public int getFlightType() {
		return flightType;
	}
	public long getScheduleTime() {
		return scheduleTime;
	}
	public long getActualTime() {
		return actualTime;
	}
	public int getYear(){
		return year;
	}
	public int getMonth(){
		return month;
	}
	public int getDayOfMonth(){
		return dayOfMonth;
	}
	public int getActualElapsedTime(){
		return actualElapsedTime;
	}
	public int getCRSElapsedTime(){
		return cRSElapsedTime;
	}
	public int getDistance(){
		return distance;
	}
	public int getDistanceGroup(){
		return distanceGroup;
	}
	public String getAirportO(){
		return airPortNameO;
	}
	public String getAirportD(){
		return airPortNameD;
	}
	public int getFlightNum()
	{
		return flightNum;
	}
	public String getCarrier()
	{
		return carrier;
	}
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(cityName);
		out.writeUTF(airPortNameO);
		out.writeUTF(airPortNameD);
		out.writeInt(flightNum);
		out.writeInt(flightType);
		out.writeInt(cRSElapsedTime);
		out.writeLong(scheduleTime);
		out.writeLong(actualTime);
		out.writeInt(year);
		out.writeInt(month);
		out.writeInt(dayOfMonth);
		out.writeInt(actualElapsedTime);
		out.writeInt(distance);
		out.writeInt(distanceGroup);
		out.writeUTF(carrier);

	}
	@Override
	public void readFields(DataInput in) throws IOException {
		cityName = in.readUTF();
		airPortNameO = in.readUTF();
		airPortNameD = in.readUTF();
		flightNum = in.readInt();
		flightType = in.readInt();
		cRSElapsedTime = in.readInt();
		scheduleTime = in.readLong();
		actualTime = in.readLong();
		year = in.readInt();
		month = in.readInt();
		dayOfMonth = in.readInt();
		actualElapsedTime = in.readInt();
		distance = in.readInt();
		distanceGroup = in.readInt();
		carrier = in.readUTF();
	}
	@Override
	public int hashCode() {
		return flightType;
	}
	@Override
	public boolean equals(Object o) {
		return false;
	}
	@Override
	public int compareTo(CustomWritable tp) {
		return 0;
	}
}
