package test;

import java.util.ArrayList;

import org.junit.Test;

/**
 * @author Mickey Vellukunnel
 */

public class NetworkPeersSharingTest {

	@Test
	public void test() {
		ArrayList<String> peerIDList = new ArrayList<>();
		peerIDList.add("1");
		peerIDList.add("2");
		peerIDList.add("3");
		peerIDList.add("4");
		peerIDList.add("5");
		peerIDList.add("6");
		peerIDList.add("7");
		peerIDList.add("8");
		peerIDList.add("9");
		peerIDList.add("10");
		peerIDList.add("11");
		PeerProcesses.initiatePeerProcessForLocalHostTesting(peerIDList);
	}

}
