package org.subethamail.smtp.server.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DummySSLSocketFactory extends SSLSocketFactory
{
    public class DummyTrustManager implements X509TrustManager
    {

        public void checkClientTrusted(X509Certificate[] cert, String authType)
        {
        }

        public void checkServerTrusted(X509Certificate[] cert, String authType)
        {
        }

        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }
    }

    private SSLSocketFactory factory;
    private SSLContext sslcontext;

    public DummySSLSocketFactory()
    {
        try
        {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null,
                    new TrustManager[] { new DummyTrustManager() }, new SecureRandom());
            factory = sslcontext.getSocketFactory();
        }
        catch (Exception ex)
        {
            // ignore
        }
    }

    public static SocketFactory getDefault()
    {
        return new DummySSLSocketFactory();
    }

    @Override
	public Socket createSocket(Socket socket, String s, int i, boolean flag)
            throws IOException
    {
        return factory.createSocket(socket, s, i, flag);
    }

    @Override
	public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1,
            int j) throws IOException
    {
        return factory.createSocket(inaddr, i, inaddr1, j);
    }

    @Override
	public Socket createSocket(InetAddress inaddr, int i) throws IOException
    {
        return factory.createSocket(inaddr, i);
    }

    @Override
	public Socket createSocket(String s, int i, InetAddress inaddr, int j)
            throws IOException
    {
        return factory.createSocket(s, i, inaddr, j);
    }

    @Override
	public Socket createSocket(String s, int i) throws IOException
    {
        return factory.createSocket(s, i);
    }

    @Override
	public String[] getDefaultCipherSuites()
    {
        return factory.getDefaultCipherSuites();
    }

    @Override
	public Socket createSocket() throws IOException
    {
        return factory.createSocket();
    }

    @Override
	public String[] getSupportedCipherSuites()
    {
        return factory.getSupportedCipherSuites();
    }

    public SSLContext getSSLContext()
    {
        return sslcontext;
    }
}