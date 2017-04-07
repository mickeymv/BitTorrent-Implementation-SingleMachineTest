package peer;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import connection.TCPConnectionManager;
import logging.FileLogger;
import peer.PeerProcess;
import type.PeerInfo;
import util.ConfigurationSetup;
import util.Util;

/**
 * @author Xiaolong Li
 */

public class PeerProcessTest {
	
	PeerProcess peerProcess = null;
	@Before
	public void pretest() {
		
		Util inst = Util.initializeUtil();
		ConfigurationSetup instance = ConfigurationSetup.getInstance();
		System.out.println("peer list size: " + instance.getPeerList().size());
		peerProcess = new PeerProcess("2");
	}
	
	
	@Test
	public void test() {
		
		System.out.println("local peer id: " + peerProcess.getLocalPeerID());
		
		// print the size of neighbors
		System.out.println("size of neighbors: " 
				+ peerProcess.getNeighbors().size());
		for (PeerInfo peer : peerProcess.getNeighbors()) {
			
			System.out.println(peer.getPeerID());
		}
		assertTrue(1 == peerProcess.getNeighbors().size());
		
		
		
		
	}


}