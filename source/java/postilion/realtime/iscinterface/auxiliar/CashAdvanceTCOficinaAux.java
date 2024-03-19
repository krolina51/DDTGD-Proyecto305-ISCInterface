package postilion.realtime.iscinterface.auxiliar;

import java.text.SimpleDateFormat;
import java.util.Date;

import postilion.realtime.genericinterface.eventrecorder.events.TryCatchException;
import postilion.realtime.iscinterface.ISCInterfaceCB;
import postilion.realtime.iscinterface.crypto.Crypto;
import postilion.realtime.iscinterface.crypto.PinPad;
import postilion.realtime.iscinterface.database.DBHandler;
import postilion.realtime.iscinterface.message.ISCReqInMsg;
import postilion.realtime.iscinterface.util.Client;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.model.TransactionSetting;
import postilion.realtime.sdk.crypto.CryptoCfgManager;
import postilion.realtime.sdk.crypto.CryptoManager;
import postilion.realtime.sdk.crypto.DesKwp;
import postilion.realtime.sdk.crypto.XCrypto;
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
	public static int pos9f27,pos95,pos9f26,pos9f02,pos9f03,pos82,pos9f36,pos9f1a,pos5f2a,pos9a,pos9c,pos9f37,pos9f10;
	public static int pos9f1e,pos9f33,pos9f35,pos9f09,pos9f34,pos84;
	public static int pos9f39,pos5f34;
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
	public static final int CLASE_PAGO_INI = 146; // 2-Normal
	public static final int CLASE_PAGO_FIN = 146 + 1;
	public static final int DIA_FECHA_APLICACION_DEL_PAGO_INI = 147;
	public static final int DIA_FECHA_APLICACION_DEL_PAGO_FIN = 147 + 2;
	public static final int MES_FECHA_APLICACION_DEL_PAGO_INI = 149;
	public static final int MES_FECHA_APLICACION_DEL_PAGO_FIN = 149 + 2;
	public static final int CODIGO_ENTIDAD_QUE_AUTORIZA_EL_DEBITO_INI = 160;
	public static final int CODIGO_ENTIDAD_QUE_AUTORIZA_EL_DEBITO_FIN = 160 + 4;
	public static final int NUMERO_DE_CUOTAS_INI = 173;
	public static final int NUMERO_DE_CUOTAS_FIN = 173 + 2;
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
	public static final int SERIAL_INI = 277;
	public static final int SERIAL_FIN = 277 + 10;
	public static final int PINBLOCK_INI = 314;
	public static final int PINBLOCK_FIN = 314 + 16;
	public static final int DATA_EMV_INI = 332;
	public static final int DATA_EMV_FIN = 332 + 499; 
	
	public static final String CODIGO_ENTIDAD_BANCO_DE_BOGOTA = "0001";
	public static final String ENRUTAR_A_FIRSTDATA = "100";
	public static final String ENRUTAR_A_ATH_CONEXION_OFICINAS = "50";
	public static final String MENSAJE_BASE24_FORMATO_ATM = "1";
	public static final String MENSAJE_BASE24_FORMATO_POS = "2";

	public static final String INITIAL_SPACE = "   ";

	public Iso8583Post processMsg(Iso8583Post out, ISCReqInMsg in, TransactionSetting tSetting, String cons, boolean enableMonitor, boolean isNextDay) throws XPostilion 
	{
		String tramaCompletaAscii = INITIAL_SPACE + Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString()));

		try {
			BusinessCalendar objectBusinessCalendar = new BusinessCalendar("DefaultBusinessCalendar");
			Date businessCalendarDate = null;
			String settlementDate = null;

			Logger.logLine("Reflected:\n" + in.toString(), enableMonitor);

			StructuredData sd = null;
			StructuredData sdOriginal = new StructuredData();

			if (out.getStructuredData() != null) 
			{
				sd = out.getStructuredData();
			} 
			else {
				sd = new StructuredData();
			}

			if (tramaCompletaAscii.substring(BYTES_ESTADO_INI, BYTES_ESTADO_FIN).matches("^((040)|(050)|(060)|(070))")) 
			{
				businessCalendarDate = objectBusinessCalendar.getNextBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			} 
			else {
				businessCalendarDate = objectBusinessCalendar.getCurrentBusinessDate();
				settlementDate = new SimpleDateFormat("MMdd").format(businessCalendarDate);
			}

			String p12 = new DateTime().get("HHmmss");
			String p13 = new DateTime().get("MMdd");
			String codigoOficina = tramaCompletaAscii.substring(COD_OFICINA_INI, COD_OFICINA_FIN);
			String numeroSecuencia = tramaCompletaAscii.substring(NUM_SEQ_TX_ACTUAL_INI, NUM_SEQ_TX_ACTUAL_FIN);
			String numeroSecuenciaOriginalAReversar = tramaCompletaAscii.substring(NUM_SEQ_TX_ORIG_A_REVERSAR_INI, NUM_SEQ_TX_ORIG_A_REVERSAR_FIN);
			String tipoMensaje = tramaCompletaAscii.substring(BYTES_ESTADO_INI, BYTES_ESTADO_FIN);
			/*
			 * Para la variable tipoMensaje, va a tener alguno de los siguientes valores:
			 * 000 = Normal 
			 * 010 = Devolución 
			 * 020 = Anulación 
			 * 030 = Anulación de devolución
			 * 040 = Transacción Next Day 
			 * 050 = Devolución Next Day 
			 * 060 = Anulación NextDay 
			 * 070 = Anulación Devolución NextDay
			 * 080 = Reverso 
			 */
			
			String keyReverse = null;
			String numeroIdentificacion = tramaCompletaAscii.substring(NUMERO_IDENTIFICACION_INI, NUMERO_IDENTIFICACION_FIN);
			String cuentaAAcreditar = tramaCompletaAscii.substring(NUMERO_CUENTA_ACREDITAR_INI, NUMERO_CUENTA_ACREDITAR_FIN);
			String cuentaADebitar = tramaCompletaAscii.substring(NUMERO_CUENTA_DEBITADA_INI, NUMERO_CUENTA_DEBITADA_FIN);
			String identificadorPinpad = tramaCompletaAscii.substring(IDENTIFICADOR_PINPAD_INI, IDENTIFICADOR_PINPAD_FIN);
			String identificacionCanal = "OF";
			String modalidadPago = tramaCompletaAscii.substring(MODALIDAD_PAGO_INI, MODALIDAD_PAGO_FIN); 
			String naturalezaPago = tramaCompletaAscii.substring(NATURALEZA_PAGO_INI, NATURALEZA_PAGO_FIN); 
			/*
			 * VALORES POSIBLES PARA LA NATURALEZA DEL PAGO: 
			 * 0: CargoCuenta
			 * 1: Efectivo
			 * 2: Cheque
			 * 5: Pago
			 * 6: Avance TC
			 */
			
			String clasePagoCredito = tramaCompletaAscii.substring(CLASE_PAGO_INI, CLASE_PAGO_FIN);
			String secuenciaTS = tramaCompletaAscii.substring(SECUENCIA_TS_INI, SECUENCIA_TS_FIN);
			String codigoEntidadAutorizadoraDebito = tramaCompletaAscii.substring(CODIGO_ENTIDAD_QUE_AUTORIZA_EL_DEBITO_INI, CODIGO_ENTIDAD_QUE_AUTORIZA_EL_DEBITO_FIN);
			String numeroCuotas = tramaCompletaAscii.substring(NUMERO_DE_CUOTAS_INI, NUMERO_DE_CUOTAS_FIN);
			String codigoEntidadAdquiriente = CODIGO_ENTIDAD_BANCO_DE_BOGOTA;
			String codigoOficinaAdquiriente = tramaCompletaAscii.substring(COD_OFICINA_ADQUIRIENTE_INI, COD_OFICINA_ADQUIRIENTE_FIN);
			String base24Field41 = codigoEntidadAdquiriente.concat(codigoOficinaAdquiriente).concat("00003   ");
			String p41 = base24Field41.substring(0, 8);
			String codigoDaneCiudadOficinaAdquiriente = tramaCompletaAscii.substring(CODIGO_DANE_OFICINA_ADQUIRIENTE_INI, CODIGO_DANE_OFICINA_ADQUIRIENTE_FIN);
			String nombreOficinaAdquiriente = tramaCompletaAscii.substring(NOMBRE_OFICINA_ADQUIRIENTE_INI, NOMBRE_OFICINA_ADQUIRIENTE_FIN);
			String tipoCuentaDebito = tramaCompletaAscii.substring(TIPO_CUENTA_DEBITADA_INI, TIPO_CUENTA_DEBITADA_FIN);
			String track2 = tramaCompletaAscii.substring(TRACK2_INI, TRACK2_FIN);
			String fechaVencimiento = track2.substring(17, 21);
			String serialPinpad = tramaCompletaAscii.substring(SERIAL_INI, SERIAL_FIN);
			String pinBlock = tramaCompletaAscii.substring(PINBLOCK_INI, PINBLOCK_FIN);
			String p37 = "0901".concat(codigoOficinaAdquiriente).concat(numeroSecuencia);
			String key = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
			String key420 = "0420".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
			String seqNr = tramaCompletaAscii.substring(NUM_SEQ_TX_ACTUAL_INI, NUM_SEQ_TX_ACTUAL_FIN);
			String seqNrReverse = tramaCompletaAscii.substring(NUM_SEQ_TX_ORIG_A_REVERSAR_INI, NUM_SEQ_TX_ORIG_A_REVERSAR_FIN);
			String binTarjeta = "007701000000000000";
			String dataEmv = tramaCompletaAscii.substring(DATA_EMV_INI);

			/****************************** VALIDACIÓN DE TITULARIDAD ******************************/
			String msgFromValidationTC = "";
			//Validacion Titularidad, trae informacion nombre e identificacion del tarjetahabiente solo para tarjeta Bco Bta
			if( codigoEntidadAutorizadoraDebito.equals(CODIGO_ENTIDAD_BANCO_DE_BOGOTA) ) {
				Client udpClientValidation = new Client(ISCInterfaceCB.ipServerValidation, ISCInterfaceCB.portServerValidation);				
				msgFromValidationTC = udpClientValidation.sendMsgForValidationTitular(cuentaADebitar, enableMonitor);
			}
			/****************************** FIN DE VALIDACIÓN DE TITULARIDAD ******************************/

			String p22 = "010";
			/* Para transacciones con tarjeta presente, se usa formato ATM. En caso contrario, formato POS. */
			String formatoMensajeBase24 = MENSAJE_BASE24_FORMATO_POS;
			if (codigoOficina.startsWith("4") && identificadorPinpad.equals("PP")) 
			{
				p22 = "051";
				formatoMensajeBase24 = MENSAJE_BASE24_FORMATO_ATM;
			}

			String franquicia = " ";
			String token_BM = "";
			String token_03 = "";
			String token_24 = "";
			String token_B4 = "";
			String token_QT = "";
			
			// -------------------Lógica Tokens First Data formato POS - Inicio ---------------------
			if (formatoMensajeBase24.equals(MENSAJE_BASE24_FORMATO_POS)) // Si formato mensaje es POS
			{
				// Token BM
				token_BM = "! BM00036 Q401501030      00000000000000000000";

				// Token C0
				String token_C0 = "! C000026                   2";

				if (cuentaAAcreditar.startsWith("5")) 
				{
					franquicia = "M";
				}
				
				String fld_present = "0";
				if (identificadorPinpad.equals("PP")) // Tarjeta presente 
				{
					fld_present = "1";
				}

				token_C0 = token_C0.concat(franquicia).concat(" ").concat(fld_present).concat("    ");
			}	
			// -------------------Logica Tokens First Data formato POS - Fin ---------------------
			
			// -------------------Logica Tokens First Data formato ATM - Inicio ---------------------
			else {	
				
				// Token 03
				String cardVerifyFlag = "  ";

				if (formatoMensajeBase24.equals(MENSAJE_BASE24_FORMATO_ATM)) // Si formato mensaje es ATM
					cardVerifyFlag = " Y";
				else // Si formato mensaje es POS
					cardVerifyFlag = "PN";

				token_03 = "! 0300006 01".concat(cardVerifyFlag).concat("00");

				// Token 24
				if (cuentaADebitar.startsWith("4"))
					franquicia = "V "; // Visa
				else
					franquicia = "M "; // Mastercard

				token_24 = "! 2400010 ".concat(franquicia).concat("A0").concat(fechaVencimiento).concat("0 ");

				// Token B4
				token_B4 = "! B400020 ".concat(p22).concat("51000000000100520");

				// Token BM
				token_BM = "";
				if (naturalezaPago.equals("6")) // Si es avance
					token_BM = "! BM00036 Q101013000      00000000000000000000";

				// Token QT
				token_QT = "! QT00032 013000".concat("0000").concat(numeroIdentificacion.substring(4, 12)).concat("00000000000000");
			}
			// -------------------Logica Tokens First Data formato ATM - Fin ---------------------

			// ***********************************************************************************************************************
			// TRANSLATE PIN
			String newPin = "                ";
			if (!pinBlock.equals("                ")) {
				try {
					Crypto crypto = new Crypto(enableMonitor);
					PinPad pinpad = new PinPad();
					String pan = track2.substring(0, 16);
					CryptoCfgManager crypcfgman = CryptoManager.getStaticConfiguration();
					
					DesKwp kwpAth;
					if(codigoEntidadAutorizadoraDebito.equals(CODIGO_ENTIDAD_BANCO_DE_BOGOTA))
					{
						kwpAth = crypcfgman.getKwp("KPE_FIRST_DATA");
					}
					else {
						kwpAth = crypcfgman.getKwp("ATH_KPE_OFI");
					}
					Logger.logLine("kwp:" + kwpAth.getName(), enableMonitor);

					pinpad = (PinPad) ISCInterfaceCB.pinpadData.get(codigoOficinaAdquiriente + serialPinpad);
					Logger.logLine("kwp:" + kwpAth.getName(), enableMonitor);
					if (pinpad == null) {
						ISCInterfaceCB.pinpadData.clear();
						ISCInterfaceCB.pinpadData = DBHandler.loadPinPadKeys();
						pinpad = (PinPad) ISCInterfaceCB.pinpadData.get(codigoOficinaAdquiriente + serialPinpad);
					}
					if (pinpad == null || pinpad.getKey_exc() == null) {
						sd.put("ERROR", "PINPAD NO INICIALIZADO O SIN LLAVE DE INTERCAMBIO");
					} else {
						Logger.logLine("pinpad.getKey_exc():" + pinpad.getKey_exc(), enableMonitor);
						Logger.logLine("kwpAth.getValueUnderKsk():" + kwpAth.getValueUnderKsk(), enableMonitor);
						Logger.logLine("encPinBlock:" + pinBlock, enableMonitor);
						Logger.logLine("pan:" + pan, enableMonitor);
						newPin = crypto.translatePin(pinpad.getKey_exc(), kwpAth.getValueUnderKsk(), pinBlock, pan,
								enableMonitor);
						if (newPin.equals("FFFFFFFFFFFFFFFF")) {
							// 127.22 TAG ERROR DE CRIPTOGRAFÍA
							//sd.put("SANITY_ERROR", "ERROR TRANSLATE PIN");	// Habilitar esta línea cuando finalicemos ANULACIÓN.
							Logger.logLine("ERROR TRANSLATE PIN", enableMonitor);
							newPin = pinBlock;
							newPin = "FFFFFFFFFFFFFFFF";	// eliminar esta línea después de pruebas internas.
						}
					}

					Logger.logLine("newPin:" + newPin, enableMonitor);
				} catch (XCrypto e) {
					sd.put("ERROR", "ERROR CRIPTOGRAFIA");
					Logger.logLine("KWP ERROR: " + e.toString(), enableMonitor);
					EventRecorder.recordEvent(new Exception(e.toString()));
				}
			}
			// FIN DE TRANSLATE PIN
			// ***********************************************************************************************************************

			// CAMPO 3 PROCESSING CODE
			out.putField(Iso8583.Bit._003_PROCESSING_CODE, "013000");
			// CAMPO 7 TRANSMISSION DATE N TIME
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
			out.putField(Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOC,
					codigoOficinaAdquiriente.concat(nombreOficinaAdquiriente)
					.concat(codigoDaneCiudadOficinaAdquiriente)
					.concat("        BO CO"));
			// CAMPO 49
			out.putField(Iso8583Post.Bit._049_CURRENCY_CODE_TRAN, "170"); // "170" indica pesos colombianos.
			// CAMPO 52
			out.putField(Iso8583Post.Bit._052_PIN_DATA, "FFFFFFFF");
			// CAMPO 59
			out.putField(Iso8583Post.Bit._059_ECHO_DATA, numeroSecuencia);
			// TRACK2 Field 98
			out.putField(Iso8583.Bit._098_PAYEE, "0054150070650000000000000");

			
			if (codigoEntidadAutorizadoraDebito.equals(CODIGO_ENTIDAD_BANCO_DE_BOGOTA) 
					&& codigoOficina.startsWith("4")
					&& identificadorPinpad.equals("PP")) 
			{
				sd.put("indicator_product", MENSAJE_BASE24_FORMATO_ATM); // INDICADOR DE MENSAJE BASE24 EN FORMATO "ATM"
				sd.put("CHANNEL_PCODE", "OFICINA_ATM"); // INDICADOR DE TRANSACCIÓN EN OFICINA. DEBE COINCIDIR CON LO QUE VA ANTES DEL PROCESSING_CODE EN LA LLAVE EN EL ARCHIVO JsonHashPrd.json

				/*
				 * ISO8583 DEBE TENER LOS SIGUIENTES CAMPOS: 
				 * 3, 4 json, 7, 11 json, 12, 13, 15, 22, 25 json, 26 json, 32 json, 35, 37, 41 json, 42 json, 43, 49 json, 52 json, 59, 98 json, 100, 123 json.
				 */

				// CAMPO 100 PARA ENRUTAMIENTO
				out.putField(Iso8583.Bit._100_RECEIVING_INST_ID_CODE, ENRUTAR_A_FIRSTDATA); // Enruta a FirstData

				/*
				 * BASE24 DEBE TENER LOS SIGUIENTES CAMPOS: 
				 * 3, 4, 7, 11, 12, 13, 17, 18, 22, 32, 35, 37, 41, 43, 48, 49, 52, 60, 61, 100, 126. 
				 * Entonces, en el archivo "JsonHashPrd.json" configurar para borrado del mensaje ISO8583Post los campos simples: 
				 * 15, 25, 26, 42, 98, 123
				 */

				// 127.22 TAG B24_Field_17
				sd.put("B24_Field_17", settlementDate);
				// 127.22 TAG B24_Field_18
				sd.put("B24_Field_18", "6010");
				// 127.22 TAG B24_Field_35
				sd.put("B24_Field_35", track2.substring(0, 37));
				// 127.22 TAG B24_Field_41
				sd.put("B24_Field_41", base24Field41);
				// CAMPO 48
				sd.put("B24_Field_48", "1                       10000017000000000000");
				// 127.22 TAG B24_Field_52
				sd.put("B24_Field_52", newPin);
				// 127.22 TAG B24_Field_60
				sd.put("B24_Field_60", "P801P8010000");
				// 127.22 TAG B24_Field_61
				sd.put("B24_Field_61", "000100000000000");
				// 127.22 TAG B24_Field_100 RECV_INST_ID
				sd.put("B24_Field_100", "10000000001");
				// 127.22 TAG B24_Field_126 longitud total: 166 Tokens: 03, 24, B4, BM, QT (ojo: token QT varía entre mensajes 0200 y 0210)
				sd.put("B24_Field_126", "& 0000600166".concat(token_03).concat(token_24).concat(token_B4).concat(token_BM).concat(token_QT));
				// 127.22 TAG ERROR
				if (msgFromValidationTC.startsWith("NO")) 
					sd.put("ERROR", "TARJETA NO EXISTE");
				
				sd.put("SEC_ACCOUNT_TYPE", "   ");

				// PROCESAMIENTO DE REVERSO
				/*
				 * ISO8583post DEBE TENER LOS SIGUIENTES CAMPOS: 3, 4, 7, 11, 12, 13, 22, 25,
				 * 41, 42, 43, 49,
				 */
				/*
				 * BASE24 DEBE TENER LOS SIGUIENTES CAMPOS: 3, 4, 7, 11, 12, 13, 15, 17, 18, 22,
				 * 32, 35, 37, 38, 39, 41, 43, 48, 49, 52, 60, 61, 90, 100, 126.
				 */
				

			} // FIN LÓGICA PARA TARJETAS BBOG (FIRSTDATA)
			
			// INICIO DE LÓGICA PARA TARJETAS AVAL (ATH)
			else { 
				sd.put("indicator_product", MENSAJE_BASE24_FORMATO_POS); // INDICADOR DE MENSAJE BASE24 EN FORMATO "POS".
				sd.put("CHANNEL_PCODE", "OFICINA_POS"); // INDICADOR DE TRANSACCIÓN EN OFICINA.

				/*
				 * ISO8583 DEBE TENER LOS SIGUIENTES CAMPOS: 
				 * 3, 4 json, 7, 11 json, 12, 13, 15, 22, 25 json, 26 json, 32 json, 35, 37, 41 json, 42 json, 43, 49 json, 52 json, 59, 98 json, 100, 123 json.
				 */

				// CAMPO 100 PARA ENRUTAMIENTO
				out.putField(Iso8583.Bit._100_RECEIVING_INST_ID_CODE, ENRUTAR_A_ATH_CONEXION_OFICINAS); // Enruta a ATH

				/*
				 * BASE24 DEBE TENER LOS SIGUIENTES CAMPOS: 3, 4, 7, 11, 12, 13, 15, 17, 32, 35, 37, 41, 42, 43, 48, 49, 52, 102, 104, 126. 
				 * Entonces, en el archivo "JsonHashPrd.json" configurar para borrado del mensaje ISO8583Post los campos simples: 
				 * 22, 25, 26, 42, 59, 98, 100, 123
				 */

				// 127.22 TAG B24_Field_17
				sd.put("B24_Field_17", settlementDate);
				// 127.22 TAG B24_Field_35
				sd.put("B24_Field_35", track2.substring(0, 37));
				// 127.22 TAG B24_Field_41
				sd.put("B24_Field_41", base24Field41);
				// 127.22 TAG B24_Field_42
				sd.put("B24_Field_42", numeroCuotas.concat("0000000000000"));
				// CAMPO 48
				sd.put("B24_Field_48", "000000000000               ");
				// 127.22 TAG B24_Field_52
				sd.put("B24_Field_52", pinBlock);
				// 127.22 TAG B24_Field_102
				sd.put("B24_Field_102", "00".concat(track2.substring(0, 16)));
				// 127.22 TAG B24_Field_104
				sd.put("B24_Field_104", "00".concat(numeroIdentificacion));
				
				// Extracción TLV para que ATH realice validación de ARQC
				String decoded = dataEmv;
				sd.put("decoded", decoded);
				
				String parts[] = decoded.split("\\+");
				
				for (int i=0; i<parts.length; i++) {
					if(parts[i].startsWith("9F27"))
						pos9f27 = i;
					if(parts[i].startsWith("95"))
						pos95 = i;
					if(parts[i].startsWith("9F26"))
						pos9f26 = i;
					if(parts[i].startsWith("9F02"))
						pos9f02 = i;
					if(parts[i].startsWith("9F03"))
						pos9f03 = i;
					if(parts[i].startsWith("82"))
						pos82 = i;
					if(parts[i].startsWith("9F36"))
						pos9f36 = i;
					if(parts[i].startsWith("9F1A"))
						pos9f1a = i;
					if(parts[i].startsWith("5F2A"))
						pos5f2a = i;
					if(parts[i].startsWith("9A"))
						pos9a = i;
					if(parts[i].startsWith("9C"))
						pos9c = i;
					if(parts[i].startsWith("9F37"))
						pos9f37 = i;
					if(parts[i].startsWith("9F10"))
						pos9f10 = i;
					if(parts[i].startsWith("9F1E"))
						pos9f1e = i;
					if(parts[i].startsWith("9F33"))
						pos9f33 = i;
					if(parts[i].startsWith("9F35"))
						pos9f35 = i;
					if(parts[i].startsWith("9F09"))
						pos9f09 = i;
					if(parts[i].startsWith("9F34"))
						pos9f34 = i;
					if(parts[i].startsWith("84"))
						pos84 = i;
					if(parts[i].startsWith("9F39"))
						pos9f39 = i;
					if(parts[i].startsWith("5F34"))
						pos5f34 = i;
				}
				
				String s126 = constructField126(parts);
				sd.put("B24_Field_126", s126);
				
			} // FIN DE LÓGICA PARA TARJETAS AVAL (ATH)
			
			// INICIO DE LÓGICA DE REVERSO O ANULACIÓN
			if (tipoMensaje.equals("080") // Reverso
				|| tipoMensaje.equals("020") // Anulación
			) {
				/*Logger.logLine("\nInicio de lógica de reverso\n", enableMonitor);
				sdOriginal = DBHandler.getKeyOriginalTxBySeqNr(numeroSecuenciaOriginalAReversar); //sdOriginal = DBHandler.getKeyOriginalTxBySeqNr(seqNrReverse);
				Logger.logLine("sdOriginal:\t" + sdOriginal + "\n", enableMonitor);
				keyReverse = sdOriginal.get("KeyOriginalTx");
				Logger.logLine("keyReverse:\t" + keyReverse + "\n", enableMonitor);
				String keyAnulacion = "0200".concat(p37).concat(p13).concat(p12).concat("00").concat(settlementDate);
				Logger.logLine("keyAnulacion:\t" + keyAnulacion + "\n", enableMonitor);
				if (keyReverse == null) {
					keyReverse = "0000000000";
					sd.put("REV_DECLINED", "TRUE");
				} else {
					out.putField(Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS, Pack.resize(keyReverse, 42, '0', true));
					out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key420);out.putPrivField(Iso8583Post.PrivBit._011_ORIGINAL_KEY, keyReverse);
					out.setMessageType(Iso8583.MsgTypeStr._0420_ACQUIRER_REV_ADV);
					sd.put("B24_Field_95", "000000000000000000000000000000000000000000");
					sd.put("KEY_REVERSE", keyReverse);
					sd.put("B24_Field_90", keyReverse + "0000000000");
					out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, keyReverse.substring(4,16));
					sd.put("B24_Field_37", keyReverse.substring(4,16));
					
					if (tipoMensaje.equals("020")) {
						sd.put("ANULACION", "TRUE");
						sd.put("B24_Field_15", settlementDate);
						sd.put("B24_Field_38", sdOriginal.get("Autorizacion_Original"));
						sd.put("KeyOriginalTx", keyReverse);
						sd.put("B24_Field_52", "0000000000000000");
						sd.put("B24_Field_54", "000".concat(sdOriginal.get("Monto_Original")).concat("000000000000000000").concat(sdOriginal.get("Monto_Original")));
						out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, keyAnulacion);
						token_QT = "! QT00032 01300000000000000000000000000000";
						sd.put("B24_Field_126", "& 0000600166".concat(token_03).concat(token_24).concat(token_B4).concat(token_BM).concat(token_QT));
					}
				}*/
				
				sdOriginal = DBHandler.getKeyOriginalTxBySeqNr(seqNrReverse);
				keyReverse = sdOriginal.get("KeyOriginalTx");
				if(keyReverse == null) {
					keyReverse = "0000000000";
					sd.put("REV_DECLINED", "TRUE");
				}
				out.putField(Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS, Pack.resize(keyReverse, 42, '0', true));
				sd.put("B24_Field_52", "0000000000000000");
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, "0420".concat(Transform.fromEbcdicToAscii(Transform.fromHexToBin(in.getTotalHexString().substring(248, 268)))).concat("0"+cons.substring(2, 5)));
				out.putPrivField(Iso8583Post.PrivBit._011_ORIGINAL_KEY, keyReverse);
				out.setMessageType(Iso8583.MsgTypeStr._0420_ACQUIRER_REV_ADV);
				sd.put("B24_Field_95", "000000000000000000000000000000000000000000");
				sd.put("B24_Field_90", keyReverse+"0000000000");
				out.putField(Iso8583.Bit._037_RETRIEVAL_REF_NR, keyReverse.substring(4,16));
				sd.put("B24_Field_37", keyReverse.substring(4,16));
				sd.put("B24_Field_15", settlementDate);
				sd.put("B24_Field_38", "000000");
				sd.put("B24_Field_39", "17");	// Validar en AUTRA si este valor debe ir así.
				token_QT = "! QT00032 01300000000000000000000000000000";
				sd.put("B24_Field_126", "& 0000600166".concat(token_03).concat(token_24).concat(token_B4).concat(token_BM).concat(token_QT));

			} // FIN DE PROCESAMIENTO DE REVERSO
			else {
				// 127.2 SWITCHKEY
				out.putPrivField(Iso8583Post.PrivBit._002_SWITCH_KEY, key);
				ISCInterfaceCB.cacheKeyReverseMap.put(numeroSecuencia, key);
			}

			sd.put("Entidad_autoriza_debito", codigoEntidadAutorizadoraDebito);

			// 127.22 TAG MSG_HEXA
			sd.put("IN_MSG", in.getTotalHexString());
			// 127.22 TAG Entidad Autorizadora
			sd.put("ENT_AUT", codigoEntidadAutorizadoraDebito);

			////////// TAGS EXTRACT

			sd.put("VIEW_ROUTER", "V2");
			sd.put("Codigo_FI_Origen", "1019");
			sd.put("Nombre_FI_Origen", "CIC");
			sd.put("Ind_4xmil", "1");
			sd.put("Tarjeta_Amparada", cuentaADebitar);
			sd.put("Codigo_Transaccion_Producto", "02");
			sd.put("Tipo_de_Cuenta_Debitada", "CRE");
			sd.put("CLIENT_CARD_NR_1", binTarjeta);
			sd.put("PAN_Tarjeta", cuentaADebitar.substring(0, 16).concat("   "));
			sd.put("Numero_Secuencia", "00" + numeroSecuencia);
			sd.put("Numero_de_Recibo_de_Terminal", "00" + numeroSecuencia);
			sd.put("Ofi_Adqui", codigoOficinaAdquiriente);
			if( tipoMensaje.equals("080") // Reverso
					|| tipoMensaje.equals("020"))	// Anulación 
			{
				sd.put("Codigo_Transaccion", "39");
				sd.put("Nombre_Transaccion", "REVDEB");
				sd.put("Codigo_de_Autorizacion", "0000000000");
				sd.put("Indicador_Autorizacion", "0");
			}
			else {
				sd.put("Codigo_Transaccion", "20");
				sd.put("Nombre_Transaccion", "AVANCE");
			}
			sd.put("FI_CREDITO", "0000");
			sd.put("FI_DEBITO", "0000");
			sd.put("SEC_ACCOUNT_TYPE", "   ");
			sd.put("Codigo_de_Red", "1019");
			sd.put("Numero_Terminal", codigoOficinaAdquiriente);
			sd.put("Nombre_Establecimiento_QR", codigoOficinaAdquiriente.concat(nombreOficinaAdquiriente));
			sd.put("Identificacion_Canal", identificacionCanal);
			sd.put("Codigo_Establecimiento", "          ");
			sd.put("TITULAR_TC",msgFromValidationTC);
			sd.put("TRANSACTION_INPUT", "AVANCE_TC_CANALVIRTUAL"); // DEFINIR TEXTO A CONFIGURAR PARA AVANCE TC EN OFICINA.
			sd.put("Indicador_AVAL", "1");
			sd.put("SECUENCIA_REQ", secuenciaTS);
			sd.put("Mod_Credito", naturalezaPago);
			sd.put("Clase_Pago", clasePagoCredito);
			sd.put("Ent_Adq", CODIGO_ENTIDAD_BANCO_DE_BOGOTA);
			sd.put("Numero_de_cuotas_AV", numeroCuotas);
			sd.put("Canal", "01");
			sd.put("pos_entry_mode", "000");
			sd.put("service_restriction_code", "000");
			sd.put("Entidad", "0000");
			sd.put("Identificador_Terminal", "0");
			sd.put("Indicador_Efectivo_Cheque", modalidadPago);

			if (codigoEntidadAutorizadoraDebito.equals(CODIGO_ENTIDAD_BANCO_DE_BOGOTA)) // Si es Banco de Bogotá
			{
				sd.put("PRIM_ACCOUNT_NR", Pack.resize(cuentaADebitar.substring(6, 16), 18, '0', false));
				sd.put("SEC_ACCOUNT_NR_PAGOTC", "000000000000000000");
				sd.put("Vencimiento", fechaVencimiento);
				sd.put("Dispositivo", "B");
				sd.put("BIN_Cuenta", cuentaADebitar.substring(0, 6));
			} else // Si es Aval
			{
				sd.put("PRIM_ACCOUNT_NR", cuentaADebitar);
				sd.put("SEC_ACCOUNT_NR_PAGOTC", cuentaAAcreditar);
				sd.put("Vencimiento", "0000");
				sd.put("Dispositivo", "D");
				sd.put("BIN_Cuenta", "000000");
			}

			if (!msgFromValidationTC.startsWith("NO") && codigoEntidadAutorizadoraDebito.equals(CODIGO_ENTIDAD_BANCO_DE_BOGOTA))
			{
				sd.put("CUSTOMER_NAME", msgFromValidationTC.substring(3,29));	
				sd.put("ID_CLIENT", msgFromValidationTC.substring(36,49));
			}
			else
			{
				sd.put("CUSTOMER_NAME", "0000000000000000000000000000");
				sd.put("ID_CLIENT", "0000000000000");
			}

			
			//sd.put("forzar_delay", "true"); // INDICADOR PARA FORZAR UN DELAY. BORRAR ESTA LÍNEA DESPUÉS DE HACER PRUEBA INTERNA.

			out.putStructuredData(sd);

		} catch (Exception e) {
			EventRecorder.recordEvent(e);
			EventRecorder.recordEvent(new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n", Utils.getStringMessageException(e) }));
			e.printStackTrace();
			e.toString();
		}

		return out;
	}
	
	public static String constructField126(String[] tokens) {
		StringBuilder sb = new StringBuilder();
		sb.append(constructTokenQT(tokens));
		sb.append(constructTokenB2(tokens));
		sb.append(constructTokenB3(tokens));
		sb.append(constructTokenB4(tokens));
		return sb.toString();
	}
	
	public static String constructTokenQT(String[] tokens) {
		return "& 0000500342! QT00032 0110000000000000000000000000000 ";
	}
	
	public static String constructTokenB2(String[] tokens) {
		StringBuilder b2 = new StringBuilder();
		try {
			b2.append("! B200158 7FF90000");
			b2.append(tokens[pos9f27].substring(6,8)); //9f27
			b2.append(tokens[pos95].substring(4,14)); //95
			b2.append(tokens[pos9f26].substring(6,22)); //9f26
			b2.append(tokens[pos9f02].substring(6,18)); //9f02
			b2.append(tokens[pos9f03].substring(6,18)); //9f03
			b2.append(tokens[pos82].substring(4,8)); //82
			b2.append(tokens[pos9f36].substring(6,10)); //9f36
			b2.append(tokens[pos9f1a].substring(7,10)); //9f1a
			b2.append(tokens[pos5f2a].substring(7,10)); //5f2a
			b2.append(tokens[pos9a].substring(4,10)); //9a
			b2.append(tokens[pos9c].substring(4,6)); //9c
			b2.append(tokens[pos9f37].substring(6,14)); //9f37
			b2.append(tokens[pos9f10].length()>15 ? "00" : "000").append(Pack.resize(tokens[pos9f10].substring(4), 66, '0', true)); //9f10
		}catch(Exception e) {
			e.printStackTrace();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		
		return b2.toString();
	}
	
	public static String constructTokenB3(String[] tokens) {
		StringBuilder b3 = new StringBuilder();
		
		try {
			b3.append("! B300080 CF00");
			b3.append(Transform.fromHexToBin(tokens[pos9f1e].substring(6,22))); //9f1e
			b3.append(tokens[pos9f33].substring(6,12)); //9f33
			b3.append("00000000000000").append(tokens[pos9f35].substring(6,8)); //9f35
			b3.append(tokens[pos9f09].substring(6,10)); //9f09
			b3.append(tokens[pos9f34].substring(6,12)); //9f34
			b3.append("00").append(tokens[pos84].substring(2,18)); //84-4f
			b3.append("000000000000000000");
		}catch(Exception e) {
			e.printStackTrace();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		
		return b3.toString();
	}
	
	public static String constructTokenB4(String[] tokens) {
		StringBuilder b4 = new StringBuilder();
		
		try {
			b4.append("! B400020 ");
			b4.append(tokens[pos9f39].substring(6,8)); //9f39
			b4.append("151");
			b4.append(tokens[pos5f34].substring(6,8)); //5f34
			b4.append("0000000000000");
		}catch(Exception e) {
			e.printStackTrace();
			EventRecorder.recordEvent(
					new Exception("ERROR processMsg: " + e.toString()));
			EventRecorder.recordEvent(new TryCatchException(new String[] { "ISCInterCB-IN", "ISCInterfaceCB",
					"Method :[" + "processMsg" + "]\n" + "processMsg: " + "\n",
					Utils.getStringMessageException(e) }));
		}
		
		return b4.toString();
	}

}
