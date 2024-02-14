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

public class CashAdvanceTCOficinaAux {
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
	public static final int NATURALEZA_PAGO_INI = 145;
	public static final int NATURALEZA_PAGO_FIN = 145 + 1;
	public static final int CLASE_PAGO_INI = 146; //2-Normal
	public static final int CLASE_PAGO_FIN = 146 + 1;
	public static final int DIA_FECHA_APLICACION_DEL_PAGO_INI = 147;
	public static final int DIA_FECHA_APLICACION_DEL_PAGO_FIN = 147 + 2;
	public static final int MES_FECHA_APLICACION_DEL_PAGO_INI = 149;
	public static final int MES_FECHA_APLICACION_DEL_PAGO_FIN = 149 + 2;
	public static final int CODIGO_ENTIDAD_QUE_AUTORIZA_EL_DEBITO_INI = 160;
	public static final int CODIGO_ENTIDAD_QUE_AUTORIZA_EL_DEBITO_FIN = 160 + 4;
	public static final int IDENTIFICADOR_PINPAD_INI = 175;
	public static final int IDENTIFICADOR_PINPAD_FIN = 175 + 2;
	public static final int COD_OFICINA_ADQUIRIENTE_INI = 181;
	public static final int COD_OFICINA_ADQUIRIENTE_FIN = 181 + 4;
	public static final int NUM_SEQ_TX_ACTUAL_INI = 206;
	public static final int NUM_SEQ_TX_ACTUAL_FIN = 206 + 4;
	public static final int NUM_SEQ_TX_ORIG_A_REVERSAR_INI = 210;
	public static final int NUM_SEQ_TX_ORIG_A_REVERSAR_FIN = 210 + 4;
	public static final int CODIGO_DANE_OFICINA_ADQUIRIENTE_INI = 214;
    public static final int CODIGO_DANE_OFICINA_ADQUIRIENTE_FIN = 214 + 5;
	public static final int NOMBRE_OFICINA_ADQUIRIENTE_INI = 219;
	public static final int NOMBRE_OFICINA_ADQUIRIENTE_FIN = 219 + 18;
	public static final int TRACK2_INI = 237;
	public static final int TRACK2_FIN = 237 + 40;
	public static final int PINBLOCK_INI = 314;
	public static final int PINBLOCK_FIN = 314 + 16;
	/*public static final int CANTIDAD_CHEQUES_INI = 832;
	public static final int CANTIDAD_CHEQUES_FIN = 832 + 2;*/	// Se deja comentada para evitar NullPointerException porque la trama es de 831 caracteres en total.
	String ENRUTAR_A_FIRSTDATA = "100";
	String ENRUTAR_A_ATH_CONEXION_OFICINAS = "50";
	//String ENRUTAR_A_ATH_CONEXION_INTERNET = "70";
	String MENSAJE_BASE24_FORMATO_ATM = "1";
	String MENSAJE_BASE24_FORMATO_POS = "2";
	
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
		
			if (tramaCompletaAscii.substring(BYTES_ESTADO_INI,BYTES_ESTADO_FIN).matches("^((040)|(050)|(060)|(070))")) {
				businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			} else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}
					
			String tranType = "01";
			String fromAccount = "30";
			String toAccount = "00";
			String p12 = new DateTime().get("HHmmss");
			String p13 = new DateTime().get("MMdd");
			String codigoOficina = tramaCompletaAscii.substring(COD_OFICINA_INI,COD_OFICINA_FIN);
			String numeroSecuencia = tramaCompletaAscii.substring(NUM_SEQ_TX_ACTUAL_INI, NUM_SEQ_TX_ACTUAL_FIN);
			String numeroSecuenciaOriginalAReversar = tramaCompletaAscii.substring(NUM_SEQ_TX_ORIG_A_REVERSAR_INI, NUM_SEQ_TX_ORIG_A_REVERSAR_FIN);
			String tipoMensaje = tramaCompletaAscii.substring(BYTES_ESTADO_INI,BYTES_ESTADO_FIN);	
			/* Para la variable tipoMensaje, va a tener alguno de los siguientes valores: 	
			 	000 = Normal 
				010 = Devolución   
				020 = Anulación 
				030 = Anulación de devolución 
				080 = Reverso  
				040 = Transacción Next Day 
				050 = Devolución Next Day 
				060 = Anulación NextDay 
				070 = Anulación Devolución NextDay */
			String keyReverse = null;
			String terminal = tramaCompletaAscii.substring(TERMINAL_INI, TERMINAL_FIN);
			String numeroIdentificacion = tramaCompletaAscii.substring(NUMERO_IDENTIFICACION_INI, NUMERO_IDENTIFICACION_FIN);
			String cuentaAAcreditar = tramaCompletaAscii.substring(NUMERO_CUENTA_ACREDITAR_INI, NUMERO_CUENTA_ACREDITAR_FIN);
			String cuentaADebitar = tramaCompletaAscii.substring(NUMERO_CUENTA_DEBITADA_INI, NUMERO_CUENTA_DEBITADA_FIN);
			String identificadorPinpad = tramaCompletaAscii.substring(IDENTIFICADOR_PINPAD_INI, IDENTIFICADOR_PINPAD_FIN);  
			String identificacionCanal = "  ";
			String modalidadPago = tramaCompletaAscii.substring(MODALIDAD_PAGO_INI, MODALIDAD_PAGO_FIN); // 0: CargoCuenta, 1: Efectivo, 2: Cheque
			String naturalezaPago = tramaCompletaAscii.substring(NATURALEZA_PAGO_INI,NATURALEZA_PAGO_FIN); // 5: Pago TC, 6: Avance TC
			String clasePagoCredito = tramaCompletaAscii.substring(CLASE_PAGO_INI,CLASE_PAGO_FIN);
			String secuenciaTS = tramaCompletaAscii.substring(SECUENCIA_TS_INI,SECUENCIA_TS_FIN);
			String codigoEntidadAutorizadora = tramaCompletaAscii.substring(CODIGO_ENTIDAD_QUE_AUTORIZA_EL_DEBITO_INI, CODIGO_ENTIDAD_QUE_AUTORIZA_EL_DEBITO_FIN);	// Se corrigió posición 72 por posición 160.
			String codigoEntidadAdquiriente = "0001";
			String codigoOficinaAdquiriente = tramaCompletaAscii.substring(COD_OFICINA_ADQUIRIENTE_INI, COD_OFICINA_ADQUIRIENTE_FIN);
			String base24Field41 = codigoEntidadAdquiriente.concat(codigoOficinaAdquiriente).concat("00003   ");
			String p41 = base24Field41.substring(0, 8);
			String base24Field42 = "380000000000000";		// Examinar en AUTRA si el valor es distinto de espacios, y si es quemado o calculado o copiado.
			String codigoDaneCiudadOficinaAdquiriente = tramaCompletaAscii.substring(CODIGO_DANE_OFICINA_ADQUIRIENTE_INI, CODIGO_DANE_OFICINA_ADQUIRIENTE_FIN);
			String nombreOficinaAdquiriente = tramaCompletaAscii.substring(NOMBRE_OFICINA_ADQUIRIENTE_INI, NOMBRE_OFICINA_ADQUIRIENTE_FIN);
			String tipoCuentaDebito = tramaCompletaAscii.substring(TIPO_CUENTA_DEBITADA_INI, TIPO_CUENTA_DEBITADA_FIN);
			String FI_Tarjeta = "";
			String track2 = tramaCompletaAscii.substring(TRACK2_INI, TRACK2_FIN);
			String fechaVencimiento = track2.substring(17,21);
			String pinBlock = tramaCompletaAscii.substring(PINBLOCK_INI, PINBLOCK_FIN);
			//String cantidadCheques = tramaCompletaAscii.substring(CANTIDAD_CHEQUES_INI, CANTIDAD_CHEQUES_FIN);	// Se deja comentada para evitar NullPointerException porque la trama es de 831 caracteres en total.
			String p37 = "0901".concat(codigoOficinaAdquiriente).concat(numeroSecuencia);
			String key = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
			String binTarjeta = "000000000000000000";

			
        	//Validacion Titularidad, trae informacion nombre e identificacion del tarjetahabiente solo para tarjeta Bco Bta
			//Client udpClientValidation = new Client(ISCInterfaceCB.ipServerValidation, ISCInterfaceCB.portServerValidation);				
			//String msgFromValidationTC = udpClientValidation.sendMsgForValidationTitular(cuentaCreditar, enableMonitor);
			
			String p22 = "010";
			if (codigoOficina.substring(0, 1).equals("4") &&
			    identificadorPinpad.equals("PP"))
			{
				p22 = "051";
			}
			

			//-------------------Lógica Tokens First Data formato POS - Inicio ---------------------
			//Token 04
			String token_04 = "! 0400020  00000000000Y     Y0";
			String token_BM = "";
			//Token BM
			switch (modalidadPago)
			{
				case "0": //Cargo cuenta
					if (tipoCuentaDebito.equals("1"))
						token_BM = "! BM00036 Q401502030      00000000000000000000";
					else
						token_BM = "! BM00036 Q401501030      00000000000000000000";							
				break;
				case "1": //Efectivo
					token_BM = "! BM00036 Q406500130      00000000000000000000";					
				break;
				case "2": //Cheque
					token_BM = "! BM00036 Q406500230      00000000000000000000";
				break;
			}		
			
			//Token C0
			String token_C0 = "! C000026                   2";
			String franquicia = " ";
			if(cuentaAAcreditar.substring(0,1).equals("5")){
				franquicia = "M";
			}
			String fld_present = "0";
			if(identificadorPinpad.equals("PP")){ //Transacción presente
				fld_present = "1"; 
			}
			
			token_C0 = token_C0.concat(franquicia).concat(" ").concat(fld_present).concat("    ");
			//Token C4
			String crdhldr_present_ind = "0";
			String crd_present_ind = "0";
			if(codigoOficina.substring(0,1).equals("4"))  	// Si es en Oficina
			{
				crdhldr_present_ind = "5";
				crd_present_ind = "1";
			}
			String token_C4 = "! C400012 1 2".concat(crdhldr_present_ind).concat(crd_present_ind).concat("00  600");
			//Token Q4
			String moneda = "170"; //Moneda Pesos
			if(codigoOficina.substring(0,1).equals("4") && codigoOficina.substring(1,2).equals("1"))
				 moneda = "840"; //Moneda Dolares
			String token_Q4 = "! Q400122 ".concat("000000000000000000000000000000000000000000000000000000000000000000000000000000000000000").concat(moneda).concat("00").concat(settlementDate).concat("00000000000000000000000000");
			//-------------------Logica Tokens First Data formato POS - Fin ---------------------

			//-------------------Logica Tokens First Data formato ATM - Inicio ---------------------
			//Token 03
			String cardVerifyFlag = "  ";
			//String typeMsg = "  ";
			String typeMsg = codigoEntidadAutorizadora.equals("0001") ? MENSAJE_BASE24_FORMATO_ATM : MENSAJE_BASE24_FORMATO_POS;	// Para TC BBOG es "1". Para otras TC es "2".
			
			if (typeMsg.equals(MENSAJE_BASE24_FORMATO_ATM)) //Si formato mensaje es ATM
				cardVerifyFlag = " Y";
			else //Si formato mensaje es POS 
				cardVerifyFlag = "PN";

			String token_03 = "! 0300006 01".concat(cardVerifyFlag).concat("00");
			
			//Token 24
			if(cuentaADebitar.substring(0,1).equals("4"))
				franquicia = "V ";  //Visa
			else
				franquicia = "M ";  //Mastercard
							
			String token_24 = "! 2400010 ".concat(franquicia).concat("A0").concat(fechaVencimiento).concat("0 ");
			
			//Token B4				
			String token_B4 = "! B400020 ".concat(p22).concat("51000000000100520");
			          
			//Token BM
			token_BM = "";
			if (naturalezaPago.equals("6")) //Si es avance
				token_BM = "! BM00036 Q101013000      00000000000000000000";

			//Token QT
			String token_QT = "";
			token_QT = "! QT00032 013000".concat("0000").concat(numeroIdentificacion.substring(4,12)).concat("00000000000000");
			
			//-------------------Logica Tokens First Data formato ATM - Fin ---------------------

			
			if(naturalezaPago.equals("6")			// si es AVANCE
				&& codigoOficina.startsWith("4")	// y si es en OFICINA
			)
			if (codigoEntidadAutorizadora.equals("0001")) {	// INICIO LÓGICA PARA TARJETAS BBOG (FIRSTDATA)
				sd.put("indicator_product", MENSAJE_BASE24_FORMATO_ATM);	// INDICADOR DE MENSAJE BASE24 EN FORMATO "ATM"
				
				/* ISO8583 DEBE TENER LOS SIGUIENTES CAMPOS:	
				 * 3, 4 json, 7, 11 json, 12, 13, 15, 22, 25 json, 26 json, 32 json, 35, 37, 41 json, 42 json, 43, 49 json, 52 json, 59, 98 json, 100, 123 json.*/
				// CAMPO 3 PROCESSING CODE
				out.putField(Iso8583.Bit._003_PROCESSING_CODE, tranType.concat(fromAccount).concat(toAccount));
				//CAMPO 7 TRANSMISSION DATE N TIME
				out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));
				// CAMPO 12 TIME LOCAL
				out.putField(Iso8583.Bit._012_TIME_LOCAL, p12);
				// CAMPO 13 TRANSMISSION DATE N TIME
				out.putField(Iso8583.Bit._013_DATE_LOCAL, p13);
				// CAMPO 15 FECHA DE COMPENSACIÓN
				out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);
				// TRACK2 Field 22
				out.putField(Iso8583.Bit._022_POS_ENTRY_MODE, p22);
				// TRACK2 Field 35
				out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");
				// CAMPO 37 Retrieval Reference Number
				out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, p37);
				// CAMPO 41 
				out.putField(Iso8583Post.Bit._041_CARD_ACCEPTOR_TERM_ID, p41);
				// TRACK2 Field 43
				out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, codigoOficinaAdquiriente.concat(nombreOficinaAdquiriente).concat(codigoDaneCiudadOficinaAdquiriente).concat("        BO CO"));
				// CAMPO 49
				out.putField(Iso8583Post.Bit._049_CURRENCY_CODE_TRAN, "170"); 	// "170" indica pesos colombianos.
				// CAMPO 52
				out.putField(Iso8583Post.Bit._052_PIN_DATA, "FFFFFFFF");
				// CAMPO 59
				out.putField(Iso8583Post.Bit._059_ECHO_DATA, numeroSecuencia);
				// TRACK2 Field 98
				out.putField(Iso8583.Bit._098_PAYEE, "0054150070650000000000000");
				// CAMPO 100 PARA ENRUTAMIENTO
				out.putField(Iso8583.Bit._100_RECEIVING_INST_ID_CODE, ENRUTAR_A_FIRSTDATA);	//Enruta a FirstData
				
				/* BASE24 DEBE TENER LOS SIGUIENTES CAMPOS:	3, 4, 7, 11, 12, 13, 17, 18, 22, 32, 35, 37, 41, 43, 48, 49, 52, 60, 61, 100, 126. 
				 * Entonces, quitar del mensaje ISO8583Post los campos simples: 15, 25, 26, 42, 98, 123 */
				/* En el archivo "JsonHashPrd.json" en la línea 170, veo que para el p3=013000 se eliminan los siguientes campos: 
				 * 14, 15, 25, 26, 40, 52, 56, 102, 103, 104, 123 
				 * PREGUNTAR A MENESES CÓMO QUITAR EL CAMPO 42 Y EL 98 SIN AFECTAR LAS OTRAS TX CON p3=013000 QUE SÍ CONTIENEN CAMPO 42 O 98 */	
				
 				// 127.22 TAG B24_Field_17
				sd.put("B24_Field_17", settlementDate);
				// 127.22 TAG B24_Field_18
				sd.put("B24_Field_18", "6010");
				// 127.22 TAG B24_Field_35
				sd.put("B24_Field_35", track2.substring(0,37));
				// 127.22 TAG B24_Field_41
				sd.put("B24_Field_41", base24Field41);
				// CAMPO 48
				sd.put("B24_Field_48", "1                       10000017000000000000");
				// 127.22 TAG B24_Field_52
				sd.put("B24_Field_52", pinBlock);
				// 127.22 TAG B24_Field_60
				sd.put("B24_Field_60", "P801P8010000");
				// 127.22 TAG B24_Field_61
				sd.put("B24_Field_61", "000100000000000");
				// 127.22 TAG B24_Field_100 RECV_INST_ID
				sd.put("B24_Field_100", "10000000001");
				// 127.22 TAG B24_Field_126 	longitud total: 166		Tokens: 03,	24,	B4,	BM,	QT (ojo: token QT varía entre mensajes 0200 y 0210)
				sd.put("B24_Field_126", "& 0000600166".concat(token_03).concat(token_24).concat(token_B4).concat(token_BM).concat(token_QT));
				
				// PROCESAMIENTO DE REVERSO
				/* ISO8583post DEBE TENER LOS SIGUIENTES CAMPOS:	3, 4, 7, 11, 12, 13, 22, 25, 41, 42, 43, 49,  */
				/* BASE24 DEBE TENER LOS SIGUIENTES CAMPOS:			3, 4, 7, 11, 12, 13, 15, 17, 18, 22, 32, 35, 37, 38, 39, 41, 43, 48, 49, 52, 60, 61, 90, 100, 126. */
				if (	tipoMensaje.equals("080")		//Reverso
						|| tipoMensaje.equals("020")	//Anulación
					) 
				{
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
						//sd.put("B24_Field_95", "000000000000000000000000000000000000000000");
						sd.put("KEY_REVERSE", keyReverse);
						sd.put("B24_Field_90", keyReverse+"0000000000");
						//sd.put("B24_Field_37", keyReverse.substring(4,16));
						
						if (tipoMensaje.equals("020")) {
							sd.put("ANULACION", "TRUE");
							sd.put("B24_Field_15", settlementDate);
							sd.put("B24_Field_38", sdOriginal.get("Autorizacion_Original"));
							sd.put("KeyOriginalTx", keyReverse);
							sd.put("B24_Field_52", "0000000000000000");
							sd.put("B24_Field_54", "000".concat(sdOriginal.get("Monto_Original"))
									.concat("000000000000000000")
									.concat(sdOriginal.get("Monto_Original")));
							out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, keyAnulacion);
							token_QT = "! QT00032 01300000000000000000000000000000";
							sd.put("B24_Field_126", "& 0000600166".concat(token_03).concat(token_24).concat(token_B4).concat(token_BM).concat(token_QT));
						}
					}

				}	// FIN DE PROCESAMIENTO DE REVERSO 
				else {
					//out.putField(Iso8583Post.Bit._059_ECHO_DATA, Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(406, 414))));
					//out.putField(Iso8583Post.Bit._059_ECHO_DATA, numeroSecuencia);	// VALIDAR CON MENESES SI ESTE CAMPO APLICA A MENSAJE ISO8583 PARA TC BBOG Y TC AVAL
					// 127.2 SWITCHKEY
					out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);
					ISCInterfaceCB.cacheKeyReverseMap.put(numeroSecuencia, key);
				}

			} // FIN LÓGICA PARA TARJETAS BBOG (FIRSTDATA)
			else {	// INICIO DE LÓGICA PARA TARJETAS AVAL (ATH)
				sd.put("indicator_product", MENSAJE_BASE24_FORMATO_POS);	// INDICADOR DE MENSAJE BASE24 EN FORMATO "POS"
				
				/* ISO8583 DEBE TENER LOS SIGUIENTES CAMPOS:	
				 * 3, 4 json, 7, 11 json, 12, 13, 15, 22, 25 json, 26 json, 32 json, 35, 37, 41 json, 42 json, 43, 49 json, 52 json, 59, 98 json, 100, 123 json.*/
				// CAMPO 3 PROCESSING CODE
				out.putField(Iso8583.Bit._003_PROCESSING_CODE, tranType.concat(fromAccount).concat(toAccount));
				//CAMPO 7 TRANSMISSION DATE N TIME
				out.putField(Iso8583.Bit._007_TRANSMISSION_DATE_TIME, new DateTime(5).get("MMddHHmmss"));
				// CAMPO 12 TIME LOCAL
				out.putField(Iso8583.Bit._012_TIME_LOCAL, p12);
				// CAMPO 13 TRANSMISSION DATE N TIME
				out.putField(Iso8583.Bit._013_DATE_LOCAL, p13);
				// CAMPO 15 FECHA DE COMPENSACIÓN
				out.putField(Iso8583.Bit._015_DATE_SETTLE, settlementDate);
				// TRACK2 Field 22
				out.putField(Iso8583.Bit._022_POS_ENTRY_MODE, p22);
				// TRACK2 Field 35
				out.putField(Iso8583.Bit._035_TRACK_2_DATA, "0088010000000000000=9912000");
				// CAMPO 37 Retrieval Reference Number
				out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, p37);
				// CAMPO 41 
				out.putField(Iso8583Post.Bit._041_CARD_ACCEPTOR_TERM_ID, p41);
				// TRACK2 Field 43
				out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC, codigoOficinaAdquiriente.concat(nombreOficinaAdquiriente).concat(codigoDaneCiudadOficinaAdquiriente).concat("             "));
				// CAMPO 49
				out.putField(Iso8583Post.Bit._049_CURRENCY_CODE_TRAN, "170"); 	// "170" indica pesos colombianos.
				// CAMPO 52
				out.putField(Iso8583Post.Bit._052_PIN_DATA, "FFFFFFFF");
				// CAMPO 59
				out.putField(Iso8583Post.Bit._059_ECHO_DATA, numeroSecuencia);
				// TRACK2 Field 98
				out.putField(Iso8583.Bit._098_PAYEE, "0054150070650000000000000");
				// CAMPO 100 PARA ENRUTAMIENTO
				out.putField(Iso8583.Bit._100_RECEIVING_INST_ID_CODE, ENRUTAR_A_ATH_CONEXION_OFICINAS);	//Enruta a ATH 
				
				/* BASE24 DEBE TENER LOS SIGUIENTES CAMPOS:		3, 4, 7, 11, 12, 13, 15, 17, 32, 35, 37, 41, 42, 43, 48, 49, 52, 102, 104, 126. 
				 * Quitar del mensaje ISO8583Post los campos: 	22, 25, 26, 42, 59, 98, 100, 123 */
				/* En el archivo "JsonHashPrd.json" en la línea 170, veo que para el p3=013000 se eliminan los siguientes campos: 
				 * 14, 15, 25, 26, 40, 52, 56, 102, 103, 104, 123 
				 * PREGUNTAR A MENESES CÓMO QUITAR EL CAMPO 42 Y EL 98 SIN AFECTAR LAS OTRAS TX CON p3=013000 */
				// Desarrollo pendiente por parte de Andres Meneses.
 				// 127.22 TAG B24_Field_17
				sd.put("B24_Field_17", settlementDate);
				// 127.22 TAG B24_Field_35
				sd.put("B24_Field_35", track2.substring(0,37));
				// 127.22 TAG B24_Field_41
				sd.put("B24_Field_41", base24Field41);
				// 127.22 TAG B24_Field_42
				sd.put("B24_Field_42", base24Field42);	// Examinar en AUTRA si el valor es distinto de espacios, y si es quemado o calculado o copiado.
				// CAMPO 48
				sd.put("B24_Field_48", "000000000000               ");
				// 127.22 TAG B24_Field_52
				sd.put("B24_Field_52", pinBlock);
				// 127.22 TAG B24_Field_102
				sd.put("B24_Field_102", "00".concat(track2.substring(0,16)));
				// 127.22 TAG B24_Field_104
				sd.put("B24_Field_104", "00".concat(numeroIdentificacion));
				// 127.22 TAG B24_Field_126 	longitud total: 342		Tokens: QT (longitud 42), B2 (longitud 168), B3 (longitud 90), B4 (longitud 30)
				// Ejemplo:   (126)pos_preauth: & 0000500342! QT00032 0130000000000000000000000000000 ! B200158 7FF9000080808004080001781D35C8E468AB0000000000000000000000003C000027170170210505011347B646000706010A03A0B80100000000000000000000000000000000000000000000000000! B300080 CF00SmartPOS60D8C8000000000000001100020204000007A0000000031010000000000000000000! B400020 05151000000000000000
				//String token_QT = "! QT00032 013000".concat("0000").concat(numeroIdentificacion.substring(4,12)).concat("00000000000000");
				//String token_QT = "! QT00032 0130000000000000000000000000000 ";
				/* EXTRAER LÓGICA DESDE AUTRA PARA LOS TOKENS B2 Y B3 */
				String token_B2 = "! B200158 7FF9000080808004080001781D35C8E468AB0000000000000000000000003C000027170170210505011347B646000706010A03A0B80100000000000000000000000000000000000000000000000000";
				String token_B3 = "! B300080 CF00SmartPOS60D8C8000000000000001100020204000007A0000000031010000000000000000000";
				//String token_B4 = "! B400020 ".concat(p22).concat("51000000000100520");
				//String token_B4 = "! B400020 05151000000000000000";
				sd.put("B24_Field_126", "& 0000500342".concat(token_QT).concat(token_B2).concat(token_B3).concat(token_B4));
				
				// VALIDAR EN AUTRA SI EXISTE ANULACIÓN Y REVERSO PARA AVANCE EN OFICINA CON TC AVAL.
				
			}	// FIN DE LÓGICA PARA TARJETAS AVAL (ATH)
			

			// PROCESAMIENTO DE REVERSO
			/*if (	tipoMensaje.equals("080")			//Reverso
					|| tipoMensaje.equals("020")	//Anulación
				) 
			{
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
					
					if (tipoMensaje.equals("020")) {
						sd.put("ANULACION", "TRUE");
						sd.put("B24_Field_15", settlementDate);
						sd.put("B24_Field_38", sdOriginal.get("Autorizacion_Original"));
						sd.put("KeyOriginalTx", keyReverse);
						sd.put("B24_Field_52", "0000000000000000");
						sd.put("B24_Field_54", "000".concat(sdOriginal.get("Monto_Original"))
								.concat("000000000000000000")
								.concat(sdOriginal.get("Monto_Original")));
						out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, keyAnulacion);
					}
				}

			}	// FIN DE PROCESAMIENTO DE REVERSO 
			else {
				//out.putField(Iso8583Post.Bit._059_ECHO_DATA, Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(406, 414))));
				//out.putField(Iso8583Post.Bit._059_ECHO_DATA, numeroSecuencia);	// VALIDAR CON MENESES SI ESTE CAMPO APLICA A MENSAJE ISO8583 PARA TC BBOG Y TC AVAL
				// 127.2 SWITCHKEY
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);
				ISCInterfaceCB.cacheKeyReverseMap.put(numeroSecuencia, key);
			}*/

			//127.22 TAG MSG_HEXA
			sd.put("IN_MSG", in.getTotalHexString());
			//127.22 TAG Entidad Autorizadora
			sd.put("ENT_AUT", codigoEntidadAutorizadora);
			
			////////// TAGS EXTRACT 
			
			sd.put("VIEW_ROUTER", "V2");
			sd.put("Codigo_FI_Origen", "1019");
			sd.put("Nombre_FI_Origen", "CIC");		
			sd.put("Ind_4xmil", "1");
			sd.put("Tarjeta_Amparada", cuentaAAcreditar);
			
			if (codigoEntidadAutorizadora.equals("0001"))  //Si es Banco de Bogotá
			{	
				sd.put("Codigo_Transaccion_Producto", "02");
				sd.put("Tipo_de_Cuenta_Debitada", "CRE");
				sd.put("BIN_Cuenta", cuentaAAcreditar.substring(0,6));
				sd.put("PRIM_ACCOUNT_NR", Pack.resize(cuentaAAcreditar.substring(6,16), 18, '0', false));
				sd.put("SEC_ACCOUNT_NR_PAGOTC",Pack.resize(cuentaADebitar, 18, '0', false));
				sd.put("PAN_Tarjeta", Pack.resize(cuentaAAcreditar, 19, ' ', true));	
				if (modalidadPago.equals("0")) //Si es Cargo cuenta
				{	
					sd.put("Codigo_Transaccion", "29");
					sd.put("Nombre_Transaccion", "ONESID");
					sd.put("FI_CREDITO", "0001");
					sd.put("FI_DEBITO", "0001");
					//if (codigoOficina.substring(0, 1).equals("4") ) //Canal Oficina
					sd.put("Ind_4xmil", "0");
				}
				else //Efectivo o Cheque
				{	
					sd.put("Codigo_Transaccion", "01");
					sd.put("Nombre_Transaccion", "DEPOSI");
					sd.put("FI_CREDITO", "0000");
					sd.put("FI_DEBITO", "0000");
					sd.put("Ind_4xmil", "1");
					sd.put("SEC_ACCOUNT_TYPE", "   ");
					sd.put("Ofi_Adqui", codigoOficinaAdquiriente);
				}
			}
			else  //Si es Aval
			{
				sd.put("Codigo_Transaccion", "29");
				sd.put("Nombre_Transaccion", "ONESID");
				sd.put("BIN_Cuenta", "000000");
				sd.put("PRIM_ACCOUNT_NR", cuentaADebitar);
				sd.put("FI_DEBITO", "0001");
				sd.put("FI_CREDITO", codigoEntidadAutorizadora);
				sd.put("SEC_ACCOUNT_NR_PAGOTC", cuentaAAcreditar);
				sd.put("SEC_ACCOUNT_TYPE", "CRE");
				sd.put("PAN_Tarjeta", binTarjeta.substring(0,16).concat("   "));
				sd.put("Vencimiento", "0000");
				sd.put("Tarjeta_Amparada", "0000000000000000");
				//sd.put("Numero_Cheques", cantidadCheques);
				//if (codigoOficina.substring(0, 1).equals("4")) //Canal Oficina
				sd.put("Codigo_Transaccion_Producto", "02");
				if (modalidadPago.equals("0")) //Si es Cargo cuenta
				{		
					sd.put("Codigo_Transaccion", "29");
					sd.put("Nombre_Transaccion", "ONESID");
				}
				else //Efectivo o Cheque
				{
					sd.put("Codigo_Transaccion", "01");
					sd.put("Nombre_Transaccion", "DEPOSI");
					sd.put("PRIM_ACCOUNT_NR", cuentaAAcreditar);
					sd.put("FI_CREDITO", "0000"); 
					sd.put("SEC_ACCOUNT_NR_PAGOTC", "000000000000000000");
					sd.put("FI_DEBITO", "0000");
					sd.put("SEC_ACCOUNT_TYPE", "   ");
					sd.put("Ofi_Adqui", codigoOficinaAdquiriente);
				}
			}
			sd.put("CLIENT_CARD_NR_1", binTarjeta);
			sd.put("Codigo_de_Red", "1019");
			
			//if (codigoOficina.substring(0, 1).equals("4")){ //Canal Oficina
			sd.put("Numero_Terminal", codigoOficinaAdquiriente);
			sd.put("Nombre_Establecimiento_QR", codigoOficinaAdquiriente.concat(nombreOficinaAdquiriente));
			//}
			/*else {  //Otros canales 
				 sd.put("Numero_Terminal",codigoOficina);
				 sd.put("Nombre_Establecimiento_QR", "                    ");
			}*/
			
			sd.put("Identificacion_Canal", identificacionCanal);
			sd.put("Codigo_Establecimiento", "          ");
			
			//if (!msgFromValidationTC.equals("NO") && entidadAutorizadora.equals("0001"))
			//{
			//sd.put("CUSTOMER_NAME",Pack.resize(msgFromValidationTC.substring(4,25), 28,' ', true));	
			//sd.put("ID_CLIENT",Pack.resize(msgFromValidationTC.substring(25,33), 13, '0', false));
			//}
			//else
			//{
			//sd.put("CUSTOMER_NAME", "                            ");	
			//sd.put("ID_CLIENT", "0000000000000");
			//}	
			
			//sd.put("TITULAR_TC",msgFromValidationTC);
			sd.put("TRANSACTION_INPUT", "PAGO_TC_CANALVIRTUAL");	// DEFINIR TEXTO A CONFIGURAR PARA AVANCE TC EN OFICINA. 
			sd.put("Indicador_AVAL", "1");	
			sd.put("SECUENCIA_REQ", secuenciaTS);
			sd.put("Mod_Credito", naturalezaPago);
			sd.put("Clase_Pago", clasePagoCredito);
			sd.put("Ent_Adq", "0001");
			sd.put("Dispositivo", "0");
			sd.put("Canal", "01");
			sd.put("pos_entry_mode", "000");
			sd.put("service_restriction_code", "000");
			sd.put("Entidad", "0000");
			sd.put("Identificador_Terminal", "0");
			sd.put("Indicador_Efectivo_Cheque", modalidadPago);
			
							
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
