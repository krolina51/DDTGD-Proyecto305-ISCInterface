package postilion.realtime.iscinterface.web.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase que encapsula el objeto Json correspondiente a toda la configuracion
 * @author HFLORES
 *
 */
public class WholeTransSetting {
	
	@JsonProperty("messages")
	private TransactionSetting[] allTran;
	
	/**
	 * Atributo de clase que hace referencia a la lista de transacciones configuradas
	 * 
	 */
	public WholeTransSetting() {
		super();
		// TODO Auto-generated constructor stub
	}

	public WholeTransSetting(TransactionSetting[] allTran) {
		super();
		this.allTran = allTran;
	}

	@JsonGetter("messages")
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
