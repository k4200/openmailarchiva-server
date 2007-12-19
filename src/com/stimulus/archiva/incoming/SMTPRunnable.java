package com.stimulus.archiva.incoming;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.sendmail.jilter.JilterProcessor;
import com.stimulus.archiva.domain.Agent;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.exception.ArchiveException;
import com.stimulus.archiva.exception.SMTPServerException;
import com.stimulus.util.Compare;

public class SMTPRunnable implements Runnable
{
    protected static Logger logger = Logger.getLogger(SMTPRunnable.class);
    protected SocketChannel socket = null;
    protected JilterProcessor processor = null;
    protected StoreMessageCallback callback;
    
    private PrintWriter out;
    private BufferedReader in;
    private int errors = 0;
    private boolean hello = false;
    
    private static int SMTP_TIMEOUT = 600000;
    private static final String MESSAGE_PERMANENT_PROCESSING_ERROR = "554 Failed to archive message; ";
    private static final String MESSAGE_TEMPORARY_PROCESSING_ERROR = "451 Failed to archive message; ";
    private static final String MESSAGE_NOTRECOGNIZED  = "502 Syntax error, command unrecognized.";
    private static final String MESSAGE_NOTIMPLEMENTED = "502 Command not implemented.";
    private static final String MESSAGE_POLICY_DISALLOW = "550 Requested action not taken: command rejected for policy reasons";
    private static final String WELCOME_MESSAGE = "220";
    private static final String MESSAGE_BAD_COMMAND_SEQUENCE = "503 Bad sequence of commands";
    private static final String MESSAGE_DISCONNECT = "221";
    private static final String MESSAGE_ERROR = "421";
    private static final String MESSAGE_OK = "250 OK";
    private static final String MESSAGE_SEND_DATA = "354 Start mail input; end with <CRLF>.<CRLF>";
    private static final String MESSAGE_SAVE_MESSAGE_ERROR = "500 Error:";
    private static final String COMMAND_HELO = "HELO";
    private static final String COMMAND_EHLO = "EHLO";
    private static final String COMMAND_RSET = "RSET";
    private static final String COMMAND_NOOP = "NOOP";
    private static final String COMMAND_QUIT = "QUIT";
    private static final String COMMAND_MAIL_FROM = "MAIL";
    private static final String COMMAND_RCPT_TO = "RCPT";
    private static final String COMMAND_DATA = "DATA";
    private static String domain = "";
    
    private static final String[] allCommands = {COMMAND_HELO,COMMAND_EHLO,COMMAND_RSET,COMMAND_NOOP,
    											 COMMAND_QUIT,COMMAND_MAIL_FROM,COMMAND_RCPT_TO,COMMAND_DATA};  

    public SMTPRunnable(SocketChannel socket, StoreMessageCallback callback) throws IOException {
        this.socket = socket;
        this.socket.configureBlocking(true);
        this.socket.socket().setSoTimeout(SMTP_TIMEOUT);
        this.callback = callback;
    }

    public void run() {
    	
		try {
			
			out = new PrintWriter(socket.socket().getOutputStream(), true);
		    in = new BufferedReader(new InputStreamReader( socket.socket().getInputStream() ));
		    domain = getLocalHost();
		    write( WELCOME_MESSAGE + " "+domain+" MailArchiva v" + Config.getApplicationVersion() + " SMTP Service Ready");
		    handleCommands();
		    
		}  catch(Exception e ) {
			
			 logger.debug( "smtp server exception occurred", e );
			 if (e.getCause() instanceof SocketTimeoutException) {
				 write(MESSAGE_ERROR + " Error: timeout exceeded");
			 } else
				 write(MESSAGE_ERROR + " Error: " + e.getMessage());
			 disconnect();
			 return;
		}
		try {
		    write( MESSAGE_DISCONNECT + " " + domain + " SMTP Service closing transmission channel");
		} catch( Exception e1 ) {
		    logger.debug( "could not send disconnect message", e1 );
		}
		disconnect();
	}
    
    
    
    protected void handleCommands() throws SMTPServerException {
        String inputString;
        String command;
        String argument;
 
        while( true ) {

        	if (errors>10)
        		throw new SMTPServerException("smtp maximum errors exceeded. shutdown connection. will be retried later.",logger);
        	
            inputString = read();
            
            if (inputString==null)
            	break;
            
            command = parseCommand( inputString );
            argument = parseArgument( inputString );
            
            if (command==null || command.length()<1) {
            	write(MESSAGE_SAVE_MESSAGE_ERROR+" Bad syntax");
            	errors += 1;
            	continue;
            }
            	
            if (!isValidCommand(command)) {
            	write(MESSAGE_NOTRECOGNIZED);
            	errors += 1;
            	continue;
            }
            
            errors = 0;
            
            if( command.equals( COMMAND_QUIT ) )
            	break;
            	
            if( command.equals( COMMAND_HELO ) || 
                command.equals( COMMAND_EHLO )) {
	            	InetAddress remoteAddress = socket.socket().getInetAddress();
	            	String clientIp = remoteAddress.getHostAddress();
	            	Agent agent = Config.getConfig().getAgent();
	            	if (agent.isAllowed(remoteAddress)) {
	            		write( "250 Hello " + argument );
	            		hello = true;
	                    continue;
	            	} else {
	            		write(MESSAGE_POLICY_DISALLOW);
	            		break;
	            	}
            }
            
            if( command.equals( COMMAND_DATA ) ) {
            	if (hello) {
	            	handleData();
	            	continue;
            	} else {
            		write(MESSAGE_BAD_COMMAND_SEQUENCE);
            		continue;
            	}
            }
            	
            write( MESSAGE_OK );
            
        }
        
        
    }
    

    protected void handleData() throws SMTPServerException {
        write( MESSAGE_SEND_DATA );
        SMTPDataHandler dataHandler = new SMTPDataHandler("SMTPDataHandler",socket.socket(),in,callback);
        dataHandler.start();
        try { dataHandler.join(SMTP_TIMEOUT); } catch (InterruptedException ie) {};
        if (dataHandler.isAlive()) {
        	 dataHandler.interrupt();
           	 logger.debug("smtp data handler blocked thread interrupted");
	         try { dataHandler.join(200); } catch(Exception e) {};
       		 if (dataHandler.isAlive()) {
       			  logger.debug("smtp data handler set thread min priority");
       			  dataHandler.setPriority(Thread.MIN_PRIORITY);
       			  dataHandler.stop();
       			  try { Thread.sleep(100); } catch (Exception e) {}
       			  if (dataHandler.isAlive())
       				 logger.debug("smtp data handler stuck thread still alive");
       			  else
       				 logger.debug("smtp data handler stuck thread thankfully stopped");
       		 }
       		 throw new SMTPServerException("smtp data transmit timeout exceeded. closing connection.",logger);
       	 }
        if (dataHandler.getError()) {
        	ArchiveException.RecoveryDirective recoveryDirective = dataHandler.getCause().getRecoveryDirective();
        	if (recoveryDirective == ArchiveException.RecoveryDirective.RETRYLATER)
        		write( MESSAGE_TEMPORARY_PROCESSING_ERROR + " " + dataHandler.getCause().getMessage());  
        	else if (recoveryDirective == ArchiveException.RecoveryDirective.REJECT)
        		write( MESSAGE_PERMANENT_PROCESSING_ERROR + " " + dataHandler.getCause().getMessage());  
        } else
        	write( MESSAGE_OK );
    }

    protected String read() throws SMTPServerException {
        try {
            String inputLine = in.readLine();
            
            if (inputLine!=null) {
            	inputLine=inputLine.trim();
            	logger.debug( "smtp read:" + inputLine );
            }
            return inputLine;
        }
        catch( IOException ioe ) {
            logger.debug( "failed to read from socket.", ioe );
            throw new SMTPServerException(ioe.getMessage(),ioe,logger);
        }
    }

  
    protected void write( String message ) {

		logger.debug( "smtp server writing: " + message );
        out.print( message + "\r\n" );
        out.flush();
    }

   
    protected String parseCommand( String inputString ) {

        int index = inputString.indexOf( " " );

        if( index == -1 ) {
            return inputString.toUpperCase(Locale.ENGLISH);
        } else {
            return inputString.substring( 0, index ).toUpperCase(Locale.ENGLISH);
        }
    }

    protected String parseArgument( String inputString ) {

        int index = inputString.indexOf( " " );

        if( index == -1 ) {
            return "";
        } else {
            return inputString.substring( index + 1 );
        }
    }
    
    protected boolean isValidCommand( String command ) {
    	boolean valid = false;
    	for (String com: allCommands) {
    		if (Compare.equalsIgnoreCase(com, command))
    			valid = true;
    	}
    	return valid;
    }
    
    protected void disconnect() {
    	 try {
	            if( socket != null ) {
	                socket.close();
	                if (!socket.isConnected())
	                	logger.debug( "smtp socket successfully disconnected");
	                else
	                	logger.debug( "smtp socket is still open!");
	            }
	     } catch( IOException ioe ) {
	         logger.debug( "failed to disconnect stmp connection", ioe );
	     }
    }
    
    protected String getLocalHost() {
    	try
    	{
    		InetAddress localHost = InetAddress.getLocalHost();
    		return localHost.getCanonicalHostName();
    	} catch(java.net.UnknownHostException uhe)
    	{
    		return "";
    	}
    }
    
}
