package com.stimulus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

	public class RuntimeExecutor {
	private long timeout = Long.MAX_VALUE;

	public RuntimeExecutor() {
	}
	
	public RuntimeExecutor(long timeout) {
		this.timeout = timeout;
	}


	protected String getOutputAsString(InputStream is) throws IOException {
		if (is==null) 
			return null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuffer strOutput = new StringBuffer();
		String line = null;
		do {
			line = reader.readLine();
			if (line!=null) {
				strOutput.append(line+"\n");
			}
		} while (line!=null);
	    try { reader.close(); } catch (Exception e) {}
	    return strOutput.toString();
	}

	
	public String execute(String[] commands) throws IOException, TimeoutException {
		Process p;
		
		p = Runtime.getRuntime().exec(commands);
		
		Timer timer = new Timer();
		timer.schedule(new InterruptScheduler(Thread.currentThread()), this.timeout);
	
		try {
			p.waitFor();
		} catch (InterruptedException e)
		{
			p.destroy();
			throw new TimeoutException(commands[0] + "did not return after "+this.timeout+" milliseconds");
		} finally {
			timer.cancel();
		}
		String output = getOutputAsString(p.getErrorStream());
		if (output==null || output.trim().length()<1) { 
			output = getOutputAsString(p.getInputStream());
		}
		if (output==null)
			output="";
		
		return output;
			
	
	}

	private class InterruptScheduler extends TimerTask
	{
		Thread target = null;
		public InterruptScheduler(Thread target)
		{
			this.target = target;
		}
		
		@Override
		public void run()
		{
			target.interrupt();
		}
	}

}
