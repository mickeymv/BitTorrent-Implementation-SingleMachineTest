package util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.Integer;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import type.PeerInfo;

/**
 * 1. read config files - common.cfg, peerinfo.cfg 
 * 2. store relevant common
 * variables and peer information
 * 
 * @author Mickey Vellukunnel, Arpitha, Xiaolong
 *
 *         content of common.cfg:
 *
 *         NumberOfPreferredNeighbors 2 UnchokingInterval 5
 *         OptimisticUnchokingInterval 15 FileName TheFile.dat FileSize 10000232
 *         PieceSize 32768
 *
 *
 *         content of peerInfo.cfg
 *
 *         [peer ID] [host name] [listening port] [has file or not] 1001
 *         lin114-00.cise.ufl.edu 6008 1 1002 lin114-01.cise.ufl.edu 6008 0 1003
 *         lin114-02.cise.ufl.edu 6008 0 1004 lin114-03.cise.ufl.edu 6008 0 1005
 *         lin114-04.cise.ufl.edu 6008 0 1006 lin114-05.cise.ufl.edu 6008 0
 *
 */
public class ConfigurationSetup {
	
	// from common.cfg
	 public static final int NULL = -1;
	 private static int numberOfPreferredNeighbors = NULL; 
	 private static int unchokingInterval = NULL; 
	 private static int optimisticUnchokingInterval = NULL; 
	 
	 public static String fileName = null; 
	 private static int fileSize = NULL; 
	 
	 private static int pieceSize = NULL;
	  
	 static int numberOfPieces = NULL;
	 
	 public static int getNumberOfPieces() {
		return numberOfPieces;
	}

	public static void setNumberOfPieces(int numberOfPieces) {
		ConfigurationSetup.numberOfPieces = numberOfPieces;
	}

	public int getNumberOfPreferredNeighbors(){
		 
		 return numberOfPreferredNeighbors; 
	 }
	  
	 public static int getUnchokingInterval(){ return unchokingInterval; }
	  
	 public static int getOptimisticUnchokingInterval() { 
		 return optimisticUnchokingInterval; 
	 }
	  
	 public String getFileName(){ return fileName; }
	  
	 public int getFileSize(){ return fileSize; }
	  
	 public int getPieceSize(){ return pieceSize; }
	 
	// from peerInfo.cfg

	// list of all the peers in the network (including local peer)
	private final static ArrayList<PeerInfo> peerList = new ArrayList<PeerInfo>();

	private static final String peerInfoFile = "PeerInfo.cfg";
	private static final String commonInfoFile = "Common.cfg";

	// map of peerID to the peer's position in the peers arraylist
	private static Map<String, Integer> peerIDToPositionMap = new HashMap<>();

	public ArrayList<PeerInfo> getPeerList() {
		return peerList;
	}

	public Map<String, Integer> getPeerIDToPositionMap() {
		return peerIDToPositionMap;
	}

	private static ConfigurationSetup instance = null;

	public static ConfigurationSetup getInstance() {
		if (instance == null) {
			instance = new ConfigurationSetup();
			readCommonInfoConfigFile();
		}
		return instance;
	}

	private ConfigurationSetup() {
		// reading from common.cfg
		readCommonInfoConfigFile();
		// reading from peerInfo.cfg
		readPeerInfoConfigFile();
	}

	/**
	 * Read Common.cfg
	 * TODO: Do this with the correct file format.
	 */
	private static void readCommonInfoConfigFile() {
		// reading from common.cfg
		try {
			
			 Scanner scanner = new Scanner(new
					 FileReader(commonInfoFile));
			 
			 numberOfPreferredNeighbors = Integer.parseInt(scanner.nextLine().trim());
			 unchokingInterval = Integer.parseInt(scanner.nextLine().trim());
			 optimisticUnchokingInterval = Integer.parseInt(scanner.nextLine().trim()); 
			 fileName = scanner.nextLine().trim(); 
			 fileSize = Integer.parseInt(scanner.nextLine().trim()); 
			 pieceSize = Integer.parseInt(scanner.nextLine().trim());
			  
			 if (fileSize % pieceSize == 0) { 
				 numberOfPieces = fileSize / pieceSize; 
			 } else {
				 numberOfPieces = fileSize / pieceSize + 1; 
			 }
			 scanner.close();
		} catch (Exception e) { // FileNotFoundException
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static final void readPeerInfoConfigFile() {
		int count = 0;
		try {
			// reading from peerInfo.cfg

			// read peer info
			Scanner peerScanner = new Scanner(new FileReader(peerInfoFile));

			while (peerScanner.hasNextLine()) {
				PeerInfo peerInfo = new PeerInfo();
				String string = peerScanner.nextLine();
				String[] splitBySpace = string.split(" ");
				peerInfo.setPeerID(splitBySpace[0].trim());
				peerInfo.setHostName(splitBySpace[1].trim());
				peerInfo.setPortNumber(Integer.parseInt(splitBySpace[2].trim()));
				if (splitBySpace[3].trim().equals("1")) {
					peerInfo.setHasFileInitially(true);
				} else {
					peerInfo.setHasFileInitially(false);
				}
				peerList.add(peerInfo);
				peerIDToPositionMap.put(peerInfo.getPeerID(), count++);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}