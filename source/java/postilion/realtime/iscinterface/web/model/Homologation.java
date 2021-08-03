package postilion.realtime.iscinterface.web.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Homologation {
	
	@JsonProperty("value")
	private String value;
	
	@JsonProperty("convertion")
	private String convertion;
	
	public Homologation() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Homologation(String value, String convertion) {
		super();
		this.value = value;
		this.convertion = convertion;
	}

	@JsonGetter("value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@JsonGetter("convertion")
	public String getConvertion() {
		return convertion;
	}

	public void setConvertion(String convertion) {
		this.convertion = convertion;
	}

	@Override
	public String toString() {
		return "Homologation [value=" + value + ", convertion=" + convertion + "]";
	}

}
