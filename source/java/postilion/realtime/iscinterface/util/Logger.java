package postilion.realtime.iscinterface.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Logger {
	
	public static final String filePath = "D:\\temp\\logs\\log_ISCInterfaceCB.txt";
	
	public static final String filePathError = "D:\\temp\\logs\\log_ISCInterfaceCB_ERROR.txt";

	public static void logLine(String msg, boolean log2File) {
		
		if (log2File) {
			
			Date date = new Date();
			StringBuilder sb = new StringBuilder();
			//sb.append("[").append(new Date().toString()).append("]");
			sb.append(date.toString() + "\n" + msg + "\n");
			BufferedWriter bf = null;
			try {
				bf = new BufferedWriter(new FileWriter(filePath, true));
				bf.append(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (bf != null)
					try {
						bf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			} 
			
		}
		
	}
	
	public static void logError(String msg, boolean log2File) {
		
		if (log2File) {
			
			Date date = new Date();
			StringBuilder sb = new StringBuilder();
			//sb.append("[").append(new Date().toString()).append("]");
			sb.append(date.toString() + "\n" + msg + "\n");
			BufferedWriter bf = null;
			try {
				bf = new BufferedWriter(new FileWriter(filePathError, true));
				bf.append(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (bf != null)
					try {
						bf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			} 
			
		}
		
	}
}
