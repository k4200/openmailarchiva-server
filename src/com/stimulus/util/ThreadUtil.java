package com.stimulus.util;
import java.util.concurrent.*;

public class ThreadUtil {
	
    public static ThreadFactory getDaemonThreadFactory(String name) { 
    	return new DaemonThreadFactory(name);
    }
    

	 private static class DaemonThreadFactory implements ThreadFactory {
		  
		 	 String name;
		 	 
		     public DaemonThreadFactory(String name) {
		    	 this.name = name;
		     }
		    
			public Thread newThread(Runnable r) {
			    Thread t = Executors.defaultThreadFactory().newThread(r);
			    //t.setDaemon(true);
			   // t.setName(name);
			    return t;
			}
				
	}

}
