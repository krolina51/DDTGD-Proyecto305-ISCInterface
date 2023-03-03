package postilion.realtime.iscinterface.crypto;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.ISCInterfaceCB;
import postilion.realtime.iscinterface.crypto.HSMDirectorBuild;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.sdk.crypto.CryptoCfgManager;
import postilion.realtime.sdk.crypto.CryptoManager;
import postilion.realtime.sdk.crypto.DesKek;
import postilion.realtime.sdk.crypto.DesKwp;
import postilion.realtime.sdk.crypto.XCrypto;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.util.convert.Transform;

@SuppressWarnings("deprecation")
public class Crypto {
	
	HSMDirectorBuild hsmComm;
	CryptoCfgManager crypcfgman;
	DesKek klk1 = null;
	DesKek klk2 = null;
	DesKek klkM = null;
	private static final String keyStatic = "0123456789ABCDEF";
	
	private static postilion.realtime.sdk.ipc.SecurityManager SEC_MANAGER;

	static {
		try {
			SEC_MANAGER = new postilion.realtime.sdk.ipc.SecurityManager();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Crypto(boolean log) {
		this.hsmComm = new HSMDirectorBuild();
		Logger.logLine("ip Atalla:" + ISCInterfaceCB.ipACryptotalla, log);
		Logger.logLine("port Atalla:" + ISCInterfaceCB.portACryptotalla, log);
		this.hsmComm.openConnectHSM(ISCInterfaceCB.ipACryptotalla, ISCInterfaceCB.portACryptotalla);
		
		
	}
	
	public PinPad initPinPad(boolean log) throws XCrypto {
		PinPad pinPad = new PinPad();
		
		try {
			// Leer Criptogramas PASO 1
			CryptoCfgManager crypcfgman = CryptoManager.getStaticConfiguration();
			this.klk1 = crypcfgman.getKek("HKPPKLK1");
			this.klk2 = crypcfgman.getKek("HKPPKLK2");
			this.klkM = crypcfgman.getKek("HKEYPPA2");
		
			Logger.logLine("klkM:" + klkM.getValueUnderKsk(), log);
			// Comando 10 GENERATE WORKKEY PASO 2 Documento
			String command10 = "<10#1ADNE000#" + klkM.getValueUnderKsk() + "#D#>";
			Logger.logLine("command10:" + command10, log);
			String resCommand10[] = this.hsmComm.sendCommand(command10, ISCInterfaceCB.ipACryptotalla, ISCInterfaceCB.portACryptotalla).split("#");
			Logger.logLine("resCommand10[1]:" + Arrays.toString(resCommand10), log);
			
			// Comando 1A EXPORT WORKING KEY TO NON-AKB FORMAT PASO 3 Documento
			String command1A = "<1A#0#" + klkM.getValueUnderKsk() + "#" + resCommand10[1] + "#>";
			Logger.logLine("command1A:" + command1A, log);
			String resCommand1A[] = this.hsmComm.sendCommand(command1A, ISCInterfaceCB.ipACryptotalla, ISCInterfaceCB.portACryptotalla).split("#");
			Logger.logLine("resCommand1A[1].substring(0,16):" + Arrays.toString(resCommand1A), log);
		
			// Comando 97 ENCRYPT/DECRYPT DATA parte izquierda PASO 5 Documento
			String command97 = "<97#E#6#" + klk1.getValueUnderKsk() + "#D#U#16#" + resCommand1A[1].substring(0,16) + "#2#>";
			Logger.logLine("command97:" + command97, log);
			String resCommand97[] = this.hsmComm.sendCommand(command97, ISCInterfaceCB.ipACryptotalla, ISCInterfaceCB.portACryptotalla).split("#");
			Logger.logLine("resCommand97[8]:" + Arrays.toString(resCommand97), log);
			
			// Realizando XOR PASO 6 documento
			Logger.logLine("xor1:" + resCommand1A[1].substring(16), log);
			Logger.logLine("xor2:" + resCommand97[8], log);
			String Xor = byteArraytoHexString(xorByteArray(Transform.fromHexStringToHexData(resCommand1A[1].substring(16)),
					Transform.fromHexStringToHexData(resCommand97[8])));
			Logger.logLine("Xor:" + Xor, log);
			
			// Comando 97 ENCRYPT/DECRYPT DATA con XOR PASO 7 Documento
			String command97PIzq = "<97#E#6#" + klk1.getValueUnderKsk() + "#D#U#16#" + Xor + "#2#>";
			Logger.logLine("command97PIzq:" + command97PIzq, log);
			String resCommand97PIzq[] = this.hsmComm.sendCommand(command97PIzq, ISCInterfaceCB.ipACryptotalla, ISCInterfaceCB.portACryptotalla).split("#");
			Logger.logLine("resCommand97PIzq:" + Arrays.toString(resCommand97PIzq), log);
			
			// Comando 97 ENCRYPT/DECRYPT DATA con resultado anterior PASO 8 Documento
			String command97PIzq2 = "<97#E#6#" + klk1.getValueUnderKsk() + "#D#U#16#" + resCommand97PIzq[8] + "#2#>";
			Logger.logLine("command97PIzq2:" + command97PIzq2, log);
			String resCommand97PIzq2[] = this.hsmComm.sendCommand(command97PIzq2, ISCInterfaceCB.ipACryptotalla, ISCInterfaceCB.portACryptotalla).split("#");
			Logger.logLine("resCommand97PIzq2:" + Arrays.toString(resCommand97PIzq2), log);
			
			// Comando 97 ENCRYPT/DECRYPT DATA parte derecha PASO 9 Documento
			String command97PDer = "<97#D#6#" + klk2.getValueUnderKsk() + "#D#U#16#" + resCommand97PIzq2[8] + "#2#>";
			Logger.logLine("command97PDer:" + command97PDer, log);
			String resCommand97PDer[] = this.hsmComm.sendCommand(command97PDer, ISCInterfaceCB.ipACryptotalla, ISCInterfaceCB.portACryptotalla).split("#");
			Logger.logLine("resCommand97PDer:" + Arrays.toString(resCommand97PDer), log);
			
			// Comando 97 ENCRYPT/DECRYPT DATA parte derecha PASO 10 Documento
			String command97PIzq3 = "<97#E#6#" + klk1.getValueUnderKsk() + "#D#U#16#" + resCommand97PDer[8] + "#2#>";
			Logger.logLine("command97PIzq3:" + command97PIzq3, log);
			String resCommand97PIzq3[] = this.hsmComm.sendCommand(command97PIzq3, ISCInterfaceCB.ipACryptotalla, ISCInterfaceCB.portACryptotalla).split("#");
			Logger.logLine("resCommand97PIzq3:" + Arrays.toString(resCommand97PIzq3), log);
			
			// Encripcion DES resultado Comando 1A PASO 11 Documento
			String encryptDES1A = encryptionDES(keyStatic, resCommand1A[1]);

			// Encripcion DES resultado Comando97PIzq3 PASO 12 Documento
			String encryptDES97 = encryptionDES(keyStatic, resCommand97PIzq3[8]);
			
			// Almacena llave SND en PinPad PASO 13 Documento
			pinPad.setKey_ini_snd(encryptDES1A+encryptDES97);
			
			// Encripcion DES resultado comando10 PASO 14 Documento
			String[] resCom10 = resCommand10[1].split(",");
			String encryptDES10 = encryptionDES(keyStatic, resCom10[1]+resCom10[2]);
			
			// Almacena Llave INI en PinPad PASO 15 Documento
			pinPad.setKey_ini("1ADNE000"+encryptDES10);
			pinPad.setResponseInit(resCommand1A[1]+"00"+resCommand97PIzq3[8]);
			pinPad.setError(false);
		} catch(XCrypto ex) {
			EventRecorder.recordEvent(
					new Exception("Crypyo: " + ex.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					Utils.getStringMessageException(ex) }));
		} catch (Exception e) {
			pinPad.setError(true);
			EventRecorder.recordEvent(
					new Exception("Crypyo: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					Utils.getStringMessageException(e) }));
			e.printStackTrace();
		} finally {
			this.hsmComm.closeConnectHSM();
		}
		return pinPad;
	}
	
	public PinPad exchangePinPad(boolean log, PinPad pinpad) throws XCrypto {
		
		try {
			
			// Desncripta Key_ini sin Header PASO 2 DOCUMENTO
			Logger.logLine("Key_ini:" + pinpad.getKey_ini(), log);
			String decryptDESKeyIni = decryptionDES(keyStatic, pinpad.getKey_ini().substring(8));
			Logger.logLine("decryptDESKeyIni:" + decryptDESKeyIni, log);
			
			// Arma criptograma con Header del Key_ini, resultado de desencripcion 
			String key = pinpad.getKey_ini().substring(0,8)+","+decryptDESKeyIni.substring(0,decryptDESKeyIni.length()-16)+","+decryptDESKeyIni.substring(decryptDESKeyIni.length()-16);
			Logger.logLine("key Criptograma:" + key, log);

			// Comando 10 GENERATE WORKKEY PASO 3 Documento
			String command10 = "<10#1PUNE000#" + key + "#S#^0812991#>";
			Logger.logLine("command10:" + command10, log);
			String resCommand10[] = this.hsmComm.sendCommand(command10, ISCInterfaceCB.ipACryptotalla, ISCInterfaceCB.portACryptotalla).split("#");
			Logger.logLine("resCommand10[1]:" + Arrays.toString(resCommand10), log);
			
			// Comando 1A EXPORT WORKING KEY TO NON-AKB FORMAT PASO 4 Documento
			String command1A = "<1A#0#" + key + "#" + resCommand10[1] + "#^0812991#>";
			Logger.logLine("command1A:" + command1A, log);
			String resCommand1A[] = this.hsmComm.sendCommand(command1A, ISCInterfaceCB.ipACryptotalla, ISCInterfaceCB.portACryptotalla).split("#");
			Logger.logLine("resCommand1A[1].substring(0,16):" + Arrays.toString(resCommand1A), log);
		
			// Encripta Resultado comando 10 sin Header PASO 5 DOCUMENTO
			Logger.logLine("resCommand10[1].replaceAll(\",\", \"\").substring(8):" + resCommand10[1].replaceAll(",", "").substring(8), log);
			String encryptDESCommand20 = encryptionDES(keyStatic, resCommand10[1].replaceAll(",", "").substring(8));
			
			// Almacena en llave Key_exc PASO 6 DOCUMENTO
			pinpad.setKey_exc("1PUNE000"+encryptDESCommand20);
			
			// Encripta resultado comando 1A DES PASO 7 DOCUMENTO
			String encryptDESCommand2A = encryptionDES(keyStatic, resCommand1A[1]);
			
			// Almacena en llave Key_exc_snd PASO 8 DOCUMENTO
			pinpad.setKey_exc_snd(encryptDESCommand2A);
			
			pinpad.setResponseExc(resCommand1A[1]);
			pinpad.setError(false);
		}  catch (Exception e) {
			pinpad.setError(true);
			EventRecorder.recordEvent(
					new Exception("Crypyo: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					Utils.getStringMessageException(e) }));
			e.printStackTrace();
		} finally {
			this.hsmComm.closeConnectHSM();
		}
		return pinpad;
	}
	
	/**
	 * Hace un xor byte por byte de los arreglos, los arreglos debe ser de igual
	 * tamaño
	 * 
	 * @param array1
	 * @param array2
	 * @return arreglo de byte con el resultado de la operacion
	 */
	public static byte[] xorByteArray(byte[] array1, byte[] array2) {
		byte[] xorResult = new byte[array1.length];
		for (int i = 0; i < array1.length; i++) {
			xorResult[i] = (byte) (array2[i] ^ array1[i]);
		}
		return xorResult;
	}
	
	/**
	 * Pasa un arreglo de byte a un string en hexadecimal
	 * 
	 * @param data arreglo a traducir
	 * @return String en hexadecimal que representa el arreglo de bytes recibido.
	 */
	public static String byteArraytoHexString(byte[] data) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			hexString.append(
					(Integer.toHexString(0xFF & data[i]).length() == 1) ? "0" + Integer.toHexString(0xFF & data[i])
							: Integer.toHexString(0xFF & data[i]));
		}
		return hexString.toString().toUpperCase();
	}
	
	
	/**
	 * Encripta la información suministrada con la llave en claro suministrada, con
	 * algoritmo DES - ECB - sin relleno.
	 * 
	 * @param rawKey llave en claro - representación hexadecimal.
	 * @param data   Información en claro - representación hexadecimal
	 * @return infromación encriptada - representación hexadecimal.
	 */
	public static String encryptionDES(String keyStatic, String data) {
		String cipherText = "";
		try {
			DESKeySpec keySpec = new DESKeySpec(Transform.fromHexStringToHexData(keyStatic));

			SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyfactory.generateSecret(keySpec);


			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] plainTextBytes = cipher.doFinal(Transform.fromHexStringToHexData(data));

			cipherText = byteArraytoHexString(plainTextBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cipherText;
	}
	
	/**
	 * Desencripta la información suministrada con la llave en claro suministrada, con
	 * algoritmo DES - ECB - sin relleno.
	 * 
	 * @param rawKey llave en claro - representación hexadecimal.
	 * @param data   Información en claro - representación hexadecimal
	 * @return infromación encriptada - representación hexadecimal.
	 */
	public static String decryptionDES(String llave, String data) {
		String cipherText = "";
		try {
			DESKeySpec keySpec = new DESKeySpec(Transform.fromHexStringToHexData(llave));

			SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyfactory.generateSecret(keySpec);


			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key);

			byte[] plainTextBytes = cipher.doFinal(Transform.fromHexStringToHexData(data));

			cipherText = byteArraytoHexString(plainTextBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cipherText;
	}

}
