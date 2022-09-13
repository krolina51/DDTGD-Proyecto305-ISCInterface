package postilion.realtime.iscinterface.auxiliar;


import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.message.ISCReqInMsg.Fields;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.eventrecorder.EventRecorder;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;
import postilion.realtime.sdk.util.convert.Transform;

public class PagoCreditoEfectivoAux {
	
	private static int counter = 0;
	
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor) throws XPostilion {
		
		
		try {
			
			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);
			
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
			}else if(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(282, 284))).equals("2")) {
				tipoCuentaDebitar = "02";
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
			
			String mes = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(292, 296)));
			String dia = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(288, 292)));
			String hora = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(52, 64)));
			String cuentaCreditar = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)));
			
			Logger.logLine("msg in TransferAux:\n" + in.getTotalHexString(), enableMonitor);
			String p125 =  Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1658, 1662)))
					.concat("                              ")
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1662, 1684))));
			

			
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, "50".concat(tipoCuentaDebitar).concat(tipoCuentaCreditar));
			
//			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, mes.concat(dia).concat(hora));
			
			out.putField(Iso8583.Bit._013_DATE_LOCAL, mes.concat(dia));
			
			//TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");
			
			//CAMPO 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "0901".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(356,364))))
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(406, 414)))));
			
			//TRACK2 Field 43
			out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, p43);
			
			//CAMPO 103 CREDIT ACCOUNT
			out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, tipoPago.concat("11")
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(138, 146)))).concat("0")
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)))));
			
			//127.2 SWITCHKEY
			out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(mes).concat(dia).concat(hora).concat("0"+cons.substring(2, 5)));

			//127.22 TAG B24_Field_17
			sd.put("B24_Field_17", mes.concat(dia));
			//127.22 TAG B24_Field_35
			sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaCreditar, 18, '0', false)).concat("=            "));	
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
	

}
