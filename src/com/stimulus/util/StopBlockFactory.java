package com.stimulus.util;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.logging.*;

public class StopBlockFactory implements Runnable {

	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,ThreadUtil.getDaemonThreadFactory("stopblock"));
	ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(this, 500, 500, TimeUnit.MILLISECONDS);
	WeakHashMap<Thread,StopBlock> stopBlocks = new WeakHashMap<Thread,StopBlock>();
	protected static Log logger = LogFactory.getLog(StopBlockFactory.class.getName());

	public StopBlockFactory() {
	}
	
	public void detectBlock(String description, Thread thread, StopBlockTarget target, int timeout) {
		StopBlock stopBlock = new StopBlock(description, thread, target, timeout);
		stopBlocks.put(thread,stopBlock);
	}
	
	public void endDetectBlock(Thread thread) {
		try {
			stopBlocks.remove(thread);
		} catch (Exception e) {}
	}
	
	@Override
	public void finalize() {
		shutdown();
	}
	  
	public void shutdown() {
		stopBlocks.clear();
		scheduledTask.cancel(true);
		scheduler.shutdown();
		scheduler.shutdownNow();
	}
	public void run() {
		
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			Thread.currentThread().setName("stop blocker");
			Thread.currentThread().setDaemon(true);
			for (StopBlock stopBlock : stopBlocks.values()) {
				if (stopBlock.isStuck()) {
					stopBlock.handleBlock();
					stopBlocks.remove(stopBlock.getThread());
				} 
				if (stopBlock.isTerminated()) {
					stopBlocks.remove(stopBlock.getThread());
				}
			}
		
			
		}
		
	
}
