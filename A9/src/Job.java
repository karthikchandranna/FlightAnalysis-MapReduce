import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.math.RandomUtils;


public class Job implements java.io.Serializable {
	Integer jobId;
	List<String> fileList;
	
	public Job(List<String> fileArr) {
		this.jobId = RandomUtils.nextInt();
		this.fileList = fileArr;
	}

	@Override
	public String toString() {
		return "Job [jobId=" + jobId + ", fileArr=" + fileList
				+ "]";
	}
	
}
