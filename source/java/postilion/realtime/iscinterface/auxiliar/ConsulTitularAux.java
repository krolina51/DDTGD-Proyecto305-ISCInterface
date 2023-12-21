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
import postilion.realtime.sdk.message.bitmap.ProcessingCode;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.message.bitmap.XFieldUnableToConstruct;
import postilion.realtime.sdk.util.DateTime;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class ConsulTitularAux {
	
	public static final String PCODE_CONSULTATITUL_PAGO_HIPOTECARIO = "330000";
	public static final String PCODE_CONSULTATITUL_PAGO_ROTATIVO = "334000";
	public static final String PCODE_CONSULTATITUL_PAGO_OTROSCREDITOS = "334100";
	public static final String PCODE_CONSULTATITUL_PAGO_VEHICULOS = "334200";
	public static final String PCODE_CONSULTATITUL_PAGO_TC = "333000";
	
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

			String cuenta = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(138,174)));	
			String pcode = "";
			String bin = "";
			
			String modalidad = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(182, 184)));
			String entidad = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(174, 182)));
			
			switch (modalidad) {
			case "1":
				pcode = "330000";
				sd.put("Codigo_Transaccion_Producto", "06");
				sd.put("Tipo_de_Cuenta_Debitada", "OTR");
				sd.put("Numero_Terminal", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(236,244))));
				sd.put("Mod_Credito", "1");
				sd.put("Mod_CreditoX1", "1");
				break;
			case "2":
				pcode = "334000";		
				sd.put("Codigo_Transaccion_Producto", "06");
				sd.put("Tipo_de_Cuenta_Debitada", "OTR");
				sd.put("Numero_Terminal", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(236,244))));
				sd.put("Mod_Credito", "2");
				sd.put("Mod_CreditoX1", "2");
				break;
			case "3":
				pcode = "334100";
				sd.put("Codigo_Transaccion_Producto", "06");
				sd.put("Tipo_de_Cuenta_Debitada", "OTR");
				sd.put("Numero_Terminal", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(236,244))));
				sd.put("Mod_Credito", "3");
				sd.put("Mod_CreditoX1", "3");
				break;
			case "4":
				pcode = "334200";
				sd.put("Codigo_Transaccion_Producto", "06");
				sd.put("Tipo_de_Cuenta_Debitada", "OTR");
				sd.put("Numero_Terminal", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(236,244))));
				sd.put("Mod_Credito", "4");
				sd.put("Mod_CreditoX1", "4");
				break;
			case "5":
				pcode = "333000";
				sd.put("Codigo_Transaccion_Producto", "02");
				sd.put("Tipo_de_Cuenta_Debitada", "CRE");
				sd.put("Numero_Terminal", "0000");
				sd.put("Mod_Credito", "5");
				sd.put("Mod_CreditoX1", "5");
				break;

			default:
				break;
			}
			switch (entidad) {
			case "0001":
				bin = "007701";
				break;
			case "0002":
				bin = "007702";	
				break;
			case "0023":
				bin = "007723";
				break;
			case "0052":
				bin = "000052";
				break;

			default:
				break;
			}
			
			sd.put("CLIENT_CARD_NR_1", "0000000000000000");
			sd.put("BIN_Cuenta", "000000");
			sd.put("PRIM_ACCOUNT_NR",cuenta);
			sd.put("PAN_Tarjeta", "                   ");
			sd.put("Vencimiento", "0000");
			// LOGICA PARA AUTORIZAR EN BANCODEBOGOTA, BUSCANDO TITULAR DE TARJETA EN ARCHIVOS
			if(modalidad.equals("5") && entidad.equals("0001")) {
				sd.put("CLIENT_CARD_NR_1", cuenta.substring(2));
				sd.put("BIN_Cuenta", cuenta.substring(2,8));
				sd.put("PRIM_ACCOUNT_NR",cuenta.substring(8));
				sd.put("PAN_Tarjeta", cuenta.substring(2)+"   ");
				sd.put("Vencimiento", "9912");
				sd.put("Tarjeta_Amparada", cuenta.substring(2));
				
				// SE DEBE CONSULTAR LA TARJETA Y RESPONDER DE INMEDIATO
			}
			
			//CAMPO 3
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, "320000");
			
			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));
			
			//Field 12
			out.putField(Iso8583.Bit._012_TIME_LOCAL, new DateTime().get("HHmmss"));
			
			//Field 13
			out.putField(Iso8583.Bit._013_DATE_LOCAL, new DateTime().get("MMdd"));
			
			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);
			
			//TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");
			
			//CAMPO 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "09013300".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(38, 46)))));
			
			//TRACK2 Field 43
			out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, "                                        ");
			
			//127.2 SWITCHKEY
			out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(new DateTime().get("MMddHHmmss")).concat("0"+cons.substring(2, 5)));

			//127.22 TAG B24_Field_17
			sd.put("B24_Field_3", pcode);
			//127.22 TAG B24_Field_17
			sd.put("B24_Field_15", settlementDate);
			//127.22 TAG B24_Field_35
			sd.put("B24_Field_35", bin.concat(cuenta).concat("D            "));
			//127.22 TAG B24_Field_41
			sd.put("B24_Field_41", "0001"+Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(236,244)))+"00003   ");
			
			sd.put("B24_Field_52", "FFFFFFFFFFFFFFFF");

			sd.put("IN_MSG", in.getTotalHexString());
			
			sd.put("VIEW_ROUTER", "V2");
			sd.put("TRANSACTION_INPUT", "CONSULTATITULARIDAD_HOST");
			
			
			sd.put("Codigo_Transaccion", "62");
			sd.put("Nombre_Transaccion", "CONSUL");
			
			sd.put("Codigo_FI_Origen", "1019");
			sd.put("Nombre_FI_Origen", "CIC");
			sd.put("Codigo_de_Red", "1019");
			sd.put("SEC_ACCOUNT_TYPE", "   ");
			sd.put("Codigo_Establecimiento", "          ");
			sd.put("Ent_Adq", "0001");
			sd.put("Entidad", "0000");
			sd.put("Canal", "01");
			sd.put("Identificacion_Canal", "OF");
			
			sd.put("pos_entry_mode", "000");
			sd.put("service_restriction_code", "000");
			sd.put("Identificador_Terminal", "0");
			sd.put("Dispositivo", "0");
			sd.put("FI_CREDITO_REV", "0000");
			sd.put("FI_DEBITO_REV", "0000");
			sd.put("SEC_ACCOUNT_NR_REV", "000000000000000000");
			sd.put("SEC_ACCOUNT_TYPE_REV", "   ");
			sd.put("MIX_ACCOUNT_TYPE_REV", "   ");
			sd.put("MIX_ACCOUNT_NR_REV", "000000000000000000");

			
			
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
	

}
