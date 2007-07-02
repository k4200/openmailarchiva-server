/*
MailArchiva Open Source Edition
Copyright (C) 2005 Jamie Angus Band 

This software program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
#include "Archiva.hpp"

#include <axis/AxisWrapperAPI.hpp>
#include <string.h>

using namespace std;

 Archiva::Archiva(const char* pchEndpointUri, AXIS_PROTOCOL_TYPE eProtocol)
:Stub(pchEndpointUri, eProtocol)
{
}

Archiva::Archiva()
:Stub(" ", APTHTTP1_1)
{
	m_pCall->setEndpointURI("http://localhost:8080/archiva/services/Archiva");
}

Archiva::~Archiva()
{
}


/*Methods corresponding to the web service methods*/

/*
 * This method wrap the service method getServerVersion
 */
xsd__string Archiva::getServerVersion()
{
	xsd__string Ret;
	memset(&Ret,0,sizeof(xsd__string));
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return Ret;
	if (NULL==m_pCall->getTransportProperty("SOAPAction",false))
	{
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "");
	}
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("getServerVersion", "urn:Archiva");
		applyUserPreferences();
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("getServerVersionResponse", "urn:Archiva"))
			{
				Ret = m_pCall->getElementAsString("getServerVersionReturn", 0);
			}
		}
	m_pCall->unInitialize();
		return Ret;
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost:8080/archiva/services/Archiva" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}


/*
 * This method wrap the service method storeMessage
 */
void Archiva::storeMessage(xsd__base64Binary Value0)
{
	const char* pcCmplxFaultName;
	try
	{
		if (AXIS_SUCCESS != m_pCall->initialize(CPP_RPC_PROVIDER)) 
			return ;
	if (NULL==m_pCall->getTransportProperty("SOAPAction",false))
	{
		m_pCall->setTransportProperty(SOAPACTION_HEADER , "");
	}
		m_pCall->setSOAPVersion(SOAP_VER_1_1);
		m_pCall->setOperation("storeMessage", "urn:Archiva");
		applyUserPreferences();
		m_pCall->addParameter((void*)&Value0, "in0", XSD_BASE64BINARY);
		if (AXIS_SUCCESS == m_pCall->invoke())
		{
			if(AXIS_SUCCESS == m_pCall->checkMessage("storeMessageResponse", "urn:Archiva"))
			{
			/*not successful*/
		}
		}
	m_pCall->unInitialize();
	}
	catch(AxisException& e)
	{
		int iExceptionCode = e.getExceptionCode();
		if(AXISC_NODE_VALUE_MISMATCH_EXCEPTION != iExceptionCode)
		{
			throw SoapFaultException(e);
		}
		ISoapFault* pSoapFault = (ISoapFault*)
			m_pCall->checkFault("Fault","http://localhost:8080/archiva/services/Archiva" );
		if(pSoapFault)
		{
			m_pCall->unInitialize();
			throw SoapFaultException(e);
		}
		else throw;
	}
}

