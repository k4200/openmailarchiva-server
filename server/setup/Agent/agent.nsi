
;--------------------------------

; The name of the installer
Name "MailArchiva Agent v1.2.1"

; The file to write
OutFile "C:\dev\sourcedir\server\setup\Out\agentsetup.exe"

AllowRootDirInstall false
SetDateSave on
SetDatablockOptimize on
CRCCheck on
XPStyle on
ShowUninstDetails nevershow
ShowInstDetails nevershow
AddBrandingImage left 135 0
BrandingText "Copyright Jamie Band 2006"
; The default installation directory
InstallDir $PROGRAMFILES\MailArchiva
LicenseData license_open_source_edition.rtf

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\MailArchiva\Agent" "Install_Dir"

;--------------------------------

Function .onGUIInit
   SetBrandingImage "C:\dev\sourcedir\server\setup\Agent\banner.bmp"
 FunctionEnd
 
; Pages

Page license
Page directory

Page custom customPage "" ": Settings"
Page instfiles
UninstPage uninstConfirm un.uninstImage
UninstPage instfiles

;--------------------------------




; The stuff to install
Section "MailArchiva Agent (required)"

  SectionIn RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  ; Put file there
  File /r "C:\dev\sourcedir\server\setup\Agent\Agent"
  
  ; Write the installation path into the registry
  WriteRegStr HKLM "SOFTWARE\MailArchiva\Agent" "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaAgent" "DisplayName" "MailArchiva Agent"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaAgent" "UninstallString" '"$INSTDIR\uninstallagent.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaAgent" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaAgent" "NoRepair" 1
  WriteUninstaller "uninstallagent.exe"
  ExecWait '$INSTDIR\Agent\MailArchivaAgent.exe -uninstall' $0
  ExecWait '$INSTDIR\Agent\MailArchivaAgent.exe -installexchange $R8 $R4 $R5 $R6' $0
  goto finished
startservice:
  ;MessageBox MB_YESNO "Start the MailArchiva Agent service now?" IDNO finished
  ;ExecWait '$INSTDIR\Agent\MailArchivaAgent.exe -start' $0
  ;StrCmp $0 "0" finished
  ;MessageBox MB_OK "The MailArchiva agent could not be started. Please check the Windows Event Log for details. Error Code:$\r$\n$0"
finished:
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  ;CreateDirectory "$SMPROGRAMS\Stimulus\Archiva"
  ;CreateShortCut "$SMPROGRAMS\Stimulus\Archiva\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  
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
	SetBrandingImage "C:\dev\sourcedir\server\setup\Agent\brandingimage.bmp"
FunctionEnd

Section "Uninstall"
  ExecWait '$INSTDIR\Agent\MailArchivaAgent.exe -uninstall' $0
  StrCmp $0 "0" uninstall
    MessageBox MB_OK "Service Unnstallation Failed:$\r$\n$0"
uninstall:
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MailArchivaAgent"
  DeleteRegKey HKLM SOFTWARE\MailArchiva\Agent

  ; Remove files and uninstaller
  Delete $INSTDIR\Agent\Archivaagent.exe
  Delete $INSTDIR\uninstallagent.exe

  ; Remove shortcuts, if any
  ; Delete "$SMPROGRAMS\Stimulus\ArchivaAgent\*.*"

  ; Remove directories used
  ; RMDir "$SMPROGRAMS\Agent"
  RMDir "$INSTDIR\Agent"
  RMDir "$INSTDIR"

SectionEnd