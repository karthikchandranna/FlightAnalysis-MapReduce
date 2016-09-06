import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.math.RandomUtils;


public class JobTemperature implements java.io.Serializable {
	Integer jobId;
	List<Integer> temperatures;
	List<String> fileList;
	
	public JobTemperature(List<Integer> temperatures, List<String> fileArr) {
		this.jobId = RandomUtils.nextInt();
		this.temperatures = temperatures;
		this.fileList = fileArr;
	}

	@Override
	public String toString() {
		return "JobTemperature [jobId=" + jobId + ", temperatures="
				+ temperatures + ", fileList=" + fileList + "]";
	}
	
	
}
