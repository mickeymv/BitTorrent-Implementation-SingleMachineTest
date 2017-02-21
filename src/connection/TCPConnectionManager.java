package connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import type.PeerInfo;
import type.Server.Handler;
import util.Util;

/**
 * This TCPConnectionManager will manage TCP connections between peers. It will
 * create a Server socket and multiple client sockets. All of the connections
 * will be stored in a Map which takes peerID as key.
 * 
 * @author Xiaolong Li, Mickey Vellukunnel
 *
 */
public class TCPConnectionManager {

	private static Logger logger = Logger.getLogger(TCPConnectionManager.class);

	/** This is the map for P2pConnections. Input is the peerID of a peer. */
	private static HashMap<String, P2PConnection> connMap = new HashMap<String, P2PConnection>();

	/** This is a list that contains all of the peers in the network */
	private static ArrayList<PeerInfo> peerList = new ArrayList<PeerInfo>();

	private String ID = null;
	private String hostname = null;
	private int port = -1;
	private ServerSocket listener = null;

	private static Util utilInstance = Util.getInstance();

	/*
	 * the map has as key the peer's address, formatted as
	 * 'hostName:portNumber'; and value as the peerID.
	 **/
	private static HashMap<String, String> peerAddressToPeerIDMap = new HashMap<>();

	/**
	 * Constructor, initiate the object.
	 * 
	 * @param hostname:
	 *            host name of self
	 * @param port:
	 *            port number of self
	 */
	public TCPConnectionManager(PeerInfo localPeer) {
		this.ID = localPeer.getPeerID();
		this.hostname = localPeer.getHostName();
		this.port = localPeer.getPortNumber();
		populatePeerAddressToPeerIDHashMap();
	}

	private void populatePeerAddressToPeerIDHashMap() {
		peerList = utilInstance.getPeerList();
		for (PeerInfo peer : peerList) {
			peerAddressToPeerIDMap.put(Util.getPeerAddress(peer), peer.getPeerID());
		}
	}

	/**
	 * This method will initialize the peer by creating server and client
	 * connections as required.
	 */
	public void initializePeer() {
		// if the peer is not the first, create client connections to previous
		// peers.
		if (!utilInstance.isFirstPeer(ID)) {
			createClientConnections();
		}
		// if the peer is not the last, create a server socket and listen to
		// connection requests from succeeding peers.
		if (!utilInstance.isLastPeer(ID)) {
			// create a server
			createServer(port);
		}
	}

	private void createClientConnections() {
		ArrayList<PeerInfo> previousPeers = utilInstance.getMyPreviousPeers(ID);
		for (PeerInfo peer : previousPeers) {
			Socket clientSocket;
			try {
				clientSocket = new Socket(peer.getHostName(), peer.getPortNumber());
				System.out.println("Client: " + ID + ", connected to Server: "
						+ peerAddressToPeerIDMap.get(Util.getPeerAddress(peer)));
				// String serverPeerID =
				// hostNameToPeerIDMap.get(peer.getHostName()); //
				// hostname2peerID(peer.getHostName());
				// System.out.println("Client: " + clientPeerID + ", connected
				// to Server: " + serverPeerID);
				populateConnMap(clientSocket, peer.getPeerID(), peer.getHostName(), peer.getPortNumber());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create a server
	 * 
	 * @param serverPort
	 * @return
	 */
	private void createServer(int serverPort) {

		(new Thread() {
			@Override
			public void run() {

				try {
					listener = new ServerSocket(serverPort);

					System.out.println("The server " + ID + " is running.");

					try {
						while (true) {
							new Handler(listener.accept(), ID).start();
						}
					} finally {
						listener.close();
					}
				} catch (IOException e) {
					System.err.println("Error: Cannot create server socket " + "with hostname " + hostname
							+ " port number " + port);
					e.printStackTrace();
				}
			}
		}).start();

	}

	/**
	 * Return peerID given a hostname.
	 * 
	 * @param hostname
	 * @return
	 */
	/*
	 * private static String hostname2peerID(String hostname) { String peerID =
	 * null; for (PeerInfo peer : peerList) { if
	 * (hostname.equals(peer.getHostName())) { peerID = peer.getPeerID(); break;
	 * } } return peerID; }
	 */

	/**
	 * Populate the map for TCP connections.
	 * 
	 * @param connection:
	 *            created TCP connection
	 * @param peerID:
	 *            peerID of the client.
	 */
	private static void populateConnMap(Socket connection, String peerID, String hostname, int port) {
		P2PConnection p2pConn = new P2PConnection(connection, peerID, hostname, port);
		connMap.put(peerID, p2pConn);
	}

	/**
	 * A handler thread class. Handlers are spawned from the listening loop and
	 * are responsible for dealing with a single client's requests.
	 */
	private static class Handler extends Thread {

		private String message; // message received from the client
		private String MESSAGE; // uppercase message send to the client
		private Socket connection;
		private ObjectInputStream in; // stream read from the socket
		private ObjectOutputStream out; // stream write to the socket
		private String peerID; // The index number of the client

		/*
		 * the socket object coming in is the server's socket associated with a
		 * particular incoming client tcp connection.
		 */
		public Handler(Socket connection, String serverPeerID) {
			this.connection = connection;
			String clientHostname = connection.getInetAddress().getHostName();
			String clientPeerID = peerAddressToPeerIDMap.get(clientHostname + ":" + connection.getPort());
			System.out.println("Server: " + serverPeerID + ", connected to a client: " + clientPeerID);
			populateConnMap(connection, clientPeerID, clientHostname, connection.getPort());
		}

		public void run() {

			try {
				// initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				try {
					while (true) {
						// receive the message sent from the client
						message = (String) in.readObject();
						// show the message to the user
						System.out.println("Receive message: " + message + " from client " + peerID);
						// Capitalize all letters in the message
						MESSAGE = message.toUpperCase();
						// send MESSAGE back to the client
						sendMessage(MESSAGE);
					}
				} catch (ClassNotFoundException classnot) {
					System.err.println("Data received in unknown format");
				}
			} catch (IOException ioException) {
				System.out.println("Disconnect with Client " + peerID);
			}

			finally {
				// Close connections
				try {
					in.close();
					out.close();
					connection.close();
				} catch (IOException ioException) {
					System.out.println("Disconnect with Client " + peerID);
				}
			}
		}

		// send a message to the output stream
		public void sendMessage(String msg) {
			try {
				out.writeObject(msg);
				out.flush();
				System.out.println("Send message: " + msg + " to Client " + peerID);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

	}

}
