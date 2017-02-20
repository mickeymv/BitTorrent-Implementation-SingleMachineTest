package peer;
import util.Util;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import connection.TCPConnectionManager;
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
	
	private static Logger logger = Logger.getLogger(TCPConnectionManager.class);
	
	/** peer ID*/
	private static String peerID;
	private static PeerInfo peerInstance = null;
	private static Util utilInstance = Util.getInstance();
	
	private static TCPConnectionManager connManager = null;
	private static PeerProcess instance = null;
	
	/** this list contains all other peers' information in the network. */
	private ArrayList<PeerInfo> neighbors = null;
	
	
	public static PeerInfo getLocalPeerInstance() {

		if (peerInstance == null) {
			 peerInstance = utilInstance.getPeerInfo(peerID);
		}
		return peerInstance;
	}
	
	public static PeerProcess getPeerProcessInstance() {
		
		if (instance == null) {
			
			instance = new PeerProcess();
		}
		return instance;
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
		
		getLocalPeerInstance();
		
        // start logging
        FileLogger.initialize();
		
		initiatePeerProcess();
	}
	
	public static void initiatePeerProcessForTesting(String peerID) {
		
		peerInstance = utilInstance.getPeerInfo(peerID);
		initiatePeerProcess();
	}
	
	
}
