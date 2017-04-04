package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;


public class MessagingTest {

	@Test
	public void test() {
		byte[] testMessagebyteArray = ("[I AM A MESSAGE]").getBytes();
		ByteArrayOutputStream streamToCombineByteArrays = new ByteArrayOutputStream();
		try {
			streamToCombineByteArrays.write((byte)5); //this could be any integer indicating a message type
			streamToCombineByteArrays.write(testMessagebyteArray);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int messageType = streamToCombineByteArrays.toByteArray()[0];
		
		System.out.println("The int is: " + messageType);
	}

}