package bank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// Use of Parameterized helps in this case, since multiple runs of same test are required
class FeesCalculatorTest {
	private static final double DELTA = 0.0001;

	FeesCalculator calculator = new FeesCalculator();

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void withdrawalTest() {
		assertEquals(0.2, calculator.calculateWithdrawalFee(200, 1000, false, 0));		//pass
		//assertEquals(0.01, calculator.calculateWithdrawalFee(200, 1000, false, 0));	//fail
	}

	// Spec: Assignment 1, "Deposit" fee rules. Technique: Weak Robust Equivalence Class Analysis.
	// Test-case IDs match docs/hai_assignment1.md.
	// Valid classes: one per rule of the deposit fee specification (D-01 to D-08),
	// each tested with a value from the middle of the class.
	@ParameterizedTest(name = "{0}: student={1}, amount={2}, balance={3} -> fee={4}")
	@CsvSource({
		"D-01, true, 100, 1000, 0.5",		// student, amount > 50, balance > 500: 0.5%
		"D-02, true, 100, 200, 0.25",		// student, amount > 50, balance <= 500: 0.25%
		"D-03, true, 20, 8000, 0.1",		// student, amount <= 50, balance > 5000: 0.5%
		"D-04, true, 20, 200, 0.0",			// student, amount <= 50, balance <= 5000: no fee
		"D-05, false, 1000, 5000, 8.0",		// non-student, amount > 250, balance > 2500: 0.8%
		"D-06, false, 1000, 1000, 4.0",		// non-student, amount > 250, balance <= 2500: 0.4%
		"D-07, false, 100, 20000, 0.0",		// non-student, amount <= 250, balance > 10000: no fee
		"D-08, false, 100, 1000, 0.1",		// non-student, amount <= 250, balance <= 10000: 0.1%
	})
	void validDepositTest(String id, boolean student, double amount, double balance, double fee) {
		assertEquals(fee, calculator.calculateDepositInterest(amount, balance, student), DELTA);
	}

	// Invalid classes, one invalid input per test (D-09, D-10). The specification does
	// not define the outcome; assumed to be an IllegalArgumentException.
	@ParameterizedTest(name = "{0}: student={1}, amount={2}, balance={3} is rejected")
	@CsvSource({
		"D-09, false, -100, 1000",			// amount <= 0
		"D-10, false, 100, -100",			// balance < 0
	})
	void invalidDepositTest(String id, boolean student, double amount, double balance) {
		assertThrows(IllegalArgumentException.class,
				() -> calculator.calculateDepositInterest(amount, balance, student));
	}

}
