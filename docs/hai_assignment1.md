# Assignment 1 - Deposit Fees (`bank.utils.FeesCalculator`)

**Author:** Hai
**Testing target:** `bank.utils.FeesCalculator`, method `calculateDepositInterest(double amount, double accountBalance, boolean student)`
**Technique:** Weak Robust Equivalence Class Analysis
**JUnit class:** `src/test/java/bank/utils/FeesCalculatorTest.java` (`validDepositTest`, `invalidDepositTest`)

## My part of the assignment

I performed black-box testing of the fee calculation for the **deposit** transaction. The specification
defines the fee as a percentage of the amount deposited, depending on three inputs: whether the client is a
student, the amount deposited, and the balance of the account.

| Client | Amount deposited | Account balance | Fee |
|---|---|---|---|
| Student | more than $50 | more than $500 | 0.5% of the amount |
| Student | more than $50 | $500 or less | 0.25% of the amount |
| Student | $50 or less | more than $5,000 | 0.5% of the amount |
| Student | $50 or less | $5,000 or less | no fee |
| Not a student | more than $250 | more than $2,500 | 0.8% of the amount |
| Not a student | more than $250 | $2,500 or less | 0.4% of the amount |
| Not a student | $250 or less | more than $10,000 | no fee |
| Not a student | $250 or less | $10,000 or less | 0.1% of the amount |

## Section 1 - Process and assumptions

### Process

Equivalence Class Analysis divides the input domain into classes whose members the specification treats the
same way, so that one representative value per class stands for the whole class. *Weak* means every class is
covered by at least one test case, rather than every combination of classes. *Robust* adds the invalid
classes: each invalid class gets its own test case, in which exactly one input is invalid and all other inputs
are valid.

The amount and balance thresholds are not fixed: they depend on whether the client is a student, and the
balance threshold also depends on the amount (for students, $500 when the amount is over $50, but $5,000
otherwise). The input variables therefore cannot be partitioned independently of each other. I derived the
classes from the specification instead, as follows:

1. **Student** (`boolean`) - two classes: student, not a student.
2. **Amount deposited** - split by the threshold that applies to the client: for a student, (0, 50] and
   (50, ∞); for a non-student, (0, 250] and (250, ∞).
3. **Account balance** - split by the threshold that applies to the client and the amount class: $500 or
   $5,000 for students, $2,500 or $10,000 for non-students.

This gives eight valid classes, one for each rule of the specification (**D-01** to **D-08**). Covering each
of them once satisfies the weak condition, and every fee outcome in the specification is checked.

The invalid classes come from the domains of the inputs:

- **Amount ≤ 0** (**D-09**): a deposit must be a positive amount of money.
- **Balance < 0** (**D-10**): the deposit rules describe non-negative balances only.
- `student` is a `boolean`, so it has no invalid class.

For each class I chose a representative from the **middle** of the class, away from the thresholds, because
testing the thresholds themselves is the job of Boundary Value Analysis, not Equivalence Class Analysis.

### Assumptions

- **A1 - Unit under test.** The deposit fee is computed by `FeesCalculator.calculateDepositInterest`, which
  `BankDeposit.perform` uses to calculate the fee it subtracts from the deposit. The method is tested
  directly, without the database or the ATM.
- **A2 - Output.** The method returns the fee in dollars (fee percentage × amount deposited), not the
  percentage.
- **A3 - Comparison.** Fees are `double` values, so they are compared with a tolerance of 0.0001.
- **A4 - "More than" and "or less".** The thresholds are applied exactly as worded: "more than $50" means
  amount > 50, and "$50 or less" means amount ≤ 50. The same applies to every other threshold.
- **A5 - Valid amount.** A valid deposit amount is greater than $0. There is no upper limit in the
  specification.
- **A6 - Valid balance.** A valid account balance is $0 or more.
- **A7 - Invalid input.** The specification does not say what happens for invalid input. I assume the
  method rejects it by throwing an `IllegalArgumentException` instead of returning a fee.
- **A8 - Representatives.** For invalid test cases, the other inputs are held at valid values: not a
  student, amount $100, balance $1,000.

## Section 2 - Test cases

### Equivalence classes

| Class | Variable | Definition | Valid / invalid |
|---|---|---|---|
| EC-1 | Student, amount, balance | student, amount > 50, balance > 500 | Valid |
| EC-2 | Student, amount, balance | student, amount > 50, 0 ≤ balance ≤ 500 | Valid |
| EC-3 | Student, amount, balance | student, 0 < amount ≤ 50, balance > 5,000 | Valid |
| EC-4 | Student, amount, balance | student, 0 < amount ≤ 50, 0 ≤ balance ≤ 5,000 | Valid |
| EC-5 | Student, amount, balance | non-student, amount > 250, balance > 2,500 | Valid |
| EC-6 | Student, amount, balance | non-student, amount > 250, 0 ≤ balance ≤ 2,500 | Valid |
| EC-7 | Student, amount, balance | non-student, 0 < amount ≤ 250, balance > 10,000 | Valid |
| EC-8 | Student, amount, balance | non-student, 0 < amount ≤ 250, 0 ≤ balance ≤ 10,000 | Valid |
| EC-9 | Amount | amount ≤ 0 | Invalid |
| EC-10 | Balance | balance < 0 | Invalid |

### Test cases

| ID | Class | Student | Amount | Balance | Expected outcome |
|---|---|---|---|---|---|
| D-01 | EC-1 | true | 100 | 1,000 | fee 0.50 (0.5%) |
| D-02 | EC-2 | true | 100 | 200 | fee 0.25 (0.25%) |
| D-03 | EC-3 | true | 20 | 8,000 | fee 0.10 (0.5%) |
| D-04 | EC-4 | true | 20 | 200 | fee 0.00 (no fee) |
| D-05 | EC-5 | false | 1,000 | 5,000 | fee 8.00 (0.8%) |
| D-06 | EC-6 | false | 1,000 | 1,000 | fee 4.00 (0.4%) |
| D-07 | EC-7 | false | 100 | 20,000 | fee 0.00 (no fee) |
| D-08 | EC-8 | false | 100 | 1,000 | fee 0.10 (0.1%) |
| D-09 | EC-9 | false | -100 | 1,000 | `IllegalArgumentException` |
| D-10 | EC-10 | false | 100 | -100 | `IllegalArgumentException` |

## Test results

Results from `mvn -B test` (also shown in the CI run on the pull request).

| ID | Input (student, amount, balance) | Expected outcome | Actual outcome | Result |
|---|---|---|---|---|
| D-01 | true, 100, 1000 | 0.50 | 0.20 | Fail |
| D-02 | true, 100, 200 | 0.25 | 0.20 | Fail |
| D-03 | true, 20, 8000 | 0.10 | 0.10 | Pass |
| D-04 | true, 20, 200 | 0.00 | 0.04 | Fail |
| D-05 | false, 1000, 5000 | 8.00 | 5.00 | Fail |
| D-06 | false, 1000, 1000 | 4.00 | 5.00 | Fail |
| D-07 | false, 100, 20000 | 0.00 | 0.50 | Fail |
| D-08 | false, 100, 1000 | 0.10 | 0.00 | Fail |
| D-09 | false, -100, 1000 | `IllegalArgumentException` | No exception, fee 0.00 returned | Fail |
| D-10 | false, 100, -100 | `IllegalArgumentException` | No exception, fee 0.00 returned | Fail |

**Summary:** 1 of 10 test cases passed, 9 failed.

## Fault localization

Each entry below is one suspected fault. Failing test cases that expose the same fault are grouped under a
single entry. All line numbers refer to `src/main/java/bank/utils/FeesCalculator.java`, method
`calculateDepositInterest`.

### F-1: Student amount threshold is $100 instead of $50

- **Failing test cases:** D-01, D-02
- **Location:** line 39: `if (amount > 100.00)`
- **Expected (specification):** a student deposit of $100 is "more than $50", so the fee is 0.5% (balance
  over $500) or 0.25% (balance $500 or less).
- **Observed:** both returned 0.20, which is the 0.2% rate of the "$50 or less" branch.
- **Justification:** with the threshold at 100, an amount of $100 is not greater than it, so both test cases
  take the "small amount" branch (lines 46-51) instead of the "large amount" branch.

### F-2: Student fee for a small deposit and low balance is 0.2% instead of no fee

- **Failing test cases:** D-04
- **Location:** line 50: `interestPercentage = 0.002;`
- **Expected (specification):** a student depositing $50 or less into an account with $5,000 or less pays no
  fee.
- **Observed:** 0.04, i.e. 0.2% of $20.
- **Justification:** the branch is reached correctly (amount 20 ≤ 100 and balance 200 ≤ 5,000), but it
  assigns 0.002 instead of 0.

### F-3: Non-student balance threshold is $5,000 instead of $2,500

- **Failing test cases:** D-05
- **Location:** line 55: `if (accountBalance > 5000.00)`
- **Expected (specification):** a balance of $5,000 is "more than $2,500", so the fee is 0.8% (8.00).
- **Observed:** 5.00, i.e. 0.5% of $1,000.
- **Justification:** with the threshold at 5,000, a balance of exactly $5,000 is not greater than it, so the
  test case falls into the `else` branch at line 58.

### F-4: Non-student fee for a large deposit and low balance is 0.5% instead of 0.4%

- **Failing test cases:** D-06
- **Location:** line 58: `interestPercentage = 0.005;`
- **Expected (specification):** 0.4% of $1,000 = 4.00.
- **Observed:** 5.00, i.e. 0.5% of $1,000.
- **Justification:** the branch is reached correctly (amount 1,000 > 500 and balance 1,000 ≤ 5,000), but it
  assigns 0.005 instead of 0.004.

### F-5: Non-student fees for a small deposit are swapped

- **Failing test cases:** D-07, D-08
- **Location:** line 62: `interestPercentage = 0.005;` and line 64: `interestPercentage = 0.0;`
- **Expected (specification):** for $250 or less, no fee when the balance is over $10,000 and 0.1%
  otherwise.
- **Observed:** D-07 (balance 20,000) returned 0.50 (0.5%) and D-08 (balance 1,000) returned 0.00.
- **Justification:** the balance check at line 61 is correct, but the "over $10,000" branch charges 0.5% and
  the "$10,000 or less" branch charges nothing, the opposite of the specification.

### F-6: Invalid input is not rejected

- **Failing test cases:** D-09, D-10
- **Location:** start of the method (line 33); there is no input validation.
- **Expected (assumption A7):** a negative amount or a negative balance throws `IllegalArgumentException`.
- **Observed:** no exception was thrown; a fee of 0.00 was returned in both cases.
- **Justification:** the method goes straight to the student/amount/balance branches without checking that
  the amount is positive and the balance is not negative.

Matching rows for the fault-localization table in the PR description:

| Failing test | Suspected fault (file, method, line) | One-line justification |
|---|---|---|
| D-01, D-02 | FeesCalculator.java, calculateDepositInterest, line 39 | The student amount threshold is `amount > 100` instead of 50, so a $100 deposit takes the small-deposit branch. |
| D-04 | FeesCalculator.java, calculateDepositInterest, line 50 | A student's small deposit with a balance of $5,000 or less is charged 0.2% instead of no fee. |
| D-05 | FeesCalculator.java, calculateDepositInterest, line 55 | The non-student balance threshold is `accountBalance > 5000` instead of 2,500, so a $5,000 balance gets the lower rate. |
| D-06 | FeesCalculator.java, calculateDepositInterest, line 58 | A non-student's large deposit with a low balance is charged 0.5% instead of 0.4%. |
| D-07, D-08 | FeesCalculator.java, calculateDepositInterest, lines 62 and 64 | The two non-student small-deposit rates are swapped: 0.5% instead of none above $10,000, and none instead of 0.1% otherwise. |
| D-09, D-10 | FeesCalculator.java, calculateDepositInterest, line 33 | The method has no input validation, so negative amounts and balances return a fee instead of being rejected. |

### Suspected faults masked or not reached by the test cases

No failing test exposes these yet, either because another fault sends the test case down a different branch,
or because no representative value falls between the wrong and the correct threshold. They would likely
surface once the faults above are fixed, or with Boundary Value Analysis on the thresholds.

- **M-1:** line 41: `accountBalance > 1000.00` should be 500. Masked by F-1 for D-01 and D-02.
- **M-2:** line 42: rate 0.01 (1%) should be 0.005 (0.5%). Masked by F-1.
- **M-3:** line 44: rate 0.005 (0.5%) should be 0.0025 (0.25%). Masked by F-1.
- **M-4:** line 54: `amount > 500.00` should be 250. Not reached: the representatives (100 and 1,000) are
  on the same side of both thresholds; an amount between $250 and $500 would expose it.
- **M-5:** line 56: rate 0.01 (1%) should be 0.008 (0.8%). Masked by F-3 for D-05.
