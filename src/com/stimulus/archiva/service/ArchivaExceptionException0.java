
/**
 * ArchivaExceptionException0.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4  Built on : Apr 26, 2008 (06:24:30 EDT)
 */

package com.stimulus.archiva.service;

public class ArchivaExceptionException0 extends java.lang.Exception{
    
    private com.stimulus.archiva.service.SearchServiceStub.ArchivaExceptionE faultMessage;
    
    public ArchivaExceptionException0() {
        super("ArchivaExceptionException0");
    }
           
    public ArchivaExceptionException0(java.lang.String s) {
       super(s);
    }
    
    public ArchivaExceptionException0(java.lang.String s, java.lang.Throwable ex) {
      super(s, ex);
    }
    
    public void setFaultMessage(com.stimulus.archiva.service.SearchServiceStub.ArchivaExceptionE msg){
       faultMessage = msg;
    }
    
    public com.stimulus.archiva.service.SearchServiceStub.ArchivaExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    