package postilion.realtime.iscinterface.message;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import postilion.realtime.iscinterface.message.ISCReqMessage.Constants;
import postilion.realtime.iscinterface.message.ISCReqMessage.Fields;
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

public class ISCReqMessage extends StreamMessage {
	
	private static IStreamFormatter iscReqFormatter;
	
	//Constantes de uso interno de la clase
	private static final int MAX_SIZE = 1024;

	public ISCReqMessage() {
		super(MAX_SIZE, iscReqFormatter);
	}

	public ISCReqMessage(IStreamFormatter stream) {
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
	 * creados a partir de la clase ISCReqMessage
	 * 
	 * @param containerDestination
	 * @return
	 **********************************************************************************/
	private static StreamFormatterContainer defineHeadersFormatters(StreamFormatterContainer containerDestination){
		
		containerDestination.add(new StreamFormatterFieldFixed(Fields._02_H_HEADER_FRAME, ValidatorCustom.getAnsc(), 6));	//Header :: HEADER_FRAME
		containerDestination.add(new StreamFormatterFieldFixed(Fields._03_H_CICS_TRAN_CODE, ValidatorCustom.getAnsc(), 4));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._04_H_DELIMITER, ValidatorCustom.getAnsc(), 3));//6	
		containerDestination.add(new StreamFormatterFieldFixed(Fields._05_H_TRAN_CODE, ValidatorCustom.getAnsc(), 4));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._06_H_ATM_ID, ValidatorCustom.getAnsc(), 5));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._07_H_FILLER, ValidatorCustom.getAnsc(), 3));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._08_H_TRAN_SEQ_NR, ValidatorCustom.getAnsc(), 4));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._09_H_STATE, ValidatorCustom.getAnsc(), 3));//6
		containerDestination.add(new StreamFormatterFieldFixed(Fields._10_H_TIME, ValidatorCustom.getAnsc(), 6));//12
		containerDestination.add(new StreamFormatterFieldFixed(Fields._11_H_FILLER_2, ValidatorCustom.getAnsc(), 8));//16
		containerDestination.add(new StreamFormatterFieldFixed(Fields._12_H_SUPER_ID, ValidatorCustom.getAnsc(), 8));
		
		return containerDestination;
	}
	
	public void fromHexStr(String iscReqHexStr) {
		Logger.logLine("STRING HEX:"+iscReqHexStr, false);
		this.putField(ISCReqMessage.Fields._02_H_HEADER_FRAME, Transform.fromHexToBin(iscReqHexStr.substring(0, 12)));
		this.putField(ISCReqMessage.Fields._03_H_CICS_TRAN_CODE, Transform.fromHexToBin(iscReqHexStr.substring(12, 20)));
		this.putField(ISCReqMessage.Fields._04_H_DELIMITER, Transform.fromHexToBin(iscReqHexStr.substring(20, 26)));
		this.putField(ISCReqMessage.Fields._05_H_TRAN_CODE, Transform.fromHexToBin(iscReqHexStr.substring(26, 34)));
		this.putField(ISCReqMessage.Fields._06_H_ATM_ID, Transform.fromHexToBin(iscReqHexStr.substring(34, 44)));
		this.putField(ISCReqMessage.Fields._07_H_FILLER, Transform.fromHexToBin(iscReqHexStr.substring(44, 50)));
		this.putField(ISCReqMessage.Fields._08_H_TRAN_SEQ_NR, Transform.fromHexToBin(iscReqHexStr.substring(50, 58)));
		this.putField(ISCReqMessage.Fields._09_H_STATE, Transform.fromHexToBin(iscReqHexStr.substring(58, 64)));
		this.putField(ISCReqMessage.Fields._10_H_TIME, Transform.fromHexToBin(iscReqHexStr.substring(64, 76)));
		this.putField(ISCReqMessage.Fields._11_H_FILLER_2, Transform.fromHexToBin(iscReqHexStr.substring(76, 92)));
		this.putField(ISCReqMessage.Fields._12_H_SUPER_ID, Transform.fromHexToBin(iscReqHexStr.substring(92, 108)));
		this.putField(ISCReqMessage.Fields._VARIABLE_BODY, Transform.fromHexToBin(iscReqHexStr.substring(108)));
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
		
		containerDestination.add(new StreamFormatterFieldVar(Fields._VARIABLE_BODY, ValidatorCustom.getAnsc(), 1024, true));
		
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
	public void setConstantHeaderFields() {
		Logger.logLine("Seteando campos constantes en cabecera", false);
		
		this.putField(Fields._02_H_HEADER_FRAME, Transform.fromHexToBin(Constants.HEADER_FRAME));		
		this.putField(Fields._03_H_CICS_TRAN_CODE, Transform.fromHexToBin(Constants.CICS_TRAN_CODE));	
		this.putField(Fields._04_H_DELIMITER, Transform.fromHexToBin(Constants.DELIMITER));
		this.putField(Fields._07_H_FILLER, Transform.fromHexToBin(Constants.HEADER_FILLER));
		this.putField(Fields._11_H_FILLER_2, Transform.fromHexToBin(Constants.HEADER_FILLER_2));	
		this.putField(Fields._12_H_SUPER_ID, Transform.fromHexToBin(Constants.SUPER_ID));
		
	}
	
	/**********************************************************************************
	 * Constantes codigos/nombres de constantes
	 * 
	 * @author Javier Flores
	 *
	 **********************************************************************************/
	public static class Constants {
		private Constants() {
		    throw new IllegalStateException();
		  }
		
		//Header
		private static final String HEADER_FRAME = "313233343536";
		private static final String CICS_TRAN_CODE = "E5C9C2C1"; //VIBA-->ebcdic-->hex
		private static final String HEADER_FILLER = "404040"; //Anteriormente 40404040
		private static final String HEADER_FILLER_2 = "4040404040404040";
		private static final String SUPER_ID = "F0F0F0F0F0F0F0F0";
		private static final String DELIMITER = "114040";
		//Body	
		public static final String TAG_119105_PIGNOS_6 = "119105";
		public static final String TAG_119130_DATE_6 = "119130";
		public static final String TAG_1140C3_DEBIT_ACC_TYPE_CLIENT_1 = "1140C3";
		public static final String TAG_11C5D2_DEBIT_ACC_TYPE_CORRES_1 = "11C5D2";
		public static final String TAG_11C3F0_TRAN_AMOUNT_15 = "11C3F0";
		public static final String TAG_119131_SYS_TIME_12 = "119131";
		public static final String TAG_11C37B_COR_ACCOUNT_NR_10 = "11C37B";
		public static final String TAG_114040_DEBIT_ACC_NR_10 = "114040";
		public static final String TAG_11C1C5_ORIGINAL_SEQ_6 = "11C1C5";
		public static final String TAG_11E5C6_CARD_NR_16 = "11E5C6";
		public static final String TAG_11C17A_REC_NR_6 = "11C17A";
		public static final String TAG_1191A1_TRAN_NACIONALITY_1 = "1191A1";
		public static final String TAG_119160_INPUT_NETWORK_2 = "119160";
		public static final String TAG_119161_ACQ_NETWORK_2 = "119161";
		public static final String TAG_119162_TERM_ID_8 = "119162";
		public static final String TAG_119181_ORIGINAL_TRAN_1 = "119181";
		public static final String TAG_11912F_AUTH_CODE_8 = "11912F";
		public static final String TAG_11D135_ = "11D135";
		public static final String TAG_11D142_ = "11D142";
		public static final String TAG_11D137_ = "11D137";
		public static final String TAG_11D136_SERVICE_CODE_4 = "11D136";
		public static final String TAG_119190_COMMERCE_CODE_10 = "119190";
		public static final String TAG_11924A_CREDIT_ENTITY_CODE_4 = "11924A";
		public static final String TAG_119115_CREDIT_ACC_TYPE_1 = "119115";
		public static final String TAG_11912D_AVAL_CREDIT_ACC_NR_20 = "11912D";
		public static final String TAG_119199_TERM_LOCATION_40 = "119199";
		public static final String TAG_11E4F0_ = "11E4F0";
		public static final String TAG_11E4F9_ = "11E4F9";
		public static final String TAG_11E4F8_ = "11E4F8";
		public static final String TAG_11A2C7_PAY_MODE_INDIC = "11A2C7";
		public static final String TAG_1197A5_DEBIT_CARD_TYPE_2 = "1197A5";
		public static final String TAG_11E4F2_IDEN_DOC_TYPE_1 = "11E4F2";
		public static final String TAG_11E4F3_IDEN_DOC_NR_16 = "11E4F3";
		public static final String TAG_11D140_ACQ_ENTITY_4 = "11D140";
		public static final String TAG_11D138_ACQ_OFFICE_4 = "11D138";
		public static final String TAG_11D139_DEVICE_1 = "11D139";
		public static final String TAG_11E5C7_CORRES_CARD_NR_16 = "11E5C7";
		public static final String TAG_1197A7_CORRES_CARD_TYPE_2 = "1197A7";
		public static final String TAG_11A9B1_TRAN_INDICATOR_1 = "11A9B1";
		public static final String TAG_11E5F1_CELULAR_NR_12 = "11E5F1";
		public static final String TAG_11E5F3_VIRT_PURCH_INDICATOR_1 = "11E5F3";
		public static final String TAG_11D141_STANDIN_INDICATOR_1 = "11D141";
		public static final String TAG_11E0E2_TRAN_IDENTIFICATOR_4 = "11E0E2";
		public static final String TAG_11913D_SECURE_AMOUNT_15 = "11913D";
		public static final String TAG_11914D_PA_MODE = "11914D";
		
		//Miscelaneos
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
		private static final String _03_H_CICS_TRAN_CODE = "cics-tran-code";
		private static final String _04_H_DELIMITER = "delimiter";
		public static final String _05_H_TRAN_CODE = "tran-code";
		public static final String _06_H_ATM_ID = "atm-id";
		private static final String _07_H_FILLER = "filler";
		public static final String _08_H_TRAN_SEQ_NR = "tran-seq";
		public static final String _09_H_STATE = "state";
		public static final String _10_H_TIME = "time";
		private static final String _11_H_FILLER_2 = "filler-2";
		private static final String _12_H_SUPER_ID = "super-id";
		public static final String _VARIABLE_BODY = "var-body";
		
	}

	/***********************************************************************************
	 * Metodo toString para visualizar el contenido del objeto de una forma parecida
	 * a como debe ir el mensaje hacia el autorizador
	 **********************************************************************************/
	public String getTotalString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.getField(Fields._02_H_HEADER_FRAME))
		.append(this.getField(Fields._03_H_CICS_TRAN_CODE)).append(this.getField(Fields._04_H_DELIMITER))
		.append(this.getField(Fields._05_H_TRAN_CODE)).append(this.getField(Fields._06_H_ATM_ID))
		.append(this.getField(Fields._07_H_FILLER)).append(this.getField(Fields._08_H_TRAN_SEQ_NR))
		.append(this.getField(Fields._09_H_STATE)).append(this.getField(Fields._10_H_TIME))
		.append(this.getField(Fields._11_H_FILLER_2)).append(this.getField(Fields._12_H_SUPER_ID))
		.append(this.getField(Fields._VARIABLE_BODY));
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
	    sb.append("ISC REQUEST:\n");
	    sb.append("header-frame").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("header-frame"))).append("\n");
	    sb.append("cics-tran-code").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("cics-tran-code"))).append("\n");
	    sb.append("delimiter").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("delimiter"))).append("\n");
	    sb.append("tran-code").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("tran-code"))).append("\n");
	    sb.append("atm-id").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("atm-id"))).append("\n");
	    sb.append("filler").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("filler"))).append("\n");
	    sb.append("tran-seq").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("tran-seq"))).append("\n");
	    sb.append("state").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("state"))).append("\n");
	    sb.append("time").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("time"))).append("\n");
	    sb.append("filler-2").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("filler-2"))).append("\n");
	    sb.append("super-id").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("super-id"))).append("\n");
	    sb.append("var-body").append("\t---> ").append(Transform.fromBinToHex(getField("var-body"))).append("\n");
	    sb.append("splitted-body:\n");
				
		        // Get the regex to be checked 
		        String regex = "11[^01]{1}[a-zA-Z\\d]{3}"; 
		  
		        // Create a pattern from regex 
		        Pattern pattern = Pattern.compile(regex); 
		  
		        // Get the String to be matched 
		        String stringToBeMatched 
		            = Transform.fromBinToHex(this.getField(Fields._VARIABLE_BODY)); 
		  
		        // Create a matcher for the input String 
		        Matcher matcher = pattern.matcher(stringToBeMatched); 
		  
		        // Get the current matcher state 
		        MatchResult result = matcher.toMatchResult(); 
				
				String[] tags = (stringToBeMatched).split(regex);
				
				int i = 0;
		        while (matcher.find()) {
		        	i++;
		        	sb.append("		("+matcher.group().substring(2)+") : "+UtilidadesMensajeria.ebcdicToAscii(tags[i])).append("\n");
		        }
				
				//sb.append("\t").append(ISCReqMessage.Constants.da)
		return sb.toString();
	}
}