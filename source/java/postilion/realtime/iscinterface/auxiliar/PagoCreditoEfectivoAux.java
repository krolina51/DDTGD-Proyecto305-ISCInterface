package postilion.realtime.iscinterface.auxiliar;


import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.ISCInterfaceCB;
import postilion.realtime.iscinterface.database.DBHandler;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.message.ISCReqInMsg.Fields;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.env.calendar.BusinessCalendar;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.DateTime;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class PagoCreditoEfectivoAux {
	
	private static int counter = 0;
	
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor) throws XPostilion {
		
		
		try {
			
			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;
			String tranType = null;
			
			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);
			
			
			if(in.getTotalHexString().substring(46,52).matches("^((F0F4F0)|(F0F5F0)|(F0F6F0)|(F0F7F0))")) {
				businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}
			
			
			String p37 = "0901".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(356,364))))
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(406, 414))));
			
			String p12 = new DateTime().get("HHmmss");
			String p13 = new DateTime().get("MMdd");
			
			String key = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
			String seqNr = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(406, 414)));
			String seqNrReverse = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(414, 422)));
			String keyReverse = null;
			
			StructuredData sd = null;
			
			if(out.getStructuredData() != null) {
				sd = out.getStructuredData();	
			} else {
				sd = new StructuredData();
			}
			
			
			
			String p41 = "0001".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(356,364))))
					.concat("00003   ");
			String bin = "008801";
			String p43 = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(356,364)))
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(432,468))))
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(422,432))))
					.concat("             ");
			String codEntidadAut = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(138,146)));
			if(codEntidadAut.equals("0002")) {
				bin = "007702";
			}else if(codEntidadAut.equals("0023")) {
				bin = "007723";
			}else if(codEntidadAut.startsWith("0052")) {
				bin = "000052";
			}
			
			String tipoCuentaDebitar =  "";
			if (Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(282, 284))).equals("1")) {
				tipoCuentaDebitar = "01";
				sd.put("Indicador_Efectivo_Cheque", "1");
			}else if(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(282, 284))).equals("2")) {
				tipoCuentaDebitar = "02";
				sd.put("Indicador_Efectivo_Cheque", "2");
				sd.put("Numero_Cheques", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1658, 1662))));
			}
			
			String naturaleza = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(284, 286)));
			String tipoCuentaCreditar =  "";
			switch (naturaleza) {
			case "1":
				tipoCuentaCreditar = "00";
				break;
			case "2":
				tipoCuentaCreditar = "40";
				break;
			case "3":
				tipoCuentaCreditar = "41";
				break;
			case "4":
				tipoCuentaCreditar = "42";
				break;
			case "5":
				tipoCuentaCreditar = "30";
				break;	

			default:
				tipoCuentaCreditar = "00";
				break;
			}
			
			String tipoPago = "";
			if(tipoCuentaCreditar.equals("30")
					&& (codEntidadAut.substring(1,2).equals("0"))) {
				tipoPago = "0";
			}else if (tipoCuentaCreditar.equals("30")
					&& (codEntidadAut.substring(1,2).equals("1"))) {
				tipoPago = "1";
			}else {
				switch (Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(286, 288)))) {
				case "2":
					tipoPago = "2";
					break;
				case "3":
					tipoPago = "3";
					break;
				case "4":
					tipoPago = "4";
					break;
				case "5":
					tipoPago = "5";
					break;	

				default:
					tipoPago = "1";
					break;
				}
			}
			
			tranType = "50";
			String cuentaCreditar = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)));
			
			//CAMPO 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, p37);
			
			// PROCESAMIENTO DE REVERSO
			if(Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("080")
					|| Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) {
				
				keyReverse = (String) ISCInterfaceCB.cacheKeyReverseMap.get(seqNrReverse);
				if(keyReverse == null)
					keyReverse = DBHandler.getKeyOriginalTxBySeqNr(seqNrReverse);
				if(keyReverse == null) {
					keyReverse = "0000000000";
					sd.put("REV_DECLINED", "TRUE");
				}
				out.putField(Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS, Pack.resize(keyReverse, 42, '0', true));
				
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0420".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(248, 268)))).concat("0"+cons.substring(2, 5)));
				out.putPrivField(Iso8583Post.PrivBit._011_ORIGINAL_KEY, keyReverse);
				sd.put("B24_Field_95", "000000000000000000000000000000000000000000");
				sd.put("B24_Field_90", keyReverse+"0000000000");
				out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, keyReverse.substring(4,16));
				sd.put("B24_Field_37", keyReverse.substring(4,16));
				
				if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020"))
					tranType = "20";
				
			//PROCESAMIENTO TX FINANCIERA	
			} else {
				Logger.logLine("msg in TransferAux:\n" + in.getTotalHexString(), enableMonitor);
				String p125 =  Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1658, 1662)))
						.concat("                              ")
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1662, 1684))));
				
				//127.22 TAG B24_Field_125
				sd.put("B24_Field_125", p125);
				
				out.putField(Iso8583Post.Bit._059_ECHO_DATA,Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(406, 414))));
				
				
				//127.2 SWITCHKEY
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);		
				ISCInterfaceCB.cacheKeyReverseMap.put(seqNr,key);
			}
				
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, tranType.concat(tipoCuentaDebitar).concat(tipoCuentaCreditar));
			
//			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));
			
			out.putField(Iso8583.Bit._013_DATE_LOCAL, p13);
			
			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);
			
			//TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");
			
			//TRACK2 Field 43
			out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, p43);
			
			//CAMPO 103 CREDIT ACCOUNT
			out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, tipoPago.concat("11")
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(138, 146)))).concat("0")
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)))));
			

			//127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			//127.22 TAG B24_Field_35
			sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaCreditar, 18, '0', false)).concat("=            "));	
			//127.22 TAG B24_Field_41
			sd.put("B24_Field_41", p41);
			
			
			
			////////// TAGS EXTRACT 
			
			sd.put("VIEW_ROUTER", "V2");
			
			sd.put("Codigo_FI_Origen", "1019");
			sd.put("Nombre_FI_Origen", "CIC");			
			sd.put("Identificacion_Canal", "OF");
			sd.put("Canal", "01");
			sd.put("Dispositivo", "D");
			if(tipoCuentaCreditar.equals("30")) {
				sd.put("Codigo_Transaccion_Producto", "02");
				sd.put("Codigo_Transaccion", "01");
				sd.put("Tipo_de_Cuenta_Debitada", "CRE");
				sd.put("Mod_Credito", "5");
			} else {
				sd.put("Codigo_Transaccion_Producto", "06");
				sd.put("Codigo_Transaccion", "01");
				sd.put("Mod_Credito", "3");
			}
			sd.put("Nombre_Transaccion", "DEPOSI");
			sd.put("CLIENT_CARD_NR_1", bin.concat("0000000000000"));
			sd.put("PRIM_ACCOUNT_NR", Pack.resize(cuentaCreditar, 18, '0', false));
			sd.put("Codigo_de_Red", "1019");
			sd.put("Identificacion_Canal", "OF");
			sd.put("Codigo_Establecimiento", "          ");
			sd.put("SEC_ACCOUNT_TYPE", "   ");
			sd.put("PAN_Tarjeta", bin.concat("0000000000000"));
			sd.put("Indicador_AVAL", "1");
			sd.put("Vencimiento", "0000");
			sd.put("SECUENCIA", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(242, 282))));
			sd.put("Ofi_Adqui", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(356, 364))));
			sd.put("Clase_Pago", "2");
			sd.put("Ent_Adq", "0001");
			sd.put("Dispositivo", "0");
			sd.put("Canal", "01");
			sd.put("service_restriction_code", "000");
			sd.put("pos_entry_mode", "000");
			sd.put("Entidad", "0000");
			sd.put("Identificador_Terminal", "0");
			sd.put("Numero_Cedula", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1662, 1684))));
			sd.put("Inscripcion_Indicador", "1");
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
	

}
