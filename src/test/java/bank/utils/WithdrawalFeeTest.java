package bank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// Spec: Assignment 1, "Withdrawal" fee rules. Technique: Robust Worst Case BVA.
// Test-case IDs (W-1 to W-216) match docs/assignment1.md, Appendix A.
class WithdrawalFeeTest {
    private static final double DELTA = 0.000000001;
    private static final double[] AMOUNTS = { 0.01, 0.02, 100.00 };

    // Starting balance and expected non student rate
    private static final double[][] BALANCE_CASES = {
            { 0.00, 0.003 },
            { 0.01, 0.003 },
            { 999.99, 0.003 },
            { 1000.00, 0.001 },
            { 1000.01, 0.001 },
            { 2500.00, 0.001 },
            { 4999.99, 0.001 },
            { 5000.00, 0.0 },
            { 5000.01, 0.0 }
    };

    private static final int[] DAYS = {
            Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY
    };

    private final FeesCalculator calculator = new FeesCalculator();

    // Cross the valid boundary and nominal inputs
    @ParameterizedTest(name = "W-{index} student={0}, amount={1}, balance={2}, day={3}, fee={4}")
    @MethodSource("validCases")
    void calculatesSpecifiedFee(
            boolean student, double amount, double balance, int day, double expectedFee) {
        double actualFee = calculator.calculateWithdrawalFee(amount, balance, student, day);
        assertEquals(expectedFee, actualFee, DELTA);
    }

    static Stream<Arguments> validCases() {
        Stream.Builder<Arguments> cases = Stream.builder();
        for (boolean student : new boolean[] { true, false }) {
            for (double amount : AMOUNTS) {
                for (double[] balanceCase : BALANCE_CASES) {
                    for (int day : DAYS) {
                        boolean weekend = day == Calendar.SATURDAY || day == Calendar.SUNDAY;
                        double studentRate = weekend ? 0.001 : 0.0;
                        double rate = student ? studentRate : balanceCase[1];
                        cases.add(Arguments.of(student, amount, balanceCase[0], day, amount * rate));
                    }
                }
            }
        }
        return cases.build();
    }
}
