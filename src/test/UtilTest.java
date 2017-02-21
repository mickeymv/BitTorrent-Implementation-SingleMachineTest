package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import type.PeerInfo;
import util.Util;

public class UtilTest {

	Util testInstance = Util.getInstance();
	@Test
	public void test() {
		ArrayList<PeerInfo> peerList = testInstance.getPeerList();
		
		for (PeerInfo peer: peerList) {
			
			System.out.println(peer.getPeerID() + " \t" + peer.getHostName() 
			 		+ " \t" + peer.getPortNumber() + " " + peer.isHasFileInitially());
		}
	}
}
