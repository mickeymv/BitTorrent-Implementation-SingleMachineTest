package peer;

import util.ConfigurationSetup;
import util.Util;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import connection.TCPConnectionManager;
import type.PeerInfo;
import logging.FileLogger;

/**
 * This class sets up a peer in a peer-to-peer network 1. Initiate peer and set
 * peer ID. 2. Make TCP connections to other peers.
 * 
 * @author Xiaolong Li, Mickey Vellukunnel
 *
 */
public class PeerProcess {

	private Logger logger = Logger.getLogger(TCPConnectionManager.class);

	/** peer ID */
	private String localPeerID;
	private PeerInfo localPeerInfo = null;
	private  Util utilInstance = Util.initializeUtil();

	private ArrayList<Byte> localPeerBitField = null;

	/* Map of peers' bitfields */
	private HashMap<String, ArrayList<Byte>> peersBitfields = new HashMap<>();

	private TCPConnectionManager connManager = null;

	/** this list contains all other peers' information in the network. */
	private ArrayList<PeerInfo> neighbors = new ArrayList<>();

	/** the indice of the preferred neighbor set. */
	private HashMap<String, Boolean> preferred_neighbors = new HashMap<>();
	/** the unchoked neighbor. */
	private int unchoked_neighbor = -1;
	/** time interval used to update preferred neighbors. */
	private int time_interval_p_preferred_neighbor = 0;
	/** time intervals used to update unchoked neighbor. */
	private int time_interval_m_unchoked_neighbor = 0;
	/** number of preferred neighbors. */
	private int k_preferred_neighbors = 0;
	
	private boolean gotCompletedFile = false;

	public PeerProcess(String localPeerID) {
		this.localPeerID = localPeerID;
		localPeerInfo = utilInstance.getPeerInfo(localPeerID);
		localPeerBitField = utilInstance.getPeerBitfield(localPeerInfo.isHasFileInitially());
		// System.out.println("the bitfield for peer: "+localPeerID+" is:");
		// utilInstance.printBitfield(localPeerBitField);
		time_interval_p_preferred_neighbor = ConfigurationSetup.getInstance().getUnchokingInterval();
		time_interval_m_unchoked_neighbor = ConfigurationSetup.getInstance().getOptimisticUnchokingInterval();
		k_preferred_neighbors = ConfigurationSetup.getInstance().getNumberOfPreferredNeighbors();
		findNeighbors();
		setPeersBitfields();
		this.gotCompletedFile = localPeerInfo.isHasFileInitially();
	}

	private void setPeersBitfields() {
		for (PeerInfo peer : neighbors) {
			setPeerBitField(peer.getPeerID(), utilInstance.getPeerBitfield(peer.isHasFileInitially()));
		}
	}

	public void setPeerBitField(String remotePeerID, ArrayList<Byte> remotePeerBitfield) {
		peersBitfields.put(remotePeerID, remotePeerBitfield);
		System.out.println("the bitfield for peer: "+remotePeerID+" is:");
		utilInstance.printBitfield(remotePeerBitfield);
	}

	public ArrayList<Byte> getPeerBitField(String remotePeerID) {
		return peersBitfields.get(remotePeerID);
	}

	/**
	 * Self initiation for the local peer.
	 */
	// TODO: Change this to private after local testing is done and before
	// remote machine testing.
	public void initiatePeerProcess() {
		// read configurations files and initialize local peer (setup
		// connections to all other peers).
		connManager = new TCPConnectionManager(localPeerInfo);
		connManager.initializePeer();
	}

	/**
	 * Initialize the neighbors list.
	 */
	private void findNeighbors() {

		for (PeerInfo peer : Util.getPeerList()) {

			if (!peer.getPeerID().equals(getPeerID())) {

				neighbors.add(peer);
				preferred_neighbors.put(peer.getPeerID(), false);
			}
		}

	}

	/**
	 * Choke a peer
	 * 
	 * @param peerID
	 */
	public void choke(int peerID) {

	}

	/**
	 * unchoke a peer
	 * 
	 * @param peerID
	 */
	public void unchoke(int peerID) {

	}

	/**
	 * Determine preferred neighbors every p seconds.
	 */
	public void determinePreferredNeighbors() {
		if(this.gotCompletedFile) { //determine preferred neighbors randomly
			
		} else { //use downloading rates.
			
		}
	}

	/**
	 * Determines unchoked neighbor every m seconds.
	 * optimistically unchoked neighbor randomly among neighbors that are choked at that moment but are interested in its data.
	 */
	public void determineUnchokedNeighbor() {
		
	}

	/**
	 * Peer starts running from here
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("\n\nError: Incorrect number of arguments. Syntax is, \"java PeerProcess [peerID]\"");
			return;
		}
		PeerProcess localPeer = new PeerProcess(args[2]);
		// start logging
		FileLogger.initialize(args[2]);
		localPeer.initiatePeerProcess();
	}

	public String getPeerID() {
		return localPeerID;
	}

	public void setPeerID(String peerID) {
		localPeerID = peerID;
	}

}
