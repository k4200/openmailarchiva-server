// Register.cpp: implementation of the CRegister class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"

#include "Register.h"

#ifdef _DEBUG
#undef THIS_FILE
static char THIS_FILE[]=__FILE__;
#define new DEBUG_NEW
#endif

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////



CRegister::CRegister(char *pKey)
{


	DWORD dwDisposition=0;
	
	RegCreateKeyEx(HKEY_LOCAL_MACHINE, pKey,0, NULL, 0, KEY_ALL_ACCESS, NULL, &hKey, &dwDisposition);


}

CRegister::~CRegister()
{

}

BOOL CRegister::getRegValue(char* value, char *key,DWORD *aSize)
{
	DWORD dwType=REG_BINARY;
	DWORD dwDataSize=0;

	if(RegQueryValueEx(hKey, key, 0, &dwType,NULL, &dwDataSize)==ERROR_SUCCESS && hKey!=NULL)
	{
			*aSize=dwDataSize;
			if (value!=NULL)
				RegQueryValueEx(hKey, key, 0, &dwType,(PBYTE)(LPTSTR)value,&dwDataSize);
			return TRUE;
	} else
		return FALSE;
}


void CRegister::setRegValue(char *key,char * newValue,long actualSize)
{

	
	DWORD dwType=REG_BINARY;
	
	if(hKey!=NULL)
	{
			 RegSetValueEx(hKey,_T(key),0,dwType,(const unsigned char *)newValue,actualSize);
	}
	

}
