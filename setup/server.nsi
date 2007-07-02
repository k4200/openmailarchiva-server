; server.nsi
;
; This script is based on example1.nsi, but it remember the directory, 
; has uninstall support and (optionally) installs start menu shortcuts.
;
; It will install makensisw.exe into a directory that the user selects,

;--------------------------------

;--------------------------------

!macro BIMAGE IMAGE PARMS
	Push $0
	GetTempFileName $0
	File /oname=$0 "${IMAGE}"
	SetBrandingImage ${PARMS} $0
	Delete $0
	Pop $0
!macroend


!define JRE_VERSION "1.6"

!Macro "CreateURL" "URLFile" "URLSite" "URLDesc"
  WriteINIStr "$INSTDIR\${URLFile}.url" "InternetShortcut" "URL" "${URLSite}"
  SetShellVarContext "all"
  CreateShortCut "$SMPROGRAMS\MailArchiva\${URLFile}.lnk" "$INSTDIR\${URLFile}.url" "" \
                 "" 0 "SW_SHOWNORMAL" "" "${URLDesc}"
!macroend


; The name of the installer
Name "MailArchiva Server v1.3.1"

; The file to write
OutFile "Out\serversetup.exe"

; The default installation directory
InstallDir $PROGRAMFILES\MailArchiva

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\MailArchiva\Server" "Install_Dir"

;--------------------------------

BrandingText "Copyright (c) Jamie Band 2005-2007"

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
	MessageBox MB_OK "The Java Runtime Environment (JRE) 1.6 or higher is required. Please install it and re-run setup."
	Quit
 finished:
  ReadRegStr $R0 HKLM \
   "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaServer" \
   "UninstallString"
   StrCmp $R0 "" ok
  
   MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
   "A version of Mail Archiva Server is already installed. $\n$\nClick `OK` to remove the \
   previous version or `Cancel` to cancel this upgrade." \
   IDOK uninst
   Abort
   
 ;Run the uninstaller
 uninst:
   ClearErrors
   CopyFiles /SILENT $INSTDIR\Server\webapps\mailarchiva\WEB-INF\conf\server.conf $INSTDIR\server.conf
   CopyFiles /SILENT $INSTDIR\Server\webapps\mailarchiva\WEB-INF\conf\users.conf $INSTDIR\users.conf
   
servicenotinstalledx:
   ExecWait '$R0 _?=$INSTDIR' ;Do not copy the uninstaller to a temp file  
   IfErrors no_remove_uninstaller
     ;You can either use Delete /REBOOTOK in the uninstaller or add some code
     ;here to remove the uninstaller. Use a registry key to check
     ;whether the user has chosen to uninstall. If you are using an uninstaller
     ;components page, make sure all sections are uninstalled.
   no_remove_uninstaller:
  
  ok:
 
 
FunctionEnd


Function .onGUIInit
   !insertmacro BIMAGE "banner.bmp" /RESIZETOFIT

   
 FunctionEnd


; Pages

Page license
Page components
Page directory
Page custom registrationPage "" ": Product Registration"
Page instfiles
UninstPage uninstConfirm un.uninstImage
UninstPage instfiles

;--------------------------------


; The stuff to install

Section "MailArchiva Server .WAR (required)"

  SectionIn RO
  SetDetailsPrint none
  SetShellVarContext all
  RMDir /r /REBOOTOK "$INSTDIR\Server\"
  SetOutPath $INSTDIR
  File "banner.bmp"
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR\Server\webapps
  ; Put file there
  File "MailArchiva.war"
   ZipDLL::extractall 'MailArchiva.war' '$INSTDIR\Server\webapps\mailarchiva'
  CopyFiles $INSTDIR\server.conf $INSTDIR\Server\webapps\mailarchiva\WEB-INF\conf\server.conf 
  CopyFiles $INSTDIR\users.conf $INSTDIR\Server\webapps\mailarchiva\WEB-INF\conf\users.conf
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
  !insertmacro "CreateURL" "Upgrade to Enterprise Edition" "http://www.mailarchiva.com/enterprise" "Upgrade to MailArchiva Enterprise Edition"
  !insertmacro "CreateURL" "Visit MailArchiva Website" "http://www.mailarchiva.com" "Visit MailArchiva Website"
  ExecShell "open" "http://www.mailarchiva.com/register.php?name=$R4&company=$R5&email=$R6&tel=$R7&mailboxes=$R8"
  
SectionEnd


Section "Application Server (optional)"
SetShellVarContext all
SetOutPath '$INSTDIR\'
File /r "server\"
nsExec::Exec /TIMEOUT=0 '$INSTDIR\Server\bin\MailArchivaServer.exe //IS//MailArchivaServer  --JvmMs=128 --JvmMx=1024  --Startup=auto --DisplayName="MailArchiva Server" --Install="$INSTDIR\Server\bin\MailArchivaServer.exe" --Classpath="$5\lib\classes.zip;$INSTDIR\Server\bin\bootstrap.jar" --Jvm=auto  --StartMode=jvm --JvmOptions="-Dcatalina.home=$INSTDIR\Server;-Djava.endorsed.dirs=$INSTDIR\Server\common\endorsed;-Djava.io.tmpdir=$INSTDIR\Server\temp;-Dfile.encoding=UTF-8" --StartClass=org.apache.catalina.startup.Bootstrap --StartParams=start --StopMode=jvm --StopClass=org.apache.catalina.startup.Bootstrap --StopParams=stop --LogPath="$INSTDIR\Server\logs" --LogLevel=INFO'
Sleep 500
Exec '"$INSTDIR\Server\bin\MailArchivaServerW" //MS//MailArchivaServer'
SectionEnd




 Function registrationPage

   GetTempFileName $R0
   File /oname=$R0 registrationinfo.ini
retryx:
   InstallOptions::dialog $R0
   Pop $R1
   StrCmp $R1 "cancel" done
   StrCmp $R1 "back" done
   StrCmp $R1 "success" validate
   error: MessageBox MB_OK|MB_ICONSTOP "An error occurred:$\r$\n$R1"
   
   validate:
    
   ; full name
   ReadINIStr $R4 $R0 "Field 3" "State"
   StrCmp $R4 "" errorx
   ; company
   ReadINIStr $R5 $R0 "Field 5" "State"
   StrCmp $R5 "" errorx
   ; email
   ReadINIStr $R6 $R0 "Field 7" "State"
   StrCmp $R6 "" errorx
   ; telephone
   ReadINIStr $R7 $R0 "Field 9" "State"
   StrCmp $R7 "" errorx
   ; mailboxes
   ReadINIStr $R8 $R0 "Field 11" "State"
   StrCmp $R8 "" errorx
   Return
   
   errorx:
   MessageBox MB_OK|MB_ICONSTOP "Cannot proceed with installation. All fields must be completed."
   Goto retryx
   done:
 FunctionEnd
 
 
 # Uses $0
 Function openLinkNewWindow
   Push $3 
   Push $2
   Push $1
   Push $0
   ReadRegStr $0 HKCR "http\shell\open\command" ""
 # Get browser path
     DetailPrint $0
   StrCpy $2 '"'
   StrCpy $1 $0 1
   StrCmp $1 $2 +2 # if path is not enclosed in " look for space as final char
     StrCpy $2 ' '
   StrCpy $3 1
   loop:
     StrCpy $1 $0 1 $3
     DetailPrint $1
     StrCmp $1 $2 found
     StrCmp $1 "" found
     IntOp $3 $3 + 1
     Goto loop
  
   found:
     StrCpy $1 $0 $3
     StrCmp $2 " " +2
       StrCpy $1 '$1"'
  
   Pop $0
   Exec '$1 $0'
   Pop $1
   Pop $2
   Pop $3
FunctionEnd

;--------------------------------

; Uninstaller

Function un.uninstImage
	!insertmacro BIMAGE "banner.bmp" /RESIZETOFIT
FunctionEnd

Section "Uninstall"
  SetShellVarContext all
   CopyFiles /SILENT $INSTDIR\Server\webapps\mailarchiva\WEB-INF\conf\server.conf $INSTDIR
  CopyFiles /SILENT $INSTDIR\Server\webapps\mailarchiva\WEB-INF\conf\users.conf $INSTDIR
  
  services::IsServiceInstalled 'MailArchivaServer'
  Pop $0
  ;MessageBox MB_OK|MB_ICONSTOP "out:$0"
  StrCmp $0 'No' servicenotinstalled
  nsExec::Exec /TIMEOUT=0 '$INSTDIR\Server\bin\MailArchivaServer.exe //SS//MailArchivaServer'
  Sleep 1000
  nsExec::Exec /TIMEOUT=0 '$INSTDIR\Server\bin\MailArchivaserver.exe //DS//MailArchivaServer'
  Sleep 1000
  servicenotinstalled:
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

  MessageBox MB_OK "A backup of your server configuration has been placed in $INSTDIR."

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