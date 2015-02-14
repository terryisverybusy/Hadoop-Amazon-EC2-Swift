import java.util.concurrent.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class WordCountJ{
    /*should be the expected number of unique words*/
    static final int H_SIZE = 100000;
    static final int LF        = 2;
    
    public static void main(String[] args){
	Date start = new Date();
	if(args.length < 3){
	    System.out.println(“Please input the arguments:\n" + "USAGE: java WordCountJ THREADS OUTPUT [FILES...]         \n");
	    return;
	}
		
	//get number of threads
	int tc = Integer.parseInt(args[0]);
	String outfile = args[1];

	//make a threadsafe queue of all files to process
	ConcurrentLinkedQueue<String> files = new ConcurrentLinkedQueue<String>();
	for(int i=2;i<args.length;i++){
	    files.add(args[i]);
	}

	//hastable for results
	Hashtable<String,Integer> results = new Hashtable<String,Integer>(H_SIZE,LF);
	
	//spin up the threads
	Thread[] workers = new Thread[tc];
	for(int i=0;i<tc;i++){
	    workers[i] = new Worker(files,results);
	    workers[i].start();
	}
	
	//wait for them to finish
	try{
	    for(int i=0;i<tc;i++){	 
		workers[i].join();
	    }
	}catch(Exception e){System.out.println("Caught Exception: " + e.getMessage());}

	//terminal output
	Date end = new Date();
	System.out.println(end.getTime() - start.getTime() + " total milliseconds");
	System.out.println(results.size()+ " unique words");
	
	//sort results for easy comparison/verification
	List<Map.Entry<String,Integer>> sorted_results 
	      = new ArrayList<Map.Entry<String,Integer>>(results.entrySet());
	Collections.sort(sorted_results,new KeyComp());
	//file output
	try{
	    PrintStream out = 
		new PrintStream(outfile);
	    for(int i=0;i<sorted_results.size();i++){
	      out.println(sorted_results.get(i).getKey()+ "\t" + sorted_results.get(i).getValue());
	  }
	}catch(Exception e){System.out.println("Caught Exception: " + e.getMessage());}
    }
    
}

class Worker extends Thread{
    ConcurrentLinkedQueue<String> files;
    Hashtable<String,Integer> results;
    
    //
    static final Pattern pattern = Pattern.compile("(([a-zA-Z0-9](\\S[a-zA-Z0-9])?)+)[^a-zA-Z0-9]+");

    Worker(ConcurrentLinkedQueue<String> files,
	   Hashtable<String,Integer> results){
	super();
	this.files = files;
	this.results=results;
    }
	
    public void run(){
	//each file is processed into a local hash table and then merged with the global results
	//this will cause much less contention on the global table, but still avoids a sequential update
	Hashtable<String,Integer> local_results = 
	    new Hashtable<String,Integer>(WordCountJ.H_SIZE,WordCountJ.LF);
	//grab a file to work on
	String cf;
	while( (cf = files.poll()) != null){
	    try{
		BufferedReader input = new BufferedReader(new FileReader(cf));
		String text;
		//well go line-by-line... maybe this is not the fastest
		while((text=input.readLine()) != null){
		    //parse words
		    //System.out.println(text);
		    Matcher matcher = pattern.matcher(text);
		    while(matcher.find()){			
			String word = matcher.group(1);
			if(local_results.containsKey(word)){
			    local_results.put(word,1+local_results.get(word));
			}else{
			    local_results.put(word,1);
			}			    
		    }
		}
		input.close();
	    }catch (Exception e) {
		System.out.println(" caught a " + e.getClass() +
				   "\n with message: " + e.getMessage());
		return;
	    }
	    	    
	    //
	    Iterator<Map.Entry<String,Integer>> updates=local_results.entrySet().iterator();
	    while(updates.hasNext()){
		Map.Entry<String,Integer> kv = updates.next();
		String k = kv.getKey();
		Integer v = kv.getValue();
		synchronized(results){
		    if(results.containsKey(k)){
			results.put(k,v+results.get(k));
		    }else{
			results.put(k,v);
		    }	      		    		   
		}
	    }
	    local_results.clear();
	}
    }
	    
}

class KeyComp implements Comparator<Map.Entry<String,Integer>>{
	
     public int compare(Map.Entry<String,Integer> a,Map.Entry<String,Integer> b){
	return a.getKey().compareTo(b.getKey());
    }
}