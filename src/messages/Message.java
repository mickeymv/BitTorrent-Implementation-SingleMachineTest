package messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import connection.TCPConnectionManager;
import peer.PeerProcess;
import util.Util;

/**
 * @author Mickey Vellukunnel
 *
 */
public class Message {
	
	public static final int MESSAGETYPE_CHOKE = 0;
	public static final int MESSAGETYPE_UNCHOKE = 1;
	public static final int MESSAGETYPE_INTERESTED = 2;
	public static final int MESSAGETYPE_NOTINTERESTED = 3;
	public static final int MESSAGETYPE_HAVE = 4;
	public static final int MESSAGETYPE_BITFIELD = 5;
	public static final int MESSAGETYPE_REQUEST = 6;
	public static final int MESSAGETYPE_PIECE = 7;
	
	private String localPeerID, remotePeerID;
	private PeerProcess localPeerProcessInstance = null;
	DataOutputStream out;


	public Message(String localPeerID, String remotePeerID, PeerProcess localPeerProcessInstance) {
		this.localPeerID = localPeerID;
		this.remotePeerID = remotePeerID;
		this.localPeerProcessInstance = localPeerProcessInstance;
		//System.err.println("MessageConstructor for localPeer#" +localPeerID);
		out = TCPConnectionManager.getDataOutputStream(localPeerID, remotePeerID);
	}

	public Message() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param messageType, the type of message
	 * @param messagePayload, the payload
	 * @return the byte array to be sent across as the complete message, which is the messageType + Payload
	 */
	public byte[] getMessage(int messageType, byte[] messagePayload) {
		ByteArrayOutputStream streamToCombineByteArrays = new ByteArrayOutputStream();
		try {
			streamToCombineByteArrays.write((byte)messageType);
			streamToCombineByteArrays.write(messagePayload);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return streamToCombineByteArrays.toByteArray();
	}
	
	/**
	 * 
	 * @param message, the received byte array
	 * @return, the byte array payload
	 */
	public byte[] getMessagePayload(byte[] message) {		
		return Arrays.copyOfRange(message, 1, message.length);
	}
	
	/**
	 * 
	 * @param message, the received byte array
	 * @return, the message type
	 */
	public  int getMessageType(byte[] message) {		
		return message[0];
	}
	
	/**
	 * 
	 * @param messageType, the type of message to sent
	 * @param messagePayload, the required payload
	 * @param toPeerID, which peer to sent it to
	 */
	public  void sendMessage(int messageType, byte[] messagePayload, String toPeerID) {
		ByteArrayOutputStream streamToCombineByteArrays = new ByteArrayOutputStream();
		try {
			streamToCombineByteArrays.write((byte)messageType);
			streamToCombineByteArrays.write(messagePayload);
			byte[] message = streamToCombineByteArrays.toByteArray();
			out.writeInt(message.length);
			out.write(message);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * For messages without a payload.
	 * 
	 * @param messageType, the type of message to sent
	 * @param toPeerID, which peer to sent it to
	 */
	public  void sendMessage(int messageType, String toPeerID) {
		try {
			out.writeInt(1);
			out.write((byte)messageType);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// send a message to the output stream
	public  void sendMessage(byte[] msg, String fromPeerID, String toPeerID) {
		try {
			out.writeInt(msg.length);
			out.write(msg);
			out.flush();
			// System.out.println("Send message: " + new String(msg) + " from "
			// + fromPeerID + " to " + toPeerID);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	/**
	 * Broadcast to all peers of the local peer that this peer
	 * "has" the specified piece.
	 * @param pieceIndex
	 */
	public  void broadcastHavePieceIndexMessageToAllPeers(int pieceIndex) {
		// TODO Auto-generated method stub
	}

	/**
	 * Send "NOT_INTERESTED" message to the peers who the local peer
	 * is not interested in.
	 * @param notInterestingPeers
	 */
	public  void broadcastNotInterestedToUnInterestingPeers(ArrayList<String> notInterestingPeers) {
		// TODO Auto-generated method stub
		
	}

	public  void sendPieceMessage(int pieceIndex, byte[] pieceDataMessagePayload, String toRemotePeerID) {
		ByteArrayOutputStream streamToCombineByteArrays = new ByteArrayOutputStream();
		try {
			streamToCombineByteArrays.write((byte)Message.MESSAGETYPE_PIECE);
			byte[] pieceIndexMessagePayload = Util.intToByteArray(pieceIndex);
			streamToCombineByteArrays.write(pieceIndexMessagePayload);
			streamToCombineByteArrays.write(pieceDataMessagePayload);
			byte[] message = streamToCombineByteArrays.toByteArray();
			out.writeInt(message.length);
			out.write(message);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
