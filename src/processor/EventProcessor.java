package processor;

import messages.Message;
import peer.PeerProcess;
import test.PeerProcesses;
import util.Util;

/**
 * @author Mickey Vellukunnel
 */

/*
 * TODO: Now for local testing, we have a map of peerIDs to their PeerProcesses.
 * In the actual program on remote server, it will be only one PeerProcess running on 
 * each single remote machine instance and everything in it will be static. 
 */

public class EventProcessor {
	
	private String localPeerID, remotePeerID;
	private PeerProcess localPeerProcessInstance = null;
	
	public EventProcessor(String localPeerID, String remotePeerID) {
		this.localPeerID = localPeerID;
		this.remotePeerID = remotePeerID;
		localPeerProcessInstance = PeerProcesses.peerProcesses.get(localPeerID);
	}

	/**
	 * Get a message from peer B.
	 * @param messageBytes
	 */
	public void processMessage(byte[] messageBytes) {
		/*
		 * An actual message consists of; 1. 4-byte message length field (It
		 * does not include the length of the message length field itself.), [We
		 * are not using it in our implementation since we can read message
		 * length for DataInputStreams] 2. 1-byte message type field, and a 3.
		 * message payload with variable size. (optional)
		 */

		// TODO: process message. first byte is message type. rest of the bytes is
		// optional message payload
		
		//System.out.println(x);
		int type = Message.getMessageType(messageBytes);
		byte[] payload = Message.getMessagePayload(messageBytes);
		
		switch(type) {
		
		
		case Message.MESSAGETYPE_CHOKE:
			/** This happens when;
			 * Received when this peer becomes a preferred or optimistically unchoked neighbor of a remote peer,
			 * and if this peer is interested in a piece from that peer.
			 * If this peer has a complete file, or that remote peer has no interesting pieces, then this was receieved in error;
			 */
			
			if(localPeerProcessInstance.checkIfInterested(remotePeerID)) {
				System.out.println("in peer: " + this.localPeerID+", got CHOKE message from peer: "+this.remotePeerID+", when that peer does not have any interesting pieces. This peer should not have been selected by that peer!");
				Message.sendMessage(Message.MESSAGETYPE_NOTINTERESTED, remotePeerID);
				return;
			} else {
			// do nothing
			}
			break;
			
			
		case Message.MESSAGETYPE_UNCHOKE:
			/** This happens when;
			 * 
			 * Received when this peer stops being a preferred or optimistically unchoked neighbor of a remote peer,
			 * and if this peer is interested in a piece from that peer.
			 * If this peer has a complete file, or that remote peer has no interesting pieces, then this was receieved in error;
			 */
			
			/* Action to take;
			 * 
			 * if local peer has complete file, send "not_interested" message to B. 
			 * else, send "request" to B, if B has pieces 
			 * that local peer doesn't have and hasen't requested.
			*/
			if(localPeerProcessInstance.checkIfInterested(remotePeerID)) {
				System.out.println("in peer: " + this.localPeerID+", got UNCHOKE message from peer: "+this.remotePeerID+", when that peer does not have any interesting pieces. This peer should not have been selected by that peer!");
				Message.sendMessage(Message.MESSAGETYPE_NOTINTERESTED, remotePeerID);
				return;
			} else {
				int pieceToBeRequestedFromPeer = localPeerProcessInstance.getPieceToBeRequested(remotePeerID);
				if(pieceToBeRequestedFromPeer == -1) {
					System.out.println("in peer: " + this.localPeerID+", got UNCHOKE message from peer: "+this.remotePeerID+", when that peer does not have any interesting pieces. This peer should not have been selected by that peer!");
					return;
				} else {
					byte[] pieceIndexMessagePayload = Util.intToByteArray(pieceToBeRequestedFromPeer);
					Message.sendMessage(Message.MESSAGETYPE_REQUEST, pieceIndexMessagePayload, remotePeerID);
				}
			}
			
			break;
			
		case Message.MESSAGETYPE_INTERESTED:
			/** This happens when;
			 * 
			 * Received if this peer sent a "have" message to a remote peer, and that peer wants that piece.
			 */
			
			/* Action to take;
			 * 
			 * if B is choked, add B into interested_peer_list if B is not there.
			 * else B is unchoked, 
			 * 		check "sent_have_map" 1). if B is there, send piece x from the
			 * 							 map to B, and update bitfield for B
			 * 							2). if B is not there, add B into interesetd_peer_list
			 */
			
			this.localPeerProcessInstance.addInterestedNeighbor(remotePeerID);
			int pieceIndexToBeSent = this.localPeerProcessInstance.getPieceIndexToSendToPeer(remotePeerID);
			byte[] pieceMessagePayload = Util.getPieceAsByteArray(pieceIndexToBeSent);
			Message.sendMessage(Message.MESSAGETYPE_PIECE, pieceMessagePayload, remotePeerID);
			
			break;
		case Message.MESSAGETYPE_NOTINTERESTED:
			/** This happens when;
			 * 
			 * Received if this peer sent a "have" message and that peer didn't want it, 
			 * or if that remote peer got a piece and decided that this local peer is no longer interesting for it.
			 */
			/*
			 * update interested peers list.
			 */
			localPeerProcessInstance.removeNeighborWhoIsNotInterested(remotePeerID);
			break;
		case Message.MESSAGETYPE_HAVE:
			/** This happens when;
			 * 
			 * the remote peer has received a piece, so it sends that information to
			 * all of its neighbors so that they can update their bitfields and request
			 * that piece as needed.
			 */
			/*
			 * Check if x is what we don't have and not requested yet.
			 * 		1). if so, send "interested" to B
			 * 		2). else, send "not interested" to B.
			 * Update bitfield of B.
			 */
			
			int pieceIndex = Util.intFromByteArray(payload);
			
			if (this.localPeerProcessInstance.isPieceNotAvailableOrNotRequested(pieceIndex)) {
				Message.sendMessage(Message.MESSAGETYPE_INTERESTED, remotePeerID);
			} else {
				Message.sendMessage(Message.MESSAGETYPE_NOTINTERESTED, remotePeerID);
			}
			
			this.localPeerProcessInstance.updateBitField(remotePeerID,pieceIndex);
			
			break;
		case Message.MESSAGETYPE_REQUEST:
			/*
			 * send x as "piece" message to B, and update bitfield of B.
			 */
		case Message.MESSAGETYPE_PIECE:
			/*
			 * 1). if x not exist locally, save x as a piece file.
			 * 		and a). send "have" x messages to all peers.
			 * 		 	b). send "not_interested" messages to peers who don't have interesting pieces.
			 * 			c). check if we have all pieces. 
			 * 				i). If we do, 
			 * 						update "have_complete_file" variable, and create the
			 * 						complete data file.
			 * 				    				check bitfield of other peers. If all of them have all
			 * 								pieces, then end the peer program.
			 * 				ii). if we do not, send "request" about another piece, we don't have and
			 * 							not requested.
			 */
			
		}
		
		
		
	}
	
	public void preferredNeighborsUpdated() {
		//Only choose preferred and Unchoked neighbor from this PeerProcess.interestedNeighbors list!
		
		/*
		 * re-select k preferred neighbors from interested neighbors.
		 * 
		 * If B is an interested neighbor, then;
		 * Send unchoke message to B if B was choked before.
		 * send choke message to B if B was unchoked.
		 */
	}
	
	public void optimiticallyUnchokedNeighborUpdated() {
		//Only choose preferred and Unchoked neighbor from this PeerProcess.interestedNeighbors list!
		
		/*
		 * re-select one optimistically unchoked neighbor from interested neighbors.
		 * send choked to previous "optimistically unchoked neighbor" if it is not preferred neighbor now.
		 * send unchoke to the new "optimistically unchoked neighbor"
		 */
	}
	
	private boolean hasCompleteFile() {
		return localPeerProcessInstance.getGotCompletedFile();
	}
	
}






