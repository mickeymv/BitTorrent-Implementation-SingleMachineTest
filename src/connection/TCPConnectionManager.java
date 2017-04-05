package connection;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import listener.MessageListener;
import messages.HandShake;
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

	private String localPeerID = null;
	private String localHostname = null;
	private int localPeerServerListeningPort = -1;
	private ServerSocket listener = null;

	private static Util utilInstance = Util.initializeUtil();

	/*
	 * This is required for local testing. the map has as key the peer's
	 * address, formatted as 'hostName:portNumber'; and value as the peerID.
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
		this.localPeerID = localPeer.getPeerID();
		this.localHostname = localPeer.getHostName();
		this.localPeerServerListeningPort = localPeer.getPortNumber();
		populatePeerAddressToPeerIDHashMap();
	}

	/*
	 * This is required for local testing.
	 */
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
		if (!utilInstance.isFirstPeer(localPeerID)) {
			createClientConnections();
		}
		// if the peer is not the last, create a server socket and listen to
		// connection requests from succeeding peers.
		if (!utilInstance.isLastPeer(localPeerID)) {
			// create a server
			createServer(localPeerServerListeningPort);
		}
	}

	private void createClientConnections() {
		ArrayList<PeerInfo> previousPeers = utilInstance.getMyPreviousPeers(localPeerID);
		for (PeerInfo remotePeerServer : previousPeers) {
			// Each client connection connecting to a server is in a separate
			// thread.
			(new Thread() {
				@Override
				public void run() {
					// System.out.println("inside Client: " + localPeerID + " 's
					// thread to connect to Server: "
					// + remotePeerServer.getPeerID());
					Socket localPeerClientSocket;
					try {

						localPeerClientSocket = new Socket(remotePeerServer.getHostName(),
								remotePeerServer.getPortNumber());

						populateConnMap(localPeerClientSocket, remotePeerServer.getPeerID(),
								remotePeerServer.getHostName(), remotePeerServer.getPortNumber());

						// System.out.println("123inside after client connected
						// to server: " + localPeerID + "
						// "+remotePeerServer.getPeerID());

						peerAddressToPeerIDMap.put(localHostname + ":" + localPeerClientSocket.getLocalPort(),
								localPeerID);

						HandShake.establishClientHandShakeTwoWayStream(localPeerID, remotePeerServer.getPeerID());

						// System.out.println("Client: " + localPeerID + ",
						// connected to Server: "
						// +
						// peerAddressToPeerIDMap.get(Util.getPeerAddress(remotePeerServer)));

						// String serverPeerID =
						// hostNameToPeerIDMap.get(peer.getHostName()); //
						// hostname2peerID(peer.getHostName());
						// System.out.println("Client: " + clientPeerID + ",
						// connected
						// to Server: " + serverPeerID);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}).start();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
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

		try {
			listener = new ServerSocket(serverPort);
			// System.out.println("The server " + localPeerID + " is running.");

			// the listening server should be in a separate thread or else it
			// will block the main thread.
			(new Thread() {
				@Override
				public void run() {
					// System.out.println("Inside the server " + localPeerID + "
					// thread, listening to client requests...");
					while (true) {
						try {

							// all incoming client connection requests would be
							// handled in a separate thread

							new PeerServerHandler(listener.accept(), localPeerID).start();
						} catch (IOException e) {
							System.err.println("Error: Cannot create server socket " + "with hostname " + localHostname
									+ " port number " + localPeerServerListeningPort);
							e.printStackTrace();
						}
						// finally {
						// try {
						// listener.close();
						// } catch (IOException e) {
						// // TODO Auto-generated catch block
						// e.printStackTrace();
						// }
						// }
					}

				}
			}).start();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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
	 *            created local TCP connection (local peer's socket
	 *            server/client)
	 * @param peerID:
	 *            peerID of the (remote) client/server.
	 */
	private static void populateConnMap(Socket localSocket, String remotePeerID, String hostname, int port) {
		P2PConnection p2pConn = new P2PConnection(localSocket, remotePeerID, hostname, port);
		connMap.put(remotePeerID, p2pConn);
	}

	/**
	 * A handler thread class. Handlers are spawned from the listening loop and
	 * are responsible for dealing with a single client's requests.
	 */
	private static class PeerServerHandler extends Thread {
		private Socket localPeerSocket;
		// private DataInputStream in; // stream read from the client socket
		// private DataOutputStream out; // stream write to the client socket
		private String remoteClientPeerID; // The index number of the client
		private String localServerPeerID;

		/*
		 * the socket object coming in is the server's socket associated with a
		 * particular incoming client tcp connection.
		 */
		public PeerServerHandler(Socket connection, String localServerPeerID) {
			this.localPeerSocket = connection;
			String clientHostname = connection.getInetAddress().getHostName();
			remoteClientPeerID = peerAddressToPeerIDMap.get(clientHostname + ":" + connection.getPort());
			// System.out.println("Server: " + localServerPeerID + ", connected
			// to a client with address: "
			// + clientHostname + ":" + connection.getPort() + " and ID: " +
			// remoteClientPeerID);
			populateConnMap(localPeerSocket, remoteClientPeerID, clientHostname, connection.getPort());
			this.localServerPeerID = localServerPeerID;
			// System.out.println("123inside after server connected to client: "
			// + localServerPeerID + " "+clientPeerID);
		}

		public void run() {
			//System.out.println("Inside the server " + localServerPeerID + " thread,after accepted a client request...");
			HandShake.establishServerHandShakeTwoWayStream(localServerPeerID, remoteClientPeerID);
			
			MessageListener localPeerMessageListener = new MessageListener(localServerPeerID, remoteClientPeerID, getDataInputStream(remoteClientPeerID));
			
			localPeerMessageListener.startListening();
		}

	}

	public static DataOutputStream getDataOutputStream(String remotePeerID) {
		return connMap.get(remotePeerID).getDataOutputStream();
	}

	public static DataInputStream getDataInputStream(String remotePeerID) {
		return connMap.get(remotePeerID).getDataInputStream();
	}

}
