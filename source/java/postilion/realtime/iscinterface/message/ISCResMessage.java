package postilion.realtime.iscinterface.message;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import postilion.realtime.iscinterface.message.ISCReqMessage.Fields;
import postilion.realtime.iscinterface.util.UtilidadesMensajeria;
import postilion.realtime.sdk.message.Validator;
import postilion.realtime.sdk.message.stream.IStreamFormatter;
import postilion.realtime.sdk.message.stream.StreamFormatterContainer;
import postilion.realtime.sdk.message.stream.StreamFormatterFieldFixed;
import postilion.realtime.sdk.message.stream.StreamFormatterFieldVar;
import postilion.realtime.sdk.message.stream.StreamMessage;
import postilion.realtime.sdk.util.convert.Transform;

public class ISCResMessage extends StreamMessage {
	
	protected static IStreamFormatter iscReqFormatter;
	
	
	//Constantes de uso interno de la clase
	private static final int MAX_SIZE = 10240;
	
	//Variable reservada para llevar el conteo del numero de Bytes que posee el mensaje
	private int currentSize = 0; 

	public ISCResMessage() {
		super(MAX_SIZE, iscReqFormatter);
		//setConstantFields();
	}

	//Recibe el IStream formater y el stream en cuestion
	public ISCResMessage(IStreamFormatter stream) {
		super(MAX_SIZE, stream);
		//setConstantFields();
	}

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
	 * creados a partir de la clase ISCReqMessage
	 * 
	 * @param containerDestination
	 * @return
	 **********************************************************************************/
	private static StreamFormatterContainer defineHeadersFormatters(StreamFormatterContainer containerDestination){
		
		//StreamFormatterFieldFixed hTotalLenght = new StreamFormatterFieldFixed(Fields._01_H_TOTAL_LENGTH, Validator.getAns(), 4);
		//containerDestination.add(hTotalLenght);
		StreamFormatterFieldFixed hHeaderFrame = new StreamFormatterFieldFixed(Fields._02_H_HEADER_FRAME, Validator.getAns(), 12);
		containerDestination.add(hHeaderFrame);
		StreamFormatterFieldFixed hDelimiter = new StreamFormatterFieldFixed(Fields._03_H_DELIMITER, Validator.getAns(), 10);
		containerDestination.add(hDelimiter);
		StreamFormatterFieldFixed hCicsTranCode = new StreamFormatterFieldFixed(Fields._04_H_CICS_TRAN_CODE, Validator.getAns(), 8);
		containerDestination.add(hCicsTranCode);
		StreamFormatterFieldFixed hState = new StreamFormatterFieldFixed(Fields._05_H_STATE, Validator.getAns(), 4);
		containerDestination.add(hState);
		StreamFormatterFieldFixed hAtmId = new StreamFormatterFieldFixed(Fields._06_H_ATM_ID, Validator.getAns(), 16);
		containerDestination.add(hAtmId);
		StreamFormatterFieldFixed hTransSeqNr = new StreamFormatterFieldFixed(Fields._07_H_TRAN_SEQ_NR, Validator.getAns(), 8);
		containerDestination.add(hTransSeqNr);
		StreamFormatterFieldFixed hCurBalance = new StreamFormatterFieldFixed(Fields._08_H_CUR_BALANCE, Validator.getAns(), 26);
		containerDestination.add(hCurBalance);
		StreamFormatterFieldFixed hFiller = new StreamFormatterFieldFixed(Fields._09_H_FILLER, Validator.getAns(), 20);
		containerDestination.add(hFiller);
		StreamFormatterFieldFixed hDelimiter2 = new StreamFormatterFieldFixed(Fields._10_H_MSG_DELIMITER, Validator.getAns(), 10);
		containerDestination.add(hDelimiter2);

		
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
		
		StreamFormatterFieldVar varBody = new StreamFormatterFieldVar(Fields._VARIABLE_BODY, Validator.getAns(), 9216, true);
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
		
		private static final String HEADER_FRAME = "313233343536";
		private static final String CICS_TRAN_CODE = "E5C9C2C1"; //VIBA-->ebcdic-->hex
		private static final String HEADER_FILLER = "40404040";
		private static final String HEADER_FILLER_2 = "4040404040404040";
		private static final String SUPER_ID = "F0F0F0F0F0F0F0F0";
		private static final String MSG_DELIMITER = "11C2601D60";
		private static final String SEPARATOR = "11";
		
		
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
		private static final String _02_H_HEADER_FRAME = "header-frame";
		private static final String _03_H_DELIMITER = "delimiter";
		private static final String _04_H_CICS_TRAN_CODE = "cics-tran-code";
		public static final String _05_H_STATE = "state";
		public static final String _06_H_ATM_ID = "atm-id";
		public static final String _07_H_TRAN_SEQ_NR = "tran-seq";
		public static final String _08_H_CUR_BALANCE = "cur-balance";
		private static final String _09_H_FILLER = "filler";
		public static final String _10_H_MSG_DELIMITER = "msg-delimiter";
		
		public static final String _VARIABLE_BODY = "var-body";
		
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
	public String getTotalString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.getField(Fields._01_H_TOTAL_LENGTH)).append(this.getField(Fields._02_H_HEADER_FRAME))
		.append(this.getField(Fields._03_H_DELIMITER)).append(this.getField(Fields._04_H_CICS_TRAN_CODE))
		.append(this.getField(Fields._05_H_STATE)).append(this.getField(Fields._06_H_ATM_ID))
		.append(this.getField(Fields._07_H_TRAN_SEQ_NR)).append(this.getField(Fields._08_H_CUR_BALANCE))
		.append(this.getField(Fields._09_H_FILLER)).append(this.getField(Fields._10_H_MSG_DELIMITER))
		.append(this.getField(Fields._VARIABLE_BODY));
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
	    sb.append("ISC RESPONSE:\n");
	    sb.append("header-frame").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("header-frame"))).append("\n");
	    sb.append("delimiter").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("delimiter"))).append("\n");
	    sb.append("cics-tran-code").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("cics-tran-code"))).append("\n");
	    sb.append("state").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("state"))).append("\n");
	    sb.append("atm-id").append("\t\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("atm-id"))).append("\n");
	    sb.append("tran-seq").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("tran-seq"))).append("\n");
	    sb.append("cur-balance").append("\t\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("cur-balance"))).append("\n");
	    sb.append("filler").append("\t\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("filler"))).append("\n");
	    sb.append("msg-delimiter").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(Transform.fromBinToHex(getField("msg-delimiter")))).append("\n");
	    sb.append("var-body").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("var-body"))).append("\n");

//	    sb.append("splitted-body:\n");
//				
//		        // Get the regex to be checked 
//		        String regex = "11[^01]{1}[a-zA-Z\\d]{3}"; 
//		  
//		        // Create a pattern from regex 
//		        Pattern pattern = Pattern.compile(regex); 
//		  
//		        // Get the String to be matched 
//		        String stringToBeMatched 
//		            = Transform.fromBinToHex(this.getField(Fields._VARIABLE_BODY)); 
//		  
//		        // Create a matcher for the input String 
//		        Matcher matcher = pattern.matcher(stringToBeMatched); 
//		  
//		        // Get the current matcher state 
//		        MatchResult result = matcher.toMatchResult(); 
//				
//				String[] tags = (stringToBeMatched).split(regex);
//				
//				int i = 0;
//		        while (matcher.find()) {
//		        	i++;
//		        	sb.append("		("+matcher.group().substring(2)+") : "+UtilidadesMensajeria.ebcdicToAscii(tags[i])).append("\n");
//		        }
				
				//sb.append("\t").append(ISCReqMessage.Constants.da)
		return sb.toString();
	}
}