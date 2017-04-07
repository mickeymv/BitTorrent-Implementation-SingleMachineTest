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
import util.ConfigurationSetup;
import util.Util;

/**
 * @author Mickey Vellukunnel
 */

public class PeerProcessTest {

	private PeerProcess localPeerProcessInstance = null;
	private String localPeerID, remotePeerID;

	@Before
	public void preTest(String localPeerID, String remotePeerID) {
		ConfigurationSetup instance = ConfigurationSetup.getInstance();
		Util testInstance = Util.initializeUtil();		
		this.localPeerID = localPeerID;
		this.remotePeerID = remotePeerID;
		localPeerProcessInstance = PeerProcesses.peerProcesses.get(localPeerID);
	}
	
	public void testUninterestedPeerList(){
		ArrayList<String> notInterestingPeers = this.localPeerProcessInstance.getListOfUnInterestingPeers();
		for(int i = 0; i < notInterestingPeers.size();i++){
			System.out.println(notInterestingPeers.get(i).toString());
		}
		}
	
	public void testUpdateInterestedPeerList(){
		try {
			this.localPeerProcessInstance.updateInterested_peer_list(remotePeerID, Message.MESSAGETYPE_INTERESTED);
			this.localPeerProcessInstance.updateInterested_peer_list(remotePeerID, Message.MESSAGETYPE_NOTINTERESTED);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}


}
