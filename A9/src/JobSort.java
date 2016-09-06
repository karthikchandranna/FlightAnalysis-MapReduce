
import org.apache.commons.lang.math.RandomUtils;


public class JobSort implements java.io.Serializable {
	Integer jobId;
	Integer temperature;
	
	public JobSort(Integer temperature) {
		this.jobId = RandomUtils.nextInt();
		this.temperature = temperature;
	}

	@Override
	public String toString() {
		return "JobSort [jobId=" + jobId + ", temperature=" + temperature + "]";
	}	
	
	
}