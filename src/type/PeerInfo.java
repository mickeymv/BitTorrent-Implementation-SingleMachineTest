/**
 * 
 */
package type;

/**
 * @author Mickey Vellukunnel (mickeymv)
 *	Class which encapsulates details about a peer such as 
 *	ID, hostname, portnumber, whether it has the initial file or not, BitField, etc.
 */
public class PeerInfo {
	
	private String peerID;
	private String hostName;
	private int portNumber;
	boolean hasFileInitially;
	int bitfield;    // bitfield of this peer.
	
	public int getBitfield() {
		return bitfield;
	} 
	
	public void setBitfield(int bitfield) {
		this.bitfield = bitfield;
	}

	public PeerInfo(String peerID, String hostName, int portNumber, boolean hasFileInitially) {
		this.peerID = peerID;
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.hasFileInitially = hasFileInitially;
	}
	
	public PeerInfo() {
		// TODO Auto-generated constructor stub
	}

	public String getPeerID() {
		return peerID;
	}
	public void setPeerID(String peerID) {
		this.peerID = peerID;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public int getPortNumber() {
		return portNumber;
	}
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
	public boolean isHasFileInitially() {
		return hasFileInitially;
	}
	public void setHasFileInitially(boolean hasFileInitially) {
		this.hasFileInitially = hasFileInitially;
	}
	
	

}
