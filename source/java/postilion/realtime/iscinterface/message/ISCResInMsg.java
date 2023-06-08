package postilion.realtime.iscinterface.message;

import postilion.realtime.iscinterface.validator.ValidatorCustom;
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

		StreamFormatterFieldFixed hDelimiter = new StreamFormatterFieldFixed(Fields._01_H_DELIMITER, ValidatorCustom.getAnsc(), 5);
		containerDestination.add(hDelimiter);
		StreamFormatterFieldFixed tranCode = new StreamFormatterFieldFixed(Fields._02_H_TRAN_CODE, ValidatorCustom.getAnsc(), 4);
		containerDestination.add(tranCode);
		StreamFormatterFieldFixed state = new StreamFormatterFieldFixed(Fields._03_H_STATE, ValidatorCustom.getAnsc(), 2);
		containerDestination.add(state);
		StreamFormatterFieldFixed terminal = new StreamFormatterFieldFixed(Fields._04_H_TERMINAL, ValidatorCustom.getAnsc(), 4);
		containerDestination.add(terminal);
		StreamFormatterFieldFixed filler = new StreamFormatterFieldFixed(Fields._05_H_FILLER, ValidatorCustom.getAnsc(), 4);
		containerDestination.add(filler);
		StreamFormatterFieldFixed tranSeq = new StreamFormatterFieldFixed(Fields._06_H_TRAN_SEQ_NR, ValidatorCustom.getAnsc(), 4);
		containerDestination.add(tranSeq);

		
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
		
		
	}
	
	
	/**********************************************************************************
	 * Constantes codigos/nombres de campos
	 * 
	 * @author Javier Flores
	 *
	 **********************************************************************************/
	public static class Fields {
		
		public static final String _01_H_TOTAL_LENGTH = "total-length";
		public static final String _01_H_DELIMITER = "delimiter";
		public static final String _02_H_TRAN_CODE = "tran-code";
		public static final String _03_H_STATE = "state";
		public static final String _04_H_TERMINAL = "terminal";
		public static final String _05_H_FILLER = "filler";
		public static final String _06_H_TRAN_SEQ_NR = "tran-seq";
		public static final String _VARIABLE_BODY = "var-body";	
		
	}
	
	public void fromHexStr(String iscReqHexStr) {
		this.putField(ISCResInMsg.Fields._01_H_DELIMITER, Transform.fromHexToBin(iscReqHexStr.substring(0, 10)));
		this.putField(ISCResInMsg.Fields._02_H_TRAN_CODE, Transform.fromHexToBin(iscReqHexStr.substring(10, 18)));
		this.putField(ISCResInMsg.Fields._03_H_STATE, Transform.fromHexToBin(iscReqHexStr.substring(18, 22)));
		this.putField(ISCResInMsg.Fields._04_H_TERMINAL, Transform.fromHexToBin(iscReqHexStr.substring(22, 30)));
		this.putField(ISCResInMsg.Fields._05_H_FILLER, Transform.fromHexToBin(iscReqHexStr.substring(30, 38)));
		this.putField(ISCResInMsg.Fields._06_H_TRAN_SEQ_NR, Transform.fromHexToBin(iscReqHexStr.substring(38, 46)));
		this.putField(ISCResInMsg.Fields._VARIABLE_BODY, Transform.fromHexToBin(iscReqHexStr.substring(46)));
	}
	
	
	/***********************************************************************************
	 * Metodo que retorna la longuitud del msg, sin incluir al campo total length,
	 * debe ser llamado despues de settear todos los demas campos
	 **********************************************************************************/
	public int getPreviewLenght() {
		
		int length = 0;
		
		length += this.getField(Fields._01_H_DELIMITER).length() + this.getField(Fields._02_H_TRAN_CODE).length()
				+ this.getField(Fields._03_H_STATE).length() + this.getField(Fields._04_H_TERMINAL).length()
				+ this.getField(Fields._05_H_FILLER).length() + this.getField(Fields._06_H_TRAN_SEQ_NR).length();
		
		return length;
	}
	
	/***********************************************************************************
	 * Metodo toString para visualizar el contenido del objeto de una forma parecida
	 * a como debe ir el mensaje hacia el autorizador
	 **********************************************************************************/
	public String getTotalHexString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(Transform.fromBinToHex(this.getField(Fields._01_H_DELIMITER)))
		.append(Transform.fromBinToHex(this.getField(Fields._02_H_TRAN_CODE)))
		.append(Transform.fromBinToHex(this.getField(Fields._03_H_STATE)))
		.append(Transform.fromBinToHex(this.getField(Fields._04_H_TERMINAL)))
		.append(Transform.fromBinToHex(this.getField(Fields._05_H_FILLER)))
		.append(Transform.fromBinToHex(this.getField(Fields._06_H_TRAN_SEQ_NR)))
		.append(Transform.fromBinToHex(this.getField(Fields._VARIABLE_BODY)));
		
		return sb.toString();
	}

	/***********************************************************************************
	 * Metodo toString para visualizar el contenido del objeto de una forma parecida
	 * a como debe ir el mensaje hacia el autorizador
	 **********************************************************************************/
	public String getTotalString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.getField(Fields._01_H_DELIMITER)).append(this.getField(Fields._02_H_TRAN_CODE))
		.append(this.getField(Fields._03_H_STATE)).append(this.getField(Fields._04_H_TERMINAL))
		.append(this.getField(Fields._05_H_FILLER)).append(this.getField(Fields._06_H_TRAN_SEQ_NR))
		.append(this.getField(Fields._VARIABLE_BODY));
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ISC REQIN RESPONSE:\n");
	    sb.append("delimiter").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("delimiter"))).append("\n");
	    sb.append("tran-code").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("tran-code"))).append("\n");
	    sb.append("state").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("state"))).append("\n");
	    sb.append("terminal").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("terminal"))).append("\n");
	    sb.append("filler").append("\t\t---> ").append(Transform.fromEbcdicToAscii(getField("filler"))).append("\n");
	    sb.append("tran-seq").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("tran-seq"))).append("\n");
	    sb.append("var-body").append("\t---> ").append(Transform.fromEbcdicToAscii(getField("var-body"))).append("\n");
	    //sb.append(Transform.fromEbcdicToAscii((getField("var-body")))).append("\n");


		return sb.toString();
	}
}