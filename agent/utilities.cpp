#include "stdafx.h"
#include "utilities.h"

namespace util {

//
// Load a message resource fom the .exe and format it with the passed insertions
//
UINT LoadMessage( DWORD dwMsgId, PTSTR pszBuffer, UINT cchBuffer, ... )
{
	va_list args;
	va_start( args, cchBuffer );
	
	return FormatMessage( 
		FORMAT_MESSAGE_FROM_HMODULE,
		NULL,					// Module (e.g. DLL) to search for the Message. NULL = own .EXE
		dwMsgId,				// Id of the message to look up (aus "Messages.h")
		LANG_NEUTRAL,			// Language: LANG_NEUTRAL = current thread's language
		pszBuffer,				// Destination buffer
		cchBuffer,				// Character count of destination buffer
		&args					// Insertion parameters
	);
}


//
// Installs our app as a source of events under the name pszName into the registry
//
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


//
// Removes the passed source of events from the registry
//
void RemoveEventSource( PCTSTR pszName )
{
	DWORD dwError = 0;
	TCHAR szPath[ MAX_PATH ];
	
	_stprintf( szPath, _T("SYSTEM\\CurrentControlSet\\Services\\EventLog\\Application\\%s"), pszName );
	dwError = RegDeleteKey( HKEY_LOCAL_MACHINE, szPath );
}


}	// namespace util