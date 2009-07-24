
/**
 * SimpleAPICallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4  Built on : Apr 26, 2008 (06:24:30 EDT)
 */

    package com.stimulus.archiva.webservice;

    /**
     *  SimpleAPICallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class SimpleAPICallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public SimpleAPICallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public SimpleAPICallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for login method
            * override this method for handling normal response from login operation
            */
           public void receiveResultlogin(
                    com.stimulus.archiva.webservice.SimpleAPIStub.LoginResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from login operation
           */
            public void receiveErrorlogin(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getMessageByID method
            * override this method for handling normal response from getMessageByID operation
            */
           public void receiveResultgetMessageByID(
                    com.stimulus.archiva.webservice.SimpleAPIStub.GetMessageByIDResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getMessageByID operation
           */
            public void receiveErrorgetMessageByID(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for updateConfiguration method
            * override this method for handling normal response from updateConfiguration operation
            */
           public void receiveResultupdateConfiguration(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from updateConfiguration operation
           */
            public void receiveErrorupdateConfiguration(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for searchMessage method
            * override this method for handling normal response from searchMessage operation
            */
           public void receiveResultsearchMessage(
                    com.stimulus.archiva.webservice.SimpleAPIStub.SearchMessageResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from searchMessage operation
           */
            public void receiveErrorsearchMessage(java.lang.Exception e) {
            }
                


    }
    