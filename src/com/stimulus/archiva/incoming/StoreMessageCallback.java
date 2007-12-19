package com.stimulus.archiva.incoming;

import java.io.InputStream;
import com.stimulus.archiva.exception.ArchiveException;

public interface StoreMessageCallback {
	
	void store(InputStream is, String ipAddress) throws ArchiveException;
}