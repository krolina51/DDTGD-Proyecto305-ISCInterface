package postilion.realtime.iscinterface.auxiliar;


import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.ISCInterfaceCB;
import postilion.realtime.iscinterface.database.DBHandler;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
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

public class PagoConvenioInternetAux {
	
	/*
	 * Clase Auxiliar para proceso de campos transaccion Pago de convenios que viaja por el canal internet
	 * */
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor, boolean isNextDay) throws XPostilion {
		
		StructuredData field_structure_w = new StructuredData();
		try {
			
			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;
			String tranType = null;
			
			StructuredData sd = null;
			StructuredData sdOriginal = new StructuredData();
			
			if(out.getStructuredData() != null) {
				sd = out.getStructuredData();	
			} else {
				sd = new StructuredData();
			}
			
			sd.put("TXINNEXTDAY", isNextDay ? "TRUE": "FALSE");
			if(in.getTotalHexString().substring(46,52).matches("^((F0F4F0)|(F0F5F0)|(F0F6F0)|(F0F7F0))")
					|| Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._10_H_NEXTDAY_IND)).equals("1")) {
				
				if(isNextDay) {
					businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
					settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
				}else {
					businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
					settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
				}
				
			}else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}
			
			String p37 = "0901"
				    .concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38))))
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(38, 46))));
			
			String p12 = new DateTime().get("HHmmss");
			String p13 = new DateTime().get("MMdd");
			
			String key = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
			String seqNr = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(618, 626)));
			String seqNrReverse = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(626, 634)));
			String keyReverse = null;
			
			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);
			
			
			
			
			tranType = "40";
			String debitAccountType = "";
			String valueAccountType = "";
			String cuenta = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(208,240)));
			String factura = Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(146,206))).replace(" ", ""), 30, '0', false);
			String datosAdicionales = Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(296,536))).replace(" ", ""), 30, '0', false);
					
			String p62 = factura.concat(datosAdicionales);
	
			//Construccion Campo 125
			String p125 = "";
			p125 = Pack.resize(" ",90,' ',true);		
			
			//VERIFICANDO TIPO DE CUENTA DE LA TX
			valueAccountType = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_TYPE_ACCOUNT, ISCReqInMsg.POS_end_TYPE_ACCOUNT)));
			switch (valueAccountType) {
			case "0":
				debitAccountType = "10";
				break;
			case "1":			
				debitAccountType = "20";
				break;
			default:
				field_structure_w.put("06_ERRORS","El valor del campo tipo Cuenta es incorrecto" + valueAccountType);	
				debitAccountType ="10";
				break;
			}
			
			
			
			// PROCESAMIENTO DE REVERSO
			if(Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("080")) {
				
				sdOriginal = DBHandler.getKeyOriginalTxBySeqNr(seqNrReverse);
				keyReverse = sdOriginal.get("KeyOriginalTx");
				
				if(keyReverse == null) {
					keyReverse = "0000000000";
					sd.put("REV_DECLINED", "TRUE");
				}
				out.putField(Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS, Pack.resize(keyReverse, 42, '0', true));
				
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0420".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(248, 268)))).concat("0"+cons.substring(2, 5)));
				out.putPrivField(Iso8583Post.PrivBit._011_ORIGINAL_KEY, keyReverse);
				
				if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020"))
					tranType = "20";
				
			//PROCESAMIENTO TX FINANCIERA	
			} else {
				out.putField(Iso8583Post.Bit._059_ECHO_DATA,Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(618, 626))));			
				//127.2 SWITCHKEY
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);		
				ISCInterfaceCB.cacheKeyReverseMap.put(seqNr,key);
			}
			//Field 3
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, tranType.concat(debitAccountType).concat("00"));
			
			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));
			
			//Field 12
			out.putField(Iso8583.Bit._012_TIME_LOCAL, p12);
			
			//Field 13
			out.putField(Iso8583.Bit._013_DATE_LOCAL, p13);
			
			//Field 15
			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);
			
			//TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "008801".concat(cuenta.substring(3).concat("D99120000000100000")));

			//Field 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, p37);
				
			//Field 102
			out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, Pack.resize(cuenta, 18, '0', false));
			
			//Field 103
			out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, 
					"0"
					.concat("0001")
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(578,586))))
					.concat("0")
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(858,874)))));
						

			//127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			//127.22 TAG B24_Field_35
			sd.put("B24_Field_35", "008801".concat(cuenta.substring(3).concat("D99120000000100000")));
			//127.22 TAG B24_Field_62
			sd.put("B24_Field_62", p62);
			//127.22 TAG B24_Field_125
			sd.put("B24_Field_125", p125);
			sd.put("IN_MSG", in.getTotalHexString());
			
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
