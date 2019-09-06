package postilion.realtime.iscinterface.message;

import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.sdk.message.Validator;
import postilion.realtime.sdk.message.stream.IStreamFormatter;
import postilion.realtime.sdk.message.stream.StreamFormatterContainer;
import postilion.realtime.sdk.message.stream.StreamFormatterFieldFixed;
import postilion.realtime.sdk.message.stream.StreamFormatterFieldVar;
import postilion.realtime.sdk.message.stream.StreamMessage;
import postilion.realtime.sdk.util.convert.Transform;
import postilion.realtime.iscinterface.validator.ValidatorCustom;

public class ISCReqMessage extends StreamMessage {
	
	protected static IStreamFormatter iscReqFormatter;
	
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
		containerDestination.add(new StreamFormatterFieldFixed(Fields._06_H_ATM_ID, ValidatorCustom.getAnsc(), 4));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._07_H_FILLER, ValidatorCustom.getAnsc(), 4));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._08_H_TRAN_SEQ_NR, ValidatorCustom.getAnsc(), 4));//8
		containerDestination.add(new StreamFormatterFieldFixed(Fields._09_H_STATE, ValidatorCustom.getAnsc(), 3));//6
		containerDestination.add(new StreamFormatterFieldFixed(Fields._10_H_TIME, ValidatorCustom.getAnsc(), 6));//12
		containerDestination.add(new StreamFormatterFieldFixed(Fields._11_H_FILLER_2, ValidatorCustom.getAnsc(), 8));//16
		containerDestination.add(new StreamFormatterFieldFixed(Fields._12_H_SUPER_ID, ValidatorCustom.getAnsc(), 8));
		
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
		Logger.logLine("Seteando campos constantes en cabecera");
		
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
		//Header
		private static final String HEADER_FRAME = "313233343536";
		private static final String CICS_TRAN_CODE = "E5C9C2C1"; //VIBA-->ebcdic-->hex
		private static final String HEADER_FILLER = "40404040";
		private static final String HEADER_FILLER_2 = "4040404040404040";
		private static final String SUPER_ID = "F0F0F0F0F0F0F0F0";
		private static final String DELIMITER = "114040";
		//Body	
		public static final String _01_DATE_TAG = "119130";
		public static final String _02_DEBIT_ACC_TYPE_TAG = "1140C3";
		public static final String _03_TRAN_AMOUNT_TAG = "11C3F0";
		public static final String _04_SYS_TIME_TAG = "119131";
		public static final String _05_DEBIT_ACC_NR_TAG = "114040";
		public static final String _REV_06_ORIGINAL_SEQ = "11C1C5";
		public static final String _06_CARD_NR_TAG = "11E5C6";
		public static final String _07_REC_NR_TAG = "11C17A";
		public static final String _08_TRAN_NACIONALITY_TAG = "1191A1";
		public static final String _09_INPUT_NETWORK_TAG = "119160";
		public static final String _10_ACQ_NETWORK_TAG = "119161";
		public static final String _11_TERM_ID_TAG = "119162";
		public static final String _12_ORIGINAL_TRAN_TAG = "119181";
		public static final String _13_AUTH_CODE_TAG = "11912F";
		public static final String _14_CREDIT_ENTITY_CODE_TAG = "11924A";
		public static final String _15_CREDIT_ACC_TYPE_TAG = "119115";
		public static final String _16_AVAL_CREDIT_ACC_NR_TAG = "11912D";
		public static final String _17_TERM_LOCATION_TAG = "119199";
		public static final String _18_DEBIT_CARD_TYPE_TAG = "1197A5";
		public static final String _19_IDEN_DOC_TYPE_TAG = "11E4F2";
		public static final String _20_IDEN_DOC_NR_TAG = "11E4F3";
		public static final String _21_ACQ_ENTITY_TAG = "11D140";
		public static final String _22_ACQ_OFFICE_TAG = "11D138";
		public static final String _23_DEVICE_TAG = "11D139";
		public static final String _24_CORRES_CARD_NR_TAG = "11E5C7";
		public static final String _25_CORRES_CARD_TYPE_TAG = "1197A7";
		public static final String _26_TRAN_INDICATOR_TAG = "11A9B1";
		public static final String _27_VIRT_PURCH_INDICATOR_TAG = "11E5F3";
		public static final String _28_STANDIN_INDICATOR_TAG = "11D141";
		public static final String _29_TRAN_IDENTIFICATOR_TAG = "11E0E2";
		public static final String _30_SECURE_AMOUNT_TAG = "11913D";
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
}