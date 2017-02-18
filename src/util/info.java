package util;
/**
 * 1. read config files - common.cfg, peerinfo.cfg
 * 2. From common.cfg getPeerList, getPeerInfo, getPeerID, getPort
 * 3. amIFirst, amILast for returns boolean
 * 4. getMyPeers(myID)
 * @author Arpitha
 *
 * contents of common.cfg:
 *
 *  NumberOfPreferredNeighbors 2
    UnchokingInterval 5
    OptimisticUnchokingInterval 15
    FileName TheFile.dat
    FileSize 10000232
    PieceSize 32768
 *
 */
public class info {

    private final int numberOfPreferredNeighbors;
    private final int unchokingInterval;
    private final int optimisticUnchokingInterval;
    private final String fileName;
    private final int fileSize;
    private final int pieceSize;

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
            this.numPieces = this.fileSize/this.pieceSize;
        } else {
            this.numPieces = this.fileSize/this.pieceSize + 1;
        }

        scanner.close();
    }


}
