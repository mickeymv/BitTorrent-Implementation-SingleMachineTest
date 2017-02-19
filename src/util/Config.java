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
 * 2. From common.cfg getPeerList, getPeerInfo, getPeerID, getPort
 * 3. amIFirst, amILast for returns boolean
 * 4. getMyPeers(myID)
 * @author Arpitha
 *
 * content of common.cfg:
 *
 *  NumberOfPreferredNeighbors 2
    UnchokingInterval 5
    OptimisticUnchokingInterval 15
    FileName TheFile.dat
    FileSize 10000232
    PieceSize 32768
 *
 *
 * content of peerInfo.cfg
 *
 * [peer ID] [host name] [listening port] [has file or not]
 *  1001 lin114-00.cise.ufl.edu 6008 1
    1002 lin114-01.cise.ufl.edu 6008 0
    1003 lin114-02.cise.ufl.edu 6008 0
    1004 lin114-03.cise.ufl.edu 6008 0
    1005 lin114-04.cise.ufl.edu 6008 0
    1006 lin114-05.cise.ufl.edu 6008 0
 *
 */
public class Config {

    //from common.cfg
	/*
    private final int numberOfPreferredNeighbors;
    private final int unchokingInterval;
    private final int optimisticUnchokingInterval;
    private final String fileName;
    private final int fileSize;
    private final int pieceSize;

    final int numberOfPieces;
    public int getNumberOfPreferredNeighbors(){
        return numberOfPreferredNeighbors;
    }

    public int getUnchokingInterval(){
        return unchokingInterval;
    }

    public int getOptimisticUnchokingInterval(){
        return optimisticUnchokingInterval;
    }

    public String getFileName(){
        return fileName;
    }

    public int getFileSize(){
        return fileSize;
    }

    public int getPieceSize(){
        return pieceSize;
    }
*/
    //from peerInfo.cfg
   
    private final ArrayList<PeerInfo> peerList;
    
    private static final String peerInfoFile = "PeerInfo.cfg";
    private static final String commonInfoFile = "Common.cfg";

    private final int numberOfPeers;
    
    private Map<String, Integer> peerIDToPositionMap = new HashMap<>();

    public ArrayList<PeerInfo> getPeerList(){
        return peerList;
    }
    
    public Map<String, Integer> getPeerIDToPositionMap() {
		return peerIDToPositionMap;
	}

	private static Config instance = null;
    
    public static Config getInstance() {
    		if(instance == null) {
    			instance = new Config();
    		}
    		return instance;
    }

    private Config(){

        //reading from common.cfg

        Scanner scanner;
        int count = 0;
        peerList = new ArrayList<PeerInfo>();

		try {
			/*
			scanner = new Scanner(new FileReader(commonInfoFile));
		

        this.numberOfPreferredNeighbors = Integer.parseInt(scanner.nextLine().trim());
        this.unchokingInterval = Integer.parseInt(scanner.nextLine().trim());
        this.optimisticUnchokingInterval = Integer.parseInt(scanner.nextLine().trim());
        this.fileName = scanner.nextLine().trim();
        this.fileSize = Integer.parseInt(scanner.nextLine().trim());
        this.pieceSize = Integer.parseInt(scanner.nextLine().trim());

        if (this.fileSize%this.pieceSize == 0) {
            this.numberOfPieces = this.fileSize/this.pieceSize;
        } else {
            this.numberOfPieces = this.fileSize/this.pieceSize + 1;
        }

        scanner.close();
*/
        // reading from peerInfo.cfg

        //read peer info
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
            peerIDToPositionMap.put(peerInfo.getPeerID(),count++);
        }} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        this.numberOfPeers = count;
    }
}