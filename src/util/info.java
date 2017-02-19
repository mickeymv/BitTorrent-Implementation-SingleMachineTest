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

    //from peerInfo.cfg
    private final ArrayList<Integer> peerList;
    private final ArrayList<Integer> myPeerList;
    private final ArrayList<Integer> myPreviousPeers;
    private final ArrayList<String> hostName;
    private final ArrayList<Integer> ports;
    private final ArrayList<boolean> hasFile;
    private final boolean amIFirst;
    private final boolean amILast;

    private final int numberOfPeers;

    public ArrayList<Integer> getPeerList(){
        return peerList;
    }

    public ArrayList<Integer> getMyPeerList(){
        return myPeerList;
    }

    public ArrayList<Integer> getMyPreviousPeers(){
        return myPreviousPeers;
    }

    public ArrayList<String> getHostName(){
        return hostName;
    }

    public ArrayList<Integer> getPorts(){
        return ports;
    }

    public ArrayList<boolean> getHasFile(){
        return hasFile;
    }

    public boolean getAmIFirst(int peerID){
        return amIFirst;
    }

    public boolean getAmILast(int peerID){
        return amILast;
    }

    public Config(String commonConfig){

        //reading from common.cfg

        Scanner scanner = new Scanner(new FileReader(commonConfig));

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

        // reading from peerInfo.cfg

        //read peer info
        Scanner peerScanner = new Scanner(new FileReader(peerInfo));

        peerList = new ArrayList<Integer>();
        hostName = new ArrayList<String>();
        ports = new ArrayList<Integer>();
        hasFile = new ArrayList<Boolean>();

        uploadPorts = new ArrayList<Integer>();
        havePorts = new ArrayList<Integer>();

        int count = 0;
        while (peerScanner.hasNextLine()) {

            String string = peerScanner.nextLine();
            String[] splitBySpace = string.split(" ");
            this.peerList.add(Integer.parseInt(splitBySpace[0].trim()));
            this.hostName.add(splitBySpace[1].trim());
            this.ports.add(Integer.parseInt(splitBySpace[2].trim()));
            this.uploadPorts.add(Integer.parseInt(splitBySpace[2].trim()) + 1);
            this.havePorts.add(Integer.parseInt(splitBySpace[2].trim()) + 2);
            if (splitBySpace[3].trim().equals("1")) {
                this.hasFile.add(true);
            } else {
                this.hasFile.add(false);
            }
            count++;
        }

        this.numberOfPeers = count;
    }
}
