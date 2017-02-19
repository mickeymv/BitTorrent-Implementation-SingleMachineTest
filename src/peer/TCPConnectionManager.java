package peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import type.PeerInfo;
import type.Server.Handler;

/**
 * This TCPConnectionManager will manage TCP connections between peers. 
 * It will create a Server socket and multiple client sockets.
 * All of the connections will be stored in a Map which takes peerID 
 * as key.
 * 
 * @author adminuser
 *
 */
public class TCPConnectionManager {

	/** This is the map for P2pConnections. Input is the peerID of a peer.*/
	private static HashMap<String, P2pConnection> connMap = new HashMap<String, P2pConnection>();

	private String ID = null;
	private String hostname = null;
	private int port = -1;
	private ArrayList<PeerInfo> previousPeers = null;
	private boolean isLastPeer = false;
	private ServerSocket listener = null;
	/**
	 * Constructor, initiate the object.
	 * @param hostname: host name of self
	 * @param port: port number of self
	 * @param perviousPeers: list of previous peers.
	 * @param isLastPeer: is self the last peer in the list. If true, this peer will not
	 * 						create a server object. 
	 */
	public TCPConnectionManager(String ID, String hostname, int port, 
			ArrayList<PeerInfo> perviousPeers, boolean isLastPeer) {
		this.ID = ID;
		this.hostname = hostname;
		this.port = port;
		this.previousPeers = previousPeers;
		this.isLastPeer = isLastPeer;
	}
	/**
	 * This method will initiate peer by creating server and client.
	 */
	public void initiatePeer() {

		if (! isLastPeer) {
			// create a server
			listener = createServer(port);
		}


	}
	/**
	 * Create a connection to a peer.
	 * @param peerID
	 * @return
	 */
	public Socket createConnection(String peerID) {

		return null;
	}

	/**
	 * Create a server
	 * @param serverPort
	 * @return
	 */
	public ServerSocket createServer(int serverPort) {

		ServerSocket listener = new ServerSocket(serverPort);
		System.out.println("The server is running."); 

		try {
			while(true) {
				new Handler(listener.accept()).start();
				System.out.println("Client "  + ID + " is connected!");
			}
		} finally {
			listener.close();
		} 
	}

	/**
	 * Return peerID given a hostname.
	 * @param hostname
	 * @return
	 */
	private static String hostname2peerID(String hostname) {
		//TODO: 
		
		return null;
	}
	
	/**
	 * Populate the map for TCP connections.
	 * 
	 * @param connection: created TCP connection
	 * @param peerID: peerID of the client.
	 */
	private static void populateConnMap(Socket connection, String peerID,
			String hostname, int port) {
		
		P2pConnection p2pConn = 
				new P2pConnection(connection, peerID, hostname, port);
		connMap.put(peerID, p2pConn);
	}

	/**
	 * A handler thread class.  Handlers are spawned from the listening
	 * loop and are responsible for dealing with a single client's requests.
	 */
	private static class Handler extends Thread {

		private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
		private ObjectInputStream in;	//stream read from the socket
		private ObjectOutputStream out;    //stream write to the socket
		private String peerID;		//The index number of the client

		public Handler(Socket connection) {
			this.connection = connection;
			String clientHostname = connection.getInetAddress().getHostName();
			String clientPeerID = hostname2peerID(clientHostname);
			populateConnMap(connection, clientPeerID, clientHostname, connection.getPort());
			
		}

		public void run() {
			
			try{
				//initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				try{
					while(true) {
						//receive the message sent from the client
						message = (String)in.readObject();
						//show the message to the user
						System.out.println("Receive message: " + message + " from client " + peerID);
						//Capitalize all letters in the message
						MESSAGE = message.toUpperCase();
						//send MESSAGE back to the client
						sendMessage(MESSAGE);
					}
				}
				catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
			} catch(IOException ioException){
				System.out.println("Disconnect with Client " + peerID);
			}

			finally{
				//Close connections
				try{
					in.close();
					out.close();
					connection.close();
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Client " + peerID);
				}
			}
		}

		//send a message to the output stream
		public void sendMessage(String msg)
		{
			try{
				out.writeObject(msg);
				out.flush();
				System.out.println("Send message: " + msg + " to Client " + peerID);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}

	}




}
