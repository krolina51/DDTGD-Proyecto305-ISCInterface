package postilion.realtime.iscinterface.auxiliar;

import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.ISCInterfaceCB;
import postilion.realtime.iscinterface.database.DBHandler;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.util.Client;
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

public class CashAdvanceTCAux {
	public static final int TERMINAL_INI = 14;
	public static final int TERMINAL_FIN = 14 + 4;
	public static final int COD_OFICINA_INI = 18;
	public static final int COD_OFICINA_FIN = 18 + 4;
	public static final int NUM_SEQ_TX_INI = 22;
	public static final int NUM_SEQ_TX_FIN = 22 + 4;
	public static final int BYTES_ESTADO_INI = 26;
	public static final int BYTES_ESTADO_FIN = 26 + 3;
	public static final int HORA_INI = 29;
	public static final int HORA_FIN = 29 + 6;
	public static final int NUMERO_IDENTIFICACION_INI = 51;
	public static final int NUMERO_IDENTIFICACION_FIN = 51 + 16;
	public static final int NUMERO_TARJETA_INI = 71;
	public static final int NUMERO_TARJETA_FIN = 71 + 16;
	public static final int TIPO_CUENTA_INI = 87;
	public static final int TIPO_CUENTA_FIN = 87 + 1;
	public static final int NUMERO_CUENTA_INI = 88;
	public static final int NUMERO_CUENTA_FIN = 88 + 16;
	public static final int VALOR_TOTAL_INI = 104;
	public static final int VALOR_TOTAL_FIN = 104 + 14;
	public static final int CUS_INI = 118; //Codigo Seguimiento PSE
	public static final int CUS_FIN = 118 + 10;	
	public static final int FLAG_TRANSACCION_INI = 128; //0=Avance, 1=Pago Impuesto DIAN, 2=Pago Imp distrital(compra) 
	public static final int FLAG_TRANSACCION_FIN = 128 + 1;
	public static final int MCC_INI = 129; 
	public static final int MCC_FIN = 129 + 4; 
	public static final int CODIGO_COMERCIO_INI = 133;
	public static final int CODIGO_COMERCIO_FIN = 133 + 16;
	public static final int NUMERO_CUOTAS_INI = 149;
	public static final int NUMERO_CUOTAS_FIN = 149 + 2;
	
	
	public static final String INITIAL_SPACE = "   ";
	

	public Iso8583Post processMsg(Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons,boolean enableMonitor, boolean isNextDay) throws XPostilion {
		
		String tramaCompletaAscii = INITIAL_SPACE + Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString()));

		try {
			
			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;

			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);

			StructuredData sd = null;
			StructuredData sdOriginal = new StructuredData();

			if (out.getStructuredData() != null) {
				sd = out.getStructuredData();
			} else {
				sd = new StructuredData();
			}
		
			if(Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._10_H_NEXTDAY_IND)).equals("1")) {
				businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}
					
			String p12 = new DateTime().get("HHmmss");
			String p13 = new DateTime().get("MMdd");
			String codigoOficina = tramaCompletaAscii.substring(COD_OFICINA_INI,COD_OFICINA_FIN);
			String p37 = "0901".concat(codigoOficina).concat(tramaCompletaAscii.substring(NUM_SEQ_TX_INI,NUM_SEQ_TX_FIN));
			String key = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
			String keyReverse = null;
			String terminal = tramaCompletaAscii.substring(TERMINAL_INI, TERMINAL_FIN);
			String numeroTarjeta = tramaCompletaAscii.substring(NUMERO_TARJETA_INI, NUMERO_TARJETA_FIN);
			String valorTotal = tramaCompletaAscii.substring(VALOR_TOTAL_INI, VALOR_TOTAL_FIN);
			String flag_transaccion = tramaCompletaAscii.substring(FLAG_TRANSACCION_INI, FLAG_TRANSACCION_FIN);
			
			
			String identificacionCanal = "  ";
			String FI_Tarjeta = "";
			
			String binTarjeta = "000000000000000000";
			String p125 = "                                                                                          ";
			
			// NATURALEZA DE LA TRANSACCION ES TARJETA DE CREDITO

			String tranType = "01";
			String fromAccount = "30";
			String toAccount = "00";

			String mcc = "    ";
			String codigoComercio = "                ";
			String numeroCuotas = "  ";
			if (flag_transaccion.equals("2") || flag_transaccion.equals("3"))// Es Pago impuesto distrital o Compra BNPL
			{
				mcc = tramaCompletaAscii.substring(MCC_INI, MCC_FIN);
				codigoComercio = tramaCompletaAscii.substring(CODIGO_COMERCIO_INI, CODIGO_COMERCIO_FIN);
				numeroCuotas = tramaCompletaAscii.substring(NUMERO_CUOTAS_INI, NUMERO_CUOTAS_FIN);
				
				tranType = "00";
				fromAccount = "00";
				toAccount = "30";
			}
			
			String p41 = "";
			String p43 = "BOG                                BOCCO";
			String token_BM ="";
			String token_Q4 = "";
			String tx_subtype ="";
			
			switch (codigoOficina.substring(0, 1)) {
			
				case "8":		
					identificacionCanal = "IT";
					tx_subtype = "Q103013000";
					if(codigoOficina.equals("8332")){	// canal PPA
						p41 = "0001833200002   ";
					}
					else if(codigoOficina.equals("8300") ||  // Canal Corporate 
						   codigoOficina.equals("8600")){	 // canal ICBS
							identificacionCanal = "85";
						}
						else {
							p41 = "1019800000002   ";	// canal internet
							binTarjeta = "008801000000000000";
							}
					
					break;
				case "9":	// canal IVR
					p41 = "1019900000005   ";
					p43 = "BOG                                BO CO";
					tx_subtype = "Q101013000";
					identificacionCanal = "IV";
					binTarjeta = "009901000000000000";
					break;
				default: 
					p41 = "0001829700003   ";
					tx_subtype = "Q103013000";
					break;
				}
			
				//Token BM
				if (flag_transaccion.equals("2")) //Es Pago impuesto distrital
				token_BM = "! BM00036 Q".concat("201").concat(tx_subtype.substring(4,10)).concat("      00000000000000000000");
				else
				token_BM = "! BM00036 ".concat(tx_subtype).concat("      00000000000000000000");
				
				
				//Token 04
				String token_04 = "! 0400020  00000000000Y     Y0";				
						
				//Token C0
				String token_C0 = "! C000026                   2";
				String franquicia = " ";
				if(numeroTarjeta.substring(0,1).equals("5")){
					franquicia = "M";
				}
				String fld_present = "0";
				token_C0 = token_C0.concat(franquicia).concat(" ").concat(fld_present).concat("    ");
				
				//Token C4
				String crdhldr_present_ind = "0";
				String crd_present_ind = "0";
				if(codigoOficina.substring(0,1).equals("4") || //Oficina
				   codigoOficina.substring(0,1).equals("6") || //Aval Net
				   codigoOficina.substring(0,1).equals("8") || //Internet
				   codigoOficina.substring(0,1).equals("9")){  //IVR
					crdhldr_present_ind = "5";
					crd_present_ind = "1";
				}
				String token_C4 = "! C400012 1 2".concat(crdhldr_present_ind).concat(crd_present_ind).concat("00  600");
				
				//Token Q4
				String moneda = "170"; //Moneda Pesos
				
				if (flag_transaccion.equals("3")) //Es Compra BNPL
					token_Q4 = "! Q400122 ".concat(numeroCuotas).concat("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000").concat(moneda).concat("00").concat(settlementDate).concat("00000000000000000000000000");
				else  //Avance, Pago impuestos
					token_Q4 = "! Q400122 ".concat("000000000000000000000000000000000000000000000000000000000000000000000000000000000000000").concat(moneda).concat("00").concat(settlementDate).concat("00000000000000000000000000");
					
				//Logica Tokens First Data Fin -----------------------------------------
	
				// 127.22 TAG B24_Field_18 / 127.22 TAG B24_Field_42
				switch(flag_transaccion)
				{
					case "1": //Es Pago impuesto DIAN
						sd.put("B24_Field_18", "9311");
					break;
					case "2": //Es Pago impuesto distrital
						sd.put("B24_Field_18", "6015");
					break;
					case "3": //Es Compra BNPL
						sd.put("B24_Field_18", mcc);
						sd.put("B24_Field_42", codigoComercio.substring(1,16));
					break;
					default: //Es avance
						sd.put("B24_Field_18", "6010");
						sd.put("B24_Field_42", "               ");
					break;
				}
				
				// 127.22 TAG B24_Field_35
				sd.put("B24_Field_35", numeroTarjeta.concat("=99120000000000000000"));
						
				// 127.22 TAG B24_Field_48
				sd.put("B24_Field_48", "1                  00000000");
				// 127.22 TAG B24_Field_60
				sd.put("B24_Field_60", "P801P80100000000");
				// 127.22 TAG B24_Field_61
				sd.put("B24_Field_61", "0001000000000000000");
				// 127.22 TAG B24_Field_63
				//sd.put("B24_Field_63",
				sd.put("B24_Field_63",
					"& 0000600278".concat(token_04).concat(token_BM).concat(token_C0).concat(token_C4).concat(token_Q4));  
				// 127.22 TAG B24_Field_100
				sd.put("B24_Field_100", "10000000001");
				// 127.22 TAG B24_Field_121
				sd.put("B24_Field_121", "00000000000000000000");
				// 127.22 TAG B24_Field_124
				sd.put("B24_Field_124", "000000000");
				// 127.22 TAG B24_Field_125
				sd.put("B24_Field_125", "000000000000");
				// 127.22 TAG B24_Field_126
				sd.put("B24_Field_126", "00000000000000000000000000000000000000");
				// 127.22 TAG FI_Tarjeta
				sd.put("FI_Tarjeta", FI_Tarjeta);
			   				
			//FIN LOGICA FIRSDATA	

			// 127.2 SWITCHKEY    VALIDAR CON MENESES SI ESTE VALOR VA EN ISO8583
			out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);

			// Field 3
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, tranType.concat(fromAccount).concat(toAccount));

			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));

			// CAMPO 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, p37);

			// CAMPO 12 TIME LOCAL
			out.putField(Iso8583.Bit._012_TIME_LOCAL, p12);

			// CAMPO 13 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._013_DATE_LOCAL, p13);

			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);

			// TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");		
			
			// TRACK2 Field 43
			out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, p43);
			
			// 127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			// 127.22 TAG B24_Field_41
			sd.put("B24_Field_41", p41);
			        
			//127.22 TAG MSG_HEXA
			sd.put("IN_MSG", in.getTotalHexString());
			
			////////// TAGS EXTRACT 
			sd.put("VIEW_ROUTER", null); //No genera registro extract	
			
			
			sd.put("forzar_delay", "true"); // INDICADOR PARA FORZAR UN DELAY. BORRAR ESTA LÍNEA DESPUÉS DE HACER PRUEBA INTERNA.
							
			out.putStructuredData(sd);

		} catch (Exception e) {
			e.toString();
			EventRecorder.recordEvent(new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n", Utils.getStringMessageException(e) }));
		}

		return out;
	}
}
