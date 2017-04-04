package test;

import static org.junit.Assert.*;

import org.junit.Test;

import util.ConfigurationSetup;
import util.Util;

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
		Util inst = Util.getInstance();
		ConfigurationSetup instance = ConfigurationSetup.getInstance();
		
		Util.initiateBitfield();
		Util.createRandomDataFile(instance.getFileSize());
		Util.splitDataFile();
		
		
		//System.out.println(ConfigurationSetup.numberOfPieces);
		//Util.printBitfield();
		
		
	}

}
