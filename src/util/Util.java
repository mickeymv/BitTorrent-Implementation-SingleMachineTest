package util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.io.File;
import connection.TCPConnectionManager;
import peer.PeerProcess;
import type.PeerInfo;

/**
* @author Mickey Vellukunnel, Xiaolong Li, Arpitha
*
*/

public class Util {

	private static Util instance = null;
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

	private static ArrayList<PeerInfo> peerList;
	private static Map<String, Integer> peerIDToPositionMap;
	private static ConfigurationSetup configInstance = null;

	public static final String PROJECT_TOP_LEVEL_DIRECTORY = "project";
	public static final String PEER_DIRECTORY_PREFIX = "peer_";
	private static final String PIECE_PREFIX = "_piece_";

	private Util() {
	}

	public static Util initializeUtil() {
		if (instance == null) {
			instance = new Util();
			configInstance = ConfigurationSetup.getInstance();
			peerIDToPositionMap = configInstance.getPeerIDToPositionMap();
			peerList = configInstance.getPeerList();
		}
		return instance;
	}

	/*
	 * Converts an integer to its 4 byte representation.
	 */
	public static final byte[] intToByteArray(int peerIDIntegerValue) {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(peerIDIntegerValue);
		return b.array();
	}

	public static final int intFromByteArray(byte[] integerBytes) {
		return ByteBuffer.wrap(integerBytes).getInt(0);
	}

	public static ArrayList<PeerInfo> getPeerList() {
		return peerList;
	}

	public static ArrayList<PeerInfo> getMyPeerList(String peerID) {
		ArrayList<PeerInfo> myPeerList = new ArrayList<PeerInfo>();

		for (PeerInfo peerInfo : peerList) {
			if (! peerInfo.getPeerID().equals(peerID)) {
				myPeerList.add(peerInfo);
			}
		}
		return myPeerList;
	}

	public static boolean isPieceIndexSetInBitField(int pieceIndex, ArrayList<Byte> bitField) {
		// System.out.println("the bitfield to check in is:");
		// Util.printBitfield(bitField);
		int positionOfPieceWithinByte = pieceIndex % 8;
		// System.out.println("positionOfPieceWithinByte is:" +
		// positionOfPieceWithinByte);
		// System.out.println("the piece# to check for is:" + pieceIndex);
		int byteContainingPiece = pieceIndex / 8;
		// System.out.println("byteContainingPiece# is:" + byteContainingPiece);
		
		if(bitField == null || bitField.size() == 0) {
			//TODO: Remove this and figure out why this happens!
			return false;
		}
		
		byte checkByte = bitField.get(byteContainingPiece);
		// System.out.println("the byte containing piece is:" +
		// String.valueOf(checkByte));
		return isBitSetInPosition(positionOfPieceWithinByte, checkByte);
	}

	public static boolean isBitSetInPosition(int position, byte checkByte) {
		switch (position) {
		case 0:
			return (checkByte & 128) == 128;
		case 1:
			return (checkByte & 64) == 64;
		case 2:
			return (checkByte & 32) == 32;
		case 3:
			return (checkByte & 16) == 16;
		case 4:
			return (checkByte & 8) == 8;
		case 5:
			return (checkByte & 4) == 4;
		case 6:
			return (checkByte & 2) == 2;
		case 7:
			return (checkByte & 1) == 1;
		}

		return false;
	}

	public static ArrayList<PeerInfo> getMyPreviousPeers(String peerID) {
		ArrayList<PeerInfo> myPreviousPeers = new ArrayList<PeerInfo>();
		int peerPosition = peerIDToPositionMap.get(peerID);
		for (int i1 = 0; i1 < peerPosition; i1++) {
			myPreviousPeers.add(peerList.get(i1));
		}
		return myPreviousPeers;
	}

	public static boolean isFirstPeer(String peerID) {
		if (peerList.get(0).getPeerID() == peerID) {
			return true;
		}
		return false;
	}

	public static boolean isLastPeer(String peerID) {
		if (peerList.get(peerList.size() - 1).getPeerID() == peerID) {
			return true;
		}
		return false;
	}

	public static PeerInfo getPeerInfo(String peerID) {
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
	 * get bitfield for a peer according to the local file.
	 * 
	 * @return
	 */
	public static ArrayList<Byte> getPeerBitfield(boolean hasFileInitially) {
		ArrayList<Byte> bitField = new ArrayList<Byte>();
		
		// if there is no local file, set the bitmap to be null
		if (ConfigurationSetup.getInstance().getFileName() == null) {
			return bitField;
		} else {
			bitField = new ArrayList<Byte>();
			
			int lengthOfBitfield = ConfigurationSetup.getNumberOfPieces() / 8;
			
			//System.err.println("length of bitfield:" + lengthOfBitfield);
			// set all full bytes
			for (int i = 0; i < lengthOfBitfield; i++) {

				Byte b = hasFileInitially ? new Byte((byte) 0xFF) : new Byte((byte) 0x00); // if
																							// the
																							// local
																							// peer
																							// has
																							// file,
																							// set
																							// corresponding
																							// bits
																							// to
																							// 1,
																							// else
																							// set
																							// everything
																							// to
																							// zero
				bitField.add(b);
			}
			
			//System.err.println("current bitfield: " + bitfieldToString(bitField));

			if (hasFileInitially) {
				// set remaining bits of the last byte.
				int remaining = ConfigurationSetup.getNumberOfPieces() - (lengthOfBitfield) * 8;
				// System.out.println("number of pieces:" +
				// ConfigurationSetup.getNumberOfPieces() + " \n" + "remaining:"
				// + remaining);
				if (remaining != 0) {
					Byte b = setFirstNDigits(remaining);
					bitField.add(b);
				}
			} else {
				
				Byte b = new Byte((byte)0x00);
				bitField.add(b);
			}
			return bitField;
		}
	}

	public static void makePeerDirectory(String localPeerID) {
		File temp_path = new File(PROJECT_TOP_LEVEL_DIRECTORY);
		String pieceDir = PROJECT_TOP_LEVEL_DIRECTORY + "/" + PEER_DIRECTORY_PREFIX + localPeerID + "/";
		temp_path = new File(pieceDir);
		temp_path.mkdirs();
	}

	/**
	 * split the data file into pieces. Directory: project/peer_[peerID]
	 */
	public static void splitDataFile(String localPeerID) {

		Path path = Paths.get(PROJECT_TOP_LEVEL_DIRECTORY + "/" + PEER_DIRECTORY_PREFIX + localPeerID + "/" + ConfigurationSetup.getInstance().getFileName());
		// String pieceDir = "project/peer_" +
		// PeerProcess.getLocalPeerInstance().getPeerID();
		File temp_path = new File(PROJECT_TOP_LEVEL_DIRECTORY);
		String pieceDir = PROJECT_TOP_LEVEL_DIRECTORY + "/" + PEER_DIRECTORY_PREFIX + localPeerID + "/"; // File.pathSeparator
																											// giving
																											// ':'
																											// ?

		byte[] byteChunk;
		FileOutputStream outputStream = null;
		int pieceSize = ConfigurationSetup.getInstance().getPieceSize();
		temp_path = new File(pieceDir);
		temp_path.mkdirs();

		try {
			//System.err.println("The path is: " + path.toString());
			byte[] data = Files.readAllBytes(path);
			long fileLength = data.length;
			long remaining = fileLength;
			// System.out.println("size of data:" + data.length);
			int from = 0;
			int to = 0;
			int chunkIdx = -1;
			String pieceFileName = null;

			while (remaining > 0) { // if there is more data
				if (remaining <= pieceSize) {
					to = from + (int) remaining;
					remaining = 0;
				} else {
					to = from + pieceSize;
					remaining -= pieceSize;
				}

				// System.out.println("from:" + from + " to:" + to);

				byteChunk = Arrays.copyOfRange(data, from, to);
				chunkIdx++;
				pieceFileName = pieceDir + PIECE_PREFIX + Integer.toString(chunkIdx);
				outputStream = new FileOutputStream(new File(pieceFileName));
				outputStream.write(byteChunk);
				outputStream.flush();
				outputStream.close();
				byteChunk = null;
				outputStream = null;

				from = to;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Merge all pieces of data file into a single file.
	 * 
	 */
	public static void mergeDataPieces(String localPeerID, String directory) {

		File path = new File(directory);
		FileOutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream(ConfigurationSetup.getInstance().getFileName(), true);
		} catch (FileNotFoundException e1) {
			System.err.print("Cannot create data file.");
			e1.printStackTrace();
		}

		for (int i = 0; i < ConfigurationSetup.getNumberOfPieces(); i++) {

			String pieceFileName = directory + "/" + "peer_" + localPeerID + "/_piece_" + i; // File.separator
			// giving
			// ':'
			// ?
			File file = new File(pieceFileName);
			if (file.exists()) {

				byte[] data;
				try {
					data = Files.readAllBytes(file.toPath());
					outputStream.write(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {

				System.err.println("piece file not found! " + pieceFileName);
			}
		}

		try {
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Save a piece of data into a binary file.
	 * 
	 * @param data
	 */
	public static void savePieceFile(byte[] data, String localPeerID, String pieceNumber) {

		FileOutputStream outputStream = null;

		String filePath = PROJECT_TOP_LEVEL_DIRECTORY + "/" + PEER_DIRECTORY_PREFIX + localPeerID + "/" + PIECE_PREFIX
				+ pieceNumber;

		try {
			outputStream = new FileOutputStream(new File(filePath));
			outputStream.write(data);
			outputStream.flush();
			outputStream.close();
			data = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return piece file name given piece number
	 * 
	 * @param pieceNum
	 * @return
	 */
	public static String getPieceFileName(String localPeerID, int pieceNum) {

		return "project/peer_" + localPeerID + "/_piece_" + pieceNum; // File.separator
	}

	/**
	 * read a piece of data file as a byte array
	 * 
	 * @param pieceNum
	 */
	public static byte[] getPieceAsByteArray(String localPeerID, int pieceNum) {
		Path path = Paths.get(getPieceFileName(localPeerID, pieceNum));
		byte[] data = null;

		try {
			if (path.toFile().exists())
				data = Files.readAllBytes(path);
			else
				System.err.println("file not exist:" + path.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * Set the first N digits of a Byte object
	 * 
	 * @return
	 */
	public static Byte setFirstNDigits(int n) {

		byte b = 0;
		for (int i = 0; i < n; i++) {
			b = (byte) (b >> 1);
			b = (byte) (b | 0b10000000);
			// System.out.println("b:" + b + '\t');
		}
		return new Byte(b);
	}

	/**
	 * Return the number of 1's in the Byte
	 * 
	 * @param map
	 * @return
	 */
	public static int getFirstNDigits(Byte map) {

		for (int i = 0; i < 8; i++) {

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
	 * 
	 * @param size
	 */
	public static void createRandomDataFile(int size) {

		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		Random r = new Random();

		int nbBytes = 0;
		DataOutputStream dos;
		try {
			dos = new DataOutputStream(new FileOutputStream(ConfigurationSetup.getInstance().getFileName()));
			while (nbBytes < size) {

				int nbBytesToWrite = Math.min(size - nbBytes, bufferSize);
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

	public static String byte2BinaryString(Byte b) {
		
		return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}
	
	
	public static void printByteToBinaryString(Byte b) {

		String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
		System.out.println(s1);
	}

	/**
	 * @param peerID
	 * @param fileName
	 * @throws IOException
	 */
	public static void move(String peerID, String fileName) throws IOException {

		// String fileName = "TheFile.dat";

		/*
		 * String fileName2 = "~/BitTorrent-Implementation/peer_"+peerID;
		 * 
		 * Path source = Paths.get(fileName); Path target =
		 * Paths.get(fileName2);
		 * 
		 * Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		 */

		try {

			File afile = new File(fileName);

			if (afile.renameTo(new File("peer_" + peerID))) {
				System.out.println("File is moved successful!");
			} else {
				System.out.println("File is failed to move!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Print bitfield as a string.
	 */
	public static void printBitfield(ArrayList<Byte> bitField) {
		
		for (Byte b : bitField) {
			printByteToBinaryString(b);
		}
	}
	
	public static String bitfieldToString(ArrayList<Byte> bitfield) {
		
		StringBuilder builder = new StringBuilder();
		for (Byte b : bitfield) {
			
			builder.append(byte2BinaryString(b) + "\n");
		}
		
		return builder.toString();
	}

	/**
	 * Set the corresponding bit in the bitfield for the piece.
	 * 
	 * @param pieceIndex
	 * @param bitField
	 */
	public static synchronized void setPieceIndexInBitField(int pieceIndex, ArrayList<Byte> bitField) {
		int positionOfPieceWithinByte = pieceIndex % 8;
		//System.out.println("the piece# to check for is:" + pieceIndex);
		int byteContainingPiece = pieceIndex / 8;
		//System.out.println("byteContainingPiece is:" + byteContainingPiece);
		//System.out.println("size of bitfield is:" + bitField.size());
		if(bitField == null || bitField.size() == 0) {
			//TODO: Remove this and figure out why this happens!
			return;
		}
		byte checkByte = bitField.get(byteContainingPiece);
		bitField.set(byteContainingPiece, setBitInPosition(positionOfPieceWithinByte, checkByte));
	}

	public static byte setBitInPosition(int position, byte checkByte) {
		switch (position) {
		case 0:
			return (byte) (checkByte | 128);
		case 1:
			return (byte) (checkByte | 64);
		case 2:
			return (byte) (checkByte | 32);
		case 3:
			return (byte) (checkByte | 16);
		case 4:
			return (byte) (checkByte | 8);
		case 5:
			return (byte) (checkByte | 4);
		case 6:
			return (byte) (checkByte | 2);
		case 7:
			return (byte) (checkByte | 1);
		}

		return (Byte) null;
	}
}
