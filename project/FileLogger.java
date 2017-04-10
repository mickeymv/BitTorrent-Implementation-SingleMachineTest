
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import PeerProcess;

public final class FileLogger {

	public void initialize(String localPeerID) {
		
		//Logger logger = Logger.getLogger(FileLogger.class);
		try {
			
			File logFileDirectory = new File("project");
			if (! logFileDirectory.exists())
				logFileDirectory.mkdirs();
			
			//Date date = new Date();
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.h-mm.a");
			//String formattedDate = sdf.format(date);
			
			String filename = "log_peer_" + localPeerID + ".log";
			
			File logFile = new File(logFileDirectory, filename);
			
			if (!logFile.exists()) {
				logFile.getParentFile().mkdirs();
				try {
//					System.out.println("create file: " + logFile.getAbsolutePath());
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
