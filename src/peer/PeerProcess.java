package peer;

import util.ConfigurationSetup;
import util.Util;

import java.awt.List;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import java.util.Collections;

import connection.TCPConnectionManager;
import type.PeerInfo;
import logging.FileLogger;
import messages.Message;

/**
 * This class sets up a peer in a peer-to-peer network 1. Initiate peer and set
 * peer ID. 2. Make TCP connections to other peers.
 * 
 * @author Xiaolong Li, Mickey Vellukunnel
 *
 */
public class PeerProcess {

	private Logger logger = Logger.getLogger(PeerProcess.class);
	private static Calendar calendar = Calendar.getInstance();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
	
	/** peer ID */
	private String localPeerID;
	private PeerInfo localPeerInfo = null;
	private Util utilInstance = Util.initializeUtil();

	private ArrayList<Byte> localPeerBitField = null;

	/* Map of peers' bitfields */
	private HashMap<String, ArrayList<Byte>> peersBitfields = new HashMap<>();

	private TCPConnectionManager connManager = null;

	public TCPConnectionManager getConnManager() {
		if (connManager == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return connManager;
	}

	/** this list contains all other peers' information in the network. */
	private ArrayList<PeerInfo> neighbors = new ArrayList<>();

	/** the index of the preferred neighbor set. */
	private HashMap<String, Boolean> preferred_neighbors = new HashMap<>();
	private String optimistically_unchoked_neighbor = null;
	
	/** the unchoked neighbor. */
	private int unchoked_neighbor = -1;

	/** time interval used to update preferred neighbors. */
	private int time_interval_p_preferred_neighbor = 0;
	
	/** time intervals used to update unchoked neighbor. */
	private int time_interval_m_unchoked_neighbor = 0;
	
	/** number of preferred neighbors. */
	private int k_preferred_neighbors = 0;
	
	/** data structures for maintaining the control logic*/
	/** a map to find which peer is in interested. */
	HashMap<String, Boolean> interested_peer_list = new HashMap<String, Boolean>();
	/** track the download speed from each peers, in number of pieces. */
	HashMap<String, Integer> download_speed = new HashMap<String, Integer>();

	
	/*
	 * map of neighbors who are interested in local pieces. Only choose
	 * preferred and Unchoked neighbor from this list!
	 */
	//private ArrayList<String> interestedNeighbors = new ArrayList<>();

	/*
	 * piecesRemainingToBeRequested + piecesRequested are the pieces the local
	 * peer DOES not yet have.
	 */
	// piecesRemainingToBeRequested -> pieces not present locally, and not
	// requested yet
	private HashMap<Integer, Integer> piecesRemainingToBeRequested = new HashMap<>();
	// piecesRequested -> pieces not present locally, and which have been
	// requested
	private HashMap<Integer, Integer> piecesRequested = new HashMap<>();

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
		time_interval_p_preferred_neighbor = ConfigurationSetup.getUnchokingInterval();
		time_interval_m_unchoked_neighbor = ConfigurationSetup.getOptimisticUnchokingInterval();
		k_preferred_neighbors = ConfigurationSetup.getInstance().getNumberOfPreferredNeighbors();
		findNeighbors();
		setPeersBitfields();
		this.gotCompletedFile = localPeerInfo.isHasFileInitially();
		initializePiecesRemainingMap();
		//if local peer has complete file, divide file into required pieces and place into correct peer sub-directory.
		if(gotCompletedFile) {
			Util.splitDataFile(localPeerID);
		} else {
			Util.makePeerDirectory(localPeerID);
		}
	}

	public void addInterestedNeighbor(String remotePeerID) {
		this.interested_peer_list.put(remotePeerID, true);
		//this.interestedNeighbors.add(remotePeerID);
	}

	private void initializePiecesRemainingMap() {
		if (!gotCompletedFile) {
			for (int i = 0; i < ConfigurationSetup.getNumberOfPieces(); i++) {
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
		// System.out.println("the bitfield for peer: "+remotePeerID+" is:");
		// utilInstance.printBitfield(remotePeerBitfield);
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
		connManager = new TCPConnectionManager(localPeerInfo, this);
		connManager.initializePeer();
	}

	/**
	 * Initialize the neighbors list.
	 */
	private void findNeighbors() {

		neighbors = Util.getMyPeerList(localPeerID);
		
		for (PeerInfo peer : Util.getPeerList()) {
			if (!peer.getPeerID().equals(getPeerID())) {
				// none of the neighbors is preferred neighbor at the begining.
				preferred_neighbors.put(peer.getPeerID(), false);
			}
		}
	}

	/**
	 * Choke a peer
	 * 
	 * @param peerID
	 */
	public void choke(String peerID) {
		//Message.sendMessage(Message.MESSAGETYPE_CHOKE, peerID);
	}

	/**
	 * unchoke a peer
	 * 
	 * @param peerID
	 */
	public void unchoke(String peerID) {
		//Message.sendMessage(Message.MESSAGETYPE_UNCHOKE, peerID);
	}

	/**
	 * Initially, choose k preferred neighbors randomly.
	 */
	public void initializePreferredNeighbors() {
		
		int k = k_preferred_neighbors;
		
		ArrayList<String> peerIDs = new ArrayList<String>();
		
		StringBuilder peer_list = new StringBuilder();
		synchronized(interested_peer_list) {
			// get peerID list of all interested neighbors
			for (PeerInfo peer : neighbors) {
				if (interested_peer_list.containsKey(peer.getPeerID())) {
					
					peerIDs.add(peer.getPeerID());
				}	
			}
			
			Collections.shuffle(peerIDs);
			for (int i = 0; i < k && i < peerIDs.size(); i++) {
	
				preferred_neighbors.put(peerIDs.get(i), true);
				peer_list.append(peerIDs.get(i) + ", ");
			}
		}
		
		if (peer_list.length() > 0) peer_list.setLength(peer_list.length()-2);
		//[Time]: Peer [peer_ID] has the preferred neighbors [preferred neighbor ID list].
		//[preferred neighbor list] is the list of peer IDs separated by comma ‘,’.
		
		logger.info(dateFormat.format(calendar.getTime()) 
				+ ": Peer " + localPeerID 
				+ " has the preferred neighbors "
				+ "[" + peer_list.toString() 
				+ "]" + ".");
	}

	/**
	 * Then every p seconds, peer A reselects its preferred neighbors. To make
	 * the decision, peer A calculates the downloading rate from each of its
	 * neighbors, respectively, during the previous unchoking interval. Among
	 * neighbors that are interested in its data, peer A picks k neighbors that
	 * has fed its data at the highest rate.
	 * 1). select at most K preferred neighbors from interested_peer_list
	 * 2). choke peers that are not in the newPreferredKNeighbors
	 * 3). unchoke peers that were not in preferredKNeighbors and now in newPreferredKNeighbors
	 * 
	 */
	public void updatePreferredNeighbors() throws Exception{
		
		HashMap<String, Boolean> newPreferredKNeighbors = new HashMap<String, Boolean>();
		StringBuilder peer_list = new StringBuilder();
		
		int k = ConfigurationSetup.getInstance().getNumberOfPreferredNeighbors();
		synchronized(download_speed) {
			download_speed = new HashMap<String, Integer> (sortByValue(download_speed));
		}
		
		boolean noOneInterested = true;
		
		synchronized(interested_peer_list) {
			
			// use the first k peers in the interested_peer_list
			int index = 0;
			for (Map.Entry<String, Integer> entry : download_speed.entrySet()) {
				if (index >= k) break;
				
				if (interested_peer_list.containsKey(entry.getKey())) {
					newPreferredKNeighbors.put(entry.getKey(), true);
					noOneInterested = false;
					index ++;
				}
			}
		}
		if (noOneInterested) {
			
			throw new Exception("no one is interested!");
		}
		
		HashMap<String, String> needToNotify = new HashMap<String, String>();
		
		// choke peers that is not in newPreferredKNeighbors
		// unchoke peers that were not in preferredKNeighbors and now selected.
		
		synchronized(preferred_neighbors) {
			for (PeerInfo peer : neighbors) {
				String id = peer.getPeerID();
				if (! newPreferredKNeighbors.containsKey(id)) { // if id is selected this time
					needToNotify.put(id, "choke");
					//choke(id); // choke peer
				} else if(! preferred_neighbors.containsKey(id)) { // 
					needToNotify.put(id, "unchoke");
				}
			}
		}
		
		synchronized(preferred_neighbors) {
			// update preferred_neighbors
			for (String peerid : preferred_neighbors.keySet()) {
				
				if (newPreferredKNeighbors.containsKey(peerid)) {
					preferred_neighbors.put(peerid, true);
					peer_list.append(peerid + ", ");
				} else {
					preferred_neighbors.put(peerid, false);
				}
			}
		}
		
		if (peer_list.length() > 0) peer_list.setLength(peer_list.length()-2);
		//[Time]: Peer [peer_ID] has the preferred neighbors [preferred neighbor ID list].
		//[preferred neighbor list] is the list of peer IDs separated by comma ‘,’.
		
		logger.info(dateFormat.format(calendar.getTime()) 
				+ ": Peer " + localPeerID 
				+ " has the preferred neighbors "
				+ "[" + peer_list.toString() 
				+ "]" + ".");
		
		// notify neighbors
		for (String peerid : needToNotify.keySet()) {
			
			if (needToNotify.get(peerid).equals("choke")) {
				
				choke(peerid);
			} else {
				unchoke(peerid);
			}
		}
	}
	
	/**
	 * sort map entries by values.
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
	

	/**
	 * Determines unchoked neighbor every m seconds. optimistically unchoked
	 * neighbor randomly among neighbors that are choked at that moment but are
	 * interested in its data.
	 */
	public void updateUnchokedNeighbor() throws Exception{
		
		ArrayList<String> interested_choked_peers = new ArrayList<String>();
		Set<String> peerSet = null;
		synchronized(interested_peer_list) {
			peerSet = interested_peer_list.keySet();
		}
		
		if (peerSet.isEmpty())
			throw new Exception("interested_peer_list is empty!");
			
		for (String peer : peerSet) {
			
			if (interested_peer_list.get(peer) == true 
					&& ! preferred_neighbors.containsKey(peer)) {
				
				interested_choked_peers.add(peer);
			}
		}
		
		if (! interested_choked_peers.isEmpty()) {
			Collections.shuffle(interested_choked_peers);
			
			synchronized(optimistically_unchoked_neighbor) {
				optimistically_unchoked_neighbor = 
						interested_choked_peers.get(0);
			}
		} else {
			Random random = new Random();
			int idx = random.nextInt(neighbors.size());
			
			synchronized(optimistically_unchoked_neighbor) {
				optimistically_unchoked_neighbor = 
						neighbors.get(idx).getPeerID();
			}
		}
		
		//[Time]: Peer [peer_ID] has the optimistically unchoked neighbor
		// [optimistically unchoked neighbor ID].
		logger.info(dateFormat.format(calendar.getTime())
				+ ": Peer " + localPeerID 
				+ " has the optimistically unchoked neighbor "
				+ optimistically_unchoked_neighbor + ".");
		
		// unchoke the new optimistically unchoked neighbor
		unchoke(optimistically_unchoked_neighbor);
	}
	
	public void updateInterested_peer_list(String remotePeerID, int messageType) throws Exception{
		HashMap<String, Boolean> interested_peer_list = new HashMap<String, Boolean>();

		if(messageType == Message.MESSAGETYPE_INTERESTED){
			interested_peer_list.put(remotePeerID, true);
		}
		else if(messageType == Message.MESSAGETYPE_NOTINTERESTED){
			interested_peer_list.put(remotePeerID, false);
		}
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
	 * This method returns the piece index that we are interested in from 
	 * the remote peer.
	 * Should update the remaining pieces and requested pieces map.
	 * 
	 * @param remotePeerID
	 * @return the piece index of the piece required from the remote peer, else
	 *         -1 if there is no piece required from that particular remote
	 *         peer.
	 */
	public int getPieceToBeRequested(String remotePeerID) {
		ArrayList<Byte> remotePeerBitField = peersBitfields.get(remotePeerID);
		//System.out.println("The bitfield for remotepeer#" + remotePeerID);
		//Util.printBitfield(remotePeerBitField);
		//System.out.println("The local bitfield for localpeer#" + this.localPeerID);
		//Util.printBitfield(this.localPeerBitField);
		for (int i = 0; i < ConfigurationSetup.getNumberOfPieces(); i++) {
			//System.out.println("Checking pieice#"+i+" for local bitfield for peer#" + this.localPeerID);
			if (piecesRemainingToBeRequested.containsKey(i) && !this.piecesRequested.containsKey(i)) {
				//System.out.println("pieice#"+i+"is not in local bitfield for peer#" + this.localPeerID);
				if(Util.isPieceIndexSetInBitField(i, remotePeerBitField)) {
					System.out.println("local peer#" + this.localPeerID + " NEEDS piece#" + i + " that remote peer#" + remotePeerID + " has!");
					return i;
				}
			}
		}

		return -1; // if the remote peer does not have any piece which this
					// local peer requires.
	}

	/**
	 * 
	 * @param remotePeerID
	 * @return boolean true/false as to whether this local peer is interested in
	 *         any of the remote peer's pieces.
	 */
	public boolean checkIfInterested(String remotePeerID) {
		if (this.gotCompletedFile || getPieceToBeRequested(remotePeerID) != -1) {
			return false;
		} else {
			return true;
		}
	}

	public void removeNeighborWhoIsNotInterested(String remotePeerID) {
		this.interested_peer_list.put(remotePeerID, false);
		//this.interestedNeighbors.remove(remotePeerID);
	}

	/**
	 * 
	 * @param pieceIndex
	 * @return boolean true if whether this piece is needed (i.e. piece is not
	 *         available locally AND not requested yet), false otherwise.
	 */
	public boolean isPieceNotAvailableOrNotRequested(int pieceIndex) {
		if (this.piecesRemainingToBeRequested.containsKey(pieceIndex)
				&& !this.piecesRequested.containsKey(pieceIndex)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Update the remotePeer's local bitfield to set the given pieceIndex.
	 * @param remotePeerID
	 * @param pieceIndex
	 */
	public void updateBitField(String remotePeerID, int pieceIndex) {
		ArrayList<Byte> remotePeerBitField = peersBitfields.get(remotePeerID);
		Util.setPieceIndexInBitField(pieceIndex, remotePeerBitField);
	}

	/**
	 * 
	 * @param pieceIndex
	 * @return true if the requested piece is available locally.
	 */
	public boolean isPieceAvailableLocally(int pieceIndex) {
		if (this.piecesRemainingToBeRequested.containsKey(pieceIndex) || this.piecesRequested.containsKey(pieceIndex)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 
	 * @return list of peers which do not have interesting pieces for the local peer.
	 */
	public ArrayList<String> getListOfUnInterestingPeers() {
		
		ArrayList<String> uninterestedPeerList = new ArrayList<>();
		for(int i = 0; i < neighbors.size(); i++){
			uninterestedPeerList.add(neighbors.get(i).getPeerID());
		}
		for(int i = 0; i < neighbors.size(); i++){
			ArrayList<Byte> remotePeerBitField = peersBitfields.get(neighbors.get(i));
			for(int j=0; j<remotePeerBitField.size();j++){
				for(int k = 0; k < 8; k++){
					if(Integer.toBinaryString(localPeerBitField.get(j)).charAt(k)== 0){
						if(Integer.toBinaryString(remotePeerBitField.get(j)).charAt(k)!= 0){
							uninterestedPeerList.remove(i);
							break;
						}
					}
				}
			}
		}
		
		return uninterestedPeerList;
	}

	/**
	 * This is called when a piece has been requested from a remote peer.
	 * @param pieceToBeRequestedFromPeer
	 */
	public void updatePieceRequested(int pieceToBeRequestedFromPeer) {
		this.piecesRemainingToBeRequested.remove(pieceToBeRequestedFromPeer);
		this.piecesRequested.put(pieceToBeRequestedFromPeer, pieceToBeRequestedFromPeer);
	}

	/**
	 * This is called when a piece is received from a remote peer.
	 * Also update the local bitfield.
	 * @param pieceIndex
	 */
	public void updatePieceRecieved(int pieceIndex) {
		this.piecesRequested.remove(pieceIndex);
		Util.setPieceIndexInBitField(pieceIndex, this.localPeerBitField);
	}
	
	/**
	 * Get the number of pieces received so far.
	 * This method will access piecesRemainingToBeRequested and piecesRequested.
	 * @return
	 */
	public int getNumberOfPiecesSoFar() {
		
		if (gotCompletedFile) {
			return ConfigurationSetup.getNumberOfPieces();
		} else {
			return ConfigurationSetup.getNumberOfPieces() 
					- piecesRemainingToBeRequested.size() 
					- piecesRequested.size();
		}
	}
	

	// <<<<<<<**************** getter and setters *********************
	public String getLocalPeerID() {
		return localPeerID;
	}

	public void setLocalPeerID(String localPeerID) {
		this.localPeerID = localPeerID;
	}

	public PeerInfo getLocalPeerInfo() {
		return localPeerInfo;
	}

	public void setLocalPeerInfo(PeerInfo localPeerInfo) {
		this.localPeerInfo = localPeerInfo;
	}

	public ArrayList<Byte> getLocalPeerBitField() {
		return localPeerBitField;
	}

	public void setLocalPeerBitField(ArrayList<Byte> localPeerBitField) {
		this.localPeerBitField = localPeerBitField;
	}

	public HashMap<String, ArrayList<Byte>> getPeersBitfields() {
		return peersBitfields;
	}

	public void setPeersBitfields(HashMap<String, ArrayList<Byte>> peersBitfields) {
		this.peersBitfields = peersBitfields;
	}

	public void setConnManager(TCPConnectionManager connManager) {
		this.connManager = connManager;
	}

	public ArrayList<PeerInfo> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(ArrayList<PeerInfo> neighbors) {
		this.neighbors = neighbors;
	}

	public HashMap<String, Boolean> getPreferred_neighbors() {
		return preferred_neighbors;
	}

	public void setPreferred_neighbors(HashMap<String, Boolean> preferred_neighbors) {
		this.preferred_neighbors = preferred_neighbors;
	}

	public String getOptimistically_unchoked_neighbor() {
		return optimistically_unchoked_neighbor;
	}

	public void setOptimistically_unchoked_neighbor(String optimistically_unchoked_neighbor) {
		this.optimistically_unchoked_neighbor = optimistically_unchoked_neighbor;
	}

	public int getUnchoked_neighbor() {
		return unchoked_neighbor;
	}

	public void setUnchoked_neighbor(int unchoked_neighbor) {
		this.unchoked_neighbor = unchoked_neighbor;
	}

	public int getK_preferred_neighbors() {
		return k_preferred_neighbors;
	}

	public void setK_preferred_neighbors(int k_preferred_neighbors) {
		this.k_preferred_neighbors = k_preferred_neighbors;
	}

	public HashMap<String, Boolean> getInterested_peer_list() {
		return interested_peer_list;
	}

	public void setInterested_peer_list(HashMap<String, Boolean> interested_peer_list) {
		this.interested_peer_list = interested_peer_list;
	}

	public HashMap<String, Integer> getDownload_speed() {
		return download_speed;
	}

	public void setDownload_speed(HashMap<String, Integer> download_speed) {
		this.download_speed = download_speed;
	}

	public HashMap<Integer, Integer> getPiecesRemainingToBeRequested() {
		return piecesRemainingToBeRequested;
	}

	public void setPiecesRemainingToBeRequested(HashMap<Integer, Integer> piecesRemainingToBeRequested) {
		this.piecesRemainingToBeRequested = piecesRemainingToBeRequested;
	}

	public HashMap<Integer, Integer> getPiecesRequested() {
		return piecesRequested;
	}

	public void setPiecesRequested(HashMap<Integer, Integer> piecesRequested) {
		this.piecesRequested = piecesRequested;
	}
	// >>>>>>**************** getter and setters *********************
	
	

}
