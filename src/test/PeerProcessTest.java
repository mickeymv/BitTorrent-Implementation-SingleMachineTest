package test;

import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import connection.TCPConnectionManager;
import logging.FileLogger;
import messages.Message;
import peer.PeerProcess;
import type.PeerInfo;
import util.ConfigurationSetup;
import util.Util;

/**
 * @author Mickey Vellukunnel
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
		assertTrue(5 == peerProcess.getNeighbors().size());	
	}

	/*
	@Test
	public void testUninterestedPeerList(){
		ArrayList<String> notInterestingPeers = peerProcess.getListOfUnInterestingPeers();
		for(int i = 0; i < notInterestingPeers.size();i++){
			System.out.println("Not interested peer list "+notInterestingPeers.get(i).toString());
		}		
		}
	/*
	@Test
	public void testUpdateInterestedPeerList(){
		for(int i = 0; i < peerProcess.getNeighbors().size(); i++){
		try {
			peerProcess.updateInterested_peer_list(peerProcess.getNeighbors().get(i).getPeerID(), Message.MESSAGETYPE_INTERESTED);
			peerProcess.updateInterested_peer_list(peerProcess.getNeighbors().get(i).getPeerID(), Message.MESSAGETYPE_NOTINTERESTED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
	*/


}
