package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import connection.TCPConnectionManager;
import logging.FileLogger;
import peer.PeerProcess;
import type.PeerInfo;
import util.Util;

/**
 * @author Mickey Vellukunnel
 */

public class PeerProcessTest {

	@Test
	public void test() {
		ArrayList<String> peerIDList = new ArrayList<>();
		peerIDList.add("1");
		peerIDList.add("2");
		peerIDList.add("3");
		peerIDList.add("4");
//		peerIDList.add("5");
//		peerIDList.add("6");
		initiatePeerProcessForLocalHostTesting(peerIDList);
	}
	
	public void initiatePeerProcessForLocalHostTesting(ArrayList<String> peerIDList) {
		ArrayList<PeerProcess> peerProcesses = new ArrayList<>();
		for (String peerID : peerIDList) {
			PeerProcess localPeer = new PeerProcess(peerID);
			peerProcesses.add(localPeer);
			// start logging
			//FileLogger.initialize(peerID);
			localPeer.initiatePeerProcess();
		}
		
		try {
			Thread.sleep(20000); // give enough time before the main program
									// exits for the different threads to finish
									// execution (check to see if the various
									// ports/streams have the data or not)
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
