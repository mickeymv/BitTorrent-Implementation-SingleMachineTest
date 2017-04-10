package peer;

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

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
		
		interested_peer_list.put("1", false);
		interested_peer_list.put("3", true);
		interested_peer_list.put("4", false);
		interested_peer_list.put("5", true);
		interested_peer_list.put("6", true);
		peerProcess.setInterested_peer_list(interested_peer_list);
		HashMap<String, Integer> download_speed = peerProcess.getDownload_speed();
		download_speed.put("1", 10);
		download_speed.put("3", 1);
		download_speed.put("4", 2);
		download_speed.put("5", 3);
		download_speed.put("6", 1);
		peerProcess.setDownload_speed(download_speed);
	}
	
	
	@Test
	public void test_updatePreferredNeighbors() {
		
		System.out.println("local peer id: " + peerProcess.getLocalPeerID());
		
		// print the size of neighbors
		System.out.println("size of neighbors: "
				+ peerProcess.getNeighbors().size());
		for (PeerInfo peer : peerProcess.getNeighbors()) {
			
			System.out.println(peer.getPeerID());
		}
		System.out.println(peerProcess.getDownload_speed().get("5"));
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
	
	@Test
	public void test_start_p_timer() {
		
		
	}
	
	@Test
	public void test_start_m_timer() {
		
		
	}
	
}