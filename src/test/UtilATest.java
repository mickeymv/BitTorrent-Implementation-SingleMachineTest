package test;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import type.PeerInfo;
import util.ConfigurationSetup;
import util.Util;

public class UtilATest {
	ConfigurationSetup instance = ConfigurationSetup.getInstance();
	Util testInstance = Util.getInstance();
	
	
	@Test
	public void testMoveUtil() throws IOException{
		String peerID = "60016";
		Util.move(peerID, ConfigurationSetup.fileName);
	}
	
	@Test
	public void test() {
		ArrayList<PeerInfo> peerList = testInstance.getPeerList();
		
		for (PeerInfo peer: peerList) {
			
			System.out.println(peer.getPeerID() + " \t" + peer.getHostName() 
			 		+ " \t" + peer.getPortNumber() + " " + peer.isHasFileInitially());
		}
	}	
}
