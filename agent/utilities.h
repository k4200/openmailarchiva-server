#ifndef __UTILITIES_H__
#define __UTILITIES_H__


namespace util {

	//
	// Load a message resource fom the .exe and format it with the passed insertions
	//
	UINT LoadMessage( DWORD dwMsgId, PTSTR pszBuffer, UINT cchBuffer, ... );

	//
	// Installs our app as a source of events under the name pszName into the registry
	//
	void AddEventSource( PCTSTR pszName, DWORD dwCategoryCount = 0);

	//
	// Removes the passed source of events from the registry
	//
	void RemoveEventSource( PCTSTR pszName );
}



#endif // __UTILITIES_H__