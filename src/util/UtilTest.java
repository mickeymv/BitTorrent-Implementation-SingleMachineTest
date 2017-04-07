package util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class UtilTest {

	@Test
	public void test() {
//		fail("Not yet implemented");
		
//		Byte b = Util.setFirstNDigits(3);
//		String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
//		System.out.println(s1);
//		
//		int n = Util.getFirstNDigits(new Byte((byte)0XF8));
//		System.out.println(n);
		
		Util inst = Util.initializeUtil();
		ConfigurationSetup instance = ConfigurationSetup.getInstance();
		
		//Util.initiateBitfield();
		//Util.createRandomDataFile(instance.getFileSize());
		//Util.splitDataFile();
		//Util.mergeDataPieces("project/");
		
		
		//System.out.println(ConfigurationSetup.numberOfPieces);
		//Util.printBitfield();
		byte[] data = Util.getPieceAsByteArray(1);
		for (byte b : data) {
			
			Util.printByteToBinaryString(b);
		}		
		
	}

}
