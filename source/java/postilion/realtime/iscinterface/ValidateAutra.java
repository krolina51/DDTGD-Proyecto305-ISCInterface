package postilion.realtime.iscinterface;

import java.util.Arrays;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.genericinterface.translate.bitmap.Base24Ath;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.XFieldUnableToConstruct;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Transform;

public class ValidateAutra {
	
	public String ruta;
	public int rute;
	public String p100Valor;
	
	
	public ValidateAutra() {
		this.ruta = "";
		this.rute = 1;
		this.p100Valor = null;
	}

	/**
	 * @return the ruta
	 */
	public String getRuta() {
		return ruta;
	}

	/**
	 * @param ruta the ruta to set
	 */
	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	/**
	 * @return the rute
	 */
	public int getRute() {
		return rute;
	}

	/**
	 * @param rute the rute to set
	 */
	public void setRute(int rute) {
		this.rute = rute;
	}

	/**
	 * @return the p100Valor
	 */
	public String getP100Valor() {
		return p100Valor;
	}

	/**
	 * @param p100Valor the p100Valor to set
	 */
	public void setP100Valor(String p100Valor) {
		this.p100Valor = p100Valor;
	}

	
	/************************************************************************************
	 *
	 * @param Base24Ath
	 * @return int 1, routing to AUTRA, 0 routing to Capa Integracion
	 * @throws XPostilion if field 37 is not present
	 * @throws Exception
	 ************************************************************************************/
	public static ValidateAutra getRoutingData(ISCReqInMsg iscInReq, boolean enableMonitor)
			throws XPostilion {
		ValidateAutra validateAutra = new ValidateAutra();
		
		try {
			
			String codigoTx = Transform.fromEbcdicToAscii(iscInReq.getField(ISCReqInMsg.Fields._04_H_AUTRA_CODE));
			String codigoOficina = Transform.fromEbcdicToAscii(iscInReq.getField(ISCReqInMsg.Fields._06_H_OFFICE_CODE));
			String tarjeta = null;
			String naturaleza = null;
			String bin = null;
			String serial = null;
			String terminal = null;
			
			if(codigoTx.equals("8550")) {
				tarjeta = Transform.fromEbcdicToAscii(Transform.fromHexToBin(iscInReq.getTotalHexString().substring(468, 500)));
				naturaleza = Transform.fromEbcdicToAscii(Transform.fromHexToBin(iscInReq.getTotalHexString().substring(ISCReqInMsg.POS_ini_TRAN_NATURE, ISCReqInMsg.POS_end_TRAN_NATURE)));
				bin = tarjeta.substring(0,6);
				serial = Transform.fromEbcdicToAscii(Transform.fromHexToBin(iscInReq.getTotalHexString().substring(548,568)));
				terminal = Transform.fromEbcdicToAscii(Transform.fromHexToBin(iscInReq.getTotalHexString().substring(568,588)));
			}
			
			if(codigoTx.equals("8580")) {
				serial = Transform.fromEbcdicToAscii(Transform.fromHexToBin(
						iscInReq.getTotalHexString().substring(ISCReqInMsg.POS_ini_SERIAL, ISCReqInMsg.POS_end_SERIAL)));
				terminal = Transform.fromEbcdicToAscii(Transform.fromHexToBin(iscInReq.getTotalHexString()
						.substring(ISCReqInMsg.POS_ini_TERMINALPINPAD, ISCReqInMsg.POS_end_TERMINALPINPAD)));
			}
			
			if(codigoTx.equals("8510") || codigoTx.equals("8520")) {
				tarjeta = Transform.fromEbcdicToAscii(Transform.fromHexToBin(iscInReq.getTotalHexString().substring(858)));
			}
			
			if(codigoTx.equals("8510") || codigoTx.equals("8520")) {
				tarjeta = Transform.fromEbcdicToAscii(Transform.fromHexToBin(iscInReq.getTotalHexString().substring(858)));
			}
			
			if(codigoTx.equals("9050")) {
				tarjeta = Transform.fromEbcdicToAscii(Transform.fromHexToBin(iscInReq.getTotalHexString().substring(136,168)));
			}
			
			
			
			String keyBinNaturalezaSerial = null;
			String keyBinNaturalezaTerminal = null;
			String keyBinNaturaleza = null;
			String keyNaturaleza = null;
			String keyNaturalezaSerial = null;
			String keyNaturalezaTerminal = null;
			String keyTarjetaNaturaleza = null;
			String keyTarjetaNaturalezaSerial = null;
			String keyTarjetaNaturalezaTerminal = null;
			String keyTranCodeOfi = null;
			String keyTranCodeOfiSerial = null;
			String keyTranCodeOfiTerminal = null;
			String value[] = null;
			
			
			keyBinNaturalezaSerial = (naturaleza != null && bin != null && serial != null) ? codigoTx+"_"+codigoOficina+"_"+naturaleza+"_"+serial+"_"+bin : null;
			keyBinNaturalezaTerminal = (naturaleza != null && bin != null && terminal != null) ? codigoTx+"_"+codigoOficina+"_"+naturaleza+"_"+terminal+"_"+bin : null;
			keyBinNaturaleza = (naturaleza != null && bin != null) ? codigoTx+"_"+codigoOficina+"_"+naturaleza+"_"+bin : null;
			keyBinNaturaleza = (naturaleza != null && bin != null) ? codigoTx+"_"+codigoOficina+"_"+naturaleza+"_"+bin : null;
			keyTarjetaNaturalezaSerial = (naturaleza != null && tarjeta != null && serial != null) ? codigoTx+"_"+codigoOficina+"_"+naturaleza+"_"+serial+"_"+tarjeta : null;
			keyTarjetaNaturalezaTerminal = (naturaleza != null && tarjeta != null && terminal != null) ? codigoTx+"_"+codigoOficina+"_"+naturaleza+"_"+terminal+"_"+tarjeta : null;
			keyTarjetaNaturaleza = (naturaleza != null && tarjeta != null) ? codigoTx+"_"+codigoOficina+"_"+naturaleza+"_"+tarjeta : null;
			keyNaturalezaSerial = (naturaleza != null && serial != null) ? codigoTx+"_"+codigoOficina+"_"+naturaleza+"_"+serial : null;
			keyNaturalezaTerminal = (naturaleza != null && terminal != null) ? codigoTx+"_"+codigoOficina+"_"+naturaleza+"_"+terminal : null;
			keyNaturaleza = (naturaleza != null) ? codigoTx+"_"+codigoOficina+"_"+naturaleza : null;
			keyTranCodeOfiSerial = (serial != null) ? codigoTx+"_"+codigoOficina+"_"+serial : null;
			keyTranCodeOfiTerminal = (terminal != null) ? codigoTx+"_"+codigoOficina+"_"+terminal : null;
			keyTranCodeOfi = codigoTx+"_"+codigoOficina;
			Logger.logLine("keyBinNaturaleza " + keyBinNaturaleza, enableMonitor);
			Logger.logLine("keyTarjetaNaturaleza " + keyTarjetaNaturaleza, enableMonitor);
			Logger.logLine("keyNaturaleza " + keyNaturaleza, enableMonitor);
			
			ISCInterfaceCB.filtroISC.forEach((k,v) -> {
				Logger.logLine("key: " + k + " with value: " + v, enableMonitor);
			});
			
			// Verifica naturaleza, serial y bines
			if(keyBinNaturalezaSerial != null && ISCInterfaceCB.filtroISC.containsKey(keyBinNaturalezaSerial)) {
				value = ISCInterfaceCB.filtroISC.get(keyBinNaturalezaSerial).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			
			// Verifica naturaleza, terminal y bines
			if(keyBinNaturalezaTerminal != null && ISCInterfaceCB.filtroISC.containsKey(keyBinNaturalezaTerminal)) {
				value = ISCInterfaceCB.filtroISC.get(keyBinNaturalezaTerminal).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			
			// Verifica naturaleza y bines
			if(keyBinNaturaleza != null && ISCInterfaceCB.filtroISC.containsKey(keyBinNaturaleza)) {
				value = ISCInterfaceCB.filtroISC.get(keyBinNaturaleza).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			
			// Verifica naturaleza, serial y tarjetas
			if(keyTarjetaNaturalezaSerial != null && ISCInterfaceCB.filtroISC.containsKey(keyTarjetaNaturalezaSerial)) {
				value = ISCInterfaceCB.filtroISC.get(keyTarjetaNaturalezaSerial).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			
			// Verifica naturaleza, terminal y tarjetas
			if(keyTarjetaNaturalezaTerminal != null && ISCInterfaceCB.filtroISC.containsKey(keyTarjetaNaturalezaTerminal)) {
				value = ISCInterfaceCB.filtroISC.get(keyTarjetaNaturalezaTerminal).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			
			// Verifica naturaleza y tarjetas
			if(keyTarjetaNaturaleza != null && ISCInterfaceCB.filtroISC.containsKey(keyTarjetaNaturaleza)) {
				value = ISCInterfaceCB.filtroISC.get(keyTarjetaNaturaleza).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			
			// Verifica solo naturaleza con serial
			if(keyNaturalezaSerial != null && ISCInterfaceCB.filtroISC.containsKey(keyNaturalezaSerial)) {
				value = ISCInterfaceCB.filtroISC.get(keyNaturalezaSerial).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			// Verifica solo naturaleza con terminal
			if(keyNaturalezaTerminal != null && ISCInterfaceCB.filtroISC.containsKey(keyNaturalezaTerminal)) {
				value = ISCInterfaceCB.filtroISC.get(keyNaturalezaTerminal).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			
			// Verifica solo naturaleza
			if(keyNaturaleza != null && ISCInterfaceCB.filtroISC.containsKey(keyNaturaleza)) {
				value = ISCInterfaceCB.filtroISC.get(keyNaturaleza).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			
			// Verifica solo codigo de tx y oficina y serial
			if(keyTranCodeOfiSerial != null && ISCInterfaceCB.filtroISC.containsKey(keyTranCodeOfiSerial)) {
				value = ISCInterfaceCB.filtroISC.get(keyTranCodeOfiSerial).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			// Verifica solo codigo de tx y oficina y terminal
			if(keyTranCodeOfiTerminal != null && ISCInterfaceCB.filtroISC.containsKey(keyTranCodeOfiTerminal)) {
				value = ISCInterfaceCB.filtroISC.get(keyTranCodeOfiTerminal).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			
			// Verifica solo codigo de tx y oficina
			if(keyTranCodeOfi != null && ISCInterfaceCB.filtroISC.containsKey(keyTranCodeOfi)) {
				value = ISCInterfaceCB.filtroISC.get(keyTranCodeOfi).split("_");
				validateAutra.setRuta(value[0]);
				validateAutra.setRute(value[0].toLowerCase().equals("capa") ? TransactionRouting.INT_CAPA_DE_INTEGRACION 
						: TransactionRouting.INT_AUTRA);
				validateAutra.setP100Valor(value[1]);
				
				Logger.logLine("validateAutra Ruta: " + validateAutra.getRuta(), enableMonitor);
				Logger.logLine("validateAutra Rute: " + validateAutra.getRute(), enableMonitor);
				Logger.logLine("validateAutra p100: " + validateAutra.getP100Valor(), enableMonitor);
				return validateAutra;
			}
			
		} catch (Exception e) {
			EventRecorder.recordEvent(
					new Exception("Aramando mensaje: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					Utils.getStringMessageException(e) }));
		}

		return validateAutra;
	}
	
	public static final class TransactionRouting {
		public static final int INT_CAPA_DE_INTEGRACION = 0;
		public static final int INT_AUTRA = 1;
	}

}
