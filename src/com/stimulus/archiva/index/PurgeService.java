package com.stimulus.archiva.index;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.stimulus.archiva.domain.Service;
import com.stimulus.archiva.domain.ServiceDelegate;
import com.stimulus.archiva.domain.Service.Status;

public class PurgeService implements Service {
	protected ServiceDelegate serviceDelegate;
	protected static final Log logger = LogFactory.getLog(PurgeService.class);
	protected static final Log audit = LogFactory.getLog("com.stimulus.archiva.audit");
	protected static ScheduledExecutorService scheduler;
	protected static ScheduledFuture<?> scheduledTask;
	protected Status status = Status.STOPPED;

	public PurgeService() {
		serviceDelegate = new ServiceDelegate("purge service", this, logger);
	}

	@Override
	public void startup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prepareShutdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getServiceName() {
		return serviceDelegate.getServiceName();
	}

	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reloadConfig() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return false;
	}

}
