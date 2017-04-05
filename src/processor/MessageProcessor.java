package processor;

/**
 * @author Mickey Vellukunnel
 */

public class MessageProcessor {

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
	}
}
