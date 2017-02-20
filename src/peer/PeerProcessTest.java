package peer;

import static org.junit.Assert.*;

import org.junit.Test;

import type.PeerInfo;

public class PeerProcessTest {

	PeerInfo localPeerInfo = PeerProcess.getLocalPeerInstance();
	
	@Test
	public void test() {
		
		PeerProcess.initiatePeerProcessForTesting("1");
		
	}

}
