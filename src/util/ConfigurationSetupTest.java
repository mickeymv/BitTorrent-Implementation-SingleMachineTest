package util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigurationSetupTest {
	
	@Test
	public void test() {
//		fail("Not yet implemented");
		
		ConfigurationSetup config = ConfigurationSetup.getInstance();
		System.out.println(config.getFileName() + " \n"
				+ config.getFileSize() + " \n"
				+ config.getNumberOfPreferredNeighbors() + " \n"
				+ config.getOptimisticUnchokingInterval()  + " \n"
				+ config.getPieceSize() + " \n"
				+ config.getUnchokingInterval() + " \n"
				+ config.getPeerIDToPositionMap() + " \n"
				+ config.getPeerList());
		Util.createRandomDataFile(config.getFileSize());
	}

}
