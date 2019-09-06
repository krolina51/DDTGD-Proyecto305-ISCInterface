package postilion.realtime.iscinterface.validator;

import postilion.realtime.iscinterface.util.Logger;
import postilion.realtime.sdk.message.Field;
import postilion.realtime.sdk.message.IValidator;

public class ValidatorAlphaNumericSpecialControl implements IValidator {

	private static final boolean[] invalid = new boolean[256];

	public final String describe() {
		return "ans ";
	}

	public final boolean isValid(Field field) {
		return isValid(field.data, 0, field.data.length);
	}

	public static final boolean isValid(byte[] data, int offset, int length) {
		/*for (int i = 0; i < length; i++) {
			int val = data[(offset++)];
			if (val < 0)
				val += 256;
			if (invalid[val] != false) {
				return false;
			}
		}*/
		return true;
	}

	static {
		for (int i = 0; i < 32; i++) {
			invalid[i] = true;
		}

		for (int i = 32; i < 256; i++) {
			invalid[i] = false;
		}
	}

}
