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
	PeerProcess localPeerProcessInstance = null;
	String localPeerID, remotePeerID;

	@Before
	public void preTest(String localPeerID, String remotePeerID) {
		ConfigurationSetup instance = ConfigurationSetup.getInstance();
		Util testInstance = Util.initializeUtil();		
		this.localPeerID = localPeerID;
		this.remotePeerID = remotePeerID;
		localPeerProcessInstance = PeerProcesses.peerProcesses.get(localPeerID);
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

	@Test
	public void testUninterestedPeerList(){
		test();
		ArrayList<String> notInterestingPeers = this.localPeerProcessInstance.getListOfUnInterestingPeers();
		for(int i = 0; i < notInterestingPeers.size();i++){
			System.out.println(notInterestingPeers.get(i).toString());
		}
		}
	@Test
	public void testUpdateInterestedPeerList(){
		test();
		try {
			this.localPeerProcessInstance.updateInterested_peer_list(remotePeerID, Message.MESSAGETYPE_INTERESTED);
			this.localPeerProcessInstance.updateInterested_peer_list(remotePeerID, Message.MESSAGETYPE_NOTINTERESTED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}


}
