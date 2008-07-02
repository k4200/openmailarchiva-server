package com.stimulus.archiva.domain;

import java.io.InputStream;
import com.stimulus.archiva.exception.ArchiveException;

public interface FetchMessageCallback {

	public void store(InputStream is, String remoteIP) throws ArchiveException;
}
