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

	// Spec: Assignment 1, "Transfer" fee rules. Technique: Decision Table Analysis.
	// Test-case IDs match docs/hai_assignment1.md. One test case per rule (R1 to R16).
	// "Small" amount: < 200 (student), < 100 (non-student).
	// "Low" from balance: < 2000 (student, and non-student amount >= 100), < 4000 (non-student amount < 100).
	// "Low" to balance: < 1000 (student, and non-student amount >= 100), < 2000 (non-student amount < 100).
	@ParameterizedTest(name = "{0}: student={1}, amount={2}, from={3}, to={4} -> fee={5}")
	@CsvSource({
		"T-01, true, 100, 1000, 500, 0.1",			// R1: student, small, low from, low to: 0.1%
		"T-02, true, 100, 1000, 1500, 0.05",		// R2: student, small, low from, high to: 0.05%
		"T-03, true, 100, 3000, 500, 0.05",			// R3: student, small, high from, low to: 0.05%
		"T-04, true, 100, 3000, 1500, 0.025",		// R4: student, small, high from, high to: 0.025%
		"T-05, true, 500, 1000, 500, 0.25",			// R5: student, large, low from, low to: 0.05%
		"T-06, true, 500, 1000, 1500, 0.125",		// R6: student, large, low from, high to: 0.025%
		"T-07, true, 500, 3000, 500, 1.25",			// R7: student, large, high from, low to: 0.25%
		"T-08, true, 500, 3000, 1500, 0.625",		// R8: student, large, high from, high to: 0.125%
		"T-09, false, 50, 2000, 1000, 0.1",			// R9: non-student, small, low from, low to: 0.2%
		"T-10, false, 50, 2000, 3000, 0.05",		// R10: non-student, small, low from, high to: 0.1%
		"T-11, false, 50, 6000, 1000, 0.5",			// R11: non-student, small, high from, low to: 1%
		"T-12, false, 50, 6000, 3000, 0.25",		// R12: non-student, small, high from, high to: 0.5%
		"T-13, false, 500, 1000, 500, 1.0",			// R13: non-student, large, low from, low to: 0.2%
		"T-14, false, 500, 1000, 1500, 0.5",		// R14: non-student, large, low from, high to: 0.1%
		"T-15, false, 500, 3000, 500, 2.5",			// R15: non-student, large, high from, low to: 0.5%
		"T-16, false, 500, 3000, 1500, 1.25",		// R16: non-student, large, high from, high to: 0.25%
	})
	void transferTest(String id, boolean student, double amount, double from, double to, double fee) {
		assertEquals(fee, calculator.calculateTransferFee(amount, from, to, student), DELTA);
	}

}
