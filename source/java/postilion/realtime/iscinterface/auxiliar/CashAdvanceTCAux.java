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
	//BNPL public static final int MCC_INI = 129; 
	//BNPL public static final int MCC_FIN = 129 + 4; 
	//BNPL public static final int CODIGO_COMERCIO_INI = 133;
	//BNPL public static final int CODIGO_COMERCIO_FIN = 133 + 16;
	//BNPL public static final int NUMERO_CUOTAS_INI = 149;
	//BNPL public static final int NUMERO_CUOTAS_FIN = 149 + 2;
	
	
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
			//BNPL String mcc = tramaCompletaAscii.substring(MCC_INI, MCC_FIN);
			//BNPLString codigoComercio = tramaCompletaAscii.substring(CODIGO_COMERCIO_INI, CODIGO_COMERCIO_FIN);
			//BNPLString numeroCuotas = tramaCompletaAscii.substring(NUMERO_CUOTAS_INI, NUMERO_CUOTAS_FIN);
			
			String identificacionCanal = "  ";
			String FI_Tarjeta = "";
			
			String binTarjeta = "000000000000000000";
			String p125 = "                                                                                          ";
			
			// NATURALEZA DE LA TRANSACCION ES TARJETA DE CREDITO

			String tranType = "01";
			String fromAccount = "30";
			String toAccount = "00";

			if (flag_transaccion.equals("2") || flag_transaccion.equals("3"))// Es Pago impuesto distrital o Compra BNPL
			{
				tranType = "00";
				fromAccount = "00";
				toAccount = "30";
			}
			
			String p41 = "";
			String p43 = "BOG                                BOCCO";
			String token_BM ="";
			String token_Q4 = "";
			String tx_subtype ="";
			String codigoOfiAdqui = "";
			String nombreOficinaAdqui = "";
			String codigoDaneOfiAdqui = "";
					
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
				
				//BNPL if (flag_transaccion.equals("3")) //Es Compra BNPL
				//BNPL	token_Q4 = "! Q400122 ".concat(numeroCuotas).concat("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000").concat(moneda).concat("00").concat(settlementDate).concat("00000000000000000000000000");
				//BNPL else  //Avance, Pago impuestos
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
					//BNPL case "3": //Es Compra BNPL
						//BNPL sd.put("B24_Field_18", mcc);
						//BNPL sd.put("B24_Field_42", codigoComercio.substring(1,16));
					//BNPL break;
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


			// PROCESAMIENTO DE REVERSO
			if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("080") //Reverso
					|| Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) { //Anulacion

				String key420 = "0420".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
				String keyAnulacion = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
				
				Logger.logLine("sdOriginal:\n" + sdOriginal, enableMonitor);
				keyReverse = sdOriginal.get("KeyOriginalTx");
				if(keyReverse == null) {
					keyReverse = "0000000000";
					sd.put("REV_DECLINED", "TRUE");
				} else {
					out.putField(Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS, Pack.resize(keyReverse, 42, '0', true));
					
					out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key420);
					//out.putPrivField(Iso8583Post.PrivBit._011_ORIGINAL_KEY, keyReverse);
					sd.put("B24_Field_95", "000000000000000000000000000000000000000000");
					sd.put("KEY_REVERSE", keyReverse);
					sd.put("B24_Field_90", keyReverse+"0000000000");
					//sd.put("B24_Field_37", keyReverse.substring(4,16));
					
					if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) {
						tranType = "20";
						sd.put("ANULACION", "TRUE");
						sd.put("B24_Field_15", settlementDate);
						sd.put("B24_Field_38", sdOriginal.get("Autorizacion_Original"));
						sd.put("KeyOriginalTx", keyReverse);
						sd.put("B24_Field_54", "000".concat(sdOriginal.get("Monto_Original"))
								.concat("000000000000000000")
								.concat(sdOriginal.get("Monto_Original")));
						out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, keyAnulacion);
					}
				}


			// PROCESAMIENTO TX FINANCIERA
			} else {
				//Se comenta porque no se usa en la TX Avance Tarjeta Credito BDB- Canales virtuales
				/*
				out.putField(Iso8583Post.Bit._059_ECHO_DATA, Transform
						.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(406, 414))));*/
				// 127.2 SWITCHKEY
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);
				//ISCInterfaceCB.cacheKeyReverseMap.put(seqNr, key);
			}


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
			
			sd.put("Codigo_FI_Origen", "1019");
			sd.put("Nombre_FI_Origen", "CIC");		
			
			sd.put("Ind_4xmil", "1");
			sd.put("Tarjeta_Amparada", numeroTarjeta);
			
			sd.put("Codigo_Transaccion_Producto", "02");
			sd.put("Tipo_de_Cuenta_Debitada", "CRE");
			sd.put("BIN_Cuenta", numeroTarjeta.substring(0,6));
			sd.put("PRIM_ACCOUNT_NR", Pack.resize(numeroTarjeta.substring(6,16), 18, '0', false));
			sd.put("SEC_ACCOUNT_NR_PAGOTC",Pack.resize(numeroTarjeta, 18, '0', false));
			sd.put("PAN_Tarjeta", Pack.resize(numeroTarjeta, 19, ' ', true));	
			
			sd.put("Codigo_Transaccion", "20");
			sd.put("Nombre_Transaccion", "AVANCE");
			sd.put("FI_CREDITO", "0001");
			sd.put("FI_DEBITO", "0001");
			
			sd.put("CLIENT_CARD_NR_1", binTarjeta);
			sd.put("Codigo_de_Red", "1019");
			
			sd.put("Numero_Terminal",codigoOficina);
			sd.put("Nombre_Establecimiento_QR", "                    ");
			
			sd.put("Identificacion_Canal", identificacionCanal);
			sd.put("Codigo_Establecimiento", "          ");
			
			sd.put("Indicador_AVAL", "1");	
			sd.put("SECUENCIA_REQ", "00000000000000000000");
			
			sd.put("Mod_Credito", "0");
			sd.put("Clase_Pago", "0");
			sd.put("Ent_Adq", "0001");
			sd.put("Dispositivo", "0");
			sd.put("Canal", "01");
			sd.put("pos_entry_mode", "000");
			sd.put("service_restriction_code", "000");
			sd.put("Entidad", "0000");
			sd.put("Identificador_Terminal", "0");
			sd.put("Indicador_Efectivo_Cheque", "0");
							
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
