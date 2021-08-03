package postilion.realtime.iscinterface.security;

import java.net.URL;
import java.util.Properties;
import postilion.realtime.iscinterface.security.config.SSLGeneralConfig;
import postilion.realtime.iscinterface.util.Utils;

public class ClientApp {
	
	static Properties props = null;
	
	public static void main(String[] args) {
		System.out.println("In main");

		try {
			props = Utils.readPropsFromBasePropFile();
		} catch (Exception e) {
			System.out.println("Exception in reading properties file : system.properties");
			e.printStackTrace();
			System.exit(-1);
		}
		
		SSLGeneralConfig gc = SSLGeneralConfig.getInstance();
		gc.setKEYSTOREPATH((String) props.getProperty("KEYSTOREPATH"));
		gc.setTRUSTSTOREPATH((String) props.getProperty("TRUSTSTOREPATH"));
		gc.setKEYSTOREPW((String) props.getProperty("KEYSTOREPW"));
		gc.setTRUSTSTOREPW((String) props.getProperty("TRUSTSTOREPW"));
		gc.setKEYPASS((String) props.getProperty("KEYPASS"));
		gc.setKeystoreType((String) props.getProperty("keystoreType"));
		gc.setTrustAllCertificate((String) props.getProperty("trustAllCertificate"));
		gc.setKeymanageralgorithm((String) props.getProperty("keymanageralgorithm"));
		try {
			
			//A SOAP web service call
			SSLClient sslClient = SSLClient.getSSLClient();
			String strurl = "https://localhost:23521/app/v1/myservice";// you can add all the urls in config file
			URL url = new URL(strurl);
			String method = "POST";
			String message = "your soap message body";
			String msgtype = "text/xml";
			String response = sslClient.sendRequest(url, method, message, msgtype);
			
			//A RESTFul GET web service call
			strurl = "https://localhost:23521/app/v1/test/Student.json?studentId=9999";
			url = new URL(strurl);
			method = "GET";
			message = "";
			msgtype = "text/xml";
			response = sslClient.sendRequest(url, method, message, msgtype);
			
			//A RESTFul POST web service call
			strurl = "https://localhost:23521/app/v1/test/Student.json";
			url = new URL(strurl);
			method = "POST";
			message = "your json message body";
			msgtype = "text/xml";
			response = sslClient.sendRequest(url, method, message, msgtype);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}