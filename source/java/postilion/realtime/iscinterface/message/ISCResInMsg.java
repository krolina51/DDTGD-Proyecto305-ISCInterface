package postilion.realtime.iscinterface.message;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import postilion.realtime.iscinterface.message.ISCReqMessage.Constants;
import postilion.realtime.iscinterface.message.ISCReqMessage.Fields;
import postilion.realtime.iscinterface.util.UtilidadesMensajeria;
import postilion.realtime.iscinterface.validator.ValidatorCustom;
import postilion.realtime.sdk.message.Validator;
import postilion.realtime.sdk.message.stream.IStreamFormatter;
import postilion.realtime.sdk.message.stream.StreamFormatterContainer;
import postilion.realtime.sdk.message.stream.StreamFormatterFieldFixed;
import postilion.realtime.sdk.message.stream.StreamFormatterFieldVar;
import postilion.realtime.sdk.message.stream.StreamMessage;
import postilion.realtime.sdk.util.convert.Transform;

public class ISCResInMsg extends StreamMessage {
	
	protected static IStreamFormatter iscReqFormatter;
	
	
	//Constantes de uso interno de la clase
	private static final int MAX_SIZE = 10240;
	
	//Variable reservada para llevar el conteo del numero de Bytes que posee el mensaje
	private int currentSize = 0; 

	public ISCResInMsg() {
		super(MAX_SIZE, iscReqFormatter);
		//setConstantFields();
	}

	//Recibe el IStream formater y el stream en cuestion
	public ISCResInMsg(IStreamFormatter stream) {
		super(MAX_SIZE, stream);
		//setConstantFields();
	}

	static {

		StreamFormatterContainer RequestDataContainer = new StreamFormatterContainer();

		//RequestDataContainer = defineHeadersFormatters(RequestDataContainer);
		RequestDataContainer = defineBodyFormatters(RequestDataContainer);

		StreamFormatterContainer message = new StreamFormatterContainer();
		message.add(RequestDataContainer);
		iscReqFormatter = message;
		
	}

	/**********************************************************************************
	 * 
	 * Metodo que define el orden y structura de campos para la cabecera de los objetos
	 * creados a partir de la clase ISCReqMessage
	 * 
	 * @param containerDestination
	 * @return
	 **********************************************************************************/
	private static StreamFormatterContainer defineHeadersFormatters(StreamFormatterContainer containerDestination){
		
		//StreamFormatterFieldFixed hTotalLenght = new StreamFormatterFieldFixed(Fields._01_H_TOTAL_LENGTH, Validator.getAns(), 4);
		//containerDestination.add(hTotalLenght);
		StreamFormatterFieldFixed tranCode = new StreamFormatterFieldFixed(Fields._02_H_TRAN_CODE, Validator.getAns(), 4);
		containerDestination.add(tranCode);
		StreamFormatterFieldFixed hDelimiter = new StreamFormatterFieldFixed(Fields._03_H_DELIMITER, Validator.getAns(), 10);
		containerDestination.add(hDelimiter);
		StreamFormatterFieldFixed autraCode = new StreamFormatterFieldFixed(Fields._04_H_AUTRA_CODE, Validator.getAns(), 4);
		containerDestination.add(autraCode);
		StreamFormatterFieldFixed terminal = new StreamFormatterFieldFixed(Fields._05_H_TERMINAL, Validator.getAns(), 4);
		containerDestination.add(terminal);
		StreamFormatterFieldFixed officeCode = new StreamFormatterFieldFixed(Fields._06_H_OFFICE_CODE, Validator.getAns(), 4);
		containerDestination.add(officeCode);
		StreamFormatterFieldFixed tranSeq = new StreamFormatterFieldFixed(Fields._07_H_TRAN_SEQ_NR, Validator.getAns(), 4);
		containerDestination.add(tranSeq);
		StreamFormatterFieldFixed state = new StreamFormatterFieldFixed(Fields._08_H_STATE, Validator.getAns(), 3);
		containerDestination.add(state);
		StreamFormatterFieldFixed time = new StreamFormatterFieldFixed(Fields._09_H_TIME, Validator.getAns(), 6);
		containerDestination.add(time);
		StreamFormatterFieldFixed nextDay = new StreamFormatterFieldFixed(Fields._10_H_NEXTDAY_IND, Validator.getAns(), 1);
		containerDestination.add(nextDay);
		StreamFormatterFieldFixed filler = new StreamFormatterFieldFixed(Fields._11_H_FILLER, Validator.getAns(), 15);
		containerDestination.add(filler);

		
		return containerDestination;
	}
	
	/**********************************************************************************
	 * 
	 * Metodo que define el orden y structura de campos para el cuerpo de los objetos
	 * creados a partir de la clase ISCReqMessage
	 * 
	 * @param containerDestination
	 * @return
	 **********************************************************************************/
	private static StreamFormatterContainer defineBodyFormatters(StreamFormatterContainer containerDestination) {
		
		StreamFormatterFieldVar varBody = new StreamFormatterFieldVar(Fields._VARIABLE_BODY, ValidatorCustom.getAnsc(), 9216, true);
		containerDestination.add(varBody);
		
		return containerDestination;
	}
	
	/**********************************************************************************
	 * 
	 * Metodo auxiliar el mismo es invocado desde el constructor de la clase de manera
	 * los separadores se coloquen desde la creación misma de los objetos de tipo
	 * ISCReqMessage
	 * @param msg
	 * 
	 **********************************************************************************/
	public void setConstantFields() {	
		this.putField(Fields._03_H_DELIMITER, Constants.DELIMITER);	
	}

	/**********************************************************************************
	 * 
	 * Sobreescritura del metodo de StreamMessage en este se realizan validaciones
	 * previas del campo a "settearse", como transformación de valores o colocación de
	 * valores constantes para campos que asi lo requieran
	 * 
	 **********************************************************************************/
	@Override
	public void putField(String fieldName, String fieldVal) {
	
		
		super.putField(fieldName, fieldVal);
	}

	
	/**********************************************************************************
	 * Constantes codigos/nombres de constantes
	 * 
	 * @author Javier Flores
	 *
	 **********************************************************************************/
	public static class Constants {
		
		private static final String HEADER_FRAME = "123456";
		private static final String CICS_TRAN_CODE = "E2D9D3D5"; //SRLN
		private static final String HEADER_FILLER = "40404040";
		private static final String HEADER_FILLER_2 = "4040404040404040";
		private static final String SUPER_ID = "F0F0F0F0F0F0F0F0";
		private static final String MSG_DELIMITER = "11C2601D60";
		private static final String SEPARATOR = "11";
		private static final String DELIMITER = "1140401D60";
		private static final String STATE = "F120";
		
		
		private static final String DATE_TAG = "9130";
		private static final String DEBIT_ACC_TYPE_TAG = "40C3";
		private static final String TRAN_AMOUNT_TAG = "C3F0";
		private static final String SYS_TIME_TAG = "9131";
		private static final String DEBIT_ACC_NR_TAG = "4040";
		private static final String CARD_NR_TAG = "E5C6";
		private static final String REC_NR_TAG = "C17A";
		private static final String TRAN_NACIONALITY_TAG = "91A1";
		private static final String INPUT_NETWORK_TAG = "9160";
		private static final String ACQ_NETWORK_TAG = "9161";
		private static final String TERM_ID_TAG = "9162";
		private static final String ORIGINAL_TRAN_TAG = "9181";
		private static final String AUTH_CODE_TAG = "912F";
		private static final String CREDIT_ENTITY_CODE_TAG = "924A";
		private static final String CREDIT_ACC_TYPE_TAG = "9115";
		private static final String AVAL_CREDIT_ACC_NR_TAG = "912D";
		private static final String TERM_LOCATION_TAG = "9199";
		private static final String DEBIT_CARD_TYPE_TAG = "97A5";
		private static final String IDEN_DOC_TYPE_TAG = "E4F2";
		private static final String IDEN_DOC_NR_TAG = "E4F3";
		private static final String ACQ_ENTITY_TAG = "D140";
		private static final String ACQ_OFFICE_TAG = "D138";
		private static final String DEVICE_TAG = "D139";
		private static final String CORRES_CARD_NR_TAG = "E5C7";
		private static final String CORRES_CARD_TYPE_TAG = "97A7";
		private static final String TRAN_INDICATOR_TAG = "A9B1";
		private static final String VIRT_PURCH_INDICATOR_TAG = "E5F3";
		private static final String STANDIN_INDICATOR_TAG = "D141";
		private static final String TRAN_IDENTIFICATOR_TAG = "E0E2";
		private static final String CORRES_CARD_NR_2_TAG = "913D";
		
		
		
		private static final String BIN_TRAN_CODE = "450942";
		private static final String NATIONAL_CUR_CODE = "170";
		private static final String ATH_ACQ_CODE = "10000000054";
		
	}
	
	
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
		private static final String _02_H_HEADER_FRAME = "header-frame";
		private static final String _04_H_CICS_TRAN_CODE = "cics-tran-code";
		public static final String _05_H_STATE = "state";
		public static final String _06_H_ATM_ID = "atm-id";
		public static final String _08_H_CUR_BALANCE = "cur-balance";
		private static final String _09_H_FILLER = "filler";
		public static final String _10_H_MSG_DELIMITER = "msg-delimiter";
		private static final String _03_H_CICS_TRAN_CODE = "cics-tran-code";
		private static final String _04_H_DELIMITER = "delimiter";
		public static final String _05_H_TRAN_CODE = "tran-code";
		private static final String _07_H_FILLER = "filler";
		public static final String _08_H_TRAN_SEQ_NR = "tran-seq";
		public static final String _09_H_STATE = "state";
		public static final String _10_H_TIME = "time";		
		
	}
	
	
	/***********************************************************************************
	 * Metodo que retorna la longuitud del msg, sin incluir al campo total length,
	 * debe ser llamado despues de settear todos los demas campos
	 **********************************************************************************/
	public int getPreviewLenght() {
		
		int length = 0;
		
		length += this.getField(Fields._02_H_HEADER_FRAME).length() + this.getField(Fields._03_H_DELIMITER).length()
				+ this.getField(Fields._04_H_CICS_TRAN_CODE).length() + this.getField(Fields._05_H_STATE).length()
				+ this.getField(Fields._06_H_ATM_ID).length() + this.getField(Fields._07_H_TRAN_SEQ_NR).length()
				+ this.getField(Fields._08_H_CUR_BALANCE).length() + this.getField(Fields._09_H_FILLER).length() 
				+ this.getField(Fields._10_H_MSG_DELIMITER).length();
		
		return length;
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

	/***********************************************************************************
	 * Metodo toString para visualizar el contenido del objeto de una forma parecida
	 * a como debe ir el mensaje hacia el autorizador
	 **********************************************************************************/
	public String getTotalString() {
		
		StringBuilder sb = new StringBuilder();
		
//		sb.append(this.getField(Fields._01_H_TOTAL_LENGTH)).append(this.getField(Fields._02_H_HEADER_FRAME))
//		.append(this.getField(Fields._03_H_DELIMITER)).append(this.getField(Fields._04_H_CICS_TRAN_CODE))
//		.append(this.getField(Fields._05_H_STATE)).append(this.getField(Fields._06_H_ATM_ID))
//		.append(this.getField(Fields._07_H_TRAN_SEQ_NR)).append(this.getField(Fields._08_H_CUR_BALANCE))
//		.append(this.getField(Fields._09_H_FILLER)).append(this.getField(Fields._10_H_MSG_DELIMITER))
		sb.append(UtilidadesMensajeria.ebcdicToAscii(this.getField(Fields._VARIABLE_BODY)));
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
//	    sb.append("ISC RESPONSE:\n");
//	    sb.append("tran-code").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("tran-code"))).append("\n");
//	    sb.append("delimiter").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("delimiter"))).append("\n");
//	    sb.append("autra-tran-code").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("autra-tran-code"))).append("\n");
//	    sb.append("terminal").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("terminal"))).append("\n");
//	    sb.append("office-code").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("office-code"))).append("\n");
//	    sb.append("tran-seq").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("tran-seq"))).append("\n");
//	    sb.append("state").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("state"))).append("\n");
//	    sb.append("time").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("time"))).append("\n");
//	    sb.append("nextday-ind").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("nextday-ind"))).append("\n");
//	    sb.append("filler").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("filler"))).append("\n");
	    sb.append(Transform.fromBinToHex((getField("var-body")))).append("\n");

//	    sb.append("splitted-body:\n");
//				
//	    sb.append("header-frame").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("header-frame"))).append("\n");
//	    sb.append("delimiter").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("delimiter"))).append("\n");
//	    sb.append("cics-tran-code").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("cics-tran-code"))).append("\n");
//	    sb.append("state").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("state"))).append("\n");
//	    sb.append("atm-id").append("\t\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("atm-id"))).append("\n");
//	    sb.append("tran-seq").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("tran-seq"))).append("\n");
//	    sb.append("cur-balance").append("\t\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("cur-balance"))).append("\n");
//	    sb.append("filler").append("\t\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("filler"))).append("\n");
//	    sb.append("msg-delimiter").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(Transform.fromBinToHex(getField("msg-delimiter")))).append("\n");
//	    sb.append("var-body").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("var-body"))).append("\n");
//		        }
				
				//sb.append("\t").append(ISCReqMessage.Constants.da)
		return sb.toString();
	}
}