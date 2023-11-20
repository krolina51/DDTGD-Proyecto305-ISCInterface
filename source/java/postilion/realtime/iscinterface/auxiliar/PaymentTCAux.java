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

public class PaymentTCAux {
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
	public static final int TIPO_IDENTIFICACION_INI = 71;
	public static final int TIPO_IDENTIFICACION_FIN = 71 + 1;
	public static final int CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_INI = 72;
	public static final int CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_FIN = 72 + 4;
	public static final int TIPO_CUENTA_ACREDITAR_INI = 76;
	public static final int TIPO_CUENTA_ACREDITAR_FIN = 76 + 1;
	public static final int NUMERO_CUENTA_ACREDITAR_INI = 77;
	public static final int NUMERO_CUENTA_ACREDITAR_FIN = 77 + 16;
	public static final int TIPO_CUENTA_DEBITADA_INI = 93;
	public static final int TIPO_CUENTA_DEBITADA_FIN = 93 + 1;
	public static final int NUMERO_CUENTA_DEBITADA_INI = 94;
	public static final int NUMERO_CUENTA_DEBITADA_FIN = 94 + 16;
	public static final int VALOR_TRANSFERENCIA_INI = 110;
	public static final int VALOR_TRANSFERENCIA_FIN = 110 + 14;
	public static final int SECUENCIA_TS_INI = 124;
	public static final int SECUENCIA_TS_FIN = 124 + 20;
	public static final int MODALIDAD_PAGO_INI = 144;
	public static final int MODALIDAD_PAGO_FIN = 144 + 1;
	public static final int MODALIDAD_PAGO_CREDITO_INI = 145;
	public static final int MODALIDAD_PAGO_CREDITO_FIN = 145 + 1;
	public static final int CLASE_PAGO_INI = 146;
	public static final int CLASE_PAGO_FIN = 146 + 1;
	public static final int DIA_FECHA_APLICACION_DEL_PAGO_INI = 147;
	public static final int DIA_FECHA_APLICACION_DEL_PAGO_FIN = 147 + 2;
	public static final int MES_FECHA_APLICACION_DEL_PAGO_INI = 149;
	public static final int MES_FECHA_APLICACION_DEL_PAGO_FIN = 149 + 2;
	public static final int NUM_SEQ_TX_ACTUAL_INI = 206;
	public static final int NUM_SEQ_TX_ACTUAL_FIN = 206 + 4;
	public static final int NUM_SEQ_TX_ORIG_A_REVERSAR_INI = 210;
	public static final int NUM_SEQ_TX_ORIG_A_REVERSAR_FIN = 210 + 4;
	public static final int IDENTIFICADOR_PINPAD_INI = 175;
	public static final int IDENTIFICADOR_PINPAD_FIN = 175 + 2;
	public static final int COD_OFICINA_ADQUIRIENTE_INI = 181;
	public static final int COD_OFICINA_ADQUIRIENTE_FIN = 181 + 4;
	
	public static final String INITIAL_SPACE = "   ";
	

	public Iso8583Post processMsg(Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons,boolean enableMonitor) throws XPostilion {
		
		String tramaCompletaAscii = INITIAL_SPACE + Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString()));

		try {
			
			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;

			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);

			StructuredData sd = null;

			if (out.getStructuredData() != null) {
				sd = out.getStructuredData();
			} else {
				sd = new StructuredData();
			}
		
			if (tramaCompletaAscii.substring(BYTES_ESTADO_INI,BYTES_ESTADO_FIN).matches("^((F0F4F0)|(F0F5F0)|(F0F6F0)|(F0F7F0))")) {
				businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			} else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}
			String p43 = "";
			String tranType = null;
			String fromAccount = null;
			String toAccount = null;
			String p12 = new DateTime().get("HHmmss");
			String p13 = new DateTime().get("MMdd");
			String codigoOficina = tramaCompletaAscii.substring(COD_OFICINA_INI,COD_OFICINA_FIN);
			String p37 = "0901".concat(codigoOficina).concat(tramaCompletaAscii.substring(NUM_SEQ_TX_INI,NUM_SEQ_TX_FIN));
			String key = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);			
			String seqNr = tramaCompletaAscii.substring(NUM_SEQ_TX_ACTUAL_INI, NUM_SEQ_TX_ACTUAL_FIN);
			String seqNrReverse = tramaCompletaAscii.substring(NUM_SEQ_TX_ORIG_A_REVERSAR_INI, NUM_SEQ_TX_ORIG_A_REVERSAR_FIN);
			String keyReverse = null;
			String terminal = tramaCompletaAscii.substring(TERMINAL_INI, TERMINAL_FIN);
			String cuentaCreditar = tramaCompletaAscii.substring(NUMERO_CUENTA_ACREDITAR_INI, NUMERO_CUENTA_ACREDITAR_FIN);
			String cuentaDebitar = tramaCompletaAscii.substring(NUMERO_CUENTA_DEBITADA_INI, NUMERO_CUENTA_DEBITADA_FIN);
			String identificadorPinpad = tramaCompletaAscii.substring(IDENTIFICADOR_PINPAD_INI, IDENTIFICADOR_PINPAD_FIN);  
			String identificacionCanal = "";
			String modalidadPagoCredito = tramaCompletaAscii.substring(MODALIDAD_PAGO_CREDITO_INI,MODALIDAD_PAGO_CREDITO_FIN);
			String clasePagoCredito = tramaCompletaAscii.substring(CLASE_PAGO_INI,CLASE_PAGO_FIN);
			String secuenciaTS = tramaCompletaAscii.substring(SECUENCIA_TS_INI,SECUENCIA_TS_FIN);
			String entidadAutorizadora = tramaCompletaAscii.substring(CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_INI, CODIGO_ENTIDAD_QUE_AUTORIZA_EL_CREDITO_FIN);
			String p41 = entidadAutorizadora.concat(tramaCompletaAscii.substring(COD_OFICINA_ADQUIRIENTE_INI, COD_OFICINA_ADQUIRIENTE_FIN)).concat("00003   ");
			String binTarjeta = "000000000000000000";
			String p100 = "";
			String p125 = "                                                                                          ";
			// NATURALEZA DE LA TRANSACCION ES TARJETA DE CREDITO
			toAccount = "30";
			
			//Armamos Case para determinar Nombre de Terminal segun Origen de la transaccion (Primer digito de codigo de Oficina)
            String p43_pos_6_a_22 = "     OFICINA BBOG";	
			switch (codigoOficina.substring(0,1)) {
			case "3":
				p43_pos_6_a_22 = "  FIDUCOMERCIO   ";
				break;
			case "5":
				p43_pos_6_a_22 = "     INTN BBS BTA";
				break;
			case "6":
				p43_pos_6_a_22 = "    BCAMOVIL  BB ";
				break;
			case "7":
				p43_pos_6_a_22 = "  890AGILIZADORBB";
				break;
			case "8":
				p43_pos_6_a_22 = "     INTERNET BTA";
				break;
			case "9":
				p43_pos_6_a_22 = "     IVR BOGOTA  ";
				break;
			default:
				p43_pos_6_a_22 = "     OFICINA BBOG";
				break;
			}
			
			p43 = "BOG  ".concat(p43_pos_6_a_22).concat("             BOCCO");
			
			String p22 = "010";
			if (codigoOficina.substring(0, 1).equals("4")) {
				p22 = "051";
			}
			
			// Inicio Logica para tarjetas BBOG (FirstData)
			if (entidadAutorizadora.equals("0001")) {	
				// Armado P41 para Firsdata
				p100 = "100";
				switch (codigoOficina.substring(0, 1)) {
					case "2":
						p41 = codigoOficina.substring(1, 2).equals("5") && p22.equals("051") 
						?  "0001000000008   "	// canal redes externas
						:  "0001930000002   ";	// canal CNB Moviles
					break;
				case "4":	// canal Oficinas
					p41 = "00010".concat(codigoOficina.substring(1, 4)).concat("00003   ");
					identificacionCanal = "OF";
					binTarjeta = "007701000000000000";
					break;
				case "5":	// canal BBS
					p41 = "0001820500002   ";
					identificacionCanal = "BS";
					break;
				case "6":	// canal WAP
					p41 = "0001829800002   ";
					break;
				case "7":	// canal Agilizador
					p41 = "0001829900002   ";
					break;
				case "8":
					if(codigoOficina.equals("8332")){	// canal PPA
						p41 = "0001833200002   ";
					}
					else {
						p41 = "0001820100002   ";	// canal internet
						binTarjeta = "008801000000000000";
					}
					identificacionCanal = "IT";
					break;
				case "9":	// canal IVR
					p41 = "0001920100005   ";
					identificacionCanal = "IV";
					binTarjeta = "009901000000000000";
					break;
				default: 
					p41 = "0001829700003   ";
					break;
				}
				p43 = "BOG  ".concat(p43_pos_6_a_22).concat("                  ");

				tranType = "20";
				fromAccount = "00";
				//Logica Tokens First Data
				//Token 04
				String token_04 = "!0400020  00000000000Y     Y0";
				//Token BM
				String token_BM = "! BM00036 Q401501030      00000000000000000000";
				//Token C0
				String token_C0 = "! C000026                   2";
				String franquicia = " ";
				if(cuentaCreditar.substring(0,1).equals("5")){
					franquicia = "M";
				}
				String fld_present = "0";
				if(identificadorPinpad.equals("PP")){ //Transacción presente
					fld_present = "1"; 
				}
				
				token_C0 = token_C0.concat(franquicia).concat(fld_present).concat("    ");
				//Token C4
				String token_C4 = "1 2";
				String crdhldr_present_ind = "0";
				String crd_present_ind = "0";
				if(codigoOficina.substring(0,1).equals("4") || //Oficina
				   codigoOficina.substring(0,1).equals("6") || //Aval Net
				   codigoOficina.substring(0,1).equals("8") || //Internet
				   codigoOficina.substring(0,1).equals("9")){  //IVR
					crdhldr_present_ind = "5";
					crd_present_ind = "1";
				}
				token_C4 = token_C4.concat(crdhldr_present_ind).concat(crd_present_ind).concat("00  600");
				//Token Q4
				String token_Q4 = "";

				// 127.22 TAG B24_Field_18
				sd.put("B24_Field_18", "6010");
				// 127.22 TAG B24_Field_35
				sd.put("B24_Field_35", cuentaCreditar.concat("=99122010000000000"));
				// 127.22 TAG B24_Field_48
				sd.put("B24_Field_48", "1                  00000000");
				// 127.22 TAG B24_Field_60
				sd.put("B24_Field_60", "P801P80100000000");
				// 127.22 TAG B24_Field_61
				sd.put("B24_Field_61", "0001000000000000000");
				// 127.22 TAG B24_Field_63
				sd.put("B24_Field_63",
						"& 0000600278! 0400020  00000000000Y     Y0! BM00036     500130      00000000000000000000! C000026                   2  0    ! C400012 1 25100  600! Q400122 23000000000000000000000000000000000000000000000000000000000000000000000000000000000000017000041500000000000000000000000000");
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

			} else {	// Lógica para tarjetas AVAL (ATH)
				String bin = "008801";
				p100 = "70";
				switch (codigoOficina.substring(0, 1)) {
					case "2":						
						p41 = codigoOficina.substring(1,2).equals("5") && p22.equals("051") 
							? "0001000000008   "  // Canal redes externas
							: "0001930000002   "; // Canal CNB Moviles
					break;
				case "3":
					p41 = "0001829600002   ";
					break;
				case "4":	// canal Oficinas
					p41 = "00010".concat(codigoOficina.substring(1, 4)).concat("00003   ");
					p100 = "50";
					break;
				case "5":
					p41 = codigoOficina.substring(1,2).equals("3") 
					    ? "0001000000008   " // canal Corporate
					    : codigoOficina.substring(1,2).equals("6") 
						? "0001930000002   " // canal Business
						: "0001820500002   "; // canal BBS
					break;
				case "6":	// canal WAP
					p41 = "0001829800002   ";
					break;
				case "7":	// canal Agilizador
					p41 = "0001829900002   ";
					break;
				case "8":
					p41 = "0001820100002   ";	// canal internet	
					break;
				case "9":	// canal IVR
					p41 = "0001920100005   ";
					break;
				default: 
					p41 = "0001829700003   ";
					p100 = "70";
					break;
				}
				
				tranType = "50";
				fromAccount = "";
				String modalidadPago = tramaCompletaAscii.substring(MODALIDAD_PAGO_INI, MODALIDAD_PAGO_FIN);
				if (tramaCompletaAscii.substring(TIPO_CUENTA_DEBITADA_INI, TIPO_CUENTA_DEBITADA_FIN).equals("0")) {
					fromAccount = "10";	// cuenta de ahorros
				} else if(tramaCompletaAscii.substring(TIPO_CUENTA_DEBITADA_INI, TIPO_CUENTA_DEBITADA_FIN).equals("1")){
					fromAccount = "20";	// cuenta corriente
				} else if(modalidadPago.equals("1")){
					fromAccount = "01";	// efectivo
				} else if(modalidadPago.equals("2")){
					fromAccount = "02";	// cheque
				}

				// TRACK2 Field 98
				out.putField(Iso8583.Bit._098_PAYEE, "0054150070650000000000000");

				// CAMPO 102 DEBIT ACCOUNT
				out.putField(Iso8583.Bit._102_ACCOUNT_ID_1,"00010".concat(cuentaCreditar));

				// CAMPO 103 CREDIT ACCOUNT
				out.putField(Iso8583.Bit._103_ACCOUNT_ID_2, "011".concat(entidadAutorizadora).concat("0").concat(cuentaCreditar));
				
				// 127.22 TAG B24_Field_35
				sd.put("B24_Field_35", bin.concat(Pack.resize(cuentaDebitar, 18, '0', false)).concat("D            "));
				// 127.22 TAG B24_Field_52
				sd.put("B24_Field_52", "FFFFFFFFFFFFFFFF");
				// 127.22 TAG B24_Field_60
				sd.put("B24_Field_60", "0901BBOG+000");
				// 127.22 TAG B24_Field_104
				sd.put("B24_Field_104", "000000000000000000");

			}

			// PROCESAMIENTO DE REVERSO
			if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("080")
					|| Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020")) {

				keyReverse = (String) ISCInterfaceCB.cacheKeyReverseMap.get(seqNrReverse);
				if (keyReverse == null)
					keyReverse = DBHandler.getKeyOriginalTxBySeqNr(seqNrReverse);

				if (keyReverse == null) {
					keyReverse = "0000000000";
					sd.put("REV_DECLINED", "TRUE");
				}
				out.putField(Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS, Pack.resize(keyReverse, 42, '0', true));

				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY,
						"0420".concat(Transform
								.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(248, 268))))
								.concat("0" + cons.substring(2, 5)));
				out.putPrivField(Iso8583Post.PrivBit._011_ORIGINAL_KEY, keyReverse);

				if (Transform.fromEbcdicToAscii(in.getField(ISCReqInMsg.Fields._08_H_STATE)).equals("020"))
					tranType = "20";

				// PROCESAMIENTO TX FINANCIERA
			} else {
				out.putField(Iso8583Post.Bit._059_ECHO_DATA, Transform
						.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(406, 414))));
				// 127.2 SWITCHKEY
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);
				ISCInterfaceCB.cacheKeyReverseMap.put(seqNr, key);
			}

			// Field 3
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, tranType.concat(fromAccount).concat(toAccount));

//			//CAMPO 7 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));

			// CAMPO 37 Retrieval Reference Number
			out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, p37);

			// CAMPO 12 TIME LOCAL
			out.putField(Iso8583.Bit._012_TIME_LOCAL, p12);

			// CAMPO 13 TRANSMISSION DATE N TIME
			out.putField(Iso8583.Bit._013_DATE_LOCAL, p13);

			out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);

			// TRACK2 Field 22
			out.putField(Iso8583.Bit._022_POS_ENTRY_MODE, p22);

			// TRACK2 Field 35
			out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");
			
			// TRACK2 Field 43
			out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, p43);
			
			// CAMPOO 100 ROUTING 
			out.putField(Iso8583.Bit._100_RECEIVING_INST_ID_CODE, p100);
			
			// 127.22 TAG B24_Field_17
			sd.put("B24_Field_17", settlementDate);
			// 127.22 TAG B24_Field_41
			sd.put("B24_Field_41", p41);
			// 127.22 TAG B24_Field_125
			sd.put("B24_Field_125", p125);
			//127.22 TAG MSG_HEXA
			sd.put("IN_MSG", in.getTotalHexString());
			//127.22 TAG Entidad Autorizadora
			sd.put("ENT_AUT", entidadAutorizadora);
			//127.22 TAG Entidad Autorizadora
			sd.put("P100", p100);
			
			////////// TAGS EXTRACT 
			
			sd.put("VIEW_ROUTER", "V2");
			sd.put("Codigo_FI_Origen", "1019");
			sd.put("Nombre_FI_Origen", "CIC");		
			sd.put("Codigo_Transaccion_Producto", "02");
			sd.put("Codigo_Transaccion", "29");
			sd.put("Nombre_Transaccion", "ONESID");
			sd.put("Tipo_de_Cuenta_Debitada", "CRE");			
			sd.put("BIN_Cuenta", cuentaCreditar.substring(0,6));
			sd.put("PRIM_ACCOUNT_NR", Pack.resize(cuentaCreditar.substring(7,16), 18, '0', false));
			sd.put("CLIENT_CARD_NR_1", binTarjeta);
			sd.put("Codigo_de_Red", "1019");
			sd.put("Numero_Terminal", terminal);
			sd.put("Identificacion_Canal", identificacionCanal);
			sd.put("Codigo_Establecimiento", "          ");
			sd.put("Nombre_Establecimiento_QR", "                    ");
			sd.put("FI_CREDITO", "0001");
			sd.put("SEC_ACCOUNT_NR_PAGOTC",Pack.resize(cuentaDebitar, 18, '0', false));
			sd.put("FI_DEBITO", "0001");
			sd.put("PAN_Tarjeta", Pack.resize(cuentaCreditar, 19, ' ', true));	
			sd.put("Indicador_AVAL", "1");
			sd.put("Ind_4xmil", "0");
			sd.put("SECUENCIA", secuenciaTS);
			sd.put("Tarjeta_Amparada", cuentaCreditar);
			sd.put("Mod_Credito", modalidadPagoCredito);
			sd.put("Clase_Pago", clasePagoCredito);
			sd.put("Ent_Adq", "0001");
			sd.put("Dispositivo", "0");
			sd.put("Canal", "01");
			sd.put("pos_entry_mode", "000");
			sd.put("service_restriction_code", "000");
			sd.put("Entidad", "0000");
			sd.put("Identificador_Terminal", "0");
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
