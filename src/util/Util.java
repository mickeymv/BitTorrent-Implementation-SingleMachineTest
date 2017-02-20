package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import type.PeerInfo;
/*
* @author Mickey Vellukunnel, Arpitha
*
*/

public class Util {

	private static Util instance = null;

	private static ArrayList<PeerInfo> peerList;
	private static Map<String, Integer> peerIDToPositionMap;
	private static ConfigurationSetup configInstance = null;

	private Util() {
	}

	public static Util getInstance() {
		if (instance == null) {
			instance = new Util();
			configInstance = ConfigurationSetup.getInstance();
			peerIDToPositionMap = configInstance.getPeerIDToPositionMap();
			peerList = configInstance.getPeerList();
		}
		return instance;
	}

	public ArrayList<PeerInfo> getPeerList() {
		return peerList;
	}

	public ArrayList<PeerInfo> getMyPeerList(String peerID) {
		ArrayList<PeerInfo> myPeerList = new ArrayList<PeerInfo>();

		for (PeerInfo peerInfo : peerList) {
			if (peerInfo.getPeerID() != peerID) {
				myPeerList.add(peerInfo);
			}
		}
		return myPeerList;
	}

	public ArrayList<PeerInfo> getMyPreviousPeers(String peerID) {
		ArrayList<PeerInfo> myPreviousPeers = new ArrayList<PeerInfo>();
		int peerPosition = peerIDToPositionMap.get(peerID);
		for (int i1 = 0; i1 < peerPosition; i1++) {
			myPreviousPeers.add(peerList.get(i1));
		}
		return myPreviousPeers;
	}

	public boolean isFirstPeer(String peerID) {
		if (peerList.get(0).getPeerID() == peerID) {
			return true;
		}
		return false;
	}

	public boolean isLastPeer(String peerID) {
		if (peerList.get(peerList.size() - 1).getPeerID() == peerID) {
			return true;
		}
		return false;
	}

	public PeerInfo getPeerInfo(String peerID) {
		int peerPosition = peerIDToPositionMap.get(peerID);
		PeerInfo peerInfo = peerList.get(peerPosition);
		return peerInfo;
	}
}
