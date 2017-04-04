package util;

import java.util.ArrayList;
import java.util.Random;

public class NeighborSelection {
	
	
	
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
		
		for(int i = 0; i <= k; i++){
			
		}
				
		return preferredKNeighbors;	
	}

}
