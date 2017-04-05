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
	private static Util utilInstance = Util.initializeUtil();
	
	private ArrayList<Byte> localPeerBitField = null;

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

	public PeerProcess(String localPeerID) {
		this.localPeerID = localPeerID;
		localPeerInfo = utilInstance.getPeerInfo(localPeerID);
		localPeerBitField = utilInstance.initializeLocalPeerBitfield(localPeerInfo.isHasFileInitially());
		//System.out.println("the bitfield for peer: "+localPeerID+" is:");
		//utilInstance.printBitfield(localPeerBitField);
	}

	/**
	 * Self initiation for the local peer.
	 */
	//TODO: Change this to private after local testing is done
	public void initiatePeerProcess() {
		// read configurations files and initialize local peer.
		connManager = new TCPConnectionManager(localPeerInfo);
		connManager.initializePeer();
		time_interval_p_preferred_neighbor = ConfigurationSetup.getInstance().getUnchokingInterval();
		time_interval_m_unchoked_neighbor = ConfigurationSetup.getInstance().getOptimisticUnchokingInterval();
		k_preferred_neighbors = ConfigurationSetup.getInstance().getNumberOfPreferredNeighbors();
		findNeighbors();
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

	}

	/**
	 * Determines unchoked neighbor every m seconds.
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
