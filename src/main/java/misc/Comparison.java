package misc;


import java.math.BigDecimal;

public enum Comparison {
	EQUAL,
	UNEQUAL,
	GREATER,
	GREATER_THAN_EQUAL,
	LESSER,
	LESSER_THAN_EQUAL;

	/**
	 * Evaluates the current comparison operation against the specified numbers.
	 * @param number1 Number to compare on the left hand side of the operator.
	 * @param number2 Number to compare on the right hand side of the operator.
	 * @return Result of the comparison operation.
	 */
	public boolean evaluateComparison(Number number1, Number number2) {
		boolean result = false;
		BigDecimal num1 = new BigDecimal(number1.doubleValue());
		BigDecimal num2 = new BigDecimal(number2.doubleValue());

		switch(this) {
			case EQUAL:
			case GREATER_THAN_EQUAL:
			case LESSER_THAN_EQUAL:
				result = (num1.compareTo(num2) == 0);
		}
		if(this == Comparison.LESSER || this == Comparison.LESSER_THAN_EQUAL) {
			result |= (num1.compareTo(num2) < 0);
		}
		else if(this == Comparison.GREATER || this == Comparison.GREATER_THAN_EQUAL) {
			result |= (num1.compareTo(num2) > 0);
		}
		return result;
	}
}
