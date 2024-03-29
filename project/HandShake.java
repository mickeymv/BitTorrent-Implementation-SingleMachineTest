
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import connection.TCPConnectionManager;
import listener.MessageListener;
import peer.PeerProcess;
import util.Util;

/**
 * @author Mickey Vellukunnel Establish handshake and exchange BitFields.
 */
public class HandShake {

	public HandShake(TCPConnectionManager connManager) {
		 this.connManager = connManager;
	}
	
	private static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";

	private static Util utilInstance = Util.initializeUtil();

	TCPConnectionManager connManager;
	
	/**
	 * This is called by the client socket. Sends handshake to server and waits
	 * for one back.
	 * 
	 * @param localClientPeerID
	 * @param remoteServerPeerID
	 */
	
	public  void establishClientHandShakeTwoWayStream(String localClientPeerID, String remoteServerPeerID) {
		try {
			byte handshakeMessageBytes[] = getHandShakeBytes(localClientPeerID);

			// System.out.println("The handshake message from client: " +
			// localClientPeerID + ", to server: "+remoteServerPeerID+", is:
			// "+new String(handshakeMessageBytes));

			byte[] peerIDBytes = Arrays.copyOfRange(handshakeMessageBytes, 28, handshakeMessageBytes.length);

			// System.out.println("The peerID of local client is: " + new
			// String(peerIDBytes));

			// send HandShake to the listening server
			new Message(localClientPeerID, remoteServerPeerID, connManager.localPeerProcessInstance).sendMessage(handshakeMessageBytes);

			int messageLength;
			byte[] messageBytes = null; // message received back from the server

			/* Blocking call! */
			// receive the HandShake message sent back from the server
			messageLength = TCPConnectionManager.getDataInputStream(localClientPeerID, remoteServerPeerID).readInt();

			// System.out.println("The message length received from server is: "
			// + messageLength);

			if (messageLength == 32) {
				messageBytes = new byte[messageLength];
				TCPConnectionManager.getDataInputStream(localClientPeerID, remoteServerPeerID).readFully(messageBytes, 0,
						messageBytes.length);

				int receivedHandShakeFromPeer = utilInstance
						.intFromByteArray(Arrays.copyOfRange(messageBytes, 28, messageBytes.length));
				// make sure handshake is from expected peer
				if (receivedHandShakeFromPeer != Integer.parseInt(remoteServerPeerID)) {
					System.err.println("\nERROR! " + localClientPeerID + " local client Received handshake from Peer "
							+ receivedHandShakeFromPeer + "instead of from server " + remoteServerPeerID);
				} else {

					System.out.println(localClientPeerID + " local client Received handshake: "
							+ new String(messageBytes) + " from server " + remoteServerPeerID);
				}

			} else {
				System.err.println("\nERROR!" + localClientPeerID
						+ " local client Received something other than a handshake from server " + remoteServerPeerID);
			}
		} catch (IOException ioException) {
			System.out.println("Disconnect with Server after handshake " + remoteServerPeerID);
		}
	}

	private  byte[] getHandShakeBytes(String fromPeerID) {
		byte[] handshakeHeaderbyteArray = HANDSHAKE_HEADER.getBytes(); // 18
																		// bytes
		byte[] tenByteZeroBits = new byte[10]; // 10 bytes
		byte[] peerIDintBytes = utilInstance.intToByteArray(Integer.parseInt(fromPeerID)); // 4
																							// byte
																							// integer
																							// representation
																							// of
																							// the
																							// peerID

		ByteArrayOutputStream streamToCombineByteArrays = new ByteArrayOutputStream();
		try {
			streamToCombineByteArrays.write(handshakeHeaderbyteArray);
			streamToCombineByteArrays.write(tenByteZeroBits);
			streamToCombineByteArrays.write(peerIDintBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("The generated handShake message is: "+new
		// String(streamToCombineByteArrays.toByteArray()));
		return streamToCombineByteArrays.toByteArray();
	}

	/**
	 * This is called by the server socket. Waits for handshake from client and
	 * sends one back.
	 * 
	 * @param localServerPeerID
	 * @param remoteClientPeerID
	 */
	public  void establishServerHandShakeTwoWayStream(String localServerPeerID, String remoteClientPeerID) {
		try {

			int messageLength;
			byte[] messageBytes = null;

			/* Blocking call! */
			// receive the HandShake message sent from the client
			messageLength = TCPConnectionManager.getDataInputStream(localServerPeerID, remoteClientPeerID).readInt();

			// System.out.println("The message length received from client is: "
			// + messageLength);

			if (messageLength == 32) {

				messageBytes = new byte[messageLength];
				TCPConnectionManager.getDataInputStream(localServerPeerID, remoteClientPeerID).readFully(messageBytes, 0,
						messageBytes.length); // read

				// System.out.println("Inside server: "+localServerPeerID+",
				// after receiving HandShake: "+new String(messageBytes)+" from
				// client "+remoteClientPeerID);

				byte[] peerIDBytes = Arrays.copyOfRange(messageBytes, 28, messageBytes.length);

				// System.out.println("The peerID of client is: " + new
				// String(peerIDBytes));

				int receivedHandShakeFromPeer = utilInstance.intFromByteArray(peerIDBytes);

				// make sure handshake is from expected peer
				if (receivedHandShakeFromPeer != Integer.parseInt(remoteClientPeerID)) {
					System.err.println("\nERROR! " + localServerPeerID + " local Server Received handshake from Peer "
							+ receivedHandShakeFromPeer + "instead of from client " + remoteClientPeerID);
				} else {

					System.out.println(localServerPeerID + " local Server Received handshake: "
							+ new String(messageBytes) + " from client " + remoteClientPeerID);
				}
			} else {
				System.err.println("\nERROR! " + localServerPeerID
						+ " server received something other than expected handshake from client " + remoteClientPeerID);
			}

			byte handshakeMessageBytes[] = getHandShakeBytes(localServerPeerID);

			// send HandShake back to the listening client
			new Message(localServerPeerID, remoteClientPeerID, connManager.localPeerProcessInstance).sendMessage(handshakeMessageBytes);

		} catch (IOException ioException) {
			System.out.println("Disconnect with client after handshake " + remoteClientPeerID);
		}

	}
}
