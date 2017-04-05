package messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import connection.TCPConnectionManager;

/**
 * @author Mickey Vellukunnel
 *
 */
public class Message {

	private Message() {
	}
	
	public static final int MESSAGETYPE_CHOKE = 0;
	public static final int MESSAGETYPE_UNCHOKE = 1;
	public static final int MESSAGETYPE_INTERESTED = 2;
	public static final int MESSAGETYPE_NOTINTERESTED = 3;
	public static final int MESSAGETYPE_HAVE = 4;
	public static final int MESSAGETYPE_BITFIELD = 5;
	public static final int MESSAGETYPE_REQUEST = 6;
	public static final int MESSAGETYPE_PIECE = 7;

	/**
	 * 
	 * @param messageType, the type of message
	 * @param messagePayload, the payload
	 * @return the byte array to be sent across as the complete message, which is the messageType + Payload
	 */
	public static byte[] getMessage(int messageType, byte[] messagePayload) {
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
	public static byte[] getMessagePayload(byte[] message) {		
		return Arrays.copyOfRange(message, 1, message.length);
	}
	
	/**
	 * 
	 * @param message, the received byte array
	 * @return, the message type
	 */
	public static int getMessageType(byte[] message) {		
		return message[0];
	}
	
	/**
	 * 
	 * @param messageType, the type of message to sent
	 * @param messagePayload, the required payload
	 * @param toPeerID, which peer to sent it to
	 */
	public static void sendMessage(int messageType, byte[] messagePayload, String toPeerID) {
		ByteArrayOutputStream streamToCombineByteArrays = new ByteArrayOutputStream();
		try {
			streamToCombineByteArrays.write((byte)messageType);
			streamToCombineByteArrays.write(messagePayload);
			byte[] message = streamToCombineByteArrays.toByteArray();
			DataOutputStream out = TCPConnectionManager.getDataOutputStream(toPeerID);
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
	public static void sendMessage(int messageType, String toPeerID) {
		try {
			DataOutputStream out = TCPConnectionManager.getDataOutputStream(toPeerID);
			out.writeInt(1);
			out.write((byte)messageType);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// send a message to the output stream
	public static void sendMessage(byte[] msg, String fromPeerID, String toPeerID) {
		try {
			DataOutputStream out = TCPConnectionManager.getDataOutputStream(toPeerID);
			out.writeInt(msg.length);
			//System.out.println("The handshake message length from "+fromPeerID+" to "+toPeerID+" is "+msg.length);
			out.write(msg);
			out.flush();
			// System.out.println("Send message: " + new String(msg) + " from "
			// + fromPeerID + " to " + toPeerID);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
