package postilion.realtime.iscinterface.validator;

import java.io.FileNotFoundException;

import postilion.realtime.iscinterface.util.FlowDirection;
import postilion.realtime.iscinterface.util.TagCreationResult;
import postilion.realtime.iscinterface.web.model.Field;
import postilion.realtime.sdk.message.IMessage;

public interface IFieldMapper {

	public TagCreationResult mapField(IMessage inputMsg, Field curField, FlowDirection msgDirection, boolean enableLog) throws FileNotFoundException;
	
}
