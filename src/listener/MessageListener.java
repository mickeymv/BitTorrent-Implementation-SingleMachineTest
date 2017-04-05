package listener;

import java.io.DataInputStream;
import java.io.IOException;

import connection.TCPConnectionManager;
import processor.EventProcessor;

/**
 * @author Mickey Vellukunnel
 *
 *	Every peer will have a message listener for every other peer it is connected to.
 *	It will also have an instance of MessageProcessor for every peer it is connected to.
 *
 */

public class MessageListener {
	private String localPeerID, remotePeerID;
	private DataInputStream in;
	EventProcessor messageProcessor;
	private boolean listeningSocketOpen = true;

	public MessageListener(String localPeerID, String remotePeerID, DataInputStream in) {
		this.localPeerID = localPeerID;
		this.remotePeerID = remotePeerID;
		this.in = in;
		messageProcessor = new EventProcessor();
	}

	/**
	 * Start listening for incoming messages.
	 */
	public void startListening() {
		while (listeningSocketOpen) {
			int messageLength;
			byte[] messageBytes = null;

			// receive the message sent from the peer
			try {
				messageLength = in.readInt();

				/*
				 * An actual message consists of; 1. 4-byte message length field
				 * (It does not include the length of the message length field
				 * itself.), [We are not using it in our implementation since we
				 * can read message length for DataInputStreams] 2. 1-byte
				 * message type field, and a 3. message payload with variable
				 * size. (optional)
				 */

				if (messageLength >= 1) {
					in.readFully(messageBytes, 0, messageBytes.length); // read
					messageProcessor.processMessage(messageBytes);
				} else {
					System.out.println("ERROR! " + localPeerID
							+ " local peer Received message of incorrect length from Peer " + remotePeerID);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void stopListening() {
		listeningSocketOpen = false;
	}

}
