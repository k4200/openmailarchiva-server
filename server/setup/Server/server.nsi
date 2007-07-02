; server.nsi
;
; This script is based on example1.nsi, but it remember the directory, 
; has uninstall support and (optionally) installs start menu shortcuts.
;
; It will install makensisw.exe into a directory that the user selects,

;--------------------------------

!define JRE_VERSION "1.4.1"


; The name of the installer
Name "MailArchiva Server v1.2"

; The file to write
OutFile "C:\dev\sourcedir\server\setup\Out\serversetup.exe"

; The default installation directory
InstallDir $PROGRAMFILES\MailArchiva

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\MailArchiva\Server" "Install_Dir"

;--------------------------------

BrandingText "Copyright Jamie Band 2006"

AllowRootDirInstall false
SetDateSave on
SetDatablockOptimize on
CRCCheck on
XPStyle on
ShowUninstDetails nevershow
ShowInstDetails nevershow
AddBrandingImage left 135 0
LicenseData license_open_source_edition.rtf

Function .onInit
			
	Push "${JRE_VERSION}"
	Call DetectJRE  
	Pop $5	  ; DetectJRE's return value
	StrCmp $5 "0"  nojava
  	StrCmp $5 "-1" nojava
  	goto finished
 
 nojava:
	MessageBox MB_OK "The Java Runtime Environment (JRE) 1.4.1 or higher is required. Please install it and re-run setup."
	Quit
 finished:
 
FunctionEnd

Function .onGUIInit
   
   
   SetBrandingImage "C:\dev\sourcedir\server\setup\Server\banner.bmp"
   
 FunctionEnd


; Pages

Page license
Page components
Page directory
Page instfiles
UninstPage uninstConfirm un.uninstImage
UninstPage instfiles

;--------------------------------


; The stuff to install

Section "MailArchiva Server .WAR (required)"

  SectionIn RO
  SetDetailsPrint none
  SetShellVarContext all
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR\Server\webapps
  
  ; Put file there
  File "C:\dev\sourcedir\server\setup\server\MailArchiva.war"
  
  ; Write the installation path into the registry
  WriteRegStr HKLM "SOFTWARE\MailArchiva\Server" "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaServer" "DisplayName" "MailArchiva Server"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaServer" "UninstallString" '"$INSTDIR\uninstallserver.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaServer" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaServer" "NoRepair" 1
  WriteUninstaller "uninstallserver.exe"
  CreateDirectory "$SMPROGRAMS\MailArchiva"
  CreateShortCut "$SMPROGRAMS\MailArchiva\MailArchiva Console Login.lnk" "http://localhost:8090/mailarchiva" 
  CreateShortCut "$SMPROGRAMS\MailArchiva\MailArchiva Service Manager.lnk" "$INSTDIR\Server\bin\MailArchivaServerW.exe" "//MS//MailArchivaServer"
  CreateShortCut "$SMSTARTUP\MailArchiva Server.lnk" "$INSTDIR\Server\bin\MailArchivaServerW.exe" "//MS//MailArchivaServer"
 
SectionEnd


Section "Application Server (optional)"
  SetShellVarContext all
  SetOutPath '$INSTDIR\'
  File /r "C:\dev\sourcedir\server\setup\server\server"
  ;File "C:\dev\sourcedir\server\setup\Server\brandingimage.bmp"
  ;ExecWait '$INSTDIR\Server\bin\MailArchivaServer.exe //SS//MailArchivaServer' $0
  ;ExecWait '$INSTDIR\Server\bin\MailArchivaServer.exe //DS//MailArchivaServer' $0
   ExecWait '$INSTDIR\Server\bin\MailArchivaServer.exe //IS//MailArchivaServer  --JvmMs=128 --JvmMx=256 --Startup=auto --DisplayName="MailArchiva Server" --Install="$INSTDIR\Server\bin\MailArchivaServer.exe" --Classpath="$5\lib\classes.zip;$INSTDIR\Server\bin\bootstrap.jar" --Jvm=auto  --StartMode=jvm --JvmOptions="-Dcatalina.home=$INSTDIR\Server;-Djava.endorsed.dirs=$INSTDIR\Server\common\endorsed;-Djava.io.tmpdir=$INSTDIR\Server\temp" --StartClass=org.apache.catalina.startup.Bootstrap --StartParams=start --StopMode=jvm --StopClass=org.apache.catalina.startup.Bootstrap --StopParams=stop --LogPath="$INSTDIR\Server\logs" --LogLevel=INFO'
  Exec '"$INSTDIR\Server\bin\MailArchivaServerW" //MS//MailArchivaServer'

  
SectionEnd


 Function customPage

   GetTempFileName $R0
   File /oname=$R0 agentsettings.ini
retryx:
   InstallOptions::dialog $R0
   Pop $R1
   StrCmp $R1 "cancel" done
   StrCmp $R1 "back" done
   StrCmp $R1 "success" success
   error: MessageBox MB_OK|MB_ICONSTOP "An error occurred:$\r$\n$R1"
   success:
   
	   ; exchange address
	   ReadINIStr $R4 $R0 "Field 2" "State"
	   ; windows user account
	   ReadINIStr $R5 $R0 "Field 4" "State"
	   ; password
	   ReadINIStr $R6 $R0 "Field 6" "State"
	   ;server uri
	   ReadINIStr $R8 $R0 "Field 8" "State"
	   
	   ;push "Archiva"
	   ;push $R6
	   ;push $R5
	   ;push $R4
	   
    	   ;Profiler::createProfile
    	   ;Pop $R1
    	  ;StrCmp $R1 "0" done
    	   ;MessageBox MB_ABORTRETRYIGNORE "The mail settings you entered are incorrect." IDIGNORE done IDRETRY retryx 
	   ;Quit
           
    	   
   done:
 FunctionEnd

;--------------------------------

; Uninstaller

Function un.uninstImage
	SetBrandingImage "C:\dev\sourcedir\server\setup\Server\brandingimage.bmp"
FunctionEnd

Section "Uninstall"
  SetShellVarContext all
  SetBrandingImage "$INSTDIR\brandingimage.bmp"
  ExecWait '$INSTDIR\Server\bin\MailArchivaserver.exe //DS//MailArchivaServer' $0
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaServer"
  DeleteRegKey HKLM SOFTWARE\Stimulus\MailArchivaServer

  ; Remove files and uninstaller
  
  Delete $INSTDIR\uninstallserver.exe

  ; Remove shortcuts, if any
 
  RMDir /r /REBOOTOK "$SMPROGRAMS\MailArchiva"
  
  ; Remove directories used
  RMDir /r /REBOOTOK "$INSTDIR\Server"
  Delete "$SMSTARTUP\MailArchiva Server.lnk" 

SectionEnd

; Returns: 0 - JRE not found. -1 - JRE found but too old. Otherwise - Path to JAVA EXE
 
; DetectJRE. Version requested is on the stack.
; Returns (on stack)	"0" on failure (java too old or not installed), otherwise path to java interpreter
; Stack value will be overwritten!
 
Function DetectJRE
  Exch $0	; Get version requested  
		; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1	; $1 = Java version string (ie 1.5.0)
  Push $2	; $2 = Javahome
  Push $3	; $3 and $4 are used for checking the major/minor version of java
  Push $4
  ;MessageBox MB_OK "Detecting JRE"
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ;MessageBox MB_OK "Read : $1"
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  ;MessageBox MB_OK "Read 3: $2"
  StrCmp $2 "" DetectTry2
  Goto GetJRE
 
DetectTry2:
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ;MessageBox MB_OK "Detect Read : $1"
  StrCmp $1 "" NoFound
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  ;MessageBox MB_OK "Detect Read 3: $2"
  StrCmp $2 "" NoFound
 
GetJRE:
; $0 = version requested. $1 = version found. $2 = javaHome
  ; MessageBox MB_OK "Getting JRE"
  IfFileExists "$2\bin\java.exe" 0 NoFound
  StrCpy $3 $0 1			; Get major version. Example: $1 = 1.5.0, now $3 = 1
  StrCpy $4 $1 1			; $3 = major version requested, $4 = major version found
  ; MessageBox MB_OK "Want $3 , found $4"
  IntCmp $4 $3 0 FoundOld FoundNew
  StrCpy $3 $0 1 2
  StrCpy $4 $1 1 2			; Same as above. $3 is minor version requested, $4 is minor version installed
  ; MessageBox MB_OK "Want $3 , found $4" 
  IntCmp $4 $3 FoundNew FoundOld FoundNew
 
NoFound:
 ; MessageBox MB_OK "JRE not found"
  Push "0"
  Goto DetectJREEnd
 
FoundOld:
; MessageBox MB_OK "JRE too old: $3 is older than $4"
;  Push ${TEMP2}
  Push "-1"
  Goto DetectJREEnd  
FoundNew:
 ; MessageBox MB_OK "JRE is new: $3 is newer than $4"
 ; java
  Push $2
;  Push "OK"
;  Return
   Goto DetectJREEnd
DetectJREEnd:
	; Top of stack is return value, then r4,r3,r2,r1
	Exch	; => r4,rv,r3,r2,r1,r0
	Pop $4	; => rv,r3,r2,r1r,r0
	Exch	; => r3,rv,r2,r1,r0
	Pop $3	; => rv,r2,r1,r0
	Exch 	; => r2,rv,r1,r0
	Pop $2	; => rv,r1,r0
	Exch	; => r1,rv,r0
	Pop $1	; => rv,r0
	Exch	; => r0,rv
	Pop $0	; => rv 
FunctionEnd