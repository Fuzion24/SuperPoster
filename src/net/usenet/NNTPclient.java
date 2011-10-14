package net.usenet;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;



public class NNTPclient {

    private Socket nntpClient;
    private SSLSocket sslNNTPclient;
    private SocketAddress sockAdd = null;
    private BufferedReader nntpReader = null;
    private OutputStream nntpWriter = null;
    private PosterSettings posterSettings = null;
    public Boolean postingAllowed;
    public String status;
    public int KBperSec = 0;
    private Boolean connected = false;
    private Boolean debug = true;

    public NNTPclient(PosterSettings settings) {
        posterSettings = settings;
    }

    @Override
    protected void finalize() throws Throwable {
        disconnect();
        super.finalize();
    }

    private static void printSocketInfo(SSLSocket s) {
        System.out.println("Socket class: " + s.getClass());
        System.out.println("   Remote address = "
                + s.getInetAddress().toString());
        System.out.println("   Remote port = " + s.getPort());
        System.out.println("   Local socket address = "
                + s.getLocalSocketAddress().toString());
        System.out.println("   Local address = "
                + s.getLocalAddress().toString());
        System.out.println("   Local port = " + s.getLocalPort());
        System.out.println("   Need client authentication = "
                + s.getNeedClientAuth());
        SSLSession ss = s.getSession();
        System.out.println("   Cipher suite = " + ss.getCipherSuite());
        System.out.println("   Protocol = " + ss.getProtocol());
    }

    public void connect() throws NNTPException {
        if (posterSettings == null) {
            throw new NNTPException("No Server Settings have been initialized");
        }
        NNTPresponse currentRes = null;
        if (posterSettings.requiresSSL == true) {
            SSLSocketFactory f;
            try {
                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs,
                        String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs,
                        String authType) {
                }
            }};

                // Install the all-trusting trust manager

                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                f = sc.getSocketFactory();
                InetAddress addr = InetAddress.getByName(posterSettings.hostName);
                sockAdd = new InetSocketAddress(addr, posterSettings.port);
                sslNNTPclient = (SSLSocket) f.createSocket();
                sslNNTPclient.connect(sockAdd, posterSettings.connectTimeoutMs);
                if (debug) {
                    printSocketInfo(sslNNTPclient);
                }
                sslNNTPclient.startHandshake();
                nntpWriter = sslNNTPclient.getOutputStream();
                nntpReader = new BufferedReader(new InputStreamReader(
						sslNNTPclient.getInputStream(), "ISO-8859-1"));
                currentRes = getResponse(null);

                if ((currentRes.Code != 200) && (currentRes.Code != 201)) {
                    disconnect();
                    throw new NNTPException("Error Connecting to Server: "
                            + currentRes.Info);
                }

                if (currentRes.Code == 200) {
                    postingAllowed = true;
                } else {
                    postingAllowed = false;
                }

                if (posterSettings.requiresLogin) {
                    Authenticate(posterSettings.userName,
                    		posterSettings.passWord);
                }
            } catch (NNTPException e) {
                throw e;
            } catch (Exception e) {
                throw new NNTPException("Error connecting to server: "
                        + e.toString());
            }

        } else {
            try {
                InetAddress addr = InetAddress.getByName(posterSettings.hostName);
                sockAdd = new InetSocketAddress(addr, posterSettings.port);
                nntpClient = new Socket();
                nntpClient.connect(sockAdd, posterSettings.connectTimeoutMs);

                nntpWriter = nntpClient.getOutputStream();
                nntpReader = new BufferedReader(new InputStreamReader(
						nntpClient.getInputStream(), "ISO-8859-1"));
                currentRes = getResponse(null);

                if ((currentRes.Code != 200) && (currentRes.Code != 201)) {
                    disconnect();
                    throw new NNTPException("Error Connecting to Server: "
                            + currentRes.Info);
                }

                if (currentRes.Code == 200) {
                    postingAllowed = true;
                } else {
                    postingAllowed = false;
                }

                if (posterSettings.requiresLogin) {
                    Authenticate(posterSettings.userName,
                    		posterSettings.passWord);
                }

            } catch (NNTPException e) {
                throw e;
            } catch (Exception e) {
            	e.printStackTrace();
                throw new NNTPException("Error connecting to server: "
                        + e.toString());
            }
        }
        connected = true;
    }

    public Boolean isConnected() {
        connected = nntpClient.isConnected();
        return connected;
    }

    public void selectGroup(String group) throws NNTPException {
        NNTPresponse res = getResponse("GROUP " + group);
        if (res.Code != 211) {
            throw new NNTPException("Error switching to group: " + res.Info);
        }
    }

  
    private void sendLine(String message) throws NNTPException {
        // Ensures that a CR/LF is sent at the end of every line
        message += "\r\n";
        if (debug) {
            System.out.print("Sent: " + message);
        }
        byte[] byteMsg = message.getBytes();

        try {
        	
            nntpWriter.write(byteMsg);
            nntpWriter.flush();
        } catch (IOException e) {

            throw new NNTPException("Error Sending to server: " + e.toString());

        }

    }

    private Boolean Authenticate(String Username, String Password)
            throws NNTPException {

        NNTPresponse res = getResponse("AUTHINFO USER " + Username);

        if (res.Code == 381) // More Authentication Information Required
        {
            res = getResponse("AUTHINFO PASS " + Password);
        }
        if (res.Code != 281) // Authentication rejected
        {
            throw new NNTPException("Error authenticating with server: "
                    + res.Info);
        }

        return true;
    }

    public void disconnect() {
        try {
            NNTPresponse res = getResponse("QUIT");
            if (res.Code != 205) {
                throw new NNTPException("Server did not disconnect cleanly: "
                        + res.Info);
            }
            nntpClient.close();
            nntpWriter = null;
            nntpReader = null;
        } catch (Exception e) {
            //TODO: Handle this guy
        }
    }
    
    public void post(String subject, String from, String newsgroup, String header, byte[] fullMessage, String trailer) throws Exception
    {
    
    	NNTPresponse res = getResponse("POST");
        if (res.Code != 340) {
            throw new NNTPException("Cannot post: "
                    + res.Info);
        }
                
        String postHeader = "From: " + from + "\r\n" +
        					"Newsgroups: " + newsgroup + "\r\n" + //TODO: Handle multiple newsgroups
        					"Subject: " + subject + "\r\n" +
        					"Organization: turboPoster \r\n\r\n";
        
        System.out.print(postHeader);
        nntpWriter.write(postHeader.getBytes());
        nntpWriter.write(header.getBytes());
        nntpWriter.write(fullMessage);
		nntpWriter.write(0x0D);
		nntpWriter.write(0x0A);
        nntpWriter.write(trailer.getBytes());
        //Write .\r\n
		nntpWriter.write(0x0D);
		nntpWriter.write(0x0A);
		nntpWriter.write(0x2E);
		nntpWriter.write(0x0D);
		nntpWriter.write(0x0A);
	
        nntpWriter.flush(); 
        res = getResponse(null);
        
    }
    private NNTPresponse getResponse(String message) throws NNTPException {

        String lineRead = null;

        try {
            if (message != null) {
                sendLine(message);
            }
            lineRead = nntpReader.readLine();
            status = "Received: " + lineRead;

            if (debug) {
                System.out.println(status);
            }
            return new NNTPresponse(lineRead);
        } catch (NNTPException e) {
            throw e;
        } catch (Exception e) {
            throw new NNTPException("Problem receiving response from server: "
                    + e.toString());
        }

    }

    private class NNTPresponse {

        public int Code;
        public String Info;

        public NNTPresponse(String fullResponse) throws NNTPException {

            try {

                Code = Integer.parseInt(fullResponse.substring(0, 3));
            } catch (NumberFormatException e) {
                throw new NNTPException("Server response not expected: " + Info);
            }
            Info = fullResponse;
        }

        @Override
        public String toString() {
            return Info;
        }
    }

    public class NNTPException extends Exception {

        private static final long serialVersionUID = 2525901341922310074L;
        String error;
        private int code;

        NNTPException(String errorString) {
            error = errorString;
            setCode(-1);
        }

        NNTPException(NNTPresponse res) {
            error = res.Info;
            setCode(res.Code);
        }

        @Override
        public String toString() {
            return error;
        }

        public int setCode(int code) {
            this.code = code;
            return code;
        }

        public int getCode() {
            return code;
        }
    }
}
