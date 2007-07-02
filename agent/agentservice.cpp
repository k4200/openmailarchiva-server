/*
MailArchiva Open Source Edition
Copyright (C) 2005 Jamie Angus Band 

This software program is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published by the 
Free Software Foundation; either version 2.1 of the License, or (at your option)
any later version.
 
This library is distributed in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 
You should have received a copy of the GNU Lesser General Public License along
with this library; if not, write to the Free Software Foundation, Inc., 
51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

#include "stdafx.h"
#include <stdio.h>
#include <tchar.h>
#include <mapi.h>
#include <mapix.h>
#include <mapiutil.h>
#include <mspst.h>
#include "MailArchiva.hpp"
#include <axis/AxisException.hpp>
#include <tchar.h>
#include <math.h>
#include "GZipHelper.h"
#include "MailexAgent.h"
#include "Register.h"
#include <edkmdb.h>
#include <msi.h>
#include "MyIStream.cpp"
#include "ConverterSession.h"

DEFINE_GUID(CLSID_IConverterSession, 0x4e3a7680, 0xb77a, 0x11d0, 0x9d, 0xa5, 0x0, 0xc0, 0x4f, 0xd6, 0x56, 0x85);
DEFINE_GUID(IID_IConverterSession, 0x4b401570, 0xb77b, 0x11d0, 0x9d, 0xa5, 0x0, 0xc0, 0x4f, 0xd6, 0x56, 0x85);
#define MSG_ERR_EXIST                         ((DWORD)0xC0000004L)

TCHAR* gszServiceName = TEXT("MailArchiva Agent");
TCHAR* m_agentRegistryKey = TEXT("Software\\MailArchiva\\Agent");
IConverterSession* pConverterSession	  = NULL;
SERVICE_STATUS serviceStatus;
SERVICE_STATUS_HANDLE serviceStatusHandle = 0;
HANDLE ServiceControlEvent				  = 0;
HANDLE hThread							  = NULL;
LHANDLE						m_hSession;            //Mapi Session handle
LPMAPISESSION				m_lpMAPISession	 = NULL;
LPMDB						m_lpMDB			 = NULL;
LPMAPIFOLDER				m_lpInboxFolder  = NULL;
HANDLE						m_hEventSource   = NULL;
TCHAR						m_url[255]			 = "";
TCHAR						m_profileName[255]   = TEXT("mailarchiva");
TCHAR						m_password[255]		 = TEXT("");
TCHAR						m_fullLoginName[255] = TEXT("");
TCHAR						m_exchange[255]		 = TEXT("");
TCHAR						m_MAPILoginType[255] = TEXT("Profile");
TCHAR						m_userName[255]		 = TEXT("");
TCHAR						m_domain[255]		 = TEXT("");
HANDLE hToken				= 0;
bool notRespondingNotice    = false;
CRITICAL_SECTION			m_ProcessMessageCriticalSection;

DWORD WINAPI ThreadProc(LPVOID lpParameter);
void SetTheServiceStatus(DWORD dwCurrentState, DWORD dwWin32ExitCode, DWORD dwCheckPoint,   DWORD dwWaitHint);
STDMETHODIMP ListMessages(LPMDB lpMDB,LPMAPIFOLDER lpInboxFolder,LPMAPISESSION lpSession);
STDMETHODIMP OpenDefaultMessageStore(LPMAPISESSION lpMAPISession,LPMDB * lpMDB);
STDMETHODIMP OpenInbox(LPMDB lpMDB,LPMAPIFOLDER *lpInboxFolder);
HRESULT GetOutlookVersionString(LPTSTR* ppszVer);

bool SendMessages();
void Deinitialize();
bool Initialize();
DWORD InstallService();
DWORD UninstallService();
void RunService();
void AddEventSource( PCTSTR pszName, DWORD dwCategoryCount /* =0 */ );
void RemoveEventSource( PCTSTR pszName );
void showHelp();
DWORD StopAgentService(SC_HANDLE hSCM,SC_HANDLE hService,BOOL fStopDependencies,DWORD dwTimeout );
DWORD StartAgentService();
BOOL Impersonate(TCHAR* szUserName, TCHAR* szDomain, TCHAR* szPassword);
BOOL CreateMAPIProfile(TCHAR* szProfile, TCHAR* szMailbox, TCHAR* szServer);
void ErrorStopService(DWORD category, TCHAR* friendlyErrorMsg, BOOL reportLastError);
void ReportError(DWORD category, TCHAR* friendlyErrorMessage, BOOL reportLastError);
void CheckOutlookVersion();
bool InitMAPI();
void DeInitMAPI();

int _cdecl _tmain( int argc, TCHAR* argv[] )
{

	if ( argc > 3 && 
	     ((lstrcmpi( argv[1], TEXT("-installprofile")) == 0) || 
		 (lstrcmpi( argv[1], TEXT("-configprofile")) == 0) || 
		 (lstrcmpi( argv[1], TEXT("-installexchange")) == 0) ||
		 (lstrcmpi( argv[1], TEXT("-configexchange")) == 0) ))
	{
		if (strlen(argv[2])>255 || strlen(argv[3])>255) 
		{
			cout << "illegal argument size (greater than 255 characters in length)\n\n";
			return -1;
		}
		lstrcat(m_url,"http://");
		TCHAR* pdest = strchr( argv[2], '/');
		int urilen = (int)(pdest - argv[2]);
		if (urilen<1)
			urilen = strlen(argv[2]);
		TCHAR temp[255] = "";
		strncpy(temp, argv[2], urilen);
		lstrcat(m_url,temp);
		if (strstr( argv[2], ":")==0) {
			lstrcat(m_url,":8090");
		}
		lstrcat(m_url,&(argv[2][urilen]));
		
		if (m_url[strlen(m_url)-1]=='/')
			lstrcat(m_url,"services/MailArchiva");
		else
			lstrcat(m_url,"/services/MailArchiva");

		if ((lstrcmpi( argv[1], TEXT("-installprofile")) == 0) || 
			 (lstrcmpi( argv[1], TEXT("-configprofile")) == 0)) 
		{
			lstrcpy(m_profileName, argv[3]);
			lstrcpy(m_MAPILoginType,TEXT("Profile"));
		} else {
			lstrcpy(m_exchange, argv[3]);
			lstrcpy(m_MAPILoginType,TEXT("Exchange"));
		}

		lstrcpy(m_fullLoginName, argv[4]);
		lstrcpy(m_password, argv[5]);

		TCHAR* pdest2 = strchr(m_fullLoginName, '@')-1;
		int domainlen = (int)(pdest2 - m_fullLoginName);
		
		if (domainlen<0) {
			cout << "\nInvalid loginname parameter. Must be in the format username@domain\n\n";
			return -1;
		} 

		CRegister registry(m_agentRegistryKey);
		registry.setRegValue("url",m_url,strlen(m_url));
		registry.setRegValue("login",m_fullLoginName,strlen(m_fullLoginName));
		registry.setRegValue("p",m_password,strlen(m_password));
		registry.setRegValue("MAPILoginType",m_MAPILoginType,strlen(m_MAPILoginType));

		if (lstrcmpi( m_MAPILoginType, TEXT("Profile"))==0)
			registry.setRegValue("profile",m_profileName,strlen(m_profileName));
		else
			registry.setRegValue("exchange",m_exchange,strlen(m_exchange));

		if (lstrcmpi( argv[1], TEXT("-installprofile"))==0 ||
		    lstrcmpi( argv[1], TEXT("-installexchange"))==0)
			return InstallService();
		else
			cout << "\nAgent configuration changed.\n\n";
	}
	else if ( argc > 1 && lstrcmpi( argv[1], TEXT("-uninstall") ) == 0 )
	{
		return UninstallService();
	} else if ( argc > 1 && lstrcmpi( argv[1], TEXT("-start") ) == 0 )
	{
		return StartAgentService();
	} else if (argc == 1)
	{
		CRegister registry(m_agentRegistryKey);
		DWORD size = 0;
		BOOL success = registry.getRegValue(m_url, "url",&size);
		success = registry.getRegValue(m_fullLoginName, "login",&size);
		success = registry.getRegValue(m_password, "p",&size);
		success = registry.getRegValue(m_MAPILoginType, "MAPILoginType",&size) && success;
		
		if (lstrcmpi( m_MAPILoginType, TEXT("Profile"))==0)
			success = registry.getRegValue(m_profileName, "profile",&size) && success;
		else
			success = registry.getRegValue(m_exchange, "exchange",&size) && success;

		TCHAR* pdest3 = strchr( m_fullLoginName, '@');
		int usernamelen = (int)(pdest3 - m_fullLoginName);
		if (usernamelen >0) {
			strncpy(m_userName, m_fullLoginName, usernamelen);
			strcpy(m_domain, pdest3+1);
		} else 
			success = false;
		if (!success)
		{
			cout << "The service could not be executed as it is not installed incorrectly.\n\n";
			return -1;
		} 
        RunService();	
	} else 
	{
		showHelp();
		return -1;
	}
	return 0;
}

void showHelp() 
{
	cout << "\n\nMailArchiva Agent v1.1.1 Command Help (/?)\n\n";
	cout << "Usage: MailArchivaAgent -installprofile uri profile loginname password\n";
	cout << "Usage: MailArchivaAgent -installexchange uri exchange loginname password\n";
	cout << "       MailArchivaAgent -configprofile uri profile loginname password\n";
	cout << "       MailArchivaAgent -configexchange uri exchange loginname password\n";
	cout << "       MailArchivaAgent -uninstall\n";
	cout << "       MailArchivaAgent -start\n";
	cout << "       MailArchivaAgent /?\n\n";
	cout << "Argument/s:\n\n";
	cout << "       -installexchange Installs agent service using exchange address\n";
	cout << "          uri           URI of server (e.g. mailarchiva.company.com/mailarchiva)\n";
	cout << "          exchange      Network address for exchange server\n";
	cout << "          loginname     Windows login name for journalling account\n";
	cout << "          password      Windows password for journalling account\n";
	cout << "       -installprofile  Installs agent service using a mapi profile (see -installexchange arguments)\n";
	cout << "          profile       MAPI profile name for journalling mailbox (e.g. mailarchiva)\n";
	cout << "       -configprofile   Configure agent service (see -installprofile arguments)\n";
	cout << "       -configexchange  Configure agent service (see -configexchange arguments)\n";
	cout << "       -uninstall       Uninstall agent service\n\n";
	cout << "Example Usage:\n\n";
	cout << "       MailArchivaAgent -installprofile mailarchiva.company.com/mailarchiva archiva journal@company.com psswd_hty*1\n\n";
	cout << "       MailArchivaAgent -installexchange mailarchiva.company.com/mailarchiva exchange.company.com journal@company.com psswd_hty*1\n\n";
}

void WINAPI ServiceControlHandler( DWORD controlCode )
{
 switch ( controlCode )
  {
	case SERVICE_CONTROL_INTERROGATE:
	break;

	case SERVICE_CONTROL_SHUTDOWN:
	case SERVICE_CONTROL_STOP:
    	SetTheServiceStatus(SERVICE_STOPPED, NO_ERROR,0,0);
		SetEvent( ServiceControlEvent );
		return;

	case SERVICE_CONTROL_PAUSE:
		break;

	case SERVICE_CONTROL_CONTINUE:
		break;

	default:
		if ( controlCode >= 128 && controlCode <= 255 )
			break;
		else
			break;
	}
	SetServiceStatus( serviceStatusHandle, &serviceStatus );
}

void WINAPI ServiceMain( DWORD /*argc*/, TCHAR* /*argv*/[] )
{
	DWORD ThreadId;
	DWORD dwWaitRes;

	serviceStatusHandle = RegisterServiceCtrlHandler( gszServiceName,ServiceControlHandler );

	if ( serviceStatusHandle )
	{
		SetTheServiceStatus(SERVICE_START_PENDING, NO_ERROR,0,0);

 		ServiceControlEvent = CreateEvent( 0, FALSE, FALSE, 0 );

		if (!Initialize()) {
			ErrorStopService(FATAL_ERROR,_T("Failed to initialize agent"),FALSE);
			goto exitservice;
		}

		hThread = CreateThread(NULL,0,ThreadProc,
									0,0,&ThreadId);
		if (hThread  == INVALID_HANDLE_VALUE) {
			ErrorStopService(FATAL_ERROR,_T("Failed to spawn thread for sending messages."),FALSE);
			goto exitservice;
		}
		SetTheServiceStatus(SERVICE_RUNNING, NO_ERROR,0,0);
		
		WaitForSingleObject( ServiceControlEvent, INFINITE );

		dwWaitRes = WaitForSingleObject(hThread,5000);

		if((dwWaitRes == WAIT_FAILED)||(dwWaitRes==WAIT_ABANDONED)) {
			ErrorStopService(FATAL_ERROR,_T("Could not continue waiting for service."),FALSE);
			goto exitservice;
		}
		else
			SetTheServiceStatus(SERVICE_STOP_PENDING, 0, 0, 3000);

		SetTheServiceStatus(SERVICE_STOP_PENDING, NO_ERROR,0,0);
exitservice: 
		SetTheServiceStatus(SERVICE_STOPPED, NO_ERROR,0,0);
		CloseHandle(ServiceControlEvent);
		CloseHandle(hThread);
		CloseHandle(serviceStatusHandle);
		ReportEvent(m_hEventSource,EVENTLOG_INFORMATION_TYPE,0,APPLICATION_SHUTDOWN_INFORMATION,NULL,0,0,NULL,NULL);
		Deinitialize();
		return;
	}
}

void RunService()
{
	SERVICE_TABLE_ENTRY serviceTable[] =
	{
 		{ gszServiceName, ServiceMain },
		{ 0, 0 }
	};

	StartServiceCtrlDispatcher( serviceTable );
}


DWORD WINAPI ThreadProc(LPVOID lpParameter)
{
    INT nThreadNum = (INT)lpParameter;

    while(WaitForSingleObject(ServiceControlEvent, 1000) != WAIT_OBJECT_0)
    {
		Sleep(2000);
		SendMessages();
    }
    return 0;
}
 
void SetTheServiceStatus(DWORD dwCurrentState, DWORD dwWin32ExitCode,
                        DWORD dwCheckPoint,   DWORD dwWaitHint)
{
    SERVICE_STATUS ss;  // Current status of the service.

    // Disable control requests until the service is started.
    
	if (dwCurrentState == SERVICE_START_PENDING)
        ss.dwControlsAccepted = 0;
    else
        ss.dwControlsAccepted =
                    SERVICE_ACCEPT_STOP|SERVICE_ACCEPT_SHUTDOWN;
                   
    // Initialize ss structure.
    ss.dwServiceType             = SERVICE_WIN32_OWN_PROCESS;
    ss.dwServiceSpecificExitCode = 0;
    ss.dwCurrentState            = dwCurrentState;
    ss.dwWin32ExitCode           = dwWin32ExitCode;
    ss.dwCheckPoint              = dwCheckPoint;
    ss.dwWaitHint                = dwWaitHint;

    SetServiceStatus(serviceStatusHandle, &ss);
}


DWORD InstallService()
{
	DWORD dwError = NO_ERROR;
	AddEventSource( gszServiceName, 2 ); // do in setup (temporary)
	
	SC_HANDLE serviceControlManager = OpenSCManager( 0, 0,
	SC_MANAGER_CREATE_SERVICE );

	if ( serviceControlManager )
	{
		char path[ _MAX_PATH + 1 ];
		if ( GetModuleFileName( 0, path, sizeof(path)/sizeof(path[0]) ) > 0 )
		{
			SC_HANDLE service = CreateService( serviceControlManager,
				gszServiceName, gszServiceName,
				SERVICE_ALL_ACCESS, SERVICE_WIN32_OWN_PROCESS,
				SERVICE_AUTO_START, SERVICE_ERROR_IGNORE, path,
				0, 0, 0, 0, 0 );
			if ( service )
			{
				CloseServiceHandle( service );
				printf("Success. Agent is installed.\n");
			}
			else
			{
				DWORD dwError = GetLastError();
				if(dwError == ERROR_SERVICE_EXISTS)
				printf("Agent service already exists.\n");
				else
				printf("Agent could not be installed. Error Code %d\n", dwError);
			}
		}
		CloseServiceHandle( serviceControlManager );
	}
	return dwError;
}

HRESULT StartMAPI ( LPMAPISESSION *pSess )
{
	
    // Declare and initialize any variables used by MAPI.
    HRESULT hRes = S_OK;
    MAPIINIT_0 pMapiInit = { MAPI_INIT_VERSION, MAPI_NT_SERVICE };
    ULONG ulFlags = 0L;
    LPMAPISESSION pSvcSess = NULL;
    
	hRes = MAPIInitialize ( &pMapiInit );

    if ( !FAILED ( hRes ) )
    {
		ulFlags = MAPI_NO_MAIL |
					MAPI_NEW_SESSION |
					MAPI_EXPLICIT_PROFILE |
					MAPI_NT_SERVICE ;

		if (strlen(m_password)<1)
			hRes = MAPILogonEx ( 0L, m_profileName, NULL, ulFlags, &pSvcSess);
		else
			hRes = MAPILogonEx ( 0L, m_profileName, m_password, ulFlags, &pSvcSess);
    }

    if ( !FAILED ( hRes ) )
    {
	 *pSess = pSvcSess;
	} else {
		CreateMAPIProfile(m_profileName,m_fullLoginName, m_exchange);
		if (strlen(m_password)<1)
			hRes = MAPILogonEx ( 0L, m_profileName, NULL, ulFlags, &pSvcSess);
		else
			hRes = MAPILogonEx ( 0L, m_profileName, m_password, ulFlags, &pSvcSess);

		if (FAILED ( hRes ) ) {
			PCTSTR aInsertions[] = { m_profileName };
			ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,MAPI_LOGON_ERROR,NULL,1,0,aInsertions,NULL);
		}
	}
    return hRes;
}

bool SendMessages()
{
    EnterCriticalSection(&m_ProcessMessageCriticalSection); 
		ListMessages(m_lpMDB,m_lpInboxFolder,m_lpMAPISession);
	LeaveCriticalSection(&m_ProcessMessageCriticalSection);
	return true;
}

STDMETHODIMP ListMessages(LPMDB lpMDB,LPMAPIFOLDER lpInboxFolder,LPMAPISESSION lpSession)
{
	DWORD i;
	HRESULT hRes				= S_OK;
	LPMAPITABLE lpContentsTable = NULL;
	LPSRowSet pRows				= NULL;
	LPSTREAM lpStream			= NULL;
	DWORD ulFileSize			= 0;
	void* pszBuffer				= NULL;
	BOOL release				= FALSE;

	enum {
		ePR_SENT_REPRESENTING_NAME,
		ePR_SUBJECT,
		ePR_BODY,
		ePR_PRIORITY,
		ePR_ENTRYID,
		ePR_TRANSPORT_MESSAGE_HEADERS,
		NUM_COLS};

	static SizedSPropTagArray(NUM_COLS,sptCols) = { NUM_COLS,
		PR_SENT_REPRESENTING_NAME,
		PR_SUBJECT,
		PR_BODY,
		PR_PRIORITY,
		PR_ENTRYID,
		PR_TRANSPORT_MESSAGE_HEADERS
	};

	
	hRes = lpInboxFolder->GetContentsTable(0,&lpContentsTable);
	if (FAILED(hRes)) {
		if (!notRespondingNotice) {
			ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,MAPI_INBOX_CONTENTS_ERROR,NULL,0,0,NULL,NULL);
			ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,EXCHANGE_NOT_RESPONDING,NULL,0,0,NULL,NULL);
			notRespondingNotice = true;
			// the exchange server may have restarted
		} 
		// try and fix the situation (exchange is very buggy)
		DeInitMAPI();
		Sleep(5000);
		InitMAPI();
		goto quit;
	} else if (notRespondingNotice) {
			ReportEvent(m_hEventSource,EVENTLOG_INFORMATION_TYPE,0,EXCHANGE_RESPONDING,NULL,0,0,NULL,NULL);
			notRespondingNotice = false; // we dont want to fillup the event viewer
	}

	hRes = HrQueryAllRows(lpContentsTable,(LPSPropTagArray) &sptCols,NULL,NULL,0,&pRows);

	if (FAILED(hRes)) { 
		ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,MAPI_INBOX_CONTENTS_ERROR,NULL,0,0,NULL,NULL);
		Sleep(5000);
		goto quit;
	}

	// initialize outlook api
	if (pRows->cRows>0) {
			if (SUCCEEDED(CoInitialize(NULL))) {
					if (!SUCCEEDED(CoCreateInstance(CLSID_IConverterSession, NULL, CLSCTX_INPROC_SERVER,IID_IConverterSession, (void**) &pConverterSession))) {
						ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,OUTLOOK_OUTLMIME_DLL,NULL,1,0,NULL,NULL);
						goto quit;
					} else
						release = TRUE;
			}
	}

	for (i = 0; i < pRows -> cRows; i++)
	{
		LPMESSAGE lpMessage = NULL;
		ULONG ulObjType		= NULL;
		LPSPropValue lpProp = NULL;
		BOOL error			= FALSE;

		hRes = lpMDB->OpenEntry(
				pRows->aRow[i].lpProps[ePR_ENTRYID].Value.bin.cb,
				(LPENTRYID) pRows->aRow[i].lpProps[ePR_ENTRYID].Value.bin.lpb,
				NULL,
				MAPI_BEST_ACCESS,
				&ulObjType,
				(LPUNKNOWN *) &lpMessage);
				
			if (!FAILED(hRes))
			{
			
				MyIStream* istream = new MyIStream; 			
				HRESULT hr = NULL;
				hr = pConverterSession->MAPIToMIMEStm(lpMessage,(LPSTREAM)istream,CCSF_SMTP);
				if (FAILED(hr)) goto failedconvert;

				istream->GetPtr(&pszBuffer);
				istream->GetSize(&ulFileSize);
	
				try
				{
					
					CA2GZIP gzip((char*)pszBuffer,ulFileSize);
					//memcpy(pszBuffer,gzip.pgzip,gzip.Length);
					//FILE *aout=fopen("c:\\test.gz","wb");
					//size_t count1=fwrite(gzip.pgzip,1,gzip.Length,aout);
					//fclose(aout);
					MailArchiva webservice(m_url);
					xsd__base64Binary bb;
					bb.set((xsd__unsignedByte*) gzip.pgzip, gzip.Length);
					//bb.__size = gzip.Length;
					//bb.__ptr = gzip.pgzip;
					
					webservice.storeMessage(bb);
				} catch (AxisException & e)
				{
					error = TRUE;
					PCTSTR aInsertions[] = { e.what() };
					ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,ARCHIVE_MESSAGE_ERROR,NULL,1,0,aInsertions,NULL);
				}
				catch (exception & e)
				{
					error = TRUE;
					PCTSTR aInsertions[] = { e.what() };
					ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,ARCHIVE_MESSAGE_ERROR,NULL,1,0,aInsertions,NULL);
				}
failedconvert:
				delete istream;
				MAPIFreeBuffer(pszBuffer);
			} else
			{
				ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,MAPI_OPEN_MESSAGE_ERROR,NULL,0,0,NULL,NULL);
				error = TRUE;
			}
			
			UlRelease(lpMessage);
			if (!error) {
				SBinary bin;
				bin = pRows->aRow[i].lpProps[ePR_ENTRYID].Value.bin;
				ENTRYLIST list;
				list.cValues = 1;
				list.lpbin = &bin;
				hRes = lpInboxFolder->DeleteMessages(&list,NULL,NULL,0);
			} else 
				Sleep(5000);
			MAPIFreeBuffer(lpProp);
		}

	quit:
	if (release)
		UlRelease(pConverterSession);
	FreeProws(pRows);
	UlRelease(lpContentsTable);


	return hRes;
}



   STDMETHODIMP OpenInbox(LPMDB lpMDB,LPMAPIFOLDER *lpInboxFolder)
   {
		ULONG        cbInbox;
		LPENTRYID    lpbInbox;
		ULONG        ulObjType;
		HRESULT      hRes = S_OK;
		LPMAPIFOLDER	lpTempFolder = NULL;
			
		*lpInboxFolder = NULL;

		//The Inbox is usually the default receive folder for the message store
		//You call this function as a shortcut to get it's Entry ID
		hRes = lpMDB->GetReceiveFolder(
			NULL,      //Get default receive folder
			NULL,      //Flags
			&cbInbox,  //Size and ...
			&lpbInbox, //Value of the EntryID to be returned
			NULL);     //You don't care to see the class returned
		if (FAILED(hRes)) goto quit;

		hRes = lpMDB->OpenEntry(
			cbInbox,                      //Size and...
			lpbInbox,                     //Value of the Inbox's EntryID
			NULL,                         //We want the default interface    (IMAPIFolder)
			MAPI_BEST_ACCESS,             //Flags
			&ulObjType,                   //Object returned type
			(LPUNKNOWN *) &lpTempFolder); //Returned folder
		if (FAILED(hRes)) goto quit;

		//Assign the out parameter
		*lpInboxFolder = lpTempFolder;

		//Always clean up your memory here!
		quit:
		MAPIFreeBuffer(lpbInbox);
		return hRes;
   }


   STDMETHODIMP OpenDefaultMessageStore(LPMAPISESSION lpMAPISession,LPMDB * lpMDB)
   {
		LPMAPITABLE pStoresTbl = NULL;
		LPSRowSet   pRow = NULL;
		static      SRestriction sres;
		SPropValue  spv;
		HRESULT     hRes;
		LPMDB       lpTempMDB = NULL;

		enum {EID, NAME, NUM_COLS};
		static SizedSPropTagArray(NUM_COLS,sptCols) = {NUM_COLS, PR_ENTRYID, PR_DISPLAY_NAME};

		*lpMDB = NULL;

		//Get the table of all the message stores available
		hRes = lpMAPISession -> GetMsgStoresTable(0, &pStoresTbl);
		if (FAILED(hRes)) goto quit;

		//Set up restriction for the default store
		sres.rt = RES_PROPERTY; //Comparing a property
		sres.res.resProperty.relop = RELOP_EQ; //Testing equality
		sres.res.resProperty.ulPropTag = PR_DEFAULT_STORE; //Tag to compare
		sres.res.resProperty.lpProp = &spv; //Prop tag and value to compare against

		spv.ulPropTag = PR_DEFAULT_STORE; //Tag type
		spv.Value.b   = TRUE; //Tag value

		//Convert the table to an array which can be stepped through
		//Only one message store should have PR_DEFAULT_STORE set to true, so only one will be returned
		hRes = HrQueryAllRows(
		pStoresTbl, //Table to query
		(LPSPropTagArray) &sptCols, //Which columns to get
		&sres, //Restriction to use
		NULL, //No sort order
		0, //Max number of rows (0 means no limit)
		&pRow); //Array to return
		if (FAILED(hRes)) goto quit;

		//Open the first returned (default) message store
		hRes = lpMAPISession->OpenMsgStore(
			NULL,//Window handle for dialogs
			pRow->aRow[0].lpProps[EID].Value.bin.cb,//size and...
			(LPENTRYID)pRow->aRow[0].lpProps[EID].Value.bin.lpb,//value of entry to open
			NULL,//Use default interface (IMsgStore) to open store
			MAPI_BEST_ACCESS,//Flags
			&lpTempMDB);//Pointer to place the store in
		if (FAILED(hRes)) goto quit;

		//Assign the out parameter
		*lpMDB = lpTempMDB;

		//Always clean up your memory here!
	quit:
		FreeProws(pRow);
		UlRelease(pStoresTbl);
		if (FAILED(hRes))
		{
			HRESULT hr;
			LPMAPIERROR lpError; 
			hr = lpMAPISession->GetLastError(hRes,0,&lpError);
			if (!hr)
			{
				MAPIFreeBuffer(lpError);
			}
		}
		return hRes;
   } 


bool Initialize()
{
	InitializeCriticalSection(&m_ProcessMessageCriticalSection);
  
	m_hEventSource = RegisterEventSource( NULL, gszServiceName );
	CheckOutlookVersion();
	
	try {
		MailArchiva webservice(m_url);
	} catch (AxisException & e)
	{
		PCTSTR aInsertions[] = { e.what () };
		ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,GET_CONFIGURATION_ERROR,NULL,1,0,aInsertions,NULL);
		goto quit;
	}
	
	if (!InitMAPI())
		goto quit;
	TCHAR szUserName[ 128 ];
	DWORD cchUserName = 128;
	GetUserName( szUserName, &cchUserName );
	PCTSTR aInsertions[] = { szUserName};
	ReportEvent(m_hEventSource,EVENTLOG_INFORMATION_TYPE,0,EVENT_STARTED_BY,NULL,1,0,aInsertions,NULL);
	return true;
quit:
	return false;
}	

bool InitMAPI() {
	if (!Impersonate(m_userName, m_domain, m_password))
			return false;
	HRESULT hRes;
	hRes = StartMAPI(&m_lpMAPISession);
	LPSPropValue  tmp = NULL;
	if (FAILED(hRes)) {
		ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,MAPI_START_ERROR,NULL,0,0,NULL,NULL);
		return false;
	}

	hRes = OpenDefaultMessageStore(m_lpMAPISession,&m_lpMDB);

	if (FAILED(hRes)) {
		ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,MAPI_OPEN_MESSAGESTORE_ERROR,NULL,0,0,NULL,NULL);
		return false;
	}
	hRes = OpenInbox(m_lpMDB,&m_lpInboxFolder);
	if (FAILED(hRes)) { 
		ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,MAPI_OPEN_INBOX_ERROR,NULL,0,0,NULL,NULL);
		return false;
	}
	hRes = HrGetOneProp(m_lpInboxFolder,PR_DISPLAY_NAME,&tmp);
	if (FAILED(hRes)) { 
		ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,MAPI_OPEN_INBOX_ERROR,NULL,0,0,NULL,NULL);
		return false;
	}
	if (tmp) MAPIFreeBuffer(tmp);
	return true;
}
void Deinitialize()
{
	CoUninitialize();
	DeInitMAPI();
	DeregisterEventSource( m_hEventSource );
	CloseHandle(m_hEventSource);
	DeleteCriticalSection(&m_ProcessMessageCriticalSection);
}

void DeInitMAPI() {
	CloseHandle(hToken); 
	UlRelease(m_lpInboxFolder);
	UlRelease(m_lpMDB);
	UlRelease(m_lpMAPISession);
	MAPIUninitialize();
}


void AddEventSource( PCTSTR pszName, DWORD dwCategoryCount /* =0 */ )
{
	HKEY	hRegKey = NULL; 
	DWORD	dwError = 0;
	TCHAR	szPath[ MAX_PATH ];
	
	_stprintf( szPath, _T("SYSTEM\\CurrentControlSet\\Services\\EventLog\\Application\\%s"), pszName );

	// Create the event source registry key
	dwError = RegCreateKey( HKEY_LOCAL_MACHINE, szPath, &hRegKey );

	// Name of the PE module that contains the message resource
	GetModuleFileName( NULL, szPath, MAX_PATH );

	// Register EventMessageFile
	dwError = RegSetValueEx( hRegKey, _T("EventMessageFile"), 0, REG_EXPAND_SZ, 
		                    (PBYTE) szPath, (_tcslen( szPath) + 1) * sizeof TCHAR ); 
	

	// Register supported event types
	DWORD dwTypes = EVENTLOG_ERROR_TYPE | EVENTLOG_WARNING_TYPE | EVENTLOG_INFORMATION_TYPE; 
	dwError = RegSetValueEx( hRegKey, _T("TypesSupported"),	0, REG_DWORD, 
		                    (LPBYTE) &dwTypes, sizeof dwTypes );

	// If we want to support event categories, we have also to register	the CategoryMessageFile.
	// and set CategoryCount. Note that categories need to have the message ids 1 to CategoryCount!

	if( dwCategoryCount > 0 ) {

		dwError = RegSetValueEx( hRegKey, _T("CategoryMessageFile"), 0, REG_EXPAND_SZ,
								 (PBYTE) szPath, (_tcslen( szPath) + 1) * sizeof TCHAR );

		dwError = RegSetValueEx( hRegKey, _T("CategoryCount"), 0, REG_DWORD, 
			                    (PBYTE) &dwCategoryCount, sizeof dwCategoryCount );
	}		
	RegCloseKey( hRegKey );
} 

// Removes the passed source of events from the registry
void RemoveEventSource( PCTSTR pszName )
{
	DWORD dwError = 0;
	TCHAR szPath[ MAX_PATH ];
	
	_stprintf( szPath, _T("SYSTEM\\CurrentControlSet\\Services\\EventLog\\Application\\%s"), pszName );
	dwError = RegDeleteKey( HKEY_LOCAL_MACHINE, szPath );
}


DWORD UninstallService()
{
	DWORD dwError = NO_ERROR;
	SC_HANDLE serviceControlManager = OpenSCManager( 0, 0,
	SC_MANAGER_CONNECT );

	if ( serviceControlManager )
	{
		SC_HANDLE service = OpenService( serviceControlManager,
			gszServiceName, SERVICE_QUERY_STATUS | SERVICE_STOP | DELETE );
		if ( service )
		{
			SERVICE_STATUS serviceStatus;
			if ( QueryServiceStatus( service, &serviceStatus ) )
			{
				ControlService( service, SERVICE_CONTROL_STOP,&serviceStatus );
				if ( serviceStatus.dwCurrentState != SERVICE_STOPPED )
					StopAgentService(serviceControlManager,service,TRUE, 5000);
				ControlService( service, SERVICE_CONTROL_STOP,&serviceStatus );
				if ( serviceStatus.dwCurrentState == SERVICE_STOPPED )
				{
					if(DeleteService( service )) {
						printf("Success. Agent is now uninstalled.\n");
						RemoveEventSource( gszServiceName ); // do in setup (temporary)
					} else
					{
						printf("The agent could not uninstalled as it is started.\n");
						dwError = GetLastError();
						if(dwError == ERROR_ACCESS_DENIED)
							printf("'Access Denied' while trying to uninstall the agent \n\n");
						else if(dwError == ERROR_INVALID_HANDLE)
							printf("'Handle Invalid' while trying to uninstall the agent\n\n");
						else if(dwError == ERROR_SERVICE_MARKED_FOR_DELETE)
							printf("Agent is already marked for deletion\n\n");
					}
				} else
				{
					dwError = GetLastError();
					printf("The agent could not uninstalled as it is started. Please stop the service before uninstalling.\n\n");
				}
				}
				CloseServiceHandle( service );
			}
			CloseServiceHandle( serviceControlManager );
		}
		return dwError;
}

DWORD StopAgentService(SC_HANDLE hSCM,SC_HANDLE hService,BOOL fStopDependencies, DWORD dwTimeout ) {
   
   SERVICE_STATUS ss;
   DWORD dwStartTime = GetTickCount();

   // Make sure the service is not already stopped
   if ( !QueryServiceStatus( hService, &ss ) )
      return GetLastError();

   if ( ss.dwCurrentState == SERVICE_STOPPED ) 
      return ERROR_SUCCESS;

   // If a stop is pending, just wait for it
   while ( ss.dwCurrentState == SERVICE_STOP_PENDING ) {

      Sleep( ss.dwWaitHint );
      if ( !QueryServiceStatus( hService, &ss ) )
         return GetLastError();

      if ( ss.dwCurrentState == SERVICE_STOPPED )
         return ERROR_SUCCESS;

      if ( GetTickCount() - dwStartTime > dwTimeout )
         return ERROR_TIMEOUT;
   }

   // If the service is running, dependencies must be stopped first
   if ( fStopDependencies ) {

      DWORD i;
      DWORD dwBytesNeeded;
      DWORD dwCount;

      LPENUM_SERVICE_STATUS   lpDependencies = NULL;
      ENUM_SERVICE_STATUS     ess;
      SC_HANDLE               hDepService;

      // Pass a zero-length buffer to get the required buffer size
      if ( EnumDependentServices( hService, SERVICE_ACTIVE, 
         lpDependencies, 0, &dwBytesNeeded, &dwCount ) ) {

         // If the Enum call succeeds, then there are no dependent
         // services so do nothing

      } else {
         
         if ( GetLastError() != ERROR_MORE_DATA )
            return GetLastError(); // Unexpected error

         // Allocate a buffer for the dependencies
         lpDependencies = (LPENUM_SERVICE_STATUS) HeapAlloc( 
               GetProcessHeap(), HEAP_ZERO_MEMORY, dwBytesNeeded );

         if ( !lpDependencies )
            return GetLastError();

         __try {

            // Enumerate the dependencies
            if ( !EnumDependentServices( hService, SERVICE_ACTIVE, 
                  lpDependencies, dwBytesNeeded, &dwBytesNeeded,
                  &dwCount ) )
               return GetLastError();

            for ( i = 0; i < dwCount; i++ ) {

               ess = *(lpDependencies + i);

               // Open the service
               hDepService = OpenService( hSCM, ess.lpServiceName, 
                     SERVICE_STOP | SERVICE_QUERY_STATUS );
               if ( !hDepService )
                  return GetLastError();

               __try {

                  // Send a stop code
                  if ( !ControlService( hDepService, SERVICE_CONTROL_STOP,
                        &ss ) )
                     return GetLastError();

                  // Wait for the service to stop
                  while ( ss.dwCurrentState != SERVICE_STOPPED ) {

                     Sleep( ss.dwWaitHint );
                     if ( !QueryServiceStatus( hDepService, &ss ) )
                        return GetLastError();

                     if ( ss.dwCurrentState == SERVICE_STOPPED )
                        break;

                     if ( GetTickCount() - dwStartTime > dwTimeout )
                        return ERROR_TIMEOUT;
                  }

               } __finally {

                  // Always release the service handle
                  CloseServiceHandle( hDepService );

               }

            }

         } __finally {

            // Always free the enumeration buffer
            HeapFree( GetProcessHeap(), 0, lpDependencies );

         }
      } 
   }

   // Send a stop code to the main service
   if ( !ControlService( hService, SERVICE_CONTROL_STOP, &ss ) )
      return GetLastError();

   // Wait for the service to stop
   while ( ss.dwCurrentState != SERVICE_STOPPED ) {

      Sleep( ss.dwWaitHint );
      if ( !QueryServiceStatus( hService, &ss ) )
         return GetLastError();

      if ( ss.dwCurrentState == SERVICE_STOPPED )
         break;

      if ( GetTickCount() - dwStartTime > dwTimeout )
         return ERROR_TIMEOUT;
   }

   // Return success
   return ERROR_SUCCESS;
}

DWORD StartAgentService() 
{ 
	
    SERVICE_STATUS ssStatus; 
    DWORD dwOldCheckPoint; 
    DWORD dwStartTickCount;
    DWORD dwWaitTime;
    DWORD dwStatus = NO_ERROR;
	DWORD dwWaitHint = 60000;
	SC_HANDLE serviceControlManager = OpenSCManager( 0, 0,SC_MANAGER_CREATE_SERVICE );
 
    SC_HANDLE schService = OpenService( 
        serviceControlManager,          // SCM database 
        gszServiceName,         
        SERVICE_ALL_ACCESS); 
 
    if (schService == NULL) 
    { 
       goto serviceexit;
    }
 
    if (!StartService(
            schService,  // handle to service 
            0,           // number of arguments 
            NULL) )      // no arguments 
    {
         goto serviceexit;
    }
    else 
    {
        printf("Service start pending.\n"); 
    }
 
    // Check the status until the service is no longer start pending. 
 
    if (!QueryServiceStatus( 
            schService,   // handle to service 
            &ssStatus) )  // address of status information structure
    {
         goto serviceexit;
    }
 
    // Save the tick count and initial checkpoint.

    dwStartTickCount = GetTickCount();
    dwOldCheckPoint = ssStatus.dwCheckPoint;

    while (ssStatus.dwCurrentState == SERVICE_START_PENDING) 
    { 
        // Do not wait longer than the wait hint. A good interval is 
        // one tenth the wait hint, but no less than 1 second and no 
        // more than 10 seconds. 
 
        dwWaitTime = dwWaitHint / 10; // ssStatus.

        if( dwWaitTime < 1000 )
            dwWaitTime = 1000;
        else if ( dwWaitTime > 10000 )
            dwWaitTime = 10000;

        Sleep( dwWaitTime );

        // Check the status again. 
 
        if (!QueryServiceStatus( 
                schService,   // handle to service 
                &ssStatus) )  // address of structure
            break; 
 
        if ( ssStatus.dwCheckPoint > dwOldCheckPoint )
        {
            // The service is making progress.

            dwStartTickCount = GetTickCount();
            dwOldCheckPoint = ssStatus.dwCheckPoint;
        }
        else
        {
            if(GetTickCount()-dwStartTickCount > dwWaitHint)
            {
                // No progress made within the wait hint
                break;
            }
        }
    } 


    if (ssStatus.dwCurrentState == SERVICE_RUNNING) 
    {
        printf("Agent started successfully.\n"); 
        dwStatus = 0;
    }
    else 
    { 
        printf("\nAgent failed to start. See Windows Event Viewer for details...\n\n");
        dwStatus = GetLastError();
    } 
serviceexit:
    CloseServiceHandle(schService); 
	CloseServiceHandle( serviceControlManager );
    return dwStatus;
}


BOOL Impersonate(TCHAR* szUserName, TCHAR* szDomain, TCHAR* szPassword)
{
	
	DWORD  dwSize			= 50+1;
	char   szUser[50+1]		= {0};
	PROFILEINFO MyProfile	= {0};

	LUID Luid;
	if(!LookupPrivilegeValue(NULL, SE_TCB_NAME, &Luid)) 
	{
		ReportError(USER_ACCOUNT_LOGIN_ERROR, TEXT("LookupPrivilegeValue failed"),TRUE);
		goto Exit;
	}

	HANDLE hProcToken;
	if(!OpenProcessToken(GetCurrentProcess(),TOKEN_ADJUST_PRIVILEGES|TOKEN_QUERY|TOKEN_DUPLICATE,&hProcToken)) 
	{
		ReportError(USER_ACCOUNT_LOGIN_ERROR, TEXT("OpenProcessToken failed"),TRUE);
		goto Exit;
	}

	TOKEN_PRIVILEGES TokenPriv;
	TokenPriv.PrivilegeCount            = 1;
	TokenPriv.Privileges[0].Luid        = Luid;
	TokenPriv.Privileges[0].Attributes  = SE_PRIVILEGE_ENABLED;

	if(!AdjustTokenPrivileges(hProcToken,FALSE,&TokenPriv,0,NULL,NULL))  
	{
		ReportError(USER_ACCOUNT_LOGIN_ERROR, TEXT("AdjustTokenPrivileges failed"),TRUE);
		goto Exit;
	}

	// Log on as user with valid credentials for accessing the Exchange server.
	if (!LogonUser(szUserName, szDomain, szPassword,LOGON32_LOGON_SERVICE,LOGON32_PROVIDER_DEFAULT, &hToken)) 
	{
		ReportError(USER_ACCOUNT_LOGIN_ERROR, TEXT("LogonUser failed"),TRUE);
		goto Exit;
	}

	//Instructions for LoadUserProfile taken from Q196070
	//LoadUserProfile requires userenv.h
	//Added userenv.lib to libraries
	MyProfile.dwSize = sizeof(PROFILEINFO);
	MyProfile.lpUserName = szUserName;
	MyProfile.dwFlags = PI_NOUI;

	// If you make this call, you don't need to create a dynamic profile.
	if (!LoadUserProfile(hToken,&MyProfile))
	{
		ReportError(USER_ACCOUNT_LOGIN_ERROR, TEXT("LoadUserProfile failed"),TRUE);
		goto Exit;
	}

	// Impersonate the logged on user
	// so that we get the correct NTLM security context.
	if (!ImpersonateLoggedOnUser(hToken)) 
	{
		ReportError(USER_ACCOUNT_LOGIN_ERROR, TEXT("ImpersonateLoggedOnUser failed"),TRUE);	
		goto Exit;
	}
	// Impersonate the logged on user
	// so that we get the correct NTLM security context.
	if (!ImpersonateLoggedOnUser(hToken)) 
	{
		ReportError(USER_ACCOUNT_LOGIN_ERROR, TEXT("ImpersonateLoggedOnUser failed"),TRUE);	
		goto Exit;
	}
	RegCloseKey(HKEY_CURRENT_USER);
	return TRUE;
	Exit:
		return FALSE;
}

// CreateProfileWithIProfAdmin function: This uses the MAPI IProfAdmin to 
// programmatically create a profile. No UI is displayed.
BOOL CreateMAPIProfile(TCHAR* szProfile, TCHAR* szMailbox, TCHAR* szServer)
{
    HRESULT         hRes = S_OK;            // Result from MAPI calls.
    LPPROFADMIN     lpProfAdmin = NULL;     // Profile Admin object.
    LPSERVICEADMIN  lpSvcAdmin = NULL;      // Service Admin object.
    LPMAPITABLE     lpMsgSvcTable = NULL;   // Table to hold services.
    LPSRowSet       lpSvcRows = NULL;       // Rowset to hold results of table query.
    SPropValue      rgval[2];               // Property structure to hold values we want to set.
    SRestriction    sres;                   // Restriction structure.
    SPropValue      SvcProps;               // Property structure for restriction.
	BOOL			success = FALSE;

    // This indicates columns we want returned from HrQueryAllRows.
    enum {iSvcName, iSvcUID, cptaSvc};
    SizedSPropTagArray(cptaSvc,sptCols) = { cptaSvc, PR_SERVICE_NAME, PR_SERVICE_UID };

    // Get an IProfAdmin interface.
    if (FAILED(hRes = MAPIAdminProfiles(0,              // Flags.
                                        &lpProfAdmin))) // Pointer to new IProfAdmin.
    {
		ReportError(CREATE_MAPI_PROFILE_ERROR, TEXT("Error getting IProfAdmin interface"),TRUE);
        goto error;
    }

    // Create a new profile.
    if (FAILED(hRes = lpProfAdmin->CreateProfile(szProfile,     // Name of new profile.
                                                 NULL,          // Password for profile.
                                                 NULL,          // Handle to parent window.
                                                 NULL)))        // Flags.
    {
		ReportError(CREATE_MAPI_PROFILE_ERROR, TEXT("Error creating profile"),TRUE);
        goto error;
    }

    // Get an IMsgServiceAdmin interface off of the IProfAdmin interface.

    if (FAILED(hRes = lpProfAdmin->AdminServices(szProfile,     // Profile that we want to modify.
                                                 NULL,          // Password for that profile.
                                                 NULL,          // Handle to parent window.
                                                 0,             // Flags.
                                                 &lpSvcAdmin))) // Pointer to new IMsgServiceAdmin.
    {
		ReportError(CREATE_MAPI_PROFILE_ERROR, TEXT("Error getting IMsgServiceAdmin interface"),TRUE);
		goto error;
    }

    // Create the new message service for Exchange.

    if (FAILED(hRes = lpSvcAdmin->CreateMsgService("MSEMS",     // Name of service from MAPISVC.INF.
                                                   NULL,        // Display name of service.
                                                   NULL,        // Handle to parent window.
                                                   NULL)))      // Flags.
    {
		ReportError(CREATE_MAPI_PROFILE_ERROR, TEXT("Error creating Exchange message service"),TRUE);
		goto error;
    }
        
    // We now need to get the entry id for the new service.
    // This can be done by getting the message service table
    // and getting the entry that corresponds to the new service.

    if (FAILED(hRes = lpSvcAdmin->GetMsgServiceTable(0,                 // Flags.
                                                     &lpMsgSvcTable)))  // Pointer to table.
    {
        cout<<"Error getting Message Service Table.";
		ReportError(CREATE_MAPI_PROFILE_ERROR, TEXT("Error creating Exchange message service"),TRUE);
		goto error;
    }

    // Set up restriction to query table.

    sres.rt = RES_CONTENT;
    sres.res.resContent.ulFuzzyLevel = FL_FULLSTRING;
    sres.res.resContent.ulPropTag = PR_SERVICE_NAME;
    sres.res.resContent.lpProp = &SvcProps;

    SvcProps.ulPropTag = PR_SERVICE_NAME;
    SvcProps.Value.lpszA = "MSEMS";

    // Query the table to get the entry for the newly created message service.

    if (FAILED(hRes = HrQueryAllRows(lpMsgSvcTable,
                                     (LPSPropTagArray)&sptCols,
                                     &sres,
                                     NULL,
                                     0,
                                     &lpSvcRows)))
    {
		ReportError(CREATE_MAPI_PROFILE_ERROR, TEXT("Error querying table for message service"),TRUE);
		goto error;
    }

    // Setup a SPropValue array for the properties you need to configure.

    // First, the server name.
    ZeroMemory(&rgval[1], sizeof(SPropValue) );
    rgval[1].ulPropTag = PR_PROFILE_UNRESOLVED_SERVER;
    rgval[1].Value.lpszA = szServer;

    // Next, the mailbox name.
    ZeroMemory(&rgval[0], sizeof(SPropValue) );
    rgval[0].ulPropTag = PR_PROFILE_UNRESOLVED_NAME; 
    rgval[0].Value.lpszA = szMailbox;

    // Configure the message service with the above properties.

        if (FAILED(hRes = lpSvcAdmin->ConfigureMsgService(
        (LPMAPIUID)lpSvcRows->aRow->lpProps[iSvcUID].Value.bin.lpb, // Entry ID of service to configure.
        NULL,                                                       // Handle to parent window.
        0,                                                          // Flags.
        2,                                                          // Number of properties we are setting.
        rgval)))                                                    // Pointer to SPropValue array.
    {
		ReportError(CREATE_MAPI_PROFILE_ERROR, TEXT("Error configuring message service"),TRUE);
        goto error;
    }
	success = TRUE;
    goto cleanup;

error:
   
	success = FALSE;

cleanup:
    // Clean up.
    if (lpSvcRows) FreeProws(lpSvcRows);
    if (lpMsgSvcTable) lpMsgSvcTable->Release();
    if (lpSvcAdmin) lpSvcAdmin->Release();
    if (lpProfAdmin) lpProfAdmin->Release();

    return success;

} 

void GetErrorString(TCHAR* friendlyErrorMessage, BOOL reportLastError, TCHAR* errorOut)
{
	ULONG   lastError			= 0;
	LPVOID  lpvMessageBuffer	= 0;
	TCHAR   lastErrorMsg[255]	= TEXT("");
	TCHAR   buffer[255]			= TEXT("");

	lstrcpy(errorOut,"");
	lstrcat(errorOut,(TCHAR *)friendlyErrorMessage);
	lastError = GetLastError();
	if (reportLastError && lastError != 0) {
		FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER|FORMAT_MESSAGE_FROM_SYSTEM,
				  NULL, GetLastError(),
				  MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
				 (LPTSTR)&lpvMessageBuffer, 0, NULL);
		lstrcpy(lastErrorMsg, (TCHAR *)lpvMessageBuffer);
		LocalFree(lpvMessageBuffer);
		wsprintf(buffer,TEXT(". Error %d: "),lastError);
		lstrcat(errorOut,(TCHAR *)buffer);
		lstrcat(errorOut,(TCHAR *)lastErrorMsg);
	}
}

void ReportError(DWORD errorType, TCHAR* friendlyErrorMessage, BOOL reportLastError)
{
	TCHAR completeError[1024];
	GetErrorString(friendlyErrorMessage,reportLastError,completeError);
	PCTSTR aInsertions[] = { completeError };
	ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,errorType,NULL,1,0,aInsertions,NULL);
	SetLastError(0);
}

void ErrorStopService(DWORD category, TCHAR* friendlyErrorMsg, BOOL reportLastError)
{
	ReportError(category, friendlyErrorMsg, reportLastError);
	ULONG lastError = GetLastError();
	SetEvent(ServiceControlEvent);
	DWORD dwWaitRes = WaitForSingleObject(hThread,5000);
	if((dwWaitRes == WAIT_FAILED)||(dwWaitRes==WAIT_ABANDONED))
        SetTheServiceStatus(SERVICE_STOPPED, lastError, 0, 0);
    else
		SetTheServiceStatus(SERVICE_STOP_PENDING, 0, 0, 3000);
} 

void CheckOutlookVersion() {
	
	LPTSTR version = NULL;
	GetOutlookVersionString(&version);
	if (version!=NULL)
	{
		char * pch;
		pch = strtok (version,".");
		int major = atoi( pch );
		pch = strtok (NULL, ".");
		pch = strtok (NULL, ".");
		int build = atoi( pch );
		if (major<11 || (major==11 && build <6359)){
			PCTSTR aInsertions[] = { version };
			ReportEvent(m_hEventSource,EVENTLOG_ERROR_TYPE,0,INCORRECT_OUTLOOK_VERSION,NULL,1,0,aInsertions,NULL);	
		}
		free(version);
	} 
}

HRESULT GetOutlookVersionString(LPTSTR* ppszVer)
{
	TCHAR pszaOutlookQualifiedComponents[][MAX_PATH] = {TEXT("{BC174BAD-2F53-4855-A1D5-0D575C19B1EA}"),
														TEXT("{BC174BAD-2F53-4855-A1D5-1D575C19B1EA}") };
	int nOutlookQualifiedComponents = 2;
	int i				= 0;
	DWORD dwValueBuf	= 0;
	UINT ret			= 0;
	HRESULT hr			= E_FAIL;
	LPTSTR pszTempPath	= NULL;
	LPTSTR pszTempVer	= NULL;
	assert(ppszVer);

	for (i = 0; i < nOutlookQualifiedComponents; i++)
	{
		ret = MsiProvideQualifiedComponent(	pszaOutlookQualifiedComponents[i],
											TEXT("outlook.exe"),
											INSTALLMODE_DEFAULT,
											NULL,
											&dwValueBuf);
		if (ret == ERROR_SUCCESS)
		{
			break;
		}
	}

	if (ret == ERROR_SUCCESS)
	{
		dwValueBuf += 1;
		pszTempPath = (LPTSTR) malloc(dwValueBuf * sizeof(TCHAR));
		
		if (pszTempPath != NULL)
		{
			if ((ret = MsiProvideQualifiedComponent( pszaOutlookQualifiedComponents[i],
												TEXT("outlook.exe"),
												INSTALLMODE_EXISTING,
												pszTempPath,
												&dwValueBuf)) !=ERROR_SUCCESS)
			{
				goto Error;
			}

			pszTempVer = (LPTSTR) malloc(MAX_PATH * sizeof(TCHAR));
			dwValueBuf = MAX_PATH;
			if((ret =  MsiGetFileVersion(pszTempPath,
									pszTempVer,
									&dwValueBuf,
									NULL,
									NULL))!= ERROR_SUCCESS)
			{
				goto Error;	
			}
			*ppszVer = pszTempVer;
			pszTempVer = NULL;
			hr = S_OK;
		}
	}

Error:
	free(pszTempVer);
	free(pszTempPath);
	return hr;
}

