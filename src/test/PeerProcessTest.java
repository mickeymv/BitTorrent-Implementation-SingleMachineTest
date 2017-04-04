package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import peer.PeerProcess;
import type.PeerInfo;

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
		PeerProcess.initiatePeerProcessForLocalHostTesting(peerIDList);
	}

}
