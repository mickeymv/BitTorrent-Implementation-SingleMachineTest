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

	private static Logger logger = Logger.getLogger(TCPConnectionManager.class);

	/** peer ID */
	private static String peerID;
	private static PeerInfo peerInstance = null;
	private static Util utilInstance = Util.initializeUtil();

	private TCPConnectionManager connManager = null;
	private PeerProcess instance = null;

	/** this list contains all other peers' information in the network. */
	private ArrayList<PeerInfo> neighbors = null;

	/** the indice of the preferred neighbor set. */
	private HashMap<String, Boolean> preferred_neighbors = null;
	/** the unchoked neighbor. */
	private int unchoked_neighbor = -1;
	/** time interval used to update preferred neighbors. */
	private int time_interval_p_preferred_neighbor = 0;
	/** time intervals used to update unchoked neighbor. */
	private int time_interval_m_unchoked_neighbor = 0;
	/** number of preferred neighbors. */
	private int k_preferred_neighbors = 0;

	public PeerProcess() {

	}

	public static PeerInfo getLocalPeerInstance() {

		if (peerInstance == null) {
			peerInstance = utilInstance.getPeerInfo(peerID);
		}
		return peerInstance;
	}

	/**
	 * Self initiation for the local peer.
	 */
	private void initiatePeerProcess() {
		// read configurations files and initialize local peer.
		Util.initializeUtil();
		connManager = new TCPConnectionManager(peerInstance);
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

			if (!peer.getPeerID().equals(peerID)) {

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
		PeerProcess localPeer = new PeerProcess();
		localPeer.peerID = args[2];
		localPeer.getLocalPeerInstance();
		// start logging
		FileLogger.initialize();
		localPeer.initiatePeerProcess();
	}

	public static void initiatePeerProcessForLocalHostTesting(ArrayList<String> peerIDList) {
		for (String peerID : peerIDList) {
			PeerInfo peerInstance = Util.getPeerInfo(peerID);
			TCPConnectionManager connManager = new TCPConnectionManager(peerInstance);
			connManager.initializePeer();
		}
		try {
			Thread.sleep(20000); // give enough time before the main program
									// exits for the different threads to finish
									// execution (check to see if the various
									// ports/streams have the data or not)
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
