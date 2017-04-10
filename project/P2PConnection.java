
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * This is a wrapper class, which will contain all information about a TCP
 * connection between peers.
 * 
 * @author Xiaolong Li, Mickey Vellukunnel
 *
 */
public class P2PConnection {

	private Socket localSocket = null;
	private String remotePeerID = null;
	private String remotePeerHostName = null;
	private int remotePeerPortNumber = -1;
	private DataOutputStream dataOutputStream = null;
	private DataInputStream dataInputStream = null;

	public P2PConnection(Socket connection, String peerID, String hostname, int port) {
		this.localSocket = connection;
		this.remotePeerID = peerID;
		this.remotePeerHostName = hostname;
		this.remotePeerPortNumber = port;
		try {
			this.dataInputStream = new DataInputStream(connection.getInputStream());
			this.dataOutputStream = new DataOutputStream(connection.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DataOutputStream getDataOutputStream() {
		return dataOutputStream;
	}

	public DataInputStream getDataInputStream() {
		return dataInputStream;
	}

	// public void setConnection(Socket connection) {
	// this.localSocket = connection;
	// }

	public String getRemotePeerID() {
		return remotePeerID;
	}

	// public void setRemotePeerID(String peerID) {
	// this.remotePeerID = peerID;
	// }

	public String getRemotePeerHostname() {
		return remotePeerHostName;
	}

	// public void setHostname(String hostname) {
	// this.remotePeerHostName = hostname;
	// }

	public int getRemotePeerPort() {
		return remotePeerPortNumber;
	}

	// public void setPort(int port) {
	// this.remotePeerPortNumber = port;
	// }

	// public Socket getConnection() {
	// return localSocket;
	// }

	public void closeConnection() {
		try {
			this.dataInputStream.close();
			this.dataOutputStream.close();
			this.localSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
