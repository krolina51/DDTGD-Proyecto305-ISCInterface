package postilion.realtime.iscinterface.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Logger {
	
	public static final String filePath = "C:\\temp\\logs\\log_ISCInterface.txt";

	public static void logLine(String msg) {
		
		Date date = new Date();
		StringBuilder sb = new StringBuilder();
		//sb.append("[").append(new Date().toString()).append("]");
		sb.append(date.toString()+"\n"+msg+"\n");
		BufferedWriter bf = null; 
		try {
			bf = new BufferedWriter(new FileWriter(filePath, true));
			bf.append(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(bf != null)
				try {
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
	}
}
