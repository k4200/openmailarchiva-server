////////////////////////////////////////
// Eventlog categories
//
// Categories always have to be the first entries in a message file!
//
//
//  Values are 32 bit values layed out as follows:
//
//   3 3 2 2 2 2 2 2 2 2 2 2 1 1 1 1 1 1 1 1 1 1
//   1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0
//  +---+-+-+-----------------------+-------------------------------+
//  |Sev|C|R|     Facility          |               Code            |
//  +---+-+-+-----------------------+-------------------------------+
//
//  where
//
//      Sev - is the severity code
//
//          00 - Success
//          01 - Informational
//          10 - Warning
//          11 - Error
//
//      C - is the Customer code flag
//
//      R - is a reserved bit
//
//      Facility - is the facility code
//
//      Code - is the facility's status code
//
//
// Define the facility codes
//


//
// Define the severity codes
//


//
// MessageId: CATEGORY_ONE
//
// MessageText:
//
//  First category event
//
#define CATEGORY_ONE                     0x00000001L

//
// MessageId: CATEGORY_TWO
//
// MessageText:
//
//  Second category event
//
#define CATEGORY_TWO                     0x00000002L

////////////////////////////////////////
// Events
//
//
// MessageId: EVENT_STARTED_BY
//
// MessageText:
//
//  MailArchiva Agent started successfully by user %1
//
#define EVENT_STARTED_BY                 0x00000003L

//
// MessageId: RFC822_DLL_ERROR
//
// MessageText:
//
//  Failed to load DLL %1. This is necessary for RFC822 message conversion
//
#define RFC822_DLL_ERROR                 0x00000004L

//
// MessageId: GET_CONFIGURATION_ERROR
//
// MessageText:
//
//  Error occured while retrieving config parameters from server. %1
//
#define GET_CONFIGURATION_ERROR          0x00000005L

//
// MessageId: MAPI_START_ERROR
//
// MessageText:
//
//  Failed to start MAPI. Please check that MAPI is configured correctly.
//
#define MAPI_START_ERROR                 0x00000006L

//
// MessageId: MAPI_LOGON_ERROR
//
// MessageText:
//
//  Could not logon to MAPI account %1. Make sure your MAPI account exists. Service must run on account with sufficient priviledges.
//
#define MAPI_LOGON_ERROR                 0x00000007L

//
// MessageId: MAPI_PROFILE_ERROR
//
// MessageText:
//
//  Could not create MAPI profile %1. 
//
#define MAPI_PROFILE_ERROR               0x00000008L

//
// MessageId: MAPI_OPEN_MESSAGESTORE_ERROR
//
// MessageText:
//
//  Failed to open default MAPI message store. Please check that MAPI is configured correctly.
//
#define MAPI_OPEN_MESSAGESTORE_ERROR     0x00000009L

//
// MessageId: MAPI_OPEN_INBOX_ERROR
//
// MessageText:
//
//  Failed to open inbox associated with MAPI message store. Please check that MAPI is configured correctly.
//
#define MAPI_OPEN_INBOX_ERROR            0x0000000AL

//
// MessageId: APPLICATION_STARTED_INFORMATION
//
// MessageText:
//
//  MailArchiva Agent has started successfully.
//
#define APPLICATION_STARTED_INFORMATION  0x0000000BL

//
// MessageId: APPLICATION_SHUTDOWN_INFORMATION
//
// MessageText:
//
//  MailArchiva has shutdown.
//
#define APPLICATION_SHUTDOWN_INFORMATION 0x0000000CL

//
// MessageId: MAPI_INBOX_CONTENTS_ERROR
//
// MessageText:
//
//  Failed to retrieve the contents of MAPI inbox
//
#define MAPI_INBOX_CONTENTS_ERROR        0x0000000DL

//
// MessageId: USER_ACCOUNT_LOGIN_ERROR
//
// MessageText:
//
//  Failed to login to the user account. %1
//
#define USER_ACCOUNT_LOGIN_ERROR         0x0000000EL

//
// MessageId: CREATE_MAPI_PROFILE_ERROR
//
// MessageText:
//
//  Failed to create a MAPI profile. %1
//
#define CREATE_MAPI_PROFILE_ERROR        0x0000000FL

//
// MessageId: RFC822_MESSAGE_CONVERSION_ERROR
//
// MessageText:
//
//  Failed to convert message to RFC822 format
//
#define RFC822_MESSAGE_CONVERSION_ERROR  0x00000010L

//
// MessageId: ARCHIVE_MESSAGE_ERROR
//
// MessageText:
//
//  Failed to archive message. Check server logs. Error %1.
//
#define ARCHIVE_MESSAGE_ERROR            0x00000011L

//
// MessageId: MAPI_OPEN_MESSAGE_ERROR
//
// MessageText:
//
//  Failed to open message using MAPI store.
//
#define MAPI_OPEN_MESSAGE_ERROR          0x00000012L

//
// MessageId: FATAL_ERROR
//
// MessageText:
//
//  MailArchiva Agent service is stopped due to a fatal error. Error %1
//
#define FATAL_ERROR                      0x00000013L

//
// MessageId: LOAD_MAPIAPI_DLL
//
// MessageText:
//
//  Failed to load mailapi.dll. Please ensure that it exists on the path. Error %1 
//
#define LOAD_MAPIAPI_DLL                 0x00000014L

//
// MessageId: INCORRECT_OUTLOOK_VERSION
//
// MessageText:
//
//  Warning: Microsoft Office Outlook 2003 Service Pack 1 (version 11.0.6359.0) or greater must be installed. Current version is %1 
//
#define INCORRECT_OUTLOOK_VERSION        0x00000015L

//
// MessageId: OUTLOOK_OUTLMIME_DLL
//
// MessageText:
//
//  MailArchiva Agent could not locate a required component of Outlook, outlmime.dll. 
//
#define OUTLOOK_OUTLMIME_DLL             0x00000016L

//
// MessageId: OUTLOOK_DETECTION
//
// MessageText:
//
//  Warning: could not retrieve Outlook versioning information. %1
//
#define OUTLOOK_DETECTION                0x00000017L

//
// MessageId: EXCHANGE_NOT_RESPONDING
//
// MessageText:
//
//  Exchange is not responding. Will keep trying to reach it..
//
#define EXCHANGE_NOT_RESPONDING          0x00000018L

//
// MessageId: EXCHANGE_RESPONDING
//
// MessageText:
//
//  Exchange is back online. Email archiving proceeding..
//
#define EXCHANGE_RESPONDING              0x00000019L

