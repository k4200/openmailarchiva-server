package com.stimulus.archiva.monitor;

import java.util.*;

public class Event {
	
	  public enum Category { EXCEPTION, SPACE, MAILFLOW, VOLSTATUS, LICENSE, NOARCHIVE }
		
	  protected static LinkedList<Event> events = new LinkedList<Event>();
	  protected String message = null;
	  protected Date time = null;
	  protected Category category = null;
	  
	  public Event(String message,Category category) {
		  this.message = message;
		  time = new Date();
		  this.category = category;
	  }
	  
	  public String getMessage() { return message; }
	  
	  public Date getTime() { return time; }
	  
	  public Category getCategory() { return category; }


	public static void notifyEvent(String eventDescription, Category category) {
		  if (eventDescription==null) 
			  return;
		  int total = 0;
		  ArrayList<Event> delEvents = new ArrayList<Event>();
		  for (Event event : events) {
			  if (event.getCategory()==category) {
				  total++;
				  if (total > 5 ) {
					  delEvents.add(event);
				  }
			  }
		  }
		  for (Event event : delEvents)
			  events.remove(event);
		  
		  events.add(new Event(eventDescription,category));
	}
	
	public static List<Event> getEvents() { return events; }
}