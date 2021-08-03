package postilion.realtime.iscinterface.util;

import java.util.ArrayList;
import java.util.List;

import postilion.realtime.sdk.message.IMessage;

public class MsgMappedResult {
	
	private IMessage inputMsg;
	private String outputMsg;
	private IMessage outputMsgObj;
	private List<TagCreationResult> errors;
	private boolean containsError;
	
	public MsgMappedResult() {
		super();
		
		errors = new ArrayList<TagCreationResult>();
		// TODO Auto-generated constructor stub
	}

	public MsgMappedResult(IMessage inputMsg, String outputMsg, List<TagCreationResult> errors, boolean containsError) {
		super();
		
		errors = new ArrayList<TagCreationResult>();
		
		this.inputMsg = inputMsg;
		this.outputMsg = outputMsg;
		this.errors = errors;
		this.containsError = containsError;
	}
	
	public MsgMappedResult(IMessage inputMsg, IMessage outputMsgObj, List<TagCreationResult> errors, boolean containsError) {
		super();
		
		errors = new ArrayList<TagCreationResult>();
		
		this.inputMsg = inputMsg;
		this.outputMsgObj = outputMsgObj;
		this.errors = errors;
		this.containsError = containsError;
	}

	public IMessage getInputMsg() {
		return inputMsg;
	}

	public void setInputMsg(IMessage inputMsg) {
		this.inputMsg = inputMsg;
	}

	public String getOutputMsg() {
		return outputMsg;
	}

	public void setOutputMsg(String outputMsg) {
		this.outputMsg = outputMsg;
	}
	
	public IMessage getOutputMsgObj() {
		return outputMsgObj;
	}

	public void setOutputMsgObj(IMessage outputMsgObj) {
		this.outputMsgObj = outputMsgObj;
	}

	public List<TagCreationResult> getErrors() {
		return errors;
	}

	public void setErrors(List<TagCreationResult> errors) {
		this.errors = errors;
	}

	public boolean isContainsError() {
		return containsError;
	}

	public void setContainsError(boolean containsError) {
		this.containsError = containsError;
	}
		

	
}
