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

	// FeesCalculator uses java.util.Calendar: SUNDAY=1, MONDAY=2,
	// FRIDAY=6, SATURDAY=7. Test both Saturday and Sunday explicitly.
	// Rates are percentages; expected fee = amount * rate / 100.
	@ParameterizedTest(name = "{0}: student={1}, amount={2}, balance={3}, day={4}, rate={5}%")
	@CsvSource({
		"W-01, true, 200, 1000, 2, 0",       // student, weekday: no fee
		"W-02, true, 200, 1000, 7, 0.1",     // student, Saturday: 0.1%
		"W-03, true, 200, 6000, 2, 0",       // student weekday: balance does not matter
		"W-04, true, 200, 6000, 1, 0.1",     // student Sunday: balance does not matter
		"W-05, false, 200, 999.99, 2, 0.3", // non-student, balance < 1000: 0.3%
		"W-06, false, 200, 1000, 2, 0.1",   // non-student, balance = 1000: 0.1%
		"W-07, false, 200, 4999.99, 2, 0.1", // non-student, balance < 5000: 0.1%
		"W-08, false, 200, 5000, 2, 0",     // non-student, balance = 5000: no fee
		"W-09, false, 200, 7000, 6, 0",     // non-student, >= 5000: no fee, regardless of day
		"W-10, false, 200, 500, 6, 0.3",    // non-student, < 1000: 0.3%, regardless of day
		"W-11, true, 200, 1000, 6, 0",      // student, Friday: no fee
		"W-12, false, 200, 10000, 2, 0"    // non-student, balance above fee-free threshold
	})
	void withdrawalFeeTest(String id, boolean student, double amount, double balance, int day,
			double ratePercent) {
		assertEquals(amount * ratePercent / 100,
				calculator.calculateWithdrawalFee(amount, balance, student, day), DELTA, id);
	}

	// Spec: Assignment 1, "Deposit" fee rules. Technique: Weak Robust Equivalence Class Analysis.
	// Test-case IDs match docs/assignment1.md.
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

	// Boundary-value tests for strict (>) versus inclusive (<=) comparisons.
	// Includes fee-free cases exactly at the thresholds.
	@ParameterizedTest(name = "{0}: student={1}, amount={2}, balance={3}, rate={4}%")
	@CsvSource({
		"DB-01, true, 49.99, 400, 0",       // amount just below 50
		"DB-02, true, 50, 400, 0",          // amount exactly 50: no fee
		"DB-03, true, 50.01, 400, 0.25",    // amount just above 50
		"DB-04, true, 100, 499.99, 0.25",   // balance just below 500
		"DB-05, true, 100, 500, 0.25",      // balance exactly 500
		"DB-06, true, 100, 500.01, 0.5",    // balance just above 500
		"DB-07, true, 50, 4999.99, 0",     // balance just below 5000: no fee
		"DB-08, true, 50, 5000, 0",        // balance exactly 5000: no fee
		"DB-09, true, 50, 5000.01, 0.5",   // balance just above 5000
		"DB-10, false, 249.99, 1000, 0.1", // amount just below 250
		"DB-11, false, 250, 1000, 0.1",    // amount exactly 250
		"DB-12, false, 250.01, 1000, 0.4", // amount just above 250
		"DB-13, false, 300, 2499.99, 0.4", // balance just below 2500
		"DB-14, false, 300, 2500, 0.4",    // balance exactly 2500
		"DB-15, false, 300, 2500.01, 0.8", // balance just above 2500
		"DB-16, false, 250, 9999.99, 0.1", // balance just below 10000
		"DB-17, false, 250, 10000, 0.1",   // balance exactly 10000
		"DB-18, false, 250, 10000.01, 0",  // balance just above 10000: no fee
		"DB-19, true, 50.01, 5000.01, 0.5", // student, amount > 50 still uses 500 threshold
		"DB-20, false, 250.01, 10000.01, 0.8" // non-student, amount > 250 still uses 2500 threshold
	})
	void depositBoundaryTest(String id, boolean student, double amount, double balance,
			double ratePercent) {
		assertEquals(amount * ratePercent / 100,
				calculator.calculateDepositInterest(amount, balance, student), DELTA, id);
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
	// Test-case IDs match docs/assignment1.md. One test case per rule (R1 to R16).
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

	// Threshold tests: just below, exactly at, and just above each relevant cutoff.
	// The two non-student transfer size groups use DIFFERENT balance cutoffs.
	@ParameterizedTest(name = "{0}: student={1}, amount={2}, from={3}, to={4}, rate={5}%")
	@CsvSource({
		// Student amount threshold: 200
		"TB-01, true, 199.99, 1000, 500, 0.1",
		"TB-02, true, 200, 1000, 500, 0.05",
		"TB-03, true, 200.01, 1000, 500, 0.05",
		// Student, amount < 200: source threshold 2000
		"TB-04, true, 100, 1999.99, 500, 0.1",
		"TB-05, true, 100, 2000, 500, 0.05",
		"TB-06, true, 100, 2000.01, 500, 0.05",
		// Student, amount < 200: destination threshold 1000
		"TB-07, true, 100, 1000, 999.99, 0.1",
		"TB-08, true, 100, 1000, 1000, 0.05",
		"TB-09, true, 100, 1000, 1000.01, 0.05",
		// Student, amount >= 200: source threshold 2000
		"TB-10, true, 500, 1999.99, 500, 0.05",
		"TB-11, true, 500, 2000, 500, 0.25",
		"TB-12, true, 500, 2000.01, 500, 0.25",
		// Student, amount >= 200: destination threshold 1000
		"TB-13, true, 500, 3000, 999.99, 0.25",
		"TB-14, true, 500, 3000, 1000, 0.125",
		"TB-15, true, 500, 3000, 1000.01, 0.125",
		// Non-student amount threshold: 100 (changes the balance cutoffs)
		"TB-16, false, 99.99, 3000, 1500, 0.2",
		"TB-17, false, 100, 3000, 1500, 0.25",
		"TB-18, false, 100.01, 3000, 1500, 0.25",
		// Non-student, amount < 100: source threshold 4000
		"TB-19, false, 50, 3999.99, 1000, 0.2",
		"TB-20, false, 50, 4000, 1000, 1.0",
		"TB-21, false, 50, 4000.01, 1000, 1.0",
		// Non-student, amount < 100: destination threshold 2000
		"TB-22, false, 50, 1000, 1999.99, 0.2",
		"TB-23, false, 50, 1000, 2000, 0.1",
		"TB-24, false, 50, 1000, 2000.01, 0.1",
		// Non-student, amount >= 100: source threshold 2000
		"TB-25, false, 500, 1999.99, 500, 0.2",
		"TB-26, false, 500, 2000, 500, 0.5",
		"TB-27, false, 500, 2000.01, 500, 0.5",
		// Non-student, amount >= 100: destination threshold 1000
		"TB-28, false, 500, 1000, 999.99, 0.2",
		"TB-29, false, 500, 1000, 1000, 0.1",
		"TB-30, false, 500, 1000, 1000.01, 0.1"
	})
	void transferBoundaryTest(String id, boolean student, double amount, double from, double to,
			double ratePercent) {
		assertEquals(amount * ratePercent / 100,
				calculator.calculateTransferFee(amount, from, to, student), DELTA, id);
	}

}
