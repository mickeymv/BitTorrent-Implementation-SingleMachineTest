package processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.log4j.Logger;

import messages.Message;
import peer.PeerProcess;
import test.PeerProcesses;
import util.Util;

/**
 * @author Mickey Vellukunnel
 */

/*
 * TODO: Now for local testing, we have a map of peerIDs to their PeerProcesses.
 * In the actual program on remote server, it will be only one PeerProcess
 * running on each single remote machine instance and everything in it will be
 * static.
 */

public class EventProcessor {

	private Logger logger = Logger.getLogger(PeerProcess.class);
	private static Calendar calendar = Calendar.getInstance();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
	
	private String localPeerID, remotePeerID;
	private PeerProcess localPeerProcessInstance = null;
	
	private Message messageHandler;

	public EventProcessor(String localPeerID, String remotePeerID) {
		this.localPeerID = localPeerID;
		this.remotePeerID = remotePeerID;
		//TODO: make sure when we actually test on remote machine, that we have a way to access the local running peerProcess.
		localPeerProcessInstance = PeerProcesses.peerProcesses.get(localPeerID);
		messageHandler = new Message(localPeerID, remotePeerID,localPeerProcessInstance);
	}

	/**
	 * Get a message from peer B.
	 * 
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

		// TODO: process message. first byte is message type. rest of the bytes
		// is
		// optional message payload

		// System.out.println(x);
		int type = messageHandler.getMessageType(messageBytes);
		byte[] payload = messageHandler.getMessagePayload(messageBytes);
		int pieceIndex = -1;
		switch (type) {

		case Message.MESSAGETYPE_CHOKE:
			/**
			 * This happens when; Received when this peer becomes a preferred or
			 * optimistically unchoked neighbor of a remote peer, and if this
			 * peer is interested in a piece from that peer. If this peer has a
			 * complete file, or that remote peer has no interesting pieces,
			 * then this was receieved in error;
			 */
			
			//[Time]: Peer [peer_ID 1] is choked by [peer_ID 2].
			logger.info(dateFormat.format(calendar.getTime()) 
					+ ": Peer " + localPeerID 
					+ " is choked by "
					+ this.remotePeerID + ".");
			
			System.out.println("in peer: " + this.localPeerID + ", got CHOKE message from peer: "
					+ this.remotePeerID);
			
			if (localPeerProcessInstance.checkIfInterested(remotePeerID)) {
				System.err.println("\nERROR! in peer: " + this.localPeerID + ", got CHOKE message from peer: "
						+ this.remotePeerID
						+ ", when that peer does not have any interesting pieces. This peer should not have been selected by that peer!");
				messageHandler.sendMessage(Message.MESSAGETYPE_NOTINTERESTED, remotePeerID);

				return;
			} else {
				// do nothing
			}
			break;

		case Message.MESSAGETYPE_UNCHOKE:
			/**
			 * This happens when;
			 * 
			 * Received when this peer stops being a preferred or optimistically
			 * unchoked neighbor of a remote peer, and if this peer is
			 * interested in a piece from that peer. If this peer has a complete
			 * file, or that remote peer has no interesting pieces, then this
			 * was receieved in error;
			 */
			// [Time]: Peer [peer_ID 1] is unchoked by [peer_ID 2].
			logger.info(dateFormat.format(calendar.getTime()) 
					+ ": Peer " + localPeerID 
					+ " is unchoked by "
					+ this.remotePeerID + ".");
			
			/*
			 * Action to take;
			 * 
			 * if local peer has complete file, send "not_interested" message to
			 * B. else, send "request" to B, if B has pieces that local peer
			 * doesn't have and hasen't requested.
			 */
			System.out
					.println("in peer: " + this.localPeerID + ", got UNCHOKE message from peer: " + this.remotePeerID);
			if (localPeerProcessInstance.checkIfInterested(remotePeerID)) {
				System.err.println("\nERROR! in peer: " + this.localPeerID + ", got UNCHOKE message from peer: "
						+ this.remotePeerID
						+ ", when that peer does not have any interesting pieces. This peer should not have been selected by that peer!");
				messageHandler.sendMessage(Message.MESSAGETYPE_NOTINTERESTED, remotePeerID);
				return;
			} else {
				int pieceToBeRequestedFromPeer = localPeerProcessInstance.getPieceToBeRequested(remotePeerID);
				if (pieceToBeRequestedFromPeer == -1) {
					System.err.println("\nERROR! in peer: " + this.localPeerID + ", got UNCHOKE message from peer: "
							+ this.remotePeerID
							+ ", when that peer does not have any interesting pieces. This peer should not have been selected by that peer!");
					return;
				} else {
					byte[] pieceIndexMessagePayload = Util.intToByteArray(pieceToBeRequestedFromPeer);
					messageHandler.sendMessage(Message.MESSAGETYPE_REQUEST, pieceIndexMessagePayload, remotePeerID);
					System.out.println("peer#: " + this.localPeerID + " sent a request for piece# "
							+ pieceToBeRequestedFromPeer + " to peer#" + this.remotePeerID);
					this.localPeerProcessInstance.updatePieceRequested(pieceToBeRequestedFromPeer);
				}
			}

			break;

		case Message.MESSAGETYPE_INTERESTED:
			/**
			 * This happens when;
			 * 
			 * Received if this peer sent a "have" message to a remote peer, and
			 * that peer wants that piece.
			 */
			//[Time]: Peer [peer_ID 1] received the ‘interested’
			// message from [peer_ID 2].
			logger.info(dateFormat.format(calendar.getTime()) 
					+ ": Peer " + localPeerID
					+ " received the ‘interested’ message from "
					+ this.remotePeerID + ".");
			
			/*
			 * Action to take;
			 * 
			 * if B is choked, add B into interested_peer_list if B is not
			 * there. else B is unchoked, check "sent_have_map" 1). if B is
			 * there, send piece x from the map to B, and update bitfield for B
			 * 2). if B is not there, add B into interesetd_peer_list
			 */

			System.out.println(
					"in peer: " + this.localPeerID + ", got INTERESTED message from peer: " + this.remotePeerID);

			this.localPeerProcessInstance.addInterestedNeighbor(remotePeerID);
			int pieceIndexToBeSent = this.localPeerProcessInstance.getPieceIndexToSendToPeer(remotePeerID);

			try {
				this.localPeerProcessInstance.updateInterested_peer_list(remotePeerID, Message.MESSAGETYPE_INTERESTED);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] pieceMessagePayload = Util.getPieceAsByteArray(localPeerID, pieceIndexToBeSent);
			messageHandler.sendPieceMessage(pieceIndex, pieceMessagePayload, remotePeerID);
			System.out.println("peer#: " + this.localPeerID + " sent piece message Piece# " + pieceIndexToBeSent
					+ " to peer#" + this.remotePeerID);

			break;
		case Message.MESSAGETYPE_NOTINTERESTED:
			/**
			 * This happens when;
			 * 
			 * Received if this peer sent a "have" message and that peer didn't
			 * want it, or if that remote peer got a piece and decided that this
			 * local peer is no longer interesting for it.
			 */
			//[Time]: Peer [peer_ID 1] received the ‘not interested’
			// message from [peer_ID 2].
			logger.info(dateFormat.format(calendar.getTime()) 
					+ ": Peer " + localPeerID
					+ " received the ‘not interested’ message from "
					+ this.remotePeerID + ".");
			
			
			/*
			 * update interested peers list.
			 */

			System.out.println(
					"in peer: " + this.localPeerID + ", got NOT_INTERESTED message from peer: " + this.remotePeerID);

			localPeerProcessInstance.removeNeighborWhoIsNotInterested(remotePeerID);
			
			try {
				this.localPeerProcessInstance.updateInterested_peer_list(remotePeerID, Message.MESSAGETYPE_NOTINTERESTED);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		case Message.MESSAGETYPE_HAVE:
			/**
			 * This happens when;
			 * 
			 * the remote peer has received a piece, so it sends that
			 * information to all of its neighbors so that they can update their
			 * bitfields and request that piece as needed.
			 */
			
			
			
			/*
			 * Check if x is what we don't have and not requested yet. 1). if
			 * so, send "interested" to B 2). else, send "not interested" to B.
			 * Update bitfield of B.
			 */
			
			pieceIndex = Util.intFromByteArray(payload);
			
			// [Time]: Peer [peer_ID 1] received the ‘have’ message
			// from [peer_ID 2] for the piece [piece index].
			logger.info(dateFormat.format(calendar.getTime()) 
					+ ": Peer " + localPeerID 
					+ " received the ‘have’ message from "
					+ this.remotePeerID 
					+ " for the piece "
					+ pieceIndex + ".");
			
			System.out.println("in peer: " + this.localPeerID + ", got 'HAVE' piece#" +pieceIndex+ " message from peer: "
					+ this.remotePeerID);

			if (this.localPeerProcessInstance.isPieceNotAvailableOrNotRequested(pieceIndex)) {
				messageHandler.sendMessage(Message.MESSAGETYPE_INTERESTED, remotePeerID);
				System.out.println(
						"peer#: " + this.localPeerID + " sent an INTERESTED (after receieving a HAVE ) for piece# "
								+ pieceIndex + " to peer#" + this.remotePeerID);

			} else {
				messageHandler.sendMessage(Message.MESSAGETYPE_NOTINTERESTED, remotePeerID);
			}

			this.localPeerProcessInstance.updateBitField(remotePeerID, pieceIndex);

			break;
		case Message.MESSAGETYPE_REQUEST:
			/**
			 * This happens when;
			 * 
			 * the local peer sent the remote peer an "unchoke" message, and
			 * that remote peer responds back with the piece that it requires
			 * from the local peer.
			 */
			/*
			 * 1) If B is unchoked, send x as "piece" message to B, and update
			 * bitfield of B. 2) if B is choked, put into map_of_requested
			 * pieces.
			 */
			pieceIndex = Util.intFromByteArray(payload);

			System.out.println("in peer: " + this.localPeerID + ", got 'REQUEST' piece#" + pieceIndex
					+ " message from peer: " + this.remotePeerID);

			// Safety check: check if piece is available in local peer

			if (this.localPeerProcessInstance.isPieceAvailableLocally(pieceIndex)) {
				pieceMessagePayload = Util.getPieceAsByteArray(localPeerID, pieceIndex);
				messageHandler.sendPieceMessage(pieceIndex, pieceMessagePayload, remotePeerID);
				System.out.println("peer#: " + this.localPeerID + " sent a Piece message piece# " + pieceIndex
						+ " to peer#" + this.remotePeerID);

			} else {
				System.out.println("in peer: " + this.localPeerID + ", got REQUEST of piece '" + pieceIndex
						+ "' message from peer: " + this.remotePeerID
						+ ", when this local peer does not have that piece. This piece should not have been selected by that peer!");
				return;
			}

		case Message.MESSAGETYPE_PIECE:
			/**
			 * This happens when; 1. The remote peer sents a "Have" message, and
			 * the local responds with "Intereted". 2. The local peer sends a
			 * "request" message.
			 * 
			 */
			/*
			 * 1). if x not exist locally, save x as a piece file. and a). send
			 * "have" x messages to all peers. b). send "not_interested"
			 * messages to peers who don't have interesting pieces. c). check if
			 * we have all pieces. i). If we do, update "have_complete_file"
			 * variable, and create the complete data file. check bitfield of
			 * other peers. If all of them have all pieces, then end the peer
			 * program. ii). if we do not, send "request" about another piece,
			 * we don't have and not requested.
			 */

			byte[] pieceIndexBytesArray = Arrays.copyOfRange(payload, 0, 4);
			byte[] pieceDataBytesArray = Arrays.copyOfRange(payload, 4, payload.length);

			pieceIndex = Util.intFromByteArray(pieceIndexBytesArray);

			System.out.println("in peer: " + this.localPeerID + ", got 'PIECE' piece#" + pieceIndex
					+ " message from peer: " + this.remotePeerID);
			
			Util.savePieceFile(pieceDataBytesArray, this.localPeerID, String.valueOf(pieceIndex));

			this.localPeerProcessInstance.updatePieceRecieved(pieceIndex);
			messageHandler.broadcastHavePieceIndexMessageToAllPeers(pieceIndex);
			
			//[Time]: Peer [peer_ID 1] has downloaded the piece [piece index]
			// from [peer_ID 2]. Now the number of pieces it has is 
			// [number of pieces].
			logger.info(dateFormat.format(calendar.getTime()) 
					+ ": Peer " + localPeerID 
					+ " has downloaded the piece " + pieceIndex
					+ " from " + this.remotePeerID
					+ ". Now the number of pieces it has is "
					+ localPeerProcessInstance.getNumberOfPiecesSoFar() + ".");
			
			messageHandler.broadcastHavePieceIndexMessageToAllPeers(pieceIndex);
			
			ArrayList<String> notInterestingPeers = this.localPeerProcessInstance.getListOfUnInterestingPeers();
			messageHandler.broadcastNotInterestedToUnInterestingPeers(notInterestingPeers);
			
			/*
			 * { // TODO: Implement this properly! ArrayList<String>
			 * notInterestingPeers =
			 * this.localPeerProcessInstance.getListOfUnInterestingPeers();
			 * Message.broadcastNotInterestedToUnInterestingPeers(
			 * notInterestingPeers); }
			 */
			/*
			 * //TODO 1. check for complete file i. Make complete file ii. Check
			 * if all other peers are complete. iii. End all processes.
			 */

			if (!this.localPeerProcessInstance.getGotCompletedFile()) {
				int pieceToBeRequestedFromPeer = localPeerProcessInstance.getPieceToBeRequested(remotePeerID);
				if (pieceToBeRequestedFromPeer == -1) {
					// this peer is no longer interested in the remote peer
					// (does not any interesting pieces)
					messageHandler.sendMessage(Message.MESSAGETYPE_NOTINTERESTED, remotePeerID);
					return;
				} else {
					byte[] pieceIndexMessagePayload = Util.intToByteArray(pieceToBeRequestedFromPeer);
					messageHandler.sendMessage(Message.MESSAGETYPE_REQUEST, pieceIndexMessagePayload, remotePeerID);
					System.out.println("peer#: " + this.localPeerID + " sent a REQUEST message for piece# " + pieceIndex
							+ " to peer#" + this.remotePeerID);

					this.localPeerProcessInstance.updatePieceRequested(pieceToBeRequestedFromPeer);
				}
			}

		}

	}

	private boolean hasCompleteFile() {
		return localPeerProcessInstance.getGotCompletedFile();
	}

}
