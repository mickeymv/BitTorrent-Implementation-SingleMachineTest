package messages;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import connection.TCPConnectionManager;
import util.Util;

/**
 * @author Mickey Vellukunnel
 *
 */

public class HandShake {

	private HandShake() {
	}
	
	private static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";

	private static Util utilInstance = Util.getInstance();

	public static void establishClientHandShakeTwoWayStream(String localClientPeerID, String remoteServerPeerID) {
		try {
			byte handshakeMessageBytes[] = getHandShakeBytes(localClientPeerID);

			// send HandShake to the listening server
			Message.sendMessage(handshakeMessageBytes, localClientPeerID, remoteServerPeerID);

			int messageLength;
			byte[] messageBytes = null; // message received back from the server

			// receive the HandShake message sent back from the server
			messageLength = TCPConnectionManager.getDataInputStream(remoteServerPeerID).readInt();

			// System.out.println("The message length received from server is: "
			// + messageLength);

			if (messageLength == 22) { // TODO: should be 32 including the
										// headshakeHeader + zero bits + 4 byte
										// peer id representation
				messageBytes = new byte[messageLength];
				TCPConnectionManager.getDataInputStream(remoteServerPeerID).readFully(messageBytes, 0,
						messageBytes.length); // read
				// the
				// message
				// show the message to the user
				System.out.println(localClientPeerID + " local client Received handshake: " + new String(messageBytes)
						+ " from server " + remoteServerPeerID);

				// TODO: Make sure the handshake is from the expected peer by
				// cross-checking the peerID
			} else {
				System.out.println("ERROR!" + localClientPeerID
						+ " local client Received something other than a handshake from server " + remoteServerPeerID);
			}
		} catch (IOException ioException) {
			System.out.println("Disconnect with Server after handshake " + remoteServerPeerID);
		}
	}

	private static byte[] getHandShakeBytes(String fromPeerID) {
		byte[] handshakeHeaderbyteArray = ("[" + HANDSHAKE_HEADER + " " + fromPeerID + "]").getBytes(); //TODO: Supposed to be 18 bytes
		byte[] tenByteZeroBits = new byte[10]; //10 bytes
		byte[] peerIDintBytes = utilInstance.intToByteArray(Integer.parseInt(fromPeerID)); //TODO: should be a 4 byte integer representation of the peerID

		ByteArrayOutputStream streamToCombineByteArrays = new ByteArrayOutputStream();
		try {
			streamToCombineByteArrays.write(handshakeHeaderbyteArray);
			streamToCombineByteArrays.write(tenByteZeroBits);
			streamToCombineByteArrays.write(peerIDintBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return streamToCombineByteArrays.toByteArray();
		return handshakeHeaderbyteArray;
	}

	public static void establishServerHandShakeTwoWayStream(String localServerPeerID, String remoteClientPeerID) {
		try {

			int messageLength;
			byte[] messageBytes = null;

			// receive the HandShake message sent from the client
			messageLength = TCPConnectionManager.getDataInputStream(remoteClientPeerID).readInt();

			// System.out.println("The message length received from client is: "
			// + messageLength);

			if (messageLength == 22) { // TODO: should be 32 including the
										// headshakeHeader + zero bits + 4 byte
										// peer id representation
				messageBytes = new byte[messageLength];
				TCPConnectionManager.getDataInputStream(remoteClientPeerID).readFully(messageBytes, 0,
						messageBytes.length); // read
				// the
				// message
				// show the message to the user
				System.out.println(localServerPeerID + " Server Received handshake: " + new String(messageBytes)
						+ " from client " + remoteClientPeerID);

				// TODO: Make sure the handshake is from the expected peer by
				// cross-checking the peerID

			} else {
				System.out.println("ERROR! " + localServerPeerID
						+ " server received something other than expected handshake from client " + remoteClientPeerID);
			}

			byte handshakeMessageBytes[] = getHandShakeBytes(localServerPeerID);

			// send HandShake back to the listening client
			Message.sendMessage(handshakeMessageBytes, localServerPeerID, remoteClientPeerID);
		} catch (IOException ioException) {
			System.out.println("Disconnect with client after handshake " + remoteClientPeerID);
		}

	}
}
