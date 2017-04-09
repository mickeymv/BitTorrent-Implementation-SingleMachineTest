package connection;

import java.io.ByteArrayOutputStream;
import messages.Message;
import peer.PeerProcess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
	private static Calendar calendar = Calendar.getInstance();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");

	/** This is the map for P2pConnections. Input is the peerID of a peer. */

	/**
	 * This is the map for P2pConnections.
	 * 
	 * TODO: for local testing we cannot make this static.
	 * 
	 * But for actual machine testing, this can be made static with the key as
	 * the remote peer alone. For now, key has to be "localPeerID:remotePeerID"
	 * 
	 * Key is the peerID of a peer (when actual remote machine testing).
	 */
	private static HashMap<String, P2PConnection> connMap = new HashMap<String, P2PConnection>();

	/** This is a list that contains all of the peers in the network */
	private static ArrayList<PeerInfo> peerList = new ArrayList<PeerInfo>();

	private final String localPeerID;
	private final String localHostname;
	private final int localPeerServerListeningPort;
	private ServerSocket listener = null;

	private static Util utilInstance = Util.initializeUtil();

	private HandShake handShakeHandler;

	public PeerProcess localPeerProcessInstance;
	
	/**
	 * TODO: Remove this after local testing.
	 * This is required for local testing. the map has as key the peer's
	 * address, formatted as 'hostName:portNumber'; and value as the peerID.
	 * 
	 * The only thing required for remote machine testing is getPeerIDFromHostName();
	 **/
	private static HashMap<String, String> peerAddressToPeerIDMap = new HashMap<>();

	/**
	 * Constructor, initiate the object.
	 * 
	 * @param peerProcess
	 * 
	 * @param hostname:
	 *            host name of self
	 * @param port:
	 *            port number of self
	 */
	public TCPConnectionManager(PeerInfo localPeer, PeerProcess peerProcess) {
		this.localPeerID = localPeer.getPeerID();
		this.localHostname = localPeer.getHostName();
		this.localPeerServerListeningPort = localPeer.getPortNumber();
		this.localPeerProcessInstance = peerProcess;
		populatePeerAddressToPeerIDHashMap();
	}

	private HandShake getHandShakeHandler() {
		if (handShakeHandler == null) {
			handShakeHandler = new HandShake(this);
		}
		return handShakeHandler;
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
			// System.out.println("Create client connections for peer#" +
			// localPeerID);
			createClientConnections();
		}
		// if the peer is not the last, create a server socket and listen to
		// connection requests from succeeding peers.
		if (!utilInstance.isLastPeer(localPeerID)) {
			// create a server
			createServer(localPeerServerListeningPort);
		}

		// Sent and recieve Bitfield messages. For now doing locally as
		// all are on one machine.

		{// TODO: Remove this. Only for initial testing of piece
			// transfer and protocol stability without preferred
			// neighbor mechanism.
			(new Thread() {
				@Override
				public void run() {
					// System.err.println("In INITIAL choking for local
					// peer#"+localPeerID);
					// initialize preferred neighbors.
					for (PeerInfo peer : peerList) {
						if (!localPeerID.equals(peer.getPeerID())) {
							
							if (localPeerProcessInstance.checkIfInterested(peer.getPeerID()) == true) {
								
								new Message(localPeerID, peer.getPeerID(), localPeerProcessInstance)
										.sendMessage(Message.MESSAGETYPE_INTERESTED);
							} else {
								
								new Message(localPeerID, peer.getPeerID(), localPeerProcessInstance)
										.sendMessage(Message.MESSAGETYPE_NOTINTERESTED);
							}
							
							// System.out.println("In peer#" + localPeerID + ", and sent a unchoke message to peer#"
							//		+ peer.getPeerID());
							//new Message(localPeerID, peer.getPeerID(), localPeerProcessInstance)
							//	.sendMessage_bitfield(localPeerProcessInstance.getLocalPeerBitField());
						}
					}
					
					localPeerProcessInstance.start_p_timer();
					localPeerProcessInstance.start_m_timer();
					
					//localPeerProcessInstance.initializePreferredNeighbors();
					//try {
					//	localPeerProcessInstance.updateUnchokedNeighbor();
					//} catch (Exception e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
					//}
				}
			}).start();
		}
	}

	/**
	 * Creates connections from local peer to previous peers.
	 */
	private void createClientConnections() {
		ArrayList<PeerInfo> previousPeers = Util.getMyPreviousPeers(localPeerID);
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

						logger.info(dateFormat.format(calendar.getTime()) + ": Peer " + remotePeerServer.getPeerID()
								+ " makes a connection to Peer " + localPeerID + ".");

						populateConnMap(localPeerID, localPeerClientSocket, remotePeerServer.getPeerID(),
								remotePeerServer.getHostName(), remotePeerServer.getPortNumber());

						peerAddressToPeerIDMap.put(localHostname + ":" + localPeerClientSocket.getLocalPort(),
								localPeerID);

						// System.out.println("Mapping for "+localHostname + ":"
						// + localPeerClientSocket.getLocalPort()+" to
						// "+localPeerID+ " has been entered");

						// System.out.println("123inside after client connected
						// to server: " + localPeerID + " "
						// + remotePeerServer.getPeerID());

						// TODO: uncomment this for handshakes!
						// getHandShakeHandler().establishClientHandShakeTwoWayStream(localPeerID,
						// remotePeerServer.getPeerID());

						// System.out.println("ClientHandler: Trying to get
						// DatainputStream for "+localPeerID+":"+
						// remotePeerServer.getPeerID());

						MessageListener localPeerMessageListener = new MessageListener(localPeerID,
								remotePeerServer.getPeerID(),
								TCPConnectionManager.getDataInputStream(localPeerID, remotePeerServer.getPeerID()));
						localPeerMessageListener.startListening();

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
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
	 * TODO: This is the only function needed when we test on actual remote
	 * machines. There is no need for a peerAddress to peerID map then.
	 * 
	 * @param hostname
	 * @return
	 */

	private static String getPeerIDFromHostName(String hostname) {
		String peerID = null;
		for (PeerInfo peer : peerList) {
			if (hostname.equals(peer.getHostName())) {
				return peer.getPeerID();
			}
		}
		return peerID;
	}

	/**
	 * Populate the map for TCP connections.
	 * 
	 * @param connection:
	 *            created local TCP connection (local peer's socket
	 *            server/client)
	 * @param peerID:
	 *            peerID of the (remote) client/server.
	 */
	private synchronized static void populateConnMap(String localPeerID, Socket localSocket, String remotePeerID,
			String hostname, int port) {
		P2PConnection p2pConn = new P2PConnection(localSocket, remotePeerID, hostname, port);
		connMap.put(localPeerID + ":" + remotePeerID, p2pConn);

		// System.out.println("Connection added for
		// "+localPeerID+":"+remotePeerID);

		// System.out.println("connMap has a socket for the remotePeer#" +
		// remotePeerID);
	}

	/**
	 * A handler thread class. Handlers are spawned from the listening loop and
	 * are responsible for dealing with a single client's requests.
	 */
	private class PeerServerHandler extends Thread {
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
			while (!peerAddressToPeerIDMap.containsKey(clientHostname + ":" + connection.getPort())) {
				System.err.println("\nWait for the local server peer# " + localServerPeerID
						+ ", peerAddressToPeerIDMap to have a mapping for the host# " + clientHostname + ":"
						+ connection.getPort() + "\n");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			remoteClientPeerID = peerAddressToPeerIDMap.get(clientHostname + ":" + connection.getPort());

			// log the connection: [Time]: Peer [peer_ID 1] is connected from
			// Peer [peer_ID 2].
			logger.info(dateFormat.format(calendar.getTime()) + ": Peer " + localServerPeerID
					+ " is connected from Peer " + remoteClientPeerID + ".");

			// System.out.println("Server: " + localServerPeerID + ", connected
			// to a client with address: "
			// + clientHostname + ":" + connection.getPort() + " and ID: " +
			// remoteClientPeerID);
			populateConnMap(localServerPeerID, localPeerSocket, remoteClientPeerID, clientHostname,
					connection.getPort());
			this.localServerPeerID = localServerPeerID;
			// System.out.println("123inside after server connected to client: "
			// + localServerPeerID + " "+clientPeerID);
		}

		public void run() {
			// System.out.println("Inside the server " + localServerPeerID + "
			// thread,after accepted a client request...");

			// TODO: uncomment this for handshakes!
			// getHandShakeHandler().establishServerHandShakeTwoWayStream(localServerPeerID,
			// remoteClientPeerID);

			// System.out.println("ServerHandler: Trying to get DatainputStream
			// for "+localPeerID+":"+ remoteClientPeerID);

			MessageListener localPeerMessageListener = new MessageListener(localServerPeerID, remoteClientPeerID,
					TCPConnectionManager.getDataInputStream(localPeerID, remoteClientPeerID));
			localPeerMessageListener.startListening();
		}

	}

	public static DataOutputStream getDataOutputStream(String localPeerID, String remotePeerID) {

		while (!connMap.containsKey(localPeerID + ":" + remotePeerID)) {
			// wait for the connection socket to be created from the thread.
			// System.err.println("OUT");
//			System.err.println("\nWait for the local peer# " + localPeerID
//					+ ", connMap to have a socket for the remotePeer#" + remotePeerID + "\n");

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// System.out.println("out of Waiting for the connMap to have a socket
		// for the remotePeer#" + remotePeerID);

		return connMap.get(localPeerID + ":" + remotePeerID).getDataOutputStream();
	}

	public static DataInputStream getDataInputStream(String localPeerID, String remotePeerID) {

		while (!connMap.containsKey(localPeerID + ":" + remotePeerID)) {
			// wait for the connection socket to be created from the thread.
			// System.err.println("IN");
//			System.err.println("\nWait for the local peer# " + localPeerID
//					+ ", connMap to have a socket for the remotePeer# " + remotePeerID + "\n");

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return connMap.get(localPeerID + ":" + remotePeerID).getDataInputStream();
	}
	
	/**
	 * Broadcast to all peers of the local peer that this peer
	 * "has" the specified piece.
	 * @param pieceIndex
	 */
	public  void broadcastHavePieceIndexMessageToAllPeers(int pieceIndex) {
		for(PeerInfo peer: this.localPeerProcessInstance.getNeighbors()) {
			sendHaveMessage(pieceIndex, peer.getPeerID());
		}
	}
	


	/**
	 * Send "NOT_INTERESTED" message to the peers who the local peer
	 * is not interested in.
	 * @param notInterestingPeers
	 */
	public  void broadcastNotInterestedToUnInterestingPeers(ArrayList<String> notInterestingPeers) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * 
	 * @param messageType, the type of message to sent
	 * @param messagePayload, the required payload
	 */
	public  void sendHaveMessage(int pieceIndex, String remotePeerID) {
		while (!connMap.containsKey(localPeerID + ":" + remotePeerID)) {
			// wait for the connection socket to be created from the thread.
			// System.err.println("OUT");
//			System.err.println("\nWait for the local peer# " + localPeerID
//					+ ", connMap to have a socket for the remotePeer#" + remotePeerID + "\n");

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		DataOutputStream out = connMap.get(localPeerID + ":" + remotePeerID).getDataOutputStream();
		ByteArrayOutputStream streamToCombineByteArrays = new ByteArrayOutputStream();
		byte[] pieceIndexMessagePayload = Util.intToByteArray(pieceIndex);
		try {
			streamToCombineByteArrays.write((byte)Message.MESSAGETYPE_HAVE);
			streamToCombineByteArrays.write(pieceIndexMessagePayload);
			byte[] message = streamToCombineByteArrays.toByteArray();
			out.writeInt(message.length);
			out.write(message);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
