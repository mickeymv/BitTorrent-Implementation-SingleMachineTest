package logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import peer.PeerProcess;

public final class FileLogger {

	public static void initialize() {
		
		//Logger logger = Logger.getLogger(FileLogger.class);
		try {
			
			File logFileDirectory = new File("", "logs");
			logFileDirectory.mkdirs();
			
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.h-mm.a");
			String formattedDate = sdf.format(date);
			
			String filename = "log_peer_" + PeerProcess.getLocalPeerInstance().getPeerID() + ".log";
			
			File logFile = new File(logFileDirectory.getAbsolutePath(), filename);

			if (!logFile.exists()) {
				logFile.getParentFile().mkdirs();
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			FileOutputStream fos = new FileOutputStream(logFile, false);
			PrintStream ps = new PrintStream(fos);

			System.setOut(ps);
			//logger.info("This initializes log4j!");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
