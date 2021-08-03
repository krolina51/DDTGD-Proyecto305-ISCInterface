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

public class CopyFieldMapper implements IFieldMapper {

	@Override
	public TagCreationResult mapField(IMessage inputMsg, Field curField, FlowDirection msgDirection, boolean enableLog) throws FileNotFoundException {

		TagCreationResult tcr = new TagCreationResult();
		tcr.setTagName(curField.getTagPrefix() != null ? curField.getTagPrefix() : curField.getDescription());
		
		String extractedVal = "";

//			extractedVal = (curField.getCopyFinalIndex() == 0) ? msgSD.get(curField.getCopyTag()) :
//				msgSD.get(curField.getCopyTag()).substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex()) ;
//			extractedVal = msgSD.get(curField.getCopyTag()).substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex() == 0 ? msgSD.get(curField.getCopyTag()).length() - 1 : curField.getCopyFinalIndex()) ;	

		if (msgDirection.equals(FlowDirection.ISO2ISC)) {
			try {
				if (curField.getCopyFrom() == 1) {

					Logger.logLine("COPY ::" + ((Iso8583Post) inputMsg).getStructuredData().get(curField.getCopyTag()),
							enableLog);
					Logger.logLine("SD ::" + ((Iso8583Post) inputMsg).getStructuredData().get(curField.getCopyTag()),
							enableLog);

				}

				if (!curField.isHeaderField())
					tcr.setTagVal(curField.getTagPrefix());

				if (curField.getCopyFrom() == 0)
					extractedVal = (curField.getCopyFinalIndex() == 0) ? ((Iso8583Post) inputMsg).getField(curField.getCopyTag())
							: ((Iso8583Post) inputMsg).getField(curField.getCopyTag()).substring(curField.getCopyInitialIndex(),
									curField.getCopyFinalIndex());
				else {
					if (((Iso8583Post) inputMsg).getStructuredData().get(curField.getCopyTag()) != null) {
						extractedVal = (curField.getCopyFinalIndex() == 0)
								? ((Iso8583Post) inputMsg).getStructuredData().get(curField.getCopyTag())
								: ((Iso8583Post) inputMsg).getStructuredData().get(curField.getCopyTag())
										.substring(curField.getCopyInitialIndex(), curField.getCopyFinalIndex());
					} else {
						extractedVal = curField.getConditionalVal();
					}

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
			
			Logger.logLine(extractedVal + "--" + Transform.fromAsciiToEbcdic(extractedVal) + "--"
					+ UtilidadesMensajeria.asciiToEbcdic(extractedVal), enableLog);
			tcr.setTagVal(UtilidadesMensajeria
					.asciiToEbcdic(Pack.resize(extractedVal, curField.getTagValueLength(),
							curField.getPadChar() == null ? '0' : curField.getPadChar().charAt(0), false))
					.toUpperCase());
		}
		return tcr;
	}

}
