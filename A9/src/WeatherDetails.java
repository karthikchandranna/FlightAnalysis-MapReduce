import java.io.DataOutput;
import java.io.IOException;
import java.util.Comparator;

public class WeatherDetails implements Comparator<WeatherDetails>{
	int wban;
	int	date;
	int time;
	int temp;
	WeatherDetails(){
	}
	void set(int wban, int date, int time, int temp){
		this.wban = wban;
		this.date = date;
		this.time = time;
		this.temp = temp;
	}
	
	public int getTemp(){
		return temp;
	}
	public int getWban(){
		return wban;
	}
	public int getTime(){
		return time;
	}
	public int getDate(){
		return date;
	}
	void write(DataOutput out){
		try {
			out.writeInt(wban);
			out.writeInt(date);
			out.writeInt(time);
			out.writeInt(temp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	@Override
	public int compare(WeatherDetails o1, WeatherDetails o2) {
		// ascending order
		return o1.temp - o2.temp;
		// descending order
		// return o2.temp - o1.temp;
	}
	
	/*public static void main(String args[]){
		
		List<WeatherDetails> wListArr =  new ArrayList<WeatherDetails>();
		WeatherDetails w = new WeatherDetails();
		w.set(1, 1, 1, 150);
		WeatherDetails x = new WeatherDetails();
		x.set(2, 1, 1, 100);
		WeatherDetails y = new WeatherDetails();
		y.set(3, 1, 1, 200);
		WeatherDetails z = new WeatherDetails();
		z.set(4, 1, 1, 175);
		
		wListArr.add(w);
		wListArr.add(x);
		wListArr.add(y);
		wListArr.add(z);
		
		Collections.sort(wListArr, new WeatherDetails());
		for(WeatherDetails w1: wListArr)
	        System.out.println(w1.getTemp() +"  : "+w1.getWban());
		 
	}*/
}
	