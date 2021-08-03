package postilion.realtime.iscinterface.util;

public class TagCreationResult {
	
	private String tagName;
	private String tagVal;
	private String tagError;
	
	public TagCreationResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TagCreationResult(String tagName, String tagVal, String tagError) {
		super();
		this.tagName = tagName;
		this.tagVal = tagVal;
		this.tagError = tagError;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getTagVal() {
		return tagVal;
	}

	public void setTagVal(String tagVal) {
		this.tagVal = tagVal;
	}

	public String getTagError() {
		return tagError;
	}

	public void setTagError(String tagError) {
		this.tagError = tagError;
	}

	@Override
	public String toString() {
		return "TagCreationResult [tagName=" + tagName + ", tagVal=" + tagVal + ", tagError=" + tagError + "]";
	}

}
