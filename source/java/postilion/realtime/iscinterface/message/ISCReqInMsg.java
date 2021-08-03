package postilion.realtime.iscinterface.message;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import postilion.realtime.iscinterface.message.ISCReqInMsg.Fields;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.UtilidadesMensajeria;
import postilion.realtime.sdk.message.Validator;
import postilion.realtime.sdk.message.stream.IStreamFormatter;
import postilion.realtime.sdk.message.stream.StreamFormatterContainer;
import postilion.realtime.sdk.message.stream.StreamFormatterFieldFixed;
import postilion.realtime.sdk.message.stream.StreamFormatterFieldVar;
import postilion.realtime.sdk.message.stream.StreamMessage;
import postilion.realtime.sdk.message.stream.XStreamBase;
import postilion.realtime.sdk.util.convert.Transform;
import postilion.realtime.iscinterface.validator.ValidatorCustom;

public class ISCReqInMsg extends StreamMessage {
	
	private static IStreamFormatter iscReqFormatter;
	
	//Constantes de uso interno de la clase
	private static final int MAX_SIZE = 1024;

	public ISCReqInMsg() {
		super(MAX_SIZE, iscReqFormatter);
	}

	public ISCReqInMsg(IStreamFormatter stream) {
		super(MAX_SIZE, stream);
	}

	//Bloque estatico que define la plantilla del mensaje "iscReqFormatter"
	static {

		StreamFormatterContainer RequestDataContainer = new StreamFormatterContainer();

		RequestDataContainer = defineHeadersFormatters(RequestDataContainer);
		RequestDataContainer = defineBodyFormatters(RequestDataContainer);

		StreamFormatterContainer message = new StreamFormatterContainer();
		message.add(RequestDataContainer);
		iscReqFormatter = message;
		
	}

	/**********************************************************************************
	 * 
	 * Metodo que define el orden y structura de campos para la cabecera de los objetos
	 * creados a partir de la clase ISCReqInMsg
	 * 
	 * @param containerDestination
	 * @return
	 **********************************************************************************/
	private static StreamFormatterContainer defineHeadersFormatters(StreamFormatterContainer containerDestination){
		
		containerDestination.add(new StreamFormatterFieldFixed(Fields._02_H_TRAN_CODE, ValidatorCustom.getAnsc(), 4));	//Header :: HEADER_FRAME
		containerDestination.add(new StreamFormatterFieldFixed(Fields._03_H_DELIMITER, ValidatorCustom.getAnsc(), 3));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._04_H_AUTRA_CODE, ValidatorCustom.getAnsc(), 4));//6	
		containerDestination.add(new StreamFormatterFieldFixed(Fields._05_H_TERMINAL, ValidatorCustom.getAnsc(), 4));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._06_H_OFFICE_CODE, ValidatorCustom.getAnsc(), 4));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._07_H_TRAN_SEQ_NR, ValidatorCustom.getAnsc(), 4));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._08_H_STATE, ValidatorCustom.getAnsc(), 3));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._09_H_TIME, ValidatorCustom.getAnsc(), 6));//6
		containerDestination.add(new StreamFormatterFieldFixed(Fields._10_H_NEXTDAY_IND, ValidatorCustom.getAnsc(), 1));//12
		containerDestination.add(new StreamFormatterFieldFixed(Fields._11_H_FILLER, ValidatorCustom.getAnsc(), 15));//16
		
		return containerDestination;
	}
	
	public void fromHexStr(String iscReqHexStr) {
		Logger.logLine("STRING HEX:"+iscReqHexStr, true);
		this.putField(ISCReqInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin(iscReqHexStr.substring(0, 8)));
		this.putField(ISCReqInMsg.Fields._03_H_DELIMITER, Transform.fromHexToBin(iscReqHexStr.substring(8, 14)));
		this.putField(ISCReqInMsg.Fields._04_H_AUTRA_CODE, Transform.fromHexToBin(iscReqHexStr.substring(14, 22)));
		this.putField(ISCReqInMsg.Fields._05_H_TERMINAL, Transform.fromHexToBin(iscReqHexStr.substring(22, 30)));
		this.putField(ISCReqInMsg.Fields._06_H_OFFICE_CODE, Transform.fromHexToBin(iscReqHexStr.substring(30, 38)));
		this.putField(ISCReqInMsg.Fields._07_H_TRAN_SEQ_NR, Transform.fromHexToBin(iscReqHexStr.substring(38, 46)));
		this.putField(ISCReqInMsg.Fields._08_H_STATE, Transform.fromHexToBin(iscReqHexStr.substring(46, 52)));
		this.putField(ISCReqInMsg.Fields._09_H_TIME, Transform.fromHexToBin(iscReqHexStr.substring(52, 64)));
		this.putField(ISCReqInMsg.Fields._10_H_NEXTDAY_IND, Transform.fromHexToBin(iscReqHexStr.substring(64, 66)));
		this.putField(ISCReqInMsg.Fields._11_H_FILLER, Transform.fromHexToBin(iscReqHexStr.substring(66, 96)));
		this.putField(ISCReqInMsg.Fields._VARIABLE_BODY, Transform.fromHexToBin(iscReqHexStr.substring(96)));
	}
	
	/**********************************************************************************
	 * 
	 * Metodo que define el orden y structura de campos para el cuerpo de los objetos
	 * creados a partir de la clase ISCReqInMsg
	 * 
	 * @param containerDestination
	 * @return
	 **********************************************************************************/
	private static StreamFormatterContainer defineBodyFormatters(StreamFormatterContainer containerDestination) {
		
		containerDestination.add(new StreamFormatterFieldVar(Fields._VARIABLE_BODY, ValidatorCustom.getAnsc(), 1024, true));
		
		return containerDestination;
	}
	
//	/**********************************************************************************
//	 * 
//	 * Metodo auxiliar el mismo es invocado desde el constructor de la clase de manera
//	 * los separadores se coloquen desde la creación misma de los objetos de tipo
//	 * ISCReqInMsg
//	 * @param msg
//	 * 
//	 **********************************************************************************/
//	public void setConstantHeaderFields() {
//		Logger.logLine("Seteando campos constantes en cabecera", false);
//		
//		this.putField(Fields._02_H_TRAN_CODE, Transform.fromHexToBin(Constants.HEADER_FRAME));		
//		this.putField(Fields._03_H_CICS_TRAN_CODE, Transform.fromHexToBin(Constants.CICS_TRAN_CODE));	
//		this.putField(Fields._04_H_DELIMITER, Transform.fromHexToBin(Constants.DELIMITER));
//		this.putField(Fields._07_H_FILLER, Transform.fromHexToBin(Constants.HEADER_FILLER));
//		this.putField(Fields._11_H_FILLER_2, Transform.fromHexToBin(Constants.HEADER_FILLER_2));	
//		this.putField(Fields._12_H_SUPER_ID, Transform.fromHexToBin(Constants.SUPER_ID));
//		
//	}
	
//	/**********************************************************************************
//	 * Constantes codigos/nombres de constantes
//	 * 
//	 * @author Javier Flores
//	 *
//	 **********************************************************************************/
//	public static class Constants {
//		private Constants() {
//		    throw new IllegalStateException();
//		  }
//		
//		//Header
//		private static final String HEADER_FRAME = "313233343536";
//		private static final String CICS_TRAN_CODE = "E5C9C2C1"; //VIBA-->ebcdic-->hex
//		private static final String HEADER_FILLER = "404040"; //Anteriormente 40404040
//		private static final String HEADER_FILLER_2 = "4040404040404040";
//		private static final String SUPER_ID = "F0F0F0F0F0F0F0F0";
//		private static final String DELIMITER = "114040";
//		//Body	
//		public static final String TAG_119105_PIGNOS_6 = "119105";
//		public static final String TAG_119130_DATE_6 = "119130";
//		public static final String TAG_1140C3_DEBIT_ACC_TYPE_CLIENT_1 = "1140C3";
//		public static final String TAG_11C5D2_DEBIT_ACC_TYPE_CORRES_1 = "11C5D2";
//		public static final String TAG_11C3F0_TRAN_AMOUNT_15 = "11C3F0";
//		public static final String TAG_119131_SYS_TIME_12 = "119131";
//		public static final String TAG_11C37B_COR_ACCOUNT_NR_10 = "11C37B";
//		public static final String TAG_114040_DEBIT_ACC_NR_10 = "114040";
//		public static final String TAG_11C1C5_ORIGINAL_SEQ_6 = "11C1C5";
//		public static final String TAG_11E5C6_CARD_NR_16 = "11E5C6";
//		public static final String TAG_11C17A_REC_NR_6 = "11C17A";
//		public static final String TAG_1191A1_TRAN_NACIONALITY_1 = "1191A1";
//		public static final String TAG_119160_INPUT_NETWORK_2 = "119160";
//		public static final String TAG_119161_ACQ_NETWORK_2 = "119161";
//		public static final String TAG_119162_TERM_ID_8 = "119162";
//		public static final String TAG_119181_ORIGINAL_TRAN_1 = "119181";
//		public static final String TAG_11912F_AUTH_CODE_8 = "11912F";
//		public static final String TAG_11D135_ = "11D135";
//		public static final String TAG_11D142_ = "11D142";
//		public static final String TAG_11D137_ = "11D137";
//		public static final String TAG_11D136_SERVICE_CODE_4 = "11D136";
//		public static final String TAG_119190_COMMERCE_CODE_10 = "119190";
//		public static final String TAG_11924A_CREDIT_ENTITY_CODE_4 = "11924A";
//		public static final String TAG_119115_CREDIT_ACC_TYPE_1 = "119115";
//		public static final String TAG_11912D_AVAL_CREDIT_ACC_NR_20 = "11912D";
//		public static final String TAG_119199_TERM_LOCATION_40 = "119199";
//		public static final String TAG_11E4F0_ = "11E4F0";
//		public static final String TAG_11E4F9_ = "11E4F9";
//		public static final String TAG_11E4F8_ = "11E4F8";
//		public static final String TAG_11A2C7_PAY_MODE_INDIC = "11A2C7";
//		public static final String TAG_1197A5_DEBIT_CARD_TYPE_2 = "1197A5";
//		public static final String TAG_11E4F2_IDEN_DOC_TYPE_1 = "11E4F2";
//		public static final String TAG_11E4F3_IDEN_DOC_NR_16 = "11E4F3";
//		public static final String TAG_11D140_ACQ_ENTITY_4 = "11D140";
//		public static final String TAG_11D138_ACQ_OFFICE_4 = "11D138";
//		public static final String TAG_11D139_DEVICE_1 = "11D139";
//		public static final String TAG_11E5C7_CORRES_CARD_NR_16 = "11E5C7";
//		public static final String TAG_1197A7_CORRES_CARD_TYPE_2 = "1197A7";
//		public static final String TAG_11A9B1_TRAN_INDICATOR_1 = "11A9B1";
//		public static final String TAG_11E5F1_CELULAR_NR_12 = "11E5F1";
//		public static final String TAG_11E5F3_VIRT_PURCH_INDICATOR_1 = "11E5F3";
//		public static final String TAG_11D141_STANDIN_INDICATOR_1 = "11D141";
//		public static final String TAG_11E0E2_TRAN_IDENTIFICATOR_4 = "11E0E2";
//		public static final String TAG_11913D_SECURE_AMOUNT_15 = "11913D";
//		public static final String TAG_11914D_PA_MODE = "11914D";
//		
//		//Miscelaneos
//		private static final String BIN_TRAN_CODE = "450942";
//		private static final String NATIONAL_CUR_CODE = "170";
//		private static final String ATH_ACQ_CODE = "10000000054";	
//	}
	
	/**********************************************************************************
	 * Constantes codigos/nombres de campos
	 * 
	 * @author Javier Flores
	 *
	 **********************************************************************************/
	public static class Fields {
		
		public static final String _01_H_TOTAL_LENGTH = "total-length";
		public static final String _02_H_TRAN_CODE = "tran-code";
		public static final String _03_H_DELIMITER = "delimiter";
		public static final String _04_H_AUTRA_CODE = "autra-tran-code";
		public static final String _05_H_TERMINAL = "terminal";
		public static final String _06_H_OFFICE_CODE = "office-code";
		public static final String _07_H_TRAN_SEQ_NR = "tran-seq";
		public static final String _08_H_STATE = "state";
		public static final String _09_H_TIME = "time";
		public static final String _10_H_NEXTDAY_IND = "nextday-ind";
		public static final String _11_H_FILLER = "filler";
		public static final String _VARIABLE_BODY = "var-body";
		
	}

	/***********************************************************************************
	 * Metodo toString para visualizar el contenido del objeto de una forma parecida
	 * a como debe ir el mensaje hacia el autorizador
	 **********************************************************************************/
	public String getTotalHexString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(Transform.fromBinToHex(this.getField(Fields._02_H_TRAN_CODE)))
		.append(Transform.fromBinToHex(this.getField(Fields._03_H_DELIMITER)))
		.append(Transform.fromBinToHex(this.getField(Fields._04_H_AUTRA_CODE)))
		.append(Transform.fromBinToHex(this.getField(Fields._05_H_TERMINAL)))
		.append(Transform.fromBinToHex(this.getField(Fields._06_H_OFFICE_CODE)))
		.append(Transform.fromBinToHex(this.getField(Fields._07_H_TRAN_SEQ_NR)))
		.append(Transform.fromBinToHex(this.getField(Fields._08_H_STATE)))
		.append(Transform.fromBinToHex(this.getField(Fields._09_H_TIME)))
		.append(Transform.fromBinToHex(this.getField(Fields._10_H_NEXTDAY_IND)))
		.append(Transform.fromBinToHex(this.getField(Fields._11_H_FILLER)))
		.append(Transform.fromBinToHex(this.getField(Fields._VARIABLE_BODY)));
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
	    sb.append("ISC REQUEST:\n");
	    sb.append("tran-code").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("tran-code"))).append("\n");
	    sb.append("delimiter").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("delimiter"))).append("\n");
	    sb.append("autra-tran-code").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("autra-tran-code"))).append("\n");
	    sb.append("terminal").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("terminal"))).append("\n");
	    sb.append("office-code").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("office-code"))).append("\n");
	    sb.append("tran-seq").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("tran-seq"))).append("\n");
	    sb.append("state").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("state"))).append("\n");
	    sb.append("time").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("time"))).append("\n");
	    sb.append("nextday-ind").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("nextday-ind"))).append("\n");
	    sb.append("filler").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("filler"))).append("\n");
	    sb.append("var-body").append("\t---> ").append(Transform.fromBinToHex(getField("var-body"))).append("\n");

				
		//sb.append("\t").append(ISCReqInMsg.Constants.da)
		return sb.toString();
	}
}