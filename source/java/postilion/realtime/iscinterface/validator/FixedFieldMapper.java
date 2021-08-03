package postilion.realtime.iscinterface.validator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;

import postilion.realtime.iscinterface.util.FlowDirection;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.TagCreationResult;
import postilion.realtime.iscinterface.util.UtilidadesMensajeria;
import postilion.realtime.iscinterface.web.model.Field;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.message.IMessage;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class FixedFieldMapper implements IFieldMapper{
	
	@Override
	public TagCreationResult mapField(IMessage inputMsg, Field curField, FlowDirection msgDirection, boolean enableLog) throws FileNotFoundException {
		
		TagCreationResult tcr = new TagCreationResult();
		tcr.setTagName(curField.getTagPrefix() != null ? curField.getTagPrefix() : curField.getDescription());
		
		String extractedVal = "";
		
		if (msgDirection.equals(FlowDirection.ISO2ISC)) {
			
			try {
				if (curField.isHeaderField()) {
					extractedVal = curField.getValueHex() != null ? curField.getValueHex()
							: UtilidadesMensajeria.asciiToEbcdic(curField.getValue()).toUpperCase();
					Logger.logLine(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
							+ UtilidadesMensajeria.asciiToEbcdic(extractedVal), enableLog);
					tcr.setTagVal(extractedVal);
				}

				else {
					extractedVal = curField.getTagPrefix()
							.concat(UtilidadesMensajeria.asciiToEbcdic(curField.getValue()).toUpperCase());
					Logger.logLine(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
							+ UtilidadesMensajeria.asciiToEbcdic(extractedVal), enableLog);
					tcr.setTagVal(extractedVal);
				}
			} 
			catch (Exception e) {
				StringWriter outError = new StringWriter();
				e.printStackTrace(new PrintWriter(
						outError + "\n" + "ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString()));
				Logger.logLine("ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString(),
						enableLog);
				EventRecorder.recordEvent(
						new Exception("ERROR COPYING FIELD: " + curField.getTagPrefix() + outError.toString()));
				tcr.setTagError(curField.getTagPrefix());
			}
			
		}
		return tcr;
	}

}
