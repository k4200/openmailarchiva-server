package com.stimulus.archiva.incoming;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.sendmail.jilter.JilterHandler;
import com.sendmail.jilter.JilterProcessor;

public class JilterRunnable implements Runnable
{
    protected static Logger logger = Logger.getLogger(JilterRunnable.class);
    protected SocketChannel socket = null;
    protected JilterProcessor processor = null;

    public JilterRunnable(SocketChannel socket, JilterHandler handler) throws IOException {
        this.socket = socket;
        this.socket.configureBlocking(true);
        this.processor = new JilterProcessor(handler);
    }

    public void run() {
        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(4096);

        try {
            while (this.processor.process(this.socket, (ByteBuffer) dataBuffer.flip())) {
                dataBuffer.compact();
                if (this.socket.read(dataBuffer) == -1) {
                    logger.debug("socket reports EOF, exiting read loop");
                    break;
                }
            }
        }
        catch (IOException e) {
            logger.debug("Unexpected exception, connection will be closed", e);
        } finally {
            logger.debug("closing processor");
            this.processor.close();
            logger.debug("processor closed");
            try {
                logger.debug("closing socket");
                this.socket.close();
                logger.debug("socket closed");
            } catch (IOException e) {
                logger.debug("Unexpected exception", e);
            }
        }
    }
}
