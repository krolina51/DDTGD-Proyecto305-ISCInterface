package postilion.realtime.iscinterface.web.model;

import java.util.Arrays;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase que encapsula el objeto Json correspondiente a la configuracion de una transaccion en especifico
 * @author HFLORES
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionSetting {
	
	/**
	 * Atributo de clase que sirve como descripcion de la trasaccion, normalmente es un texto informativo
	 * 
	 */
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("tran_key")
	private String tranKey;
	
	@JsonProperty("pre_operations")
	private String preOps;
	
	@JsonProperty("phases")
	private HashMap<Integer, String> phases;
	
	@JsonProperty("direction")
	private String direction;
	
	@JsonProperty("auxiliar_class")
	private String auxiliarClass;
	
	/**
	 * Atributo de clase que contiene la lista de campos que seran procesados para armado
	 * del mensaje correspondiente a una transaccion
	 * 
	 */
	@JsonProperty("fields")
	private Field[] fields;
	
	public TransactionSetting() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TransactionSetting(String description, String tranKey, String preOps, Field[] fields) {
		super();
		this.description = description;
		this.tranKey = tranKey;
		this.preOps = preOps;
		this.fields = fields;
	}
	
	public TransactionSetting(String description, String tranKey, String preOps, Field[] fields, String direction, String auxiliarClass) {
		super();
		this.description = description;
		this.tranKey = tranKey;
		this.preOps = preOps;
		this.fields = fields;
		this.direction = direction;
		this.auxiliarClass = auxiliarClass;
	}
	
	public TransactionSetting(String description, String tranKey, String preOps, Field[] fields, String direction) {
		super();
		this.description = description;
		this.tranKey = tranKey;
		this.preOps = preOps;
		this.fields = fields;
		this.direction = direction;
		
	}
	
	public TransactionSetting(String description, String tranKey, String preOps, HashMap<Integer, String> phases, Field[] fields) {
		super();
		this.description = description;
		this.tranKey = tranKey;
		this.preOps = preOps;
		this.phases = phases;
		this.fields = fields;
	}
	
	@JsonGetter("description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonGetter("tran_key")
	public String getTranKey() {
		return tranKey;
	}

	public void setTranKey(String tranKey) {
		this.tranKey = tranKey;
	}
	
	@JsonGetter("pre_operations")
	public String getPreOps() {
		return preOps;
	}

	public void setPreOps(String preOps) {
		this.preOps = preOps;
	}
	
	@JsonGetter("phases")
	public HashMap<Integer, String> getPhases() {
		return phases;
	}

	public void setPhases(HashMap<Integer, String> phases) {
		this.phases = phases;
	}

	@JsonGetter("fields")
	public Field[] getFields() {
		return fields;
	}

	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	@JsonGetter("direction")
	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	@JsonGetter("auxiliar_class")
	public String getAuxiliarClass() {
		return auxiliarClass;
	}

	public void setAuxiliarClass(String auxiliarClass) {
		this.auxiliarClass = auxiliarClass;
	}

	@Override
	public String toString() {
		return "TransactionSetting [description=" + description + ", tranKey=" + tranKey + ", preOps=" + preOps
				+ ", phases=" + phases + ", direction=" + direction + ", auxiliarClass=" + auxiliarClass + ", fields="
				+ Arrays.toString(fields) + "]";
	}


	
}
