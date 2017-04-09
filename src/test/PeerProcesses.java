package test;

import java.util.ArrayList;
import util.Util;

import java.util.HashMap;

import peer.PeerProcess;

public class PeerProcesses {
	
	private PeerProcesses(){		
	}
	
	
	/*
	 * TODO: For local testing, we have a map of peerIDs to their PeerProcesses.
	 * When running the final actual program on remote server, it will be only one PeerProcess running on 
	 * each single remote machine instance and everything in it will be static. 
	 */
	public static HashMap<String, PeerProcess> peerProcesses = new HashMap<>();
	
	
	public static void initiatePeerProcessForLocalHostTesting(ArrayList<String> peerIDList) {
		Util.createRandomDataFile(1024); //change according to what is in Common.cfg
		for (String peerID : peerIDList) {
			PeerProcess localPeer = new PeerProcess(peerID);
			peerProcesses.put(peerID, localPeer);
			// start logging
			//FileLogger.initialize(peerID);
			//System.out.println("Going to create connections for peer# "+peerID);
			localPeer.initiatePeerProcess();
			//System.out.println("Done creating connections for peer# "+peerID);
		}
		
		try {
			Thread.sleep(200000); // give enough time before the main program
									// exits for the different threads to finish
									// execution (check to see if the various
									// ports/streams have the data or not)
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
}