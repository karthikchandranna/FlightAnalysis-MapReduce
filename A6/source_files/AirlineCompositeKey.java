import org.apache.commons.lang.text.StrTokenizer;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * This class is a writable having details of the factors used for airline delay prediction
 *
 * Created by sujith, karthik on 3/7/16.
 */
public class AirlineCompositeKey implements WritableComparable<AirlineCompositeKey> {

    public  int year;
    public  int quarter;
    public  int month;
    public  int day;
    public  int dayOfWeek;
    public  int date;
    public  int flno;
    public  int orgid;
    public  int destid;
    public  int crsArrTime;
    public  int crsDepTime;
    public  int crsElapTime;
    public  String carrier;
    public  int isFromChicago;
    public  int isHoliday;
    public  int isPopularSrcDest;
    public  int isBusyDay;
    public  int distance;
    public  String isDelayed;

    public AirlineCompositeKey(){}


    public AirlineCompositeKey(String data) {
        StrTokenizer t = new StrTokenizer(data,',','"');
        t.setIgnoreEmptyTokens(false);
        String[] line = t.getTokenArray();
        
        this.year = Integer.parseInt(line[Constants.YEAR]);
        this.quarter = Integer.parseInt(line[Constants.QUARTER]);
        this.month = Integer.parseInt(line[Constants.MONTH]);
        this.day = Integer.parseInt(line[Constants.DAYOFMONTH]);
        this.dayOfWeek = Integer.parseInt(line[Constants.DAYOFWEEK]);
        this.date = Integer.parseInt(line[Constants.DATE]);
        this.flno = Integer.parseInt(line[Constants.FLNO]);
        this.orgid = Integer.parseInt(line[Constants.ORGID]);
        this.destid = Integer.parseInt(line[Constants.DESTID]);
        this.crsArrTime = Integer.parseInt(line[Constants.CRSARRTIME]);
        this.crsDepTime = Integer.parseInt(line[Constants.CRSDEPTIME]);
        this.crsElapTime = Integer.parseInt(line[Constants.CRSELAPTIME]);
        this.carrier = line[Constants.CARRIER];
        this.isFromChicago = Integer.parseInt(line[Constants.ISFROMCHICAGO]);
        this.isHoliday = Integer.parseInt(line[Constants.ISHOLIDAY]);
        this.isPopularSrcDest = Integer.parseInt(line[Constants.ISPOPULARSRCDEST]);
        this.isBusyDay = Integer.parseInt(line[Constants.ISBUSYDAY]);
        this.distance = Integer.parseInt(line[Constants.DISTANCE]);
        this.isDelayed = line[Constants.ISDELAYED];
                
    }
    
    @Override
    public int compareTo(AirlineCompositeKey airlineCompositeKey) {
        if (date >= airlineCompositeKey.date) {
            return 1;
        }
        return 0;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(year);
        dataOutput.writeInt(quarter);
        dataOutput.writeInt(month);
        dataOutput.writeInt(day);
        dataOutput.writeInt(dayOfWeek);
        dataOutput.writeInt(date);
        dataOutput.writeInt(flno);
        dataOutput.writeInt(orgid);
        dataOutput.writeInt(destid);
        dataOutput.writeInt(crsArrTime);
        dataOutput.writeInt(crsDepTime);
        dataOutput.writeInt(crsElapTime);
        dataOutput.writeUTF(carrier);
        dataOutput.writeInt(isFromChicago);
        dataOutput.writeInt(isHoliday);
        dataOutput.writeInt(isPopularSrcDest);
        dataOutput.writeInt(isBusyDay);
        dataOutput.writeInt(distance);
        dataOutput.writeUTF(isDelayed);
        
        
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
          year = dataInput.readInt();
          quarter = dataInput.readInt();
          month = dataInput.readInt();
          day = dataInput.readInt();
          dayOfWeek = dataInput.readInt();
          date = dataInput.readInt();
          flno = dataInput.readInt();
          orgid = dataInput.readInt();
          destid = dataInput.readInt();
          crsArrTime = dataInput.readInt();
          crsDepTime = dataInput.readInt();
          crsElapTime = dataInput.readInt();
          carrier = dataInput.readUTF();
          isFromChicago =  dataInput.readInt();
          isHoliday  = dataInput.readInt();
          isPopularSrcDest = dataInput.readInt();
          isBusyDay = dataInput.readInt();
          distance = dataInput.readInt();
          isDelayed = dataInput.readUTF();
    }
}
