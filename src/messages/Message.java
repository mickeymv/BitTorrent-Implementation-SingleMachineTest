package messages;

import java.io.DataOutputStream;
import java.io.IOException;

import connection.TCPConnectionManager;

/**
 * @author Mickey Vellukunnel
 *
 */
public class Message {

	private Message() {

	}

	// send a message to the output stream
	public static void sendMessage(byte[] msg, String fromPeerID, String toPeerID) {
		try {
			DataOutputStream out = TCPConnectionManager.getDataOutputStream(toPeerID);
			out.writeInt(msg.length);
			out.write(msg);
			out.flush();
			// System.out.println("Send message: " + new String(msg) + " from "
			// + fromPeerID + " to " + toPeerID);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
