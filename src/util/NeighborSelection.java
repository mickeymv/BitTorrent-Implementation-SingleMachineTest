package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import messages.Message;
import type.PeerInfo;
import util.ConfigurationSetup;
import util.Util;

public class NeighborSelection {
	
	static String peerID = null;
	
	ArrayList<PeerInfo> myPeers = Util.getMyPeerList(peerID);
	
	int p = ConfigurationSetup.getUnchokingInterval();
	
	int m = ConfigurationSetup.getOptimisticUnchokingInterval();
	
	public ArrayList<String> selectKPreferredNeighbors(ArrayList<String> myPeers, int k){
		
		ArrayList<String> preferredKNeighbors = new ArrayList<>();
		
		ArrayList<String> myPeerList = myPeers;
		
		//choosing k random neighbors from myPeers List for a Peer initially
		if(preferredKNeighbors == null){

		Random randomizer = new Random();
		for(int i = 0; i <= k; i++){
			String random = myPeers.get(randomizer.nextInt(myPeers.size()));
			preferredKNeighbors.add(random);
			myPeers.remove(random);
		}
		
		return preferredKNeighbors;
		}
		
		HashMap<String, Integer> peerToPiecesMap = new HashMap<>();
		
		for(int i = 0; i <= k; i++){
//			if(myPeers.get(i).getMessageType() == Message.MESSAGETYPE_PIECE){
				if(peerToPiecesMap.containsKey(myPeers.get(i))){
					int pieces = peerToPiecesMap.get(myPeers.get(i))+1;
					peerToPiecesMap.put(myPeers.get(i), pieces);
				}
				else{
					peerToPiecesMap.put(myPeers.get(i), 1);
				}
			}
//		}
		
		sortByValue(peerToPiecesMap);
		
		for (Map.Entry<String, Integer> entry : peerToPiecesMap.entrySet()) {
		  if (peerToPiecesMap.size() > k) break;
		  preferredKNeighbors.add(entry.getKey());
		}
				
		return preferredKNeighbors;	
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
	    return map.entrySet()
	              .stream()
	              .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
	              .collect(Collectors.toMap(
	                Map.Entry::getKey, 
	                Map.Entry::getValue, 
	                (e1, e2) -> e1, 
	                LinkedHashMap::new
	              ));
	}

}
