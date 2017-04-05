package peer;

import util.ConfigurationSetup;
import util.Util;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Logger;
import java.util.Collections;

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

	/** the index of the preferred neighbor set. */
	private HashMap<String, Boolean> preferred_neighbors = new HashMap<>();
	
	/** the unchoked neighbor. */
	private int unchoked_neighbor = -1;
	
	/** time interval used to update preferred neighbors. */
	private int time_interval_p_preferred_neighbor = 0;
	
	/** time intervals used to update unchoked neighbor. */
	private int time_interval_m_unchoked_neighbor = 0;
	
	/** number of preferred neighbors. */
	private int k_preferred_neighbors = 0;
	
	/*Map of which piece index was sent as a "have" message to which remote peer.
	 * remotePeerID -> PieceIndex*/
	private HashMap<String, Integer> sentHaveMap = new HashMap<>();
	
	/*map of neighbors who are interested in local pieces. Only choose preferred and Unchoked neighbor from this list!*/
	private ArrayList<String> interestedNeighbors = new ArrayList<>();
	
	/*piecesRemainingToBeRequested + piecesRequested are the pieces the local peer DOES not yet have.*/
	
	private HashMap<Integer,Integer> piecesRemainingToBeRequested = new HashMap<>();
	
	private HashMap<Integer,Integer> piecesRequested = new HashMap<>();
	
	private boolean gotCompletedFile = false;

	public boolean getGotCompletedFile() {
		return gotCompletedFile;
	}

	public void setGotCompletedFile(boolean gotCompletedFile) {
		this.gotCompletedFile = gotCompletedFile;
	}

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
		initializePiecesRemainingMap();
	}
	
	/**
	 * 
	 * @return piece index of the piece which this local peer said it had (via a "have" message sent previously).
	 */
	public int getPieceIndexToSendToPeer(String remotePeerID) {
		return this.sentHaveMap.get(remotePeerID);
	}
	
	public void addInterestedNeighbor(String remotePeerID) {
		this.interestedNeighbors.add(remotePeerID);
	}
	
	private void initializePiecesRemainingMap() {
		if(!gotCompletedFile) {
			for(int i=0;i<ConfigurationSetup.getInstance().getNumberOfPieces();i++) {
				this.piecesRemainingToBeRequested.put(i, i);
			}
		}
	}

	private void setPeersBitfields() {
		for (PeerInfo peer : neighbors) {
			setPeerBitField(peer.getPeerID(), utilInstance.getPeerBitfield(peer.isHasFileInitially()));
		}
	}

	public void setPeerBitField(String remotePeerID, ArrayList<Byte> remotePeerBitfield) {
		peersBitfields.put(remotePeerID, remotePeerBitfield);
		//System.out.println("the bitfield for peer: "+remotePeerID+" is:");
		//utilInstance.printBitfield(remotePeerBitfield);
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
			if (! peer.getPeerID().equals(getPeerID())) {
				// add as neighbor
				neighbors.add(peer);
				// none of the neighbors is preferred neighbor at the beginning.
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
	 * Initially, choose k preferred neighbors randomly.
	 */
	public void initializePreferredNeighbors() {
		
		int k = k_preferred_neighbors;
		
		ArrayList<String> peerIDs = new ArrayList<String>();
		
		for (PeerInfo peer : neighbors) {
			
			peerIDs.add(peer.getPeerID());
		}
		
		Collections.shuffle(peerIDs);
		
		for (int i = 0; i < k && i < peerIDs.size(); i ++) {
			
			preferred_neighbors.put(peerIDs.get(i), true);
		}
	}
	
	
	/**
	 * Determine preferred neighbors every p seconds.
	 * 
	 * Then every p seconds, peer A reselects its
	 * preferred neighbors. To make the decision,
	 * peer A calculates the downloading rate from 
	 * each of its neighbors, respectively, during 
	 * the previous unchoking interval. Among neighbors 
	 * that are interested in its data, peer A picks 
	 * k neighbors that has fed its data at the 
	 * highest rate.
	 */
	public void determinePreferredNeighbors() {
		//Only choose preferred and Unchoked neighbor from this interestedNeighbors list!
		
		if(this.gotCompletedFile) { //determine preferred neighbors randomly
			
		} else { //use downloading rates.
			
		}
	}

	/**
	 * Determines unchoked neighbor every m seconds.
	 * optimistically unchoked neighbor randomly among neighbors that are choked at that moment but are interested in its data.
	 */
	public void determineUnchokedNeighbor() {
		//Only choose preferred and Unchoked neighbor from this interestedNeighbors list!
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

	/**
	 * Should update the remaining pieces and requested pieces map.
	 * 
	 * @param remotePeerID
	 * @return the piece index of the piece required from the remote peer, else -1 if there is no piece required from that particular remote peer.
	 */
	public int getPieceToBeRequested(String remotePeerID) {
		//TODO: IMPLEMENT!
		return -1; //if the remote peer does not have any piece which this local peer requires.
	}

	/**
	 * 
	 * @param remotePeerID
	 * @return boolean true/false as to whether this local peer is interested in any of the remote peer's pieces.
	 */
	public boolean checkIfInterested(String remotePeerID) {
		if(this.gotCompletedFile || getPieceToBeRequested(remotePeerID) != -1) {
			return false;
		} else {
			return true;
		}
	}

	public void removeNeighborWhoIsNotInterested(String remotePeerID) {
		this.interestedNeighbors.remove(remotePeerID);
	}
	
	/**
	 * 
	 * @param pieceIndex
	 * @return boolean true if whether this piece is needed, false otherwise.
	 */
	public boolean isPieceNotAvailableOrNotRequested(int pieceIndex) {
		if(this.piecesRemainingToBeRequested.containsKey(pieceIndex) && !this.piecesRequested.containsKey(pieceIndex)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param remotePeerID
	 * @param pieceIndex
	 */
	public void updateBitField(String remotePeerID, int pieceIndex) {
		ArrayList<Byte> remotePeerBitField = peersBitfields.get(remotePeerID);
		Util.setPieceIndexInBitField(remotePeerBitField, pieceIndex);
	}

}
