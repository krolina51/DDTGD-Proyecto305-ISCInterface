package postilion.realtime.iscinterface.message;

import postilion.realtime.iscinterface.message.ISCResMessage.Fields;
import postilion.realtime.sdk.message.Validator;
import postilion.realtime.sdk.message.stream.IStreamFormatter;
import postilion.realtime.sdk.message.stream.StreamFormatterContainer;
import postilion.realtime.sdk.message.stream.StreamFormatterFieldVar;
import postilion.realtime.sdk.message.stream.StreamMessage;

public class KeepAliveMessage extends StreamMessage{
	
	protected static IStreamFormatter iscReqFormatter;
	
	
	//Constantes de uso interno de la clase
	private static final int MAX_SIZE = 2048;
	
	//Variable reservada para llevar el conteo del numero de Bytes que posee el mensaje
	private int currentSize = 0; 

	public KeepAliveMessage() {
		super(MAX_SIZE, iscReqFormatter);
		//setConstantFields();
	}

	//Recibe el IStream formater y el stream en cuestion
	public KeepAliveMessage(IStreamFormatter stream) {
		super(MAX_SIZE, stream);
		//setConstantFields();
	}
	
	static {

		StreamFormatterContainer RequestDataContainer = new StreamFormatterContainer();

		RequestDataContainer = defineBodyFormatters(RequestDataContainer);

		StreamFormatterContainer message = new StreamFormatterContainer();
		message.add(RequestDataContainer);
		iscReqFormatter = message;
		
	}
	
	private static StreamFormatterContainer defineBodyFormatters(StreamFormatterContainer containerDestination) {
		
		StreamFormatterFieldVar varBody = new StreamFormatterFieldVar(Fields._VARIABLE_BODY, Validator.getAns(), 100, true);
		containerDestination.add(varBody);
		
		return containerDestination;
	}
	
	public static class Fields {
		
		public static final String _VARIABLE_BODY = "var-body";
		
	}

}
