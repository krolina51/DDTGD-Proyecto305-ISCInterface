package postilion.realtime.iscinterface.message;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import postilion.realtime.iscinterface.message.ISCResMessage.Fields;
import postilion.realtime.iscinterface.util.UtilidadesMensajeria;
import postilion.realtime.sdk.message.Validator;
import postilion.realtime.sdk.message.stream.IStreamFormatter;
import postilion.realtime.sdk.message.stream.StreamFormatterContainer;
import postilion.realtime.sdk.message.stream.StreamFormatterFieldVar;
import postilion.realtime.sdk.message.stream.StreamMessage;
import postilion.realtime.sdk.util.convert.Transform;

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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
	    sb.append("ISC KEEP ALIVE:\n");
	    sb.append("var-body").append("\t---> ").append(UtilidadesMensajeria.ebcdicToAscii(getField("var-body"))).append("\n");;
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
