package postilion.realtime.iscinterface.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.genericinterface.translate.bitmap.Base24Ath;
import postilion.realtime.iscinterface.ISCInterfaceCB;
import postilion.realtime.iscinterface.auxiliar.TransferAux;
import postilion.realtime.iscinterface.crypto.Crypto;
import postilion.realtime.iscinterface.crypto.PinPad;
import postilion.realtime.iscinterface.database.DBHandler;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.message.ISCReqMessage;
import postilion.realtime.iscinterface.message.ISCResInMsg;
import postilion.realtime.iscinterface.message.ISCResMessage;
import postilion.realtime.iscinterface.web.HttpCryptoServ;
import postilion.realtime.iscinterface.web.model.Field;
import postilion.realtime.iscinterface.web.model.Homologation;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.iscinterface.web.model.WholeTransSetting;
import postilion.realtime.library.common.InitialLoadFilter;
import postilion.realtime.library.common.model.ResponseCode;
import postilion.realtime.sdk.crypto.CryptoCfgManager;
import postilion.realtime.sdk.crypto.CryptoManager;
import postilion.realtime.sdk.crypto.DesKwp;
import postilion.realtime.sdk.crypto.XCrypto;
import postilion.realtime.sdk.env.calendar.BusinessCalendar;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.ipc.SecurityManager;
import postilion.realtime.sdk.ipc.XEncryptionKeyError;
import postilion.realtime.sdk.message.IMessage;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.message.bitmap.XFieldUnableToConstruct;
import postilion.realtime.sdk.util.DateTime;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class Utils {

	private static final String EBCDIC_ENCODING = "IBM-1047";
	private static final String HEX_CHARS = "0123456789abcdef";
	private static final String KEY_VALUES_REGEX = "(\\w*)=(.*)";
	
	
	/** Instancia de SecurityManager **/
	public static SecurityManager securityManager = null;

	static {
		try {
			securityManager = new SecurityManager();
		} catch (Exception e) {
			System.exit(1);
		}
	}
	
	public static String getHashPanCNB(String pan) throws Exception {
		return securityManager.hashToString(pan, SecurityManager.DigestAlgorithm.HMAC_SHA1, true);
	}
	
	/**
	 * Obtiene el id de cuenta en claro.
	 * 
	 * @param object El id de la cuenta cifrado.
	 * @return Id de cuenta en claro.
	 * @throws XEncryptionKeyError En caso de error.
	 */
	public static String getClearAccount(String encryptedAccId) throws XEncryptionKeyError {
		return securityManager.decryptToString(encryptedAccId);
	}

	public static String ebcdicToAscii(String strHexEbcdic) {
		String strAscii = "";

		try {
			byte[] ebcdicByteArray = new BigInteger(strHexEbcdic, 16).toByteArray();
			strAscii = new String(ebcdicByteArray, EBCDIC_ENCODING);
		} catch (Exception e) {
			strAscii = "";
		}

		return strAscii;
	}

	public static String asciiToEbcdic(String strAscii) {
		String strEbcdicHex = "";

		try {
			byte[] ebcdicByteArray = strAscii.getBytes(EBCDIC_ENCODING);
			strEbcdicHex = asHex(ebcdicByteArray);
		} catch (Exception e) {
			strEbcdicHex = "";
		}

		return strEbcdicHex;
	}

	public static String asHex(byte[] buf) {
		char[] hexCharsArray = HEX_CHARS.toCharArray();
		char[] chars = new char[2 * buf.length];
		for (int i = 0; i < buf.length; ++i) {
			chars[2 * i] = hexCharsArray[(buf[i] & 0xF0) >>> 4];
			chars[2 * i + 1] = hexCharsArray[buf[i] & 0x0F];
		}
		return new String(chars);
	}

	public static HashMap<String, String> stringToHashmap(String stringList) {
		Pattern pattern = Pattern.compile(KEY_VALUES_REGEX);
		Matcher matcher = pattern.matcher(stringList);
		HashMap hm = new HashMap();
		while (matcher.find()) {
			String key = matcher.group(1);
			String value = matcher.group(2);
			hm.put(key, value.trim());
		}
		return hm;
	}

	public static HashMap<String, String> putStringIntoHashmap(HashMap hm, String stringList) {
		Pattern pattern = Pattern.compile(KEY_VALUES_REGEX);
		Matcher matcher = pattern.matcher(stringList);
		while (matcher.find()) {
			String key = matcher.group(1);
			String value = matcher.group(2);
			if(key.toUpperCase().contains("MOVIMIE")) {
				hm.put(key, value);
			}
			else {
				hm.put(key, value.trim());
			}
		}
		return hm;
	}

	public static String hashmapToString(HashMap<String, String> hm) {

		String result;
		String hmKey;
		String hmValue;
		StringBuilder message = new StringBuilder();

		for (Map.Entry i : hm.entrySet()) {
			hmKey = i.getKey().toString();
			try {
				hmValue = i.getValue().toString();
			} catch (Exception ex) {
				hmValue = "";
			}
			message.append(hmKey + "=" + hmValue + "\n");
		}

		result = message.toString();

		return result;
	}

	public static String padLeft(String inputString, String padChar, int length) {
		if (inputString.length() >= length) {
			return inputString;
		}
		StringBuilder sb = new StringBuilder();
		while (sb.length() < length - inputString.length()) {
			sb.append(padChar);
		}
		sb.append(inputString);

		return sb.toString();
	}

	/**
	 * Recupera los valores del cuerpo (variable) del msj de respuesta en un mapa
	 * NOMBRE CAMPO >> VALOR CAMPO
	 * 
	 * @param varBody     String cuerpo msj
	 * @param regexVar    Expresiï¿½n regular para extraer campos
	 * @param outTemplate Expresiï¿½n regular valores de salida
	 * @param delimiter   delimitador de campos en el body
	 * @return
	 */
//	public static HashMap<String, String> getBodyInnerFields(String varBody, String regexVar, String outTemplate,
//			String delimiter) {
//
//		Logger.logLine("***Recuperando campos del msg variable");
//
//		HashMap<String, String> innerFields;
//		String parteVariableEbcdic = varBody;
//		String parteVariableAscii = Utils
//				.ebcdicToAscii(parteVariableEbcdic.replaceAll(delimiter == null ? "" : delimiter, ""));
//
//		Logger.logLine("BUSQUEDA DE CAMPOS EN RSP BODY   " + parteVariableAscii);
//		Logger.logLine("BUSQUEDA DE CAMPOS EN RSP REGEX  " + regexVar);
//		Logger.logLine("BUSQUEDA DE CAMPOS EN RSP OUTPUT " + outTemplate);
//
//		String parteVariable = parteVariableAscii.replaceAll(regexVar, outTemplate);
//		innerFields = (HashMap<String, String>) Utils.stringToHashmap(parteVariable);
//
//		Logger.logLine("***" + innerFields + "***");
//
//		return innerFields;
//	}

	public static HashMap<String, String> getBodyInnerFields(String varBody, String tranCode, String isCostInq, boolean enableLog) {

		HashMap<String, String> innerFields = new HashMap<String, String>();

		String varBodyHex = UtilidadesMensajeria.asciiToEbcdic(varBody);

		Logger.logLine("***Recuperando campos del msg variable:\n" + varBody + "\n" + varBodyHex + "\n"
				+ UtilidadesMensajeria.ebcdicToAscii(varBody), enableLog);

		//CUPO CREDITO ROTATIVO
		if (tranCode != null && tranCode.equals("AT2I") && isCostInq != null && isCostInq.equals("FALSE")) {
			
			String[] tagx = varBody.split("11C2601D60");
			
			int targetIndex = 0;
			
			String consulResult = "";
			
			for (int i = 0; i < tagx.length; i++) {
				
				if (UtilidadesMensajeria.ebcdicToAscii(tagx[i]).contains("CCCON0")) {
					
					consulResult = UtilidadesMensajeria.ebcdicToAscii(tagx[i+1]);

					break;
				}
				
			}

			Logger.logLine("CONSUL RES:" + consulResult, enableLog);

			Utils.putStringIntoHashmap(innerFields, "P102_1=".concat(consulResult.substring(17, 22)));
			Utils.putStringIntoHashmap(innerFields, "P102_2=".concat(consulResult.substring(22, 37)));

			String saldoT = consulResult.substring(39, 55).replaceAll("\\s", "").replaceAll("\\.", "").replaceAll("\\,",
					"");
			String saldoD = consulResult.substring(58, 74).replaceAll("\\s", "").replaceAll("\\.", "").replaceAll("\\,",
					"");

			String saldos = Pack.resize(saldoT, 15, '0', false);
			saldos = saldos.concat("000000000000000").concat(Pack.resize(saldoD, 15, '0', false));

			innerFields.put("SALDOS", saldos);
			innerFields.put("SALDO_T", saldoT);
			innerFields.put("SALDO_D", saldoD);

			Logger.logLine("SALDOS ROT:" + innerFields.get("SALDOS"), enableLog);

		}
		
		if(tranCode != null && tranCode.equals("AT1I") && isCostInq != null && isCostInq.equals("FALSE")) {
			
			String rspBody = UtilidadesMensajeria.ebcdicToAscii(varBody);
			Logger.logLine("CONSUL RES:" + rspBody, enableLog);
			
			String[] rspFields = rspBody.split("\\s+");	
			
			innerFields.put("TIT_IDEN", rspFields[1]);
			innerFields.put("TIT_TYPE", rspFields[2]);
			innerFields.put("TIT_NOMBRE", rspBody.substring(rspBody.indexOf(rspFields[3])));
			
				
		}

		if (tranCode != null && tranCode.equals("ATCG") && isCostInq != null && isCostInq.equals("FALSE")) {

			String consulResult = UtilidadesMensajeria.ebcdicToAscii(varBody);
			Logger.logLine("CONSUL RES:" + consulResult, enableLog);
			
			innerFields.put("ATCG_ID_TYPE", consulResult.substring(39, 40));
			innerFields.put("ATCG_ID_NR", consulResult.substring(40, 56));
			innerFields.put("ATCG_GIRO_NR", consulResult.substring(56, 74));
			innerFields.put("ATCG_GIRO_AMOUNT", consulResult.substring(74, 89));
			innerFields.put("ATCG_ACCOUNT_TYPE", consulResult.substring(89, 90));
			innerFields.put("ATCG_ACCOUNT_NR", consulResult.substring(90, 100));
			innerFields.put("ATCG_FLAG", consulResult.substring(100, 101));
			
			innerFields.put("ATCG_GIRO_KEY", innerFields.get("ATCG_ID_TYPE").concat(innerFields.get("ATCG_ID_NR").concat(innerFields.get("ATCG_GIRO_NR"))));

//			String saldoT = consulResult.substring(39, 55).replaceAll("\\s", "").replaceAll("\\.", "").replaceAll("\\,", "");

		}

		else {

			for (String s : varBody.split("11C2601D60")) {

				for (Map.Entry<String, String> e : OUTPUT_FIELDS.entrySet()) {
					String temp = Utils.ebcdicToAscii(s).replaceAll(e.getKey(), e.getValue());

					if (temp != null && !temp.equals("") && !temp.equals(" ")) {
						Utils.putStringIntoHashmap(innerFields, temp);
					}

				}

			}
		}


		return innerFields;
	}

	public static List<String> getErrorsFromResponse(String errorRegex, String input) {
		List<String> output = new ArrayList<>();
		Pattern p = Pattern.compile(errorRegex);
		Matcher m = p.matcher(input);
		while (m.find()) {
			output.add(m.group(1));
			Logger.logLine("ERROR::" + m.group(1), false);
		}
		return output;
	}

	public static String list2String(List list, char separator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			sb.append(list.get(i)).append(separator);
		}
		return sb.toString();
	}

	/**************************************************************************************
	 * Construye StreamMessage de prueba con la estructura ISCReqMessage el mismo
	 * podrï¿½ ser enviado a la entidad remota para efectos de prueba
	 * 
	 * @return
	 **************************************************************************************/
	public static ISCReqMessage constructTestStreamReqMsg(String consecutive) {
		ISCReqMessage output = new ISCReqMessage();

		output.setConstantHeaderFields();
		output.putField(ISCReqMessage.Fields._05_H_TRAN_CODE, Transform.fromAsciiToEbcdic("ATWI"));
		output.putField(ISCReqMessage.Fields._06_H_ATM_ID, Transform.fromAsciiToEbcdic("AT07"));
		output.putField(ISCReqMessage.Fields._08_H_TRAN_SEQ_NR, Transform.fromAsciiToEbcdic("2293"));
		output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));
		output.putField(ISCReqMessage.Fields._10_H_TIME, Transform.fromAsciiToEbcdic("150451"));

		output.putField(ISCReqMessage.Fields._VARIABLE_BODY, Utils
				.prepareTestVariableReqBody()/* .replace("\u0011", ".").replace("\u0015", " ").replace("\u0007", ".") */);

		Logger.logLine("DUMMY ISC:" + Transform.fromBinToHex(output.getTotalString()), false);

		return output;
	}

	public static ISCReqMessage prepareMessageHeader(Iso8583Post inputMsg, String tranType, String hour4Check,
			boolean isNextDay, ISCInterfaceCB instance) throws Exception {

		ISCReqMessage msgHeaderSetted = new ISCReqMessage();
		msgHeaderSetted.setConstantHeaderFields();

		msgHeaderSetted.putField(ISCReqMessage.Fields._06_H_ATM_ID,
				Transform.fromAsciiToEbcdic(inputMsg.getStructuredData().get(SEQ_TERMINAL).split(",")[0].trim()));

		msgHeaderSetted.putField(ISCReqMessage.Fields._08_H_TRAN_SEQ_NR,
				Transform.fromAsciiToEbcdic(inputMsg.getStructuredData().get(SEQ_TERMINAL).split(",")[1].trim()));

		msgHeaderSetted.putField(ISCReqMessage.Fields._10_H_TIME,
				Transform.fromAsciiToEbcdic(inputMsg.getField(Iso8583Post.Bit._012_TIME_LOCAL)));

		Logger.logLine("[Utils][prepareMessageHeader]:" + tranType, false);

		switch (tranType) {
		case TT_WITHDRAWAL:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					inputMsg.getField(Iso8583Post.Bit._002_PAN).substring(0, 6).equals("450942")
							? Transform.fromAsciiToEbcdic("ATWV")
							: Transform.fromAsciiToEbcdic("ATWI"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, false);
			break;
		case TT_WITHDRAWAL_CB_ATTF:
		case TT_TRANSFER_CB_ATTF:
		case TT_DEPOSIT_CB_ATTF:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					Transform.fromAsciiToEbcdic(Constant.Misce.ATTF));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, false);
			break;
		case TT_WITHDRAWAL_CB_ATTC:
		case TT_TRANSFER_CB_ATTC:
		case TT_DEPOSIT_CB_ATTC:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					Transform.fromAsciiToEbcdic(Constant.Misce.ATTC));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, false);
			break;
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_TRANSFER_CB_ATTD:
		case TT_DEPOSIT_CB_ATTD:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					Transform.fromAsciiToEbcdic(Constant.Misce.ATTD));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, false);
			break;
		case TT_GOOD_N_SERVICES:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					inputMsg.getField(Iso8583Post.Bit._002_PAN).substring(0, 6).equals("450942")
							? Transform.fromAsciiToEbcdic("POWV")
							: Transform.fromAsciiToEbcdic("POWI"));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, false);
			break;
		case TT_REVERSE:
		case TT_REP_REVERSE:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					inputMsg.getField(Iso8583Post.Bit._002_PAN).substring(0, 6).equals("450942")
							? Transform.fromAsciiToEbcdic("ATWV")
							: Transform.fromAsciiToEbcdic("ATWI"));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("188"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, true);
			break;
		case TT_REVERSE_CB_ATTF:
		case TT_REP_REVERSE_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
		case TT_REV_TRANSFER_CB_ATTF:
		case TT_REV_REP_TRANSFER_CB_ATTF:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					Transform.fromAsciiToEbcdic(Constant.Misce.ATTF));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("188"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, true);
			break;
		case TT_REVERSE_CB_ATTC:
		case TT_REP_REVERSE_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
		case TT_REV_TRANSFER_CB_ATTC:
		case TT_REV_REP_TRANSFER_CB_ATTC:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					Transform.fromAsciiToEbcdic(Constant.Misce.ATTC));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("188"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, true);
			break;
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTD:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					Transform.fromAsciiToEbcdic(Constant.Misce.ATTD));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("188"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, true);
			break;
		case TT_REVERSE_GNS:
		case TT_REP_REVERSE_GNS:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE, Transform.fromAsciiToEbcdic("POWI"));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("188"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, true);
			break;
		case TT_COST_INQUIRY:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					inputMsg.getField(Iso8583Post.Bit._002_PAN).substring(0, 6).equals("450942")
							? Transform.fromAsciiToEbcdic("ATWV")
							: Transform.fromAsciiToEbcdic("ATWI"));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("001"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, false);
			break;
		case TT_BALANCE_INQUIRY_CB:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE, Transform.fromAsciiToEbcdic("ATCO"));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("040"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, false);
			break;
		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_MIXT:
		case TT_PAYMENT_OBLIG_CB_DEBIT:
		case TT_PAYMENT_OBLIG_CB_MIXT:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE, Transform.fromAsciiToEbcdic("ATPS"));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, false);
			break;
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE, Transform.fromAsciiToEbcdic("ATPS"));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("188"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, true);
			break;
		case TT_PAYMENT_CB_CREDIT:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE, Transform.fromAsciiToEbcdic("ATPF"));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, false);
			break;
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE, Transform.fromAsciiToEbcdic("ATPF"));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("188"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, true);
			break;
		case TT_CARD_PAYMENT:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					Transform.fromAsciiToEbcdic(Constant.Misce.ATPG));

			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));
			setHeaderField9(msgHeaderSetted, inputMsg, hour4Check, isNextDay, instance, false);
			break;
		case TT_MORTGAGE_PAYMENT:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE,
					Transform.fromAsciiToEbcdic(Constant.Misce.ATPA));

			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));
			break;
		default:
			msgHeaderSetted.putField(ISCReqMessage.Fields._05_H_TRAN_CODE, Transform.fromAsciiToEbcdic("9999"));
			msgHeaderSetted.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));
			break;
		}

		return msgHeaderSetted;
	}

	private static String prepareBodyDebitAccTypeClient(Iso8583Post msg, String tranType) throws XPostilion {

		String debitAccType = "";
		StructuredData sd = new StructuredData();
		try {
			sd = msg.getStructuredData();
		} catch (XPostilion e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception(outError.toString()));
		}

		if (msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("7")
				&& (tranType.equals(TT_WITHDRAWAL_CB_ATTD) || tranType.equals(TT_WITHDRAWAL_CB_ATTF))) {

			debitAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1140C3_DEBIT_ACC_TYPE_CLIENT_1)
					.concat(msg.getProcessingCode().getFromAccount().substring(0, 1).equals("1")
							? Transform.fromAsciiToEbcdic("0")
							: Transform.fromAsciiToEbcdic("1"));

		}

		else {

			if (sd.get(Constant.TagNames.P_CODE).equals("000000")) { // processing codeiso

				debitAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1140C3_DEBIT_ACC_TYPE_CLIENT_1)
						.concat(msg.getProcessingCode().getFromAccount().substring(0, 1).equals("1")
								? Transform.fromAsciiToEbcdic("0")
								: Transform.fromAsciiToEbcdic("1"));

			} else {

				if (sd.get(Constant.TagNames.P_CODE).substring(2, 3).equals("1"))
					debitAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1140C3_DEBIT_ACC_TYPE_CLIENT_1)
							.concat(Transform.fromAsciiToEbcdic("0"));
				else {

					debitAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1140C3_DEBIT_ACC_TYPE_CLIENT_1)
							.concat(sd.get(Constant.TagNames.P_CODE).substring(2, 3).equals("2")
									? Transform.fromAsciiToEbcdic("1")
									: Transform.fromAsciiToEbcdic(sd.get(Constant.TagNames.P_CODE).substring(2, 3)));
				}

			}
		}
		return debitAccType;
	}

	private static String prepareCreditAccType(Iso8583Post msg, String tranType) throws XPostilion {

		String creditAccType = "";
		StructuredData sd = new StructuredData();
		try {
			sd = msg.getStructuredData();
		} catch (XPostilion e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception(outError.toString()));
		}

		if (msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("7")
				&& (tranType.equals(TT_WITHDRAWAL_CB_ATTF) || tranType.equals(TT_WITHDRAWAL_CB_ATTC))) {

			creditAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C5D2_DEBIT_ACC_TYPE_CORRES_1)
					.concat(Transform.fromAsciiToEbcdic("1"));

		}

		else {

			if (sd.get(Constant.TagNames.P_CODE).equals("000000")) { // processing codeiso

				creditAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C5D2_DEBIT_ACC_TYPE_CORRES_1)
						.concat(msg.getProcessingCode().getToAccount().substring(0, 1).equals("1")
								? Transform.fromAsciiToEbcdic("0")
								: Transform.fromAsciiToEbcdic("1"));

			} else {

				if (sd.get(Constant.TagNames.P_CODE).substring(4, 5).equals("1"))
					creditAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C5D2_DEBIT_ACC_TYPE_CORRES_1)
							.concat(Transform.fromAsciiToEbcdic("0"));
				else {

					creditAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C5D2_DEBIT_ACC_TYPE_CORRES_1)
							.concat(sd.get(Constant.TagNames.P_CODE).substring(4, 5).equals("2")
									? Transform.fromAsciiToEbcdic("1")
									: Transform.fromAsciiToEbcdic(sd.get(Constant.TagNames.P_CODE).substring(4, 5)));
				}

			}

		}

		return creditAccType;
	}

	private static String prepareDebitAccTypeClient2(Iso8583Post msg, String tranType) throws XFieldUnableToConstruct {
		String debitAccType = null;
		StructuredData sd = new StructuredData();

		switch (tranType) {
		case TT_TRANSFER_CB_ATTF:
		case TT_REV_TRANSFER_CB_ATTF:
		case TT_REV_REP_TRANSFER_CB_ATTF:
			debitAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C5D2_DEBIT_ACC_TYPE_CORRES_1)
					.concat(msg.getProcessingCode().getToAccount().substring(0, 1).equals("1")
							? Transform.fromAsciiToEbcdic("0")
							: Transform.fromAsciiToEbcdic("1"));
			break;

		default:

			try {
				sd = msg.getStructuredData();
			} catch (XPostilion e) {
				StringWriter outError = new StringWriter();
				e.printStackTrace(new PrintWriter(outError));
				EventRecorder.recordEvent(new Exception(outError.toString()));
			}

			debitAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C5D2_DEBIT_ACC_TYPE_CORRES_1)
					.concat(Transform.fromAsciiToEbcdic(
							null != sd.get("CLIENT_ACCOUNT_TYPE") ? mapAccountType(sd.get("CLIENT_ACCOUNT_TYPE"))
									: "1"));
			break;
		}

		return debitAccType;
	}

	private static String prepareCreditAccTypeCorres(Iso8583Post msg, String tranType) throws XPostilion {
		String creditAccType = null;
		StructuredData sd = msg.getStructuredData();

		switch (tranType) {
		case TT_PAYMENT_CB_MIXT:
			if (sd.get("PRIM_COV_PAYMENT_TYPE") != null && sd.get("PRIM_COV_PAYMENT_TYPE").equals("1")) {
				creditAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C5D2_DEBIT_ACC_TYPE_CORRES_1)
						.concat(Transform.fromAsciiToEbcdic(null != sd.get("PRIM_COV_ACCOUNT_TYPE")
								? mapAccountType(sd.get("PRIM_COV_ACCOUNT_TYPE"))
								: "1"));
			} else {
				creditAccType = "";
			}

			break;

		default:
			creditAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C5D2_DEBIT_ACC_TYPE_CORRES_1)
					.concat(Transform.fromAsciiToEbcdic(
							null != sd.get("PRIM_COV_ACCOUNT_TYPE") ? mapAccountType(sd.get("PRIM_COV_ACCOUNT_TYPE"))
									: "1"));
			break;
		}

		return creditAccType;
	}

	private static String mapAccountType(String inputAccType) {
		String accType = "";
		switch (inputAccType) {
		case "10":
			accType = "0";
			break;
		case "20":
			accType = "1";
			break;
		default:
			accType = "1";
			break;
		}
		return accType;
	}

	private static String prepareCorresAccountNr(Iso8583Post msg) {
		String corAccountNr = null;
		StructuredData sd = new StructuredData();
		try {
			sd = msg.getStructuredData();
		} catch (XPostilion e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception(outError.toString()));
		}

		corAccountNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C37B_COR_ACCOUNT_NR_10)
				.concat(Transform.fromAsciiToEbcdic(
						null != sd.get("CORRES_ACCOUNT_NR") ? Pack.resize(sd.get("CORRES_ACCOUNT_NR"), 10, '0', false)
								: null != sd.get("CLIENT_ACCOUNT_NR")
										? Pack.resize(sd.get("CLIENT_ACCOUNT_NR"), 10, '0', false)
										: "0000000000"));

		return corAccountNr;
	}

	private static String prepareClient2AccountNr(Iso8583Post msg) {
		Logger.logLine("--0--", false);
		String cli2AccountNr = null;
		StructuredData sd = new StructuredData();
		try {
			sd = msg.getStructuredData();
		} catch (XPostilion e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception(outError.toString()));
		}

		cli2AccountNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C37B_COR_ACCOUNT_NR_10)
				.concat(Transform
						.fromAsciiToEbcdic(null != sd.get("CLIENT2_ACCOUNT_NR")
								? Pack.resize(sd.get("CLIENT2_ACCOUNT_NR")
										.substring(sd.get("CLIENT2_ACCOUNT_NR").length() - 10), 10, '0', false)
								: "0000000000"));

		return cli2AccountNr;
	}

	private static String prepareCreditAccount(Iso8583Post msg) {
		Logger.logLine("--0--", false);
		String cli2AccountNr = null;
		StructuredData sd = new StructuredData();
		try {
			sd = msg.getStructuredData();
		} catch (XPostilion e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception(outError.toString()));
		}

		cli2AccountNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C37B_COR_ACCOUNT_NR_10)
				.concat(Transform
						.fromAsciiToEbcdic(null != sd.get("ACCOUNT_DEST_DEPOSIT")
								? Pack.resize(sd.get("ACCOUNT_DEST_DEPOSIT")
										.substring(sd.get("ACCOUNT_DEST_DEPOSIT").length() - 10), 10, '0', false)
								: "0000000000"));

		return cli2AccountNr;
	}

	private static String prepareCovenantAccountNr(Iso8583Post msg, String tranType) throws XPostilion {
		String corAccountNr = null;
		StructuredData sd = msg.getStructuredData();

		switch (tranType) {
		case TT_PAYMENT_CB_MIXT:

			if (sd.get("PRIM_COV_PAYMENT_TYPE") != null && sd.get("PRIM_COV_PAYMENT_TYPE").equals("1")) {
				corAccountNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C37B_COR_ACCOUNT_NR_10)
						.concat(Transform.fromAsciiToEbcdic(null != sd.get("PRIM_COV_ACCOUNT_NR")
								? Pack.resize(sd.get("PRIM_COV_ACCOUNT_NR"), 10, '0', false)
								: "0000000000"));
			} else {
				corAccountNr = "";
			}

			break;

		default:

			corAccountNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C37B_COR_ACCOUNT_NR_10)
					.concat(Transform.fromAsciiToEbcdic(null != sd.get("PRIM_COV_ACCOUNT_NR")
							? Pack.resize(sd.get("PRIM_COV_ACCOUNT_NR"), 10, '0', false)
							: "0000000000"));

			break;
		}

		return corAccountNr;
	}

	private static String prepareDebitAccountNr(Iso8583Post msg, String tranType) throws XPostilion {
		String debitAccountNr = null;

		switch (tranType) {
		case TT_DEPOSIT_CB_ATTD:
		case TT_DEPOSIT_CB_ATTF:
		case TT_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTC:

			debitAccountNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_114040_DEBIT_ACC_NR_10).concat(Transform
					.fromAsciiToEbcdic(Pack.resize(msg.getStructuredData().get("CORRES_ACCOUNT_NR"), 10, '0', false)));

			break;

		case TT_PAYMENT_CB_CREDIT:

			debitAccountNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_114040_DEBIT_ACC_NR_10)
					.concat(Transform.fromAsciiToEbcdic("0000000000"));

			break;
		default:

			if (null != msg.getStructuredData().get("CLIENT_ACCOUNT_NR")
					&& !msg.getStructuredData().get("CLIENT_ACCOUNT_NR").equals("")) {
				debitAccountNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_114040_DEBIT_ACC_NR_10)
						.concat(Transform.fromAsciiToEbcdic(
								Pack.resize(msg.getStructuredData().get("CLIENT_ACCOUNT_NR"), 10, '0', false)));
			} else if (null != msg.getStructuredData().get("CORRES_ACCOUNT_NR")
					&& !msg.getStructuredData().get("CORRES_ACCOUNT_NR").equals("")) {
				debitAccountNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_114040_DEBIT_ACC_NR_10)
						.concat(Transform.fromAsciiToEbcdic(
								Pack.resize(msg.getStructuredData().get("CORRES_ACCOUNT_NR"), 10, '0', false)));
			} else {
				debitAccountNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_114040_DEBIT_ACC_NR_10)
						.concat(Transform.fromAsciiToEbcdic(
								Pack.resize(msg.getField(Iso8583.Bit._102_ACCOUNT_ID_1), 10, '0', false)));
			}

			break;
		}

		return debitAccountNr;
	}

	private static String prepareCardNr(Iso8583Post msg, String tranType) throws XPostilion {
		String cardNr = null;

		switch (tranType) {
		case TT_DEPOSIT_CB_ATTF:
		case TT_DEPOSIT_CB_ATTD:
			cardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C6_CARD_NR_16)
					.concat(Transform.fromAsciiToEbcdic("                "));
			break;
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
			cardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C6_CARD_NR_16)
					.concat(Transform.fromAsciiToEbcdic("0000000000000000"));
			break;
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTD:
		case TT_WITHDRAWAL_CB_ATTF:
		case TT_REVERSE_CB_ATTF:
		case TT_REP_REVERSE_CB_ATTF:
		case TT_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTD:
		case TT_WITHDRAWAL:
		case TT_REVERSE:
		case TT_REP_REVERSE:
			cardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C6_CARD_NR_16)
					.concat(Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 16)));
			break;

		default:
			if (!msg.isFieldSet(Iso8583.Bit._102_ACCOUNT_ID_1)) {
				cardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C6_CARD_NR_16).concat(Transform
						.fromAsciiToEbcdic(Pack.resize(msg.getStructuredData().get("CORRES_CARD_NR"), 16, '0', false)));
			} else if (msg.getStructuredData().get("CLIENT_CARD_NR") != null) {
				cardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C6_CARD_NR_16).concat(Transform
						.fromAsciiToEbcdic(Pack.resize(msg.getStructuredData().get("CLIENT_CARD_NR"), 16, '0', false)));
			} else {
				cardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C6_CARD_NR_16).concat(
						Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 16)));
			}
			break;
		}

		return cardNr;
	}

	private static String prepareAcqInstIdCode(Iso8583Post msg, String trantype) {
		String acqInstIdCode = "";

		switch (trantype) {
		case TT_WITHDRAWAL_CB_ATTF:
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_REVERSE_CB_ATTF:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTF:
		case TT_REP_REVERSE_CB_ATTD:
		case TT_WITHDRAWAL:
		case TT_REVERSE:
		case TT_REP_REVERSE:
			acqInstIdCode = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119160_INPUT_NETWORK_2)
					.concat(Transform.fromAsciiToEbcdic("02"));
			break;
		case TT_TRANSFER_CB_ATTC:
		case TT_REV_TRANSFER_CB_ATTC:
		case TT_REV_REP_TRANSFER_CB_ATTC:
			acqInstIdCode = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119160_INPUT_NETWORK_2)
					.concat(Transform.fromAsciiToEbcdic("04"));
			break;
		case TT_PAYMENT_CB_CREDIT:
		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_DEPOSIT_CB_ATTC:
		case TT_DEPOSIT_CB_ATTF:
		case TT_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
		case TT_CARD_PAYMENT:
		case TT_TRANSFER_CB_ATTF:
		case TT_REV_TRANSFER_CB_ATTF:
		case TT_REV_REP_TRANSFER_CB_ATTF:
		case TT_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTD:
			acqInstIdCode = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119160_INPUT_NETWORK_2)
					.concat(Transform.fromAsciiToEbcdic("02"));
			break;
		default:

			try {
				acqInstIdCode = msg.getField(Iso8583.Bit._032_ACQUIRING_INST_ID_CODE);
				switch (acqInstIdCode) {
				case "10000000054":
					acqInstIdCode = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119160_INPUT_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("02"));
				case "10000000074":
					acqInstIdCode = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119160_INPUT_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("03"));
				default:
					acqInstIdCode = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119160_INPUT_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("04"));
				}
			} catch (XPostilion e) {
				StringWriter outError = new StringWriter();
				e.printStackTrace(new PrintWriter(outError));
				EventRecorder.recordEvent(new Exception(outError.toString()));
			}

			break;
		}

		return acqInstIdCode;
	}

	private static String prepareAcqNetwork(Iso8583Post msg, String trantype) throws XPostilion {
		String acqNetwork = "";

		switch (trantype) {
		case TT_WITHDRAWAL_CB_ATTF:
		case TT_WITHDRAWAL_CB_ATTC:
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_DEPOSIT_CB_ATTF:
		case TT_DEPOSIT_CB_ATTC:
		case TT_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
		case TT_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTD:
		case TT_TRANSFER_CB_ATTC:
		case TT_REV_TRANSFER_CB_ATTC:
		case TT_REV_REP_TRANSFER_CB_ATTC:
		case TT_TRANSFER_CB_ATTF:
		case TT_REV_TRANSFER_CB_ATTF:
		case TT_REV_REP_TRANSFER_CB_ATTF:
		case TT_PAYMENT_CB_CREDIT:
		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_CARD_PAYMENT:
		case TT_PAYMENT_OBLIG_CB_MIXT:
		case TT_PAYMENT_OBLIG_CB_DEBIT:
			acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
					.concat(Transform.fromAsciiToEbcdic("99"));
			break;
//		case TT_CARD_PAYMENT:
//			acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
//					.concat(Transform.fromAsciiToEbcdic("01"));
//			break;
		case TT_WITHDRAWAL:

			if (msg.getStructuredData().get("B24_Field_41").substring(0, 4).equals("0054")) {

				if (msg.getStructuredData().get("B24_Field_48").substring(0, 4).equals("0001")) {
					acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("01"));
				} else if (msg.getStructuredData().get("B24_Field_48").substring(0, 4).equals("0002")) {
					acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("09"));
				} else if (msg.getStructuredData().get("B24_Field_48").substring(0, 4).equals("0023")) {
					acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("03"));
				} else if (msg.getStructuredData().get("B24_Field_48").substring(0, 4).equals("0052")) {
					acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("04"));
				} else {
					acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("99"));
				}

			} else {

				if (msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("1004")) {
					acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("05"));
				} else if (msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("1005")) {
					acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("07"));
				} else if (msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("1006")) {
					acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("06"));
				} else {
					acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
							.concat(Transform.fromAsciiToEbcdic("99"));
				}
			}

			break;
		default:
			acqNetwork = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2)
					.concat(Transform.fromAsciiToEbcdic("04"));

			break;
		}

		return acqNetwork;
	}

	public static String getInternalTranType(Iso8583Post msg, boolean localCovVal) throws XPostilion {
		String tranType = msg.getProcessingCode().getTranType().concat("_").concat(String.valueOf(msg.getMsgType()));

		// Retiro CNB
		if (msg.getProcessingCode().getTranType().equals("50")
				&& msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("7")
				&& msg.getProcessingCode().getToAccount().equals("43")) {
			tranType = tranType.concat("_").concat(msg.getStructuredData().get("B24_Field_103").substring(2, 3));
		}

		// PAGO TARJETA
		else if (msg.getProcessingCode().getTranType().equals("50")
				&& msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("1")
				&& (msg.getProcessingCode().getFromAccount().equals("10")
						|| msg.getProcessingCode().getFromAccount().equals("20"))
				&& msg.getProcessingCode().getToAccount().equals("30")) {
			tranType = tranType.concat("_").concat("96");
		}

		// PAGO CREDITO HIPOTECARIO
		else if (msg.getProcessingCode().getTranType().equals("50")
				&& msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("1")
				&& (msg.getProcessingCode().getFromAccount().equals("10")
						|| msg.getProcessingCode().getFromAccount().equals("20"))
				&& msg.getProcessingCode().getToAccount().equals("00")) {
			tranType = tranType.concat("_").concat("95");
		}

		// TRANSFER CNB
		else if (msg.getProcessingCode().getTranType().equals("40")
				&& msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("7")
				&& (msg.getProcessingCode().getFromAccount().equals("10")
						|| msg.getProcessingCode().getFromAccount().equals("20"))
				&& (msg.getProcessingCode().getToAccount().equals("10")
						|| msg.getProcessingCode().getToAccount().equals("20")))
			tranType = tranType.concat("_").concat(msg.getStructuredData().get("B24_Field_103").substring(2, 3))
					.concat("_").concat("99");

		// DEPOSIT CNB
		else if (msg.getProcessingCode().getTranType().equals("40")
				&& msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("7")
				&& (msg.getProcessingCode().getFromAccount().equals("20")
						|| msg.getProcessingCode().getFromAccount().equals("10"))
				&& (msg.getProcessingCode().getToAccount().equals("24")
						|| msg.getProcessingCode().getToAccount().equals("14")))
			tranType = tranType.concat("_").concat(msg.getStructuredData().get("B24_Field_103").substring(2, 3))
					.concat("_").concat("98");

		// PSP CNB
		else if (msg.getProcessingCode().getTranType().equals("40")
				&& msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("7")
				&& msg.getProcessingCode().getToAccount().equals("00")) {

			tranType = tranType.concat("_").concat(msg.getStructuredData().get("B24_Field_103").substring(0, 1));

			if (tranType.equals(TT_PAYMENT_CB_CREDIT) || tranType.equals(TT_PAYMENT_CB_MIXT)
					|| tranType.equals(TT_REV_PAYMENT_CB_CREDIT) || tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT)
					|| tranType.equals(TT_REV_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT)) {

				if (search4covenant(msg)) {

					if ((tranType.equals(TT_PAYMENT_CB_CREDIT) || tranType.equals(TT_REV_PAYMENT_CB_CREDIT)
							|| tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT))
							&& msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE").equals("0")) {
						tranType = "00";
					} else if ((tranType.equals(TT_PAYMENT_CB_MIXT) || tranType.equals(TT_PAYMENT_CB_CREDIT)
							|| tranType.equals(TT_REV_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT)
							|| tranType.equals(TT_REV_PAYMENT_CB_CREDIT)
							|| tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT))
							&& msg.getStructuredData().get("PRIM_COV_ACCOUNT_NR").substring(0, 6).equals("940999")) {

						tranType = "05_1";
					}

				} else {
					tranType = "05";
				}

			} else if (tranType.equals(TT_PAYMENT_CB_DEBIT)) {
				StructuredData sd = msg.getStructuredData();
				sd.put("PRIM_COV_ABO", "2");
			}

		}

		// POB CNB
		else if (msg.getProcessingCode().getTranType().equals("50")
				&& msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("7")
				&& (msg.getProcessingCode().getToAccount().equals("00")
						|| msg.getProcessingCode().getToAccount().equals("30")
						|| msg.getProcessingCode().getToAccount().equals("40")
						|| msg.getProcessingCode().getToAccount().equals("41"))) {
			tranType = tranType.concat("_").concat(msg.getStructuredData().get("B24_Field_103").substring(2, 3))
					.concat("_").concat("97");

		}

		return tranType;

	}

	private String getConvenat(String covId) {
		return ISCInterfaceCB.convenios.get(covId);
	}

	private static String prepareTermId(Iso8583Post msg, String tranType) throws XPostilion {

		String termId = "";

		switch (tranType) {

		case TT_DEPOSIT_CB_ATTC:

		case TT_WITHDRAWAL_CB_ATTF:
		case TT_WITHDRAWAL_CB_ATTC:
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_PAYMENT_CB_MIXT:
		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
		case TT_REVERSE_CB_ATTF:
		case TT_REVERSE_CB_ATTC:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTF:
		case TT_REP_REVERSE_CB_ATTC:
		case TT_REP_REVERSE_CB_ATTD:
		case TT_CARD_PAYMENT:
			termId = Utils.padLeft(msg.getStructuredData().get("B24_Field_41").substring(4, 8), "0", 8);
			break;

		default:
			termId = Utils.padLeft(msg.getField(Iso8583.Bit._041_CARD_ACCEPTOR_TERM_ID).substring(4, 8), "0", 8);
			break;
		}

		return termId;
	}

	private static String prepareIdenDocNr(Iso8583Post msg, String tranType) throws XPostilion {

		String idenDocNr = "";

		switch (tranType) {
		case TT_COST_INQUIRY:
		case TT_WITHDRAWAL_CB_ATTC:
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
		case TT_REVERSE_CB_ATTC:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTC:
		case TT_REP_REVERSE_CB_ATTD:
		case TT_TRANSFER_CB_ATTC:
		case TT_REV_TRANSFER_CB_ATTC:
		case TT_REV_REP_TRANSFER_CB_ATTC:
		case TT_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTD:
		case TT_WITHDRAWAL:
		case TT_REVERSE:
		case TT_REP_REVERSE:
			idenDocNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F3_IDEN_DOC_NR_16)
					.concat(Transform.fromAsciiToEbcdic("0000000000000000"));
			break;

		case TT_DEPOSIT_CB_ATTC:
		case TT_DEPOSIT_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
			idenDocNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F3_IDEN_DOC_NR_16)
					.concat(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("B24_Field_104").substring(12)));
			break;

		case TT_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
			idenDocNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F3_IDEN_DOC_NR_16).concat(
					Transform.fromAsciiToEbcdic(msg.getStructuredData().get("CORRES_CUSTOMER_ID").substring(9)));
			break;

		case TT_WITHDRAWAL_CB_ATTF:
		case TT_REVERSE_CB_ATTF:
		case TT_REP_REVERSE_CB_ATTF:
			idenDocNr = "";
			break;
		default:

			if (null != msg.getStructuredData().get(Constant.TagNames.CUSTOMER_ID)) {
				idenDocNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F3_IDEN_DOC_NR_16).concat(Transform
						.fromAsciiToEbcdic(msg.getStructuredData().get(Constant.TagNames.CUSTOMER_ID).substring(9)));
			} else if (null != msg.getStructuredData().get("CORRES_CUSTOMER_ID")) {
				idenDocNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F3_IDEN_DOC_NR_16).concat(
						Transform.fromAsciiToEbcdic(msg.getStructuredData().get("CORRES_CUSTOMER_ID").substring(9)));
			}

			break;
		}

		return idenDocNr;

	}

	private static String prepareCardClase(Iso8583Post msg, String tranType) throws XPostilion {

		String cardClass = "";

		switch (tranType) {
		case TT_WITHDRAWAL_CB_ATTC:
		case TT_REVERSE_CB_ATTC:
		case TT_REP_REVERSE_CB_ATTC:
			cardClass = "";
			break;
		case TT_WITHDRAWAL_CB_ATTF:
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_REVERSE_CB_ATTF:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTF:
		case TT_REP_REVERSE_CB_ATTD:
			cardClass = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1197A5_DEBIT_CARD_TYPE_2)
					.concat(Transform.fromAsciiToEbcdic(null != msg.getStructuredData().get("CLIENT_CARD_CLASS")
							? msg.getStructuredData().get("CLIENT_CARD_CLASS").substring(9, 11)
							: "VS"));
			break;
		case TT_TRANSFER_CB_ATTC:
		case TT_REV_TRANSFER_CB_ATTC:
		case TT_REV_REP_TRANSFER_CB_ATTC:
			cardClass = "";
			break;
		case TT_DEPOSIT_CB_ATTC:
		case TT_DEPOSIT_CB_ATTF:
		case TT_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
			cardClass = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1197A5_DEBIT_CARD_TYPE_2)
					.concat(Transform.fromAsciiToEbcdic("  "));
			break;
		case TT_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
			cardClass = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1197A5_DEBIT_CARD_TYPE_2)
					.concat(Transform.fromAsciiToEbcdic(null != msg.getStructuredData().get("CLIENT_CARD_CLASS")
							? msg.getStructuredData().get("CLIENT_CARD_CLASS").substring(9, 11)
							: null != msg.getStructuredData().get("DEBIT_CARD_CLASS")
									? msg.getStructuredData().get("DEBIT_CARD_CLASS").substring(9, 11)
									: " "));
			break;
		default:
			cardClass = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1197A5_DEBIT_CARD_TYPE_2)
					.concat(Transform.fromAsciiToEbcdic("VS"));
			break;
		}

		return cardClass;

	}

	private static String prepareDevice(Iso8583Post msg, String tranType) throws XPostilion {

		String device = "";

		switch (tranType) {
		case TT_WITHDRAWAL_CB_ATTF:
		case TT_WITHDRAWAL_CB_ATTC:
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_TRANSFER_CB_ATTF:
		case TT_TRANSFER_CB_ATTC:
		case TT_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTC:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTF:
		case TT_REV_REP_TRANSFER_CB_ATTC:
		case TT_REV_REP_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTF:
		case TT_PAYMENT_CB_MIXT:
		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
		case TT_REVERSE_CB_ATTF:
		case TT_REVERSE_CB_ATTC:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTF:
		case TT_REP_REVERSE_CB_ATTC:
		case TT_REP_REVERSE_CB_ATTD:
		case TT_DEPOSIT_CB_ATTC:
		case TT_DEPOSIT_CB_ATTD:
		case TT_DEPOSIT_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
			device = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D139_DEVICE_1)
					.concat(Transform.fromAsciiToEbcdic("P"));
			break;
		default:
			device = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D139_DEVICE_1)
					.concat(Transform.fromAsciiToEbcdic("A"));
			break;
		}

		return device;

	}

	private static String prepareCorresCardNr(Iso8583Post msg, String tranType) throws XPostilion {

		String corresCardNr = "";

		switch (tranType) {
		case TT_WITHDRAWAL_CB_ATTF:
		case TT_WITHDRAWAL_CB_ATTC:

		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
		case TT_REVERSE_CB_ATTF:
		case TT_REVERSE_CB_ATTC:

		case TT_REP_REVERSE_CB_ATTF:
		case TT_REP_REVERSE_CB_ATTC:

		case TT_DEPOSIT_CB_ATTF:
		case TT_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
			corresCardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C7_CORRES_CARD_NR_16)
					.concat(Transform.fromAsciiToEbcdic(null != msg.getStructuredData().get("CORRES_CARD_NR")
							? msg.getStructuredData().get("CORRES_CARD_NR")
							: Constant.Misce.STR_SIXTEEN_ZEROS));
			break;
		case TT_PAYMENT_CB_MIXT:
			corresCardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C7_CORRES_CARD_NR_16)
					.concat(Transform.fromAsciiToEbcdic(Constant.Misce.STR_SIXTEEN_ZEROS));
			break;
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTD:
			corresCardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C7_CORRES_CARD_NR_16)
					.concat(msg.getStructuredData().get("B24_Field_112") != null
							? Transform.fromAsciiToEbcdic(msg.getStructuredData().get("B24_Field_112"))
							: Transform.fromAsciiToEbcdic(Constant.Misce.STR_SIXTEEN_ZEROS));
			break;
		case TT_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTD:
			corresCardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C7_CORRES_CARD_NR_16)
					.concat(msg.getStructuredData().get("B24_Field_112") != null
							? Transform.fromAsciiToEbcdic(msg.getStructuredData().get("B24_Field_112"))
							: Transform.fromAsciiToEbcdic("                "));
			break;
		default:
			corresCardNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C7_CORRES_CARD_NR_16)
					.concat(Transform.fromAsciiToEbcdic(Constant.Misce.STR_SIXTEEN_ZEROS));
			break;
		}

		return corresCardNr;

	}

	private static String prepareCorresCardClass(Iso8583Post msg, String tranType) throws XPostilion {

		String corresCardClass = "";

		switch (tranType) {
		case TT_WITHDRAWAL_CB_ATTF:
		case TT_WITHDRAWAL_CB_ATTC:
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTD:
		case TT_REVERSE_CB_ATTF:
		case TT_REVERSE_CB_ATTC:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTF:
		case TT_REP_REVERSE_CB_ATTC:
		case TT_REP_REVERSE_CB_ATTD:
		case TT_DEPOSIT_CB_ATTD:
		case TT_DEPOSIT_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
			corresCardClass = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1197A7_CORRES_CARD_TYPE_2)
					.concat(Transform.fromAsciiToEbcdic(null != msg.getStructuredData().get("CORRES_CARD_CLASS")
							? msg.getStructuredData().get("CORRES_CARD_CLASS").substring(9, 11)
							: "NB"));
			break;
		case TT_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
			corresCardClass = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1197A7_CORRES_CARD_TYPE_2)
					.concat(Transform.fromAsciiToEbcdic("  "));
			break;
		case TT_TRANSFER_CB_ATTF:
		case TT_REV_TRANSFER_CB_ATTF:
		case TT_REV_REP_TRANSFER_CB_ATTF:
			corresCardClass = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1197A7_CORRES_CARD_TYPE_2)
					.concat(Transform.fromAsciiToEbcdic(Constant.Misce.STR_TWO_ZEROS));
			break;
		default:
			corresCardClass = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1197A7_CORRES_CARD_TYPE_2)
					.concat(Transform.fromAsciiToEbcdic(Constant.Misce.STR_TWO_ZEROS));
			break;
		}

		return corresCardClass;

	}

	private static String prepareD135(Iso8583Post msg, String tranType) throws XPostilion {
		String d135 = "";
		switch (tranType) {
		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
			d135 = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D135_)
					.concat(Transform.fromAsciiToEbcdic("000000000000000000000072534777"));
			break;
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
			d135 = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D135_)
					.concat(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("B24_Field_62").substring(0, 30)));
			break;
		default:
			d135 = "";
			break;
		}

		return d135;
	}

	private static String prepareD142(Iso8583Post msg, String tranType) throws XPostilion {
		String d142 = "";
		switch (tranType) {

		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_PAYMENT_OBLIG_CB_DEBIT:
		case TT_PAYMENT_OBLIG_CB_MIXT:
			d142 = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D142_)
					.concat(Transform.fromAsciiToEbcdic("00005440"));
			break;
		case TT_WITHDRAWAL_CB_ATTF:
		case TT_REVERSE_CB_ATTF:
		case TT_REP_REVERSE_CB_ATTF:
		case TT_DEPOSIT_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
		case TT_TRANSFER_CB_ATTF:
		case TT_REV_TRANSFER_CB_ATTF:
		case TT_REV_REP_TRANSFER_CB_ATTF:
			d142 = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D142_)
					.concat(Transform.fromAsciiToEbcdic("00000000"));
			break;
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
			d142 = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D142_)
					.concat(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("B24_Field_103").substring(10)));
			break;
		default:
			d142 = "";
			break;
		}

		return d142;
	}

	private static String isDeposit(Iso8583Post msg, String tranType) {

		String isDeposit = "";

		switch (tranType) {
		case TT_DEPOSIT_CB_ATTC:
		case TT_DEPOSIT_CB_ATTF:
		case TT_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTF:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTF:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
			isDeposit = Transform.fromHexToBin("11E2E5").concat(Transform.fromAsciiToEbcdic("1"));
			break;
		default:
			isDeposit = "";
			break;
		}

		return isDeposit;
	}

	private static String isVirtualShop(Iso8583Post msg, String tranType) {

		String isDeposit = "";

		switch (tranType) {
		case TT_DEPOSIT_CB_ATTC:
		case TT_DEPOSIT_CB_ATTF:

		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTF:

		case TT_REV_REP_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTF:

			isDeposit = Transform.fromHexToBin("11E5F4").concat(Transform.fromAsciiToEbcdic("1"));
			break;
		default:
			isDeposit = "";
			break;
		}

		return isDeposit;
	}

	private static String prepareD137(Iso8583Post msg, String tranType) {
		String d137 = "";
		switch (tranType) {
		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_MIXT:
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
		case TT_PAYMENT_OBLIG_CB_DEBIT:
		case TT_PAYMENT_OBLIG_CB_MIXT:
			d137 = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D137_)
					.concat(Transform.fromAsciiToEbcdic("000000000000000"));
			break;
		default:
			d137 = "";
			break;
		}

		return d137;
	}

	private static String prepareCrditEntity(Iso8583Post msg, String tranType) throws XPostilion {
		String creditEn = "";

		switch (tranType) {
		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_MIXT:
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
		case TT_PAYMENT_OBLIG_CB_DEBIT:
		case TT_PAYMENT_OBLIG_CB_MIXT:
		case TT_COST_INQUIRY:
		case TT_WITHDRAWAL_CB_ATTC:

		case TT_TRANSFER_CB_ATTC:

		case TT_REV_TRANSFER_CB_ATTC:

		case TT_REV_REP_TRANSFER_CB_ATTC:

		case TT_REVERSE_CB_ATTC:

		case TT_REP_REVERSE_CB_ATTC:

		case TT_WITHDRAWAL:
		case TT_REVERSE:
		case TT_REP_REVERSE:
			creditEn = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11924A_CREDIT_ENTITY_CODE_4)
					.concat(Transform.fromAsciiToEbcdic(Constant.Misce.STR_FOUR_ZEROS));
			break;
		case TT_DEPOSIT_CB_ATTC:
		case TT_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTD:
		case TT_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTD:
			creditEn = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11924A_CREDIT_ENTITY_CODE_4)
					.concat(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("B24_Field_103").substring(3, 7)));
			break;
		default:
			creditEn = "";
			break;
		}

		return creditEn;
	}

	private static String prepareCreditAccountType(Iso8583Post msg, String tranType) {
		String creditAccType = "";

		switch (tranType) {
		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_MIXT:
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
		case TT_PAYMENT_OBLIG_CB_DEBIT:
		case TT_PAYMENT_OBLIG_CB_MIXT:
		case TT_COST_INQUIRY:
		case TT_WITHDRAWAL_CB_ATTC:
		case TT_TRANSFER_CB_ATTC:
		case TT_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTC:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTC:
		case TT_REV_REP_TRANSFER_CB_ATTD:
		case TT_REVERSE_CB_ATTC:
		case TT_REP_REVERSE_CB_ATTC:
		case TT_WITHDRAWAL:
		case TT_REVERSE:
			creditAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119115_CREDIT_ACC_TYPE_1)
					.concat(Transform.fromAsciiToEbcdic(Constant.Misce.STR_ONE_ZERO));
			break;
		case TT_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
		case TT_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTD:
			creditAccType = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119115_CREDIT_ACC_TYPE_1)
					.concat(Transform.fromAsciiToEbcdic("3"));
			break;
		default:
			creditAccType = "";
			break;
		}

		return creditAccType;
	}

	private static String prepareAvalCreditAccountNr(Iso8583Post msg, String tranType) throws XPostilion {
		String AvalcreditAccNr = "";

		switch (tranType) {
		case TT_PAYMENT_CB_DEBIT:
		case TT_PAYMENT_CB_MIXT:
		case TT_PAYMENT_CB_CREDIT:
		case TT_REV_PAYMENT_CB_DEBIT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_CREDIT:
		case TT_REV_REP_PAYMENT_CB_DEBIT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_CREDIT:
		case TT_COST_INQUIRY:
		case TT_WITHDRAWAL_CB_ATTC:
		case TT_WITHDRAWAL_CB_ATTD:
		case TT_TRANSFER_CB_ATTC:

		case TT_REV_TRANSFER_CB_ATTC:

		case TT_REV_REP_TRANSFER_CB_ATTC:

		case TT_REVERSE_CB_ATTC:
		case TT_REVERSE_CB_ATTD:
		case TT_REP_REVERSE_CB_ATTC:
		case TT_REP_REVERSE_CB_ATTD:
		case TT_WITHDRAWAL:
		case TT_REVERSE:
		case TT_DEPOSIT_CB_ATTC:
		case TT_REV_DEPOSIT_CB_ATTC:
		case TT_REV_REP_DEPOSIT_CB_ATTC:
			AvalcreditAccNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11912D_AVAL_CREDIT_ACC_NR_20)
					.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", Constant.Misce.STR_ONE_ZERO, 20)));
			break;
		case TT_DEPOSIT_CB_ATTD:
		case TT_REV_DEPOSIT_CB_ATTD:
		case TT_REV_REP_DEPOSIT_CB_ATTD:
		case TT_TRANSFER_CB_ATTD:
		case TT_REV_TRANSFER_CB_ATTD:
		case TT_REV_REP_TRANSFER_CB_ATTD:
			AvalcreditAccNr = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11912D_AVAL_CREDIT_ACC_NR_20)
					.concat(Transform.fromAsciiToEbcdic(
							Pack.resize(msg.getStructuredData().get("B24_Field_103").substring(12), 20, '0', false)));
			break;
		default:
			AvalcreditAccNr = "";
			break;
		}

		return AvalcreditAccNr;
	}

	private static String prepareE4F0(Iso8583Post msg, String tranType) throws XPostilion {

		return Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F0_)
//				.concat(Transform.fromAsciiToEbcdic(Pack.resize(msg.getStructuredData().get("B24_Field_62").substring(msg.getStructuredData().get("B24_Field_62").length() - 30), 60, '0', false)));
//				.concat(Transform.fromAsciiToEbcdic("000000000000000000000000000000000000000000000000000000000000"));
				.concat(Transform
						.fromAsciiToEbcdic(Pack.resize(msg.getStructuredData().get("B24_Field_62"), 60, '0', false)));
	}

	private static String prepareE4F9(Iso8583Post msg, String tranType) throws XPostilion {

		return Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F9_)
//				.concat(Transform.fromAsciiToEbcdic(Pack.resize(msg.getStructuredData().get("B24_Field_62"), 60, '0', false)));
				.concat(Transform.fromAsciiToEbcdic("000000000000000000000000000000000000000000000000000000000000"));
	}

	private static String prepareE4F8(Iso8583Post msg, String tranType) throws XPostilion {

		String e4f8 = null;

		switch (tranType) {
		case TT_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
			if (null != msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE")
					&& msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE").equals("1")) {

				e4f8 = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F8_).concat(
						Transform.fromAsciiToEbcdic(msg.getStructuredData().get("PRIM_COV_REPRO_INDICATOR") != null
								? msg.getStructuredData().get("PRIM_COV_REPRO_INDICATOR")
								: "0"));

			} else {

				e4f8 = "";

			}

			break;

		default:

			e4f8 = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F8_)
					.concat(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("PRIM_COV_REPRO_INDICATOR") != null
							? msg.getStructuredData().get("PRIM_COV_REPRO_INDICATOR")
							: "0"));
			break;
		}

		return e4f8;
	}

	private static String preparePayModeIndicator(Iso8583Post msg, String tranType) throws XPostilion {

		String payModeInd = null;

		switch (tranType) {
		case TT_PAYMENT_CB_MIXT:
		case TT_REV_PAYMENT_CB_MIXT:
		case TT_REV_REP_PAYMENT_CB_MIXT:
			if (null != msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE")
					&& msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE").equals("1")) {
				payModeInd = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11A2C7_PAY_MODE_INDIC)
//						.concat(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE") != null ? msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE") : "0"));
						.concat(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE")));
			} else {
				payModeInd = "";
			}

			break;

		default:

			payModeInd = Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11A2C7_PAY_MODE_INDIC)
//					.concat(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE") != null ? msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE") : "0"));
					.concat(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE")));

			break;
		}

		return payModeInd;
	}

	public static String prepareVariableReqBody(Iso8583Post msg, String tranType) throws XPostilion {

		Logger.logLine("ARMANDO CUERPO VARIBLE: " + tranType, false);

		Logger.logLine("INICIO DE ARMADO TRAMA ISC: \n" + msg.getStructuredData().toString(), false);

		StringBuilder sb = new StringBuilder();

		sb.append(tranType.equals(TT_GOOD_N_SERVICES)
				? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119105_PIGNOS_6).concat("000000")
				: "")

				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119130_DATE_6)) // 119130_Fecha MMDDYY
				.append(Transform.fromAsciiToEbcdic(getStringDate(MMDDYYhhmmss).substring(0, 6)))

				.append(!tranType.equals(TT_WITHDRAWAL_CB_ATTC) && !tranType.equals(TT_TRANSFER_CB_ATTC) // 1140C3_DEBIT_ACC_TYPE
						&& !tranType.equals(TT_REV_TRANSFER_CB_ATTC) && !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC)
						&& !tranType.equals(TT_REVERSE_CB_ATTC) && !tranType.equals(TT_REP_REVERSE_CB_ATTC)
						&& !tranType.equals(TT_DEPOSIT_CB_ATTC) && !tranType.equals(TT_REV_DEPOSIT_CB_ATTC)
						&& !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC)
//						&& !tranType.equals(TT_PAYMENT_CB_CREDIT) && !tranType.equals(TT_REV_PAYMENT_CB_CREDIT) && !tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT)
								? prepareBodyDebitAccTypeClient(msg, tranType)
								: "")

				.append(tranType.equals(TT_WITHDRAWAL_CB_ATTF) || tranType.equals(TT_WITHDRAWAL_CB_ATTC) // 11C5D2_CREDIT_ACC_TYPE
						|| tranType.equals(TT_REVERSE_CB_ATTF) || tranType.equals(TT_REVERSE_CB_ATTC)
						|| tranType.equals(TT_REP_REVERSE_CB_ATTF) || tranType.equals(TT_REP_REVERSE_CB_ATTC)
						|| tranType.equals(TT_DEPOSIT_CB_ATTF) || tranType.equals(TT_DEPOSIT_CB_ATTC)
						|| tranType.equals(TT_REV_DEPOSIT_CB_ATTF) || tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTF)
						|| tranType.equals(TT_REV_DEPOSIT_CB_ATTC) || tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC)
						|| tranType.equals(TT_TRANSFER_CB_ATTC) || tranType.equals(TT_REV_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC) ? prepareCreditAccType(msg, tranType) : "")

				.append(tranType.equals(TT_TRANSFER_CB_ATTF) || tranType.equals(TT_TRANSFER_CB_ATTD) // 11C5D2_CREDIT_ACC_TYPE
						|| tranType.equals(TT_REV_TRANSFER_CB_ATTF) || tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF)
						|| tranType.equals(TT_REV_TRANSFER_CB_ATTD) || tranType.equals(TT_REV_REP_TRANSFER_CB_ATTD)
								? prepareDebitAccTypeClient2(msg, tranType)
								: "")

				.append(tranType.equals(TT_PAYMENT_CB_MIXT) || tranType.equals(TT_PAYMENT_CB_CREDIT) // 11C5D2_CREDIT_ACC_TYPE
						|| tranType.equals(TT_REV_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_PAYMENT_OBLIG_CB_CREDIT) || tranType.equals(TT_PAYMENT_OBLIG_CB_MIXT)
						|| tranType.equals(TT_PAYMENT_OBLIG_CB_DEBIT) ? prepareCreditAccTypeCorres(msg, tranType) : "")

				.append(tranType.equals(TT_COST_INQUIRY) // 11C3F0 Monto transaccion
						? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C3F0_TRAN_AMOUNT_15)
								.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", "0", 15)))
						: Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C3F0_TRAN_AMOUNT_15)
								.concat(Transform.fromAsciiToEbcdic(
										Utils.padLeft(msg.getField(Iso8583.Bit._004_AMOUNT_TRANSACTION), "0", 15))))

				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119131_SYS_TIME_12)) // 119131 hora de
																								// sistema
				.append(Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._012_TIME_LOCAL)))

				.append(tranType.equals(TT_WITHDRAWAL_CB_ATTF) || tranType.equals(TT_WITHDRAWAL_CB_ATTC) // 11C37B_COR_ACCOUNT_NR
						|| tranType.equals(TT_REVERSE_CB_ATTF) || tranType.equals(TT_REVERSE_CB_ATTC)
						|| tranType.equals(TT_REP_REVERSE_CB_ATTF) || tranType.equals(TT_REP_REVERSE_CB_ATTC)

								? prepareCorresAccountNr(msg)
								: "")

				.append(tranType.equals(TT_TRANSFER_CB_ATTC) || tranType.equals(TT_TRANSFER_CB_ATTF) // 11C37B_CLIENT2_ACCOUNT_NR
						|| tranType.equals(TT_REV_TRANSFER_CB_ATTC) || tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_REV_TRANSFER_CB_ATTF) || tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF)
//						&& covFounded
//						&& msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE") != null
//						&& msg.getStructuredData().get("PRIM_COV_PAYMENT_TYPE").equals("1")
								? prepareClient2AccountNr(msg)
								: "")

				.append(tranType.equals(TT_DEPOSIT_CB_ATTF) // 11C37B_CLIENT2_ACCOUNT_NR
						|| tranType.equals(TT_DEPOSIT_CB_ATTC) || tranType.equals(TT_REV_DEPOSIT_CB_ATTF)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTF) || tranType.equals(TT_REV_DEPOSIT_CB_ATTC)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC) ? prepareCreditAccount(msg) : "")

				.append(tranType.equals(TT_PAYMENT_CB_MIXT) || tranType.equals(TT_PAYMENT_CB_CREDIT) // 11C37B_COV_ACCOUNT_NR
						|| tranType.equals(TT_REV_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_PAYMENT_CB_DEBIT)
						|| tranType.equals(TT_REV_PAYMENT_CB_CREDIT) || tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_DEBIT) || tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_PAYMENT_OBLIG_CB_CREDIT) || tranType.equals(TT_PAYMENT_OBLIG_CB_MIXT)
						|| tranType.equals(TT_PAYMENT_OBLIG_CB_DEBIT) ? prepareCovenantAccountNr(msg, tranType) : "")

				.append(!tranType.equals(TT_WITHDRAWAL_CB_ATTC) && !tranType.equals(TT_TRANSFER_CB_ATTC) // 114040 OJO!
																											// 10 por
																											// especificacion
						&& !tranType.equals(TT_REV_TRANSFER_CB_ATTC) && !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC)
						&& !tranType.equals(TT_DEPOSIT_CB_ATTC) && !tranType.equals(TT_REV_DEPOSIT_CB_ATTC)
						&& !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC) ? prepareDebitAccountNr(msg, tranType) : "")

				.append(tranType.equals(TT_REVERSE) || tranType.equals(TT_REP_REVERSE)
						|| tranType.equals(TT_REVERSE_CB_ATTF) // 11C1C5 sequencia original
						|| tranType.equals(TT_REVERSE_CB_ATTC) || tranType.equals(TT_REVERSE_CB_ATTD)
						|| tranType.equals(TT_REP_REVERSE_CB_ATTF) || tranType.equals(TT_REP_REVERSE_CB_ATTC)
						|| tranType.equals(TT_REP_REVERSE_CB_ATTD) || tranType.equals(TT_REV_DEPOSIT_CB_ATTF)
						|| tranType.equals(TT_REV_DEPOSIT_CB_ATTC) || tranType.equals(TT_REV_DEPOSIT_CB_ATTD)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTF) || tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTD) || tranType.equals(TT_REV_TRANSFER_CB_ATTF)
						|| tranType.equals(TT_REV_TRANSFER_CB_ATTC) || tranType.equals(TT_REV_TRANSFER_CB_ATTD)
						|| tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF) || tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_REV_REP_TRANSFER_CB_ATTD) || tranType.equals(TT_REV_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_REV_PAYMENT_CB_DEBIT) || tranType.equals(TT_REV_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT) || tranType.equals(TT_REV_REP_PAYMENT_CB_DEBIT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT)
								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C1C5_ORIGINAL_SEQ_6)
										.concat(Transform.fromAsciiToEbcdic(
												msg.getField(Iso8583.Bit._038_AUTH_ID_RSP).substring(2)))
								: "")

				.append(tranType.equals(TT_TRANSFER_CB_ATTF) // 11E5C6 OJO! numero tarjeta
						|| tranType.equals(TT_REV_TRANSFER_CB_ATTF) || tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF)
						|| tranType.equals(TT_PAYMENT_CB_CREDIT) || tranType.equals(TT_REV_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT) || tranType.equals(TT_PAYMENT_CB_DEBIT)
						|| tranType.equals(TT_REV_PAYMENT_CB_DEBIT) || tranType.equals(TT_REV_REP_PAYMENT_CB_DEBIT)
						|| tranType.equals(TT_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT) || tranType.equals(TT_DEPOSIT_CB_ATTF)
						|| tranType.equals(TT_DEPOSIT_CB_ATTD) || tranType.equals(TT_CARD_PAYMENT)
						|| tranType.equals(TT_PAYMENT_OBLIG_CB_MIXT) || tranType.equals(TT_WITHDRAWAL_CB_ATTF)
						|| tranType.equals(TT_REVERSE_CB_ATTF) || tranType.equals(TT_REP_REVERSE_CB_ATTF)
						|| tranType.equals(TT_WITHDRAWAL_CB_ATTD) || tranType.equals(TT_REVERSE_CB_ATTD)
						|| tranType.equals(TT_REP_REVERSE_CB_ATTD) ? prepareCardNr(msg, tranType) : "")

				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C17A_REC_NR_6)) // 11C17A Numero de
																								// registro
				.append(Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR).substring(6, 12)))

				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1191A1_TRAN_NACIONALITY_1)) // 1191A1
																										// Nacionalidad
																										// transaccion
				.append(Transform
						.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._049_CURRENCY_CODE_TRAN).equals("170") ? "1" : "2"))

				.append(prepareAcqInstIdCode(msg, tranType)) // 119160 Institucion adquiriente

				.append(prepareAcqNetwork(msg, tranType)) // 119161 Red adquiriente

				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119162_TERM_ID_8)) // 119162 term id
				.append(Transform.fromAsciiToEbcdic(prepareTermId(msg, tranType)))

				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119181_ORIGINAL_TRAN_1)) // 119181 tran
																									// original
				.append(tranType.equals(TT_PAYMENT_CB_CREDIT) || tranType.equals(TT_PAYMENT_CB_DEBIT)
						|| tranType.equals(TT_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_REV_PAYMENT_CB_DEBIT) || tranType.equals(TT_REV_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT) || tranType.equals(TT_REV_REP_PAYMENT_CB_DEBIT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT) || tranType.equals(TT_CARD_PAYMENT)
						|| tranType.equals(TT_DEPOSIT_CB_ATTC) || tranType.equals(TT_REV_DEPOSIT_CB_ATTC)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC) || tranType.equals(TT_WITHDRAWAL_CB_ATTD)
						|| tranType.equals(TT_REVERSE_CB_ATTD) || tranType.equals(TT_REP_REVERSE_CB_ATTD)
						|| tranType.equals(TT_WITHDRAWAL_CB_ATTF) || tranType.equals(TT_REVERSE_CB_ATTF)
						|| tranType.equals(TT_REP_REVERSE_CB_ATTF) ? Transform.fromAsciiToEbcdic("1")
								: Transform.fromAsciiToEbcdic("0"))

				.append(!tranType.equals(TT_REVERSE) && !tranType.equals(TT_REP_REVERSE) // 11912F codigo auth
						&& !tranType.equals(TT_REVERSE_CB_ATTC) && !tranType.equals(TT_REVERSE_CB_ATTD)
						&& !tranType.equals(TT_REVERSE_CB_ATTF) && !tranType.equals(TT_REP_REVERSE_CB_ATTC)
						&& !tranType.equals(TT_REP_REVERSE_CB_ATTD) && !tranType.equals(TT_REP_REVERSE_CB_ATTF)
						&& !tranType.equals(TT_REV_PAYMENT_CB_MIXT) && !tranType.equals(TT_REV_PAYMENT_CB_DEBIT)
						&& !tranType.equals(TT_REV_PAYMENT_CB_CREDIT) && !tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT)
						&& !tranType.equals(TT_REV_REP_PAYMENT_CB_DEBIT)
						&& !tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT)
								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11912F_AUTH_CODE_8)
										.concat(Transform.fromAsciiToEbcdic(Pack.resize(
												msg.getStructuredData().get("SEQ_TERMINAL").split(",")[1].trim(), 8,
												'0', false)))
								: Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11912F_AUTH_CODE_8)
										.concat(Transform.fromAsciiToEbcdic(
												Pack.resize(msg.getField(Iso8583.Bit._038_AUTH_ID_RSP).substring(2), 8,
														'0', false))))

				// agregar aqui campos de pago D135, D142, D137

				.append(prepareD135(msg, tranType)) // 11D135_

				.append(prepareD142(msg, tranType)) // 11D142_ codigo auth

				.append(prepareD137(msg, tranType)) // 11D137_

				.append(tranType.equals(TT_GOOD_N_SERVICES) // 119190 codigo comercio
						? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119190_COMMERCE_CODE_10)
								.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", Constant.Misce.STR_ONE_ZERO, 10)))
						: "")

				.append(prepareCrditEntity(msg, tranType)) // 11924A entidad crediticia

				.append(prepareCreditAccountType(msg, tranType)) // 119115 tipo cuenta credito

				.append(prepareAvalCreditAccountNr(msg, tranType)) // 11912D AVAL_CREDIT_ACC_NR

				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119199_TERM_LOCATION_40)) // 119199 location
				.append(Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC)))

				// agregar aqui campos de pago E4F9, E4F0

				.append(tranType.equals(TT_PAYMENT_CB_DEBIT) || tranType.equals(TT_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_PAYMENT_CB_CREDIT) || tranType.equals(TT_REV_PAYMENT_CB_DEBIT)
						|| tranType.equals(TT_REV_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_DEBIT) || tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT) ? prepareE4F9(msg, tranType) : "") // 11E4F9
																											// AVAL_CREDIT_ACC_NR

				.append(tranType.equals(TT_PAYMENT_CB_DEBIT) || tranType.equals(TT_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_PAYMENT_CB_CREDIT) // 11E4F0 AVAL_CREDIT_ACC_NR
						|| tranType.equals(TT_REV_PAYMENT_CB_DEBIT) || tranType.equals(TT_REV_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_REV_PAYMENT_CB_CREDIT) || tranType.equals(TT_REV_REP_PAYMENT_CB_DEBIT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT)
								? prepareE4F0(msg, tranType)
								: "")

				.append(tranType.equals(TT_PAYMENT_CB_MIXT) || tranType.equals(TT_PAYMENT_CB_CREDIT) // 11E4F8
																										// RECIPROCITY
																										// INDICATOR
						|| tranType.equals(TT_REV_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT)
								? prepareE4F8(msg, tranType)
								: "")

				.append(prepareCardClase(msg, tranType)) // 1197A5_DEBIT_CARD_TYPE

				.append(tranType.equals(TT_COST_INQUIRY) || tranType.equals(TT_WITHDRAWAL) // 11E4F2 tipo de documento
																							// identidad
						|| tranType.equals(TT_REVERSE) || tranType.equals(TT_WITHDRAWAL_CB_ATTC)
						|| tranType.equals(TT_WITHDRAWAL_CB_ATTD) || tranType.equals(TT_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_TRANSFER_CB_ATTD) || tranType.equals(TT_REVERSE_CB_ATTC)
						|| tranType.equals(TT_REVERSE_CB_ATTD) || tranType.equals(TT_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_PAYMENT_CB_DEBIT) || tranType.equals(TT_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_REV_PAYMENT_CB_DEBIT) || tranType.equals(TT_REV_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_REV_PAYMENT_CB_CREDIT) || tranType.equals(TT_REV_REP_PAYMENT_CB_DEBIT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_DEPOSIT_CB_ATTC) || tranType.equals(TT_REV_DEPOSIT_CB_ATTC)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC) || tranType.equals(TT_REV_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_REV_TRANSFER_CB_ATTD) || tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_REV_REP_TRANSFER_CB_ATTD)
								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F2_IDEN_DOC_TYPE_1).concat(
										Transform.fromAsciiToEbcdic(Utils.padLeft("", Constant.Misce.STR_ONE_ZERO, 1)))
								: /*
									 * Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F2_IDEN_DOC_TYPE_1))
									 * .append(Transform.fromAsciiToEbcdic(Constant.Misce.STR_ONE_ZERO)
									 */ "")

				.append(prepareIdenDocNr(msg, tranType)) // 11E4F3 documento identidad

				.append(isDeposit(msg, tranType))

				.append(isVirtualShop(msg, tranType))

				.append(tranType.equals(TT_COST_INQUIRY) || tranType.equals(TT_BALANCE_INQUIRY_CB) // 11D140 entidad
																									// adquiriente
						|| tranType.equals(TT_WITHDRAWAL_CB_ATTC) || tranType.equals(TT_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_REV_TRANSFER_CB_ATTC) || tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_REVERSE_CB_ATTC) || tranType.equals(TT_REVERSE_CB_ATTD)
						|| tranType.equals(TT_REP_REVERSE_CB_ATTC) || tranType.equals(TT_REP_REVERSE_CB_ATTD)
								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D140_ACQ_ENTITY_4)
										.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", "0", 4)))
								: Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D140_ACQ_ENTITY_4)
										.concat(Transform.fromAsciiToEbcdic(Utils.padLeft(
												msg.getField(Iso8583.Bit._041_CARD_ACCEPTOR_TERM_ID).substring(4, 8),
												"0", 4))))

				.append(tranType.equals(TT_COST_INQUIRY) || tranType.equals(TT_BALANCE_INQUIRY_CB) // 11D138 oficina
																									// adquiriente
						|| tranType.equals(TT_WITHDRAWAL_CB_ATTC) || tranType.equals(TT_WITHDRAWAL_CB_ATTD)
						|| tranType.equals(TT_WITHDRAWAL_CB_ATTF) || tranType.equals(TT_REVERSE_CB_ATTC)
						|| tranType.equals(TT_REVERSE_CB_ATTD) || tranType.equals(TT_REVERSE_CB_ATTF)
						|| tranType.equals(TT_REP_REVERSE_CB_ATTC) || tranType.equals(TT_REP_REVERSE_CB_ATTD)
						|| tranType.equals(TT_REP_REVERSE_CB_ATTF) || tranType.equals(TT_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_TRANSFER_CB_ATTD) || tranType.equals(TT_TRANSFER_CB_ATTF)
						|| tranType.equals(TT_REV_TRANSFER_CB_ATTC) || tranType.equals(TT_REV_TRANSFER_CB_ATTD)
						|| tranType.equals(TT_REV_TRANSFER_CB_ATTF) || tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_REV_REP_TRANSFER_CB_ATTD) || tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF)
						|| tranType.equals(TT_REVERSE_CB_ATTC) || tranType.equals(TT_REVERSE_CB_ATTD)
						|| tranType.equals(TT_REP_REVERSE_CB_ATTC) || tranType.equals(TT_REP_REVERSE_CB_ATTD)
						|| tranType.equals(TT_PAYMENT_CB_CREDIT) || tranType.equals(TT_REV_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT) || tranType.equals(TT_DEPOSIT_CB_ATTF)
						|| tranType.equals(TT_DEPOSIT_CB_ATTC) || tranType.equals(TT_DEPOSIT_CB_ATTD)
						|| tranType.equals(TT_REV_DEPOSIT_CB_ATTF) || tranType.equals(TT_REV_DEPOSIT_CB_ATTC)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTF) || tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTD) || tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTD)
						|| tranType.equals(TT_WITHDRAWAL) || tranType.equals(TT_REVERSE)
						|| tranType.equals(TT_REP_REVERSE)
								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D138_ACQ_OFFICE_4)
										.concat(Transform.fromAsciiToEbcdic(Constant.Misce.STR_FOUR_ZEROS))
								: "")

				.append(prepareDevice(msg, tranType)) // 11D139

				.append(tranType.equals(TT_PAYMENT_CB_MIXT) || tranType.equals(TT_PAYMENT_CB_CREDIT) // 11A2C7
																										// RECIPROCITY
																										// INDICATOR
						|| tranType.equals(TT_REV_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_PAYMENT_CB_CREDIT)
						|| tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT)

								? preparePayModeIndicator(msg, tranType)
								: "")

				.append(prepareCorresCardNr(msg, tranType)) // 11E5C7

				.append(prepareCorresCardClass(msg, tranType)) // 1197A7

				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11A9B1_TRAN_INDICATOR_1)) // 11A9B1 Indicador
																										// transaccion
				.append(tranType.equals(TT_COST_INQUIRY) ? Transform.fromAsciiToEbcdic("I")
						: Transform.fromAsciiToEbcdic("M"))

				.append(isDeposit(msg, tranType))

				.append(tranType.equals(TT_DEPOSIT_CB_ATTC) || tranType.equals(TT_DEPOSIT_CB_ATTF)
						|| tranType.equals(TT_DEPOSIT_CB_ATTD) || tranType.equals(TT_REV_DEPOSIT_CB_ATTC)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC) || tranType.equals(TT_REV_DEPOSIT_CB_ATTF)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTF) || tranType.equals(TT_REV_DEPOSIT_CB_ATTD)
						|| tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTD)
								&& msg.getStructuredData().get("B24_Field_104") != null
										? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5F1_CELULAR_NR_12)
												.concat(Transform.fromAsciiToEbcdic(
														msg.getStructuredData().get("B24_Field_104").substring(0, 12)))
										: "")

				.append(tranType.equals(TT_PAYMENT_CB_MIXT) // 11E5F1 numero celular
						|| tranType.equals(TT_REV_PAYMENT_CB_MIXT) || tranType.equals(TT_REV_REP_PAYMENT_CB_MIXT)
						|| tranType.equals(TT_TRANSFER_CB_ATTC) || tranType.equals(TT_TRANSFER_CB_ATTD)
						|| tranType.equals(TT_TRANSFER_CB_ATTF) || tranType.equals(TT_REV_TRANSFER_CB_ATTC)
						|| tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC) || tranType.equals(TT_REV_TRANSFER_CB_ATTD)
						|| tranType.equals(TT_REV_REP_TRANSFER_CB_ATTD) || tranType.equals(TT_REV_TRANSFER_CB_ATTF)
						|| tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF)
								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5F1_CELULAR_NR_12)
										.concat(Transform.fromAsciiToEbcdic("000000000000"))
								: "")

				.append(!tranType.equals(TT_BALANCE_INQUIRY_CB) && !tranType.equals(TT_WITHDRAWAL_CB_ATTF)
						&& !tranType.equals(TT_REVERSE_CB_ATTF) && !tranType.equals(TT_REP_REVERSE_CB_ATTF)
						&& !tranType.equals(TT_TRANSFER_CB_ATTC) && !tranType.equals(TT_TRANSFER_CB_ATTF)
						&& !tranType.equals(TT_REV_TRANSFER_CB_ATTC) && !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC)
						&& !tranType.equals(TT_REV_TRANSFER_CB_ATTF) && !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF)
						&& !tranType.equals(TT_DEPOSIT_CB_ATTD) && !tranType.equals(TT_REV_DEPOSIT_CB_ATTD)
						&& !tranType.equals(TT_DEPOSIT_CB_ATTF) && !tranType.equals(TT_REV_DEPOSIT_CB_ATTF)
						&& !tranType.equals(TT_DEPOSIT_CB_ATTC) && !tranType.equals(TT_REV_DEPOSIT_CB_ATTC)
								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5F3_VIRT_PURCH_INDICATOR_1)
										.concat(Transform.fromAsciiToEbcdic(Constant.Misce.STR_ONE_ZERO))
								: "")

				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E0E2_TRAN_IDENTIFICATOR_4)
						.concat(Transform.fromAsciiToEbcdic(Constant.Misce.STR_FOUR_ZEROS)))

				.append(!tranType.equals(TT_BALANCE_INQUIRY_CB) && !tranType.equals(TT_WITHDRAWAL_CB_ATTF)
						&& !tranType.equals(TT_REVERSE_CB_ATTF) && !tranType.equals(TT_REP_REVERSE_CB_ATTF)
						&& !tranType.equals(TT_WITHDRAWAL_CB_ATTC) && !tranType.equals(TT_REVERSE_CB_ATTC)
						&& !tranType.equals(TT_REP_REVERSE_CB_ATTC) && !tranType.equals(TT_TRANSFER_CB_ATTC)
						&& !tranType.equals(TT_TRANSFER_CB_ATTF) && !tranType.equals(TT_REV_TRANSFER_CB_ATTC)
						&& !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC) && !tranType.equals(TT_REV_TRANSFER_CB_ATTF)
						&& !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF) && !tranType.equals(TT_DEPOSIT_CB_ATTF)
						&& !tranType.equals(TT_DEPOSIT_CB_ATTD) && !tranType.equals(TT_DEPOSIT_CB_ATTC)
						&& !tranType.equals(TT_REV_DEPOSIT_CB_ATTC) && !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC)
						&& !tranType.equals(TT_REV_DEPOSIT_CB_ATTD) && !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTD)
						&& !tranType.equals(TT_REV_DEPOSIT_CB_ATTF) && !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTF)
								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11913D_SECURE_AMOUNT_15)
										.concat(Transform.fromAsciiToEbcdic(Pack.resize(
												msg.getStructuredData().get(Constant.TagNames.SECURE_AMOUNT) != null
														? msg.getStructuredData().get(Constant.TagNames.SECURE_AMOUNT)
														: Constant.Misce.STR_ONE_ZERO,
												15, Constant.Misce.CHAR_ONE_ZERO, false)))
								: "")

				.append(msg.getStructuredData().get("PA_MODE_SELECTED") != null && (tranType.equals(TT_WITHDRAWAL)
						|| tranType.equals(TT_REVERSE) || tranType.equals(TT_REP_REVERSE))
//						&& !tranType.equals(TT_TRANSFER_CB_ATTC) && !tranType.equals(TT_TRANSFER_CB_ATTF)
//						&& !tranType.equals(TT_TRANSFER_CB_ATTD) && !tranType.equals(TT_REV_TRANSFER_CB_ATTC)
//						&& !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC) && !tranType.equals(TT_REV_TRANSFER_CB_ATTD)
//						&& !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTD) && !tranType.equals(TT_REV_TRANSFER_CB_ATTF)
//						&& !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF)
								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11914D_PA_MODE).concat(
										Transform.fromAsciiToEbcdic(msg.getStructuredData().get("PA_MODE_SELECTED")))
								: tranType.equals(TT_REVERSE)
										? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11914D_PA_MODE)
												.concat(Transform.fromAsciiToEbcdic("0"))
										: Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11914D_PA_MODE)
												.concat(Transform.fromAsciiToEbcdic("0")))

				.append(!tranType.equals(TT_BALANCE_INQUIRY_CB) && !tranType.equals(TT_WITHDRAWAL_CB_ATTF)
						&& !tranType.equals(TT_REVERSE_CB_ATTF) && !tranType.equals(TT_REP_REVERSE_CB_ATTF)
						&& !tranType.equals(TT_TRANSFER_CB_ATTC) && !tranType.equals(TT_TRANSFER_CB_ATTF)
						&& !tranType.equals(TT_REV_TRANSFER_CB_ATTC) && !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC)
						&& !tranType.equals(TT_REV_TRANSFER_CB_ATTF) && !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF)
						&& !tranType.equals(TT_DEPOSIT_CB_ATTD) && !tranType.equals(TT_REV_DEPOSIT_CB_ATTD)
						&& !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTD) && !tranType.equals(TT_REV_DEPOSIT_CB_ATTC)
						&& !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC) && !tranType.equals(TT_REV_DEPOSIT_CB_ATTF)
						&& !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTF)
//						&& !tranType.equals(TT_REV_PAYMENT_CB_CREDIT) && !tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT)
								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D141_STANDIN_INDICATOR_1)
										.concat(msg.getMessageType().equals(Iso8583.MsgTypeStr._0220_TRAN_ADV) // SI ES
																												// CAJERO
												&& msg.getResponseCode().equals(Iso8583.RspCode._00_SUCCESSFUL)
														? Transform.fromAsciiToEbcdic("S")
														: Transform.fromAsciiToEbcdic("N"))
								: "");

//				.append(!tranType.equals(TT_BALANCE_INQUIRY_CB) && !tranType.equals(TT_WITHDRAWAL_CB_ATTF)
//						&& !tranType.equals(TT_REVERSE_CB_ATTF) && !tranType.equals(TT_REP_REVERSE_CB_ATTF)
//						&& !tranType.equals(TT_WITHDRAWAL_CB_ATTD) && !tranType.equals(TT_REVERSE_CB_ATTD)
//						&& !tranType.equals(TT_REP_REVERSE_CB_ATTD) && !tranType.equals(TT_TRANSFER_CB_ATTC)
//						&& !tranType.equals(TT_TRANSFER_CB_ATTF) && !tranType.equals(TT_TRANSFER_CB_ATTD)
//						&& !tranType.equals(TT_REV_TRANSFER_CB_ATTC) && !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTC)
//						&& !tranType.equals(TT_REV_TRANSFER_CB_ATTD) && !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTD)
//						&& !tranType.equals(TT_REV_TRANSFER_CB_ATTF) && !tranType.equals(TT_REV_REP_TRANSFER_CB_ATTF)
//						&& !tranType.equals(TT_DEPOSIT_CB_ATTD) && !tranType.equals(TT_REV_DEPOSIT_CB_ATTD)
//						&& !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTD) && !tranType.equals(TT_REV_DEPOSIT_CB_ATTC)
//						&& !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTC)
//						&& !tranType.equals(TT_REV_DEPOSIT_CB_ATTF)
//						&& !tranType.equals(TT_REV_REP_DEPOSIT_CB_ATTF)
////						&& !tranType.equals(TT_REV_PAYMENT_CB_CREDIT) && !tranType.equals(TT_REV_REP_PAYMENT_CB_CREDIT) EN CAJERO
//								? Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D141_STANDIN_INDICATOR_1)
//										.concat(Transform.fromAsciiToEbcdic("N"))
//								: "");

		return sb.toString();
	}

	public static String prepareTestVariableReqBody() {
		StringBuilder sd = new StringBuilder("");

		sd.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119130_DATE_6))
				.append(Transform.fromAsciiToEbcdic("051319"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1140C3_DEBIT_ACC_TYPE_CLIENT_1))
				.append(Transform.fromAsciiToEbcdic("1"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C3F0_TRAN_AMOUNT_15))
				.append(Transform.fromAsciiToEbcdic("000000008800000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119131_SYS_TIME_12))
				.append(Transform.fromAsciiToEbcdic("150451"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_114040_DEBIT_ACC_NR_10))
				.append(Transform.fromAsciiToEbcdic("0000118158"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C6_CARD_NR_16))
				.append(Transform.fromAsciiToEbcdic("4576020000066906"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11C17A_REC_NR_6))
				.append(Transform.fromAsciiToEbcdic("007396"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1191A1_TRAN_NACIONALITY_1))
				.append(Transform.fromAsciiToEbcdic("1"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119160_INPUT_NETWORK_2))
				.append(Transform.fromAsciiToEbcdic("02"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119161_ACQ_NETWORK_2))
				.append(Transform.fromAsciiToEbcdic("04"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119162_TERM_ID_8))
				.append(Transform.fromAsciiToEbcdic("00003915"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119181_ORIGINAL_TRAN_1))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11912F_AUTH_CODE_8))
				.append(Transform.fromAsciiToEbcdic("00451473"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11924A_CREDIT_ENTITY_CODE_4))
				.append(Transform.fromAsciiToEbcdic("0000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119115_CREDIT_ACC_TYPE_1))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11912D_AVAL_CREDIT_ACC_NR_20))
				.append(Transform.fromAsciiToEbcdic("00000000000000000000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119199_TERM_LOCATION_40))
				.append(Transform.fromAsciiToEbcdic("ATH  B.AVV  LABORATORIO100100BOGOTACAGCO"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1197A5_DEBIT_CARD_TYPE_2))
				.append(Transform.fromAsciiToEbcdic("VS"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F2_IDEN_DOC_TYPE_1))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F3_IDEN_DOC_NR_16))
				.append(Transform.fromAsciiToEbcdic("0000000000000000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D140_ACQ_ENTITY_4))
				.append(Transform.fromAsciiToEbcdic("3915"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D138_ACQ_OFFICE_4))
				.append(Transform.fromAsciiToEbcdic("0000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D139_DEVICE_1))
				.append(Transform.fromAsciiToEbcdic("A"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5C7_CORRES_CARD_NR_16))
				.append(Transform.fromAsciiToEbcdic("0000000000000000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_1197A7_CORRES_CARD_TYPE_2))
				.append(Transform.fromAsciiToEbcdic("00"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11A9B1_TRAN_INDICATOR_1))
				.append(Transform.fromAsciiToEbcdic("M"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E5F3_VIRT_PURCH_INDICATOR_1))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11D141_STANDIN_INDICATOR_1))
				.append(Transform.fromAsciiToEbcdic("N"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E0E2_TRAN_IDENTIFICATOR_4))
				.append(Transform.fromAsciiToEbcdic("0000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11913D_SECURE_AMOUNT_15))
				.append(Transform.fromAsciiToEbcdic("000000000000000"));

		return sd.toString();
	}

	/***************************************************************************************
	 * Metodo auxiliar que convierte la hora actual de sistema a String con formato
	 * MMDDYYhhmmss
	 * 
	 * @return
	 ***************************************************************************************/
	public static String getStringDate(String format) {

		Calendar cal = Calendar.getInstance();
		StringBuilder date = new StringBuilder();

		if (format.equals(YYMMDDhhmmss)) {
			date.append(String.valueOf(cal.get(Calendar.YEAR)).substring(2, 4));
			date.append(String.valueOf(cal.get(Calendar.MONTH) + 1).length() > 1
					? String.valueOf(cal.get(Calendar.MONTH) + 1)
					: "0" + String.valueOf(cal.get(Calendar.MONTH) + 1));
			date.append(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)).length() > 1
					? String.valueOf(cal.get(Calendar.DAY_OF_MONTH))
					: "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			date.append(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)).length() > 1
					? String.valueOf(cal.get(Calendar.HOUR_OF_DAY))
					: "0" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
			date.append(String.valueOf(cal.get(Calendar.MINUTE)).length() > 1 ? String.valueOf(cal.get(Calendar.MINUTE))
					: "0" + String.valueOf(cal.get(Calendar.MINUTE)));
			date.append(String.valueOf(cal.get(Calendar.SECOND)).length() > 1 ? String.valueOf(cal.get(Calendar.SECOND))
					: "0" + String.valueOf(cal.get(Calendar.SECOND)));
		} else if (format.equals(MMDDYYhhmmss)) {
			date.append(String.valueOf(cal.get(Calendar.MONTH) + 1).length() > 1
					? String.valueOf(cal.get(Calendar.MONTH) + 1)
					: "0" + String.valueOf(cal.get(Calendar.MONTH) + 1));
			date.append(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)).length() > 1
					? String.valueOf(cal.get(Calendar.DAY_OF_MONTH))
					: "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			date.append(String.valueOf(cal.get(Calendar.YEAR)).substring(2, 4));
			date.append(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)).length() > 1
					? String.valueOf(cal.get(Calendar.HOUR_OF_DAY))
					: "0" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
			date.append(String.valueOf(cal.get(Calendar.MINUTE)).length() > 1 ? String.valueOf(cal.get(Calendar.MINUTE))
					: "0" + String.valueOf(cal.get(Calendar.MINUTE)));
			date.append(String.valueOf(cal.get(Calendar.SECOND)).length() > 1 ? String.valueOf(cal.get(Calendar.SECOND))
					: "0" + String.valueOf(cal.get(Calendar.SECOND)));
		} else if (format.equals(DDMMYYhhmmss)) {
			date.append(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)).length() > 1
					? String.valueOf(cal.get(Calendar.DAY_OF_MONTH))
					: "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			date.append(String.valueOf(cal.get(Calendar.MONTH) + 1).length() > 1
					? String.valueOf(cal.get(Calendar.MONTH) + 1)
					: "0" + String.valueOf(cal.get(Calendar.MONTH) + 1));
			date.append(String.valueOf(cal.get(Calendar.YEAR)).substring(2, 4));
			date.append(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)).length() > 1
					? String.valueOf(cal.get(Calendar.HOUR_OF_DAY))
					: "0" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
			date.append(String.valueOf(cal.get(Calendar.MINUTE)).length() > 1 ? String.valueOf(cal.get(Calendar.MINUTE))
					: "0" + String.valueOf(cal.get(Calendar.MINUTE)));
			date.append(String.valueOf(cal.get(Calendar.SECOND)).length() > 1 ? String.valueOf(cal.get(Calendar.SECOND))
					: "0" + String.valueOf(cal.get(Calendar.SECOND)));
		} else if (format.equals(YYYYMMDDhhmmss)) {
			date.append(String.valueOf(cal.get(Calendar.YEAR)).substring(0, 4));
			date.append(String.valueOf(cal.get(Calendar.MONTH) + 1).length() > 1
					? String.valueOf(cal.get(Calendar.MONTH) + 1)
					: "0" + String.valueOf(cal.get(Calendar.MONTH) + 1));
			date.append(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)).length() > 1
					? String.valueOf(cal.get(Calendar.DAY_OF_MONTH))
					: "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			date.append(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)).length() > 1
					? String.valueOf(cal.get(Calendar.HOUR_OF_DAY))
					: "0" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
			date.append(String.valueOf(cal.get(Calendar.MINUTE)).length() > 1 ? String.valueOf(cal.get(Calendar.MINUTE))
					: "0" + String.valueOf(cal.get(Calendar.MINUTE)));
			date.append(String.valueOf(cal.get(Calendar.SECOND)).length() > 1 ? String.valueOf(cal.get(Calendar.SECOND))
					: "0" + String.valueOf(cal.get(Calendar.SECOND)));
		}

		return date.toString();
	}

	public static int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		
		int out = r.nextInt((max - min) + 1) + min;
		
		Logger.logLine("RANDOM:"+out, false);
		
		return out;
	}

	/**
	 * Fija los valores para los campos 38 y 39 de un msj y retorna el ResponseCode
	 * 
	 * @param msg
	 * @param allCodesIscToIso
	 * @throws XPostilion
	 */
	public static ResponseCode set38And39Fields(Iso8583Post msg, HashMap<String, ResponseCode> v1CodesIscToIso, HashMap<String, ResponseCode> v2CodesIscToIso)
			throws XPostilion {

		StructuredData sd = msg.getStructuredData();
		ResponseCode responseCode = new ResponseCode();
//		ResponseCode responseCode = null;

		Logger.logLine("SETING 38 y 39\n" + sd, false);
		
		Logger.logLine("MAP SIZE" + v1CodesIscToIso.size(), false);
		
//		for(Map.Entry<String, ResponseCode> e: v1CodesIscToIso.entrySet()) {
//			Logger.logLine("KEY: " + e.getKey() + " VAL: " + e.getValue().getKeyIsc(), true);
//		}
		
		try {
			if (sd.get("ERROR") != null) {
				
				responseCode.setKeyIsc(sd.get("ERROR"));
				
				if(isMassiveTransfer(msg)) {
					
					Logger.logLine("HAY CODIGO DE ERROR EN RSP", false);
					responseCode = InitialLoadFilter.getFilterCodeISCToIso(sd.get("ERROR"),
							v2CodesIscToIso);
					Logger.logLine(">>> " + sd.get("ERROR") + " >>> " + responseCode.getKeyIsc() + "::"
							+ responseCode.getDescriptionIsc(), false);
					msg.putField(Iso8583.Bit._038_AUTH_ID_RSP, "000000");
					
				}
				else {
					
					Logger.logLine("HAY CODIGO DE ERROR EN RSP", false);
					responseCode = InitialLoadFilter.getFilterCodeISCToIso(sd.get("ERROR"),
							 v1CodesIscToIso);
					Logger.logLine(">>> " + sd.get("ERROR") + " >>> " + responseCode.getKeyIsc() + "::"
							+ responseCode.getDescriptionIsc(), false);
					msg.putField(Iso8583.Bit._038_AUTH_ID_RSP, "000000");
					
				}
						
				
			} else {
				Logger.logLine("NO HAY CODIGO DE ERROR EN RSP", false);
				responseCode = InitialLoadFilter.getFilterCodeISCToIso("0000",
						(HashMap<String, ResponseCode>) v1CodesIscToIso);
				Logger.logLine(">>> 0000 >>> " + responseCode.getKeyIsc() + "::" + responseCode.getDescriptionIsc(), false);
				msg.putField(Iso8583.Bit._038_AUTH_ID_RSP, sd.get(Constant.TagNames.SEQ_TERMINAL).split(",")[0].trim()
						.substring(2).concat(sd.get(Constant.TagNames.SEQ_TERMINAL).split(",")[1].trim().substring(1)));
			}

			Logger.logLine("RESPOSE CODE ISC>>>".concat(responseCode.getKeyIsc()).concat(" ").concat("RESPOSE CODE ISO>>>")
					.concat(responseCode.getKeyIso()).concat(" ").concat("RESPOSE CODE DESCRIP>>>")
					.concat(responseCode.getDescriptionIsc()), false);

			msg.putField(Iso8583.Bit._039_RSP_CODE,
					responseCode.getKeyIso() != null && !responseCode.getKeyIso().equals("")
							? Pack.resize(responseCode.getKeyIso(), 2, '0', false)
							: "05");
			
		} catch (Exception e) {
//			StringWriter outError = new StringWriter();
//			e.printStackTrace(new PrintWriter(outError));
//			Logger.logLine("ERROR JSON TRANS COFIG: " + outError.toString(), false);
//			EventRecorder.recordEvent(new Exception(outError.toString()));
			responseCode = new ResponseCode();
			responseCode.setDescriptionIsc("RSP CODE TRANSLATION ERROR - NOT FOUND");
			
			msg.putField(Iso8583.Bit._038_AUTH_ID_RSP, "000000");
			msg.putField(Iso8583.Bit._039_RSP_CODE,
					responseCode.getKeyIso() != null && !responseCode.getKeyIso().equals("")
							? Pack.resize(responseCode.getKeyIso(), 2, '0', false)
							: "05");
		}

		return responseCode;
	}

	/**
	 * Se determina el tipo de transacciï¿½n "AAAA_BBBBBB_C" AAAA-Tipo de Msg ;
	 * BBBBBB-Codigo proceso ; C-canal
	 * 
	 * @param msg
	 * @param canal
	 * @return
	 * @throws XPostilion
	 */
	public static String getTranType(Iso8583Post msg, String canal) throws XPostilion {
		StringBuilder tranTypeBuilder = new StringBuilder();

		if (msg.getProcessingCode().getFromAccount().substring(0, 1).equals("5")) {

			tranTypeBuilder.append(msg.getMessageType()).append("_").append("500000").append("_").append(canal);

		} else {

			StructuredData sd = msg.getStructuredData();

			
			if (sd.get("CHANNEL") != null && sd.get("CHANNEL").equals("3")) {
				tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString())
						.append("_").append(sd.get("CHANNEL"));
			} else if (sd.get("CHANNEL") != null && sd.get("CHANNEL").equals("4")) {
				tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString())
				.append("_").append(sd.get("CHANNEL"));
			} else if (sd.get("CHANNEL") != null && sd.get("CHANNEL").equals("8")) {
				tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString())
				.append("_").append(sd.get("CHANNEL"));
			}else if(msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 6).equals("777790")) {
				tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString())
				.append("_").append("GIRO");
			} else {
				tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString())
				.append("_").append(canal);
			}
			
//			if (sd.get("CHANNEL") != null && sd.get("CHANNEL").equals("3")) {
//				tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString())
//						.append("_").append(sd.get("CHANNEL"));
//			} if (sd.get("CHANNEL") != null && sd.get("CHANNEL").equals("4")) {
//				tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString())
//				.append("_").append(sd.get("CHANNEL"));
//			} else {
//				
//				if(msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 6).equals("777790")) {
//					tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString())
//					.append("_").append("GIRO");
//				}
//				else {
//					tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString())
//					.append("_").append(canal);
//				}
//
//			}

		}

		return tranTypeBuilder.toString();
	}

	/**
	 * Se determina el canal el mismo viene en la posiciï¿½n 13 del Tag "B24_Field_41"
	 * 
	 * @param msg
	 * @return
	 * @throws XPostilion
	 */
	public static String getTranChannel(String field41) throws XPostilion {
		String channel = "";
		String red = field41.substring(0, 4);
//		if (red.equals("0054") || red.equals("0001")) {
//			channel = "1";
//		} else {
//			red = field41.substring(4, 8);
//			if (red.equals("1004") || red.equals("1005") || red.equals("1006") ) {
//				channel = "1";
//			}
//		}

		channel = "1";
		return channel;
	}

	private String setComision(String b24Field126, String comision) {
		return b24Field126.substring(0, b24Field126.length() - 14)
				.concat(Pack.resize(comision.replace(".", ""), 12, '0', false))
				.concat(b24Field126.substring(b24Field126.length() - 2));
	}

	public static void putB24Field102IntoStructuredData(StructuredData sd, String tranType) throws XPostilion {
		
		Logger.logLine("REVISION GIRO SD: "+sd.toString(), false);
		
//		if(tranType.equals("0210_011000_GIRO") || tranType.equals("0210_011000_GIRO") && (null != sd.get("B24_Field_102")) && (null != sd.get("ATCG_ACCOUNT_NR"))) {
//			sd.put(Constant.B24Fields.B24_F_102, sd.get("B24_Field_102").substring(0, 4)
//					+ (Pack.resize(sd.get("ATCG_ACCOUNT_NR"), 17, '0', false)));
//		}
		if(tranType.equals("0210_011000_GIRO") || tranType.equals("0210_011000_GIRO") && (null != sd.get("B24_Field_102")) && (null != sd.get("ATCG_ACCOUNT_NR"))) {
			sd.put(Constant.B24Fields.B24_F_102, Pack.resize(sd.get("ATCG_ACCOUNT_NR"), 18, '0', false));
		}
		else {
			if (null != sd.get("B24_Field_102") && null != sd.get("CLIENT_ACCOUNT_NR")) {
				sd.put(Constant.B24Fields.B24_F_102, sd.get("B24_Field_102").substring(0, 4)
						+ (Pack.resize(sd.get("CLIENT_ACCOUNT_NR"), 17, '0', false)));
				Logger.logLine("SETTING 102 RSP: " + sd.get(Constant.B24Fields.B24_F_102), false);
			}
		}
	}

	public static void putB24Field102IntoStructuredDataCUSTOM(StructuredData sd) throws XPostilion {
		if (null != sd.get("CLIENT_ACCOUNT_NR")) {
			sd.put(Constant.B24Fields.B24_F_102, Pack.resize(sd.get("CLIENT_ACCOUNT_NR"), 28, '0', false));
			Logger.logLine("SETTING 102 RSP: " + sd.get(Constant.B24Fields.B24_F_102), false);
		}
	}

	public static void putB24Field103IntoStructuredData(StructuredData sd) throws XPostilion {
		if (null != sd.get("B24_Field_103") && null != sd.get("CORRES_ACCOUNT_NR")) {
			sd.put(Constant.B24Fields.B24_F_103, sd.get("B24_Field_103").substring(0, 7)
					+ (Pack.resize(sd.get("CORRES_ACCOUNT_NR"), 17, '0', false)));
			Logger.logLine("SETTING 103 RSP: " + sd.get(Constant.B24Fields.B24_F_103), false);
		}
	}

	public static void putB24Field126IntoStructuredData(StructuredData sd) {
		Logger.logLine("********************************SETING COMISION\n", false);
		
		if(null != sd.get(Constant.B24Fields.B24_F_126)) {
			if(null != sd.get("TRANSFER_QR") && sd.get("TRANSFER_QR").equals("TRUE") && sd.get("COMISIONIVA") != null) {
				sd.put(Constant.B24Fields.B24_F_126, sd.get("COMISIONIVA") != null
						? sd.get(Constant.B24Fields.B24_F_126).substring(0, sd.get(Constant.B24Fields.B24_F_126).length() - 14)
								.concat(Pack.resize(sd.get("COMISIONIVA").replace(".", ""), 12, '0', false))
								.concat(sd.get(Constant.B24Fields.B24_F_126)
										.substring(sd.get(Constant.B24Fields.B24_F_126).length() - 2))
						: sd.get(Constant.B24Fields.B24_F_126));
			}else {
				sd.put(Constant.B24Fields.B24_F_126, sd.get(Constant.TagNames.COMISION) != null
						? sd.get(Constant.B24Fields.B24_F_126).substring(0, sd.get(Constant.B24Fields.B24_F_126).length() - 14)
								.concat(Pack.resize(sd.get(Constant.TagNames.COMISION).replace(".", ""), 12, '0', false))
								.concat(sd.get(Constant.B24Fields.B24_F_126)
										.substring(sd.get(Constant.B24Fields.B24_F_126).length() - 2))
						: sd.get(Constant.B24Fields.B24_F_126));
			}
			
		}
		
		// String paData =

		Logger.logLine("********************************SD\n" + sd, false);
	}

	public static void putSpecialCharsOnStructuredData(StructuredData sd) {
		sd.put("TEST_TAG", "LA PURA PE\u00D1\u00F1\u00C3");
	}

	public static void putB24Field62IntoStructuredData(StructuredData sd) {
		
		if(sd.get("NOMBRE2") != null) {
			Logger.logLine("NOMBRE2 RSP "+sd.get("NOMBRE2"), false);
		}
		if(sd.get("NOMBRE1") != null) {
			Logger.logLine("NOMBRE1 RSP "+sd.get("NOMBRE1"), false);
		}


		sd.put(Constant.B24Fields.B24_F_62, sd.get("NOMBRE2") != null
				? Constant.Misce.STR_THIRTY_ZEROS
						.concat(Pack.resize(sd.get("NOMBRE2")
								.replaceAll("[^a-zA-Z\\s]", " "), 30, '0', true))
						.concat(Constant.Misce.STR_THIRTY_ZEROS).concat(Constant.Misce.STR_THIRTY_ZEROS)
						.concat(Constant.Misce.STR_THIRTY_ZEROS)
				: (sd.get("NOMBRE") != null
						? Constant.Misce.STR_THIRTY_ZEROS
								.concat(Pack.resize(sd.get("NOMBRE")
										.replaceAll("[^a-zA-Z\\s]", " "), 30, '0', true))
								.concat(Constant.Misce.STR_THIRTY_ZEROS).concat(Constant.Misce.STR_THIRTY_ZEROS)
								.concat(Constant.Misce.STR_THIRTY_ZEROS)

						: Constant.Misce.STR_THIRTY_ZEROS.concat(Constant.Misce.STR_THIRTY_ZEROS)
								.concat(Constant.Misce.STR_THIRTY_ZEROS).concat(Constant.Misce.STR_THIRTY_ZEROS)
								.concat(Constant.Misce.STR_THIRTY_ZEROS)));

		if(sd.get(Constant.B24Fields.B24_F_62) != null) {
			Logger.logLine(Constant.B24Fields.B24_F_62+" RSP "+sd.get("NOMBRE1"), false);
		}
	}

	public static void fixLast5Movements(StructuredData sd) {
		
		HashMap<String, String> movs = new HashMap<>();
		
//		Logger.logLine("MOVS_1:"+sd.get("MOVIMIEN_1") , true);
//		Logger.logLine("MOVS_2:"+sd.get("MOVIMIEN_2") , true);
//		Logger.logLine("MOVS_3:"+sd.get("MOVIMIEN_3") , true);

		if (sd.get("MOVIMIEN_1") != null) {
			
			movs.put("MOVIMIEN_1", sd.get("MOVIMIEN_1").substring(1, 31));
			movs.put("MOVIMIEN_2", sd.get("MOVIMIEN_1").substring(31, 61));
			
//			sd.put("MOVIMIEN_1", Pack.resize(sd.get("MOVIMIEN_1"), 30, ' ', false));
		} else {
			
			movs.put("MOVIMIEN_1", Pack.resize("", 30, ' ', false));
			movs.put("MOVIMIEN_2", Pack.resize("", 30, ' ', false));
			
//			sd.put("MOVIMIEN_1", Pack.resize("", 30, ' ', false));
		}

		if (sd.get("MOVIMIEN_2") != null) {
			
			movs.put("MOVIMIEN_3", sd.get("MOVIMIEN_2").substring(1, 31));
			movs.put("MOVIMIEN_4", sd.get("MOVIMIEN_2").substring(31, 61));
			
//			sd.put("MOVIMIEN_2", Pack.resize(sd.get("MOVIMIEN_2"), 30, ' ', false));
		} else {
			
			movs.put("MOVIMIEN_3", Pack.resize("", 30, ' ', false));
			movs.put("MOVIMIEN_4", Pack.resize("", 30, ' ', false));
			
//			sd.put("MOVIMIEN_2", Pack.resize("", 30, ' ', false));
		}

		if (sd.get("MOVIMIEN_3") != null) {
			
			movs.put("MOVIMIEN_5", sd.get("MOVIMIEN_3").substring(1, 31));
			
//			sd.put("MOVIMIEN_3", Pack.resize(sd.get("MOVIMIEN_3"), 30, ' ', false));
		} else {
			
			movs.put("MOVIMIEN_5", Pack.resize("", 30, ' ', false));
			
//			sd.put("MOVIMIEN_3", Pack.resize("", 30, ' ', false));
		}
		
		
		for(Map.Entry<String, String> c: movs.entrySet()) {
			
			sd.put(c.getKey(), c.getValue());
			
		}

//		if (sd.get("MOVIMIEN_4") != null) {
//			sd.put("MOVIMIEN_4", Pack.resize(sd.get("MOVIMIEN_4"), 30, ' ', false));
//		} else {
//			sd.put("MOVIMIEN_4", Pack.resize("", 30, ' ', false));
//		}
//
//		if (sd.get("MOVIMIEN_5") != null) {
//			sd.put("MOVIMIEN_5", Pack.resize(sd.get("MOVIMIEN_5"), 30, ' ', false));
//		} else {
//			sd.put("MOVIMIEN_5", Pack.resize("", 30, ' ', false));
//		}

	}

	public static void putB24Field63IntoStructuredData(StructuredData sd, ResponseCode rspCode) {
		sd.put(Constant.B24Fields.B24_F_63,
				rspCode.getKeyIsc() != null? rspCode.getKeyIsc().concat(Pack.resize(rspCode.getDescriptionIsc(), 40, ' ', true)) 
						: "ERROR: ".concat(Pack.resize("VALOR NO ENCONTRADO", 40, ' ', true)));
		
	}

	public static void putB24Field40IntoStructuredData(StructuredData sd) {
		sd.put(Constant.B24Fields.B24_F_40, "000");
	}

	public static void putB24Field44IntoStructuredData(StructuredData sd) {

		String field44 = "";

		try {
			String saldos = sd.get(Constant.TagNames.SALDOS);
			if (saldos != null) {

				Logger.logLine("AAA:" + saldos, false);
				if (saldos.length() < 45)
					saldos = "000000000000000000000000000000000000000000000";

				sd.put(Constant.TagNames.SALDO_TOTAL, saldos.substring(0, 15));
				sd.put(Constant.TagNames.SALDO_DISPONIBLE, saldos.substring(30, 45));

				if (sd.get(Constant.TagNames.SALDO_TOTAL) != null && sd.get(Constant.TagNames.SALDO_DISPONIBLE) != null) {

					if ((sd.get(Constant.TagNames.SALDO_TOTAL)
							.substring(0, sd.get(Constant.TagNames.SALDO_TOTAL).length() - 1).equals("-"))) {
						field44 += "3".concat("-").concat(Pack.resize(
								sd.get(Constant.TagNames.SALDO_TOTAL).substring(0, 12).replace(",", "").replace(".", ""),
								11, '0', false));
					} else {
						field44 += "3".concat(Pack.resize(
								sd.get(Constant.TagNames.SALDO_TOTAL).replace(",", "").replace(".", ""), 12, '0', false));
					}

					if ((sd.get(Constant.TagNames.SALDO_DISPONIBLE)
							.substring(sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1)).equals("-")) {
						field44 += "-".concat(Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE)
								.substring(0, sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1).replace(",", "")
								.replace(".", ""), 11, '0', false));
					} else {
						field44 = field44.concat(
								Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE).replace(",", "").replace(".", ""),
										12, '0', false));
					}

				} else if (sd.get(Constant.TagNames.SALDO_TOTAL) != null
						&& sd.get(Constant.TagNames.SALDO_DISPONIBLE) == null) {

					Logger.logLine("BBB:" + sd.get(Constant.TagNames.SALDO_TOTAL), false);

					if ((sd.get(Constant.TagNames.SALDO_TOTAL)
							.substring(0, sd.get(Constant.TagNames.SALDO_TOTAL).length() - 1).equals("-"))) {
						field44 += "1".concat("-").concat(Pack.resize(
								sd.get(Constant.TagNames.SALDO_TOTAL).substring(0, 12).replace(",", "").replace(".", ""),
								11, '0', false)).concat("000000000000");
					} else {
						field44 += "1"
								.concat(Pack.resize(sd.get(Constant.TagNames.SALDO_TOTAL).replace(",", "").replace(".", ""),
										12, '0', false))
								.concat("000000000000");
					}

				} else if (sd.get(Constant.TagNames.SALDO_TOTAL) == null
						&& sd.get(Constant.TagNames.SALDO_DISPONIBLE) != null) {

					Logger.logLine("CCC:" + sd.get(Constant.TagNames.SALDO_DISPONIBLE), false);

					if ((sd.get(Constant.TagNames.SALDO_DISPONIBLE)
							.substring(sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1)).equals("-")) {
						field44 += "2000000000000-".concat(Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE)
								.substring(0, sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1).replace(",", "")
								.replace(".", ""), 11, '0', false));
					} else {
						field44 += "2000000000000".concat(
								Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE).replace(",", "").replace(".", ""),
										12, '0', false));
					}

				}

			} else {
//			field44 += "0000000000000000000000000";

				Logger.logLine("DDD:" + sd.get(Constant.TagNames.SALDO_DISPONIBLE) != null
						? sd.get(Constant.TagNames.SALDO_DISPONIBLE)
						: "", false);

				if (sd.get(Constant.TagNames.SALDO_DISPONIBLE) != null) {

					if ((sd.get(Constant.TagNames.SALDO_DISPONIBLE)
							.substring(sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1)).equals("-")) {
						field44 += "2000000000000-".concat(Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE)
								.substring(0, sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1).replace(",", "")
								.replace(".", ""), 11, '0', false));
					} else {
						field44 += "2000000000000".concat(
								Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE).replace(",", "").replace(".", ""),
										12, '0', false));
					}

				} else {

					field44 += "0000000000000000000000000";

				}

			}
		} 
		catch (Exception e) {
			field44 += "0000000000000000000000000";
			
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			Logger.logError("SALDOS NO ENCONTRADOS: " + outError.toString(), false);
			EventRecorder.recordEvent(new Exception(outError.toString()));

		}

		sd.put(Constant.B24Fields.B24_F_44, field44);
	}
	
	public static void putB24Field4IntoStructuredData(StructuredData sd) {

		String field4 = "";

		try {
			if (sd.get(Constant.TagNames.SALDOS) != null) {

				Logger.logLine("AAA:" + sd.get(Constant.TagNames.SALDOS), false);

				sd.put(Constant.TagNames.SALDO_TOTAL, sd.get(Constant.TagNames.SALDOS).substring(0, 15));
				sd.put(Constant.TagNames.SALDO_DISPONIBLE, sd.get(Constant.TagNames.SALDOS).substring(30, 45));

				if (sd.get(Constant.TagNames.SALDO_TOTAL) != null && sd.get(Constant.TagNames.SALDO_DISPONIBLE) != null) {

					if ((sd.get(Constant.TagNames.SALDO_TOTAL)
							.substring(0, sd.get(Constant.TagNames.SALDO_TOTAL).length() - 1).equals("-"))) {
						field4 = Pack.resize(
								sd.get(Constant.TagNames.SALDO_TOTAL).substring(0, 12).replace(",", "").replace(".", ""), 12, '0', false);
					} else {
						field4 = Pack.resize(sd.get(Constant.TagNames.SALDO_TOTAL).replace(",", "").replace(".", ""), 12, '0', false);
					}

					if ((sd.get(Constant.TagNames.SALDO_DISPONIBLE)
							.substring(sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1)).equals("-")) {
						field4 = Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE)
								.substring(0, sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1).replace(",", "")
								.replace(".", ""), 12, '0', false);
					} else {
						field4 = Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE).replace(",", "").replace(".", ""),
										12, '0', false);
					}

				} else if (sd.get(Constant.TagNames.SALDO_TOTAL) != null
						&& sd.get(Constant.TagNames.SALDO_DISPONIBLE) == null) {

					Logger.logLine("BBB:" + sd.get(Constant.TagNames.SALDO_TOTAL), false);

					if ((sd.get(Constant.TagNames.SALDO_TOTAL)
							.substring(0, sd.get(Constant.TagNames.SALDO_TOTAL).length() - 1).equals("-"))) {
						field4 = Pack.resize(sd.get(Constant.TagNames.SALDO_TOTAL).substring(0, 12).replace(",", "").replace(".", ""),
								12, '0', false);
					} else {
						field4 = Pack.resize(sd.get(Constant.TagNames.SALDO_TOTAL).replace(",", "").replace(".", ""),
										12, '0', false);
					}

				} else if (sd.get(Constant.TagNames.SALDO_TOTAL) == null
						&& sd.get(Constant.TagNames.SALDO_DISPONIBLE) != null) {

					Logger.logLine("CCC:" + sd.get(Constant.TagNames.SALDO_DISPONIBLE), false);

					if ((sd.get(Constant.TagNames.SALDO_DISPONIBLE)
							.substring(sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1)).equals("-")) {
						field4 = Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE)
								.substring(0, sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1).replace(",", "")
								.replace(".", ""), 12, '0', false);
					} else {
						field4 = Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE).replace(",", "").replace(".", ""),
										12, '0', false);
					}

				}

			} else {

				Logger.logLine("DDD:" + sd.get(Constant.TagNames.SALDO_DISPONIBLE) != null
						? sd.get(Constant.TagNames.SALDO_DISPONIBLE)
						: "", false);

				if (sd.get(Constant.TagNames.SALDO_DISPONIBLE) != null) {

					if ((sd.get(Constant.TagNames.SALDO_DISPONIBLE)
							.substring(sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1)).equals("-")) {
						field4 = Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE)
								.substring(0, sd.get(Constant.TagNames.SALDO_DISPONIBLE).length() - 1).replace(",", "")
								.replace(".", ""), 12, '0', false);
					} else {
						field4 = Pack.resize(sd.get(Constant.TagNames.SALDO_DISPONIBLE).replace(",", "").replace(".", ""),
										12, '0', false);
					}

				} else {

					field4 += "0000000000000000000000000";

				}

			}
		} 
		catch (Exception e) {
			field4 += "0000000000000000000000000";
			
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			Logger.logError("SALDOS NO ENCONTRADOS: " + outError.toString(), false);
			EventRecorder.recordEvent(new Exception(outError.toString()));

		}

		sd.put(Constant.B24Fields.B24_F_4, field4);
	}

	public static void putB24Field48IntoStructuredData(StructuredData sd, boolean isCostConsult) {

		if (isCostConsult && sd.get(Constant.TagNames.AV_SEGURO) != null) {

			if (sd.get(Constant.TagNames.AV_SEGURO).equals("1")) {
				sd.put(Constant.B24Fields.B24_F_48, sd.get(Constant.B24Fields.B24_F_48)
						.substring(0, sd.get(Constant.B24Fields.B24_F_48).length() - 1).concat("1"));
			} else if (sd.get(Constant.TagNames.AV_SEGURO).equals("2")) {
				sd.put(Constant.B24Fields.B24_F_48, sd.get(Constant.B24Fields.B24_F_48)
						.substring(0, sd.get(Constant.B24Fields.B24_F_48).length() - 1).concat("2"));
			} else if (sd.get(Constant.TagNames.AV_SEGURO).equals("3")) {
				if (sd.get(Constant.TagNames.PA_FORZA).equals("S")) {
					sd.put(Constant.B24Fields.B24_F_48, sd.get(Constant.B24Fields.B24_F_48)
							.substring(0, sd.get(Constant.B24Fields.B24_F_48).length() - 1).concat("3"));
				} else if (sd.get(Constant.TagNames.PA_FORZA).equals("N")
						|| sd.get(Constant.TagNames.PA_FORZA).equals("99")) {
					sd.put(Constant.B24Fields.B24_F_48, sd.get(Constant.B24Fields.B24_F_48)
							.substring(0, sd.get(Constant.B24Fields.B24_F_48).length() - 1).concat("4"));
				}
			}

//			sd.put(Constant.B24Fields.B24_F_48,
//					sd.get(Constant.TagNames.AV_SEGURO) != null ? sd.get(Constant.B24Fields.B24_F_48)
//							.substring(0, sd.get(Constant.B24Fields.B24_F_48).length() - 1)
//							.concat(sd.get(Constant.TagNames.AV_SEGURO)) : sd.get(Constant.B24Fields.B24_F_48));
		} else {
			sd.put("B24_Field_48", sd.get("B24_Field_48"));
		}
	}

	public static void setHeaderField9(ISCReqMessage output, Iso8583Post inputMsg, String hour4Check, boolean isNextDay,
			ISCInterfaceCB instance, boolean isRev) throws Exception {

		if (!isNextDay) {

			Logger.logLine("[requestISOFields2ISCFields][NextDay false]", false);

			BusinessCalendar cal = null; // Calendario de Postilion "DefaultBusinessCalendar"
			Date b24Field17Date = null; // Fecha entrante en TAG "B24_Field_17"
			Date systDate = null; // Hora de sistema
			Date calCurBDate = null; // Business day en el que se encuantra el calendario
			Date calNextBDate = null; // Proximo Business day en el que se encuantra el calendario

			cal = new BusinessCalendar("DefaultBusinessCalendar"); // Obtiene "DefaultBusinessCalendar"

			b24Field17Date = Utils.string2Date(Utils.getStringDate(Utils.YYMMDDhhmmss).substring(0, 2)
					.concat(inputMsg.getStructuredData().get("B24_Field_17")), "yyMMdd");
			systDate = Utils.string2Date(Utils.getStringDate(Utils.YYMMDDhhmmss).substring(0, 6), "yyMMdd");
			calCurBDate = cal.getCurrentBusinessDate();
			calNextBDate = cal.getNextBusinessDate();

			Logger.logLine("[requestISOFields2ISCFields][Cur Business day]  \n[b24Field17Date] " + b24Field17Date
					+ "\n[systDate] " + systDate + "\n[calCurBDate] " + calCurBDate + "\n[calNextBDate] "
					+ calNextBDate, false);

			if (cal.isHoliday(systDate) || !cal.isBusinessDay(systDate)) { // la fecha del sistema indica que es feriado
																			// o fin de semana

				// DIA NO LABORABLE o DIA FERIADO

				Logger.logLine("[requestISOFields2ISCFields][FERIADO o FINSEMANA]", false);

				if (!isRev) {

					output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));
				} else {

					output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("188"));
				}

			}

			else if (cal.isBusinessDay(systDate)) {

				// DIA LABORABLE

				Logger.logLine("[requestISOFields2ISCFields][CALENDARIO]", false);

				if (Utils.checkThisHour(hour4Check, Utils.getStringDate(Utils.YYMMDDhhmmss).substring(6))) {

					// Hora EN rango

					Logger.logLine("[requestISOFields2ISCFields][HORA EN RANGO]", false);

					if (!isRev) {

						output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("040"));
					} else {

						output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("ðüð"));
					}

					instance.isNextDay = true;

				} else {
					// Hora FUERA rango

					Logger.logLine("[requestISOFields2ISCFields][HORA FUERA RANGO]", false);

					if (b24Field17Date.compareTo(calCurBDate) > 0) { // la fecha de la transaccion es futura a la fecha
																		// current en el calendario

						// FECHA FUTURA

						Logger.logLine("[requestISOFields2ISCFields][B24_Field_17 FUTURO]", false);

						if (!isRev) {

							output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("040"));
						} else {

							output.putField(ISCReqMessage.Fields._09_H_STATE,
									Transform.fromAsciiToEbcdic(UtilidadesMensajeria.ebcdicToAscii("F0FCF0")));
						}
					} else {

						if (!isRev) {

							output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));
						} else {

							output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("188"));
						}

					}

				}

			}

		} else {

			Logger.logLine("[requestISOFields2ISCFields][NextDay true]", false);

			if (!isRev) {

				output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("040"));
			} else {

				output.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("ðüð"));
			}

		}

	}

	public static Properties readPropsFromBasePropFile() {

		Properties props = null;

		try (InputStream input = new FileInputStream("./resource/security.properties")) {

			props = new Properties();

			// load a properties file
			props.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return props;
	}

	public boolean checkThisDay(String daysMap, String range, String transLocalTime) {

		String[] daysFromMap = daysMap.substring(1, daysMap.length() - 1).split("-");

		Calendar cal = Calendar.getInstance();
		int today = cal.get(Calendar.DAY_OF_WEEK) - 1;

		if (daysFromMap[today].equals("1")) {

			return checkThisHour(range, transLocalTime);
		} else {
			return false;
		}

	}

	/**
	 * 
	 * @param hour4Check
	 * @param transLocalTime
	 * @return
	 */

	public static boolean checkThisHour(String hour4Check, String transLocalTime) {

		Logger.logLine("[checkThisHour] " + hour4Check + " :: " + transLocalTime, false);

		boolean isHourInRange = false;

		int minRange = Integer
				.parseInt(hour4Check.replaceAll(":", "").length() <= 4 ? hour4Check.replaceAll(":", "") + ""
						: hour4Check.replaceAll(":", "") + "00");

		int localHour = Integer.parseInt(transLocalTime.substring(0, 4));

		if (localHour > minRange) {
			isHourInRange = true;
		}

		Logger.logLine("[checkThisHour] isHourInRange:" + isHourInRange, false);
		return isHourInRange;
	}

	public static Date string2Date(String curDate, String format) throws ParseException {
		Logger.logLine("[string2Date] " + curDate + " :: " + format, false);
		Date date1 = new SimpleDateFormat(format).parse(curDate);
		return (date1);

	}

	public static boolean search4covenant(Iso8583Post msg) throws XPostilion {

		String primCov = "";
		String secCov = "";
		boolean covFounded = true;
		StructuredData sd = msg.getStructuredData();

		Logger.logLine("COVENANTS: " + ISCInterfaceCB.convenios.size(), false);

		primCov = ISCInterfaceCB.convenios.get(msg.getStructuredData().get("B24_Field_103").substring(10)) != null
				? ISCInterfaceCB.convenios.get(msg.getStructuredData().get("B24_Field_103").substring(10))
				: "";

		Logger.logLine("CONVENIO PRIM BUSQUEDA - " + msg.getStructuredData().get("B24_Field_103").substring(10) + " "
				+ primCov, false);

		if (!primCov.equals("") && primCov != null) {
			sd.put("PRIM_COV_ACCOUNT_TYPE",
					primCov.split("\\|")[0].equals("") || primCov.split("\\|")[0].equals(" ") ? "0"
							: primCov.split("\\|")[0]);
			sd.put("PRIM_COV_ACCOUNT_NR",
					primCov.split("\\|")[1].equals("") || primCov.split("\\|")[1].equals(" ") ? "0"
							: primCov.split("\\|")[1]);
			sd.put("PRIM_COV_SERVICE_NAME",
					primCov.split("\\|")[2].equals("") || primCov.split("\\|")[2].equals(" ") ? "0"
							: primCov.split("\\|")[2]);
			sd.put("PRIM_COV_SERVICE_TYPE",
					primCov.split("\\|")[3].equals("") || primCov.split("\\|")[3].equals(" ") ? "0"
							: primCov.split("\\|")[3]);
			sd.put("PRIM_COV_REPRO_INDICATOR",
					primCov.split("\\|")[4].equals("") || primCov.split("\\|")[4].equals(" ") ? "0"
							: primCov.split("\\|")[4]);
			sd.put("PRIM_COV_ABO", primCov.split("\\|")[5].equals("") || primCov.split("\\|")[5].equals(" ") ? "0"
					: primCov.split("\\|")[5]);
			sd.put("PRIM_COV_PAYMENT_TYPE",
					primCov.split("\\|")[6].equals("") || primCov.split("\\|")[6].equals(" ") ? "0"
							: primCov.split("\\|")[6]);

			secCov = msg.getStructuredData().get("B24_Field_104").substring(17) != null
					? ISCInterfaceCB.convenios.get(msg.getStructuredData().get("B24_Field_103").substring(10)
							.concat(msg.getStructuredData().get("B24_Field_104").substring(16)))
					: "";

			Logger.logLine("CONVENIO SEC BUSQUEDA: " + secCov, false);

			if (secCov != "" && secCov != null) {
				sd.put("SEC_COV_ACCOUNT_TYPE",
						secCov.split("\\|")[0].equals("") || secCov.split("\\|")[0].equals(" ") ? "0"
								: secCov.split("\\|")[0]);
				sd.put("SEC_COV_ACCOUNT_NR",
						secCov.split("\\|")[1].equals("") || secCov.split("\\|")[1].equals(" ") ? "0"
								: secCov.split("\\|")[1]);
				sd.put("SEC_COV_SERVICE_NAME",
						secCov.split("\\|")[2].equals("") || secCov.split("\\|")[2].equals(" ") ? "0"
								: secCov.split("\\|")[2]);
				sd.put("SEC_COV_SERVICE_TYPE",
						secCov.split("\\|")[3].equals("") || secCov.split("\\|")[3].equals(" ") ? "0"
								: secCov.split("\\|")[3]);
				sd.put("SEC_COV_REPRO_INDICATOR",
						secCov.split("\\|")[4].equals("") || secCov.split("\\|")[4].equals(" ") ? "0"
								: secCov.split("\\|")[4]);
				sd.put("SEC_COV_ABO", primCov.split("\\|")[5].equals("") || primCov.split("\\|")[5].equals(" ") ? "0"
						: secCov.split("\\|")[5]);
				sd.put("SEC_COV_PAYMENT_TYPE",
						secCov.split("\\|")[6].equals("") || secCov.split("\\|")[6].equals(" ") ? "0"
								: secCov.split("\\|")[6]);
			}

			Logger.logLine(
					"PRIM COVENANT " + msg.getStructuredData().get("B24_Field_103").substring(10) + ":" + primCov,
					false);

		} else {
			covFounded = false;
			sd.put("COVENANT_NOT_FOUND", "COVENANT_NOT_FOUND");
			Logger.logLine("COVENANT_NOT_FOUND", false);
		}

		msg.putStructuredData(sd);

		Logger.logLine("CONVENIO SD: " + msg.getStructuredData(), false);

		return covFounded;
	}

	public static ISCReqMessage createEchoTestMsg() throws XPostilion {

		ISCReqMessage echo = new ISCReqMessage();
		prepareEchoMessage(echo);
		Logger.logLine("ECHO MSG\n" + echo.toString(), false);
		return echo;
	}

	public static ISCReqMessage prepareEchoMessage(ISCReqMessage echo) throws XPostilion {

		echo.setConstantHeaderFields();

		echo.putField(ISCReqMessage.Fields._06_H_ATM_ID, Transform.fromAsciiToEbcdic("AT000"));

		echo.putField(ISCReqMessage.Fields._08_H_TRAN_SEQ_NR, Transform.fromAsciiToEbcdic("9999"));

		echo.putField(ISCReqMessage.Fields._10_H_TIME,
				Transform.fromAsciiToEbcdic(Utils.getStringDate(YYMMDDhhmmss).substring(6)));

		echo.putField(ISCReqMessage.Fields._05_H_TRAN_CODE, Transform.fromAsciiToEbcdic("ATLG"));

		echo.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromAsciiToEbcdic("008"));

		echo.putField(ISCReqMessage.Fields._VARIABLE_BODY, prepareEchoBody());

		return echo;
	}

	public static String prepareEchoBody() {
		StringBuilder sd = new StringBuilder("");

		sd.append(Transform.fromHexToBin("114CEA")).append(Transform.fromAsciiToEbcdic("AT000"))
				.append(Transform.fromHexToBin("114CE9")).append(Transform.fromAsciiToEbcdic("@@@@@@@@"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119181_ORIGINAL_TRAN_1))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_119199_TERM_LOCATION_40))
				.append(Transform.fromAsciiToEbcdic("0000000000000000000000000000000000000000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F2_IDEN_DOC_TYPE_1))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11E4F3_IDEN_DOC_NR_16))
				.append(Transform.fromAsciiToEbcdic("0000000000000000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants.TAG_11A9B1_TRAN_INDICATOR_1))
				.append(Transform.fromAsciiToEbcdic("0"));

		return sd.toString();
	}

	public static IMessage processReqISCMsg(WholeTransSetting transMsgsConfig, ISCReqInMsg iscInReq, FlowDirection dir, String cons, boolean enableMonitor) throws XPostilion, FileNotFoundException {
		
		Iso8583Post mappedIso = new Iso8583Post();
		
		switch (Transform.fromEbcdicToAscii(iscInReq.getField(ISCReqInMsg.Fields._08_H_STATE))) {
		case "080":
			mappedIso.setMessageType(Iso8583.MsgTypeStr._0420_ACQUIRER_REV_ADV);
			break;
		case "020":
			mappedIso.setMessageType(Iso8583.MsgTypeStr._0200_TRAN_REQ);
			break;

		default:
			mappedIso.setMessageType(Iso8583.MsgTypeStr._0200_TRAN_REQ);
			break;
		}

		
		//CONSTRUCCION DE LA LLAVE DEL MSG
		String msgKey = constructMessageKeyISC2ISO(iscInReq, mappedIso, enableMonitor);

		//RECUPERACION DE LA CONFIGURACION JSON PARA LA LLAVE
		TransactionSetting tSettings = findTranSetting(transMsgsConfig, msgKey, enableMonitor);
		try {
			//VERIFICAR SI LA TRANSACCION TIENE CLASE AUXILIAR
			if (tSettings != null && tSettings.getAuxiliarClass() != null) {
				
				verifyForAuxClass(mappedIso, iscInReq, tSettings, cons, enableMonitor);
			}
			mappedIso = constructMsgISO(tSettings, iscInReq, mappedIso, enableMonitor);
		}
		catch(Exception e) {
			e.printStackTrace();
			EventRecorder.recordEvent(
					new Exception("ERROR processReqISCMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processReqISCMsg" + "]\n" + "processReqISCMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}

		Logger.logLine("OUTPUT:" + mappedIso.toString(), enableMonitor);
		return mappedIso;
	}
	
public static IMessage processAutraReqISCMsg(WholeTransSetting transMsgsConfig, ISCReqInMsg iscInReq, FlowDirection dir, String cons, boolean enableMonitor) throws XPostilion, FileNotFoundException {
		
		Iso8583Post mappedIso = new Iso8583Post();
		
		if (Transform.fromEbcdicToAscii(iscInReq.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("080")
				|| Transform.fromEbcdicToAscii(iscInReq.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) {
			mappedIso.setMessageType(Iso8583.MsgTypeStr._0420_ACQUIRER_REV_ADV);
		}else {
			mappedIso.setMessageType(Iso8583.MsgTypeStr._0200_TRAN_REQ);
		}
		
		//CONSTRUCCION DE LA LLAVE DEL MSG
		String msgKey = "CONVIVENCIA";

		//RECUPERACION DE LA CONFIGURACION JSON PARA LA LLAVE
		TransactionSetting tSettings = findTranSetting(transMsgsConfig, msgKey, enableMonitor);
		try {
			//VERIFICAR SI LA TRANSACCION TIENE CLASE AUXILIAR
			if (tSettings != null && tSettings.getAuxiliarClass() != null) {
				
				verifyForAuxClass(mappedIso, iscInReq, tSettings, cons, enableMonitor);
			}
			mappedIso = constructMsgISO(tSettings, iscInReq, mappedIso, enableMonitor);
		}
		catch(Exception e) {
			e.printStackTrace();
			EventRecorder.recordEvent(
					new Exception("ERROR processReqISCMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processReqISCMsg" + "]\n" + "processReqISCMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}

		Logger.logLine("OUTPUT:" + mappedIso.toString(), enableMonitor);
		return mappedIso;
	}
	
//	public static IMessage processReqISOMsg(WholeTransSetting transMsgsConfig, Iso8583Post isoInReq) throws XPostilion, FileNotFoundException {
//		
//		Iso8583Post mappedIso = new Iso8583Post();
//		mappedIso.setMessageType(Iso8583.MsgTypeStr._0200_TRAN_REQ);
//		
//		//CONSTRUCCION DE LA LLAVE DEL MSG
//		String msgKey = constructMessageKeyISO2ISC(isoInReq, mappedIso);
//
//		//RECUPERACION DE LA CONFIGURACION JSON PARA LA LLAVE
//		TransactionSetting tSettings = findTranSetting(transMsgsConfig, msgKey, true);
//		
//		//VERIFICAR SI LA TRANSACCION TIENE CLASE AUXILIAR
//		if (tSettings != null && tSettings.getAuxiliarClass() != null) {
//			
//			String cons = getTransactionConsecutive("AT", "00", "1");
//			
//			verifyForAuxClass(mappedIso, isoInReq, tSettings, cons);
//		}
//
//		
//		try {
//			mappedIso = constructMsgISO(tSettings, isoInReq, mappedIso);
//		}
//		catch(Exception e) {
//			StringWriter outError = new StringWriter();
//			e.printStackTrace(new PrintWriter("ERROR COPYING FIELD: " +  outError.toString()));
//			Logger.logLine("ERROR COPYING FIELD: " + outError.toString(), true);
//		}
//
//		Logger.logLine("OUTPUT:" + mappedIso.toString(), true);
//		return mappedIso;
//	}

	protected static void verifyForAuxClass(Iso8583Post out, ISCReqInMsg in, TransactionSetting tSettings, String cons, boolean enableMonitor) {
		
		Logger.logLine("postilion.realtime.iscinterface.auxiliar." + tSettings.getAuxiliarClass(), enableMonitor);

		try {
			
//			Class<?> classRequest = Class.forName("postilion.realtime.iscinterface.auxliar.".concat(tSettings.getAuxiliarClass()));	
			Class<?> classRequest = Class.forName("postilion.realtime.iscinterface.auxiliar." + tSettings.getAuxiliarClass());
            Class<?>[] argtypes = { Iso8583Post.class, ISCReqInMsg.class, TransactionSetting.class, String.class, boolean.class};

            Constructor<?> constructor = classRequest.getConstructor();
            Object obj = constructor.newInstance();
            
            Method methodExec = classRequest.getMethod("processMsg", argtypes);
            Object[] args = {out, in,  tSettings, cons, enableMonitor};
            
            out = (Iso8583Post)methodExec.invoke(obj, args);
            
            Logger.logLine("salida reflection: ISO"+out.toString(), enableMonitor);
			
			
		} catch (Exception e) {
			
			Logger.logLine("ERROR REFLECTION: " + e.getLocalizedMessage(), enableMonitor);
			Logger.logLine("ERROR REFLECTION: " + e.toString(), enableMonitor);
			e.printStackTrace();
			EventRecorder.recordEvent(
					new Exception("ERROR verifyForAuxClass: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "verifyForAuxClass" + "]\n" + "verifyForAuxClass: " + "\n",
					Utils.getStringMessageException(e) }));
		}        
			
	}

	public static TransactionSetting findTranSetting(WholeTransSetting wholeTransConfig, String msgTranKey,
			boolean enableLog) {

		TransactionSetting tranSetting = null;

		Logger.logLine("TRANKEY: " + msgTranKey, enableLog);

		if (wholeTransConfig != null && wholeTransConfig.getAllTran() != null
				&& wholeTransConfig.getAllTran().length != 0) {

			for (int i = 0; i < wholeTransConfig.getAllTran().length; i++) {

				Logger.logLine("TRANKEY IN JSON: " + wholeTransConfig.getAllTran()[i].getTranKey(), enableLog);

				if (wholeTransConfig.getAllTran()[i].getTranKey().equals(msgTranKey)) {

					Logger.logLine("-- TRANKEY MATCHED --", enableLog);
					Logger.logLine(wholeTransConfig.getAllTran()[i].getDescription(), enableLog);
					tranSetting = wholeTransConfig.getAllTran()[i];
					break;
				}
			}
		}
		
		if (tranSetting == null) {
			Logger.logLine("NO TRAN MATCH IN JSON: ", enableLog);
		}

		return tranSetting;
	}

	private static Iso8583Post constructMsgISO(TransactionSetting trSetting, ISCReqInMsg inputMsg, Iso8583Post mappedMsg, boolean enableMonitor)
			throws XPostilion, FileNotFoundException {
		
		Logger.logLine("constructMsgISO 3251:" + trSetting.getFields().length, enableMonitor);
		Logger.logLine("constructMsgISO 3252:" + inputMsg, enableMonitor);
		Logger.logLine("constructMsgISO 3253:" + inputMsg.getTotalHexString(), enableMonitor);

		
		StructuredData sd = null;
		
		if (mappedMsg.getStructuredData() != null) {
			sd = mappedMsg.getStructuredData();
		}
		else {
			sd = new StructuredData();
		}

		for (int i = 0; i < trSetting.getFields().length; i++) {

			Field cf = trSetting.getFields()[i];
			
			Logger.logLine("Field: " + cf.getDescription(), enableMonitor);

//			mappedMsg.putField(Byte.parseByte(cf.getCopyTag()),
//					inputMsg.getField(cf.getCopyTag()).substring(cf.getCopyInitialIndex(), cf.getCopyFinalIndex()));
			
			switch (cf.getFieldType()) {
			
			case "fixed":
				
				fixedField(cf, mappedMsg, sd);
				
				break;
				
			case "copy":
				
				copyField(cf, mappedMsg, inputMsg, sd, enableMonitor);
				
				break;
				
//			case "homologate":
//				
//				break;

			default:
				break;
			}
			

		}

		mappedMsg.putStructuredData(sd);

		return mappedMsg;
	}
	
	/**************************************************************************************
	 * Metodo para consultar consecutivo para la transacciï¿½n
	 * 
	 * @param atmId
	 * @return
	 **************************************************************************************/
	public static String getTransactionConsecutive(String termPrefix, String term, String termConsSection) {
		String output = null;

		// To-DO consultar consecutivo
		output = DBHandler.getCalculateConsecutive("AT", term, termConsSection, false);

		return output;
	}

//	private static HashMap<String, String> getBodyReqTags(String hexReq) {
//		
//		HashMap<String, String> tags = new HashMap<>();
//		
//		Logger.logLine("EXTRAYENDO TAGS: " + hexReq, false);
//		
//		String[] rawTags = hexReq.substring(hexReq.indexOf("119130")).split("[1]{2}([A-Z]{1}|[2-9]{1})");
//		
//		Logger.logLine("RAWTAGS SIZE: " + rawTags.length, false);
//		
//		for (int i = 1; i < rawTags.length; i++) {
//			
//			tags.put(hexReq.substring(hexReq.indexOf(rawTags[i]) - 3, hexReq.indexOf(rawTags[i]) + 3), Transform.fromEbcdicToAscii(Transform.fromHexToBin(rawTags[i].substring(3))));
//			Logger.logLine("TAG: " + hexReq.substring(hexReq.indexOf(rawTags[i]) - 3, hexReq.indexOf(rawTags[i]) + 3) + " VALOR: " + Transform.fromEbcdicToAscii(Transform.fromHexToBin(rawTags[i].substring(3))), false);
//			
//		}
//		
//		return tags;
//		
//	}

	public static WholeTransSetting retriveJsonConfig(String jsonURL, boolean enableLog) {

		WholeTransSetting transConfig = null;

		try {
			// create object mapper instance
			ObjectMapper mapper = new ObjectMapper();

			// convert JSON string to wholeTransConfig object
			transConfig = mapper.readValue(Paths.get(jsonURL).toFile(), WholeTransSetting.class);

			// print book
			Logger.logLine("JSON TRANS COFIG:" + jsonURL + " " + transConfig.toString(), false);

		} catch (Exception ex) {
			StringWriter outError = new StringWriter();
			ex.printStackTrace(new PrintWriter(outError));
			Logger.logLine("ERROR JSON TRANS COFIG: " + outError.toString(), false);
			EventRecorder.recordEvent(new Exception(outError.toString()));
		}

		return transConfig;
	}

	public static Properties retriveNDPropertiesFile(String propertiesFileURL, String interName, boolean isNextDay)
			throws IOException {

		InputStream inp = new FileInputStream(propertiesFileURL);

		Properties pfile = new Properties();
		pfile.load(inp);

		if (pfile.getProperty(interName) != null) {

			isNextDay = Boolean.valueOf(pfile.getProperty(interName));

		} else {

			isNextDay = false;
			updateNextdayPersistence(propertiesFileURL, interName, "false", true);

		}

		return pfile;
	}

	private static void updateNextdayPersistence(String propertiesFileURL, String property2Update, String value2update,
			boolean enableLog) {

		try (OutputStream output = new FileOutputStream(propertiesFileURL)) {
			Properties prop = new Properties(); // set the properties value
			prop.setProperty(property2Update, value2update);
			prop.store(output, null);
			Logger.logLine("UPDATE PROPS:\n" + prop.toString(), false);
		}

		catch (IOException io) {
			StringWriter outError = new StringWriter();
			io.printStackTrace(new PrintWriter(outError));
			Logger.logLine("ERROR IO: " + outError.toString(), false);
			EventRecorder.recordEvent(new Exception(outError.toString()));
		}

	}

	private static String constructMessageKeyISC2ISO(ISCReqInMsg msg, Iso8583Post output, boolean enableMonitor) throws XPostilion {

		Logger.logLine("Utils 3353: \n" + msg.toString(), enableMonitor);

		String msgKey = "";

		Logger.logLine("TRAN CODE: " + Transform.fromEbcdicToAscii(msg.getField(ISCReqInMsg.Fields._02_H_TRAN_CODE)), enableMonitor);
		Logger.logLine("AUTRA CODE: " + Transform.fromEbcdicToAscii(msg.getField(ISCReqInMsg.Fields._04_H_AUTRA_CODE)), enableMonitor);		
		String codOficina = Transform.fromEbcdicToAscii(Transform.fromHexToBin(msg.getTotalHexString().substring(30, 38)));

		if (Transform.fromEbcdicToAscii(msg.getField(ISCReqInMsg.Fields._02_H_TRAN_CODE)).equals("SRLN")) {

			switch (Transform.fromEbcdicToAscii(msg.getField(ISCReqInMsg.Fields._04_H_AUTRA_CODE))) {
			case "8500":
				msgKey = "SRLN_8500_VALIDACIONPIN";
				break;
			case "8510":
				msgKey = "SRLN_8510_CONSULDIV";
				break;
			case "8520":
				if (codOficina.startsWith("80")) {
					msgKey = "SRLN_8520_PAGOCONVENIOSINT";
				}else
					msgKey = "SRLN_8520_CONSULPAGODIV";
				//msgKey = msgKey.concat("_").concat("8520");
				break;
			case "8550":
				msgKey = get8550ExtentedKey(msg, output, enableMonitor);
				break;
			case "8554":
                msgKey = "SRLN_8554_TRANSFERQR";
                break;
			case "8570":
                msgKey = "SRLN_8570_CONSULTATITULARIDAD";
                break;
			default:
				msgKey = "06";
				break;
			}
		}
		return msgKey;
	}

	private boolean getCovenantISC2ISO(Iso8583Post isoMsg,String covNr, String cov2Nr, StructuredData sd) throws XPostilion {

		boolean foundedCovenant = false;

		String primCov = ISCInterfaceCB.convenios.get(covNr);

		Logger.logLine("CONVENIO PRIM BUSQUEDA - " + covNr, false);

		if (!primCov.equals("") && primCov != null) {
			foundedCovenant = true;
			sd.put("PRIM_COV_ACCOUNT_TYPE", primCov.split("\\|")[0].equals("") ? "0" : primCov.split("\\|")[0]);
			sd.put("PRIM_COV_ACCOUNT_NR", primCov.split("\\|")[1].equals("") ? "0" : primCov.split("\\|")[1]);
			sd.put("PRIM_COV_SERVICE_NAME", primCov.split("\\|")[2].equals("") ? "0" : primCov.split("\\|")[2]);
			sd.put("PRIM_COV_SERVICE_TYPE", primCov.split("\\|")[3].equals("") ? "0" : primCov.split("\\|")[3]);
			sd.put("PRIM_COV_REPRO_INDICATOR", primCov.split("\\|")[4].equals("") ? "0" : primCov.split("\\|")[4]);
			sd.put("PRIM_COV_ABO", primCov.split("\\|")[5].equals("") ? "0" : primCov.split("\\|")[5]);
			sd.put("PRIM_COV_PAYMENT_TYPE", primCov.split("\\|")[6].equals("") ? "0" : primCov.split("\\|")[6]);

			if (cov2Nr != null) {
				String secCov = ISCInterfaceCB.convenios.get(covNr.concat(cov2Nr));
				
				Logger.logLine("SEC COVENANT:" + secCov, false);

				if (secCov != "" && secCov != null) {
					sd.put("SEC_COV_ACCOUNT_TYPE", secCov.split("\\|")[0].equals("") ? "0" : secCov.split("\\|")[0]);
					sd.put("SEC_COV_ACCOUNT_NR", secCov.split("\\|")[1].equals("") ? "0" : secCov.split("\\|")[1]);
					sd.put("SEC_COV_SERVICE_NAME", secCov.split("\\|")[2].equals("") ? "0" : secCov.split("\\|")[2]);
					sd.put("SEC_COV_SERVICE_TYPE", secCov.split("\\|")[3].equals("") ? "0" : secCov.split("\\|")[3]);
					sd.put("SEC_COV_REPRO_INDICATOR", secCov.split("\\|")[4].equals("") ? "0" : secCov.split("\\|")[4]);
					sd.put("SEC_COV_ABO", primCov.split("\\|")[5].equals("") ? "0" : secCov.split("\\|")[5]);
					sd.put("SEC_COV_PAYMENT_TYPE", secCov.split("\\|")[6].equals("") ? "0" : secCov.split("\\|")[6]);
				}

			}

			isoMsg.putStructuredData(sd);

		}

		return foundedCovenant;
	}
	
	private static void fixedField(Field curField, Iso8583Post outputMsg, StructuredData sd) throws  XPostilion {

		String extractedVal = "";

		extractedVal = curField.getValue();
		extractedVal = Pack.resize(extractedVal, curField.getTagValueLength(), '0', false);
		
		Logger.logLine("Extracted:"+extractedVal, false);
		
		if(curField.getCopyTo().split("\\|")[0].equals("0")) {
			outputMsg.putField(Integer.parseInt(curField.getCopyTo().split("\\|")[1]), extractedVal);
		}
		else if (curField.getCopyTo().split("\\|")[0].equals("1")) {	
			sd.put(curField.getCopyTo().split("\\|")[1], extractedVal);		
		}

	}

	private static void copyField(Field curField, Iso8583Post outputMsg, ISCReqInMsg inputIscMsg, StructuredData sd, boolean enableMonitor)
			throws FileNotFoundException {

		String extractedVal = "";

		try {	
			Logger.logLine("DATA IN: (HEX)"+Transform.fromEbcdicToAscii(Transform.fromHexToBin(inputIscMsg.getTotalHexString())), enableMonitor);
			Logger.logLine("SUBSTRING : (HEX) init val: "+ curField.getCopyInitialIndex() + " final val:" + curField.getCopyFinalIndex() 
			+ " value extracted: " + inputIscMsg.getTotalHexString().substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex()), enableMonitor);
			extractedVal = Transform.fromEbcdicToAscii(Transform.fromHexToBin(inputIscMsg.getTotalHexString().substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex())));
			Logger.logLine("extractedVal without packResize:"+extractedVal, enableMonitor);
			extractedVal = Pack.resize(extractedVal, curField.getTagValueLength(), '0', false);

			Logger.logLine("Extracted:"+extractedVal, enableMonitor);
			
			if(curField.getCopyTo().split("\\|")[0].equals("0")) {
				outputMsg.putField(Integer.parseInt(curField.getCopyTo().split("\\|")[1]), extractedVal);
			}
			else if (curField.getCopyTo().split("\\|")[0].equals("1")) {	
				sd.put(curField.getCopyTo().split("\\|")[1], extractedVal);		
			}
			
		} 
		catch (Exception e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(
					outError + "\n" + "ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString()));
			Logger.logLine("ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString(), enableMonitor);
			EventRecorder.recordEvent(
					new Exception("ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString()));
		}

//			extractedVal = (curField.getCopyFinalIndex() == 0) ? msgSD.get(curField.getCopyTag()) :
//				msgSD.get(curField.getCopyTag()).substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex()) ;
//			extractedVal = msgSD.get(curField.getCopyTag()).substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex() == 0 ? msgSD.get(curField.getCopyTag()).length() - 1 : curField.getCopyFinalIndex()) ;	

	}

	private static void homologateField(Field curField, Iso8583Post outputMsg, ISCReqInMsg inputMsg, StructuredData sd) throws NumberFormatException, XPostilion, FileNotFoundException {

		String extractedVal = "";
		boolean homolMatch = false;

		try {
			
			extractedVal = inputMsg.getTotalHexString().substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex());
			extractedVal = UtilidadesMensajeria.ebcdicToAscii(Pack.resize(extractedVal, curField.getTagValueLength(), '0', false));
			
		} 
		catch (Exception e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(
					outError + "\n" + "ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString()));
			Logger.logLine("ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString(), false);
			EventRecorder.recordEvent(
					new Exception("ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString()));
		}

		Logger.logLine(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
				+ UtilidadesMensajeria.asciiToEbcdic(extractedVal), false);

		for (Homologation h : Arrays.asList(curField.getHomologations())) {

			if (h.getValue().equals(extractedVal)) {
				Logger.logLine("HOMOLOGACION MATCH-- homologation: " + h.getValue() + " ExtracVal" + extractedVal
						+ " convertion" + h.getConvertion(), false);
				extractedVal = h.getConvertion();
				

				if(curField.getCopyTo().split("\\|")[0].equals("0")) {
					outputMsg.putField(Integer.parseInt(curField.getCopyTo().split("\\|")[1]), extractedVal);
				}
				else if (curField.getCopyTo().split("\\|")[0].equals("1")) {	
					sd.put(curField.getCopyTo().split("\\|")[1], extractedVal);		
				}
				
				
				homolMatch = true;
				Logger.logLine("VAL homologated: " + extractedVal, false);
				break;
			}

		}

		if (!homolMatch) {

			extractedVal = curField.getConditionalVal();

		}
	}
	
	private static String get8550ExtentedKey (ISCReqInMsg isc, Iso8583Post out, boolean enableMonitor) throws XPostilion {
		
		Logger.logLine("Utils 3601:", enableMonitor);
		
		StructuredData sd = new StructuredData();
		
		String hexIsc = isc.getTotalHexString().toUpperCase();
		Logger.logLine("hexIsc:"+ hexIsc, enableMonitor);
		
		Logger.logLine("entra switch :"+ Transform.fromEbcdicToAscii(Transform.fromHexToBin(hexIsc.substring(ISCReqInMsg.POS_ini_TRAN_NATURE, ISCReqInMsg.POS_end_TRAN_NATURE))), enableMonitor);
		
		String codOficina = Transform.fromEbcdicToAscii(Transform.fromHexToBin(isc.getTotalHexString().substring(30, 38)));
		
		//Se extrae la naturaleza del mensaje para ser evaluada en el switch
		//y se determina que tipo de transaccion es.
		switch (Transform.fromEbcdicToAscii(Transform.fromHexToBin(hexIsc.substring(ISCReqInMsg.POS_ini_TRAN_NATURE, ISCReqInMsg.POS_end_TRAN_NATURE)))) {
		case "0":		
			if( codOficina.equals("8592") ) {
				sd.put("TRAN_KEY_INTERLNAL","SRLN_8550_TRANSFER_CEL2CEL");
			} else {
				sd.put("TRAN_KEY_INTERLNAL","SRLN_8550_TRANSFER");
			}
			break;
		case "1":		
			sd.put("TRAN_KEY_INTERLNAL","SRLN_8550_HIPOTECARIO");		
			break;
		case "2":		
			sd.put("TRAN_KEY_INTERLNAL","SRLN_8550_ROTATIVO");		
			break;
		case "3":				
			sd.put("TRAN_KEY_INTERLNAL","SRLN_8550_CONSUMO");	
			break;
		case "4":		
			sd.put("TRAN_KEY_INTERLNAL","SRLN_8550_MOTOSVEHICULOS");		
			break;
		case "5":		
			sd.put("TRAN_KEY_INTERLNAL","SRLN_8550_PAGOTDC");		
			break;
		case "6":	
			sd.put("TRAN_KEY_INTERLNAL","SRLN_8550_RETIROAVANCE");	
			break;
		default:
			break;
		}	
		
		// Se extrae la informacion de la oficina con el fin de identificar la transaccion Pago credito Internet 
		if (codOficina.equals("5300") || codOficina.equals("5600")) {
			sd.put("TRAN_KEY_INTERLNAL","SRLN_8550_PAGOCREDITOINT");	
		} 
		
		Logger.logLine("entra switch 2 :"+ Transform.fromEbcdicToAscii(Transform.fromHexToBin(hexIsc.substring(ISCReqInMsg.POS_ini_MSG_TYPE, ISCReqInMsg.POS_end_MSG_TYPE))), enableMonitor);
		
		switch (Transform.fromEbcdicToAscii(Transform.fromHexToBin(hexIsc.substring(ISCReqInMsg.POS_ini_MSG_TYPE, ISCReqInMsg.POS_end_MSG_TYPE)))) {
		case "000":
			sd.put("STATE_BYTE_INTERLNAL","NORMAL");
			break;
		case "010":
			sd.put("STATE_BYTE_INTERLNAL","DEVOLUCION");
			break;
		case "020":
			sd.put("STATE_BYTE_INTERLNAL","ANULACION");
			break;
		case "030":
			sd.put("STATE_BYTE_INTERLNAL","ANULACIONDEV");
			break;
		case "040":
			sd.put("STATE_BYTE_INTERLNAL","NORMAL_ND");
			break;
		case "050":
			sd.put("STATE_BYTE_INTERLNAL","DEVOLUCION_ND");
			break;
		case "060":
			sd.put("STATE_BYTE_INTERLNAL","ANULACION_ND");
			break;
		case "070":
			sd.put("STATE_BYTE_INTERLNAL","ANULACIONDEV_ND");
			break;
		case "080":
			sd.put("STATE_BYTE_INTERLNAL","REVERSO");
			break;
		default:
			break;
		}
			
		out.putStructuredData(sd);
		return sd.get("TRAN_KEY_INTERLNAL");
		
	}
	
	private static TagCreationResult extractTokenFromPinBlock(Field curField, StringBuilder msgStrBuilder, Iso8583Post msg)
			throws XPostilion {
		
		TagCreationResult res = new TagCreationResult(curField.getTagPrefix(), null, null);

		String token = "";

		String encodParam0 = "BASE64";
		
		DesKwp kwpParam1 = null;

//		try {
			CryptoCfgManager crypcfgman = CryptoManager.getStaticConfiguration();
			kwpParam1 = crypcfgman.getKwp("ATH_KPE");
			
			
			//////////////
			
			String param1 = Base64.getEncoder().encodeToString(kwpParam1.getValueUnderKsk().getBytes());

//			String kvpParam2 = "MVBVTkUwMDAsRUJDOEJDNjM0MEM2RkUyRjYxMTU2M0Y0MjY4MDdEMjM0OUI5QjdCNDU4NDNCMDk2LDg4Q0NEOTk5MDNFMjE2QTY";
			DesKwp kvpParam2 = crypcfgman.getKwp("ATH_GIROS");
			
			Logger.logLine("PIN DATA: " + msg.getField(Iso8583.Bit._052_PIN_DATA), false);
			Logger.logLine("PIN DATA to HEX: " + Transform.fromBinToHex(msg.getField(Iso8583.Bit._052_PIN_DATA)), false);
			Logger.logLine("PIN DATA to HEX to B64: " + Base64.getEncoder()
					.encodeToString(Transform.fromBinToHex(msg.getField(Iso8583.Bit._052_PIN_DATA)).getBytes()), false);

			String param3 = Base64.getEncoder()
					.encodeToString(Transform.fromBinToHex(msg.getField(Iso8583.Bit._052_PIN_DATA)).getBytes());

			String param4 = Base64.getEncoder().encodeToString(msg.getTrack2Data().getPan().getBytes());

//			String hexSeedParam5 = "MEIxQTJDM0Q0RjVFNjc4OTk4NzZGNEU1RDNDMkIwQTEwQjFBMkMzRDRGNUU2Nzg5";
			DesKwp hexSeedParam5 = crypcfgman.getKwp("ATH_GIROS_SEED");
			
			Logger.logLine("1 PARAM 2: " + kvpParam2.getName(), false);
			Logger.logLine("2 PARAM 2: " + kvpParam2.getValueUnderParent(), false);
			String param2 = kvpParam2.getValueUnderKsk();

			Logger.logLine("KWP: " + Base64.getEncoder().encodeToString(kwpParam1.getValueUnderKsk().getBytes()), false);

			String endPoint = "https://10.89.0.169:8081/entry-point/getPIN?encoding=%s&workingKey1=%s&workingKey2=%s&pinBlock=%s&pan=%s&seeds=%s";
			String[] params = new String[6];
			params[0] = encodParam0;
			params[1] = param1;
//			params[2] = kvpParam2;
			Logger.logLine("3 PARAM 2: " + param2, false);
			params[2] = Base64.getEncoder().encodeToString(param2.getBytes());
			params[3] = param3;
			params[4] = param4;
//			params[5] = hexSeedParam5;
			String param5 = hexSeedParam5.getValueUnderKsk();
			Logger.logLine("PARAM 5: " + param5, false);
			params[5] = Base64.getEncoder().encodeToString(param5.getBytes());
			
			token = HttpCryptoServ.httpConnection(endPoint, params);
			token = new String(Base64.getMimeDecoder().decode(token));

			Logger.logLine("TOKEN: " + token, false);
			
			msgStrBuilder.append(curField.getTagPrefix().concat(UtilidadesMensajeria
					.asciiToEbcdic(Pack.resize(token, curField.getTagValueLength(), '0', false)).toUpperCase()));
			
			if (token.equals("E: Sanity - 080000")) {
				
				res.setTagError("CRYPTO ERROR : E: Sanity - 080000");
			}
			else if (token.equals("E: Sanity - N")) {
				
				res.setTagError("CRYPTO ERROR : E: Sanity - N");
			}
			
			
			
//		} catch (Exception e) {
//			StringWriter outError = new StringWriter();
//			e.printStackTrace(new PrintWriter(outError));
//			Logger.logLine("KWP ERROR: " + outError.toString(), true);
//			EventRecorder.recordEvent(new Exception(outError.toString()));
//		}
		
		return res;

	}

	private static boolean isMassiveTransfer (Iso8583Post msg) throws XFieldUnableToConstruct, XPostilion {
		
		if ( (msg.getStructuredData().get("B24_Field_41").substring(12, 13).equals("2")
				&& (msg.getProcessingCode().getFromAccount().equals("10")
						|| msg.getProcessingCode().getFromAccount().equals("20"))
				&& (msg.getProcessingCode().getToAccount().equals("10")
						|| msg.getProcessingCode().getToAccount().equals("20"))
				&&
				
				(msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("8354")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("8206")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("8110")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("9631")
						|| msg.getStructuredData().get("B24_Field_41").substring(4, 8).equals("9632"))
				
				) ){
			
			return true;
			
		}
		else
			return false;
		
	}
	
	/**
	 * Metodo encargado de enviar mensajes al monitor UDP para su posteo en SQL lite
	 * 
	 * @param mon
	 * @param isoMsg
	 * @param iscMsg
	 * @param interfaceName
	 * @param refNr
	 * @param msgPrefix
	 */
	public static void postMsgInMonitor(Client mon, IMessage isoMsg, IMessage iscMsg, String interfaceName, String refNr, String msgPrefix) {
		
		try {
			if (isoMsg != null) {
				mon.sendData(Client.getMsgKeyValue(refNr, msgPrefix != null? msgPrefix + Transform.fromBinToHex(Transform.getString(isoMsg.toMsg())):
						Transform.fromBinToHex(Transform.getString(isoMsg.toMsg())), "ISO", interfaceName));

			}
			if(iscMsg != null) {
				mon.sendData(Client.getMsgKeyValue(refNr, msgPrefix != null? msgPrefix + iscMsg.toString() :
					iscMsg.toString(), "ISC", interfaceName));
//				mon.get
			}
		} catch (Exception e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception("Error Postilion: " + outError.toString()));
			Logger.logLine("Error Postilion: " + outError.toString(), false);
		}
		
	}
	
	
	/**
	 * Metodo encargado de enviar mensajes al monitor UDP para su posteo en SQL lite
	 * 
	 * @param mon
	 * @param isoMsg
	 * @param iscMsg
	 * @param interfaceName
	 * @param refNr
	 * @param msgPrefix
	 */
	public static void postLogInMonitor(Client mon, IMessage isoMsg, String output, String interfaceName, String refNr, String msgPrefix) {
		
		try {
			if (isoMsg != null) {
				mon.sendData(Client.getMsgKeyValue(refNr, msgPrefix != null? msgPrefix + Transform.fromBinToHex(Transform.getString(isoMsg.toMsg())):
						Transform.fromBinToHex(Transform.getString(isoMsg.toMsg())), "ISO", interfaceName));

			}
			if(output != null) {
				mon.sendData(Client.getMsgKeyValue(refNr, msgPrefix != null? msgPrefix + output.toString() :
					output.toString(), "LOG", interfaceName));
//				mon.get
			}
		} catch (Exception e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception("Error Postilion: " + outError.toString()));
			Logger.logLine("Error Postilion: " + outError.toString(), false);
		}
		
	}
	
	public static <E> String getBitMap(Iso8583Post msg) throws XPostilion {
		
        try {
        	
            String trama = new String(msg.getBinaryData());
            Logger.logLine("ISO:" + trama, false);
            
            
            Enumeration<E> enu = msg.enumerateSetFields();
            while(enu.hasMoreElements()) {
                Logger.logLine("ISO:" + enu.nextElement().toString(), false);
            }
            
            StringBuilder bitMap = new StringBuilder().append(trama.substring(16, 32));

            BigInteger initial = new BigInteger(trama.substring(16, 17), 16);
            StringBuilder bitMapBinario = new StringBuilder();
            
            switch (initial.compareTo(BigInteger.valueOf(4))) {
            case -1:
                bitMapBinario.append("00");
                break;
            case 0:
            case 1:
            	
                switch (initial.compareTo(BigInteger.valueOf(8))) {
                case -1:
                    bitMapBinario.append("0");
                    break;
                case 0:
                case 1:
                    bitMap.append(trama.substring(32, 48));
                    break;
                default:
                    break;
                }
                break;
                
            default:
                break;
            }
            
            bitMapBinario.append(new BigInteger(bitMap.toString(), 16).toString(2)); 
            return bitMapBinario.toString();
        } 
        catch (Exception e) {
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			EventRecorder.recordEvent(new Exception(outError.toString()));
        }
        return null;
    }
	
	public static HashMap<Integer, String> convertFields2Hash(Iso8583Post msg, boolean enableLog){
		
		HashMap<Integer, String> msgFields = new HashMap<>();
		
		try {
			for(int i = 0; i <= 128; i++) {	
				if(msg.isFieldSet(i)) {	
					
					if(i == 127) {
						if(msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA)) {
							
							msgFields.put(i, msg.getPrivField(Iso8583Post.PrivBit._022_STRUCT_DATA));
							
							HashMap<String, String> tags = getStructuredDataKeyValues(msg.getStructuredData());
							
							int j = 128;
							for(Map.Entry<String, String> e: tags.entrySet()) {
								msgFields.put(j, e.getKey().concat("|").concat(e.getValue()));
								j++;
							}
							
						}
					}
					else {
						msgFields.put(i, msg.getField(i));
					}
				}
				else {
					msgFields.put(i, "");
				}	
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		
		
		return msgFields;
	}
	
	public static HashMap<String, String> getStructuredDataKeyValues(StructuredData sd) {
		
		HashMap<String, String> sdFields = new HashMap<>();
		
		Enumeration e = sd.getTypeNames();
		
	  	while ( e.hasMoreElements() ) {
	    String tagName = e.nextElement().toString();
	    	sdFields.put(tagName, sd.get(tagName));
	    }
		
	  	return sdFields;
	}
	
	public static String getStringMessageException(Exception e) {
		StringWriter outError = new StringWriter();
		e.printStackTrace(new PrintWriter(outError));
		return outError.toString();
	}
	
	private static final Map<String, String> TRANS_HOMOLOGATION = new LinkedHashMap<>();

	static {

		TRANS_HOMOLOGATION.put("ATWI", "01");
		TRANS_HOMOLOGATION.put("ATWV", "01");

	}

	public static final int WITHDRAWAL_BODY_TYPE = 0;
	public static final int COST_INQUIRY_BODY_TYPE = 1;
	public static final int REVERSE_BODY_TYPE = 2;

	public static final String TT_WITHDRAWAL = "01_512";
	public static final String TT_REVERSE = "01_1056";
	public static final String TT_REP_REVERSE = "01_1057";
	public static final String TT_PAYMENT_CB_MIXT = "40_512_0";
	public static final String TT_PAYMENT_CB_DEBIT = "40_512_1";
	public static final String TT_PAYMENT_CB_CREDIT = "40_512_2";
	public static final String TT_REV_PAYMENT_CB_MIXT = "40_1056_0";
	public static final String TT_REV_PAYMENT_CB_DEBIT = "40_1056_1";
	public static final String TT_REV_PAYMENT_CB_CREDIT = "40_1056_2";
	public static final String TT_REV_REP_PAYMENT_CB_MIXT = "40_1057_0";
	public static final String TT_REV_REP_PAYMENT_CB_DEBIT = "40_1057_1";
	public static final String TT_REV_REP_PAYMENT_CB_CREDIT = "40_1057_2";
	public static final String TT_WITHDRAWAL_CB_ATTD = "50_512_0";
	public static final String TT_WITHDRAWAL_CB_ATTC = "50_512_1";
	public static final String TT_WITHDRAWAL_CB_ATTF = "50_512_2";
	public static final String TT_PAYMENT_OBLIG_CB_MIXT = "50_512_0_97";
	public static final String TT_PAYMENT_OBLIG_CB_DEBIT = "50_512_1_97";
	public static final String TT_PAYMENT_OBLIG_CB_CREDIT = "50_512_2_97";
	public static final String TT_REVERSE_CB_ATTD = "50_1056_0";
	public static final String TT_REVERSE_CB_ATTC = "50_1056_1";
	public static final String TT_REVERSE_CB_ATTF = "50_1056_2";
	public static final String TT_REP_REVERSE_CB_ATTD = "50_1057_0";
	public static final String TT_REP_REVERSE_CB_ATTC = "50_1057_1";
	public static final String TT_REP_REVERSE_CB_ATTF = "50_1057_2";
	public static final String TT_REVERSE_GNS = "00_1056";
	public static final String TT_REP_REVERSE_GNS = "00_1057";
	public static final String TT_COST_INQUIRY = "32_512";
	public static final String TT_BALANCE_INQUIRY_CB = "31_512";
	public static final String TT_GOOD_N_SERVICES = "00_512";
	public static final String TT_TRANSFER_CB_ATTD = "40_512_0_99";
	public static final String TT_TRANSFER_CB_ATTC = "40_512_1_99";
	public static final String TT_TRANSFER_CB_ATTF = "40_512_2_99";
	public static final String TT_REV_TRANSFER_CB_ATTD = "40_1056_0_99";
	public static final String TT_REV_TRANSFER_CB_ATTC = "40_1056_1_99";
	public static final String TT_REV_TRANSFER_CB_ATTF = "40_1056_2_99";
	public static final String TT_REV_REP_TRANSFER_CB_ATTD = "40_1057_0_99";
	public static final String TT_REV_REP_TRANSFER_CB_ATTC = "40_1057_1_99";
	public static final String TT_REV_REP_TRANSFER_CB_ATTF = "40_1057_2_99";
	public static final String TT_DEPOSIT_CB_ATTD = "40_512_0_98";
	public static final String TT_DEPOSIT_CB_ATTC = "40_512_1_98";
	public static final String TT_DEPOSIT_CB_ATTF = "40_512_2_98";
	public static final String TT_REV_DEPOSIT_CB_ATTD = "40_1056_0_98";
	public static final String TT_REV_DEPOSIT_CB_ATTC = "40_1056_1_98";
	public static final String TT_REV_DEPOSIT_CB_ATTF = "40_1056_2_98";
	public static final String TT_REV_REP_DEPOSIT_CB_ATTD = "40_1057_0_98";
	public static final String TT_REV_REP_DEPOSIT_CB_ATTC = "40_1057_1_98";
	public static final String TT_REV_REP_DEPOSIT_CB_ATTF = "40_1057_2_98";
	public static final String TT_CARD_PAYMENT = "50_512_96";
	public static final String TT_MORTGAGE_PAYMENT = "50_512_95";

	public static final String SEQ_TERMINAL = "SEQ_TERMINAL";

	public static final String YYMMDDhhmmss = "0";
	public static final String MMDDYYhhmmss = "1";
	public static final String DDMMYYhhmmss = "2";
	public static final String YYYYMMDDhhmmss = "3";

	private static final Map<String, String> OUTPUT_FIELDS = new LinkedHashMap<>();

	static {

		OUTPUT_FIELDS.put("COMISION:(.*)", "comision=$1");

		OUTPUT_FIELDS.put("SALDO DISPONIBLE:(.*)", "saldo_disponible=$1");

		OUTPUT_FIELDS.put("nombre(.*)", "nombre=$1");

		OUTPUT_FIELDS.put("saldo(.*)", "saldo=$1");

		OUTPUT_FIELDS.put("PIGNORACIONES(.*):(.*)", "pignoraciones=$2");

		OUTPUT_FIELDS.put("saldo(.*)", "saldo_total=$1");

		OUTPUT_FIELDS.put("IDENTIFI:(.*)", "identificacion=$1");

		OUTPUT_FIELDS.put("SECUENCIA(.*)", "secuencia=$1");

		OUTPUT_FIELDS.put("TIPODOC(.*)", "tipo_doc=$1");

		OUTPUT_FIELDS.put("FRML(.*)", "frml=$1");

		OUTPUT_FIELDS.put("AVSEGURO(.*)", "av_seguro=$1");

		OUTPUT_FIELDS.put("MONTOPA(.*)", "pa_monto=$1");

		OUTPUT_FIELDS.put("TASAPA(.*)", "pa_tasa=$1");

		OUTPUT_FIELDS.put("CUOTAPA(.*)", "pa_cuota=$1");

		OUTPUT_FIELDS.put("FORZAPA(.*)", "pa_forza=$1");

		OUTPUT_FIELDS.put("INDNAL:(.*)", "ind_nal=$1");

		OUTPUT_FIELDS.put("TIPTRANS:(.*)", "tipo_transporte=$1");
		
		OUTPUT_FIELDS.put("NOMBRE2:(.*)", "NOMBRE2=$1");

		OUTPUT_FIELDS.put("CCCON01(.*)", "saldos=$1");

		OUTPUT_FIELDS.put("MOVIMIEN1(.*)", "movimien_1=$1");

		OUTPUT_FIELDS.put("MOVIMIEN2(.*)", "movimien_2=$1");

		OUTPUT_FIELDS.put("MOVIMIEN3(.*)", "movimien_3=$1");

		OUTPUT_FIELDS.put("MOVIMIEN4(.*)", "movimien_4=$1");

		OUTPUT_FIELDS.put("MOVIMIEN5(.*)", "movimien_5=$1");
		
		OUTPUT_FIELDS.put("CUENTA:(.*)", "cuenta_homologada=$1");
		
		OUTPUT_FIELDS.put("COMISIONIVA:(.*)", "comisioniva=$1");

	}

	public static StringWriter printStackAndReturnErrorString(Exception e) {
		// TODO Auto-generated method stub
		
		StringWriter outError = new StringWriter();
		e.printStackTrace(new PrintWriter(outError));
		
		return outError;
	}
	
	
	public static ISCResInMsg createRspISCMsg(Iso8583Post msg, ISCReqInMsg originalReq) throws XPostilion {
		ISCResInMsg rsp = new ISCResInMsg();
		
		if(msg.getField(Iso8583.Bit._039_RSP_CODE).equals("00")) {
			rsp.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin("1140401D60"));
			rsp.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin("E2D9D3D5"));
			rsp.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin("F020"));
			rsp.putField(ISCResInMsg.Fields._04_H_TERMINAL, originalReq.getField(ISCReqInMsg.Fields._05_H_TERMINAL));
			rsp.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin("40404040"));
			rsp.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, originalReq.getField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR));
			rsp.putField(ISCResInMsg.Fields._VARIABLE_BODY, buildRspBodySucess(msg,originalReq));
		} else { 
			rsp.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin("1140401D60"));
			rsp.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin("E2D9D3D5"));
			rsp.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin("F120"));
			rsp.putField(ISCResInMsg.Fields._04_H_TERMINAL, originalReq.getField(ISCReqInMsg.Fields._05_H_TERMINAL));
			rsp.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin("40404040"));
			rsp.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, originalReq.getField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR));
			rsp.putField(ISCResInMsg.Fields._VARIABLE_BODY, buildRspBodyError(msg,originalReq));
		}
		return rsp;
	}

	public static String buildRspBodySucess(Iso8583Post msg, ISCReqInMsg originalReq) {
		StringBuilder sd = new StringBuilder("");
		try {
			String field126 = msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) ? msg.getStructuredData().get("B24_Field_126") != null ?
					msg.getStructuredData().get("B24_Field_126") : null : null;
			String field125 = msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) ? msg.getStructuredData().get("B24_Field_125") != null ?
					msg.getStructuredData().get("B24_Field_125") : null : null;
			String field102 = msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) ? msg.getStructuredData().get("B24_Field_102") != null ?
					msg.getStructuredData().get("B24_Field_102") : msg.getField(Iso8583.Bit._102_ACCOUNT_ID_1) : "000000000000000000";
			String field54 = msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) ? msg.getStructuredData().get("B24_Field_54") != null ?
					msg.getStructuredData().get("B24_Field_54") : "000000000000" : "000000000000";
			String field62 = msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) ? msg.getStructuredData().get("B24_Field_62") != null ?
					msg.getStructuredData().get("B24_Field_62") : "000000000000" : "000000000000";
			String field4 = msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) ? msg.getStructuredData().get("B24_Field_4") != null ?
					msg.getStructuredData().get("B24_Field_4") : "000000000000" : "000000000000";
			
			switch (msg.getProcessingCode().toString()) {
			case "300040":			
				
//				.append(Transform.fromHexToBin("1140401D60E2D9D3D5F020")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//				.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
				sd.append(Transform.fromAsciiToEbcdic(field54.substring(field54.length()-12)))
				.append(Transform.fromHexToBin("4E4040"))
				.append(Transform.fromAsciiToEbcdic(msg.isFieldSet(Iso8583.Bit._038_AUTH_ID_RSP) ? msg.getField(Iso8583.Bit._038_AUTH_ID_RSP) : "000000"))
				.append(Transform.fromHexToBin("404011C2601D60"))
				.append(Transform.fromAsciiToEbcdic("00"+field4))
				.append(Transform.fromAsciiToEbcdic("00"+field54.substring(12,24)))
				.append(Transform.fromAsciiToEbcdic("00"+field54.substring(24,36)))
				.append(Transform.fromHexToBin("840000000000"))
				.append(Transform.fromAsciiToEbcdic(field62.substring(30)));

				break;
				
			case "401000":			
			case "402000":			
				
//				.append(Transform.fromHexToBin("1140401D60E2D9D3D5F020")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//				.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
				sd.append(Transform.fromAsciiToEbcdic(field54.substring(field54.length()-12)))
				.append(Transform.fromHexToBin("4E4040"))
				.append(Transform.fromAsciiToEbcdic(msg.isFieldSet(Iso8583.Bit._038_AUTH_ID_RSP) ? msg.getField(Iso8583.Bit._038_AUTH_ID_RSP) : "000000"))
				.append(Transform.fromHexToBin("404011C2601D60"))
				.append(Transform.fromAsciiToEbcdic("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));

				break;	
				
			case "320000":			
				
//				.append(Transform.fromHexToBin("1140401D60E2D9D3D5F020")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//				.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
				sd.append(Transform.fromAsciiToEbcdic(field54.substring(field54.length()-12)))
				.append(Transform.fromHexToBin("4E4040"))
				.append(Transform.fromAsciiToEbcdic(msg.isFieldSet(Iso8583.Bit._038_AUTH_ID_RSP) ? msg.getField(Iso8583.Bit._038_AUTH_ID_RSP) : "000000"))
				.append(Transform.fromHexToBin("404011C2601D60"))
				.append(Transform.fromAsciiToEbcdic(Pack.resize(field125.substring(field125.length()-11), 16, '0', false)))
				.append(Transform.fromAsciiToEbcdic(field125.substring(0,31)))
				.append(Transform.fromAsciiToEbcdic("00000000000"));

				break;	

			default:
				
				String B5 = null;
				String arqc = null;
				if(field126!=null) {
					String parts[] = field126.split("!");
					
					int posB5 = 0;
					for (int i = 0; i < parts.length; i++) {
						if (parts[i].contains(" B5")) {
							posB5 = i;
							B5 = parts[posB5];
							arqc = B5.substring(13, 29);
						}
					}
				}
					 
//				.append(Transform.fromHexToBin("1140401D60E2D9D3D5F020")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//					.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
					sd.append(Transform.fromAsciiToEbcdic(field54.substring(field54.length()-12)))
					.append(Transform.fromHexToBin("4E4040"))
					.append(Transform.fromAsciiToEbcdic(msg.isFieldSet(Iso8583.Bit._038_AUTH_ID_RSP) ? msg.getField(Iso8583.Bit._038_AUTH_ID_RSP) : "000000"))
					.append(Transform.fromHexToBin("404011C2601D60"))
					.append(Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR)))
					.append(Transform.fromAsciiToEbcdic("000000000000"))
					.append(Transform.fromAsciiToEbcdic(field102))
					.append(arqc != null ? Transform.fromAsciiToEbcdic(arqc) : Transform.fromHexToBin("0000000000000000000000000000000000"));
				break;
			}
			
			
			
		} catch (XPostilion e) {
			e.toString();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		

		return sd.toString();
	}
	
	
	
	public static String buildRspBodyError(Iso8583Post msg, ISCReqInMsg originalReq) {
		StringBuilder sd = new StringBuilder("");
		try {
			String field54 = msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) && msg.getStructuredData().get("B24_Field_54") != null ?
					msg.getStructuredData().get("B24_Field_54") : msg.isFieldSet(Iso8583.Bit._054_ADDITIONAL_AMOUNTS) ? 
							msg.getField(Iso8583.Bit._054_ADDITIONAL_AMOUNTS)	: "000000000000";
			String field63 = msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) ? msg.getStructuredData().get("B24_Field_63") != null ?
					msg.getStructuredData().get("B24_Field_63") : "8601ERROR EN EL MENSAJE                     " : "8601ERROR EN EL MENSAJE                     ";
			
//			.append(Transform.fromHexToBin("1140401D60E2D9D3D5F120")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//				.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
				sd.append(Transform.fromAsciiToEbcdic(field54.substring(field54.length()-12)))
				.append(Transform.fromHexToBin("4E4040"))
				.append(Transform.fromAsciiToEbcdic("      "))
				.append(Transform.fromHexToBin("404011C2601D60"))
				.append(Transform.fromAsciiToEbcdic(field63.substring(0,4)))
				.append(Transform.fromHexToBin("60"))
				.append(Transform.fromAsciiToEbcdic(field63.substring(4)));
		} catch (XPostilion e) {
			e.toString();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		

		return sd.toString();
	}
	
	
	public static ISCResInMsg createRspISCMsgRev(Iso8583Post msg, ISCReqInMsg originalReq) throws XPostilion {
		ISCResInMsg rsp = new ISCResInMsg();
		
		if(!msg.getField(Iso8583.Bit._039_RSP_CODE).equals("00") 
				|| (msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) && msg.getStructuredData().get("REV_DECLINED") != null
						&& msg.getStructuredData().get("REV_DECLINED").equals("TRUE"))) {
			rsp.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin("1140401D60"));
			rsp.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin("E2D9D3D5"));
			rsp.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin("F020"));
			rsp.putField(ISCResInMsg.Fields._04_H_TERMINAL, originalReq.getField(ISCReqInMsg.Fields._05_H_TERMINAL));
			rsp.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin("40404040"));
			rsp.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, originalReq.getField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR));
			rsp.putField(ISCResInMsg.Fields._VARIABLE_BODY, buildRspBodyErrorRev(msg,originalReq));
		} else {
			rsp.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin("1140401D60"));
			rsp.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin("E2D9D3D5"));
			rsp.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin("F120"));
			rsp.putField(ISCResInMsg.Fields._04_H_TERMINAL, originalReq.getField(ISCReqInMsg.Fields._05_H_TERMINAL));
			rsp.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin("40404040"));
			rsp.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, originalReq.getField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR));
			rsp.putField(ISCResInMsg.Fields._VARIABLE_BODY, buildRspBodySucessRev(msg,originalReq));
		}
		return rsp;
	}
	
	
	public static String buildRspBodySucessRev(Iso8583Post msg, ISCReqInMsg originalReq) {
		StringBuilder sd = new StringBuilder("");
		try {
			String field126 = msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) ? msg.getStructuredData().get("B24_Field_126") != null ?
					msg.getStructuredData().get("B24_Field_126") : null : null;
			String field102 = msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA) ? msg.getStructuredData().get("B24_Field_102") != null ?
					msg.getStructuredData().get("B24_Field_102") : msg.getField(Iso8583.Bit._102_ACCOUNT_ID_1) : "000000000000000000";
			String field54 = "000000000000";
			String B5 = null;
			String arqc = null;
			if(field126!=null) {
				String parts[] = field126.split("!");
				
				int posB5 = 0;
				for (int i = 0; i < parts.length; i++) {
					if (parts[i].contains(" B5")) {
						posB5 = i;
						B5 = parts[posB5];
						arqc = B5.substring(13, 29);
					}
				}
			}
				
//			.append(Transform.fromHexToBin("1140401D60E2D9D3D5F020")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//				.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
				sd.append(Transform.fromAsciiToEbcdic(field54.substring(field54.length()-12)))
				.append(Transform.fromHexToBin("4E4040"))
				.append(Transform.fromHexToBin("000000000000"))
				.append(Transform.fromHexToBin("404011C2601D60"))
				.append(Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR)))
				.append(Transform.fromAsciiToEbcdic("000000000000"))
				.append(Transform.fromAsciiToEbcdic(field102))
				.append(arqc != null ? Transform.fromAsciiToEbcdic(arqc) : Transform.fromHexToBin("0000000000000000000000000000000000"));
			
		} catch (XPostilion e) {
			e.toString();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		

		return sd.toString();
	}
	
	
	
	public static String buildRspBodyErrorRev(Iso8583Post msg, ISCReqInMsg originalReq) {
		StringBuilder sd = new StringBuilder("");
		String field54 = "000000000000";
		String field63 = "2043NO MATCH FOR REVERSAL                   ";
		
//		.append(Transform.fromHexToBin("1140401D60E2D9D3D5F120")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//			.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
			sd.append(Transform.fromAsciiToEbcdic(field54.substring(field54.length()-12)))
			.append(Transform.fromHexToBin("4E4040"))
			.append(Transform.fromAsciiToEbcdic("      "))
			.append(Transform.fromHexToBin("404011C2601D60"))
			.append(Transform.fromAsciiToEbcdic(field63.substring(0,4)))
			.append(Transform.fromHexToBin("60"))
			.append(Transform.fromAsciiToEbcdic(field63.substring(4)));
		

		return sd.toString();
	}
	
	
	public static ISCResInMsg processMsgSyncPinPad(ISCReqInMsg msg, boolean log) throws XPostilion {
		ISCResInMsg rsp = new ISCResInMsg();
		Crypto crypto = new Crypto(log);
		PinPad pinpad = new PinPad();
		String codigoOficina = Transform.fromEbcdicToAscii(Transform.fromHexToBin(msg.getTotalHexString().substring(ISCReqInMsg.POS_ini_COD_OFICINA, ISCReqInMsg.POS_end_COD_OFICINA)));
		String serial = Transform.fromEbcdicToAscii(Transform.fromHexToBin(msg.getTotalHexString().substring(ISCReqInMsg.POS_ini_SERIAL, ISCReqInMsg.POS_end_SERIAL)));
		String terminal = Transform.fromEbcdicToAscii(Transform.fromHexToBin(msg.getTotalHexString().substring(ISCReqInMsg.POS_ini_TERMINALPINPAD, ISCReqInMsg.POS_end_TERMINALPINPAD)));
		
		
		try {
			switch (Transform.fromEbcdicToAscii(Transform.fromHexToBin(msg.getTotalHexString().substring(ISCReqInMsg.POS_ini_MODALIDAD, ISCReqInMsg.POS_end_MODALIDAD)))) {
			case "1":
				// PROCESA INICIALIZACION DE PINPAD
				pinpad = crypto.initPinPad(log);
				Logger.logLine("pinpad.isError():" + pinpad.isError(), log);
				Logger.logLine("pinpad.getResponseInit():" + pinpad.getResponseInit(), log);
				Logger.logLine("pinpad.getKey_ini():" + pinpad.getKey_ini(), log);
				Logger.logLine("pinpad.getKey_ini_snd():" + pinpad.getKey_ini_snd(), log);
				if(pinpad.isError()) {
					// RESPUESTA FALLIDA
					rsp.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin("1140401D60"));
					rsp.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin("E2D9D3D5"));
					rsp.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin("F120"));
					rsp.putField(ISCResInMsg.Fields._04_H_TERMINAL, msg.getField(ISCReqInMsg.Fields._05_H_TERMINAL));
					rsp.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin("40404040"));
					rsp.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, msg.getField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR));
					rsp.putField(ISCResInMsg.Fields._VARIABLE_BODY, buildRspBodyErrorInitPinPad(msg));
				}else {
					// PROCESO DE CRIPTOGRAFIA EXITOSO
					pinpad.setCodOficina(codigoOficina);
					pinpad.setSerial(serial);
					pinpad.setTerminal1(terminal);
					LocalDateTime date = LocalDateTime.now();
					Timestamp timestamp = Timestamp.valueOf(date);
					pinpad.setFechaInicializacion(timestamp);
					pinpad.setFecha_creacion(timestamp);
					pinpad.setFecha_modificacion(timestamp);
					pinpad.setUsuario_creacion("Postilion");
					pinpad.setUsuario_modificacion("Postilion");
					
					DBHandler.updateInsertPinPadDataInit(pinpad);
					ISCInterfaceCB.pinpadData.clear();
					ISCInterfaceCB.pinpadData = DBHandler.loadPinPadKeys();
					rsp.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin("1140401D60"));
					rsp.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin("E2D9D3D5"));
					rsp.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin("F020"));
					rsp.putField(ISCResInMsg.Fields._04_H_TERMINAL, msg.getField(ISCReqInMsg.Fields._05_H_TERMINAL));
					rsp.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin("40404040"));
					rsp.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, msg.getField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR));
					rsp.putField(ISCResInMsg.Fields._VARIABLE_BODY, buildRspBodySucessInitPinPad(msg,pinpad));
				}
					
				
					
				break;
			case "2":
				// PROCESA INTERCAMBIO DE LLAVES PINPAD
				pinpad = (PinPad) ISCInterfaceCB.pinpadData.get(codigoOficina+serial);
				if(pinpad == null) {
					ISCInterfaceCB.pinpadData.clear();
					ISCInterfaceCB.pinpadData = DBHandler.loadPinPadKeys();
					pinpad = (PinPad) ISCInterfaceCB.pinpadData.get(codigoOficina+serial);
				}
				if(pinpad == null || pinpad.getKey_ini() == null) {
					rsp.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin("1140401D60"));
					rsp.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin("E2D9D3D5"));
					rsp.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin("F120"));
					rsp.putField(ISCResInMsg.Fields._04_H_TERMINAL, msg.getField(ISCReqInMsg.Fields._05_H_TERMINAL));
					rsp.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin("40404040"));
					rsp.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, msg.getField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR));
					rsp.putField(ISCResInMsg.Fields._VARIABLE_BODY, buildRspBodyErrorExchangePinPad(msg, "PINPAD NO INICIALIZADO"));
				} else {
					pinpad = crypto.exchangePinPad(log, pinpad);
					if(pinpad.isError()) {
						// RESPUESTA FALLIDA
						rsp.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin("1140401D60"));
						rsp.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin("E2D9D3D5"));
						rsp.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin("F120"));
						rsp.putField(ISCResInMsg.Fields._04_H_TERMINAL, msg.getField(ISCReqInMsg.Fields._05_H_TERMINAL));
						rsp.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin("40404040"));
						rsp.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, msg.getField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR));
						rsp.putField(ISCResInMsg.Fields._VARIABLE_BODY, buildRspBodyErrorExchangePinPad(msg, "FALLO EN INTERCAMBIO"));
					} else {
						// PROCESO DE CRIPTOGRAFIA EXITOSO
						LocalDateTime date = LocalDateTime.now();
						Timestamp timestamp = Timestamp.valueOf(date);
						pinpad.setFechaIntercambio(timestamp);
						pinpad.setFecha_modificacion(timestamp);
						pinpad.setUsuario_modificacion("Postilion");
						DBHandler.updateInsertPinPadDataExchange(pinpad);
						ISCInterfaceCB.pinpadData.clear();
						ISCInterfaceCB.pinpadData = DBHandler.loadPinPadKeys();
						rsp.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin("1140401D60"));
						rsp.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin("E2D9D3D5"));
						rsp.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin("F020"));
						rsp.putField(ISCResInMsg.Fields._04_H_TERMINAL, msg.getField(ISCReqInMsg.Fields._05_H_TERMINAL));
						rsp.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin("40404040"));
						rsp.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, msg.getField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR));
						rsp.putField(ISCResInMsg.Fields._VARIABLE_BODY, buildRspBodySucessExchangePinPad(msg,pinpad));
					}
						
				}
					
				break;	
	
			default:
				break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			EventRecorder.recordEvent(
					new Exception("Aramando mensaje: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					Utils.getStringMessageException(e) }));
		}
			

		return rsp;
	}
	
	
	public static String buildRspBodySucessInitPinPad(ISCReqInMsg originalReq, PinPad pinpad) {
		StringBuilder sd = new StringBuilder("");
		
//		.append(Transform.fromHexToBin("1140401D60E2D9D3D5F020")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//			.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
			sd.append(Transform.fromAsciiToEbcdic("000000000000"))
			.append(Transform.fromHexToBin("4E4040"))
			.append(Transform.fromHexToBin("000000000000"))
			.append(Transform.fromHexToBin("404011C2601D60"))
			.append(Transform.fromAsciiToEbcdic(pinpad.getResponseInit()));
		

		return sd.toString();
	}
	
	public static String buildRspBodyErrorInitPinPad(ISCReqInMsg originalReq) {
		StringBuilder sd = new StringBuilder("");
		
//		.append(Transform.fromHexToBin("1140401D60E2D9D3D5F120")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//			.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
			sd.append(Transform.fromAsciiToEbcdic("000000000000"))
			.append(Transform.fromHexToBin("4E4040"))
			.append(Transform.fromHexToBin("000000000000"))
			.append(Transform.fromHexToBin("404011C2601D60"))
			.append(Transform.fromAsciiToEbcdic("ERROR AL CARGAR CONFIGURACION PINPAD"));
		

		return sd.toString();
	}
	
	public static String buildRspBodySucessExchangePinPad(ISCReqInMsg originalReq, PinPad pinpad) {
		StringBuilder sd = new StringBuilder("");
		
//		.append(Transform.fromHexToBin("1140401D60E2D9D3D5F020")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//			.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
			sd.append(Transform.fromAsciiToEbcdic("000000000000"))
			.append(Transform.fromHexToBin("4E4040"))
			.append(Transform.fromHexToBin("000000000000"))
			.append(Transform.fromHexToBin("404011C2601D60"))
			.append(Transform.fromAsciiToEbcdic(pinpad.getResponseExc()));
		

		return sd.toString();
	}
	
	public static String buildRspBodyErrorExchangePinPad(ISCReqInMsg originalReq, String error) {
		StringBuilder sd = new StringBuilder("");
		
//		.append(Transform.fromHexToBin("1140401D60E2D9D3D5F120")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//			.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
			sd.append(Transform.fromAsciiToEbcdic("000000000000"))
			.append(Transform.fromHexToBin("4E4040"))
			.append(Transform.fromHexToBin("000000000000"))
			.append(Transform.fromHexToBin("404011C2601D60"))
			.append(Transform.fromAsciiToEbcdic(error));
		

		return sd.toString();
	}
	
	public static ISCResInMsg processErrorMsg(ISCReqInMsg originalReq, Iso8583Post msg, String error, boolean log ) throws XPostilion {
		ISCResInMsg rsp = new ISCResInMsg();
		rsp.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin("1140401D60"));
		rsp.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin("E2D9D3D5"));
		rsp.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin("F120"));
		rsp.putField(ISCResInMsg.Fields._04_H_TERMINAL, originalReq.getField(ISCReqInMsg.Fields._05_H_TERMINAL));
		rsp.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin("40404040"));
		rsp.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, originalReq.getField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR));
		rsp.putField(ISCResInMsg.Fields._VARIABLE_BODY, processErrorMsg(originalReq, error));
		return rsp;
	}
	
	public static String processErrorMsg(ISCReqInMsg originalReq, String error) {
		StringBuilder sd = new StringBuilder("");
		
//		.append(Transform.fromHexToBin("1140401D60E2D9D3D5F120")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(22,30)))
//			.append(Transform.fromHexToBin("40404040")).append(Transform.fromHexToBin(originalReq.getTotalHexString().substring(38,46)))
			sd.append(Transform.fromAsciiToEbcdic("000000000000"))
			.append(Transform.fromHexToBin("4E4040"))
			.append(Transform.fromHexToBin("000000000000"))
			.append(Transform.fromHexToBin("404011C2601D60"))
			.append(Transform.fromAsciiToEbcdic(error));
		

		return sd.toString();
	}
	
	
}
