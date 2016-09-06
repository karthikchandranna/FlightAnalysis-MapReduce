import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * This class implements a writable comparable and is passed as a value in
 * the mapper context
 * Created by sujith, karthik on 2/20/16.
 */
public class FlightDetails implements WritableComparable<FlightDetails> {

    public Boolean type;
    public long scheduledTime;
    public long actualTime;
    public short cancelled;

    public FlightDetails(Boolean type, long scheduledTime, long actualTime, short cancelled) {
        this.type = type;
        this.scheduledTime = scheduledTime;
        this.actualTime = actualTime;
        this.cancelled = cancelled;
    }

    public FlightDetails() {}

    @Override
    public int compareTo(FlightDetails flightDetails) {
        // compare the 2 objects based on scheduledTime
        long flightScheduledTime = flightDetails.scheduledTime;
        long diff = this.scheduledTime - flightScheduledTime;

        if (diff < 0) return -1;
        if (diff == 0) return 0;
        return 1;
    }

    @Override
    public String toString() {
        return "FlightDetails{" +
                "type=" + type +
                ", scheduledTime=" + scheduledTime +
                ", actualTime=" + actualTime +
                ", cancelled=" + cancelled +
                '}';
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeBoolean(type);
        dataOutput.writeLong(scheduledTime);
        dataOutput.writeLong(actualTime);
        dataOutput.writeShort(cancelled);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        type = dataInput.readBoolean();
        scheduledTime = dataInput.readLong();
        actualTime = dataInput.readLong();
        cancelled = dataInput.readShort();
    }

}
