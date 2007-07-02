// Encrypt.cpp: implementation of the CEncrypt class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"

#include "Encrypt.h"

#ifdef _DEBUG
#undef THIS_FILE
static char THIS_FILE[]=__FILE__;
#define new DEBUG_NEW
#endif

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CEncrypt::CEncrypt(char* keyString)
{
	
	encryptKey=keyString;

		
	initialisationSuccess=CryptAcquireContext(&cspContext,NULL,NULL,PROV_RSA_FULL ,0);
	

	if(initialisationSuccess)
	{
		initialisationSuccess=CryptCreateHash(cspContext,CALG_MD5,0,0,&hashData);
	}
	
	if(initialisationSuccess)
	{
		initialisationSuccess=CryptHashData(hashData,(const unsigned char *)encryptKey,strlen(encryptKey),0);
	}

	if(initialisationSuccess)
	{
		initialisationSuccess=CryptDeriveKey(cspContext,CALG_RC2,hashData,0,&keyEncryption);
	}


}

int  CEncrypt::Decrypt(char *dest,DWORD actualSize)
{
	

	//long actualSize=dwDataSize;

	if(initialisationSuccess && dest!=NULL)
	{
		 if(!CryptDecrypt(keyEncryption,0,true,0,(unsigned char *)dest,(unsigned long *)&actualSize))
		 {
			 
			return -1;
			 
		 }

		 dest[actualSize]='\0';
		 return actualSize;
	}

	return -1;
	
	
}

int  CEncrypt::Encrypt(char *msg,long orginalSize)
{
	
	long actualSize=strlen(msg);

	
	if(initialisationSuccess)
	{
		

		 if(!CryptEncrypt(keyEncryption,NULL,true,0,(unsigned char *)msg,(unsigned long *)&actualSize,orginalSize))
		 {
			 return -1;
		 }



		  msg[actualSize]='\0';

		 	 
		 return actualSize;
		 
	}

	return -1;

}

CEncrypt::~CEncrypt()
{
	RegCloseKey(hKey);
}
