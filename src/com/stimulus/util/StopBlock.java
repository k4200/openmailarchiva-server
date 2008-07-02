package com.stimulus.util;
import org.apache.commons.logging.*;


public class StopBlock {

	Thread thread;
	long timeout;
	long lastReset;
	StopBlockTarget target;
	String description;
	protected static Log logger = LogFactory.getLog(StopBlock.class.getName());
	
	public static StopBlock getStopBlock(String description, Thread thread, StopBlockTarget target, long timeout) {
		StopBlock stopBlock = new StopBlock(description, thread, target, timeout);
		return stopBlock;
		
	}
	
	public StopBlock(String description, Thread thread, StopBlockTarget target, long timeout) {
		this.thread = thread;
		this.timeout = timeout;
		this.target = target;
		this.description = description;
		reset();
	}
	
	
	public Thread getThread() {
		return thread;
	}
	
	public void reset() {
		lastReset = System.currentTimeMillis();
	}
	
	public boolean isStuck() {
		boolean stuck = false;
		Thread.State e = thread.getState();
		if (e == Thread.State.WAITING || e == Thread.State.BLOCKED || e == Thread.State.TIMED_WAITING) {
			long duration = System.currentTimeMillis() - lastReset;
			if (duration>timeout) {
				logger.error(description+" blocked {duration='"+duration+"',timeout='"+timeout+"'}");
				stuck = true;
			}
		} else {
			reset();
		}
		return stuck;
	}
	
	public boolean isTerminated() { 
		boolean terminated = (thread==null || !thread.isAlive());
		if (terminated)
			logger.error(description+" terminated");
		return terminated;
	}
	
	public void handleBlock() {
		logger.debug("handleBlock() "+description);
		target.handleBlock(thread);
		StackTraceElement[] stackTraceElements = thread.getStackTrace();
		for (int i = 0; i < stackTraceElements.length; i++) {
			StackTraceElement stackTraceElement = stackTraceElements[i];
			logger.error("      "+stackTraceElement.toString());
		}
    	logger.debug("handleBlock() end "+description);
	}

	
}
