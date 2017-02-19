package peer;
import util.Util;

import java.util.ArrayList;

/**
 * This class define a peer in a peer-to-peer network
 * 1. Initiate peer class by reading configuration files and set peer ID.
 * 2. Make TCP connections to other peers
 * 3. 
 * @author Xiaolong Li
 *
 */
public class PeerProcess {
	
	/** peer ID*/
	private int peerID = 0;
	private int port = 0;
	private static Peer peerInstance = null;
	private static Util utilInstance = Util.getInstance();
	
	/** this list contains all other peers' information in the network. */
	private ArrayList<PeerInfo> neighbors = null;
	
	private PeerProcess(int peerID) {
		
		this.peerID = peerID;
	}
	
	public static Peer getPeerInstance(int peerID, ) {
		
		if (peerInstance == null) {
			
			return new Peer(peerID);
		} else {
			return peerInstance;
		}
	}
	
	/**
	 * Self initiation for the local machine.
	 */
	private void initiatePeer() {
		
		//get a list of all other peers in the network
		beighbors = PeerInfo.getPeerList();
		// get previous peer in order to start TCP connections
		ArrayList<PeerInfo> previousPeers = PeerInfo.getPreviousPeer(peerID);
		
		for(PeerInfo peer : previousPeers) {
			
			// create TCP connection with previous peers
		}
		
		if (previousPeers == null) {
			
			startListening();
		}
	}
	
	private void startListening() {
		
		
	}
	
	
	private void createConnection(String hostname, int port) {
		
		
	}
	
	/**
	 * Peer starts running from here
	 * @param args
	 */
	public static void main(String[] args) {
		
		
	}
	
	class PeerInfo {
		
		
	}
}
