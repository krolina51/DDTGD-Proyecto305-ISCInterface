package postilion.realtime.iscinterface.auxiliar;


import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
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

public class PaymentTCAux {
	
	
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor) throws XPostilion {
		
		
		try {
			
			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);
			
			StructuredData sd = null;
			
			if(out.getStructuredData() != null) {
				sd = out.getStructuredData();	
			} else {
				sd = new StructuredData();
			}
			String tranType = null;
			String fromAccount = null;
			String toAccount = null;
			String mes = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(292, 296)));
			String dia = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(288, 292)));
			String hora = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(52, 64)));
			
			
			//VERIFICANDO NATURALEZA DE LA TX
			switch (Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_TRAN_NATURE, ISCReqInMsg.POS_end_TRAN_NATURE)))) {
			case "1":
				toAccount = "00";
				break;
			case "2":			
				toAccount = "40";
				break;
			case "3":
				toAccount = "41";
				break;
			case "4":			
				toAccount = "42";
				break;
			case "5":			
				toAccount = "30";
				break;
			default:
				break;
			}
			
			String entidadAutorizadora = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(138, 146)));
			String p41 = entidadAutorizadora
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(356, 364)))).concat("00003   ");
			String bin = "008801";
			String p43 = "BOG       INTERNET BTA             BOCCO";
			
			String p125 = "                                                                                          ";
			
			if(entidadAutorizadora.equals("0001")) {
				tranType = "20";
				fromAccount = "00";
				
				//TRACK2 Field 22
				out.putField(Iso8583.Bit._022_POS_ENTRY_MODE, "010");
				
				//TRACK2 Field 35
				out.putField(Iso8583.Bit._035_TRACK_2_DATA, Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)))
						.concat("=99122010000000000"));
				
				//TRACK2 Field 43
				out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(356, 364)))
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(432, 468))))
						.concat(Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(422, 432))), 18, ' ', true)));
				
				p125 = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1658, 1662)))
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1662, 1680)))).concat("0");
				
				//127.22 TAG B24_Field_18
				sd.put("B24_Field_18", "6010");
				//127.22 TAG B24_Field_48
				sd.put("B24_Field_48", "1                  00000000");
				//127.22 TAG B24_Field_60
				sd.put("B24_Field_60", "P801P80100000000");
				//127.22 TAG B24_Field_61
				sd.put("B24_Field_61", "0001000000000000000");
				//127.22 TAG B24_Field_63
				sd.put("B24_Field_63", "& 0000600278! 0400020  00000000000Y     Y0! BM00036     500130      00000000000000000000! C000026                   2  0    ! C400012 1 25100  600! Q400122 23000000000000000000000000000000000000000000000000000000000000000000000000000000000000017000041500000000000000000000000000");
				//127.22 TAG B24_Field_100
				sd.put("B24_Field_100", "10000000001");
				//127.22 TAG B24_Field_121
				sd.put("B24_Field_121", "00000000000000000000");
				//127.22 TAG B24_Field_124
				sd.put("B24_Field_124", "000000000");
				//127.22 TAG B24_Field_126
				sd.put("B24_Field_126", "00000000000000000000000000000000000000");
				
			}else {
				tranType = "50";
				String cuentaDebitar = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_DEBIT_ACC_NR, ISCReqInMsg.POS_end_DEBIT_ACC_NR)));
				String tipoCuentaDebitar =  "";
				if (Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(180, 182))).equals("0")) {
					tipoCuentaDebitar = "10";
				}else if(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(180, 182))).equals("1")) {
					tipoCuentaDebitar = "20";
				}
				fromAccount = tipoCuentaDebitar;
				
				//TRACK2 Field 35
				out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");
				
				p41 = entidadAutorizadora
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(356, 364)))).concat("00002   ");
				
				//TRACK2 Field 43
				out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, p43);
				//TRACK2 Field 98
				out.putField(Iso8583.Bit._098_PAYEE, "0054150070650000000000000");
				
				//CAMPO 102 DEBIT ACCOUNT
				out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, "00010".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_DEBIT_ACC_NR, ISCReqInMsg.POS_end_DEBIT_ACC_NR)))));
				
				//CAMPO 103 CREDIT ACCOUNT
				out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "011"
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(138, 146)))).concat("0")
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_CREDIT_ACC_NR, ISCReqInMsg.POS_end_CREDIT_ACC_NR)))));
				
				//127.22 TAG B24_Field_35
				sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaDebitar, 18, '0', false)).concat("=991200000001"));	
				//127.22 TAG B24_Field_52
				sd.put("B24_Field_52", "FFFFFFFFFFFFFFFF");
				//127.22 TAG B24_Field_60
				sd.put("B24_Field_60", "0901BBOG+000");
				//127.22 TAG B24_Field_104
				sd.put("B24_Field_104", "000000000000000000");
				 
			}

			//Field 3
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, tranType.concat(fromAccount).concat(toAccount));
			
//			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, mes.concat(dia).concat(hora));
			
			//Field 13
			out.putField(Iso8583.Bit._013_DATE_LOCAL, mes.concat(dia));
			
			//CAMPO 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "0901".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(356, 364))))
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(406,414)))));
			
			//127.2 SWITCHKEY
			out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0200".concat(mes).concat(dia).concat(hora).concat("0"+cons.substring(2, 5)));

			//127.22 TAG B24_Field_17
			sd.put("B24_Field_17", mes.concat(dia));
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
