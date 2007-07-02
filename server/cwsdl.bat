set AXIS_HOME=C:\dev\sdk\axis
set AXIS_LIB=%AXIS_HOME%\lib\axisjava
set AXIS_CLASSPATH=%AXIS_LIB%\axis.jar;%AXIS_HOME%\lib\axis\wsdl2ws.jar;.;%AXIS_LIB%\xml-apis.jar;%AXIS_LIB%\jaxrpc.jar;%AXIS_LIB%\wsdl4j.jar;%AXIS_LIB%\log4j-1.2.8.jar;%AXIS_LIB%\commons-discovery.jar;%AXIS_LIB%\commons-logging.jar;%AXIS_LIB%\wsdl4j.jar;%AXIS_LIB%\saaj.jar;C:\dev\workspace\archiva\lib\activation.jar;C:\dev\workspace\archiva\lib\mail.jar;%CLASSPATH%
java -cp %AXIS_CLASSPATH% org.apache.axis.wsdl.wsdl2ws.WSDL2Ws mailarchiva.wsdl -o./ClientOut -lc++ -sclient 
