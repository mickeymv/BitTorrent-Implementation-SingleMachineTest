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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.io.File;
import connection.TCPConnectionManager;
import peer.PeerProcess;
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
	public final byte[] intToByteArray(int peerIDIntegerValue) {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(peerIDIntegerValue);
		return b.array();
	}

	public final int intFromByteArray(byte[] integerBytes) {
		return ByteBuffer.wrap(integerBytes).getInt(0);
	}

	public static ArrayList<PeerInfo> getPeerList() {
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
	 * Initialize bitfield according to the local file.
	 * 
	 * @return
	 */
	public static ArrayList<Byte> initializeLocalPeerBitfield(boolean hasFileInitially) {
		ArrayList<Byte> bitField = new ArrayList<Byte>();

		// if there is no local file, set the bitmap to be null
		if (ConfigurationSetup.getInstance().getFileName() == null) {
			return bitField;
		} else {
			bitField = new ArrayList<Byte>();

			int lengthOfBitfield = ConfigurationSetup.getNumberOfPieces() / 8;

			System.out.println("length of bitfield:" + lengthOfBitfield);
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

			if (hasFileInitially) {
				// set remaining bits of the last byte.
				int remaining = ConfigurationSetup.getNumberOfPieces() - (lengthOfBitfield) * 8;
				System.out.println("number of pieces:" + ConfigurationSetup.getNumberOfPieces() + " \n" + "remaining:"
						+ remaining);
				Byte b = setFirstNDigits(remaining);
				bitField.add(b);
			}
			return bitField;
		}
	}

	/**
	 * split the data file into pieces. Directory: project/peer_[peerID]
	 */
	public static void splitDataFile() {

		Path path = Paths.get(ConfigurationSetup.getInstance().getFileName());
		// String pieceDir = "project/peer_" +
		// PeerProcess.getLocalPeerInstance().getPeerID();
		String pieceDir = "project" + File.pathSeparator + "peer_xxxxxx";

		byte[] byteChunk;
		FileOutputStream outputStream = null;
		int pieceSize = ConfigurationSetup.getInstance().getPieceSize();
		File temp_path = new File("project");
		temp_path.mkdirs();

		try {
			byte[] data = Files.readAllBytes(path);
			long fileLength = data.length;
			long remaining = fileLength;
			System.out.println("size of data:" + data.length);
			int from = 0;
			int to = 0;
			int chunkIdx = 0;
			String pieceFileName = null;

			while (remaining > 0) { // if there is more data
				if (remaining <= pieceSize) {
					to = from + (int) remaining;
					remaining = 0;
				} else {
					to = from + pieceSize;
					remaining -= pieceSize;
				}

				System.out.println("from:" + from + " to:" + to);

				byteChunk = Arrays.copyOfRange(data, from, to);
				chunkIdx++;
				pieceFileName = pieceDir + "_piece_" + Integer.toString(chunkIdx);
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
	public static void mergeDataPieces(String directory) {

		File path = new File(directory);
		FileOutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream(ConfigurationSetup.getInstance().getFileName(), true);
		} catch (FileNotFoundException e1) {
			System.err.print("Cannot create data file.");
			e1.printStackTrace();
		}

		for (int i = 1; i <= ConfigurationSetup.getNumberOfPieces(); i++) {

			String pieceFileName = directory + File.separator + "peer_xxxxxx_piece_" + i;
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
	public static void savePieceFile(byte[] data, String name) {

		FileOutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream(new File(name));
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
	public static String getPieceFileName(int pieceNum) {

		return "project" + File.separator + "peer_xxxxxx_piece_" + pieceNum;
	}

	/**
	 * read a piece of data file as a byte array
	 * 
	 * @param pieceNum
	 */
	public static byte[] getPieceAsByteArray(int pieceNum) {
		Path path = Paths.get(getPieceFileName(pieceNum));
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
			System.out.println("b:" + b + '\t');
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
}
