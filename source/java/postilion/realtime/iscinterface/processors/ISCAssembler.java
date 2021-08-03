package postilion.realtime.iscinterface.processors;

import postilion.realtime.iscinterface.ISCInterfaceCB;
import postilion.realtime.iscinterface.message.ISCReqMessage;
import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.iscinterface.util.Utils;
import postilion.realtime.iscinterface.web.Http2CustClient;
import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Transform;

public class ISCAssembler {
	
	/**************************************************************************************
	 * Construye StreamMessage de prueba con la estructura ISCReqMessage el mismo
	 * podrá ser enviado a la entidad remota para efectos de prueba
	 * 
	 * @return
	 * @throws Exception 
	 **************************************************************************************/
	public static ISCReqMessage createISCMessage(Iso8583Post inputMsg, String hour4Check, boolean isNextDay, boolean localCovVal, ISCInterfaceCB instance) throws Exception {
		Logger.logLine("Mapeando campos de ISO a ISC", false);
		
		//Http2CustClient c = new Http2CustClient();
		//Logger.logLine("[ISCAssembler][createISCMessage][Http2 return]" + c.sendIsoMsg2IscMsg(inputMsg));
		String tranType = Utils.getInternalTranType(inputMsg, localCovVal);
		ISCReqMessage output = null;
		
		Logger.logLine("[ISCAssembler][Line 29] TRAN TYPE: " + tranType, false);
		
		switch (tranType) {
		case Utils.TT_WITHDRAWAL:
		case Utils.TT_REVERSE:
		case Utils.TT_REP_REVERSE:
		case Utils.TT_PAYMENT_CB_MIXT:
		case Utils.TT_PAYMENT_CB_DEBIT:
		case Utils.TT_PAYMENT_CB_CREDIT:
		case Utils.TT_REV_PAYMENT_CB_MIXT:
		case Utils.TT_REV_PAYMENT_CB_DEBIT:
		case Utils.TT_REV_PAYMENT_CB_CREDIT:
		case Utils.TT_REV_REP_PAYMENT_CB_MIXT:
		case Utils.TT_REV_REP_PAYMENT_CB_DEBIT:
		case Utils.TT_REV_REP_PAYMENT_CB_CREDIT:
		case Utils.TT_WITHDRAWAL_CB_ATTF:
		case Utils.TT_WITHDRAWAL_CB_ATTC:
		case Utils.TT_WITHDRAWAL_CB_ATTD:
		case Utils.TT_PAYMENT_OBLIG_CB_CREDIT:
		case Utils.TT_PAYMENT_OBLIG_CB_MIXT:
		case Utils.TT_PAYMENT_OBLIG_CB_DEBIT:
		case Utils.TT_REVERSE_CB_ATTF:
		case Utils.TT_REVERSE_CB_ATTC:
		case Utils.TT_REVERSE_CB_ATTD:
		case Utils.TT_REP_REVERSE_CB_ATTF:
		case Utils.TT_REP_REVERSE_CB_ATTC:
		case Utils.TT_REP_REVERSE_CB_ATTD:
		case Utils.TT_REVERSE_GNS:
		case Utils.TT_REP_REVERSE_GNS:
		case Utils.TT_COST_INQUIRY:
		case Utils.TT_BALANCE_INQUIRY_CB:
		case Utils.TT_GOOD_N_SERVICES:
		case Utils.TT_TRANSFER_CB_ATTF:
		case Utils.TT_TRANSFER_CB_ATTD:
		case Utils.TT_TRANSFER_CB_ATTC:
		case Utils.TT_REV_TRANSFER_CB_ATTD:
		case Utils.TT_REV_TRANSFER_CB_ATTC:
		case Utils.TT_REV_TRANSFER_CB_ATTF:
		case Utils.TT_REV_REP_TRANSFER_CB_ATTD:
		case Utils.TT_REV_REP_TRANSFER_CB_ATTC:
		case Utils.TT_REV_REP_TRANSFER_CB_ATTF:
		case Utils.TT_DEPOSIT_CB_ATTD:
		case Utils.TT_DEPOSIT_CB_ATTC:
		case Utils.TT_DEPOSIT_CB_ATTF:
		case Utils.TT_REV_DEPOSIT_CB_ATTD:
		case Utils.TT_REV_DEPOSIT_CB_ATTC:
		case Utils.TT_REV_DEPOSIT_CB_ATTF:
		case Utils.TT_REV_REP_DEPOSIT_CB_ATTD:
		case Utils.TT_REV_REP_DEPOSIT_CB_ATTC:
		case Utils.TT_REV_REP_DEPOSIT_CB_ATTF:
		case Utils.TT_CARD_PAYMENT:
		case Utils.TT_MORTGAGE_PAYMENT:
			
			output = Utils.prepareMessageHeader(inputMsg, tranType, hour4Check, isNextDay, instance);
			
			output.putField(ISCReqMessage.Fields._VARIABLE_BODY,
					Utils.prepareVariableReqBody(inputMsg, tranType));

			Logger.logLine("DATA ISC REQUEST MSG ISC:\n" + Transform.fromBinToHex(output.getTotalString())
					+ "\n===========================\n===========================\n", false);
			
			break;
		case "05":
			
			output = Utils.prepareMessageHeader(inputMsg, tranType, hour4Check, isNextDay, instance);
			output.putField(ISCReqMessage.Fields._VARIABLE_BODY,
					"COVENENT_NOT_FOUND");
			
			break;		
		case "05_1":
			
			output = Utils.prepareMessageHeader(inputMsg, tranType, hour4Check, isNextDay, instance);
			output.putField(ISCReqMessage.Fields._VARIABLE_BODY,
					"NOT_ON_US_COVENANT");
			
			break;
		case "00":
			Logger.logLine("APROBADO BATCH ", false);
			output = Utils.prepareMessageHeader(inputMsg, tranType, hour4Check, isNextDay, instance);
			output.putField(ISCReqMessage.Fields._VARIABLE_BODY,
					"BATCH_APPROVED");
			
			break;
		default:
			Logger.logLine("ERROR TRANSACCION \"" + tranType +"\" NO MAPEABLE", false);
			output = Utils.prepareMessageHeader(inputMsg, tranType, hour4Check, isNextDay, instance);
			
			output.putField(ISCReqMessage.Fields._VARIABLE_BODY,
					"TRAN_NOT_ALLOWED");
			break;
		}

		return output;
	}
	
	
	public static final String _SEQ_TERMINAL = "SEQ_TERMINAL";

}
