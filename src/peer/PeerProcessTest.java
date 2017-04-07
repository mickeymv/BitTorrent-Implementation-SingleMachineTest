package peer;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

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
	HashMap<String, Boolean> preferred_neighbors = null;
	@Before
	public void pretest() {
		
		Util inst = Util.initializeUtil();
		ConfigurationSetup instance = ConfigurationSetup.getInstance();
		System.out.println("peer list size: " + instance.getPeerList().size());
		peerProcess = new PeerProcess("2");
		preferred_neighbors = peerProcess.getPreferred_neighbors();
		HashMap<String, Boolean> interested_peer_list = peerProcess.getInterested_peer_list();
		interested_peer_list.put("3", true);
		interested_peer_list.put("4", true);
		interested_peer_list.put("5", true);
		interested_peer_list.put("6", true);
		
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
		//assertTrue(1 == peerProcess.getNeighbors().size());
		
		peerProcess.initializePreferredNeighbors();
		
		System.out.println("initialize preferred_neighbors: ");
		for (String peerID : preferred_neighbors.keySet()) {
			
			if (preferred_neighbors.get(peerID) == true) {
				System.out.println(peerID);
			}
		}
		
		System.out.println("update preferred_neighbors: ");
		try {
			peerProcess.updatePreferredNeighbors();
		} catch (Exception e) {
			System.err.println("there is not enough interested peers.");
			e.printStackTrace();
		}
		
		for (String peerID : preferred_neighbors.keySet()) {
			
			if (preferred_neighbors.get(peerID) == true) {
				System.out.println(peerID);
			}
		}
		
	}


}