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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.google.common.collect.Lists;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class SortNodeMaster implements Runnable
{
    private static final int JOB_LIST_SPLIT_SIZE = 4;
    private static final int NUMBER_OF_INSTANCES = 2; // take number of lines in node details file
    private static final int NUMBER_OF_JOBS_PER_NODE = 5;
    private static ServerSocket serverSocket = null;
    private Socket conn = null;	
    private static final int MASTER_PORT = 11555;
    private static final int NUMBER_OF_CORES = 4;
    private static List<Integer> sampledList =
             Collections.synchronizedList(new ArrayList<Integer>());
    private static AtomicInteger instancesCompletedSampling = new AtomicInteger(0);
    private static AtomicInteger instancesCompletedShuffling = new AtomicInteger(0);
    private static AtomicInteger instancesCompletedSorting = new AtomicInteger(0);
    private static List<Integer> tempPivotsList = null;
    private static Map<Integer, List<String>> instanceFilesMap = new HashMap<>();
    private static Integer sampleJobsCount = 0;
    private static Integer sampleJobListsCount = 0;
    private static Integer shuffleJobsCount = 0;
    private static Integer sortJobListsCount = 0;

    public SortNodeMaster(Socket conn) throws IOException
    {
        this.conn = conn;
    }

    public synchronized void run()
    {
        try
        {
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
            String header = in.readUTF();
           
            if (header.equals("sampled")) {
                System.out.println("Started sample");
                List<Integer> sampledNumbers = new ArrayList<Integer>();
                sampledNumbers =  (ArrayList<Integer>) in.readObject();
                instancesCompletedSampling.incrementAndGet();
                System.out.println("instancesCompletedSampling : " + instancesCompletedSampling.get());
                // instead of 2, num of instances * num of jobs per node
                if (instancesCompletedSampling.get() < sampleJobListsCount) {
                    sampledList.addAll(sampledNumbers);
                } else if (instancesCompletedSampling.get() == sampleJobListsCount) {
                    sampledList.addAll(sampledNumbers);
                    findPivots();
                }
                System.out.println("End sample");
            }
            else if (header.equals("shuffled")) {
                System.out.println("Started shuffle");
                instancesCompletedShuffling.incrementAndGet();
                // instead of 8, num of instances * num of cores
                if (instancesCompletedShuffling.get() == shuffleJobsCount) {
                    sendSortMessage();
                }
                System.out.println("End shuffle");
            }
            else if (header.equals("sorted")) {
            	 System.out.println("Started sort");
            	 instancesCompletedSorting.incrementAndGet();
            	// instead of 8, num of instances * num of cores
                 if (instancesCompletedSorting.get() == sortJobListsCount) {
                	 System.out.println("Distributed Sort is complete !!!");
                	// code to kill all instances goes here
                 }
                 System.out.println("End sort");                
            }
           
            conn.close();
        }catch(SocketTimeoutException s)
        {
            System.out.println("Socket timed out!");
        }catch(IOException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
   
    private void sendSortMessage() throws UnknownHostException, IOException {
       
    	System.out.println("Sleeping... Check op dirs");
    	try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // 1 job for each temp pivot
        List<Integer> ports = new ArrayList<Integer>();
        ports.add(6072);
        ports.add(6073);
        String serverName = "localhost";
        int iter = 1;
        Map<Integer, List<Integer>> tempsToNodeMap = new HashMap<>();
		for (Integer temperature: tempPivotsList) {
            int currentJobIndex = iter % NUMBER_OF_INSTANCES;
            List<Integer> existingTemperatures = new ArrayList<Integer>();
           
            if (tempsToNodeMap .containsKey(currentJobIndex)) {
            	existingTemperatures = tempsToNodeMap.get(currentJobIndex);
            }
            existingTemperatures.add(temperature);
            tempsToNodeMap.put(currentJobIndex,existingTemperatures);
            iter++;
        }
		int nodeNumber = 0;
        for (Integer port: ports) {
            Map<Integer, List<JobSort>> tempsCoreDistributionMap = new HashMap<Integer, List<JobSort>>();
            
            int tempsCoreDistributionIter = 1;
            for (Integer temp: tempsToNodeMap.get(nodeNumber)) {
                int currentJobIndex = tempsCoreDistributionIter % NUMBER_OF_CORES;
                List<JobSort> existingJobs = new ArrayList<>();
              
				if (tempsCoreDistributionMap.containsKey(currentJobIndex)) {
					existingJobs = tempsCoreDistributionMap.get(currentJobIndex);
                }
				JobSort job = new JobSort(temp);
				existingJobs.add(job);
                tempsCoreDistributionMap.put(currentJobIndex,existingJobs);
                tempsCoreDistributionIter++;
            }
            
            for (List<JobSort> jobs : tempsCoreDistributionMap.values()) {
                ArrayList<JobSort> partitionedJobs = new ArrayList<>();
                partitionedJobs.addAll(jobs);
                System.out.println("Printing temp list per core in node "+jobs);
                Socket client = new Socket(serverName, port);//6072
                ObjectOutputStream outToServer = new ObjectOutputStream(client.getOutputStream());
                outToServer.writeUTF("sort");
                outToServer.writeObject(partitionedJobs);
                client.close();
                sortJobListsCount++;
            }
            nodeNumber++;
            
        }
    }

    public static void findPivots() throws IOException {
        /*List<Integer> sampledList = new ArrayList<>();
        int low = 280;
        int high = 300;
        for (int i=0; i<50; i++) {
            int rand = RandomUtils.nextInt(high - low) + low;
            sampledList.add(rand);
        }*/
        Collections.sort(sampledList);
        int partitions = sampleJobsCount;//NUMBER_OF_INSTANCES * NUMBER_OF_JOBS_PER_NODE;
       
        List<Integer> maxList = new ArrayList<Integer>();
        for (List<Integer> partition : Lists.partition(sampledList,partitions)) {
            int max = Collections.max(partition);
            maxList.add(max);
        }
       
        List<Integer> temperatures = new ArrayList<Integer>();
        int next=1;
        for (Integer temperature: maxList) {
            if(next < maxList.size()){
                Integer nextTemp = maxList.get(next);
                if(nextTemp==temperature)
                    temperatures.add(temperature-1);
                else
                    temperatures.add(temperature);
                next++;
            }
            else
                temperatures.add(temperature);
        }
               
        HashSet<Integer> tempPivots = new HashSet<>(temperatures);
        tempPivotsList = new ArrayList<>(tempPivots);
        Collections.sort(tempPivotsList);
        // Sending the pivots to the nodes
        List<Integer> ports = new ArrayList<Integer>();
        ports.add(6072);
        ports.add(6073);
        String sortNode = "localhost";
        int nodeNumber = 0;
        for (Integer port: ports) {
            Map<Integer, List<String>> fileDistributionMap = new HashMap<Integer, List<String>>();;
            int iter = 1;
            for (String file: instanceFilesMap.get(nodeNumber)) {
                int currentJobIndex = iter % NUMBER_OF_CORES;
                List<String> existingFileNames = new ArrayList<String>();
               
                if (fileDistributionMap.containsKey(currentJobIndex)) {
                    existingFileNames = fileDistributionMap.get(currentJobIndex);
                }
                existingFileNames.add(file);
                fileDistributionMap.put(currentJobIndex,existingFileNames);
                iter++;
            }
            nodeNumber++;
            for (Integer jobNumber : fileDistributionMap.keySet()) {
                JobTemperature jobTemp = new JobTemperature(tempPivotsList, fileDistributionMap.get(jobNumber));
                Socket client = new Socket(sortNode, port);
                ObjectOutputStream outToServer = new ObjectOutputStream(client.getOutputStream());
                outToServer.writeUTF("pivots");
                outToServer.writeObject(jobTemp);
                client.close();
                shuffleJobsCount++;
            }
        }
       
    }

    public static void master()
    {
        try
        {
            distributeInputFiles();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String [] args) {
        master();
    }

    public static void distributeInputFiles() throws UnknownHostException, IOException {

        String sortNode = "localhost";
        //cs6240sp16 rpa9
        Map<String, Long> sortedFileMap = getFilesFromS3("s3://rpa9/climate");

        int count = 1;
        for (String fileName: sortedFileMap.keySet()) {
            int currentInstance = count % NUMBER_OF_INSTANCES;
            List<String> existingFileNames = new ArrayList<>();
            if (instanceFilesMap.containsKey(currentInstance)) {
                existingFileNames = instanceFilesMap.get(currentInstance);
                existingFileNames.add(fileName);
            } else {
                existingFileNames.add(fileName);
            }
            instanceFilesMap.put(currentInstance, existingFileNames);
            count ++;
        }

        int nodeNumber=0;
        List<Integer> ports = new ArrayList<Integer>();
        ports.add(6072);
        ports.add(6073);
        for (Integer port : ports) {
            Map<Integer, List<String>> fileDistributionMap = new HashMap<Integer, List<String>>();

            int iter = 1;
            for (String file: instanceFilesMap.get(nodeNumber)) {
                int currentJobIndex = iter % NUMBER_OF_JOBS_PER_NODE;
                List<String> existingFileNames = new ArrayList<String>();
                if (fileDistributionMap.containsKey(currentJobIndex)) {
                    existingFileNames = fileDistributionMap.get(currentJobIndex);
                }
                existingFileNames.add(file);
                fileDistributionMap.put(currentJobIndex,existingFileNames);
                iter++;
            }

            List<Job> jobList = new ArrayList<Job>();
            for (Integer jobNumber : fileDistributionMap.keySet()) {
                Job job = new Job(fileDistributionMap.get(jobNumber));
                jobList.add(job);
                sampleJobsCount++;
            }
            
            Map<Integer, List<Job>> jobPartitionsMap = new HashMap<Integer, List<Job>>();

            int jobIter = 1;
            for (Job job: jobList) {
                int currentJobIndex = jobIter % NUMBER_OF_CORES;
                List<Job> existingJobs = new ArrayList<Job>();
                if (jobPartitionsMap.containsKey(currentJobIndex)) {
                	existingJobs = jobPartitionsMap.get(currentJobIndex);
                }
                existingJobs.add(job);
                jobPartitionsMap.put(currentJobIndex,existingJobs);
                jobIter++;
            }
                       
            for (List<Job> partition : jobPartitionsMap.values()) {
                ArrayList<Job> partitionedJobs = new ArrayList<Job>();
                partitionedJobs.addAll(partition);
                Socket client = new Socket(sortNode, port);//6072
                ObjectOutputStream outToServer = new ObjectOutputStream(client.getOutputStream());
                outToServer.writeUTF("distribution");
                outToServer.writeObject(partitionedJobs);
                client.close();
                sampleJobListsCount++;
            }

            nodeNumber++;

        }
        System.out.println("sample job lists count : " + sampleJobListsCount);
       
        try {
            Socket socket = null;
            //ServerSocket serverSocket = new ServerSocket(6072);
            serverSocket = new ServerSocket(MASTER_PORT);
            while (true) {
                socket = serverSocket.accept();
                Runnable runnable = new SortNodeMaster(socket);
                Thread thread = new Thread(runnable);
                thread.start();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        

    }

    public static Map<String,Long> getFilesFromS3(String URI){
        AmazonS3 s3 = new AmazonS3Client();
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        s3.setRegion(usEast1);
        AmazonS3URI URL1 = new AmazonS3URI(URI);
        String bucketName = URL1.getBucket();
        String path = URL1.getURI().getPath().substring(1)+"/";
        ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
        .withBucketName(bucketName)
        .withPrefix(path)
        .withDelimiter("/"));
        Map<String,Long> files = new HashMap<>();
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            long sizeInMb =  (objectSummary.getSize()/(1024*1024));
            if(!files.containsKey(objectSummary.getKey()) && sizeInMb > 0) {
                files.put(objectSummary.getKey(), sizeInMb);
            }    
        }

        Map<String, Long> sortedMapAsc = sortByComparator(files, true);

        return sortedMapAsc;
    }

    private static Map<String, Long> sortByComparator(Map<String, Long> unsortMap, final boolean order)
    {

        List<Entry<String, Long>> list = new LinkedList<Entry<String, Long>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Long>>()
                {
            public int compare(Entry<String, Long> o1,
                    Entry<String, Long> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
                });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        for (Entry<String, Long> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;

    }

    public static void printMap(Map<String, Long> map)
    {
        int size = 0;
        for (Entry<String, Long> entry : map.entrySet())
        {
            System.out.println("Key : " + entry.getKey() + " Value : "+ entry.getValue());
            size += entry.getValue();
        }
        System.out.println(size);
    }   

}