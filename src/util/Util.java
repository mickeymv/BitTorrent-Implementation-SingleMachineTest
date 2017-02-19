package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import type.PeerInfo;


public class Util {

    private static Util instance = null;

    private static ArrayList<PeerInfo> peerList;
    private ArrayList<PeerInfo> myPeerList;
    private ArrayList<PeerInfo> myPreviousPeers;
    private boolean amIFirst;
    private boolean amILast;
    private static Map<String, Integer> peerIDToPositionMap ;
    private static Config configInstance = null;

    public static Util getInstance(){
        if(instance == null){
            instance = new Util();
            configInstance = Config.getInstance();
            peerIDToPositionMap = configInstance.getPeerIDToPositionMap();
            peerList = configInstance.getPeerList();
        }
        return instance;
    }

    public ArrayList<PeerInfo> getPeerList(){
        return peerList;
    }

    public ArrayList<PeerInfo> getMyPeerList(String peerID) {
        myPeerList = new ArrayList<PeerInfo>();

        for(PeerInfo peerInfo:peerList){
            if(peerInfo.getPeerID() != peerID){
                myPeerList.add(peerInfo);
            }
        }
        return myPeerList;
    }

    public ArrayList<PeerInfo> getMyPreviousPeers(String peerID) {
        int peerPosition = peerIDToPositionMap.get(peerID);
            for(int i1 = 0; i1 < peerPosition; i1++){
                myPreviousPeers.add(peerList.get(i1));
            }
        return myPreviousPeers;
    }

    public boolean isAmIFirst(String peerID) {
         if(peerList.get(0).getPeerID() == peerID){
                return true;
            }
        return false;
    }

    public boolean isAmILast(String peerID) {
        if(peerList.get(peerList.size()-1).getPeerID() == peerID){
            return true;
        }
        return false;
    }

    public PeerInfo getPeerInfo(String peerID){
    		int peerPosition = peerIDToPositionMap.get(peerID);
    		PeerInfo peerInfo = peerList.get(peerPosition);
        return peerInfo;
    }
}
