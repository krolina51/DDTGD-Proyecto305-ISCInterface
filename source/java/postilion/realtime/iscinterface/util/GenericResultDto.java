package postilion.realtime.iscinterface.util;

public class GenericResultDto {
	
	private String error;
	private String description;
	private String valName;
	private String valComment;
	private int code;
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getValName() {
		return valName;
	}
	public void setValName(String valName) {
		this.valName = valName;
	}
	public String getValComment() {
		return valComment;
	}
	public void setValComment(String valComment) {
		this.valComment = valComment;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}

}
