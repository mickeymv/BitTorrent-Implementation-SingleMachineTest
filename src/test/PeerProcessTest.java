package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import connection.TCPConnectionManager;
import logging.FileLogger;
import messages.Message;
import peer.PeerProcess;
import type.PeerInfo;
import util.Util;

/**
 * @author Mickey Vellukunnel
 */

public class PeerProcessTest {

	private PeerProcess localPeerProcessInstance = null;
	private String localPeerID, remotePeerID;

	@Before
	public void preTest(String localPeerID, String remotePeerID) {
		this.localPeerID = localPeerID;
		this.remotePeerID = remotePeerID;
		localPeerProcessInstance = PeerProcesses.peerProcesses.get(localPeerID);
	}
	@Test
	public void test() {
		ArrayList<String> peerIDList = new ArrayList<>();
		peerIDList.add("1");
		peerIDList.add("2");
		//peerIDList.add("3");
		//peerIDList.add("4");
//		peerIDList.add("5");
//		peerIDList.add("6");
		PeerProcesses.initiatePeerProcessForLocalHostTesting(peerIDList);
	}
	
	public void testUninterestedPeerList(){
		test();
		ArrayList<String> notInterestingPeers = this.localPeerProcessInstance.getListOfUnInterestingPeers();
		for(int i = 0; i < notInterestingPeers.size();i++){
			System.out.println(notInterestingPeers.get(i).toString());
		}
		}
	
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
