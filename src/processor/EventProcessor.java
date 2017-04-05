package processor;

import messages.Message;

/**
 * @author Mickey Vellukunnel
 */

public class EventProcessor {
	
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
			// do nothing
			break;
			
		
		case Message.MESSAGETYPE_UNCHOKE:
			/* 
			 * if local peer has complete file, do nothing.
			 * else, send "request" to B, if B has pieces 
			 * that local peer doesn't have and hasen't requested.
			*/
			
			break;
			
		case Message.MESSAGETYPE_INTERESTED:
			/*
			 * if B is choked, add B into interested_peer_list if B is not there.
			 * else B is unchoked, 
			 * 		check "sent_have_map" 1). if B is there, send piece x from the
			 * 							 map to B, and update bitfield for B
			 * 							2). if B is not there, add B into interesetd_peer_list
			 */
		case Message.MESSAGETYPE_NOTINTERESTED:
			/*
			 * update interested peers list.
			 */
			break;
		case Message.MESSAGETYPE_HAVE:
			/*
			 * Check if x is what we don't have and not requested yet.
			 * 		1). if so, send "interested" to B
			 * 		2). else, send "not interested" to B.
			 * Update bitfield of B.
			 */
			
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
		
		/*
		 * re-select k preferred neighbors
		 * Send unchoke message to B if B was choked before.
		 * send choke message to B if B was unchoked.
		 */
	}
	
	public void optimiticallyUnchokedNeighborUpdated() {
		
		/*
		 * re-select one optimistically unchoked neighbor
		 * send choked to previous "optimistically unchoked neighbor" if it is not preferred neighbor now.
		 * send unchoke to the new "optimistically unchoked neighbor".
		 */
	}
	
}






