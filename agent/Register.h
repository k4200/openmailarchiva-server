// Register.h: interface for the CRegister class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_REGISTER_H__571D24C0_4373_4381_8A2E_894D43EB63E6__INCLUDED_)
#define AFX_REGISTER_H__571D24C0_4373_4381_8A2E_894D43EB63E6__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#define MAX_LENGTH 256

class CRegister  
{
public:
	CRegister(char *pKey);
	virtual ~CRegister();
	char *getRegValue(char *key,DWORD *aSize);
	BOOL getRegValue(char* value, char *key,DWORD *aSize);
	void setRegValue(char *key,char * newValue,long aSize);

private:
	HKEY hKey;
	char *parentKey;
	//char *key;

};

#endif // !defined(AFX_REGISTER_H__571D24C0_4373_4381_8A2E_894D43EB63E6__INCLUDED_)
