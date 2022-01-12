package postilion.realtime.iscinterface.web;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;

import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.sdk.message.xml.XXMLMessageUnableToConstruct;
import postilion.realtime.sdk.util.XPostilion;

public class WebClient {

	// create a low-level Jetty HTTP/2 client
	HTTP2Client lowLevelClient = new HTTP2Client();
	HttpClient client = null;

	private static WebClient mywc;

	public static WebClient getWebClient() {
		if (mywc == null) {
			mywc = new WebClient();
		}
		return mywc;
	}

	private WebClient() {

		try {
			this.lowLevelClient.start();
			HttpClientTransportOverHTTP2 transport = new HttpClientTransportOverHTTP2(lowLevelClient);
			// transport.setUseALPN(false);
			this.client = new HttpClient(transport, null);
			this.client.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String createPOSTRequest(String urlStr, String msgXml)
			throws IOException, XXMLMessageUnableToConstruct, XPostilion {

		StringBuilder response = new StringBuilder();

		Logger.logLine("Consuming WS= " + urlStr + "\n" + msgXml, false);

		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Set timeout as per needs
		connection.setConnectTimeout(20000);
		connection.setReadTimeout(20000);

		// Set DoOutput to true if you want to use URLConnection for output.
		// Default is false
		connection.setDoOutput(true);
		connection.setUseCaches(true);
		connection.setRequestMethod("POST");

		// Set Headers
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/xml");

		// Write XML
		OutputStream outputStream = connection.getOutputStream();
		byte[] b = msgXml.toString().getBytes("UTF-8");
		outputStream.write(b);
		outputStream.flush();
		outputStream.close();

		// Read XML
		if (connection.getResponseCode() == 200) {
			InputStream inputStream = connection.getInputStream();
			byte[] res = new byte[8192];
			int i = 0;
			while ((i = inputStream.read(res)) != -1) {
				response.append(new String(res, 0, i));
			}
			inputStream.close();

			Logger.logLine("Response WS= " + response.toString(), false);
		} else {
			Logger.logLine("Response WS= " + connection.getResponseCode(), false);
		}

		return response.toString();

	}

	public String createPOSTRequest2(String urlStr, String msgXml)
			throws InterruptedException, TimeoutException, ExecutionException {

		Logger.logLine("****createPOSTRequest2****" + urlStr + "\n" + this.client.GET(urlStr), false);

		Request req = null;
		ContentResponse rsp = null;

		if (this.client != null) {
			Logger.logLine("Web Client NOT NULL", false);
			req = this.client.POST(urlStr);
			req.header(HttpHeader.ACCEPT, "application/xml");
			req.header(HttpHeader.CONTENT_TYPE, "application/xml");
			req.content(new StringContentProvider(msgXml));

			rsp = req.send();
			Logger.logLine("RSP::" + new String(rsp.getContent()), false);
			return rsp.getContentAsString();
		} else {
			Logger.logLine("Web Client is NULL", false);
			return null;
		}
	}

	public String retriveAllCovenatData() throws IOException {

		StringBuilder response = new StringBuilder();

		Logger.logLine("Consuming WS for covenats", false);

		URL url = new URL("http://127.0.0.1:8099/api/postcovenatdata");
//		URL url = new URL("http://10.89.0.169:8099/api/postcovenatdata");
//		URL url = new URL("http://10.94.19.244:8099/api/postcovenatdata");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Set timeout as per needs
		connection.setConnectTimeout(2000);
		connection.setReadTimeout(2000);

		// Set DoOutput to true if you want to use URLConnection for output.
		// Default is false
		connection.setDoOutput(true);
		connection.setUseCaches(true);
		connection.setRequestMethod("POST");

		// Set Headers
//		connection.setRequestProperty("Accept", "application/json");
		// connection.setRequestProperty("Content-Type", "text/plain");

		// Write XML
//		OutputStream outputStream = connection.getOutputStream();
//		byte[] b = msgXml.toString().getBytes("UTF-8");
//		outputStream.write(b);
//		outputStream.flush();
//		outputStream.close();

		// Read XML
		if (connection.getResponseCode() == 200) {
			InputStream inputStream = connection.getInputStream();
			byte[] res = new byte[16384];
			int i = 0;
			while ((i = inputStream.read(res)) != -1) {
				response.append(new String(res, 0, i));
			}
			inputStream.close();

			Logger.logLine("Response WS= " + response.toString(), false);
		} else {
			Logger.logLine("Response WS= " + connection.getResponseCode(), false);
		}

		return response.toString();

	}
	
	public String test() throws IOException {

		StringBuilder response = new StringBuilder();

		Logger.logLine("Consuming WS TEST", false);

		URL url = new URL("http://10.89.0.169:8087/entry-point/test");
//		URL url = new URL("http://127.0.0.1:8087/entry-point/test");
//		URL url = new URL("http://10.89.0.169:8099/api/postcovenatdata");
//		URL url = new URL("http://10.94.19.244:8099/api/postcovenatdata");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Set timeout as per needs
		connection.setConnectTimeout(2000);
		connection.setReadTimeout(2000);

		// Set DoOutput to true if you want to use URLConnection for output.
		// Default is false
		connection.setDoOutput(true);
		connection.setUseCaches(true);
		connection.setRequestMethod("GET");

		// Read XML
		if (connection.getResponseCode() == 200) {
			InputStream inputStream = connection.getInputStream();
			byte[] res = new byte[16384];
			int i = 0;
			while ((i = inputStream.read(res)) != -1) {
				response.append(new String(res, 0, i));
			}
			inputStream.close();

			Logger.logLine("Response WS= " + response.toString(), false);
		} else {
			Logger.logLine("Response WS= " + connection.getResponseCode(), false);
		}

		return response.toString();

	}

	public String validateMsgData(String data) throws IOException {

		StringBuilder response = new StringBuilder();

		Logger.logLine("Consuming WS VALIDATIONS", true);

		URL url = new URL("http://10.89.0.169:8087/entry-point/validate");
//		URL url = new URL("http://127.0.0.1:8087/entry-point/validate");		
//		URL url = new URL("http://10.89.0.169:8099/api/postcovenatdata");
//		URL url = new URL("http://10.94.19.244:8099/api/postcovenatdata");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Set timeout as per needs
		connection.setConnectTimeout(50);
		connection.setReadTimeout(50);

		// Set DoOutput to true if you want to use URLConnection for output.
		// Default is false
		connection.setDoOutput(true);
		connection.setUseCaches(true);
		connection.setRequestMethod("POST");

		// Set Headers
//		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "text/plain");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("Keep-Alive", "timeout=5, max=10000");
		connection.setRequestProperty( "charset", "utf-8");
		connection.setRequestProperty( "Content-Length", Integer.toString( data.length() ));
		connection.setUseCaches( false );
		try( DataOutputStream wr = new DataOutputStream( connection.getOutputStream())) {
		   wr.write( data.getBytes() );
		}

		// Write XML
//		OutputStream outputStream = connection.getOutputStream();
//		byte[] b = data.getBytes("UTF-8");
//		outputStream.write(b);
//		outputStream.flush();
//		outputStream.close();
		
		try(OutputStream os = connection.getOutputStream()) { 
			byte[] input = data.getBytes("utf-8"); 
			os.write(input, 0, input.length); 
		}

		// Read XML
		if (connection.getResponseCode() == 200) {

			try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				Logger.logLine("Response WS= " + response.toString(), true);
			}

		}
		else {
			Logger.logLine("Response WS= " + connection.getResponseCode(), true);
		}

		return response.toString();

	}
}
