package bootstrap;

import java.io.IOException;
import java.util.ArrayList;

import type.PeerInfo;

import util.Util;

/**
 *
 *	Run the Java program, StartRemotePeers, at the working directory as follows: > java StartRemotePeers

 *	The program reads file PeerInfo.cfg and starts peers specified in the file one by one. 
 *		> java peerProcess 1001
 *	It terminates after starting all peers.
 *
 */

public class StartRemotePeers {
		
		private static final String peerProcessName = "java PeerProcess";
		
		public static void main() {
			String workingDir = System.getProperty("user.dir");
			
			String peerProcessArguments = "1000"; // should be string of the peerProcessID //peer.getPeerID()
			String hostname = "xiaolong@thunder.cise.ufl.edu"; //should be given by util method //lin114-00.cise.ufl.edu
			//peer.getHostName()
			
			ArrayList<PeerInfo> peerList = Util.getInstance().getListOfPeers();
			
			try {
				for(PeerInfo peer:peerList) {
					Runtime.getRuntime().exec("ssh " + peer.getHostName() + " cd " + workingDir + " ; " +
							peerProcessName + " " + peer.getPeerID() );
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
