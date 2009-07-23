
/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

 package com.stimulus.archiva.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.*;

public class DummySSLSocketFactory extends SSLSocketFactory {
    private SSLSocketFactory factory;

    public DummySSLSocketFactory() {
		try {
		    SSLContext sslcontext = SSLContext.getInstance("TLS","SunJSSE");
		    sslcontext.init(null,new TrustManager[] { new DummyTrustManager()},null);
		    factory = sslcontext.getSocketFactory();
		} catch(Exception ex) {
		    // ignore
		}
    }

    @Override
	public Socket createSocket() throws IOException
    {
    	return factory.createSocket();
    }

    public static SocketFactory getDefault() {
   	return new DummySSLSocketFactory();
    }

    @Override
	public Socket createSocket(Socket socket, String s, int i, boolean flag)
				throws IOException {
    	return factory.createSocket(socket, s, i, flag);
    }

    @Override
	public Socket createSocket(InetAddress inaddr, int i,
				InetAddress inaddr1, int j) throws IOException {
    	return factory.createSocket(inaddr, i, inaddr1, j);
    }

    @Override
	public Socket createSocket(InetAddress inaddr, int i)
				throws IOException {
    	return factory.createSocket(inaddr, i);
    }

    @Override
	public Socket createSocket(String s, int i, InetAddress inaddr, int j)
				throws IOException {
    	return factory.createSocket(s, i, inaddr, j);
    }

    @Override
	public Socket createSocket(String s, int i) throws IOException {
    	return factory.createSocket(s, i);
    }

    @Override
	public String[] getDefaultCipherSuites() {
    	return factory.getDefaultCipherSuites();
    }

    @Override
	public String[] getSupportedCipherSuites() {
    	return factory.getSupportedCipherSuites();
    }
}

