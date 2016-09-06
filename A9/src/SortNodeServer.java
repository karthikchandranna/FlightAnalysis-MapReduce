
import java.net.*;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class SortNodeServer implements Runnable
{
	private static int NUMBER_OF_INSTANCES = 10;
	private static ServerSocket serverSocket = null;
	private Socket conn = null;
	private static int MASTER_PORT = 11555;
	private static String s3Path = "s3://rpa9/";
	public SortNodeServer(Socket conn) throws IOException
	{
		System.out.println("invoking the constructor server side and returing a runnable");
		this.conn = conn;
	}

	public synchronized void run()
	{
		try
		{
			System.out.println("Runnable in server: Just connected to "
					+ conn.getRemoteSocketAddress());
			//            DataInputStream in =
			//                  new DataInputStream(conn.getInputStream());
			ObjectInputStream inToServer = new ObjectInputStream(conn.getInputStream());
			try {
				String header = inToServer.readUTF();
				System.out.println(header);
				if (header.equals("distribution")){
					List<Job> jobs = new ArrayList<Job>();
					jobs =  (ArrayList<Job>) inToServer.readObject();
					System.out.println("server1 jobs :" + jobs);
					ArrayList<Integer> sample = new ArrayList<Integer>();
					for (Job job : jobs) {
						for(String file : job.fileList){
							System.out.println(file);
							getSample(file, sample);               
						}
						System.out.println(job.toString());
					}
					String serverName = "localhost";
					Socket socket = new Socket(serverName,MASTER_PORT);
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					//OutputStream outToMasterPrimitives = socket.getOutputStream();
					//DataOutputStream out = new DataOutputStream(outToMasterPrimitives);
					System.out.println(sample);
					out.writeUTF("sampled");
					out.writeObject(sample);
					socket.close();
				}
				else if(header.equals("pivots")){
					System.out.println("Enter mapping");
					JobTemperature job =  (JobTemperature) inToServer.readObject();                           
					System.out.println(job);
					runJob(job);
					ProcessBuilder aws = new ProcessBuilder().inheritIO().command("aws", "s3", "cp", "output-"+String.valueOf(job.jobId)+"/output", s3Path+"output", "--recursive");
					Process p = aws.start();
					try{
						p.waitFor();
					}catch(Exception e){
					}
					String serverName = "localhost";
					Socket socket = new Socket(serverName,MASTER_PORT);
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					//OutputStream outToMasterPrimitives = socket.getOutputStream();
					//DataOutputStream out = new DataOutputStream(outToMasterPrimitives);
					out.writeUTF("shuffled");
					out.writeObject(new String("Shuffle dummy"));
					socket.close();
				}
				else if(header.equals("sort")){
					List<JobSort> jobSortList = (ArrayList<JobSort>)inToServer.readObject();
					sortJob(jobSortList);                   
					System.out.println("Enter Sorting");
					String serverName = "localhost";
					Socket socket = new Socket(serverName,MASTER_PORT);
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					out.writeUTF("sorted");
					out.writeObject(new String("Sorted dummy"));
					socket.close();                   
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//System.out.println("runnable in server prints what it got from teh clinet: "+in.readUTF());

			//conn.close();
		}catch(SocketTimeoutException s)
		{
			System.out.println("Socket timed out!");
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void server()
	{
		int port = 6072; // Have a common port later
		Socket socket = null;

		try {
			serverSocket = new ServerSocket(port);
			while (true) {
				System.out.println("In server: waiting for a request from client");
				socket = serverSocket.accept();
				System.out.println("In server: Got a request from client");
				Runnable runnable = new SortNodeServer(socket);
				System.out.println("In server: runnable obj"+ runnable);
				Thread thread = new Thread(runnable);
				System.out.println("In server: executing the runnable");
				thread.start();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}                  
	}

	public static void sortJob1(int job)
	{
		int range=1;

		AmazonS3 s3 = new AmazonS3Client();
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		s3.setRegion(usEast1);
		AmazonS3URI URL1 = new AmazonS3URI(s3Path+"output/"+String.valueOf(range)+"/");
		String bucketName = URL1.getBucket();
		String path = URL1.getURI().getPath().substring(1)+"/";
		ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
		.withBucketName(bucketName)
		.withPrefix(path)
		.withDelimiter("/"));
		List<String> files = new ArrayList<>();
		for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
			long sizeInMb =  (objectSummary.getSize()/(1024*1024));
			//if(!files.containsKey(objectSummary.getKey()) && sizeInMb > 0) {
			//    files.add(objectSummary.getKey());
			//}  
		}
		List<WeatherDetails> temp = new ArrayList<WeatherDetails>();
		for(String file : files){
			S3Object object = s3.getObject(
					new GetObjectRequest(s3Path.substring(5, 8), file));
			InputStream objectData = object.getObjectContent();
			WeatherDetails tt = null;
			//ObjectInputStream ob = new ObjectInputStream(new GZIPInputStream(objectData));
			//while ((tt = (WeatherDetails) ob.readObject()) != null)
			//    temp.add(tt);
			//Collections.sort(temp);

		}
	}

	public static void sortJob(List<JobSort> jobSortList) throws IOException, ClassNotFoundException
	{
		System.out.println("Inside sortJob Fn");
		System.out.println("Jobs:" + jobSortList);
		for(JobSort jobSort : jobSortList) { 
			int range = jobSort.temperature;

			AmazonS3 s3 = new AmazonS3Client();
			Region usEast1 = Region.getRegion(Regions.US_EAST_1);
			s3.setRegion(usEast1);
			AmazonS3URI URL1 = new AmazonS3URI(s3Path+"output/"+String.valueOf(range));			
			String bucketName = URL1.getBucket();
			System.out.println("Bucket:" +bucketName);
			String path = URL1.getURI().getPath().substring(1)+"/";
			System.out.println("Bucket:" +path);
			ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
			.withBucketName(bucketName)
			.withPrefix(path)
			.withDelimiter("/"));
			List<String> files = new ArrayList<>();
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				long sizeInMb =  (objectSummary.getSize()/(1024*1024));
				if(!files.contains(objectSummary.getKey()) && sizeInMb > 0) {
					files.add(objectSummary.getKey());
				}   
			}
			System.out.println("Files list:"+files);
			List<WeatherDetails> temperatures = new ArrayList<WeatherDetails>();
			for(String file : files){
				S3Object object = s3.getObject(
						new GetObjectRequest("rpa9", file));
				InputStream objectData = object.getObjectContent();				
				ObjectInputStream ob = new ObjectInputStream(new GZIPInputStream(objectData));
				WeatherDetails tt = null;
				while ((tt = (WeatherDetails) ob.readObject()) != null)
					temperatures.add(tt);
				Collections.sort(temperatures,new WeatherDetails());
				System.out.println("Sorted temps: "+temperatures.subList(0, 1000));
			}
		}
	}
	public static void runJob(JobTemperature job){
		List<Integer> r = job.temperatures;
		HashMap<Integer, DataOutputStream> s = new HashMap<Integer, DataOutputStream>();
		ArrayList<String> name = new ArrayList<String>();
		for(int i=0; i<r.size(); i++){
			name.add("output-"+String.valueOf(job.jobId)+"/output/"+String.valueOf(r.get(i))+"/"+serverSocket.getInetAddress().getHostAddress()+"-"+String.valueOf(job.jobId));
		}
		name.add("output-"+String.valueOf(job.jobId)+"/output/"+"N"+"/"+serverSocket.getInetAddress().getHostAddress()+"-"+String.valueOf(job.jobId));
		for(int i=0; i<r.size()+1; i++){
			try{
				File f = new File(name.get(i));
				f.getParentFile().mkdirs();
				f.createNewFile();
				s.put(i, new DataOutputStream(new FileOutputStream(f)));
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		AmazonS3 s3Client = new AmazonS3Client();
		for ( String file : job.fileList){
			try{

				//System.out.println("/climate/"+path);
				S3Object object = s3Client.getObject(
						new GetObjectRequest("rpa9", file));
				InputStream objectData = object.getObjectContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(objectData)));
				String nline;
				while ((nline = br.readLine()) != null){
					mapper(nline, r, s);
				}
			}catch(Exception e){
			}
		}
		for(int i=0; i<r.size()+1; i++){
			try{
				s.get(i).close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

	}
	public static void getSample(String path, ArrayList<Integer> sample) {
		try{
			AmazonS3 s3Client = new AmazonS3Client();
			//System.out.println("/climate/"+path);
			S3Object object = s3Client.getObject(
					new GetObjectRequest("rpa9", path));
			InputStream objectData = object.getObjectContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(objectData)));
			String nline;
			WeatherDetails r = new WeatherDetails();
			int index=0;
			while ((nline = br.readLine()) != null && index < 10){
				if(weatherParser(nline, r)){
					sample.add(r.temp);
					index++;
				}
			}
			objectData.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void mapper(String line, List<Integer> keyList, HashMap<Integer, DataOutputStream> p){
		WeatherDetails r = new WeatherDetails();
		weatherParser(line, r);
		int i;
		for(i=0; i<keyList.size(); i++){
			if (r.temp < keyList.get(i))
				break;
		}
		writeContext(p.get(i), r);   
	}
	private static void writeContext(DataOutput out, Object o){
		WeatherDetails oo = (WeatherDetails) o;
		oo.write(out);
	}  
	private static boolean weatherParser(String s, WeatherDetails a){
		String[] sArray = s.split(",");
		int wban=0,date=0,time=0,temp=0;
		try{
			wban=Integer.parseInt(sArray[0]);
		}catch(Exception e){
			return false;
		}
		try{
			date=Integer.parseInt(sArray[1]);
		}catch(Exception e){
			return false;
		}
		try{
			time=Integer.parseInt(sArray[2]);
		}catch(Exception e){
			return false;
		}
		try{
			temp=Integer.parseInt(sArray[8]);
		}catch(Exception e){
			return false;
		}
		a.set(wban, date, time, temp);
		return true;
	}
	public static void main(String [] args) {
		server();
	} 
}