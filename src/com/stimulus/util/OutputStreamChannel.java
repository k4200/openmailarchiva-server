package com.stimulus.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;

public class OutputStreamChannel implements GatheringByteChannel {

  
    public static final int MAX_BUFFER = 10240;

    private final OutputStream out;

    private volatile boolean closed;

    private OutputStreamChannel(final OutputStream out) {
        super();
        this.out = out;
    }

    public static GatheringByteChannel getChannel(OutputStream out) {
        if (out instanceof FileOutputStream) {
            return ((FileOutputStream) out).getChannel();
        } else {
            return new OutputStreamChannel(out);
        }
    }

    public int write(ByteBuffer src) throws IOException {
        return (int) write(new ByteBuffer[] { src }, 0, 1);
    }

    public long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }

    public synchronized long write(ByteBuffer[] srcs, int offset, int length)
            throws IOException {
        long written = 0;
        for (int i = offset; i < length; i++) {
            long len = srcs[i].limit() - srcs[i].position();
            written += len;

            // allocate a buffer of at most 1MB
            byte[] buf = new byte[(int) Math.min(len, MAX_BUFFER)];
            while (len > 0) {
                int count = (int) Math.min(buf.length, len);
                srcs[i].get(buf, 0, count);
                len -= count;
                out.write(buf, 0, count);
            }
        }
        return written;
    }

    public synchronized void close() throws IOException {
        out.close();
        closed = true;
    }

    public boolean isOpen() {
        return !closed;
    }

}
