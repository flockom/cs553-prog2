import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.regex.*;

//TODO: modify regex to handle the special chars

public class WordCountJ{
    /*should be the expected number of unique words*/
    static final int HASH_SIZE = 100000;
    static final int LF        = 2;
    static final String USAGE=
	"USAGE: java WordCountJ THREADS OUTPUT [FILES...]         \n"+
	"   THREADS     - # of threads to use                     \n" + 
	"   OUTPUT      - file to ouput results to (overwritten)  \n" +
	"   [FILES...]  - list of files to wordcount              \n";
    
    public static void main(String[] args){
	Date start = new Date();
	if(args.length < 3){
	    System.out.println("Wrong number of arguments:\n"+USAGE);
	    return;
	}
		
	//get # threads
	int tc = Integer.parseInt(args[0]);
	String outfile = args[1];

	//make a threadsafe queue of all files to process
	ConcurrentLinkedQueue<String> files = new ConcurrentLinkedQueue<String>();
	for(int i=2;i<args.length;i++){
	    files.add(args[i]);
	}

	//hastable for results
	Hashtable<String,Integer> results = new Hashtable<String,Integer>(HASH_SIZE,LF);
	
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
	      out.println(sorted_results.get(i).getKey()+ "   " + sorted_results.get(i).getValue());
	  }
	}catch(Exception e){System.out.println("Caught Exception: " + e.getMessage());}
    }
    
}

class Worker extends Thread{
    ConcurrentLinkedQueue<String> files;
    Hashtable<String,Integer> results;
    //regex to match one word of the format (WsW)* where where W
    //is a letter or number and s is an optional symbol. Words cannot
    //end with a symbol, will match any non alpha-nums at the end to
    //move the cursor to the next alpha-num.
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
	    new Hashtable<String,Integer>(WordCountJ.HASH_SIZE,WordCountJ.LF);
	//grab a file to work on
	String cf;
	while( (cf = files.poll()) != null){
	    try(BufferedReader input = new BufferedReader(new FileReader(cf))){
		String text;
		//well go line-by-line... maybe this is not the fastest
		while((text=input.readLine()) != null){
		    //parse words
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
	    }catch (Exception e) {
		System.out.println(" caught a " + e.getClass() +
				   "\n with message: " + e.getMessage());
		return;
	    }
	    //merge local hashmap with shared one,could have a
	    //seperate thread do this but that might be cheating	    
	    
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