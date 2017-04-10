package peer;

import util.ConfigurationSetup;
import util.Util;

import java.awt.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import java.util.Collections;
import java.util.Date;

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
	private String optimistically_unchoked_neighbor = new String();

	/** the unchoked neighbor. */
	private int unchoked_neighbor = -1;

	/** time interval used to update preferred neighbors. */
	private int time_interval_p_preferred_neighbor = 0;

	/** time intervals used to update unchoked neighbor. */
	private int time_interval_m_unchoked_neighbor = 0;

	/** number of preferred neighbors. */
	private int k_preferred_neighbors = 0;

	/** data structures for maintaining the control logic */
	/**
	 * a map to find which peer is in interested. * These are peers which are
	 * interested in this local peers data.
	 */
	HashMap<String, Boolean> interested_peer_list = new HashMap<String, Boolean>();
	/** track the download speed from each peers, in number of pieces. */
	HashMap<String, Integer> download_speed = new HashMap<String, Integer>();

	/** Map to store which remote peer is left to complete their file */
	private HashMap<String, String> incompleteNeighbors = new HashMap<>();

	/*
	 * map of neighbors who are interested in local pieces. Only choose
	 * preferred and Unchoked neighbor from this list!
	 */
	// private ArrayList<String> interestedNeighbors = new ArrayList<>();

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

	/**
	 * This is the list of all peers who have currently unchoked the local peer.
	 * This is used to decide whether or not to send a 'request' message.
	 */
	private HashMap<String, String> peersWhoHaveUnChokedThisLocalPeer = new HashMap<>();

	/**
	 * This is a hashMap containing the unchokedNeighbors. This is used so as to
	 * not send unnecessary choke/un-choke messages to peers who are already
	 * choked/un-choked.
	 */
	private HashMap<String, String> unchokedNeighbors = new HashMap<>();

	private boolean gotCompletedFile = false;

	private boolean keepRunning = true;

	public Object PEER_PROCESS_LOCK = new Object();
	
	public boolean isKeepRunning() {
		return keepRunning;
	}

	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}

	/**
	 * This is called when this local peer got a unchoke from a remote peer.
	 * 
	 * @param remotePeerID
	 */
	public synchronized void addPeerWhoHasUnchokedLocal(String remotePeerID) {
		peersWhoHaveUnChokedThisLocalPeer.put(remotePeerID, remotePeerID);
	}

	/**
	 * This is called when this local peer got a choke from a remote peer.
	 * 
	 * @param remotePeerID
	 */
	public synchronized void removePeerWhoChokedLocal(String remotePeerID) {
		peersWhoHaveUnChokedThisLocalPeer.remove(remotePeerID);
	}

	/**
	 * Check if the remotePeer has unchoked this local peer.
	 * 
	 * @param remotePeerID
	 * @return
	 */
	public synchronized boolean isRemotePeerUnchokedLocal(String remotePeerID) {
		return this.peersWhoHaveUnChokedThisLocalPeer.containsKey(remotePeerID);
	}

	public boolean getGotCompletedFile() {
		boolean isCompleted = false;
		synchronized (localPeerBitField) {
			isCompleted = isPeerCompleted(localPeerBitField, localPeerID);
		}
		return isCompleted;
		// return gotCompletedFile;
	}

	public void setGotCompletedFile(boolean gotCompletedFile) {
		this.gotCompletedFile = gotCompletedFile;
	}

	public PeerProcess(String localPeerID) {
		this.localPeerID = localPeerID;
		localPeerInfo = Util.getPeerInfo(localPeerID);
		localPeerBitField = Util.getPeerBitfield(localPeerInfo.isHasFileInitially());
		// System.out.println("the bitfield for peer: "+localPeerID+" is:");
		// utilInstance.printBitfield(localPeerBitField);
		time_interval_p_preferred_neighbor = ConfigurationSetup.getUnchokingInterval();
		time_interval_m_unchoked_neighbor = ConfigurationSetup.getOptimisticUnchokingInterval();
		k_preferred_neighbors = ConfigurationSetup.getInstance().getNumberOfPreferredNeighbors();
		findNeighbors();
		setPeersBitfields();
		this.gotCompletedFile = localPeerInfo.isHasFileInitially();
		initializePiecesRemainingMap();
		// if local peer has complete file, divide file into required pieces and
		// place into correct peer sub-directory.
		if (gotCompletedFile) {
			Util.splitDataFile(localPeerID);
		} else {
			Util.makePeerDirectory(localPeerID);
		}
		clearDownloadSpeed();

		// 1). initialize interested neighbor
		// 2). initialize preferred neighbor
		// 3). initialize optimistically unchoked neighbor.
	}

	public void addInterestedNeighbor(String remotePeerID) {
		this.interested_peer_list.put(remotePeerID, true);
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
			setPeerBitField(peer.getPeerID(), Util.getPeerBitfield(peer.isHasFileInitially()));
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

		// while(keepRunning) {
		//
		// try {
		// Thread.sleep(20000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		start_p_timer();
		start_m_timer();
	}

	/**
	 * Initialize the neighbors list and the incomplete neighbors list.
	 */
	private void findNeighbors() {

		neighbors = Util.getMyPeerList(localPeerID);

		for (PeerInfo peer : Util.getPeerList()) {
			if (!peer.getPeerID().equals(getPeerID())) {
				// none of the neighbors is preferred neighbor at the begining.
				preferred_neighbors.put(peer.getPeerID(), false);
				if (!peer.isHasFileInitially()) {
					this.incompleteNeighbors.put(peer.getPeerID(), null);
				}
			}
		}
	}

	/**
	 * Called when we receive a broadcast of complete from the remote peer.
	 * 
	 * @param remotePeerID
	 */
	public void updateRemoteNeighborWhoIsComplete(String remotePeerID) {
		this.incompleteNeighbors.remove(remotePeerID);
	}

	/**
	 * Timer for preferred neighbors at ever 'p' seconds.
	 */
	public void start_p_timer() {
		Timer timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				// update preferred neighbors.
				// update download speed.
				try {
					updatePreferredNeighbors();
				} catch (Exception e) {
					System.err.println("not enough interested peers. Need "
							+ ConfigurationSetup.getInstance().getNumberOfPreferredNeighbors());
					e.printStackTrace();
				}
				clearDownloadSpeed();
			}
		}, ConfigurationSetup.getUnchokingInterval() * 1000, ConfigurationSetup.getUnchokingInterval() * 1000);

	}

	/**
	 * Timer for optimistically unchoked neighbor at ever 'm' seconds.
	 */
	public void start_m_timer() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				// update optimistically unchoked neighbor
				// System.out.println("time interval: " +
				// ConfigurationSetup.getOptimisticUnchokingInterval());
				try {
					updateUnchokedNeighbor();
				} catch (Exception e) {
					// System.err.println("not enough interested peers. Need "
					// +
					// ConfigurationSetup.getInstance().getNumberOfPreferredNeighbors());
					e.printStackTrace();
				}
			}
		}, ConfigurationSetup.getOptimisticUnchokingInterval() * 1000,
				ConfigurationSetup.getOptimisticUnchokingInterval() * 1000);

	}

	/**
	 * Initially, choose k preferred neighbors randomly.
	 */
	public void initializePreferredNeighbors() {

		int k = k_preferred_neighbors;

		ArrayList<String> peerIDs = new ArrayList<String>();

		StringBuilder peer_list = new StringBuilder();
		synchronized (interested_peer_list) {
			// get peerID list of all interested neighbors
			for (PeerInfo peer : neighbors) {
				if (interested_peer_list.containsKey(peer.getPeerID())
						&& interested_peer_list.get(peer.getPeerID()) == true) {

					peerIDs.add(peer.getPeerID());
				}
			}

			Collections.shuffle(peerIDs);
			for (int i = 0; i < k && i < peerIDs.size(); i++) {

				preferred_neighbors.put(peerIDs.get(i), true);
				peer_list.append(peerIDs.get(i) + ", ");
			}
		}

		if (peer_list.length() > 0)
			peer_list.setLength(peer_list.length() - 2);
		// [Time]: Peer [peer_ID] has the preferred neighbors [preferred
		// neighbor ID list].
		// [preferred neighbor list] is the list of peer IDs separated by comma
		// ‘,’.

		logger.info(Util.dateFormat.format(new Date()) + ": Peer " + localPeerID + " has the preferred neighbors " + "["
				+ peer_list.toString() + "]" + ".");
	}

	/**
	 * Then every p seconds, peer A reselects its preferred neighbors. To make
	 * the decision, peer A calculates the downloading rate from each of its
	 * neighbors, respectively, during the previous unchoking interval. Among
	 * neighbors that are interested in its data, peer A picks k neighbors that
	 * has fed its data at the highest rate. 1). select at most K preferred
	 * neighbors from interested_peer_list 2). choke peers that are not in the
	 * newPreferredKNeighbors 3). unchoke peers that were not in
	 * preferredKNeighbors and now in newPreferredKNeighbors
	 * 
	 */
	public void updatePreferredNeighbors() throws Exception {

		if (keepRunning == false) {

			return;
		}

		HashMap<String, Boolean> newPreferredKNeighbors = new HashMap<String, Boolean>();
		StringBuilder peer_list = new StringBuilder();

		class peer_speed_pair implements Comparable<peer_speed_pair> {
			String peerID;
			int speed;

			public peer_speed_pair(String peerid, int spe) {
				peerID = peerid;
				speed = spe;
			}

			@Override
			public int compareTo(peer_speed_pair o) {
				return (speed - o.speed);
			}
		}

		int k = ConfigurationSetup.getInstance().getNumberOfPreferredNeighbors();
		ArrayList<peer_speed_pair> speed_list = new ArrayList<peer_speed_pair>();
		for (String key : download_speed.keySet()) {

			speed_list.add(new peer_speed_pair(key, download_speed.get(key)));
		}

		Collections.sort(speed_list, Collections.reverseOrder());

		boolean noOneInterested = true;

		synchronized (interested_peer_list) {

			// use the first k peers in the interested_peer_list
			int index = 0;
			for (peer_speed_pair entry : speed_list) {
				// System.err.println("speed: " + entry.peerID);
				// System.err.println("interested?: " +
				// interested_peer_list.get(entry.peerID));
				if (index >= k)
					break;

				if (interested_peer_list.containsKey(entry.peerID) && interested_peer_list.get(entry.peerID) == true) {
					newPreferredKNeighbors.put(entry.peerID, true);
					noOneInterested = false;
					index++;
				}
			}
		}

		// if (noOneInterested) {
		if (noOneInterested) {
			// throw new Exception("no one is interested!");
			if (preferred_neighbors.isEmpty()) {

				return;
			} else {
				synchronized (preferred_neighbors) {
					// update preferred_neighbors
					for (String peerid : preferred_neighbors.keySet()) {

						this.connManager.sendMessage(peerid, Message.MESSAGETYPE_CHOKE);
					}
				}

			}
		}

		HashMap<String, String> needToNotify = new HashMap<String, String>();

		// choke peers that is not in newPreferredKNeighbors
		// unchoke peers that were not in preferredKNeighbors and now selected.

		synchronized (preferred_neighbors) {
			for (PeerInfo peer : neighbors) {
				String id = peer.getPeerID();
				if (!newPreferredKNeighbors.containsKey(id)) { // if id is
																// selected this
																// time
					needToNotify.put(id, "choke");
					// choke(id); // choke peer
				} else { //
					needToNotify.put(id, "unchoke");
				}
			}
		}

		synchronized (preferred_neighbors) {
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

		if (peer_list.length() > 0)
			peer_list.setLength(peer_list.length() - 2);
		// [Time]: Peer [peer_ID] has the preferred neighbors [preferred
		// neighbor ID list].
		// [preferred neighbor list] is the list of peer IDs separated by comma
		// ‘,’.

		logger.info(Util.dateFormat.format(new Date()) + ": Peer " + localPeerID + " has the preferred neighbors " + "["
				+ peer_list.toString() + "]" + ".");

		System.out.println(Util.dateFormat.format(new Date()) + ": Peer " + localPeerID
				+ " has the preferred neighbors " + "[" + peer_list.toString() + "]" + ".");

		// notify neighbors
		for (String peerid : needToNotify.keySet()) {
			if (needToNotify.get(peerid).equals("choke")) {
				if (this.unchokedNeighbors.containsKey(peerid)) {
					this.connManager.sendMessage(peerid, Message.MESSAGETYPE_CHOKE);
					unchokedNeighbors.remove(peerid);
				}
			} else {
				if (!this.unchokedNeighbors.containsKey(peerid)) {
					this.connManager.sendMessage(peerid, Message.MESSAGETYPE_UNCHOKE);
				}
			}
		}
	}

	/**
	 * sort map entries by values.
	 * 
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
	public void updateUnchokedNeighbor() throws Exception {

		if (keepRunning == false) {
			return;
		}

		ArrayList<String> interested_choked_peers = new ArrayList<String>();
		Set<String> peerSet = null;
		synchronized (interested_peer_list) {
			peerSet = interested_peer_list.keySet();
		}

		if (peerSet.isEmpty()) {
			// No peer is interested in local peer pieces.
			// throw new Exception("interested_peer_list is empty!");
			if (optimistically_unchoked_neighbor == null) {
				// No peer was chose before
				return;
			} else {
				// no peer is chosen this time and we choke earlier
				// optimistically_unchoked_peer.
				if (optimistically_unchoked_neighbor != null) {
					this.connManager.sendMessage(optimistically_unchoked_neighbor, Message.MESSAGETYPE_CHOKE);
				}
				optimistically_unchoked_neighbor = null;
				return;
			}
		} else {
			// there are peers interested in local peer's pieces.
			for (String peer : peerSet) {
				if (interested_peer_list.get(peer) == true && !preferred_neighbors.containsKey(peer)) {
					// choose interested peers which are choked
					interested_choked_peers.add(peer);
				}
			}

			if (!interested_choked_peers.isEmpty()) {
				// if there is an interested peer which was choked
				Collections.shuffle(interested_choked_peers);
				synchronized (optimistically_unchoked_neighbor) {
					optimistically_unchoked_neighbor = interested_choked_peers.get(0);
				}
			} else {
				// no interested peer which is currently choked
				if (optimistically_unchoked_neighbor != null) {
					this.connManager.sendMessage(optimistically_unchoked_neighbor, Message.MESSAGETYPE_CHOKE);
				}
				optimistically_unchoked_neighbor = null;
				return;
			}

			// [Time]: Peer [peer_ID] has the optimistically unchoked neighbor
			// [optimistically unchoked neighbor ID].
			logger.info(Util.dateFormat.format(new Date()) + ": Peer " + localPeerID
					+ " has the optimistically unchoked neighbor " + optimistically_unchoked_neighbor + ".");

			System.out.println(Util.dateFormat.format(new Date()) + ": Peer " + localPeerID
					+ " has the optimistically unchoked neighbor " + optimistically_unchoked_neighbor + ".");
			// unchoke the new optimistically unchoked neighbor
			if (optimistically_unchoked_neighbor != null) {
				this.connManager.sendMessage(optimistically_unchoked_neighbor, Message.MESSAGETYPE_UNCHOKE);
			}
		}
	}

	public void updateInterested_peer_list(String remotePeerID, int messageType) throws Exception {
		HashMap<String, Boolean> interested_peer_list = new HashMap<String, Boolean>();

		if (messageType == Message.MESSAGETYPE_INTERESTED) {
			interested_peer_list.put(remotePeerID, true);
		} else if (messageType == Message.MESSAGETYPE_NOTINTERESTED) {
			interested_peer_list.put(remotePeerID, false);
		}
	}

	/**
	 * When the timer is triggered, download speed will be cleared.
	 */
	public void clearDownloadSpeed() {
		for (PeerInfo peer : neighbors) {
			download_speed.put(peer.getPeerID(), 0);
		}
	}

	/**
	 * When a receive a piece, update the download speed of the source peer.
	 */
	public void updateDownloadSpeed(String peerID) {

		synchronized (download_speed) {

			if (download_speed.containsKey(peerID))
				download_speed.put(peerID, download_speed.get(peerID) + 1);
			else {
				download_speed.put(peerID, 1);
			}
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
	 * This method returns the piece index that we are interested in from the
	 * remote peer. Should update the remaining pieces and requested pieces map.
	 * 
	 * @param remotePeerID
	 * @return the piece index of the piece required from the remote peer, else
	 *         -1 if there is no piece required from that particular remote
	 *         peer.
	 */
	public int getPieceToBeRequested(String remotePeerID) {
		ArrayList<Byte> remotePeerBitField = peersBitfields.get(remotePeerID);
		ArrayList<Integer> piecesToBeRequestedArray = new ArrayList<Integer>(piecesRemainingToBeRequested.keySet());
		Collections.shuffle(piecesToBeRequestedArray);
		for (Integer i : piecesToBeRequestedArray) {
			if (!this.piecesRequested.containsKey(i)) {
				if (Util.isPieceIndexSetInBitField(i, remotePeerBitField)) {
					return i;
				}
			}
		}

		StringBuilder sbToBeReq = new StringBuilder();
		StringBuilder sbReq = new StringBuilder();

		for (Integer pieceIndex : piecesRemainingToBeRequested.keySet()) {
			sbToBeReq.append(pieceIndex + ", ");
		}

		for (Integer pieceIndex : this.piecesRequested.keySet()) {
			sbReq.append(pieceIndex + ", ");
		}

		System.err.println("This is the remote bitfield for peer:" + remotePeerID + "\n"
				+ Util.bitfieldToString(remotePeerBitField) + "\n the local bitfield for peer: " + this.localPeerID
				+ "\n" + Util.bitfieldToString(this.localPeerBitField) + "\n the local peer needs peices, "
				+ sbToBeReq.toString() + "\n the local peer requested peices, " + sbReq.toString());

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
		if (this.gotCompletedFile || getPieceToBeRequested(remotePeerID) == -1) {
			return false;
		} else {
			return true;
		}
	}

	public void removeNeighborWhoIsNotInterested(String remotePeerID) {
		this.interested_peer_list.put(remotePeerID, false);
		// this.interestedNeighbors.remove(remotePeerID);
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
	 * 
	 * @param remotePeerID
	 * @param pieceIndex
	 */
	public void updateBitField(String remotePeerID, int pieceIndex) {
		ArrayList<Byte> remotePeerBitField = peersBitfields.get(remotePeerID);
		Util.setPieceIndexInBitField(pieceIndex, remotePeerBitField);

		// System.out.println("Peer " + localPeerID + ": remote bitfield for " +
		// remotePeerID + "bitfield: "
		// + Util.bitfieldToString(remotePeerBitField));
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
	 * @return list of peers which do not have interesting pieces for the local
	 *         peer.
	 */
	public ArrayList<String> getListOfUnInterestingPeers() {

		ArrayList<String> uninterestedPeerList = new ArrayList<>();

		for (int remotePeerIndex = 0; remotePeerIndex < neighbors.size(); remotePeerIndex++) {
			ArrayList<Byte> remotePeerBitField = peersBitfields.get(neighbors.get(remotePeerIndex).getPeerID());
			boolean isRemotePeerInteresting = false;
			for (Integer pieceIndexNeededInLocalPeer : this.piecesRemainingToBeRequested.keySet()) {
				if (Util.isPieceIndexSetInBitField(pieceIndexNeededInLocalPeer, remotePeerBitField)) {
					isRemotePeerInteresting = true;
					break;
				}
			}
			if (!isRemotePeerInteresting) {
				uninterestedPeerList.add(neighbors.get(remotePeerIndex).getPeerID());
			}
		}

		return uninterestedPeerList;
	}

	/**
	 * This is called when a piece has been requested from a remote peer.
	 * 
	 * @param pieceToBeRequestedFromPeer
	 */
	public void updatePieceRequested(int pieceToBeRequestedFromPeer) {

		synchronized (piecesRemainingToBeRequested) {
			this.piecesRemainingToBeRequested.remove(pieceToBeRequestedFromPeer);
			Util.printArrayListOfIntegersFromLocalPeer(piecesRemainingToBeRequested, this.localPeerID,
					pieceToBeRequestedFromPeer, "piecesRemainingToBeRequestedMap in updatePieceRequested()");
		}
		synchronized (piecesRequested) {
			this.piecesRequested.put(pieceToBeRequestedFromPeer, pieceToBeRequestedFromPeer);
			Util.printArrayListOfIntegersFromLocalPeer(piecesRequested, this.localPeerID, pieceToBeRequestedFromPeer,
					"piecesRequested in updatePieceRequested()");
		}
		
		System.err
				.println("[debug] " +localPeerID+ " ********** piece " + pieceToBeRequestedFromPeer 
						+ " piecesRemainingToBeRequested ----> piecesRequested");
	}

	/**
	 * This is called when a piece is received from a remote peer. Also update
	 * the local bitfield.
	 * 
	 * @param pieceIndex
	 */
	public void updatePieceRecieved(int pieceIndex) {
		synchronized (piecesRequested) {
			this.piecesRequested.remove(pieceIndex);
			Util.setPieceIndexInBitField(pieceIndex, this.localPeerBitField);
			Util.printArrayListOfIntegersFromLocalPeer(piecesRequested, this.localPeerID, pieceIndex,
					"piecesRequestedMap in updatePieceRecieved()");
		}
	}

	/**
	 * Get the number of pieces received so far. This method will access
	 * piecesRemainingToBeRequested and piecesRequested.
	 * 
	 * @return
	 */
	public int getNumberOfPiecesSoFar() {

		if (gotCompletedFile) {
			return ConfigurationSetup.getNumberOfPieces();
		} else {
			return ConfigurationSetup.getNumberOfPieces() - piecesRemainingToBeRequested.size()
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

	// >>>>>>**************** getter and setters *********************

	public boolean isEveryPeerCompleted() {

		if (gotCompletedFile == false) {

			return false;
		}

		for (String peerid : peersBitfields.keySet()) {

			ArrayList<Byte> bfield = peersBitfields.get(peerid);

			if (!isPeerCompleted(bfield, peerid)) {
				// System.out.println("[debug] in peer:" + localPeerID + ", Peer
				// " + peerid + " unfinished!");
				return false;
			} else {
				// System.out.println("[debug] in peer " + localPeerID + ", Peer
				// " + peerid + " finished!");
			}
		}
		return true;
	}

	public boolean isPeerCompleted(ArrayList<Byte> bfield, String peerid) {
		int lengthOfBitfield = ConfigurationSetup.getNumberOfPieces() / 8;
		int remaining = ConfigurationSetup.getNumberOfPieces() - (lengthOfBitfield) * 8;
		// System.err.println("remaining: " + remaining);

		for (int i = 0; i < lengthOfBitfield; i++) {
			if (bfield.get(i).byteValue() != (byte) 0xFF) {
				// System.err.println("[debug] peer " + localPeerID + ":
				// bitfield of peer: " + peerid + " "
				// + Util.bitfieldToString(bfield));
				return false;
			}
		}

		if (remaining != 0) {
			Byte b = Util.setFirstNDigits(remaining);
			byte last_byte = b.byteValue();
			if (last_byte != bfield.get(bfield.size() - 1).byteValue()) {
				// System.out.println(" byte " + (bfield.size() - 1) + "
				// unfilled yet.");
				return false;
			}
		}
		return true;

	}

	public void checkIfEveryoneIsCompleteAndExit() {
		if (this.gotCompletedFile && this.incompleteNeighbors.isEmpty()) {
			System.exit(0);
		}
	}

}
