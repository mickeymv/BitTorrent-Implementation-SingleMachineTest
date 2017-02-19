package util;

import java.lang.Integer;
import java.lang.String;
import java.util.ArrayList;

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
public class info {

    //from common.cfg
    private final int numberOfPreferredNeighbors;
    private final int unchokingInterval;
    private final int optimisticUnchokingInterval;
    private final String fileName;
    private final int fileSize;
    private final int pieceSize;

    final int numberOfPieces;

    //from peerInfo.cfg
    private final ArrayList<Integer> peerID;
    private final ArrayList<String> hostName;
    private final ArrayList<Integer> ports;
    private final ArrayList<boolean> hasFile;

    private final int numberOfPeers;

    //common.cfg
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

    //peerInfo.cfg


    public Config(String commonConfig){

        //reading common.cfg file

        Scanner scanner = new Scanner(new FileReader(commonConfig));

        this.numberOfPreferredNeighbors = Integer.parseInt(in1.nextLine().trim());
        this.unchokingInterval = Integer.parseInt(in1.nextLine().trim());
        this.optimisticUnchokingInterval = Integer.parseInt(in1.nextLine().trim());
        this.fileName = in1.nextLine().trim();
        this.fileSize = Integer.parseInt(in1.nextLine().trim());
        this.pieceSize = Integer.parseInt(in1.nextLine().trim());

        if (this.fileSize%this.pieceSize == 0) {
            this.numberOfPieces = this.fileSize/this.pieceSize;
        } else {
            this.numberOfPieces = this.fileSize/this.pieceSize + 1;
        }

        scanner.close();
    }


}
