set AXIS_HOME=C:\dev\sdk\axis
set AXIS_LIB=%AXIS_HOME%\lib\axisjava
set AXISCLASSPATH=%AXIS_LIB%\axis.jar;%AXIS_LIB%\commons-discovery.jar;%AXIS_LIB%\commons-logging.jar;%AXIS_LIB%\jaxrpc.jar;%AXIS_LIB%\saaj.jar;%AXIS_LIB%\log4j-1.2.8.jar;%AXIS_LIB%\xml-apis.jar;%AXIS_LIB%\xercesImpl.jar;%AXIS_LIB%\wsdl4j.jar;"C:\Program Files\Apache Software Foundation\Tomcat 5.5\webapps\archiva\WEB-INF\classes"
java -cp %AXISCLASSPATH% -Dcatalina_home="C:\Program Files\Stimulus\archiva\Server" org.apache.axis.wsdl.Java2WSDL -o archiva.wsdl -l"http://localhost:8080/archiva/services/Archiva" -n "urn:Archiva" -p"com.stimulus.archiva.webservice" "urn:Archiva" com.stimulus.archiva.webservice.Archiva
