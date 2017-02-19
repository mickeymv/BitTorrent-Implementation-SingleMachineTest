package peer;
import util.Util;

import java.util.ArrayList;

import type.PeerInfo;
import logging.FileLogger;

/**
 * This class sets up a peer in a peer-to-peer network
 * 1. Initiate peer and set peer ID.
 * 2. Make TCP connections to other peers.

 * @author Xiaolong Li, Mickey Vellukunnel
 *
 */
public class PeerProcess {
	
	/** peer ID*/
	private static String peerID;
	private static PeerInfo peerInstance = null;
	private static Util utilInstance = Util.getInstance();
	
	private static TCPConnectionManager connManager = null;
	
	
	/** this list contains all other peers' information in the network. */
	private ArrayList<PeerInfo> neighbors = null;
	
	public static PeerInfo getLocalPeerInstance() {

		if (peerInstance == null) {
			 peerInstance = utilInstance.getPeerInfo(peerID);
		}
		return peerInstance;
	}
	
	/**
	 * Self initiation for the local peer.
	 */
	private static void initiatePeerProcess() {
		connManager = new TCPConnectionManager(peerInstance);
		connManager.initializePeer();
	}
	

	
	/**
	 * Peer starts running from here
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("\n\nError: Incorrect number of arguments. Syntax is, \"java PeerProcess [peerID]\"");
			return;
		}
		
		peerID = args[2];
		
        // start logging
        FileLogger.initialize();
		
		initiatePeerProcess();
	}
	
}
