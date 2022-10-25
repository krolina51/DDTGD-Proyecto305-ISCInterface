package postilion.realtime.iscinterface.auxiliar;


import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
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

public class TransferAuxQR {
	
	private static int counter = 0;
	
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
			
			String p41 = "0001820100002   ";
			String bin = "008801";
			String codOficina = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38)));
			if(codOficina.startsWith("82")) {
				p41 = "0001820200002   ";
			}else if(codOficina.startsWith("8")) {
				p41 = "0001820100002   ";
			}
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
			String cuentaAcreditar = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)));
			
			
			Logger.logLine("msg in TransferAux:\n" + in.getTotalHexString(), enableMonitor);
			
			String p125 =Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(894), lth( 894 + 24) ))) // nombre del comercio
					.concat(Pack.resize("",  24, '0', true))  // referencia 2, Adenda ref 2
					.concat(Pack.resize("",  25, '0', true)) // Adenda ref 3
					.concat(Pack.resize("",  8, '0', true)) // Id registro
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(71), lth( 71 + 1) ))))
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(51), lth( 51 + 16) ))))
					.concat(Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(110),lth( 110 + 14) ))),17,'0',false)) // valor pago
					.concat(Pack.resize("",  2, '0', true)) // tipo cuenta acreditar
					.concat(Pack.resize("",  15, '0', true)) // cuenta acreditar
					.concat(Pack.resize("",  1, '0', true)) // ****** validacion titularidad
					.concat(Pack.resize("8201",  5, '0', true))		
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(942), lth( 942 + 1) )))) // flag transf 1
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(943), lth( 943 + 1) )))) // flag transf 2
					.concat(Pack.resize("",  10, '0', true));
			
			//CAMPO 3 
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, "40".concat(tipoCuentaDebitar).concat(tipoCuentaCreditar));
			
			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime().get("MMddHHmmss"));
			
			//CAMPO 12 TRANSMISSION time
			out.putField(Iso8583.Bit._012_TIME_LOCAL, new DateTime().get("HHmmss"));
			
			//CAMPO 13
			out.putField(Iso8583.Bit._013_DATE_LOCAL, new DateTime().get("MMdd"));
			
			//CAMPO 15
			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);
			
			//TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");
			
			//CAMPO 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "0901"
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38))))
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_SEQUENCE_NR, ISCReqInMsg.POS_end_SEQUENCE_NR)))));
		
			//CAMPO 102 DEBIT ACCOUNT
			out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, "0001".concat(cuentaDebitar));
			
			//CAMPO 103 CREDIT ACCOUNT
			out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "011"
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(138, 146)))).concat("0")
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)))));
			
			//127.2 SWITCHKEY
			out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(mes).concat(dia).concat(hora).concat("0"+cons.substring(2, 5)));

			//127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			//127.22 TAG B24_Field_35
			sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaDebitar, 18, '0', false)).concat("=991200000001"));	
			//127.22 TAG B24_Field_41
			sd.put("B24_Field_41", p41);
			//127.22 TAG B24_Field_125
			sd.put("B24_Field_125", p125);
			
			if(in.getTotalHexString().substring(46,52).matches("^((F0F4F0)|(F0F5F0)|(F0F6F0)|(F0F7F0))"))
				sd.put("NEXTDAY", "TRUE");
			
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
	
	
	
	/**
	 * Metodo específico para transformar referenciacion posicional en trama
	 * @return
	 */
	public static final int limiteTransformadoHexa( int iIndiceTramaDocumento ) {
		return ( iIndiceTramaDocumento - 3 ) * 2; 
	}
	
	public static final int lth( int iIndiceTramaDocumento ) {
		return limiteTransformadoHexa(iIndiceTramaDocumento); 
	}

}
