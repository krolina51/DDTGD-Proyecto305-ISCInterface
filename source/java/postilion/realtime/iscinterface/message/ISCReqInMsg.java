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
	private static final int MAX_SIZE = 2048;

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
		
		containerDestination.add(new StreamFormatterFieldVar(Fields._VARIABLE_BODY, ValidatorCustom.getAnsc(), 2048, true));
		
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
	
	/**********************************************************************************
	 * Constantes codigos/nombres de constantes
	 * 
	 * @author Javier Flores
	 *
	 **********************************************************************************/

		
		public static final int POS_ini_TRAN_CODE = 0;
		public static final int POS_end_TRAN_CODE = 8;
		
		public static final int POS_ini_DELIMITER = 8; 
		public static final int POS_end_DELIMITER = 14;
		
		public static final int POS_ini_TRAN_CODE_AUTRA = 14; 
		public static final int POS_end_TRAN_CODE_AUTRA = 22; 
		
		public static final int POS_ini_FINESSE_STATION_CODE = 22;
		public static final int POS_end_FINESSE_STATION_CODE = 30;
		
		public static final int POS_ini_OFFICE_CODE = 30;
		public static final int POS_end_OFFICE_CODE = 32;
		
		public static final int POS_ini_CURRENCY = 32;
		public static final int POS_end_CURRENCY = 34;
		
		public static final int POS_ini_FILLER_0 = 34;
		public static final int POS_end_FILLER_0 = 38;
		
		public static final int POS_ini_SEQUENCE_NR = 38;
		public static final int POS_end_SEQUENCE_NR = 46;
		
		public static final int POS_ini_MSG_TYPE = 46;
		public static final int POS_end_MSG_TYPE = 52;
		
		public static final int POS_ini_TIME = 52;
		public static final int POS_end_TIME = 64;
		
		public static final int POS_ini_NEXTDAY_INDICATOR = 64;
		public static final int POS_end_NEXTDAY_INDICATOR = 66;
		
		public static final int POS_ini_FILLER_1 = 66;
		public static final int POS_end_FILLER_1 = 96;
		
		public static final int POS_ini_IDEN_NR = 96;
		public static final int POS_end_IDEN_NR = 128;
		
		public static final int POS_ini_PIN = 128;
		public static final int POS_end_PIN = 136;
		
		public static final int POS_ini_IDEN_TYPE = 136;
		public static final int POS_end_IDEN_TYPE = 138;
		
		public static final int POS_ini_AUTH_CREDIT_ENT_CODE = 138;
		public static final int POS_end_AUTH_CREDIT_ENT_CODE = 146;
		
		public static final int POS_ini_CREDIT_ACC_TYPE = 146;
		public static final int POS_end_CREDIT_ACC_TYPE = 148;
		
		public static final int POS_ini_CREDIT_ACC_NR = 148;
		public static final int POS_end_CREDIT_ACC_NR = 180;
		
		public static final int POS_ini_DEBIT_ACC_TYPE = 180;
		public static final int POS_end_DEBIT_ACC_TYPE = 182;
		
		public static final int POS_ini_DEBIT_ACC_NR = 182;
		public static final int POS_end_DEBIT_ACC_NR = 214;
		
		public static final int POS_ini_TRAN_AMOUNT = 214;
		public static final int POS_end_TRAN_AMOUNT = 242;
		
		public static final int POS_ini_SEQUENCE_TS = 242;
		public static final int POS_end_SEQUENCE_TS = 282;
		
		public static final int POS_ini_PAYMENT_MODE = 282;
		public static final int POS_end_PAYMENT_MODE = 284;
		
		public static final int POS_ini_TRAN_NATURE = 284;
		public static final int POS_end_TRAN_NATURE = 286;
		
		public static final int POS_ini_PAYMENT_CLASS = 286;
		public static final int POS_end_PAYMENT_CLASS = 288;
		
		public static final int POS_ini_PAYMENT_APPLICATION_DATE = 288;
		public static final int POS_end_PAYMENT_APPLICATION_DATE = 304;
		
		public static final int POS_ini_TRAN_ID = 304;
		public static final int POS_end_TRAN_ID = 314;
		
		public static final int POS_ini_AUTH_DEBIT_ENTITY_CODE = 314;
		public static final int POS_end_AUTH_DEBIT_ENTITY_CODE = 322;
		
		public static final int POS_ini_FILLER_2 = 322;
		public static final int POS_end_FILLER_2 = 324;
		
		public static final int POS_ini_ATM_CODE = 324;
		public static final int POS_end_ATM_CODE = 340;
		
		public static final int POS_ini_TC_CUOTA_NR = 340;
		public static final int POS_end_TC_CUOTA_NR = 344;
		
		public static final int POS_ini_PINPAD_IDEN = 344;
		public static final int POS_end_PINPAD_IDEN = 348;
		
		public static final int POS_ini_FILLER_3 = 348;
		public static final int POS_end_FILLER_3 = 356;
		
		public static final int POS_ini_ACQ_OFFICE_CODE = 356;
		public static final int POS_end_ACQ_OFFICE_CODE = 364;
		
		public static final int POS_ini_CHECK_NR = 364;
		public static final int POS_end_CHECK_NR = 404;
		
		public static final int POS_ini_CANJE_LIBERATION_DATE = 404;
		public static final int POS_end_CANJE_LIBERATION_DATE = 416;
		
		public static final int POS_ini_DEVOLUTION_REASON = 416;
		public static final int POS_end_DEVOLUTION_REASON = 420;
		
		public static final int POS_ini_BANK_CHECK_DEVOLUTION_CODE = 420;
		public static final int POS_end_BANK_CHECK_DEVOLUTION_CODE = 426;
		
		public static final int POS_ini_CUR_TRAN_SEQ_NR = 426;
		public static final int POS_end_CUR_TRAN_SEQ_NR = 434;
		
		public static final int POS_ini_ORI_TRAN_SEQ_NR = 434;
		public static final int POS_end_ORI_TRAN_SEQ_NR = 442;
		
		public static final int POS_ini_ACQ_CITY_OFFICE_DANE = 442;
		public static final int POS_end_ACQ_CITY_OFFICE_DANE = 452;
		
		public static final int POS_ini_ACQ_OFFICE_NAME = 452;
		public static final int POS_end_ACQ_OFFICE_NAME = 488;
		
		public static final int POS_ini_TRACK_2 = 488;
		public static final int POS_end_TRACK_2 = 568;
		
		public static final int POS_ini_PINPAD_SERIAL = 568;
		public static final int POS_end_PINPAD_SERIAL = 588;
		
		public static final int POS_ini_TERMINAL = 588;
		public static final int POS_end_TERMINAL = 608;
		
		public static final int POS_ini_ACH_CREDIT_ACC_NR = 608;
		public static final int POS_end_ACH_CREDIT_ACC_NR = 642;
		
		public static final int POS_ini_AKB_PIN_FORMAT = 642;
		public static final int POS_end_AKB_PIN_FORMAT = 674;
		
		public static final int POS_ini_ACC_MIGRATED_IND = 674;
		public static final int POS_end_ACC_MIGRATED_IND = 676;
		
		public static final int POS_ini_ENTRY_MODE_IND = 676;
		public static final int POS_end_ENTRY_MODE_IND = 678;
		
		public static final int POS_ini_EMV_DATA = 678;
		public static final int POS_end_EMV_DATA = 1678;
		
		public static final int POS_ini_CHECK_QUANTITY = 1678;
		public static final int POS_end_CHECK_QUANTITY = 1682;
		
		public static final int POS_ini_PAYING_PERSON_IDEN_NR = 1682;
		public static final int POS_end_PAYING_PERSON_IDEN_NR = 1704;
		
		public static final int POS_ini_FACT_NR = 1704;
		public static final int POS_end_FACT_NR = 1752;
		
		public static final int POS_ini_NOTE = 1752;
		public static final int POS_end_NOTE = 1800;
		
		public static final int POS_ini_ACC_INSCRIPTION_FLAG = 1800;
		public static final int POS_end_ACC_INSCRIPTION_FLAG = 1802;

		public static final int POS_ini_TYPE_ACCOUNT = 206;
		public static final int POS_end_TYPE_ACCOUNT = 208;
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