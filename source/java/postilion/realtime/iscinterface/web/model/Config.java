package postilion.realtime.iscinterface.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config {
	
	@JsonProperty("config_name")
	private String configName;
	
	@JsonProperty("config_value")
	private boolean configValue;
	
	@JsonProperty("config_type")
	private String configType;

	public Config() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public boolean isConfigValue() {
		return configValue;
	}

	public void setConfigValue(boolean configValue) {
		this.configValue = configValue;
	}

	public String getConfigType() {
		return configType;
	}

	public void setConfigType(String configType) {
		this.configType = configType;
	}

}
