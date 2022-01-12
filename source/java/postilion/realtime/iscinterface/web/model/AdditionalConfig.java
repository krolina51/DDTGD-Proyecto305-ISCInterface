package postilion.realtime.iscinterface.web.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AdditionalConfig {

	
	@JsonProperty("messages")
	private TransactionSetting[] allTran;
	
	/**
	 * Atributo de clase que hace referencia a la lista de transacciones configuradas
	 * 
	 */
	public AdditionalConfig() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AdditionalConfig(TransactionSetting[] allTran) {
		super();
		this.allTran = allTran;
	}

	@JsonGetter("configs")
	public TransactionSetting[] getAllTran() {
		return allTran;
	}

	public void setAllTran(TransactionSetting[] allTran) {
		this.allTran = allTran;
	}

	@Override
	public String toString() {
		return "WholeTransSetting [allTran=" + Arrays.toString(allTran) + "]";
	}
	
}
