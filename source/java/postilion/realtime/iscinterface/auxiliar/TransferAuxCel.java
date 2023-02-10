package postilion.realtime.iscinterface.auxiliar;


import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.env.calendar.BusinessCalendar;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class TransferAuxCel {
	
	
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor) throws XPostilion {
		
		
		try {
			
			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;
			
			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);
			
			StructuredData sd = null;
			
			if(out.getStructuredData() != null) {
				sd = out.getStructuredData();	
			} else {
				sd = new StructuredData();
			}
			
			
			if(in.getTotalHexString().substring(46,52).matches("^((F0F4F0)|(F0F5F0)|(F0F6F0)|(F0F7F0))")) {
				businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}
			
			String bin = "008801";
			String codOficina = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38)));
			String p41 =  "0001" + codOficina + "00006   ";
			
			String tipoCuentaCreditar =  "";
			if (Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(146, 148))).equals("0")) {
				tipoCuentaCreditar = "10";
			}else if(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(146, 148))).equals("1")) {
				tipoCuentaCreditar = "20";
			}
			
			String tipoCuentaDebitar =  "";
			if (Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(180, 182))).equals("0")) {
				tipoCuentaDebitar = "10";
			}else if(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(180, 182))).equals("1")) {
				tipoCuentaDebitar = "20";
			}
			
			String mes = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(292, 296)));
			String dia = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(288, 292)));
			String hora = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(52, 64)));
			String cuentaDebitar = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_DEBIT_ACC_NR, ISCReqInMsg.POS_end_DEBIT_ACC_NR)));
			
			Logger.logLine("msg in TransferAux:\n" + in.getTotalHexString(), enableMonitor);
			String celular = "";
			final int posInicialCelular = 1782;
			final int lonCelularHexa = 48;
			final int I_LONGITUD_CON_CELULAR = 1830; // Si en la transacccion no esta presente el celular, tratandose de una cel2cel con destino ATH desde BBOG
			if(in.getTotalHexString().length() >= I_LONGITUD_CON_CELULAR ) { // 907 * 2 = 1814, lo vimos en depuracion.
				celular = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(posInicialCelular, posInicialCelular + lonCelularHexa)));
				//1784, 1808
			} else {
				celular = "3333333333";
			}

			//int longitudHexa = in.getTotalHexString().length();
			String p125 =
					Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1684, 1732)))
							.concat(Pack.resize("",  24, ' ', true) )
							.concat("1")
							.concat((Pack.resize(celular,  24, ' ', true))), 90, ' ', true);
			
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, "40".concat(tipoCuentaDebitar).concat(tipoCuentaCreditar));
			
//			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, mes.concat(dia).concat(hora));
			
			out.putField(Iso8583.Bit._013_DATE_LOCAL, mes.concat(dia));
			
			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);
			
			//TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=2912000");

			//CAMPO 37 Retrieval Reference Number
			
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "0901".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38))))
					.concat(secuencialesAlearios(4)));
					//.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_SEQUENCE_NR, ISCReqInMsg.POS_end_SEQUENCE_NR)))));
			
			//CAMPO 102 DEBIT ACCOUNT
			out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, "00010".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_DEBIT_ACC_NR, ISCReqInMsg.POS_end_DEBIT_ACC_NR)))));
			
			//CAMPO 103 CREDIT ACCOUNT
			out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "011"
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(138, 146)))).concat("0")
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)))));
			
			//127.2 SWITCHKEY
			out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(mes).concat(dia).concat(hora).concat("0"+cons.substring(2, 5)));

			//127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			//127.22 TAG B24_Field_35
			sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaDebitar, 18, '0', false)).concat("=291200000001"));	
			//127.22 TAG B24_Field_41
			sd.put("B24_Field_41", p41);
			//127.22 TAG B24_Field_125
			sd.put("B24_Field_125", p125);
			sd.put("B24_Field_125_numerocelular", celular);
			
			////////// TAGS EXTRACT 
			
			sd.put("VIEW_ROUTER", "V2");
			
			sd.put("TRANSACTION_INPUT", "TRANSFERENCIA_CEL2CEL");
			sd.put("TRANSACTION_CNB_TYPE", "CEL2CEL_TRANSFERENCIAS");
			
			sd.put("Codigo_FI_Origen", "1019");
			sd.put("Nombre_FI_Origen", "CIC");		
			sd.put("Canal", "01");
			sd.put("Dispositivo", "D");
			
			sd.put("FI_DEBITO",(out.isFieldSet(Iso8583.Bit._102_ACCOUNT_ID_1)) ? out.getField(Iso8583.Bit._102_ACCOUNT_ID_1).substring(0, 4) : "0000");
			if (out.isFieldSet(Iso8583.Bit._103_ACCOUNT_ID_2)) {
				sd.put("FI_CREDITO", out.getField(Iso8583.Bit._103_ACCOUNT_ID_2).substring(3, 7));
				sd.put("Clase_Pago", out.getField(Iso8583.Bit._103_ACCOUNT_ID_2).substring(0, 1));
			}
			sd.put("Ent_Adq",p41.substring(0, 4));
			
			sd.put("Codigo_Transaccion_Producto",
					(out.getProcessingCode().getFromAccount().equals("10")) ? "05" : "04");
			sd.put("Tipo_de_Cuenta_Debitada",
					(out.getProcessingCode().getFromAccount().equals("10")) ? "AHO" : "CTE");
			sd.put("Codigo_Transaccion", "23");
			sd.put("Nombre_Transaccion", "TRANSD");
			

			sd.put("CLIENT_CARD_NR_1", bin.concat("0000000000000"));
			sd.put("PRIM_ACCOUNT_NR", Pack.resize(cuentaDebitar, 18, '0', false));
			sd.put("Codigo_de_Red", "1019");
			sd.put("Numero_Terminal", "8201");
			sd.put("Identificacion_Canal", "IT");
			sd.put("Codigo_Establecimiento", "          ");
			sd.put("SEC_ACCOUNT_NR",
					(out.isFieldSet(Iso8583.Bit._103_ACCOUNT_ID_2))
							? out.getField(Iso8583.Bit._103_ACCOUNT_ID_2).substring(7).trim()
							: "00000000000000000");
			sd.put("SEC_ACCOUNT_TYPE", out.getProcessingCode().getToAccount());
			sd.put("PAN_Tarjeta", bin.concat(Pack.resize(cuentaDebitar.substring(3), 13, '0', false)));
			sd.put("Tarjeta_Amparada", bin.concat(Pack.resize(cuentaDebitar.substring(3), 13, '0', false)));
			sd.put("Indicador_AVAL", "1");
			sd.put("Vencimiento", "9912");
			sd.put("Ent_Adq", "0001");
			sd.put("Dispositivo", "0");
			sd.put("Canal", "01");
			sd.put("service_restriction_code", "000");
			sd.put("pos_entry_mode", "000");
			sd.put("Entidad", "0000");
			sd.put("Identificador_Terminal", "0");
			sd.put("Numero_Cedula", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1662, 1684))));
			sd.put("Transaccion_Unica", "C201");
			sd.put("Numero_Factura", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1684, 1732))));
			sd.put("IN_MSG", in.getTotalHexString());
			///////// FIN TAGS EXTRACT
			
			out.putStructuredData(sd);	
			
		}catch(Exception e) {
			e.toString();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		
		
		
		return out;
	}
	
	public static final String secuencialesAlearios(int iCuantos)
	{
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i< iCuantos; i++ ) {
			sb.append( "" + (int)( Math.random() * 10 ) );
		}
		return sb.toString();
	}
}
