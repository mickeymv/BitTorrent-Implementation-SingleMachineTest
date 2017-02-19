package util;

import java.util.ArrayList;

import type.PeerInfo;

public class Util {
	
	private static Util instance = null;

	private Util() {
		
	}
	
	public ArrayList<PeerInfo> getPreviousPeer(String peerID) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ArrayList<PeerInfo> getListOfPeers() {
		// TODO Auto-generated method stub
		return null;
	}

	public PeerInfo getPeerInfo(String peerID) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Util getInstance() {
		if (instance == null) {
			instance = new Util();
		}
		return instance;
	}

	public boolean isLastPeer(String iD) {
		// TODO Auto-generated method stub
		return false;
	}



}
