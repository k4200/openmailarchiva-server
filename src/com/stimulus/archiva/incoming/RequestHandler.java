package com.stimulus.archiva.incoming;
import com.stimulus.archiva.domain.*;
import java.nio.channels.*;

public interface RequestHandler
{

    public void handleRequest( SocketChannel socket, FetchMessageCallback callback);
  
}