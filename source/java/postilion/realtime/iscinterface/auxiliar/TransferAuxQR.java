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
	public static final String IDENTIFICACION_TRANSACCION_DEVOLUCION = "1";
	public static final String IDENTIFICACION_TRANSACCION_ORIGINAL = "0";
	public Iso8583Post processMsg (Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor, boolean isNextDay) throws XPostilion {
		
		
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
			String andendaRef2 = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(918), lth( 918 + 24) )));
			
			Logger.logLine("msg in TransferAux:\n" + in.getTotalHexString(), enableMonitor);

			String idPagQR125 = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(943), lth( 943 + 1) )));
			String p125 = "";
			if(idPagQR125.equals(IDENTIFICACION_TRANSACCION_DEVOLUCION)) {
				p125 =Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(894), lth( 894 + 24) ))) // nombre del comercio
						.concat(Pack.resize(andendaRef2.substring(0,12),24,'0',true))// referencia 2, Adenda ref 2
						.concat(Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(945), lth( 945 + 4) )))
								.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(949), lth( 949 + 6) ))))
								.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(955), lth( 955 + 4) ))))
								, 25, '0', true)) // Adenda ref 3 Falta validar Devolucion
						.concat(Pack.resize("",  8, '0', true)) // Id registro
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(71), lth( 71 + 1) ))))//Tipo de IDentificaci�n Origen
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(51), lth( 51 + 16) ))))//Numero de Identificaci�n Origen
						.concat(Pack.resize(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(110),lth( 110 + 14) ))),17,'0',false)) // valor pago
						.concat(Pack.resize("",  2, '0', true)) // tipo de identificacion destino
						.concat(Pack.resize("",  16, '0', true)) // Numero de identificacion destino
						.concat(Pack.resize("",  1, '0', true)) // ****** validacion titularidad
						.concat(Pack.resize("",  4, '0', true))	//Terminal Rellenar con ceros
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(942), lth( 942 + 1) )))) // flag transf 1
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(943), lth( 943 + 1) )))) // flag transf 2
						.concat(Pack.resize("",  10, '0', true));
				
				//CAMPO 37 Retrieval Reference Number
				out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, "0901"
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(30, 38))))
						//.concat(secuencialesAlearios(4)));
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(ISCReqInMsg.POS_ini_SEQUENCE_NR, ISCReqInMsg.POS_end_SEQUENCE_NR)))));

			
			}else {
				p125 = Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(894), lth( 894 + 24) ))) // nombre del comercio
						.concat(Pack.resize("",  114, '0', true))
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(942), lth( 942 + 1) )))) // flag transf 1
						.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(943), lth( 943 + 1) )))) // flag transf 2
						.concat(Pack.resize("",  10, '0', true));
				
				//CAMPO 37 Retrieval Reference Number
				out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, andendaRef2.substring(0,12));

 			}
			//CAMPO 3 
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, "40".concat(tipoCuentaDebitar).concat(tipoCuentaCreditar));
			
			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));
			
        	//CAMPO 12 TRANSMISSION time
			out.putField(Iso8583.Bit._012_TIME_LOCAL, new DateTime().get("HHmmss"));
			
			//CAMPO 13
			out.putField(Iso8583.Bit._013_DATE_LOCAL, new DateTime().get("MMdd"));
			
			//CAMPO 15
			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);
			
			//TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=2912000");
			
		
			//CAMPO 102 DEBIT ACCOUNT
			out.putField(Iso8583.Bit._102_ACCOUNT_ID_1, "00010".concat(cuentaDebitar));
			
			//CAMPO 103 CREDIT ACCOUNT
			out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "011"
					.concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(138, 146)))).concat("0")
					.concat(cuentaAcreditar));
			
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
			
			if(in.getTotalHexString().substring(46,52).matches("^((F0F4F0)|(F0F5F0)|(F0F6F0)|(F0F7F0))"))
				sd.put("NEXTDAY", "TRUE");
			
			////////// TAGS EXTRACT 
			
			sd.put("VIEW_ROUTER", "V2");
			
			sd.put("TRANSACTION_INPUT", "TRANSFERENCIA_QR");
			sd.put("TRANSACTION_CNB_TYPE", "QR_TRANSFERENCIAS");
			
			sd.put("Codigo_FI_Origen", "1019");
			sd.put("Nombre_FI_Origen", "CIC");		
			sd.put("Canal", "01");
			
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
			sd.put("Dispositivo", "_");
			sd.put("Canal", "01");
			sd.put("service_restriction_code", "000");
			sd.put("pos_entry_mode", "000");
			sd.put("Entidad", "0000");
			sd.put("Identificador_Terminal", "0");
			sd.put("Numero_Cedula", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1662, 1684))));
			sd.put("SECUENCIA", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(242, 282))));
			sd.put("Nombre_Establecimiento_QR", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(lth(894), lth( 894 + 20) ))));
			sd.put("IN_MSG", in.getTotalHexString());
			
			
			switch (idPagQR125) {
			case IDENTIFICACION_TRANSACCION_ORIGINAL:
				sd.put("Transaccion_Unica", "Q001");
				break;
			case IDENTIFICACION_TRANSACCION_DEVOLUCION:
				sd.put("Transaccion_Unica", "Q004");
				break;

			default:
				break;
			}
			
			
			//sd.put("Numero_Factura", Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(1684, 1732))));
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
	
	public static final String secuencialesAlearios(int iCuantos)
	{
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i< iCuantos; i++ ) {
			sb.append( "" + (int)( Math.random() * 10 ) );
		}
		return sb.toString();
	}


}
