package postilion.realtime.iscinterface.web.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase que encapsula el objeto Json correspondiente a un campo especifico de un transaccion
 * @author HFLORES
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Field {
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("is_header_field")
	private boolean isHeaderField;
	
	@JsonProperty("tag_prefix")
	private String tagPrefix;
	
	@JsonProperty("tag_value_length")
	private int tagValueLength;
	
	@JsonProperty("field_type")
	private String fieldType;
	
	@JsonProperty("copy_from")
	private byte copyFrom;
	
	@JsonProperty("copy_to")
	private String copyTo;
	
	@JsonProperty("copy_tag")
	private String copyTag;
	
	@JsonProperty("optional_tag")
	private String optionalTag;
	
	@JsonProperty("copy_ini_index")
	private int copyInitialIndex;
	
	@JsonProperty("copy_end_index")
	private int copyFinalIndex;
	
	@JsonProperty("value")
	private String value;
	
	@JsonProperty("conditional_value")
	private String conditionalVal;
	
	@JsonProperty("value_hex")
	private String valueHex;
	
	@JsonProperty("dummy_property")
	private String dummyPro;
	
	@JsonProperty("homologation")
	private Homologation[] homologations;
	
	@JsonProperty("pad_char")
	private String padChar;
	
	@JsonProperty("validate")
	private String validate;
	
	public Field() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Field(String description, boolean isHeaderField, String tagPrefix, int tagValueLength, String fieldType,
			byte copyFrom, String copyTag, byte copyInitialIndex, byte copyFinalIndex, String value, String conditionalVal, String valueHex,
			Homologation[] homologations) {
		super();
		this.description = description;
		this.isHeaderField = isHeaderField;
		this.tagPrefix = tagPrefix;
		this.tagValueLength = tagValueLength;
		this.fieldType = fieldType;
		this.copyFrom = copyFrom;
		this.copyTag = copyTag;
		this.copyInitialIndex = copyInitialIndex;
		this.copyFinalIndex = copyFinalIndex;
		this.value = value;
		this.conditionalVal = conditionalVal;
		this.valueHex  = valueHex;
		this.homologations = homologations;
	}
	
	public Field(String description, boolean isHeaderField, String tagPrefix, int tagValueLength, String fieldType,
			byte copyFrom, byte copyTo, String copyTag, byte copyInitialIndex, byte copyFinalIndex, String value, String conditionalVal, String valueHex,
			Homologation[] homologations) {
		super();
		this.description = description;
		this.isHeaderField = isHeaderField;
		this.tagPrefix = tagPrefix;
		this.tagValueLength = tagValueLength;
		this.fieldType = fieldType;
		this.copyFrom = copyFrom;
		this.copyTag = copyTag;
		this.copyInitialIndex = copyInitialIndex;
		this.copyFinalIndex = copyFinalIndex;
		this.value = value;
		this.conditionalVal = conditionalVal;
		this.valueHex  = valueHex;
		this.homologations = homologations;
	}
	
	public Field(String description, boolean isHeaderField, String tagPrefix, int tagValueLength, String fieldType,
			byte copyFrom, String copyTag, byte copyInitialIndex, byte copyFinalIndex, String value, String conditionalVal, String valueHex,
			Homologation[] homologations, String padChar) {
		super();
		this.description = description;
		this.isHeaderField = isHeaderField;
		this.tagPrefix = tagPrefix;
		this.tagValueLength = tagValueLength;
		this.fieldType = fieldType;
		this.copyFrom = copyFrom;
		this.copyTag = copyTag;
		this.copyInitialIndex = copyInitialIndex;
		this.copyFinalIndex = copyFinalIndex;
		this.value = value;
		this.conditionalVal = conditionalVal;
		this.valueHex  = valueHex;
		this.homologations = homologations;
		this.padChar = padChar;
	}
	
	public Field(String description, boolean isHeaderField, String tagPrefix, int tagValueLength, String fieldType,
			byte copyFrom, String copyTag, String optionalTag, byte copyInitialIndex, byte copyFinalIndex, String value, String conditionalVal, String valueHex,
			Homologation[] homologations) {
		super();
		this.description = description;
		this.isHeaderField = isHeaderField;
		this.tagPrefix = tagPrefix;
		this.tagValueLength = tagValueLength;
		this.fieldType = fieldType;
		this.copyFrom = copyFrom;
		this.copyTag = copyTag;
		this.optionalTag = optionalTag;
		this.copyInitialIndex = copyInitialIndex;
		this.copyFinalIndex = copyFinalIndex;
		this.value = value;
		this.conditionalVal = conditionalVal;
		this.valueHex  = valueHex;
		this.homologations = homologations;
	}
	
	public Field(String description, boolean isHeaderField, String tagPrefix, int tagValueLength, String fieldType,
			byte copyFrom, String copyTag, byte copyInitialIndex, byte copyFinalIndex, String value, String valueHex,
			Homologation[] homologations) {
		super();
		this.description = description;
		this.isHeaderField = isHeaderField;
		this.tagPrefix = tagPrefix;
		this.tagValueLength = tagValueLength;
		this.fieldType = fieldType;
		this.copyFrom = copyFrom;
		this.copyTag = copyTag;
		this.copyInitialIndex = copyInitialIndex;
		this.copyFinalIndex = copyFinalIndex;
		this.value = value;
		this.valueHex  = valueHex;
		this.homologations = homologations;
	}

	@JsonGetter("description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonGetter("is_header_field")
	public boolean isHeaderField() {
		return isHeaderField;
	}

	public void setHeaderField(boolean isHeaderField) {
		this.isHeaderField = isHeaderField;
	}

	@JsonGetter("tag_value_length")
	public int getTagValueLength() {
		return tagValueLength;
	}

	public void setTagValueLength(int tagValueLength) {
		this.tagValueLength = tagValueLength;
	}

	@JsonGetter("field_type")
	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	@JsonGetter("copy_from")
	public byte getCopyFrom() {
		return copyFrom;
	}

	public void setCopyFrom(byte copyFrom) {
		this.copyFrom = copyFrom;
	}
	
	@JsonGetter("copy_to")
	public String getCopyTo() {
		return copyTo;
	}

	public void setCopyTo(String copyTo) {
		this.copyTo = copyTo;
	}

	@JsonGetter("copy_tag")
	public String getCopyTag() {
		return copyTag;
	}

	public void setCopyTag(String copyTag) {
		this.copyTag = copyTag;
	}
	
	@JsonGetter("optional_tag")
	public String getOptionalTag() {
		return optionalTag;
	}

	public void setOptionalTag(String optionalTag) {
		this.optionalTag = optionalTag;
	}

	@JsonGetter("copy_ini_index")
	public int getCopyInitialIndex() {
		return copyInitialIndex;
	}

	public void setCopyInitialIndex(int copyInitialIndex) {
		this.copyInitialIndex = copyInitialIndex;
	}

	@JsonGetter("copy_end_index")
	public int getCopyFinalIndex() {
		return copyFinalIndex;
	}

	public void setCopyFinalIndex(int copyFinalIndex) {
		this.copyFinalIndex = copyFinalIndex;
	}

	@JsonGetter("value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@JsonGetter("conditional_value")
	public String getConditionalVal() {
		return conditionalVal;
	}

	public void setConditionalVal(String conditionalVal) {
		this.conditionalVal = conditionalVal;
	}

	@JsonGetter("tag_prefix")
	public String getTagPrefix() {
		return tagPrefix;
	}

	public void setTagPrefix(String tagPrefix) {
		this.tagPrefix = tagPrefix;
	}

	@JsonGetter("value_hex")
	public String getValueHex() {
		return valueHex;
	}

	public void setValueHex(String valueHex) {
		this.valueHex = valueHex;
	}
	
	@JsonGetter("dummy_property")
	public String getDummyPro() {
		return dummyPro;
	}

	public void setDummyPro(String dummyPro) {
		this.dummyPro = dummyPro;
	}

	@JsonGetter("homologation")
	public Homologation[] getHomologations() {
		return homologations;
	}

	public void setHomologations(Homologation[] homologations) {
		this.homologations = homologations;
	}

	@JsonGetter("pad_char")
	public String getPadChar() {
		return padChar;
	}

	public void setPadChar(String padChar) {
		this.padChar = padChar;
	}

	@JsonGetter("validate")
	public String getValidate() {
		return validate;
	}

	public void setValidate(String validate) {
		this.validate = validate;
	}

	@Override
	public String toString() {
		return "Field [description=" + description + ", isHeaderField=" + isHeaderField + ", tagPrefix=" + tagPrefix
				+ ", tagValueLength=" + tagValueLength + ", fieldType=" + fieldType + ", copyFrom=" + copyFrom
				+ ", copyTo=" + copyTo + ", copyTag=" + copyTag + ", optionalTag=" + optionalTag + ", copyInitialIndex="
				+ copyInitialIndex + ", copyFinalIndex=" + copyFinalIndex + ", value=" + value + ", conditionalVal="
				+ conditionalVal + ", valueHex=" + valueHex + ", dummyPro=" + dummyPro + ", homologations="
				+ Arrays.toString(homologations) + ", padChar=" + padChar + ", validate=" + validate + "]";
	}


}
