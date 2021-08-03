package postilion.realtime.iscinterface.security.config;

import monitor.core.util.Logger;

public final class SSLGeneralConfig {
	private String KEYSTOREPATH = null;
	private String TRUSTSTOREPATH = null;
	private String KEYSTOREPW = null;
	private String TRUSTSTOREPW = null;
	private String KEYPASS = null;
	private String HTTPS_SERV_URL = null;
	private String trustAllCertificate = "false";// DEFAULT VALUE
	private String keystoreType = "JKS";// DEFAULT VALUE
	private String regex = null;
	private String keymanageralgorithm = null;
	private int mqreadinterval = 1;
	private int httpsfialureinterval = 5;
	private int prodissueinterval = 1;
	
	private static SSLGeneralConfig myinstance = null;

	public static SSLGeneralConfig getInstance() {
		Logger.logLine("in ApplicationConfig getInstance");
		if (myinstance == null) {
			myinstance = new SSLGeneralConfig();
		}
		return myinstance;
	}

	private SSLGeneralConfig() {
	}

	public String getKEYSTOREPATH() {
		return KEYSTOREPATH;
	}

	public void setKEYSTOREPATH(String kEYSTOREPATH) {
		KEYSTOREPATH = kEYSTOREPATH;
	}

	public String getTRUSTSTOREPATH() {
		return TRUSTSTOREPATH;
	}

	public void setTRUSTSTOREPATH(String tRUSTSTOREPATH) {
		TRUSTSTOREPATH = tRUSTSTOREPATH;
	}

	public String getKEYSTOREPW() {
		return KEYSTOREPW;
	}

	public void setKEYSTOREPW(String kEYSTOREPW) {
		KEYSTOREPW = kEYSTOREPW;
	}

	public String getTRUSTSTOREPW() {
		return TRUSTSTOREPW;
	}

	public void setTRUSTSTOREPW(String tRUSTSTOREPW) {
		TRUSTSTOREPW = tRUSTSTOREPW;
	}

	public String getKEYPASS() {
		return KEYPASS;
	}

	public void setKEYPASS(String kEYPASS) {
		KEYPASS = kEYPASS;
	}

	public String getHTTPS_SERV_URL() {
		return HTTPS_SERV_URL;
	}

	public void setHTTPS_SERV_URL(String hTTPS_SERV_URL) {
		HTTPS_SERV_URL = hTTPS_SERV_URL;
	}

	public String getTrustAllCertificate() {
		return trustAllCertificate;
	}

	public void setTrustAllCertificate(String trustAllCertificate) {
		this.trustAllCertificate = trustAllCertificate;
	}

	public String getKeystoreType() {
		return keystoreType;
	}

	public void setKeystoreType(String keystoreType) {
		this.keystoreType = keystoreType;
	}

	public String getKeymanageralgorithm() {
		return keymanageralgorithm;
	}

	public void setKeymanageralgorithm(String keymanageralgorithm) {
		this.keymanageralgorithm = keymanageralgorithm;
	}

	public int getMqreadinterval() {
		return mqreadinterval;
	}

	public void setMqreadinterval(int mqreadinterval) {
		this.mqreadinterval = mqreadinterval;
	}

	public int getHttpsfialureinterval() {
		return httpsfialureinterval;
	}

	public void setHttpsfialureinterval(int httpsfialureinterval) {
		this.httpsfialureinterval = httpsfialureinterval;
	}

	public int getProdissueinterval() {
		return prodissueinterval;
	}

	public void setProdissueinterval(int prodissueinterval) {
		this.prodissueinterval = prodissueinterval;
	}

	public void setREGEX(String regex) {
		this.regex = regex;
	}

	public String getREGEX() {
		return this.regex;
	}

	public static SSLGeneralConfig getMyinstance() {
		return myinstance;
	}

	public static void setMyinstance(SSLGeneralConfig myinstance) {
		SSLGeneralConfig.myinstance = myinstance;
	}
}
