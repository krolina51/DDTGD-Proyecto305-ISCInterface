package postilion.realtime.iscinterface.crypto;

import java.sql.Timestamp;
import java.util.Date;

import postilion.realtime.sdk.util.DateTime;

public class PinPad {
	
	String codOficina;
	String serial;
	String terminal1;
	String terminal2;
	Timestamp fechaInicializacion;
	String key_ini;
	String key_ini_snd;
	Timestamp fechaIntercambio;
	String key_exc;
	String key_exc_snd;
	Timestamp fecha_creacion;
	String usuario_creacion;
	Timestamp fecha_modificacion;
	String usuario_modificacion;
	String habilitado;
	String consecutivo;
	boolean error;
	String responseInit;
	String responseExc;
	/**
	 * @return the codOficina
	 */
	public String getCodOficina() {
		return codOficina;
	}
	/**
	 * @param codOficina the codOficina to set
	 */
	public void setCodOficina(String codOficina) {
		this.codOficina = codOficina;
	}
	/**
	 * @return the serial
	 */
	public String getSerial() {
		return serial;
	}
	/**
	 * @param serial the serial to set
	 */
	public void setSerial(String serial) {
		this.serial = serial;
	}
	/**
	 * @return the terminal1
	 */
	public String getTerminal1() {
		return terminal1;
	}
	/**
	 * @param terminal1 the terminal1 to set
	 */
	public void setTerminal1(String terminal1) {
		this.terminal1 = terminal1;
	}
	/**
	 * @return the terminal2
	 */
	public String getTerminal2() {
		return terminal2;
	}
	/**
	 * @param terminal2 the terminal2 to set
	 */
	public void setTerminal2(String terminal2) {
		this.terminal2 = terminal2;
	}
	/**
	 * @return the fechaInicializacion
	 */
	public Timestamp getFechaInicializacion() {
		return fechaInicializacion;
	}
	/**
	 * @param fechaInicializacion the fechaInicializacion to set
	 */
	public void setFechaInicializacion(Timestamp fechaInicializacion) {
		this.fechaInicializacion = fechaInicializacion;
	}
	/**
	 * @return the key_ini
	 */
	public String getKey_ini() {
		return key_ini;
	}
	/**
	 * @param key_ini the key_ini to set
	 */
	public void setKey_ini(String key_ini) {
		this.key_ini = key_ini;
	}
	/**
	 * @return the key_ini_snd
	 */
	public String getKey_ini_snd() {
		return key_ini_snd;
	}
	/**
	 * @param key_ini_snd the key_ini_snd to set
	 */
	public void setKey_ini_snd(String key_ini_snd) {
		this.key_ini_snd = key_ini_snd;
	}
	/**
	 * @return the fechaIntercambio
	 */
	public Timestamp getFechaIntercambio() {
		return fechaIntercambio;
	}
	/**
	 * @param fechaIntercambio the fechaIntercambio to set
	 */
	public void setFechaIntercambio(Timestamp fechaIntercambio) {
		this.fechaIntercambio = fechaIntercambio;
	}
	/**
	 * @return the key_exc
	 */
	public String getKey_exc() {
		return key_exc;
	}
	/**
	 * @param key_exc the key_exc to set
	 */
	public void setKey_exc(String key_exc) {
		this.key_exc = key_exc;
	}
	/**
	 * @return the key_exc_snd
	 */
	public String getKey_exc_snd() {
		return key_exc_snd;
	}
	/**
	 * @param key_exc_snd the key_exc_snd to set
	 */
	public void setKey_exc_snd(String key_exc_snd) {
		this.key_exc_snd = key_exc_snd;
	}
	/**
	 * @return the error
	 */
	public boolean isError() {
		return error;
	}
	/**
	 * @param error the error to set
	 */
	public void setError(boolean error) {
		this.error = error;
	}
	/**
	 * @return the responseInit
	 */
	public String getResponseInit() {
		return responseInit;
	}
	/**
	 * @param responseInit the responseInit to set
	 */
	public void setResponseInit(String responseInit) {
		this.responseInit = responseInit;
	}
	/**
	 * @return the responseExc
	 */
	public String getResponseExc() {
		return responseExc;
	}
	/**
	 * @param responseExc the responseExc to set
	 */
	public void setResponseExc(String responseExc) {
		this.responseExc = responseExc;
	}
	/**
	 * @return the fecha_creacion
	 */
	public Timestamp getFecha_creacion() {
		return fecha_creacion;
	}
	/**
	 * @param fecha_creacion the fecha_creacion to set
	 */
	public void setFecha_creacion(Timestamp fecha_creacion) {
		this.fecha_creacion = fecha_creacion;
	}
	/**
	 * @return the usuario_creacion
	 */
	public String getUsuario_creacion() {
		return usuario_creacion;
	}
	/**
	 * @param usuario_creacion the usuario_creacion to set
	 */
	public void setUsuario_creacion(String usuario_creacion) {
		this.usuario_creacion = usuario_creacion;
	}
	/**
	 * @return the fecha_modificacion
	 */
	public Timestamp getFecha_modificacion() {
		return fecha_modificacion;
	}
	/**
	 * @param fecha_modificacion the fecha_modificacion to set
	 */
	public void setFecha_modificacion(Timestamp fecha_modificacion) {
		this.fecha_modificacion = fecha_modificacion;
	}
	/**
	 * @return the usuario_modificacion
	 */
	public String getUsuario_modificacion() {
		return usuario_modificacion;
	}
	/**
	 * @param usuario_modificacion the usuario_modificacion to set
	 */
	public void setUsuario_modificacion(String usuario_modificacion) {
		this.usuario_modificacion = usuario_modificacion;
	}
	/**
	 * @return the habilitado
	 */
	public String getHabilitado() {
		return habilitado;
	}
	/**
	 * @param habilitado the habilitado to set
	 */
	public void setHabilitado(String habilitado) {
		this.habilitado = habilitado;
	}
	/**
	 * @return the consecutivo
	 */
	public String getConsecutivo() {
		return consecutivo;
	}
	/**
	 * @param consecutivo the consecutivo to set
	 */
	public void setConsecutivo(String consecutivo) {
		this.consecutivo = consecutivo;
	}
	
	

}
