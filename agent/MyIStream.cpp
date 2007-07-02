
#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <windows.h>
#include <objidl.h>
#include <malloc.h>
#include <assert.h>
#include <comdef.h>
#include <Guiddef.h>
#include <limits.h>

#ifdef realloc
#  undef realloc
#endif

#ifdef free
# undef free
#endif

class MyIStream : public IStream
{
private:
  BOOL UpdateTime(FILETIME& ft)
  {
    SYSTEMTIME st;
    GetSystemTime(&st);
    return SystemTimeToFileTime(&st, &ft);
  }

public:
  MyIStream::MyIStream() : m_lRef(0), m_pvBuf(NULL), m_dwBufSiz(0), m_dwBufPos(0), m_bReverted(false)
  {
    memset(&m_statstg, 0, sizeof(STATSTG));
    m_statstg.type=STGTY_STORAGE;
    m_statstg.clsid=CLSID_NULL;
    UpdateTime(m_statstg.ctime);
    m_statstg.mtime=m_statstg.ctime;
    m_statstg.atime=m_statstg.ctime;
  }

  MyIStream::~MyIStream()
  {
    assert(m_lRef==0);
    if(m_pvBuf) free(m_pvBuf);
  }

  HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void** ppvObj)
  {
    *ppvObj=NULL;
    if(
      IID_IUnknown == riid ||
      IID_IStream == riid
      ) *ppvObj = (LPUNKNOWN)this;                                              // return pointer to self
    else return E_NOINTERFACE;                                                  // we have no interface provided for that query

    ((LPUNKNOWN)(*ppvObj))->AddRef();                                           // addref instance
    return S_OK;
  }

  ULONG STDMETHODCALLTYPE AddRef(void)
  {
    return InterlockedIncrement(&m_lRef);                                       // the calls to the Win32 functions InterlockedIncrement and InterlockedDecrement are to ensure thread safety
  }

  ULONG STDMETHODCALLTYPE Release(void)
  {
    long lRef=InterlockedDecrement(&m_lRef);
    assert(lRef>=0);
    return lRef;
  }

  HRESULT STDMETHODCALLTYPE Read(void* pv, ULONG cb, ULONG *pcbRead)
  {
    if(!m_pvBuf) return S_FALSE;
    if(!pv) return STG_E_INVALIDPOINTER;
    if(m_bReverted) return STG_E_REVERTED;
    DWORD dwRead=min(m_dwBufSiz-m_dwBufPos, cb);
    if(dwRead<0) return S_FALSE;
    memcpy((BYTE*)pv+m_dwBufPos, pv, dwRead);
    if(pcbRead) *pcbRead=dwRead;
    m_dwBufPos+=dwRead;
    if(!UpdateTime(m_statstg.atime)) return E_FAIL;
    return S_OK;
  }

  HRESULT STDMETHODCALLTYPE Write(const void* pv, ULONG cb, ULONG* pcbWritten)
  {
    if(!pv) return STG_E_INVALIDPOINTER;
    if(m_bReverted) return STG_E_REVERTED;
    if(m_dwBufSiz-m_dwBufPos<cb)
    {
      m_dwBufSiz=m_dwBufPos+cb;
      m_pvBuf=realloc(m_pvBuf, m_dwBufSiz);
      if(!m_pvBuf) return STG_E_CANTSAVE;
    }
    memcpy((BYTE*)m_pvBuf+m_dwBufPos, pv, cb);
    m_dwBufPos+=cb;
    if(pcbWritten) *pcbWritten=cb;
    if(!UpdateTime(m_statstg.mtime)) return E_FAIL;
	unsigned char* testbuffer = (unsigned char*)pv;
    return S_OK;
  }


  HRESULT STDMETHODCALLTYPE Seek(LARGE_INTEGER dlibMove, DWORD dwOrigin, ULARGE_INTEGER* plibNewPosition)
  {
    LONGLONG ll;
    if(m_bReverted) return STG_E_REVERTED;
    switch(dwOrigin)
    {
    case STREAM_SEEK_SET:
      if(dlibMove.QuadPart<0 || dlibMove.QuadPart>=ULONG_MAX) return STG_E_INVALIDFUNCTION;
      m_dwBufPos=(DWORD)dlibMove.QuadPart;
      break;
    case STREAM_SEEK_CUR:
      ll=m_dwBufPos+dlibMove.QuadPart;
      if(ll<0 || ll>=ULONG_MAX) return STG_E_INVALIDFUNCTION;
      m_dwBufPos=(DWORD)ll;
    case STREAM_SEEK_END:
      ll=m_dwBufSiz+dlibMove.QuadPart;
      if(ll<0 || ll>ULONG_MAX) return STG_E_INVALIDFUNCTION;
      m_dwBufPos=(DWORD)ll;
      break;
    } // switch(dwOrigin)

    if(plibNewPosition) plibNewPosition->QuadPart=m_dwBufPos;
    if(!UpdateTime(m_statstg.atime)) return E_FAIL;
    return S_OK;
  }

  HRESULT STDMETHODCALLTYPE SetSize(ULARGE_INTEGER libNewSize)
  {
    if(m_bReverted) return STG_E_REVERTED;
    if(libNewSize.QuadPart>=ULONG_MAX) return STG_E_INVALIDFUNCTION;

    m_pvBuf=realloc(m_pvBuf, (DWORD)libNewSize.QuadPart);
    if(!UpdateTime(m_statstg.mtime)) return E_FAIL;
    return S_OK;
  }

  HRESULT STDMETHODCALLTYPE CopyTo(IStream *pstm, ULARGE_INTEGER cb, ULARGE_INTEGER *pcbRead, ULARGE_INTEGER *pcbWritten)
  {
    HRESULT hr=S_OK;
    if(!m_pvBuf) return S_FALSE;
    if(m_bReverted) return STG_E_REVERTED;
    if(cb.QuadPart>ULONG_MAX) return STG_E_INVALIDFUNCTION;
    DWORD dwRead=min(m_dwBufSiz-m_dwBufPos, (DWORD)cb.QuadPart), dwWritten;
    if(dwRead<0) return S_FALSE;
    if(pcbRead) pcbRead->QuadPart=dwRead;
    hr=pstm->Write((BYTE*)m_pvBuf+m_dwBufPos, dwRead, &dwWritten);

    if(pcbWritten) pcbWritten->QuadPart=dwWritten;
    if(!UpdateTime(m_statstg.atime)) return E_FAIL;
    return hr;
  }

  HRESULT STDMETHODCALLTYPE Commit(DWORD grfCommitFlags)
  {
    if(m_bReverted) return STG_E_REVERTED;
    if(!UpdateTime(m_statstg.atime)) return E_FAIL;
    return S_OK;
  }

  HRESULT STDMETHODCALLTYPE Revert(void)
  {
    m_bReverted=true;
    if(!UpdateTime(m_statstg.atime)) return E_FAIL;
    return S_OK;
  }

  HRESULT STDMETHODCALLTYPE LockRegion(ULARGE_INTEGER libOffset, ULARGE_INTEGER cb, DWORD dwLockType)
  { return STG_E_INVALIDFUNCTION; }

  HRESULT STDMETHODCALLTYPE UnlockRegion(ULARGE_INTEGER libOffset, ULARGE_INTEGER cb, DWORD dwLockType)
  { return STG_E_INVALIDFUNCTION; }


  
  HRESULT STDMETHODCALLTYPE Clone(IStream** ppstm)
  {
    assert(false);
    return STG_E_INVALIDFUNCTION;
  }


  HRESULT STDMETHODCALLTYPE Stat(STATSTG* pstatstg,DWORD grfStatFlag) 
  {
    pstatstg = &m_statstg; // very nasty... 
	return S_OK;
  }



  HRESULT STDMETHODCALLTYPE GetPtr(void** ppv)
  {
    if(!ppv) return STG_E_INVALIDPOINTER;
    if(m_bReverted) return STG_E_REVERTED;
    *ppv=m_pvBuf;
	unsigned char* testbuffer = (unsigned char*)m_pvBuf;
    return S_OK;
  }

  HRESULT STDMETHODCALLTYPE GetSize(DWORD* bufSize) {
	if (!bufSize) return STG_E_INVALIDPOINTER;
	*bufSize=m_dwBufSiz;
	return S_OK;
  }

private:
  long m_lRef;
  void* m_pvBuf;
  DWORD m_dwBufSiz, m_dwBufPos;
  bool m_bReverted;
  STATSTG m_statstg;
};


