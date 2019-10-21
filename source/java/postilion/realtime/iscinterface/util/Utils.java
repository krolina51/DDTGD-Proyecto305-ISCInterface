package postilion.realtime.iscinterface.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import postilion.realtime.commonclass.FilterSettings;
import postilion.realtime.commonclass.model.ResponseCode;
import postilion.realtime.iscinterface.message.ISCReqMessage;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.message.bitmap.XFieldUnableToConstruct;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class Utils {

	private static final String EBCDIC_ENCODING = "IBM-1047";
	private static final String HEX_CHARS = "0123456789abcdef";
	private static final String KEY_VALUES_REGEX = "(\\w*)=(.*)";

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

	public static HashMap<String, String> getBodyInnerFields(String varBody, String regexVar, String outTemplate,
			String delimiter) {

		Logger.logLine("***Recuperando campos del msg variable");

		HashMap<String, String> innerFields = new HashMap<>();

		Logger.logLine("***body:" + varBody);
		String parteVariableEbcdic = varBody;
		String parteVariableAscii = Utils
				.ebcdicToAscii(parteVariableEbcdic.replaceAll(delimiter == null ? "" : delimiter, ""));
		String parteVariable = parteVariableAscii.replaceAll(regexVar, outTemplate);
		innerFields = Utils.stringToHashmap(parteVariable);

		Logger.logLine("***" + innerFields + "***");

		return innerFields;
	}

	public static List<String> getErrorsFromResponse(String errorRegex, String input) {
		List<String> output = new ArrayList<>();
		Pattern p = Pattern.compile(errorRegex);
		Matcher m = p.matcher(input);
		while (m.find()) {
			output.add(m.group(1));
			Logger.logLine("ERROR::" + m.group(1));
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
	 * podrá ser enviado a la entidad remota para efectos de prueba
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

		Logger.logLine("DUMMY ISC:" + Transform.fromBinToHex(output.getTotalString()));

		return output;
	}

	public static String prepareVariableReqBody(Iso8583Post msg, int bodyType) throws XPostilion {
		StringBuilder sd = new StringBuilder("");

		sd.append(Transform.fromHexToBin(ISCReqMessage.Constants._01_DATE_TAG))
				.append(Transform.fromAsciiToEbcdic(getStringDate().substring(0, 6)))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._02_DEBIT_ACC_TYPE_TAG))
				.append(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("P_CODE").equals("000000")
						? msg.getProcessingCode().getFromAccount().substring(0, 1).equals("1") ? "0" : "1"
						: msg.getStructuredData().get("P_CODE").substring(2, 3).equals("1") ? "0"
								: msg.getStructuredData().get("P_CODE").substring(2, 3).equals("2") ? "1"
										: msg.getStructuredData().get("P_CODE").substring(2, 3)))

				.append(bodyType == _COST_INQUIRY_BODY_TYPE
						? Transform.fromHexToBin(ISCReqMessage.Constants._03_TRAN_AMOUNT_TAG)
								.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", "0", 15)))
						: Transform.fromHexToBin(ISCReqMessage.Constants._03_TRAN_AMOUNT_TAG))
				.append(Transform
						.fromAsciiToEbcdic(Utils.padLeft(msg.getField(Iso8583.Bit._004_AMOUNT_TRANSACTION), "0", 15)))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._04_SYS_TIME_TAG))
				.append(Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._012_TIME_LOCAL)))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._05_DEBIT_ACC_NR_TAG)).append(
						Transform.fromAsciiToEbcdic(
								Utils.padLeft(msg.getField(Iso8583.Bit._102_ACCOUNT_ID_1), "0", 10)))
				.append(bodyType == _REVERSE_BODY_TYPE
						? Transform.fromHexToBin(ISCReqMessage.Constants._REV_06_ORIGINAL_SEQ).concat(
								Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._038_AUTH_ID_RSP).substring(2)))
						: "")
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._06_CARD_NR_TAG))
				.append(Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._035_TRACK_2_DATA).substring(0, 16)))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._07_REC_NR_TAG))
				.append(Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._037_RETRIEVAL_REF_NR).substring(6, 12)))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._08_TRAN_NACIONALITY_TAG))
				.append(Transform
						.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._049_CURRENCY_CODE_TRAN).equals("170") ? "1" : "2"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._09_INPUT_NETWORK_TAG))
				.append(Transform.fromAsciiToEbcdic(
						msg.getField(Iso8583.Bit._032_ACQUIRING_INST_ID_CODE).equals("10000000054") ? "02"
								: msg.getField(Iso8583.Bit._032_ACQUIRING_INST_ID_CODE).equals("10000000074") ? "03"
										: "04"))

				.append(Transform.fromHexToBin(ISCReqMessage.Constants._10_ACQ_NETWORK_TAG))
				.append(Transform.fromAsciiToEbcdic("04"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._11_TERM_ID_TAG))
				.append(Transform
						.fromAsciiToEbcdic(Utils.padLeft(
								msg.getField(Iso8583.Bit._041_CARD_ACCEPTOR_TERM_ID).substring(5, 8), "0", 8)))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._12_ORIGINAL_TRAN_TAG))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(bodyType != _REVERSE_BODY_TYPE
						? Transform.fromHexToBin(ISCReqMessage.Constants._13_AUTH_CODE_TAG)
								.concat(Transform.fromAsciiToEbcdic(
										Pack.resize(msg.getStructuredData().get("SEQ_TERMINAL").split(",")[1].trim(), 8,
												'0', false)))
						: Transform.fromHexToBin(ISCReqMessage.Constants._13_AUTH_CODE_TAG)
								.concat(Transform.fromAsciiToEbcdic(Pack.resize(
										msg.getField(Iso8583.Bit._038_AUTH_ID_RSP).substring(2), 8, '0', false))))
				.append(bodyType == _COST_INQUIRY_BODY_TYPE
						? Transform.fromHexToBin(ISCReqMessage.Constants._14_CREDIT_ENTITY_CODE_TAG)
								.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", "0", 4)))
						: Transform.fromHexToBin(ISCReqMessage.Constants._14_CREDIT_ENTITY_CODE_TAG))
				.append(Transform.fromAsciiToEbcdic("0000"))
				.append(bodyType == _COST_INQUIRY_BODY_TYPE
						? Transform.fromHexToBin(ISCReqMessage.Constants._15_CREDIT_ACC_TYPE_TAG)
								.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", "0", 1)))
						: Transform.fromHexToBin(ISCReqMessage.Constants._15_CREDIT_ACC_TYPE_TAG))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(bodyType == _COST_INQUIRY_BODY_TYPE
						? Transform.fromHexToBin(ISCReqMessage.Constants._16_AVAL_CREDIT_ACC_NR_TAG)
								.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", "0", 10)))
						: Transform.fromHexToBin(ISCReqMessage.Constants._16_AVAL_CREDIT_ACC_NR_TAG))
				.append(Transform.fromAsciiToEbcdic("00000000000000000000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._17_TERM_LOCATION_TAG))
				.append(Transform.fromAsciiToEbcdic(msg.getField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC)))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._18_DEBIT_CARD_TYPE_TAG))
				.append(Transform.fromAsciiToEbcdic("VS"))
				.append(bodyType == _COST_INQUIRY_BODY_TYPE
						? Transform.fromHexToBin(ISCReqMessage.Constants._19_IDEN_DOC_TYPE_TAG)
								.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", "0", 1)))
						: Transform.fromHexToBin(ISCReqMessage.Constants._19_IDEN_DOC_TYPE_TAG))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(bodyType == _COST_INQUIRY_BODY_TYPE
						? Transform.fromHexToBin(ISCReqMessage.Constants._20_IDEN_DOC_NR_TAG)
								.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", "0", 16)))
						: Transform.fromHexToBin(ISCReqMessage.Constants._20_IDEN_DOC_NR_TAG))
				.append(Transform.fromAsciiToEbcdic(msg.getStructuredData().get("CUSTOMER_ID").substring(9)))
				.append(bodyType == _COST_INQUIRY_BODY_TYPE
						? Transform.fromHexToBin(ISCReqMessage.Constants._21_ACQ_ENTITY_TAG)
								.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", "0", 4)))
						: Transform.fromHexToBin(ISCReqMessage.Constants._21_ACQ_ENTITY_TAG))
				.append(Transform.fromAsciiToEbcdic(
						Utils.padLeft(msg.getField(Iso8583.Bit._041_CARD_ACCEPTOR_TERM_ID).substring(5, 8), "0", 4)))
				.append(bodyType == _COST_INQUIRY_BODY_TYPE
						? Transform.fromHexToBin(ISCReqMessage.Constants._22_ACQ_OFFICE_TAG)
								.concat(Transform.fromAsciiToEbcdic(Utils.padLeft("", "0", 4)))
						: Transform.fromHexToBin(ISCReqMessage.Constants._22_ACQ_OFFICE_TAG))
				.append(Transform.fromAsciiToEbcdic("0000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._23_DEVICE_TAG))
				.append(Transform.fromAsciiToEbcdic("A"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._24_CORRES_CARD_NR_TAG))
				.append(Transform.fromAsciiToEbcdic("0000000000000000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._25_CORRES_CARD_TYPE_TAG))
				.append(Transform.fromAsciiToEbcdic("00"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._26_TRAN_INDICATOR_TAG))
				.append(bodyType == _COST_INQUIRY_BODY_TYPE ? Transform.fromAsciiToEbcdic("I")
						: Transform.fromAsciiToEbcdic("M"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._27_VIRT_PURCH_INDICATOR_TAG))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._28_STANDIN_INDICATOR_TAG))
				.append(Transform.fromAsciiToEbcdic("N"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._29_TRAN_IDENTIFICATOR_TAG))
				.append(Transform.fromAsciiToEbcdic("0000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._30_SECURE_AMOUNT_TAG))
				.append(Transform.fromAsciiToEbcdic(Pack.resize(msg.getStructuredData().get("SECURE_AMOUNT") != null
						? msg.getStructuredData().get("SECURE_AMOUNT")
						: "0", 15, '0', false)));

		return sd.toString();
	}

	public static String prepareTestVariableReqBody() {
		StringBuilder sd = new StringBuilder("");

		sd.append(Transform.fromHexToBin(ISCReqMessage.Constants._01_DATE_TAG))
				.append(Transform.fromAsciiToEbcdic("051319"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._02_DEBIT_ACC_TYPE_TAG))
				.append(Transform.fromAsciiToEbcdic("1"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._03_TRAN_AMOUNT_TAG))
				.append(Transform.fromAsciiToEbcdic("000000008800000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._04_SYS_TIME_TAG))
				.append(Transform.fromAsciiToEbcdic("150451"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._05_DEBIT_ACC_NR_TAG))
				.append(Transform.fromAsciiToEbcdic("0000118158"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._06_CARD_NR_TAG))
				.append(Transform.fromAsciiToEbcdic("4576020000066906"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._07_REC_NR_TAG))
				.append(Transform.fromAsciiToEbcdic("007396"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._08_TRAN_NACIONALITY_TAG))
				.append(Transform.fromAsciiToEbcdic("1"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._09_INPUT_NETWORK_TAG))
				.append(Transform.fromAsciiToEbcdic("02"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._10_ACQ_NETWORK_TAG))
				.append(Transform.fromAsciiToEbcdic("04"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._11_TERM_ID_TAG))
				.append(Transform.fromAsciiToEbcdic("00003915"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._12_ORIGINAL_TRAN_TAG))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._13_AUTH_CODE_TAG))
				.append(Transform.fromAsciiToEbcdic("00451473"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._14_CREDIT_ENTITY_CODE_TAG))
				.append(Transform.fromAsciiToEbcdic("0000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._15_CREDIT_ACC_TYPE_TAG))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._16_AVAL_CREDIT_ACC_NR_TAG))
				.append(Transform.fromAsciiToEbcdic("00000000000000000000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._17_TERM_LOCATION_TAG))
				.append(Transform.fromAsciiToEbcdic("ATH  B.AVV  LABORATORIO100100BOGOTACAGCO"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._18_DEBIT_CARD_TYPE_TAG))
				.append(Transform.fromAsciiToEbcdic("VS"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._19_IDEN_DOC_TYPE_TAG))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._20_IDEN_DOC_NR_TAG))
				.append(Transform.fromAsciiToEbcdic("0000000000000000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._21_ACQ_ENTITY_TAG))
				.append(Transform.fromAsciiToEbcdic("3915"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._22_ACQ_OFFICE_TAG))
				.append(Transform.fromAsciiToEbcdic("0000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._23_DEVICE_TAG))
				.append(Transform.fromAsciiToEbcdic("A"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._24_CORRES_CARD_NR_TAG))
				.append(Transform.fromAsciiToEbcdic("0000000000000000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._25_CORRES_CARD_TYPE_TAG))
				.append(Transform.fromAsciiToEbcdic("00"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._26_TRAN_INDICATOR_TAG))
				.append(Transform.fromAsciiToEbcdic("M"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._27_VIRT_PURCH_INDICATOR_TAG))
				.append(Transform.fromAsciiToEbcdic("0"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._28_STANDIN_INDICATOR_TAG))
				.append(Transform.fromAsciiToEbcdic("N"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._29_TRAN_IDENTIFICATOR_TAG))
				.append(Transform.fromAsciiToEbcdic("0000"))
				.append(Transform.fromHexToBin(ISCReqMessage.Constants._30_SECURE_AMOUNT_TAG))
				.append(Transform.fromAsciiToEbcdic("000000000000000"));

		return sd.toString();
	}

	/***************************************************************************************
	 * Metodo auxiliar que convierte la hora actual de sistema a String con formato
	 * YYMMDDhhmmss
	 * 
	 * @return
	 ***************************************************************************************/
	public static String getStringDate() {

		Calendar cal = Calendar.getInstance();
		StringBuilder date = new StringBuilder();

		date.append(String.valueOf(cal.get(Calendar.YEAR)).substring(2, 4));
		date.append(
				String.valueOf(cal.get(Calendar.MONTH) + 1).length() > 1 ? String.valueOf(cal.get(Calendar.MONTH) + 1)
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

		return date.toString();
	}

	public static int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	/**
	 * Fija los valores para los campos 38 y 39 de un msj y retorna el ResponseCode
	 * 
	 * @param msg
	 * @param allCodesIscToIso
	 * @throws XPostilion
	 */
	public static ResponseCode set38And39Fields(Iso8583Post msg, HashMap<String, ResponseCode> allCodesIscToIso)
			throws XPostilion {
		StructuredData sd = msg.getStructuredData();
		ResponseCode responseCode;
		if (msg.getStructuredData().get("ERROR") != null) {
			responseCode = FilterSettings.getFilterCodeISCToIso(msg.getStructuredData().get("ERROR"), allCodesIscToIso);
			msg.putField(Iso8583.Bit._038_AUTH_ID_RSP, "000000");
		} else {
			responseCode = FilterSettings.getFilterCodeISCToIso("0000", allCodesIscToIso);
			msg.putField(Iso8583.Bit._038_AUTH_ID_RSP, sd.get("SEQ_TERMINAL").split(",")[0].trim().substring(3)
					.concat(sd.get("SEQ_TERMINAL").split(",")[1].trim()));
		}

		Logger.logLine("RESPOSE CODE KEY>>>".concat(responseCode.getKeyIsc()).concat(" ")
				.concat("RESPOSE CODE DESCRIP>>>").concat(responseCode.getDescriptionIsc()));

		msg.putField(Iso8583.Bit._039_RSP_CODE, responseCode.getKeyIso());

		return responseCode;
	}

	/**
	 * Se determina el tipo de transacción "AAAA_BBBBBB_C" AAAA-Tipo de Msg ;
	 * BBBBBB-Codigo proceso ; C-canal
	 * 
	 * @param msg
	 * @param canal
	 * @return
	 * @throws XFieldUnableToConstruct
	 */
	public static String getTranType(Iso8583Post msg, String canal) throws XFieldUnableToConstruct {
		StringBuilder tranTypeBuilder = new StringBuilder();
		tranTypeBuilder.append(msg.getMessageType()).append("_").append(msg.getProcessingCode().toString()).append("_")
				.append(canal);
		return tranTypeBuilder.toString();
	}

	/**
	 * Se determina el canal el mismo viene en la posición 13 del Tag "B24_Field_41"
	 * 
	 * @param msg
	 * @return
	 * @throws XPostilion
	 */
	public static String getTranChannel(Iso8583Post msg) throws XPostilion {
		return (msg.isPrivFieldSet(Iso8583Post.PrivBit._022_STRUCT_DATA)
				&& msg.getStructuredData().get("B24_Field_41") != null)
						? msg.getStructuredData().get("B24_Field_41").substring(12, 13)
						: null;
	}

	public static void putB24Field126IntoStructuredData(StructuredData sd) {
		sd.put("B24_Field_126",
				sd.get("COMISION") != null
						? sd.get("B24_Field_126").substring(0, sd.get("B24_Field_126").length() - 14)
								.concat(Pack.resize(sd.get("COMISION").replace(".", ""), 12, '0', false))
								.concat(sd.get("B24_Field_126").substring(sd.get("B24_Field_126").length() - 2))
						: sd.get("B24_Field_126"));
	}
	
	public static void putB24Field63IntoStructuredData(StructuredData sd, ResponseCode rspCode) {
		sd.put("B24_Field_63",rspCode.getKeyIsc().concat(Pack.resize(rspCode.getDescriptionIsc(), 44, ' ', true)));
	}
	
	public static void putB24Field40IntoStructuredData(StructuredData sd) {
		sd.put("B24_Field_40", "000");
	}
	
	public static void putB24Field44IntoStructuredData(StructuredData sd) {
		if (sd.get("SALDO_DISPONIBLE") != null) {
			sd.put("B24_Field_44", "2000000000000".concat(
					Pack.resize(sd.get("SALDO_DISPONIBLE").replace(",", "").replace(".", ""), 12, '0', false)));
		} else {
			sd.put("B24_Field_44", "0000000000000000000000000");
		}
	}
	
	public static void putB24Field48IntoStructuredData(StructuredData sd) {
		sd.put("B24_Field_48",
				sd.get("AV_SEGURO") != null ? sd.get("B24_Field_48")
						.substring(0, sd.get("B24_Field_48").length() - 1).concat(sd.get("AV_SEGURO"))
						: sd.get("B24_Field_48"));;
	}

	public static final int _WITHDRAWAL_BODY_TYPE = 0;
	public static final int _COST_INQUIRY_BODY_TYPE = 1;
	public static final int _REVERSE_BODY_TYPE = 2;
}
