; server.nsi
;
; This script is based on example1.nsi, but it remember the directory, 
; has uninstall support and (optionally) installs start menu shortcuts.
;
; It will install makensisw.exe into a directory that the user selects,

;--------------------------------

;--------------------------------



!define RELEASE_DIR "\dev\mailarchiva\release"

!macro BIMAGE IMAGE PARMS
	Push $0
	GetTempFileName $0
	File /oname=$0 "${IMAGE}"
	SetBrandingImage ${PARMS} $0
	Delete $0
	Pop $0
!macroend


!Macro "CreateURL" "URLFile" "URLSite" "URLDesc"
  WriteINIStr "$INSTDIR\${URLFile}.url" "InternetShortcut" "URL" "${URLSite}"
  SetShellVarContext "all"
  CreateShortCut "$SMPROGRAMS\MailArchiva\${URLFile}.lnk" "$INSTDIR\${URLFile}.url" "" \
                 "" 0 "SW_SHOWNORMAL" "" "${URLDesc}"
!macroend




; The name of the installer
Name "MailArchiva Open Source Edition Server v1.9"

; The file to write
OutFile "${RELEASE_DIR}\work2\serversetup.exe"

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
LicenseData mailarchiva_opensource_license_agreement.rtf

Function .onInit
			

  ReadRegStr $R0 HKLM \
   "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaServer" \
   "UninstallString"
   StrCmp $R0 "" ok
  
   MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
   "During this upgrade process, your MailArchiva configuration and data will be preserved. Click `OK` to remove the existing version of MailArchiva or `Cancel` to cancel the upgrade." \
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
   !insertmacro BIMAGE "${RELEASE_DIR}\opensource\server\windows\banner.bmp" /RESIZETOFIT

   
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
  File "${RELEASE_DIR}\opensource\server\windows\banner.bmp"
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR\Server
  ; Put file there
  File "${RELEASE_DIR}\work2\mailArchiva.war"
  ;Delete "$INSTDIR\Server\webapps\MailArchiva.war" 
  ZipDLL::extractall '$INSTDIR\Server\MailArchiva.war' '$INSTDIR\Server\webapps\mailarchiva'
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
  !insertmacro "CreateURL" "Upgrade to Enterprise Edition" "http://www.mailarchiva.com/enterprise" "Upgrade to MailArchiva Enterprise Edition"
  !insertmacro "CreateURL" "Visit MailArchiva Website" "http://www.mailarchiva.com" "Visit MailArchiva Website"
  CreateShortCut "$SMPROGRAMS\Startup\MailArchiva.lnk" "$INSTDIR\Server\bin\MailArchivaServerW.exe" "//MS//MailArchivaServer"
  RMDir /r "$INSTDIR\server\work\Catalina"
  CopyFiles $INSTDIR\jre\bin\msvcr71.dll $SYSDIR
  !insertmacro "CreateURL" "Visit MailArchiva Website" "http://www.mailarchiva.com" "Visit MailArchiva Website"
  ExecShell "open" "http://www.mailarchiva.com/register.php?name=$R4&company=$R5&email=$R6&tel=$R7&mailboxes=$R8"
SectionEnd


Section "Application Server (optional)"
SetShellVarContext all
SetOutPath '$INSTDIR\Server'
File /r "${RELEASE_DIR}\work\mailarchiva\server\"
SetOutPath '$INSTDIR\jre'
File /r "${RELEASE_DIR}\work\mailarchiva\jre\"
SetOutPath '$INSTDIR\jre64'
File /r "${RELEASE_DIR}\work\mailarchiva\jre64\"
System::Call "kernel32::GetCurrentProcess() i .s"
System::Call "kernel32::IsWow64Process(i s, *i .r0)"
IntCmp $0 0 thirtytwobit sixtyfourbit sixtyfourbit
sixtyfourbit:
SetOutPath '$INSTDIR\Server\bin'
File /oname=mailarchivaserver.exe "${RELEASE_DIR}\work\mailarchiva\server\bin\mailarchivaserver64.exe"
File /oname=mailarchivaserverw.exe "${RELEASE_DIR}\work\mailarchiva\server\bin\mailarchivaserverw64.exe"
nsExec::Exec /TIMEOUT=0 '"$INSTDIR\Server\bin\MailArchivaServer.exe" //IS//MailArchivaServer --Jvm="$INSTDIR\jre64\bin\server\jvm.dll" --Description "MailArchiva Open Source Edition Server http:/www.mailarchiva.com"  --JvmMs=256 --JvmMx=768  --Startup=auto --DisplayName="MailArchiva Server" --Install="$INSTDIR\Server\bin\MailArchivaServer.exe" --Classpath="$INSTDIR\Server\bin\bootstrap.jar" --StartMode=jvm --JvmOptions="-Dcatalina.home=$INSTDIR\Server;-Djava.endorsed.dirs=$INSTDIR\Server\endorsed;-Djava.io.tmpdir=$INSTDIR\Server\temp;-Dfile.encoding=UTF-8;-XX:PermSize=96M;-XX:MaxPermSize=96M" --StartClass=org.apache.catalina.startup.Bootstrap --StartParams=start --StopMode=jvm --StopClass=org.apache.catalina.startup.Bootstrap --StopParams=stop --LogPath="$INSTDIR\Server\logs" --LogLevel=INFO'
Push "PATH"
Push "$INSTDIR\jre64\bin"
Call AddToPath
goto install
thirtytwobit:
SetOutPath '$INSTDIR\Server\bin'
File /oname=mailarchivaserver.exe "${RELEASE_DIR}\work\mailarchiva\server\bin\mailarchivaserver32.exe"
File /oname=mailarchivaserverw.exe "${RELEASE_DIR}\work\mailarchiva\server\bin\mailarchivaserverw32.exe"
nsExec::Exec /TIMEOUT=0 '"$INSTDIR\Server\bin\MailArchivaServer.exe" //IS//MailArchivaServer --Jvm="$INSTDIR\jre\bin\client\jvm.dll" --Description "MailArchiva Open Source Server http:/www.mailarchiva.com"  --JvmMs=256 --JvmMx=768  --Startup=auto --DisplayName="MailArchiva Server" --Install="$INSTDIR\Server\bin\MailArchivaServer.exe" --Classpath="$INSTDIR\Server\bin\bootstrap.jar" --StartMode=jvm --JvmOptions="-Dcatalina.home=$INSTDIR\Server;-Djava.endorsed.dirs=$INSTDIR\Server\endorsed;-Djava.io.tmpdir=$INSTDIR\Server\temp;-Dfile.encoding=UTF-8;-XX:PermSize=96M;-XX:MaxPermSize=96M" --StartClass=org.apache.catalina.startup.Bootstrap --StartParams=start --StopMode=jvm --StopClass=org.apache.catalina.startup.Bootstrap --StopParams=stop --LogPath="$INSTDIR\Server\logs" --LogLevel=INFO'
Push "PATH"
Push "$INSTDIR\jre\bin"
install:
Sleep 500
Exec '"$INSTDIR\Server\bin\MailArchivaServerW" //MS//MailArchivaServer'
SectionEnd




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
	!insertmacro BIMAGE "${RELEASE_DIR}\opensource\server\windows\banner.bmp" /RESIZETOFIT
FunctionEnd

Section "Uninstall"
  SetShellVarContext all
   CopyFiles /SILENT $INSTDIR\Server\webapps\mailarchiva\WEB-INF\conf\server.conf $INSTDIR
   CopyFiles /SILENT $INSTDIR\Server\webapps\mailarchiva\WEB-INF\conf\users.conf $INSTDIR
   nsExec::Exec /TIMEOUT=0 '$INSTDIR\Server\bin\MailarchivaServerW.exe //MQ//MailArchivaServer'

  services::IsServiceInstalled 'MailArchivaServer'
  Pop $0
  ;MessageBox MB_OK|MB_ICONSTOP "out:$0"
  StrCmp $0 'No' servicenotinstalled
  nsExec::Exec /TIMEOUT=0 '$INSTDIR\Server\bin\MailArchivaServer.exe //SS//MailArchivaServer'
  Sleep 3000
  nsExec::Exec /TIMEOUT=0 '$INSTDIR\Server\bin\MailArchivaServer.exe //DS//MailArchivaServer'
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
  Push "PATH"
  Push $INSTDIR\jre\bin
  Call un.RemoveFromPath

  MessageBox MB_OK "A backup of your server configuration has been placed in $INSTDIR."

SectionEnd


; based upon a script of "Written by KiCHiK 2003-01-18 05:57:02"
;----------------------------------------
!verbose 3
!include "WinMessages.NSH"
!verbose 4
;====================================================
; get_NT_environment 
;     Returns: the selected environment
;     Output : head of the stack
;====================================================

;----------------------------------------------------
!define NT_current_env 'HKCU "Environment"'
!define NT_all_env     'HKLM "SYSTEM\CurrentControlSet\Control\Session Manager\Environment"'
;====================================================
; IsNT - Returns 1 if the current system is NT, 0
;        otherwise.
;     Output: head of the stack
;====================================================
!macro IsNT UN
Function ${UN}IsNT
  Push $0
  ReadRegStr $0 HKLM "SOFTWARE\Microsoft\Windows NT\CurrentVersion" CurrentVersion
  StrCmp $0 "" 0 IsNT_yes
  ; we are not NT.
  Pop $0
  Push 0
  Return
 
  IsNT_yes:
    ; NT!!!
    Pop $0
    Push 1
FunctionEnd
!macroend
!insertmacro IsNT ""
!insertmacro IsNT "un."
;====================================================
; AddToPath - Adds the given dir to the search path.
;        Input - head of the stack
;        Note - Win9x systems requires reboot
;====================================================
Function AddToPath
   Exch $0
   Push $1
   Push $2
  
   Call IsNT
   Pop $1
   StrCmp $1 1 AddToPath_NT
      ; Not on NT
      StrCpy $1 $WINDIR 2
      FileOpen $1 "$1\autoexec.bat" a
      FileSeek $1 0 END
      GetFullPathName /SHORT $0 $0
      FileWrite $1 "$\r$\nSET PATH=%PATH%;$0$\r$\n"
      FileClose $1
      Goto AddToPath_done
 
   AddToPath_NT:
      Push $4
      Push "all"
      Pop  $4
      AddToPath_NT_selection_done:
      StrCmp $4 "current" read_path_NT_current
         ReadRegStr $1 ${NT_all_env} "PATH"
         Goto read_path_NT_resume
      read_path_NT_current:
         ReadRegStr $1 ${NT_current_env} "PATH"
      read_path_NT_resume:
      StrCpy $2 $0
      StrCmp $1 "" AddToPath_NTdoIt
         StrCpy $2 "$1;$0"
      AddToPath_NTdoIt:
         StrCmp $4 "current" write_path_NT_current
            ClearErrors
            WriteRegExpandStr ${NT_all_env} "PATH" $2
            IfErrors 0 write_path_NT_resume
            MessageBox MB_YESNO|MB_ICONQUESTION "The path could not be set for all users$\r$\nShould I try for the current user?" \
               IDNO write_path_NT_failed
            ; change selection
            StrCpy $4 "current"
            Goto AddToPath_NT_selection_done
         write_path_NT_current:
            ClearErrors
            WriteRegExpandStr ${NT_current_env} "PATH" $2
            IfErrors 0 write_path_NT_resume
            MessageBox MB_OK|MB_ICONINFORMATION "The path could not be set for the current user."
            Goto write_path_NT_failed
         write_path_NT_resume:
         SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment" /TIMEOUT=5000
         DetailPrint "added path for user ($4), $0"
         write_path_NT_failed:
      
      Pop $4
   AddToPath_done:
   Pop $2
   Pop $1
   Pop $0
FunctionEnd
 
;====================================================
; RemoveFromPath - Remove a given dir from the path
;     Input: head of the stack
;====================================================
Function un.RemoveFromPath
   Exch $0
   Push $1
   Push $2
   Push $3
   Push $4
   
   Call un.IsNT
   Pop $1
   StrCmp $1 1 unRemoveFromPath_NT
      ; Not on NT
      StrCpy $1 $WINDIR 2
      FileOpen $1 "$1\autoexec.bat" r
      GetTempFileName $4
      FileOpen $2 $4 w
      GetFullPathName /SHORT $0 $0
      StrCpy $0 "SET PATH=%PATH%;$0"
      SetRebootFlag true
      Goto unRemoveFromPath_dosLoop
     
      unRemoveFromPath_dosLoop:
         FileRead $1 $3
         StrCmp $3 "$0$\r$\n" unRemoveFromPath_dosLoop
         StrCmp $3 "$0$\n" unRemoveFromPath_dosLoop
         StrCmp $3 "$0" unRemoveFromPath_dosLoop
         StrCmp $3 "" unRemoveFromPath_dosLoopEnd
         FileWrite $2 $3
         Goto unRemoveFromPath_dosLoop
 
      unRemoveFromPath_dosLoopEnd:
         FileClose $2
         FileClose $1
         StrCpy $1 $WINDIR 2
         Delete "$1\autoexec.bat"
         CopyFiles /SILENT $4 "$1\autoexec.bat"
         Delete $4
         Goto unRemoveFromPath_done
 
   unRemoveFromPath_NT:
      StrLen $2 $0
      Push "all"
      Pop  $4
 
      StrCmp $4 "current" un_read_path_NT_current
         ReadRegStr $1 ${NT_all_env} "PATH"
         Goto un_read_path_NT_resume
      un_read_path_NT_current:
         ReadRegStr $1 ${NT_current_env} "PATH"
      un_read_path_NT_resume:
 
      Push $1
      Push $0
      Call un.StrStr ; Find $0 in $1
      Pop $0 ; pos of our dir
      IntCmp $0 -1 unRemoveFromPath_done
         ; else, it is in path
         StrCpy $3 $1 $0 ; $3 now has the part of the path before our dir
         IntOp $2 $2 + $0 ; $2 now contains the pos after our dir in the path (';')
         IntOp $2 $2 + 1 ; $2 now containts the pos after our dir and the semicolon.
         StrLen $0 $1
         StrCpy $1 $1 $0 $2
         StrCpy $3 "$3$1"
 
         StrCmp $4 "current" un_write_path_NT_current
            WriteRegExpandStr ${NT_all_env} "PATH" $3
            Goto un_write_path_NT_resume
         un_write_path_NT_current:
            WriteRegExpandStr ${NT_current_env} "PATH" $3
         un_write_path_NT_resume:
         SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment" /TIMEOUT=5000
   unRemoveFromPath_done:
   Pop $4
   Pop $3
   Pop $2
   Pop $1
   Pop $0
FunctionEnd
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Uninstall sutff
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 
 
;====================================================
; StrStr - Finds a given string in another given string.
;               Returns -1 if not found and the pos if found.
;          Input: head of the stack - string to find
;                      second in the stack - string to find in
;          Output: head of the stack
;====================================================
Function un.StrStr
  Push $0
  Exch
  Pop $0 ; $0 now have the string to find
  Push $1
  Exch 2
  Pop $1 ; $1 now have the string to find in
  Exch
  Push $2
  Push $3
  Push $4
  Push $5
 
  StrCpy $2 -1
  StrLen $3 $0
  StrLen $4 $1
  IntOp $4 $4 - $3
 
  unStrStr_loop:
    IntOp $2 $2 + 1
    IntCmp $2 $4 0 0 unStrStrReturn_notFound
    StrCpy $5 $1 $3 $2
    StrCmp $5 $0 unStrStr_done unStrStr_loop
 
  unStrStrReturn_notFound:
    StrCpy $2 -1
 
  unStrStr_done:
    Pop $5
    Pop $4
    Pop $3
    Exch $2
    Exch 2
    Pop $0
    Pop $1
FunctionEnd
;====================================================

Function .onInstSuccess
	MessageBox MB_OK  "Note! The default web console login username and password is 'admin'. Visit http://knowledge.mailarchiva.com for tuning instructions."
FunctionEnd

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
 