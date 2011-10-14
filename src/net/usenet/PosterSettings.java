package net.usenet;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class PosterSettings{
	
	public Boolean requiresLogin = true;
    public Boolean requiresSSL = false;
    public String userName = "USERNAME";
    public String passWord = "PASSWORD";
    public String hostName = "news.giganews.com";
    public int port = 119;
    public int connectTimeoutMs  = 2000;
    

	public final int MAX_PART_SIZE = 500000;
	public final int LINE_LENGTH = 256;
	
    public int numOfServerConnections = 20;
    public String commentOne = "Super";
    public String commentTwo = "Duper";
    public String newsgroup = "alt.binaries.test";
    public String poster = "SuperPoster@UsenetPoster.com";
    
	
	public Boolean getRequiresLogin() {
		return requiresLogin;
	}

	public void setRequiresLogin(Boolean requiresLogin) {
		this.requiresLogin = requiresLogin;
	}

	public Boolean getRequiresSSL() {
		return requiresSSL;
	}

	public void setRequiresSSL(Boolean requiresSSL) {
		this.requiresSSL = requiresSSL;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	public void setConnectTimeoutMs(int connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
	}

	public int getNumOfServerConnections() {
		return numOfServerConnections;
	}

	public void setNumOfServerConnections(int numOfServerConnections) {
		this.numOfServerConnections = numOfServerConnections;
	}

	public String getCommentOne() {
		return commentOne;
	}

	public void setCommentOne(String commentOne) {
		this.commentOne = commentOne;
	}

	public String getCommentTwo() {
		return commentTwo;
	}

	public void setCommentTwo(String commentTwo) {
		this.commentTwo = commentTwo;
	}

	public String getNewsgroup() {
		return newsgroup;
	}

	public void setNewsgroup(String newsgroup) {
		this.newsgroup = newsgroup;
	}

	public String getPoster() {
		return poster;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public int getMAX_PART_SIZE() {
		return MAX_PART_SIZE;
	}

	public int getLINE_LENGTH() {
		return LINE_LENGTH;
	}


    public static PosterSettings loadSettings() throws Exception
    {
    	FileInputStream fis = new FileInputStream("settings.xml");
    	XMLDecoder ois = new XMLDecoder(fis);
    	PosterSettings settings = (PosterSettings) ois.readObject();
    	ois.close();
    	return settings;
    }
    
    public static PosterSettings loadSettings(String file) throws Exception
    {
    	FileInputStream fis = new FileInputStream("settings.xml");
    	XMLDecoder ois = new XMLDecoder(fis);
    	PosterSettings settings = (PosterSettings) ois.readObject();
    	ois.close();
    	return settings;
    }
    
    public static void saveSettings(PosterSettings settings) throws Exception
    {
    	FileOutputStream f_out = new FileOutputStream("settings.xml");
    	XMLEncoder obj_out = new XMLEncoder(f_out);
    	obj_out.writeObject(settings);
    	obj_out.flush();
    	obj_out.close();
    }
    
    public static void saveSettings(PosterSettings settings, String file) throws Exception
    {
    	FileOutputStream f_out = new FileOutputStream(file);
    	XMLEncoder obj_out = new XMLEncoder(f_out);
    	obj_out.writeObject (settings);
    	obj_out.flush();
    	obj_out.close();
    }
}