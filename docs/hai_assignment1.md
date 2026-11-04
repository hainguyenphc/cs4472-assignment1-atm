# Assignment 1 - Deposit and Transfer Fees (`bank.utils.FeesCalculator`)

**Author:** Hai
**JUnit class:** `src/test/java/bank/utils/FeesCalculatorTest.java`

| Part | Method under test | Technique | JUnit test methods |
|---|---|---|---|
| 1 - Deposit | `calculateDepositInterest(double amount, double accountBalance, boolean student)` | Weak Robust Equivalence Class Analysis | `validDepositTest`, `invalidDepositTest` |
| 2 - Transfer | `calculateTransferFee(double amount, double fromAccountBalance, double toAccountBalance, boolean student)` | Decision Table Analysis | `transferTest` |

# Part 1 - Deposit

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

# Part 2 - Transfer

## My part of the assignment

I performed black-box testing of the fee calculation for the **transfer** transaction. The specification
defines the fee as a percentage of the amount transferred, depending on four inputs: whether the client is a
student, the amount transferred, the balance of the account the money is coming from (*from balance*), and
the balance of the account the money is going to (*to balance*).

| Client | Amount transferred | From balance | To balance | Fee |
|---|---|---|---|---|
| Student | less than $200 | less than $2,000 | less than $1,000 | 0.1% |
| Student | less than $200 | less than $2,000 | $1,000 or more | 0.05% |
| Student | less than $200 | $2,000 or more | less than $1,000 | 0.05% |
| Student | less than $200 | $2,000 or more | $1,000 or more | 0.025% |
| Student | $200 or more | less than $2,000 | less than $1,000 | 0.05% |
| Student | $200 or more | less than $2,000 | $1,000 or more | 0.025% |
| Student | $200 or more | $2,000 or more | less than $1,000 | 0.25% |
| Student | $200 or more | $2,000 or more | $1,000 or more | 0.125% |
| Not a student | less than $100 | less than $4,000 | less than $2,000 | 0.2% |
| Not a student | less than $100 | less than $4,000 | $2,000 or more | 0.1% |
| Not a student | less than $100 | $4,000 or more | less than $2,000 | 1% |
| Not a student | less than $100 | $4,000 or more | $2,000 or more | 0.5% |
| Not a student | $100 or more | less than $2,000 | less than $1,000 | 0.2% |
| Not a student | $100 or more | less than $2,000 | $1,000 or more | 0.1% |
| Not a student | $100 or more | $2,000 or more | less than $1,000 | 0.5% |
| Not a student | $100 or more | $2,000 or more | $1,000 or more | 0.25% |

## Section 1 - Process and assumptions

### Process

A decision table lists the **conditions** of the specification (the questions it asks about the input), the
**actions** (the outcome, here the fee rate), and the **rules**: each rule is one combination of condition
values together with the action the specification prescribes for it. Every rule becomes one test case.

The transfer specification is a nested sequence of four yes/no decisions, so I used a **limited-entry**
decision table with four binary conditions:

| Condition | Meaning |
|---|---|
| C1 | The client is a student. |
| C2 | The amount is *small*: less than $200 for a student, less than $100 for a non-student. |
| C3 | The from balance is *low*: less than $2,000, except for a non-student with a small amount, where it is less than $4,000. |
| C4 | The to balance is *low*: less than $1,000, except for a non-student with a small amount, where it is less than $2,000. |

The thresholds of C2, C3 and C4 depend on the earlier conditions, so they are defined per group as above
rather than as fixed numbers. With four binary conditions there are 2^4 = **16 rules**. The specification
prescribes a fee for every one of them, so the table has no impossible rules and no "don't care" entries,
and none of the rules can be merged (each pair of neighbouring rules has a different action, or differs in
C1 or C2, which change the thresholds). The table is therefore complete and gives **16 test cases**.

For each rule I chose input values from the **middle** of the ranges the rule requires, away from the
thresholds, because the thresholds themselves are the job of Boundary Value Analysis:

| Group | Small / large amount | Low / high from balance | Low / high to balance |
|---|---|---|---|
| Student | 100 / 500 | 1,000 / 3,000 | 500 / 1,500 |
| Non-student, small amount | 50 | 2,000 / 6,000 | 1,000 / 3,000 |
| Non-student, large amount | 500 | 1,000 / 3,000 | 500 / 1,500 |

The expected fee of each test case is the amount multiplied by the rate of its rule.

### Assumptions

- **A1 - Unit under test.** The transfer fee is computed by `FeesCalculator.calculateTransferFee`. The method
  is tested directly, without the database or the ATM.
- **A2 - Output.** The method returns the fee in dollars (fee percentage × amount transferred), not the
  percentage.
- **A3 - Comparison.** Fees are `double` values, so they are compared with a tolerance of 0.0001.
- **A4 - "Less than" and "or more".** The thresholds are applied exactly as worded: "less than $200" means
  amount < 200, and "$200 or more" means amount ≥ 200. The same applies to every other threshold.
- **A5 - Rates as written.** For a student transferring $200 or more from a balance of $2,000 or more, the
  specification gives 0.25% and 0.125%, which is higher than every other student rate and breaks the
  pattern of the rest of the table (it may be a typo for 0.025% and 0.0125%). Black-box tests follow the
  specification, so the rates are used as written.
- **A6 - Invalid input.** Decision Table Analysis covers the rules the specification defines. The
  specification defines no rule for invalid input (for example a negative amount or balance), so no invalid
  test cases are included for transfer.

## Section 2 - Test cases

### Decision table

| Rule | R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8 | R9 | R10 | R11 | R12 | R13 | R14 | R15 | R16 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| C1: student | T | T | T | T | T | T | T | T | F | F | F | F | F | F | F | F |
| C2: small amount | T | T | T | T | F | F | F | F | T | T | T | T | F | F | F | F |
| C3: low from balance | T | T | F | F | T | T | F | F | T | T | F | F | T | T | F | F |
| C4: low to balance | T | F | T | F | T | F | T | F | T | F | T | F | T | F | T | F |
| **Fee rate** | 0.1% | 0.05% | 0.05% | 0.025% | 0.05% | 0.025% | 0.25% | 0.125% | 0.2% | 0.1% | 1% | 0.5% | 0.2% | 0.1% | 0.5% | 0.25% |

### Test cases

| ID | Rule | Student | Amount | From balance | To balance | Rate | Expected fee |
|---|---|---|---|---|---|---|---|
| T-01 | R1 | true | 100 | 1,000 | 500 | 0.1% | 0.10 |
| T-02 | R2 | true | 100 | 1,000 | 1,500 | 0.05% | 0.05 |
| T-03 | R3 | true | 100 | 3,000 | 500 | 0.05% | 0.05 |
| T-04 | R4 | true | 100 | 3,000 | 1,500 | 0.025% | 0.025 |
| T-05 | R5 | true | 500 | 1,000 | 500 | 0.05% | 0.25 |
| T-06 | R6 | true | 500 | 1,000 | 1,500 | 0.025% | 0.125 |
| T-07 | R7 | true | 500 | 3,000 | 500 | 0.25% | 1.25 |
| T-08 | R8 | true | 500 | 3,000 | 1,500 | 0.125% | 0.625 |
| T-09 | R9 | false | 50 | 2,000 | 1,000 | 0.2% | 0.10 |
| T-10 | R10 | false | 50 | 2,000 | 3,000 | 0.1% | 0.05 |
| T-11 | R11 | false | 50 | 6,000 | 1,000 | 1% | 0.50 |
| T-12 | R12 | false | 50 | 6,000 | 3,000 | 0.5% | 0.25 |
| T-13 | R13 | false | 500 | 1,000 | 500 | 0.2% | 1.00 |
| T-14 | R14 | false | 500 | 1,000 | 1,500 | 0.1% | 0.50 |
| T-15 | R15 | false | 500 | 3,000 | 500 | 0.5% | 2.50 |
| T-16 | R16 | false | 500 | 3,000 | 1,500 | 0.25% | 1.25 |

## Test results

Results from `mvn -B test` (also shown in the CI run on the pull request).

| ID | Input (student, amount, from, to) | Expected fee | Actual fee | Result |
|---|---|---|---|---|
| T-01 | true, 100, 1000, 500 | 0.10 | 2.50 | Fail |
| T-02 | true, 100, 1000, 1500 | 0.05 | 1.25 | Fail |
| T-03 | true, 100, 3000, 500 | 0.05 | 2.50 | Fail |
| T-04 | true, 100, 3000, 1500 | 0.025 | 1.25 | Fail |
| T-05 | true, 500, 1000, 500 | 0.25 | 12.50 | Fail |
| T-06 | true, 500, 1000, 1500 | 0.125 | 6.25 | Fail |
| T-07 | true, 500, 3000, 500 | 1.25 | 12.50 | Fail |
| T-08 | true, 500, 3000, 1500 | 0.625 | 6.25 | Fail |
| T-09 | false, 50, 2000, 1000 | 0.10 | 2.50 | Fail |
| T-10 | false, 50, 2000, 3000 | 0.05 | 2.50 | Fail |
| T-11 | false, 50, 6000, 1000 | 0.50 | 2.50 | Fail |
| T-12 | false, 50, 6000, 3000 | 0.25 | 2.50 | Fail |
| T-13 | false, 500, 1000, 500 | 1.00 | 25.00 | Fail |
| T-14 | false, 500, 1000, 1500 | 0.50 | 27.50 | Fail |
| T-15 | false, 500, 3000, 500 | 2.50 | 25.00 | Fail |
| T-16 | false, 500, 3000, 1500 | 1.25 | 27.50 | Fail |

**Summary:** 0 of 16 test cases passed, 16 failed.

## Fault localization

Each entry below is one suspected fault. All line numbers refer to
`src/main/java/bank/utils/FeesCalculator.java`, method `calculateTransferFee`. Several test cases pass through
more than one faulty statement (a wrong condition sends them to a branch that also has a wrong rate). Each
test case is listed under the **first** faulty statement on its path, which is the one it exposes; the
faulty rate it then reaches is noted in the entry.

### TF-1: Student amount threshold is $100 instead of $200

- **Failing test cases:** T-01, T-02, T-03, T-04
- **Location:** line 80: `if (amount < 100.00)`
- **Expected (specification):** a student transfer of $100 is "less than $200", so rules R1-R4 apply (fees
  0.10, 0.05, 0.05 and 0.025).
- **Observed:** 2.50, 1.25, 2.50 and 1.25, the results of the "$200 or more" branch.
- **Justification:** $100 is not less than 100, so all four test cases take the large-amount branch (lines
  94-108). T-01 and T-02 then also meet TF-2, and all four end at the wrong rates of TF-3 and TF-4.

### TF-2: Student from-balance threshold (amount $200 or more) is $1,000 instead of $2,000

- **Failing test cases:** T-05, T-06
- **Location:** line 95: `if (fromAccountBalance < 1000.00)`
- **Expected (specification):** a from balance of $1,000 is "less than $2,000", so the fee is 0.05% (0.25)
  or 0.025% (0.125).
- **Observed:** 12.50 and 6.25, the results of the "$2,000 or more" branch.
- **Justification:** $1,000 is not less than 1,000, so both test cases take the high-balance branch (lines
  101-107) and reach the wrong rates of TF-3 and TF-4.

### TF-3: Student rate for a large amount, high from balance and low to balance is 2.5% instead of 0.25%

- **Failing test cases:** T-07
- **Location:** line 103: `feePercentage = 0.025;`
- **Expected (specification):** 0.25% of $500 = 1.25.
- **Observed:** 12.50, i.e. 2.5% of $500.
- **Justification:** the branch is reached correctly, but it assigns 0.025 instead of 0.0025 (ten times too
  large).

### TF-4: Student rate for a large amount, high from balance and high to balance is 1.25% instead of 0.125%

- **Failing test cases:** T-08
- **Location:** line 105: `feePercentage = 0.0125;`
- **Expected (specification):** 0.125% of $500 = 0.625.
- **Observed:** 6.25, i.e. 1.25% of $500.
- **Justification:** the branch is reached correctly, but it assigns 0.0125 instead of 0.00125 (ten times too
  large).

### TF-5: Non-student from-balance threshold (amount less than $100) is $1,000 instead of $4,000

- **Failing test cases:** T-09, T-10
- **Location:** line 111: `if (fromAccountBalance < 1000.00)`
- **Expected (specification):** a from balance of $2,000 is "less than $4,000", so the fee is 0.2% (0.10) or
  0.1% (0.05).
- **Observed:** 2.50 for both, the result of the "$4,000 or more" branch.
- **Justification:** $2,000 is not less than 1,000, so both test cases take the high-balance branch (lines
  117-123). T-09 then also meets TF-6, and both end at the wrong rate of TF-7.

### TF-6: Non-student to-balance threshold (amount less than $100) is $1,000 instead of $2,000

- **Failing test cases:** T-11
- **Location:** line 118: `if (toAccountBalance < 1000.00)`
- **Expected (specification):** a to balance of $1,000 is "less than $2,000", so the fee is 1% of $50 = 0.50.
- **Observed:** 2.50, the result of the "$2,000 or more" branch.
- **Justification:** $1,000 is not less than 1,000, so the test case takes the `else` branch at line 121 and
  reaches the wrong rate of TF-7.

### TF-7: Non-student rate for a small amount, high from balance and high to balance is 5% instead of 0.5%

- **Failing test cases:** T-12
- **Location:** line 121: `feePercentage = 0.05;`
- **Expected (specification):** 0.5% of $50 = 0.25.
- **Observed:** 2.50, i.e. 5% of $50.
- **Justification:** the branch is reached correctly, but it assigns 0.05 instead of 0.005 (ten times too
  large).

### TF-8: Non-student from-balance threshold (amount $100 or more) is $1,000 instead of $2,000

- **Failing test cases:** T-13, T-14
- **Location:** line 125: `if (fromAccountBalance < 1000.00)`
- **Expected (specification):** a from balance of $1,000 is "less than $2,000", so the fee is 0.2% (1.00) or
  0.1% (0.50).
- **Observed:** 25.00 and 27.50, the results of the "$2,000 or more" branch.
- **Justification:** $1,000 is not less than 1,000, so both test cases take the high-balance branch (lines
  131-137) and reach the wrong rates of TF-9 and TF-10.

### TF-9: Non-student rate for a large amount, high from balance and low to balance is 5% instead of 0.5%

- **Failing test cases:** T-15
- **Location:** line 133: `feePercentage = 0.05;`
- **Expected (specification):** 0.5% of $500 = 2.50.
- **Observed:** 25.00, i.e. 5% of $500.
- **Justification:** the branch is reached correctly, but it assigns 0.05 instead of 0.005 (ten times too
  large).

### TF-10: Non-student rate for a large amount, high from balance and high to balance is 5.5% instead of 0.25%

- **Failing test cases:** T-16
- **Location:** line 135: `feePercentage = 0.055;`
- **Expected (specification):** 0.25% of $500 = 1.25.
- **Observed:** 27.50, i.e. 5.5% of $500.
- **Justification:** the branch is reached correctly, but it assigns 0.055 instead of 0.0025.

Matching rows for the fault-localization table in the PR description:

| Failing test | Suspected fault (file, method, line) | One-line justification |
|---|---|---|
| T-01, T-02, T-03, T-04 | FeesCalculator.java, calculateTransferFee, line 80 | The student amount threshold is `amount < 100` instead of 200, so a $100 transfer takes the large-amount branch. |
| T-05, T-06 | FeesCalculator.java, calculateTransferFee, line 95 | The student from-balance threshold is `fromAccountBalance < 1000` instead of 2,000, so a $1,000 balance takes the high-balance branch. |
| T-07 | FeesCalculator.java, calculateTransferFee, line 103 | The rate is 0.025 (2.5%) instead of 0.0025 (0.25%). |
| T-08 | FeesCalculator.java, calculateTransferFee, line 105 | The rate is 0.0125 (1.25%) instead of 0.00125 (0.125%). |
| T-09, T-10 | FeesCalculator.java, calculateTransferFee, line 111 | The non-student small-amount from-balance threshold is `fromAccountBalance < 1000` instead of 4,000, so a $2,000 balance takes the high-balance branch. |
| T-11 | FeesCalculator.java, calculateTransferFee, line 118 | The non-student small-amount to-balance threshold is `toAccountBalance < 1000` instead of 2,000, so a $1,000 balance takes the high-balance branch. |
| T-12 | FeesCalculator.java, calculateTransferFee, line 121 | The rate is 0.05 (5%) instead of 0.005 (0.5%). |
| T-13, T-14 | FeesCalculator.java, calculateTransferFee, line 125 | The non-student large-amount from-balance threshold is `fromAccountBalance < 1000` instead of 2,000, so a $1,000 balance takes the high-balance branch. |
| T-15 | FeesCalculator.java, calculateTransferFee, line 133 | The rate is 0.05 (5%) instead of 0.005 (0.5%). |
| T-16 | FeesCalculator.java, calculateTransferFee, line 135 | The rate is 0.055 (5.5%) instead of 0.0025 (0.25%). |

### Suspected faults masked by the faults above

Because of TF-1, TF-2, TF-5 and TF-8, no test case reaches the student small-amount branch (lines 81-92),
the student low-from-balance branch (lines 96-100), or the non-student low-from-balance branches (lines
112-116 and 126-130). The statements below sit in those branches and do not match the specification. They
would likely surface once the threshold faults are fixed.

- **M-1:** line 83: rate 0.01 (1%) should be 0.001 (0.1%).
- **M-2:** line 85: rate 0.005 (0.5%) should be 0.0005 (0.05%).
- **M-3:** line 81: `fromAccountBalance < 1000.00` should be 2,000.
- **M-4:** line 89: rate 0.05 (5%) should be 0.0005 (0.05%).
- **M-5:** line 91: rate 0.025 (2.5%) should be 0.00025 (0.025%).
- **M-6:** line 97: rate 0.005 (0.5%) should be 0.0005 (0.05%).
- **M-7:** line 99: rate 0.0025 (0.25%) should be 0.00025 (0.025%).
- **M-8:** line 112: `toAccountBalance < 1000.00` should be 2,000 (same fault as TF-6, in the low-from-balance branch).
- **M-9:** line 113: rate 0.02 (2%) should be 0.002 (0.2%).
- **M-10:** line 115: rate 0.01 (1%) should be 0.001 (0.1%).
- **M-11:** line 127: rate 0.01 (1%) should be 0.002 (0.2%).
- **M-12:** line 129: rate 0.005 (0.5%) should be 0.001 (0.1%).
