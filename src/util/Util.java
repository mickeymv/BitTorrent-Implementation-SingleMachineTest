package util;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.io.File;

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
	private static ArrayList<Byte> bitField = null;

	private Util() {
	}

	public static Util getInstance() {
		if (instance == null) {
			instance = new Util();
			configInstance = ConfigurationSetup.getInstance();
			peerIDToPositionMap = configInstance.getPeerIDToPositionMap();
			peerList = configInstance.getPeerList();
			initiateBitfield();
		}
		return instance;
	}
	
	/*
	 * Converts an integer to its 4 byte representation.
	 */
	public final byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
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

	/*
	 * Returns address of a peer (given PeerInfo object) in the format
	 * 'hostName:portNumber'
	 */
	public static String getPeerAddress(PeerInfo peer) {
		return peer.getHostName() + ":" + peer.getPortNumber();
	}
	
	
	/**
	 * Initialize bitfield according to the local file.
	 * @return
	 */
	public static void initiateBitfield() {
		
		// if there is no local file, set the bitmap to be null
		if (ConfigurationSetup.getInstance().getFileName() == null) {
			
			return;
		} else {
			bitField = new ArrayList<Byte>();
			
			int lengthOfBitfield = ConfigurationSetup.getNumberOfPieces() / 8;
			
			System.out.println("length of bitfield:" + lengthOfBitfield);
			// set all full bytes
			for (int i = 0; i < lengthOfBitfield; i ++) {
				
				Byte b = new Byte((byte)0xFF);
				bitField.add(b);
			}
			
			// set remaining bits of the last byte.
			int remaining = ConfigurationSetup.getNumberOfPieces() - (lengthOfBitfield) * 8;
			System.out.println("number of pieces:" + ConfigurationSetup.getNumberOfPieces() + " \n"
					+ "remaining:" + remaining);
			Byte b = setFirstNDigits(remaining);
			bitField.add(b);
		}
	}
	
	/**
	 * Set the first N digits of a Byte object
	 * @return
	 */
	public static Byte setFirstNDigits(int n) {
		
		byte b = 0;
		for (int i = 0; i < n; i ++) {
			b = (byte) (b>>1);
			b = (byte) (b | 0b10000000);
			System.out.println("b:" + b + '\t');
		}
		return new Byte(b);
	}
	
	/**
	 * Return the number of 1's in the Byte
	 * @param map
	 * @return
	 */
	public static int getFirstNDigits(Byte map) {
		
		for (int i = 0; i < 8; i ++) {
			
			if (map == 0) 
				return i;
			else {
				map = (byte) (map << 1);
			}
		}
		return 8;
	}
	
	/**
	 * Generate a random file with a specific size. 
	 * @param size
	 */
	public static void createRandomDataFile(int size) {
		
		  int bufferSize=1024;
		  byte[] buffer = new byte[bufferSize];
		  Random r=new Random();

		  int nbBytes=0;
		  DataOutputStream dos;
		try {
			dos = new DataOutputStream(new FileOutputStream(ConfigurationSetup.getInstance().getFileName()));
			while(nbBytes < size){
				
			    int nbBytesToWrite=Math.min(size-nbBytes,bufferSize);
			    byte[] bytes = new byte[nbBytesToWrite];
			    r.nextBytes(bytes);
				dos.write(bytes);
			    nbBytes += nbBytesToWrite;
			  }
			  dos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void printByteToBinaryString(Byte b) {
		
		String s1 = String.format("%8s", 
				Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
		System.out.println(s1);
	}

	
public static void move(String peerID, String fileName)throws IOException{
		
//		String fileName = "TheFile.dat";
		
		/*String fileName2 = "~/BitTorrent-Implementation/peer_"+peerID;
		
		Path source = Paths.get(fileName);
		Path target = Paths.get(fileName2);

		Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);*/
	
	try{

 	   File afile =new File("F:\\Semester_3\\Computer Networks\\BitTorrent-Implementation\\"+fileName);

 	   if(afile.renameTo(new File("F:\\Semester_3\\Computer Networks\\BitTorrent-Implementation\\peer\\peer_" + peerID))){
 		System.out.println("File is moved successful!");
 	   }else{
 		System.out.println("File is failed to move!");
 	   }

 	}catch(Exception e){
 		e.printStackTrace();
 	}
	
	}
	public static void printBitfield() {
		
		for (Byte b : bitField) {
			
			printByteToBinaryString(b);
		}
	}
}









