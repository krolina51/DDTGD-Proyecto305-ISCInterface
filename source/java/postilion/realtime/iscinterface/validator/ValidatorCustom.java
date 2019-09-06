package postilion.realtime.iscinterface.validator;

import postilion.realtime.sdk.message.IValidator;
import postilion.realtime.sdk.message.Validator;

public final class ValidatorCustom extends Validator {

	private static IValidator VAL_ANSC = new ValidatorAlphaNumericSpecialControl();

	public static IValidator getAnsc() {
		return VAL_ANSC;
	}

}