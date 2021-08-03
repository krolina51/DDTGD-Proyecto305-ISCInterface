package postilion.realtime.iscinterface.security.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import monitor.core.util.Logger;

import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;

public class SSLContextConfig {
	
	private SSLGeneralConfig config_ = SSLGeneralConfig.getInstance();
	private TrustManager[] trustAllCerts = null;
	private String keymanageralgorithm = null;

	public SSLContext setupSslContext() {
		SSLContext sslContext = null;
		boolean trustall = false;
		try {
			String keyStorePath = config_.getKEYSTOREPATH();
			String trustStorePath = config_.getTRUSTSTOREPATH();
			String keyStorePw = config_.getKEYSTOREPW();
			String trustStorePw = config_.getTRUSTSTOREPW();
			String keyPass = config_.getKEYPASS();
			String trustAllCertificate = config_.getTrustAllCertificate();
			String keystoreType = config_.getKeystoreType();
			keymanageralgorithm = config_.getKeymanageralgorithm();
			trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };
			if (trustAllCertificate.equalsIgnoreCase("True")) {
				trustall = true;
			}
			if (keystoreType.equalsIgnoreCase("JKS"))
				sslContext = initializeSSLContext(keyStorePath, keyStorePw, trustStorePath, trustStorePw, keyPass,
						trustall);
			else
				sslContext = initializeSSLContextP12Cert(keyStorePath, keyStorePw, trustStorePath, trustStorePw,
						keyPass, trustall);
		} catch (Exception exp) {
			Logger.logLine("ConfigException exception occurred while reading the config file : " + exp.getMessage());
			exp.printStackTrace();
		}
		return sslContext;
	}

	/**
	 * 
	 * @param keyStorePath
	 * @param pwKeyStore
	 * @param trustStorePath
	 * @param pwTrustStore
	 * @param keyPass
	 * @return
	 * @throws Exception
	 */
	private SSLContext initializeSSLContext(final String keyStorePath, final String pwKeyStore,
			final String trustStorePath, final String pwTrustStore, final String keyPass, final boolean trustall) {
		Logger.logLine(" In initializeSSLContext");
		char[] keyStorePw = pwKeyStore.toCharArray();
		char[] trustStorePw = pwTrustStore.toCharArray();
		char[] keyPw = keyPass.toCharArray();
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextInt();
		KeyStore ks = null;
		try {
			ks = KeyStore.getInstance("JKS");
		} catch (KeyStoreException exp) {
			Logger.logLine("KeyStoreException exception occurred while reading the config file : " + exp.getMessage());
		}
		FileInputStream fis = null;
		try {
			try {
				fis = new FileInputStream(keyStorePath);
			} catch (FileNotFoundException exp) {
				Logger.logLine("FileNotFoundException exception occurred " + exp.getMessage());
			}
			try {
				ks.load(fis, keyStorePw);
			} catch (NoSuchAlgorithmException exp) {
				Logger.logLine("NoSuchAlgorithmException exception occurred " + exp.getMessage());
			} catch (CertificateException exp) {
				Logger.logLine("CertificateException exception occurred " + exp.getMessage());
			} catch (IOException exp) {
				Logger.logLine("CertificateException exception occurred " + exp.getMessage());
			}
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException exp) {
					Logger.logLine("IOException exception occurred " + exp.getMessage());
				}
		}
		Logger.logLine("[initializeSSLContext] KMF keystorepw loaded.");
		KeyManagerFactory kmf = null;
		try {
			kmf = KeyManagerFactory.getInstance(keymanageralgorithm);
		} catch (NoSuchAlgorithmException exp) {
			Logger.logLine("IOException exception occurred " + exp.getMessage());
		}
		try {
			kmf.init(ks, keyPw);
		} catch (UnrecoverableKeyException exp) {
			Logger.logLine("UnrecoverableKeyException exception occurred " + exp.getMessage());
		} catch (KeyStoreException exp) {
			Logger.logLine("KeyStoreException exception occurred " + exp.getMessage());
		} catch (NoSuchAlgorithmException exp) {
			Logger.logLine("NoSuchAlgorithmException exception occurred " + exp.getMessage());
		}
		Logger.logLine("[initializeSSLContext] KMF init done.");
		KeyStore ts = null;
		try {
			ts = KeyStore.getInstance("JKS");
		} catch (KeyStoreException exp) {
			Logger.logLine("NoSuchAlgorithmException exception occurred " + exp.getMessage());
		}
		FileInputStream tfis = null;
		SSLContext sslContext = null;
		try {
			tfis = new FileInputStream(trustStorePath);
			ts.load(tfis, trustStorePw);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(keymanageralgorithm);
			tmf.init(ts);
			Logger.logLine("[initializeSSLContext] Truststore initialized");
			sslContext = SSLContext.getInstance("TLS");
			if (trustall)
				sslContext.init(kmf.getKeyManagers(), trustAllCerts, secureRandom);
			else
				sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
		} catch (NoSuchAlgorithmException exp) {
			Logger.logLine("NoSuchAlgorithmException exception occurred " + exp.getMessage());
		} catch (CertificateException exp) {
			Logger.logLine("NoSuchAlgorithmException exception occurred " + exp.getMessage());
		} catch (IOException exp) {
			Logger.logLine("NoSuchAlgorithmException exception occurred " + exp.getMessage());
		} catch (KeyStoreException exp) {
			Logger.logLine("NoSuchAlgorithmException exception occurred " + exp.getMessage());
		} catch (KeyManagementException exp) {
			Logger.logLine("NoSuchAlgorithmException exception occurred " + exp.getMessage());
		} finally {
			if (tfis != null)
				try {
					tfis.close();
				} catch (IOException exp) {
					Logger.logLine("NoSuchAlgorithmException exception occurred " + exp.getMessage());
				}
		}
		if ((sslContext == null)) {
			Logger.logLine("[initializeSSLContext] sslContext is null");
			System.exit(-1);
		}
		return sslContext;
	}

	/**
	 * 
	 * @param keyStorePath
	 * @param pwKeyStore
	 * @param trustStorePath
	 * @param pwTrustStore
	 * @param keyPass
	 * @return
	 * @throws Exception
	 */
	private SSLContext initializeSSLContextP12Cert(final String keyStorePath, final String pwKeyStore,
			final String trustStorePath, final String pwTrustStore, final String keyPass, final boolean trustall) {
		Logger.logLine("In initializeSSLContextP12Cert");
		SSLContext sslContext = null;
		String keystore = keyStorePath;
		String keystorepass = pwKeyStore;
		String truststore = trustStorePath;
		String truststorepass = pwTrustStore;
		try {
			KeyStore clientStore = KeyStore.getInstance("PKCS12");
			clientStore.load(new FileInputStream(keystore), keystorepass.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(keymanageralgorithm);
			kmf.init(clientStore, keystorepass.toCharArray());
			KeyManager[] kms = kmf.getKeyManagers();
			KeyStore trustStore = KeyStore.getInstance("JKS");
			trustStore.load(new FileInputStream(truststore), truststorepass.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(keymanageralgorithm);
			tmf.init(trustStore);
			TrustManager[] tms = tmf.getTrustManagers();
			
			sslContext = SSLContext.getInstance("TLSv1.2");
			if (trustall)
				sslContext.init(kms, trustAllCerts, new SecureRandom());
			else
				sslContext.init(kms, tms, new SecureRandom());
		} catch (NoSuchAlgorithmException exp) {
			Logger.logLine("NoSuchAlgorithmException exception occurred " + exp.getMessage());
		} catch (CertificateException exp) {
			Logger.logLine("CertificateException exception occurred " + exp.getMessage());
		} catch (IOException exp) {
			Logger.logLine("IOException occurred while reading the key file  " + exp.getMessage());
		} catch (KeyStoreException exp) {
			Logger.logLine("KeyStoreException exception occurred " + exp.getMessage());
		} catch (KeyManagementException exp) {
			Logger.logLine("KeyManagementException exception occurred " + exp.getMessage());
		} catch (UnrecoverableKeyException exp) {
			Logger.logLine("UnrecoverableKeyException exception occurred " + exp.getMessage());
		}
		if ((sslContext == null)) {
			Logger.logLine("[initializeSSLContext] sslContext is null");
			Logger.logLine("[initializeSSLContext] verify ssl config");
			Logger.logLine("MyREST application exit with status code -1");
//System.exit(-1);
		}
		Logger.logLine("[initializeSSLContextP12Cert] Truststore and KeyStore initialized");
		return sslContext;
	}
}
