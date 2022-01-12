package postilion.realtime.iscinterface.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import postilion.realtime.sdk.eventrecorder.EventRecorder;



/**
 * This class deifines a basic UDP client
 * 
 * @author Cristian Cardozp
 *
 */
public class Client {

	InetAddress ipAddress;
	int port;
	DatagramSocket socket;

	public Client() {

	}

	public Client(String ipAddress, String port) {
		Logger.logLine(" Client: 2 parametros" , false);

		if (!ipAddress.equals("0") && !port.equals("0")) {
			try {

				if (validateIp(ipAddress))
					this.ipAddress = InetAddress.getByName(ipAddress);
				else
					throw new Exception("IP parameter for server UDP, is not a IP valid");

				if (validatePort(port))
					this.port = Integer.valueOf(port);
				else
					throw new Exception("Port parameter for server UDP, is not a Port valid");
				
			} catch (Exception e) {
			
				StringWriter outError = new StringWriter();
				e.printStackTrace(new PrintWriter(outError));
				EventRecorder.recordEvent(new Exception("Exception in Constructor:  Client: " + outError.toString()));
				Logger.logLine("Exception in Constructor:  Client: " + outError.toString(), true);
				
			}
		}
	}
	
	public Client(String ipAddress, String port, String portOut) {
		//Logger.logLine(" Client: 3 parametros " + ipAddress + " " + port + " " + portOut, true);
		if (!ipAddress.equals("0") && !port.equals("0")) {
			try {

				if (validateIp(ipAddress))
					this.ipAddress = InetAddress.getByName(ipAddress);
				else
					throw new Exception("IP parameter for server UDP, is not a IP valid");

				if (validatePort(port))
					this.port = Integer.valueOf(port);
				else
					throw new Exception("Port parameter for server UDP, is not a Port valid");
				
				if (validatePort(portOut))
					this.socket = new DatagramSocket(Integer.valueOf(portOut));
				else
					throw new Exception("Port Out parameter for server UDP, is not a Port valid");
				

			} catch (Exception e) {
				
				StringWriter outError = new StringWriter();
				e.printStackTrace(new PrintWriter(outError));
				EventRecorder.recordEvent(new Exception("Exception in Constructor 2 :  Client: " + outError.toString()));
				Logger.logLine("Exception in Constructor 2 :  Client: " + outError.toString(), true);
				
			}
		}
	}

	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public InetAddress getIpAddress() {
		return this.ipAddress;
	}

	public int getPort() {
		return this.port;
	}

	/**
	 * Valida si la información en el archivo es una ip.
	 * 
	 * @param ip
	 * @return true si es un ip
	 */
	static public boolean validateIp(String ip) {
		boolean ipIsOk = false;
		Validation validator = Validation.getInstance();
		if (validator.validateByRegex(IP_REGEX, ip)) {
			ipIsOk = true;
		} else {
			Logger.logLine("IP parameter for server UDP, is not a IP valid", false);
		}
		return ipIsOk;
	}

	/**
	 * Valida si la información en el archivo es un puerto.
	 * 
	 * @param port
	 * @return true si es un puerto
	 */
	static public boolean validatePort(String port) {
		boolean portIsOk = false;
		try {
			int configPort = Integer.parseInt(port);
			if (0 < configPort && configPort < 65536) {
				portIsOk = true;
			} else {
				Logger.logLine("PORT parameter is not a value valid for a port.", false);
			}
		} catch (NumberFormatException e) {
			Logger.logLine("PORT parameter is not a value valid for a port.", false);
		}
		return portIsOk;
	}

	/**
	 * Open a socket to send data over UDP protocol
	 * 
	 * @param data to send
	 */
	public void sendData(byte[] data) {
		try {
			DatagramPacket request = new DatagramPacket(data, data.length, ipAddress, port);
//			DatagramPacket request = new DatagramPacket(data, data.length, ipAddress,
//					port + (int) (Math.random() * 10));
			this.socket.send(request);

		} catch (IOException e) {
			
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception("Exception in Constructor:  Client: " + outError.toString()));
			Logger.logLine("Exception in Constructor:  Client: " + outError.toString(), true);
			
		} 
	}
	

	static final String IP_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

	public static byte[] getMsgKeyValue(String p37, String value, String type, String nameInterface) {

		Logger.logLine("GET MSG KEY VALUE "+type , true);
//		boolean msg2 = false;
		String key = new String();
//		if (type.equals("B24")) {
//			Base24Ath msgb24Ath = new Base24Ath(null);
//			try {
//				msgb24Ath.fromMsg(Transform.fromHexToBin(value).getBytes());
//				key = "V2" + type + msgb24Ath.getField(Iso8583.Bit._003_PROCESSING_CODE)
//						+ msgb24Ath.getField(Iso8583.Bit._011_SYSTEMS_TRACE_AUDIT_NR)
//						+ msgb24Ath.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR) + nameInterface;
//				String msglog = msgb24Ath.toString();
//				String msghex = value;
//				key = key + "|" + msglog + "|" + msghex;
//				msg2 = true;
//			} catch (XPostilion e) {
//				GenericInterface.getLogger().logLine(Utils.getStringMessageException(e));
//			}
//
//		}
//		if (type.equals("ISO")) {
//			Iso8583Post msgIso = new Iso8583Post();
//			try {
//				msgIso.fromMsg(Transform.fromHexToBin(value).getBytes());
//				key = "V2" + type + msgIso.getField(Iso8583.Bit._003_PROCESSING_CODE)
//						+ msgIso.getField(Iso8583.Bit._011_SYSTEMS_TRACE_AUDIT_NR)
//						+ msgIso.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR) + nameInterface;
//				String msglog = msgIso.toString();
//				String msghex = value;
//				key = key + "|" + msglog + "|" + msghex;
//				msg2 = true;
//				GenericInterface.getLogger().logLine("msg2 "+msg2);
//			} catch (XPostilion e) {
//				GenericInterface.getLogger().logLine(Utils.getStringMessageException(e));
//			}
//
//		}
//		GenericInterface.getLogger().logLine(key);
//		if (msg2) {
//			sendDataFixed(key.getBytes());
//		}
		key = type + ":" + nameInterface + "_" + p37 + "_"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ":";
		String lKey = String.valueOf(key.length());
		String llKey = String.valueOf(lKey.length());

		String lValue = String.valueOf(value.length());
		String llValue = String.valueOf(lValue.length());

		String msg = llKey + lKey + key + llValue + lValue + value;
		return (Base64.getEncoder().encodeToString(msg.getBytes())).getBytes();
	}

}
