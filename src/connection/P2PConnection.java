package connection;

import java.net.Socket;

/**
 * This is a wrapper class, which will contain all information about 
 * a TCP connection between peers.
 * @author Xiaolong Li
 *
 */
public class P2PConnection {

	
	private Socket connection = null;
	private String peerID = null;
	private String hostname = null;
	private int port = -1;
	
	public P2PConnection(Socket connection, String peerID, String hostname, int port) {
		
		this.connection = connection;
		this.peerID = peerID;
		this.hostname = hostname;
		this.port = port;
	}
	public void setConnection(Socket connection) {
		
		this.connection = connection;
	}
	public String getPeerID() {
		return peerID;
	}
	public void setPeerID(String peerID) {
		this.peerID = peerID;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public Socket getConnection() {
		return connection;
	}
	
}
