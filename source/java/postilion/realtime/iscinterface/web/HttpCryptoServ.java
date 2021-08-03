package postilion.realtime.iscinterface.web;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import postilion.realtime.iscinterface.util.Logger;

public class HttpCryptoServ {

	/**
	 * Method for API connect
	 * 
	 * @param action - get Method for API
	 * @param key    - Value to send to method API
	 * @return - Model response API
	 */
	public static String httpConnection(String endPoint, String[] params) {
		String result = "";
		Object[] args = (Object[]) params;
		String fullEndPoint = String.format(endPoint, args);
		Logger.logLine("FULL END POINT: " + fullEndPoint, false);
		ContentResponse response;
		SslContextFactory sslContextFactory = new SslContextFactory();

		sslContextFactory.setIncludeProtocols("TLSv1.2");
		sslContextFactory.setTrustAll(true);

		HttpClient httpClient = new HttpClient(sslContextFactory);
		httpClient.setConnectTimeout(2000);
		
		
		// Start HttpClient
		try {
			httpClient.start();
			Logger.logLine("start http client", false);
		} catch (Exception e) {
			Logger.logLine("error while starting http client " + e.getMessage(), false);
		}

		try {
			Logger.logLine("getting response", false);
			response = httpClient.GET(fullEndPoint);
			Logger.logLine(response.getContentAsString(), false);
			result = response.getContentAsString();
		} catch (InterruptedException e1) {
			Logger.logLine("error getting response InterruptedException " + e1.getMessage(), false);
		} catch (ExecutionException e1) {
			Logger.logLine("error getting response ExecutionException " + e1.getMessage(), false);
		} catch (TimeoutException e1) {
			Logger.logLine("error getting response TimeoutException " + e1.getMessage(), false);
		}
		return result;
	}

	public static void main(String[] args) {
		String endPoint = "https://localhost:8081/entry-point/getPIN?encoding=%s&workingKey1=%s&workingKey2=%s&pinBlock=%s&pan=%s&seeds=%s";
		String[] params = new String[6];
		params[0] = "BASE64";
		params[1] = "MVBVTkUwMDAsRjJBODU2NjE0QTQ5REVDMDMyNEQ5RTJENDUwNzNDNDY4QjI4NjAyREEzNkExODNBLEU3RUU1Qjk2MjU1NDA3ODE";
		params[2] = "MVBVTkUwMDAsRUJDOEJDNjM0MEM2RkUyRjYxMTU2M0Y0MjY4MDdEMjM0OUI5QjdCNDU4NDNCMDk2LDg4Q0NEOTk5MDNFMjE2QTY";
		params[3] = "QkRENUY0QzIxQjVFM0I4OQ";
		params[4] = "Nzc3NzkwMDAzNTMyMDU2MQ";
		params[5] = "MEIxQTJDM0Q0RjVFNjc4OTk4NzZGNEU1RDNDMkIwQTEwQjFBMkMzRDRGNUU2Nzg5";
		System.out.println(HttpCryptoServ.httpConnection(endPoint, params));
	}

}