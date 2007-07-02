

LanguageNames =
    (
        English = 0x0409:Messages_ENU
    )


;////////////////////////////////////////
;// Eventlog categories
;//
;// Categories always have to be the first entries in a message file!
;//

MessageId       = 1
SymbolicName    = CATEGORY_ONE
Severity        = Success
Language        = English
First category event
.


MessageId       = +1
SymbolicName    = CATEGORY_TWO
Severity        = Success
Language        = English
Second category event
.


;////////////////////////////////////////
;// Events
;//

MessageId       = +1
SymbolicName    = EVENT_STARTED_BY
Language        = English
MailArchiva Agent started successfully by user %1
.


MessageId       = +1
SymbolicName    = RFC822_DLL_ERROR
Language        = English
Failed to load DLL %1. This is necessary for RFC822 message conversion
.

MessageId       = +1
SymbolicName    = GET_CONFIGURATION_ERROR
Language        = English
Error occured while retrieving config parameters from server. %1
.

MessageId       = +1
SymbolicName    = MAPI_START_ERROR
Language        = English
Failed to start MAPI. Please check that MAPI is configured correctly.
.

MessageId       = +1
SymbolicName    = MAPI_LOGON_ERROR
Language        = English
Could not logon to MAPI account %1. Make sure your MAPI account exists. Service must run on account with sufficient priviledges.
.

MessageId       = +1
SymbolicName    = MAPI_PROFILE_ERROR
Language        = English
Could not create MAPI profile %1. 
.

MessageId       = +1
SymbolicName    = MAPI_OPEN_MESSAGESTORE_ERROR
Language        = English
Failed to open default MAPI message store. Please check that MAPI is configured correctly.
.

MessageId       = +1
SymbolicName    = MAPI_OPEN_INBOX_ERROR
Language        = English
Failed to open inbox associated with MAPI message store. Please check that MAPI is configured correctly.
.

MessageId       = +1
SymbolicName    = APPLICATION_STARTED_INFORMATION
Language        = English
MailArchiva Agent has started successfully.
.

MessageId       = +1
SymbolicName    = APPLICATION_SHUTDOWN_INFORMATION
Language        = English
MailArchiva has shutdown.
.

MessageId       = +1
SymbolicName    = MAPI_INBOX_CONTENTS_ERROR
Language        = English
Failed to retrieve the contents of MAPI inbox
.

MessageId       = +1
SymbolicName    = USER_ACCOUNT_LOGIN_ERROR
Language        = English
Failed to login to the user account. %1
.

MessageId       = +1
SymbolicName    = CREATE_MAPI_PROFILE_ERROR
Language        = English
Failed to create a MAPI profile. %1
.

MessageId       = +1
SymbolicName    = RFC822_MESSAGE_CONVERSION_ERROR
Language        = English
Failed to convert message to RFC822 format
.

MessageId       = +1
SymbolicName    = ARCHIVE_MESSAGE_ERROR
Language        = English
Failed to archive message. Check server logs. Error %1.
.

MessageId       = +1
SymbolicName    = MAPI_OPEN_MESSAGE_ERROR
Language        = English
Failed to open message using MAPI store.
.

MessageId       = +1
SymbolicName    = FATAL_ERROR
Language        = English
MailArchiva Agent service is stopped due to a fatal error. Error %1
.

MessageId       = +1
SymbolicName    = LOAD_MAPIAPI_DLL
Language        = English
Failed to load mailapi.dll. Please ensure that it exists on the path. Error %1 
.

MessageId       = +1
SymbolicName    = INCORRECT_OUTLOOK_VERSION
Language        = English
Warning: Microsoft Office Outlook 2003 Service Pack 1 (version 11.0.6359.0) or greater must be installed. Current version is %1 
.

MessageId       = +1
SymbolicName    = OUTLOOK_OUTLMIME_DLL
Language        = English
MailArchiva Agent could not locate a required component of Outlook, outlmime.dll. 
.

MessageId       = +1
SymbolicName    = OUTLOOK_DETECTION
Language        = English
Warning: could not retrieve Outlook versioning information. %1
.

MessageId       = +1
SymbolicName    = EXCHANGE_NOT_RESPONDING
Language        = English
Exchange is not responding. Will keep trying to reach it..
.

MessageId       = +1
SymbolicName    = EXCHANGE_RESPONDING
Language        = English
Exchange is back online. Email archiving proceeding..
.
