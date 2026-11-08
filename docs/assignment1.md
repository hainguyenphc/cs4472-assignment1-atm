# CS 4472 Assignment 1 - Black Box Testing (Group 8)

This document is the single write-up for Assignment 1. It merges the individual write-ups of every team member
(`atm.session.Session`, and the withdrawal, deposit and transfer fees of `bank.utils.FeesCalculator`) and the
team's fee fault-localization table, which was originally kept in a Word document.

| Testing target | Method under test | Technique | JUnit class / test methods | Author(s) |
|---|---|---|---|---|
| ATM session: PIN format and withdrawal amount | `ATM.checkPin`, `Session.setAmount` | Robust Worst Case BVA, single fault assumption | `atm/session/SessionTest` | Shane |
| Withdrawal fee | `FeesCalculator.calculateWithdrawalFee` | Robust Worst Case BVA | `bank/utils/WithdrawalFeeTest` (W-1 to W-216); `bank/utils/FeesCalculatorTest#withdrawalFeeTest` (W-01 to W-12) | Jared; Zack (W-01 to W-12) |
| Deposit fee | `FeesCalculator.calculateDepositInterest` | Weak Robust Equivalence Class Analysis | `bank/utils/FeesCalculatorTest#validDepositTest`, `#invalidDepositTest` (D-01 to D-10); `#depositBoundaryTest` (DB-01 to DB-20) | Hai; Zack (DB-01 to DB-20) |
| Transfer fee | `FeesCalculator.calculateTransferFee` | Decision Table Analysis | `bank/utils/FeesCalculatorTest#transferTest` (T-01 to T-16); `#transferBoundaryTest` (TB-01 to TB-30) | Hai; Zack (TB-01 to TB-30) |

All JUnit classes live under `src/test/java`. Note on IDs: the withdrawal cases in `WithdrawalFeeTest` are
numbered without padding (`W-1` to `W-216`), while the supplementary withdrawal cases in `FeesCalculatorTest`
are zero-padded (`W-01` to `W-12`). They are different test cases.

**Contents**

1. [Section 1 - Process and assumptions](#section-1---process-and-assumptions)
2. [Section 2 - Test cases](#section-2---test-cases)
3. [Test results](#test-results)
4. [Fault localization](#fault-localization)
5. [Appendix A - Executed withdrawal fee cases (`WithdrawalFeeTest`)](#appendix-a---executed-withdrawal-fee-cases-withdrawalfeetest)

---

# Section 1 - Process and assumptions

## 1.1 ATM session - PIN format and withdrawal amount (`atm.session.Session`)

### Scope

I performed black-box testing of the ATM session for the **withdrawal** transaction only. The specification
defines two kinds of invalid input for this part:

| Input | Valid when | On invalid input |
|---|---|---|
| PIN | it is exactly 5 digits | `InvalidPinFormatException` is thrown |
| Withdrawal amount | it can be dispensed in $20 and $50 bills, and does not exceed the $1,000 daily limit | `InvalidAmountException` is thrown |

The test account has a **daily limit of $1,000** and an **available balance of $5,000**. Since the balance is
larger than the daily limit, the daily limit ($1,000) is the upper boundary for a withdrawal.

### Process

Robust Worst Case Boundary Value Analysis takes seven values for each input variable: **min-**, **min**,
**min+**, **nom**, **max-**, **max**, **max+**. *Robust* adds the two values just outside the valid range
(min- and max+). *Worst case* takes the Cartesian product of the values of all variables, which gives 7^n
test cases for n variables (Lecture 2 Part 2, slides 13-17).

Worst case testing normally rejects the **single fault assumption** (Lecture 2 Part 2, slide 15). As the
instructor clarified, the two test targets, the PIN and the withdrawal amount, are tested **separately, not
together**. Each target is therefore a **single input variable** (n = 1), and the Cartesian product of one
variable is just its own seven values. Robust worst case analysis and the single fault assumption therefore
give the same test cases: each test case contains at most one invalid value, and every other input is held
at its nominal value.

The two input variables are:

1. **Withdrawal amount** (integer dollars). Valid range: $20 (the smallest amount that can be dispensed, one
   $20 bill) to $1,000 (the daily limit). The seven robust worst case values are:

   | min- | min | min+ | nom | max- | max | max+ |
   |---|---|---|---|---|---|---|
   | 19 | 20 | 21 | 500 | 999 | 1000 | 1001 |

   The values 21 (min+) and 999 (max-) lie inside the numeric range but cannot be made from $20 and $50
   bills, so the specification makes them **invalid**. The nominal value (500) was chosen so that it is
   dispensable (10 x $50).

2. **PIN** (a sequence of characters). The rule "a valid PIN is 5 digits" bounds this one variable in two
   ways, and I applied boundary values to each, changing only one at a time (nominal PIN `55555`):
   - **Length.** The valid range is degenerate (min = max = nom = 5), so the robust values collapse to
     **4** (min-), **5** (valid) and **6** (max+).
   - **Character value.** Each character must be a digit `'0'`-`'9'`. The robust values are the characters
     immediately outside that range in the character set: `'/'` (min-, just below `'0'`) and `':'` (max+,
     just above `'9'`). Together: `'/'`, `'0'`, `'1'`, `'5'` (nom), `'8'`, `'9'`, `':'`. I applied these
     values to the **boundary positions** of the PIN (position 1 and position 5) and held the interior
     positions at the nominal digit `'5'`.

   A PIN with a wrong length *and* an invalid character would contain two faults, so it is excluded.

Duplicate nominal cases were merged, giving **15 PIN cases** and **7 amount cases** (22 in total).

### Assumptions

- **A1 - Unit under test.** The console ATM session is tested through its Java API. The Swing UI
  (`MainPanel`) is replaced by a Mockito mock, since screen layout is outside the specification.
- **A2 - PIN entry point.** The PIN is entered into the session through `ATM.checkPin(char[])`, which is the
  session's PIN step: it validates the format and then hands the PIN to `Session.addPin(char[])`. PIN test
  cases therefore enter through `ATM.checkPin`.
- **A3 - Valid PIN.** A valid PIN is exactly 5 characters, each between `'0'` and `'9'` inclusive.
- **A4 - Amount is whole dollars.** `Session.setAmount` takes an `int`, so the step between boundary
  values is $1.
- **A5 - Valid withdrawal.** A valid withdrawal can be written as a sum of $20 and $50 bills and lies between
  $20 and $1,000 inclusive.
- **A6 - The account.** The code base does not store a daily limit, so the account (daily limit $1,000,
  balance $5,000) is simulated by stubbing the bank connection (`MessageDispatcher`). It accepts the
  credentials and reports a successful transaction with a $5,000 balance.
- **A7 - Nominal values.** Whenever a variable is not under test it is held at its nominal value: card
  `4000000000000000`, PIN `55555`, transaction Withdrawal, account Chequing, amount $500.
- **A8 - Independent inputs.** The PIN and the amount are entered at different steps, and the amount rule
  does not depend on the PIN value, so the two inputs are analysed separately (confirmed by the
  instructor). Amount test cases drive
  `Session` directly with the nominal PIN so that a PIN fault cannot mask the amount result.
- **A9 - Expected outcome for valid input.** For valid input the expected outcome is that no exception is
  thrown and the session moves on to the next step.
- **A10 - Credentials.** Credential checking (correct vs. incorrect PIN) is outside the scope of this part;
  the stub always accepts the credentials, so only the *format* of the PIN is tested.

## 1.2 Withdrawal fee (`FeesCalculator.calculateWithdrawalFee`)

### Scope

I tested the withdrawal fee returned by FeesCalculator.calculateWithdrawalFee. The assignment page sets student fees by day and non student fees by starting balance.

| Customer | Condition | Expected fee |
| --- | --- | --- |
| Student | Weekday | No fee |
| Student | Weekend | 0.1% of the withdrawal |
| Non-student | Balance below $1,000 | 0.3% of the withdrawal |
| Non-student | Balance at least $1,000 and below $5,000 | 0.1% of the withdrawal |
| Non-student | Balance at least $5,000 | No fee |

### Process and assumptions (`WithdrawalFeeTest`)

I selected values right just below, at, and just above the fee thresholds where needed. My test crosses all selected valid values, including balance values for students and day values for non students. It also checks that those inputs do not affect their respective fees.

| Input | Selected valid values | Additional robust boundary values |
| --- | --- | --- |
| Student | Yes, no | The boolean parameter has two possible values |
| Withdrawal amount in dollars | 0.01, 0.02, 100.00 | 0.00, immediately below the assumed minimum of 0.01 |
| Starting balance in dollars | 0.00, 0.01, 999.99, 1000.00, 1000.01, 2500.00, 4999.99, 5000.00, 5000.01 | -0.01, immediately below the assumed minimum of 0.00 |
| Calendar day | Friday (6), Saturday (7), Sunday (1), Monday (2) | 0 and 8, outside the Calendar day range |

- The fee method is tested directly with independent inputs. Positive amounts and non negative balances are assumptions for this method. The ATM Session limit does not set the upper boundary for the fee calculation.
- The step is one cent. The expected fee is the non rounded dollar amount, compared with an absolute tolerance of 0.000000001.
- Friday/Saturday and Sunday/Monday cover both directions of the weekday/weekend transition. The selection includes both weekend days.

#### JUnit test design

The assignment asked for Robust Worst Case Boundary Value Analysis, so I used JUnit 5 Method Sourcing to generate the combinations from the selected boundary values. Each combination runs as a separate parameterized invocation with its own case ID. The appendix lists every executed case, its inputs, its expected fee, and its observed result. The handout leaves the choice of JUnit argument source open.

### Supplementary boundary cases (`FeesCalculatorTest#withdrawalFeeTest`, W-01 to W-12)

These twelve cases check the withdrawal rules at a realistic amount ($200) instead of the cent-sized amounts of
`WithdrawalFeeTest`, so that every wrong rate shows up as a visible difference in the fee.

- The amount is held at $200 in every case; only the student flag, the balance and the day change.
- The balance is placed just below, exactly at, and above the $1,000 and $5,000 non-student thresholds
  (999.99, 1,000, 4,999.99, 5,000, 7,000, 10,000), plus nominal values (500, 6,000).
- `FeesCalculator` uses `java.util.Calendar` day numbers: `SUNDAY = 1`, `MONDAY = 2`, `FRIDAY = 6`,
  `SATURDAY = 7`. Both weekend days (Saturday and Sunday) and two weekdays (Monday and Friday) are tested.
- Rates are written as percentages; the expected fee is `amount * rate / 100`, compared with a tolerance of
  0.0001.
- For students the balance does not matter, and for non-students the day does not matter; W-03, W-04, W-09
  and W-10 check that.

## 1.3 Deposit fee (`FeesCalculator.calculateDepositInterest`)

### Scope

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

### Supplementary boundary cases (`FeesCalculatorTest#depositBoundaryTest`, DB-01 to DB-20)

Equivalence Class Analysis deliberately takes representatives from the middle of each class. To also check the
strict (`>`) versus inclusive (`<=`) comparisons of the specification, twenty boundary cases were added. For
every threshold of the deposit rules ($50 and $250 amounts; $500, $2,500, $5,000 and $10,000 balances), each
case takes the value one cent below, exactly at, and one cent above the threshold, with the other inputs held
at a value from the middle of their class. DB-19 and DB-20 check that the amount threshold is applied before
the balance threshold (for example, a student amount above $50 must use the $500 balance threshold, not the
$5,000 one). The expected fee is `amount * rate / 100`, compared with a tolerance of 0.0001.

## 1.4 Transfer fee (`FeesCalculator.calculateTransferFee`)

### Scope

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

### Supplementary boundary cases (`FeesCalculatorTest#transferBoundaryTest`, TB-01 to TB-30)

Decision Table Analysis takes one representative per rule from the middle of each range. To check the
`<` versus `>=` comparisons, thirty boundary cases were added: for each threshold, the value one cent below,
exactly at, and one cent above it, with the other inputs held at values that keep the case inside a single
rule. The two non-student amount groups use **different** balance thresholds ($4,000 / $2,000 below $100, and
$2,000 / $1,000 at $100 or more), so each group's thresholds are tested separately. The expected fee is
`amount * rate / 100`, compared with a tolerance of 0.0001.

---

# Section 2 - Test cases

## 2.1 ATM session

### PIN format (`ATM.checkPin` -> `Session`)

| ID | Variable | BVA value | Input PIN | Expected outcome |
|---|---|---|---|---|
| S-PIN-01 | Length | min- (4) | `5555` | `InvalidPinFormatException` |
| S-PIN-02 | Length / all | nom (5) | `55555` | No exception |
| S-PIN-03 | Length | max+ (6) | `555555` | `InvalidPinFormatException` |
| S-PIN-04 | Digit, position 1 | min- (`/`) | `/5555` | `InvalidPinFormatException` |
| S-PIN-05 | Digit, position 1 | min (`0`) | `05555` | No exception |
| S-PIN-06 | Digit, position 1 | min+ (`1`) | `15555` | No exception |
| S-PIN-07 | Digit, position 1 | max- (`8`) | `85555` | No exception |
| S-PIN-08 | Digit, position 1 | max (`9`) | `95555` | No exception |
| S-PIN-09 | Digit, position 1 | max+ (`:`) | `:5555` | `InvalidPinFormatException` |
| S-PIN-10 | Digit, position 5 | min- (`/`) | `5555/` | `InvalidPinFormatException` |
| S-PIN-11 | Digit, position 5 | min (`0`) | `55550` | No exception |
| S-PIN-12 | Digit, position 5 | min+ (`1`) | `55551` | No exception |
| S-PIN-13 | Digit, position 5 | max- (`8`) | `55558` | No exception |
| S-PIN-14 | Digit, position 5 | max (`9`) | `55559` | No exception |
| S-PIN-15 | Digit, position 5 | max+ (`:`) | `5555:` | `InvalidPinFormatException` |

### Withdrawal amount (`Session.setAmount`)

| ID | BVA value | Input amount | Reason | Expected outcome |
|---|---|---|---|---|
| S-AMT-01 | min- | 19 | Below the smallest bill | `InvalidAmountException` |
| S-AMT-02 | min | 20 | 1 x $20 | No exception |
| S-AMT-03 | min+ | 21 | Not dispensable in $20/$50 | `InvalidAmountException` |
| S-AMT-04 | nom | 500 | 10 x $50 | No exception |
| S-AMT-05 | max- | 999 | Not dispensable in $20/$50 | `InvalidAmountException` |
| S-AMT-06 | max | 1000 | 20 x $50, equals the daily limit | No exception |
| S-AMT-07 | max+ | 1001 | Over the daily limit | `InvalidAmountException` |

## 2.2 Withdrawal fee

### `WithdrawalFeeTest` (W-1 to W-216)

The executable cases cross 2 student values, 3 amounts, 9 balances, and 4 days for 216 cases. The appendix records each input and expected result with its observed result.

Adding the selected invalid values gives a 480 combination design. Of these, 264 include at least one invalid value under the stated input assumptions. There isn't an expected response for those combinations, so they have no pass/fail assertion in this suite.

| Invalid inputs in the combination | Combinations | Expected response |
| --- | ---: | --- |
| Amount only | 72 | Unspecified |
| Balance only | 24 | Unspecified |
| Day only | 108 | Unspecified |
| Amount and balance | 8 | Unspecified |
| Amount and day | 36 | Unspecified |
| Balance and day | 12 | Unspecified |
| Amount, balance, and day | 4 | Unspecified |

The inputs and expected fee of all 216 executed cases are listed in
[Appendix A](#appendix-a---executed-withdrawal-fee-cases-withdrawalfeetest), together with their results.

### `FeesCalculatorTest#withdrawalFeeTest` (W-01 to W-12)

| ID | Student | Amount | Balance | Day | Rule | Rate | Expected fee |
|---|---|---|---|---|---|---|---|
| W-01 | true | 200 | 1,000 | Monday (2) | student, weekday | 0% | 0.00 |
| W-02 | true | 200 | 1,000 | Saturday (7) | student, weekend | 0.1% | 0.20 |
| W-03 | true | 200 | 6,000 | Monday (2) | student, weekday (balance ignored) | 0% | 0.00 |
| W-04 | true | 200 | 6,000 | Sunday (1) | student, weekend (balance ignored) | 0.1% | 0.20 |
| W-05 | false | 200 | 999.99 | Monday (2) | non-student, balance < $1,000 | 0.3% | 0.60 |
| W-06 | false | 200 | 1,000 | Monday (2) | non-student, $1,000 <= balance < $5,000 | 0.1% | 0.20 |
| W-07 | false | 200 | 4,999.99 | Monday (2) | non-student, $1,000 <= balance < $5,000 | 0.1% | 0.20 |
| W-08 | false | 200 | 5,000 | Monday (2) | non-student, balance >= $5,000 | 0% | 0.00 |
| W-09 | false | 200 | 7,000 | Friday (6) | non-student, balance >= $5,000 (day ignored) | 0% | 0.00 |
| W-10 | false | 200 | 500 | Friday (6) | non-student, balance < $1,000 (day ignored) | 0.3% | 0.60 |
| W-11 | true | 200 | 1,000 | Friday (6) | student, weekday | 0% | 0.00 |
| W-12 | false | 200 | 10,000 | Monday (2) | non-student, balance well above $5,000 | 0% | 0.00 |

## 2.3 Deposit fee

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

### Boundary cases (DB-01 to DB-20)

| ID | Threshold tested | Student | Amount | Balance | Rate | Expected fee |
|---|---|---|---|---|---|---|
| DB-01 | student amount $50, just below | true | 49.99 | 400 | 0% | 0.00 |
| DB-02 | student amount $50, at | true | 50 | 400 | 0% | 0.00 |
| DB-03 | student amount $50, just above | true | 50.01 | 400 | 0.25% | 0.125025 |
| DB-04 | student balance $500, just below | true | 100 | 499.99 | 0.25% | 0.25 |
| DB-05 | student balance $500, at | true | 100 | 500 | 0.25% | 0.25 |
| DB-06 | student balance $500, just above | true | 100 | 500.01 | 0.5% | 0.50 |
| DB-07 | student balance $5,000, just below | true | 50 | 4,999.99 | 0% | 0.00 |
| DB-08 | student balance $5,000, at | true | 50 | 5,000 | 0% | 0.00 |
| DB-09 | student balance $5,000, just above | true | 50 | 5,000.01 | 0.5% | 0.25 |
| DB-10 | non-student amount $250, just below | false | 249.99 | 1,000 | 0.1% | 0.24999 |
| DB-11 | non-student amount $250, at | false | 250 | 1,000 | 0.1% | 0.25 |
| DB-12 | non-student amount $250, just above | false | 250.01 | 1,000 | 0.4% | 1.00004 |
| DB-13 | non-student balance $2,500, just below | false | 300 | 2,499.99 | 0.4% | 1.20 |
| DB-14 | non-student balance $2,500, at | false | 300 | 2,500 | 0.4% | 1.20 |
| DB-15 | non-student balance $2,500, just above | false | 300 | 2,500.01 | 0.8% | 2.40 |
| DB-16 | non-student balance $10,000, just below | false | 250 | 9,999.99 | 0.1% | 0.25 |
| DB-17 | non-student balance $10,000, at | false | 250 | 10,000 | 0.1% | 0.25 |
| DB-18 | non-student balance $10,000, just above | false | 250 | 10,000.01 | 0% | 0.00 |
| DB-19 | student: amount > $50 uses the $500 threshold | true | 50.01 | 5,000.01 | 0.5% | 0.250050 |
| DB-20 | non-student: amount > $250 uses the $2,500 threshold | false | 250.01 | 10,000.01 | 0.8% | 2.00008 |

## 2.4 Transfer fee

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

### Boundary cases (TB-01 to TB-30)

| ID | Threshold tested | Student | Amount | From balance | To balance | Rate | Expected fee |
|---|---|---|---|---|---|---|---|
| TB-01 | student amount $200, just below | true | 199.99 | 1,000 | 500 | 0.1% | 0.19999 |
| TB-02 | student amount $200, at | true | 200 | 1,000 | 500 | 0.05% | 0.10 |
| TB-03 | student amount $200, just above | true | 200.01 | 1,000 | 500 | 0.05% | 0.100005 |
| TB-04 | student (< $200) from $2,000, just below | true | 100 | 1,999.99 | 500 | 0.1% | 0.10 |
| TB-05 | student (< $200) from $2,000, at | true | 100 | 2,000 | 500 | 0.05% | 0.05 |
| TB-06 | student (< $200) from $2,000, just above | true | 100 | 2,000.01 | 500 | 0.05% | 0.05 |
| TB-07 | student (< $200) to $1,000, just below | true | 100 | 1,000 | 999.99 | 0.1% | 0.10 |
| TB-08 | student (< $200) to $1,000, at | true | 100 | 1,000 | 1,000 | 0.05% | 0.05 |
| TB-09 | student (< $200) to $1,000, just above | true | 100 | 1,000 | 1,000.01 | 0.05% | 0.05 |
| TB-10 | student (>= $200) from $2,000, just below | true | 500 | 1,999.99 | 500 | 0.05% | 0.25 |
| TB-11 | student (>= $200) from $2,000, at | true | 500 | 2,000 | 500 | 0.25% | 1.25 |
| TB-12 | student (>= $200) from $2,000, just above | true | 500 | 2,000.01 | 500 | 0.25% | 1.25 |
| TB-13 | student (>= $200) to $1,000, just below | true | 500 | 3,000 | 999.99 | 0.25% | 1.25 |
| TB-14 | student (>= $200) to $1,000, at | true | 500 | 3,000 | 1,000 | 0.125% | 0.625 |
| TB-15 | student (>= $200) to $1,000, just above | true | 500 | 3,000 | 1,000.01 | 0.125% | 0.625 |
| TB-16 | non-student amount $100, just below | false | 99.99 | 3,000 | 1,500 | 0.2% | 0.19998 |
| TB-17 | non-student amount $100, at | false | 100 | 3,000 | 1,500 | 0.25% | 0.25 |
| TB-18 | non-student amount $100, just above | false | 100.01 | 3,000 | 1,500 | 0.25% | 0.250025 |
| TB-19 | non-student (< $100) from $4,000, just below | false | 50 | 3,999.99 | 1,000 | 0.2% | 0.10 |
| TB-20 | non-student (< $100) from $4,000, at | false | 50 | 4,000 | 1,000 | 1% | 0.50 |
| TB-21 | non-student (< $100) from $4,000, just above | false | 50 | 4,000.01 | 1,000 | 1% | 0.50 |
| TB-22 | non-student (< $100) to $2,000, just below | false | 50 | 1,000 | 1,999.99 | 0.2% | 0.10 |
| TB-23 | non-student (< $100) to $2,000, at | false | 50 | 1,000 | 2,000 | 0.1% | 0.05 |
| TB-24 | non-student (< $100) to $2,000, just above | false | 50 | 1,000 | 2,000.01 | 0.1% | 0.05 |
| TB-25 | non-student (>= $100) from $2,000, just below | false | 500 | 1,999.99 | 500 | 0.2% | 1.00 |
| TB-26 | non-student (>= $100) from $2,000, at | false | 500 | 2,000 | 500 | 0.5% | 2.50 |
| TB-27 | non-student (>= $100) from $2,000, just above | false | 500 | 2,000.01 | 500 | 0.5% | 2.50 |
| TB-28 | non-student (>= $100) to $1,000, just below | false | 500 | 1,000 | 999.99 | 0.2% | 1.00 |
| TB-29 | non-student (>= $100) to $1,000, at | false | 500 | 1,000 | 1,000 | 0.1% | 0.50 |
| TB-30 | non-student (>= $100) to $1,000, just above | false | 500 | 1,000 | 1,000.01 | 0.1% | 0.50 |

---

# Test results

Results from `mvn -B -Dmaven.test.failure.ignore=true test`, as reported by the CI run on `dev`
(GitHub Actions run 42308607812) and on the pull request. Failing tests are expected in the `phase:tests` phase.

| JUnit class | Tests run | Passed | Failed |
|---|---:|---:|---:|
| `atm.session.SessionTest` | 22 | 12 | 10 |
| `bank.utils.WithdrawalFeeTest` | 216 | 48 | 168 |
| `bank.utils.FeesCalculatorTest` (withdrawal, deposit, transfer) | 89 | 7 | 82 |
| `atm.ATMTest`, `sanity.HelloWorldTest` (starter tests) | 3 | 3 | 0 |
| **Total** | **330** | **70** | **260** |

`FeesCalculatorTest` breaks down as: `withdrawalTest` 1 run / 0 failed, W-01 to W-12 12 / 9, D-01 to D-10
10 / 9, DB-01 to DB-20 20 / 18, T-01 to T-16 16 / 16, TB-01 to TB-30 30 / 30.

## ATM session

Results from `mvn -B test` (also shown in the CI run on the pull request).

| ID | Input | Expected outcome | Actual outcome | Result |
|---|---|---|---|---|
| S-PIN-01 | `5555` | `InvalidPinFormatException` | 4-digit pin was accepted when it should throw | Fail |
| S-PIN-02 | `55555` | No exception | Valid PIN was rejected with InvalidPinFormatException | Fail |
| S-PIN-03 | `555555` | `InvalidPinFormatException` | Sent correct Exception | Pass |
| S-PIN-04 | `/5555` | `InvalidPinFormatException` | Sent correct Exception | Pass |
| S-PIN-05 | `05555` | No exception | Valid PIN was rejected with InvalidPinFormatException | Fail |
| S-PIN-06 | `15555` | No exception | Valid PIN was rejected with InvalidPinFormatException | Fail |
| S-PIN-07 | `85555` | No exception | Valid PIN was rejected with InvalidPinFormatException | Fail |
| S-PIN-08 | `95555` | No exception | Valid PIN was rejected with InvalidPinFormatException | Fail |
| S-PIN-09 | `:5555` | `InvalidPinFormatException` | Sent correct Exception | Pass |
| S-PIN-10 | `5555/` | `InvalidPinFormatException` | Sent correct Exception | Pass |
| S-PIN-11 | `55550` | No exception | Valid PIN was rejected with InvalidPinFormatException | Fail |
| S-PIN-12 | `55551` | No exception | Valid PIN was rejected with InvalidPinFormatException | Fail |
| S-PIN-13 | `55558` | No exception | Valid PIN was rejected with InvalidPinFormatException | Fail |
| S-PIN-14 | `55559` | No exception | Valid PIN was rejected with InvalidPinFormatException | Fail |
| S-PIN-15 | `5555:` | `InvalidPinFormatException` | Sent correct Exception | Pass |
| S-AMT-01 | 19 | `InvalidAmountException` | Sent correct Exception | Pass |
| S-AMT-02 | 20 | No exception | Did not send exception | Pass |
| S-AMT-03 | 21 | `InvalidAmountException` | Sent correct Exception | Pass |
| S-AMT-04 | 500 | No exception | Did not send exception | Pass |
| S-AMT-05 | 999 | `InvalidAmountException` | Sent correct Exception | Pass |
| S-AMT-06 | 1000 | No exception | Did not send exception | Pass |
| S-AMT-07 | 1001 | `InvalidAmountException` | Sent correct Exception | Pass |

**Summary:** 12 of 22 test cases passed, 10 failed.

## Withdrawal fee

### `WithdrawalFeeTest` (W-1 to W-216)

I ran the revised Withdrawal Fee Test with Maven in the Java 11 Docker container. Every invocation was checked against the Maven Surefire report. The commands below reproduce that run.

~~~powershell
docker compose run --rm atm mvn -B checkstyle:check
docker compose run --rm atm mvn -B test '-Dtest=WithdrawalFeeTest'
~~~

For passing cases, JUnit confirms that the observed value matched the expected fee within tolerance. For failed cases, the appendix records the value from the assertion message.

| Tests run | Passed | Failed assertions | Errors | Skipped |
| ---: | ---: | ---: | ---: | ---: |
| 216 | 48 | 168 | 0 | 0 |

The result of every one of the 216 cases is listed in
[Appendix A](#appendix-a---executed-withdrawal-fee-cases-withdrawalfeetest).

### `FeesCalculatorTest#withdrawalFeeTest` (W-01 to W-12)

| ID | Input (student, amount, balance, day) | Expected fee | Actual fee | Result |
|---|---|---|---|---|
| W-01 | true, 200, 1000, Monday | 0.00 | 0.20 | Fail |
| W-02 | true, 200, 1000, Saturday | 0.20 | 0.00 | Fail |
| W-03 | true, 200, 6000, Monday | 0.00 | 0.20 | Fail |
| W-04 | true, 200, 6000, Sunday | 0.20 | 0.00 | Fail |
| W-05 | false, 200, 999.99, Monday | 0.60 | 0.40 | Fail |
| W-06 | false, 200, 1000, Monday | 0.20 | 0.20 | Pass |
| W-07 | false, 200, 4999.99, Monday | 0.20 | 0.20 | Pass |
| W-08 | false, 200, 5000, Monday | 0.00 | 0.20 | Fail |
| W-09 | false, 200, 7000, Friday | 0.00 | 0.20 | Fail |
| W-10 | false, 200, 500, Friday | 0.60 | 0.40 | Fail |
| W-11 | true, 200, 1000, Friday | 0.00 | 0.20 | Fail |
| W-12 | false, 200, 10000, Monday | 0.00 | 0.00 | Pass |

**Summary:** 3 of 12 test cases passed, 9 failed. (The single starter case `withdrawalTest`, non-student,
$200, balance $1,000, expected 0.20, also passed.)

## Deposit fee

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

### Boundary cases (DB-01 to DB-20)

| ID | Input (student, amount, balance) | Expected fee | Actual fee | Result |
|---|---|---|---|---|
| DB-01 | true, 49.99, 400 | 0.00 | 0.09998 | Fail |
| DB-02 | true, 50, 400 | 0.00 | 0.10 | Fail |
| DB-03 | true, 50.01, 400 | 0.125025 | 0.10002 | Fail |
| DB-04 | true, 100, 499.99 | 0.25 | 0.20 | Fail |
| DB-05 | true, 100, 500 | 0.25 | 0.20 | Fail |
| DB-06 | true, 100, 500.01 | 0.50 | 0.20 | Fail |
| DB-07 | true, 50, 4999.99 | 0.00 | 0.10 | Fail |
| DB-08 | true, 50, 5000 | 0.00 | 0.10 | Fail |
| DB-09 | true, 50, 5000.01 | 0.25 | 0.25 | Pass |
| DB-10 | false, 249.99, 1000 | 0.24999 | 0.00 | Fail |
| DB-11 | false, 250, 1000 | 0.25 | 0.00 | Fail |
| DB-12 | false, 250.01, 1000 | 1.00004 | 0.00 | Fail |
| DB-13 | false, 300, 2499.99 | 1.20 | 0.00 | Fail |
| DB-14 | false, 300, 2500 | 1.20 | 0.00 | Fail |
| DB-15 | false, 300, 2500.01 | 2.40 | 0.00 | Fail |
| DB-16 | false, 250, 9999.99 | 0.25 | 0.00 | Fail |
| DB-17 | false, 250, 10000 | 0.25 | 0.00 | Fail |
| DB-18 | false, 250, 10000.01 | 0.00 | 1.25 | Fail |
| DB-19 | true, 50.01, 5000.01 | 0.250050 | 0.250050 | Pass |
| DB-20 | false, 250.01, 10000.01 | 2.00008 | 1.25005 | Fail |

**Summary:** 2 of 20 test cases passed, 18 failed. DB-19 passes only by coincidence: the faulty $100 student
threshold sends it to the small-deposit branch, whose rate for a balance over $5,000 (0.5%) happens to equal
the correct rate.

## Transfer fee

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

### Boundary cases (TB-01 to TB-30)

| ID | Input (student, amount, from, to) | Expected fee | Actual fee | Result |
|---|---|---|---|---|
| TB-01 | true, 199.99, 1000, 500 | 0.19999 | 4.99975 | Fail |
| TB-02 | true, 200, 1000, 500 | 0.10 | 5.00 | Fail |
| TB-03 | true, 200.01, 1000, 500 | 0.100005 | 5.00025 | Fail |
| TB-04 | true, 100, 1999.99, 500 | 0.10 | 2.50 | Fail |
| TB-05 | true, 100, 2000, 500 | 0.05 | 2.50 | Fail |
| TB-06 | true, 100, 2000.01, 500 | 0.05 | 2.50 | Fail |
| TB-07 | true, 100, 1000, 999.99 | 0.10 | 2.50 | Fail |
| TB-08 | true, 100, 1000, 1000 | 0.05 | 1.25 | Fail |
| TB-09 | true, 100, 1000, 1000.01 | 0.05 | 1.25 | Fail |
| TB-10 | true, 500, 1999.99, 500 | 0.25 | 12.50 | Fail |
| TB-11 | true, 500, 2000, 500 | 1.25 | 12.50 | Fail |
| TB-12 | true, 500, 2000.01, 500 | 1.25 | 12.50 | Fail |
| TB-13 | true, 500, 3000, 999.99 | 1.25 | 12.50 | Fail |
| TB-14 | true, 500, 3000, 1000 | 0.625 | 6.25 | Fail |
| TB-15 | true, 500, 3000, 1000.01 | 0.625 | 6.25 | Fail |
| TB-16 | false, 99.99, 3000, 1500 | 0.19998 | 4.9995 | Fail |
| TB-17 | false, 100, 3000, 1500 | 0.25 | 5.50 | Fail |
| TB-18 | false, 100.01, 3000, 1500 | 0.250025 | 5.50055 | Fail |
| TB-19 | false, 50, 3999.99, 1000 | 0.10 | 2.50 | Fail |
| TB-20 | false, 50, 4000, 1000 | 0.50 | 2.50 | Fail |
| TB-21 | false, 50, 4000.01, 1000 | 0.50 | 2.50 | Fail |
| TB-22 | false, 50, 1000, 1999.99 | 0.10 | 2.50 | Fail |
| TB-23 | false, 50, 1000, 2000 | 0.05 | 2.50 | Fail |
| TB-24 | false, 50, 1000, 2000.01 | 0.05 | 2.50 | Fail |
| TB-25 | false, 500, 1999.99, 500 | 1.00 | 25.00 | Fail |
| TB-26 | false, 500, 2000, 500 | 2.50 | 25.00 | Fail |
| TB-27 | false, 500, 2000.01, 500 | 2.50 | 25.00 | Fail |
| TB-28 | false, 500, 1000, 999.99 | 1.00 | 25.00 | Fail |
| TB-29 | false, 500, 1000, 1000 | 0.50 | 27.50 | Fail |
| TB-30 | false, 500, 1000, 1000.01 | 0.50 | 27.50 | Fail |

**Summary:** 0 of 30 test cases passed, 30 failed.

---

# Fault localization

Each entry is one suspected fault. Failing test cases that expose the same fault are grouped under a single
entry. When a test case passes through more than one faulty statement (a wrong condition sends it to a branch
that also has a wrong rate), it is listed under the **first** faulty statement on its path, which is the one it
exposes. Fault IDs are prefixed by target: `SF` session, `WF` withdrawal, `DF` deposit, `TF` transfer; `SM`,
`DM` and `TM` are suspected faults that no failing test reaches yet.

## Summary table (for the pull request)

| Failing test | Suspected fault (file, method, line) | One-line justification |
|---|---|---|
| S-PIN-01, 02, 05, 06, 07, 08, 11, 12, 13, 14 | FormatChecker.java, checkPinFormat, line 33 | **SF-1:** The length check is `pin.length == 4` instead of 5, so a 4-digit PIN is accepted and every valid 5-digit PIN is rejected. |
| 54 `WithdrawalFeeTest` cases (student, Saturday/Sunday; listed under WF-1); W-02, W-04 | FeesCalculator.java, calculateWithdrawalFee, line 13 | **WF-1:** A student withdrawing on the weekend is charged 0% instead of 0.1%. |
| 54 `WithdrawalFeeTest` cases (student, Monday/Friday; listed under WF-2); W-01, W-03, W-11 | FeesCalculator.java, calculateWithdrawalFee, line 15 | **WF-2:** A student withdrawing on a weekday is charged 0.1% instead of no fee (the weekday and weekend rates are reversed). |
| 36 `WithdrawalFeeTest` cases (non-student, balance < $1,000; listed under WF-3); W-05, W-10 | FeesCalculator.java, calculateWithdrawalFee, line 19 | **WF-3:** The non-student rate for a balance below $1,000 is 0.002 (0.2%) instead of 0.003 (0.3%). |
| 24 `WithdrawalFeeTest` cases (non-student, balance >= $5,000; listed under WF-4); W-08, W-09 | FeesCalculator.java, calculateWithdrawalFee, line 20 | **WF-4:** The no-fee threshold is `accountBalance < 10000` instead of 5,000, so balances from $5,000 to below $10,000 are charged 0.1%. |
| D-01, D-02, DB-03, DB-04, DB-05, DB-06 | FeesCalculator.java, calculateDepositInterest, line 39 | **DF-1:** The student amount threshold is `amount > 100` instead of 50, so deposits from $50.01 to $100 take the small-deposit branch. |
| D-04, DB-01, DB-02, DB-07, DB-08 | FeesCalculator.java, calculateDepositInterest, line 50 | **DF-2:** A student's small deposit with a balance of $5,000 or less is charged 0.2% instead of no fee. |
| D-05 | FeesCalculator.java, calculateDepositInterest, line 55 | **DF-3:** The non-student balance threshold is `accountBalance > 5000` instead of 2,500, so a $5,000 balance gets the lower rate. |
| D-06 | FeesCalculator.java, calculateDepositInterest, line 58 | **DF-4:** A non-student's large deposit with a low balance is charged 0.5% instead of 0.4%. |
| D-07, D-08, DB-10, DB-11, DB-16, DB-17, DB-18 | FeesCalculator.java, calculateDepositInterest, lines 62 and 64 | **DF-5:** The two non-student small-deposit rates are swapped: 0.5% instead of none above $10,000, and none instead of 0.1% otherwise. |
| D-09, D-10 | FeesCalculator.java, calculateDepositInterest, line 33 | **DF-6:** The method has no input validation, so negative amounts and balances return a fee instead of being rejected. |
| DB-12, DB-13, DB-14, DB-15, DB-20 | FeesCalculator.java, calculateDepositInterest, line 54 | **DF-7:** The non-student amount threshold is `amount > 500` instead of 250, so deposits from $250.01 to $500 take the small-deposit branch. |
| T-01, T-02, T-03, T-04, TB-01, TB-04, TB-05, TB-06, TB-07, TB-08, TB-09 | FeesCalculator.java, calculateTransferFee, line 80 | **TF-1:** The student amount threshold is `amount < 100` instead of 200, so transfers from $100 to $199.99 take the large-amount branch. |
| T-05, T-06, TB-02, TB-03, TB-10 | FeesCalculator.java, calculateTransferFee, line 95 | **TF-2:** The student from-balance threshold (amount >= $200) is `fromAccountBalance < 1000` instead of 2,000. |
| T-07, TB-11, TB-12, TB-13 | FeesCalculator.java, calculateTransferFee, line 103 | **TF-3:** The rate is 0.025 (2.5%) instead of 0.0025 (0.25%). |
| T-08, TB-14, TB-15 | FeesCalculator.java, calculateTransferFee, line 105 | **TF-4:** The rate is 0.0125 (1.25%) instead of 0.00125 (0.125%). |
| T-09, T-10, TB-16, TB-19, TB-22, TB-23, TB-24 | FeesCalculator.java, calculateTransferFee, line 111 | **TF-5:** The non-student small-amount from-balance threshold is `fromAccountBalance < 1000` instead of 4,000. |
| T-11, TB-20, TB-21 | FeesCalculator.java, calculateTransferFee, line 118 | **TF-6:** The non-student small-amount to-balance threshold is `toAccountBalance < 1000` instead of 2,000. |
| T-12 | FeesCalculator.java, calculateTransferFee, line 121 | **TF-7:** The rate is 0.05 (5%) instead of 0.005 (0.5%). |
| T-13, T-14, TB-25, TB-28, TB-29, TB-30 | FeesCalculator.java, calculateTransferFee, line 125 | **TF-8:** The non-student large-amount from-balance threshold is `fromAccountBalance < 1000` instead of 2,000. |
| T-15, TB-26, TB-27 | FeesCalculator.java, calculateTransferFee, line 133 | **TF-9:** The rate is 0.05 (5%) instead of 0.005 (0.5%). |
| T-16, TB-17, TB-18 | FeesCalculator.java, calculateTransferFee, line 135 | **TF-10:** The rate is 0.055 (5.5%) instead of 0.0025 (0.25%). |

## ATM session (`atm.session.Session`)

Each entry below is one suspected fault. Failing test cases that expose the same fault are grouped under a
single entry.

### SF-1: PIN length check expects 4 digits instead of 5

- **Failing test cases:** S-PIN-01, S-PIN-02, S-PIN-05, S-PIN-06, S-PIN-07, S-PIN-08, S-PIN-11, S-PIN-12,
  S-PIN-13, S-PIN-14
- **Location:** `atm.utils.FormatChecker`, method `checkPinFormat(char[])`, line 33: `if (pin.length == 4)`
- **Expected (specification):** A valid PIN is 5 digits, so every 5-digit PIN should be accepted and the 4-digit
  PIN `5555` should throw `InvalidPinFormatException`.
- **Observed:** The 4-digit PIN `5555` was accepted (no exception was thrown), and every valid 5-digit PIN was
  rejected with `InvalidPinFormatException`.
- **Justification:** The condition only accepts PINs of length 4, so a 4-digit PIN passes the digit check while
  every 5-digit PIN falls into the `else` branch (line 40) and throws.

### Suspected faults masked by SF-1

  No failing test exposes these yet, because every 5-digit PIN is rejected at line 33 before they run.
  They are listed here because they would likely surface once SF-1 is fixed.

- **SM-1:** `FormatChecker.checkPinFormat`, line 35: `pin[i] >= '9'` rejects the digit `9`, although `9` is a valid digit. S-PIN-08 and S-PIN-14 would likely still fail after SF-1 is fixed.
- **SM-2:** `FormatChecker.checkPinFormat`, line 34: `for (int i = 0; i < 4; i++)` checks only the first four characters, so the fifth character is never validated. S-PIN-10 and S-PIN-15 would likely fail once 5-digit PINs reach the loop.

## Withdrawal fee (`FeesCalculator.calculateWithdrawalFee`)

### `WithdrawalFeeTest` (W-1 to W-216)

| Group | Failed cases | Location and justification |
| --- | ---: | --- |
| WF-1 | 54 | Line 13 assigns no fee for a student on the weekend, where the handout specifies 0.1%. |
| WF-2 | 54 | Line 15 assigns a 0.1% fee for a student on a weekday, where the handout specifies no fee. |
| WF-3 | 36 | Line 19 assigns 0.2% for non-student balances below $1,000, where the handout specifies 0.3%. |
| WF-4 | 24 | Line 20 uses $10,000 as the upper cutoff for the 0.1% rate, where the handout specifies $5,000. |

Line numbers refer to the Fees Calculator file, specifically the method calculateWithdrawalFee.
Note: I used Codex Work to record the results as shown below:

- **WF-1** — W-2, W-3, W-6, W-7, W-10, W-11, W-14, W-15, W-18, W-19, W-22, W-23, W-26, W-27, W-30, W-31, W-34, W-35, W-38, W-39, W-42, W-43, W-46, W-47, W-50, W-51, W-54, W-55, W-58, W-59, W-62, W-63, W-66, W-67, W-70, W-71, W-74, W-75, W-78, W-79, W-82, W-83, W-86, W-87, W-90, W-91, W-94, W-95, W-98, W-99, W-102, W-103, W-106, W-107
- **WF-2** — W-1, W-4, W-5, W-8, W-9, W-12, W-13, W-16, W-17, W-20, W-21, W-24, W-25, W-28, W-29, W-32, W-33, W-36, W-37, W-40, W-41, W-44, W-45, W-48, W-49, W-52, W-53, W-56, W-57, W-60, W-61, W-64, W-65, W-68, W-69, W-72, W-73, W-76, W-77, W-80, W-81, W-84, W-85, W-88, W-89, W-92, W-93, W-96, W-97, W-100, W-101, W-104, W-105, W-108
- **WF-3** — W-109, W-110, W-111, W-112, W-113, W-114, W-115, W-116, W-117, W-118, W-119, W-120, W-145, W-146, W-147, W-148, W-149, W-150, W-151, W-152, W-153, W-154, W-155, W-156, W-181, W-182, W-183, W-184, W-185, W-186, W-187, W-188, W-189, W-190, W-191, W-192
- **WF-4** — W-137, W-138, W-139, W-140, W-141, W-142, W-143, W-144, W-173, W-174, W-175, W-176, W-177, W-178, W-179, W-180, W-209, W-210, W-211, W-212, W-213, W-214, W-215, W-216

### `FeesCalculatorTest#withdrawalFeeTest` (W-01 to W-12)

The supplementary cases expose the same four faults:

- **WF-1** (line 13, student weekend charged 0%): W-02 (Saturday) and W-04 (Sunday) returned 0.00 instead of 0.20.
- **WF-2** (line 15, student weekday charged 0.1%): W-01 (Monday), W-03 (Monday) and W-11 (Friday) returned
  0.20 instead of 0.00.
- **WF-3** (line 19, rate 0.2% instead of 0.3%): W-05 (balance $999.99) and W-10 (balance $500) returned 0.40
  instead of 0.60.
- **WF-4** (line 20, threshold $10,000 instead of $5,000): W-08 (balance $5,000) and W-09 (balance $7,000)
  returned 0.20 instead of 0.00.

W-06 and W-07 pass because the 0.1% rate for $1,000 to $4,999.99 is correct, and W-12 passes because $10,000 is
on the no-fee side of both the faulty and the correct threshold.

## Deposit fee (`FeesCalculator.calculateDepositInterest`)

Each entry below is one suspected fault. Failing test cases that expose the same fault are grouped under a
single entry. All line numbers refer to `src/main/java/bank/utils/FeesCalculator.java`, method
`calculateDepositInterest`.

### DF-1: Student amount threshold is $100 instead of $50

- **Failing test cases:** D-01, D-02
- **Location:** line 39: `if (amount > 100.00)`
- **Expected (specification):** a student deposit of $100 is "more than $50", so the fee is 0.5% (balance
  over $500) or 0.25% (balance $500 or less).
- **Observed:** both returned 0.20, which is the 0.2% rate of the "$50 or less" branch.
- **Justification:** with the threshold at 100, an amount of $100 is not greater than it, so both test cases
  take the "small amount" branch (lines 46-51) instead of the "large amount" branch.

### DF-2: Student fee for a small deposit and low balance is 0.2% instead of no fee

- **Failing test cases:** D-04
- **Location:** line 50: `interestPercentage = 0.002;`
- **Expected (specification):** a student depositing $50 or less into an account with $5,000 or less pays no
  fee.
- **Observed:** 0.04, i.e. 0.2% of $20.
- **Justification:** the branch is reached correctly (amount 20 ≤ 100 and balance 200 ≤ 5,000), but it
  assigns 0.002 instead of 0.

### DF-3: Non-student balance threshold is $5,000 instead of $2,500

- **Failing test cases:** D-05
- **Location:** line 55: `if (accountBalance > 5000.00)`
- **Expected (specification):** a balance of $5,000 is "more than $2,500", so the fee is 0.8% (8.00).
- **Observed:** 5.00, i.e. 0.5% of $1,000.
- **Justification:** with the threshold at 5,000, a balance of exactly $5,000 is not greater than it, so the
  test case falls into the `else` branch at line 58.

### DF-4: Non-student fee for a large deposit and low balance is 0.5% instead of 0.4%

- **Failing test cases:** D-06
- **Location:** line 58: `interestPercentage = 0.005;`
- **Expected (specification):** 0.4% of $1,000 = 4.00.
- **Observed:** 5.00, i.e. 0.5% of $1,000.
- **Justification:** the branch is reached correctly (amount 1,000 > 500 and balance 1,000 ≤ 5,000), but it
  assigns 0.005 instead of 0.004.

### DF-5: Non-student fees for a small deposit are swapped

- **Failing test cases:** D-07, D-08
- **Location:** line 62: `interestPercentage = 0.005;` and line 64: `interestPercentage = 0.0;`
- **Expected (specification):** for $250 or less, no fee when the balance is over $10,000 and 0.1%
  otherwise.
- **Observed:** D-07 (balance 20,000) returned 0.50 (0.5%) and D-08 (balance 1,000) returned 0.00.
- **Justification:** the balance check at line 61 is correct, but the "over $10,000" branch charges 0.5% and
  the "$10,000 or less" branch charges nothing, the opposite of the specification.

### DF-6: Invalid input is not rejected

- **Failing test cases:** D-09, D-10
- **Location:** start of the method (line 33); there is no input validation.
- **Expected (deposit assumption A7):** a negative amount or a negative balance throws `IllegalArgumentException`.
- **Observed:** no exception was thrown; a fee of 0.00 was returned in both cases.
- **Justification:** the method goes straight to the student/amount/balance branches without checking that
  the amount is positive and the balance is not negative.

### DF-7: Non-student amount threshold is $500 instead of $250

- **Failing test cases:** DB-12, DB-13, DB-14, DB-15, DB-20
- **Location:** line 54: `if (amount > 500.00)`
- **Expected (specification):** a non-student deposit of more than $250 pays 0.4% (balance $2,500 or less) or
  0.8% (balance over $2,500).
- **Observed:** DB-12 to DB-15 returned 0.00 and DB-20 returned 1.25005 (0.5%), the results of the "$250 or
  less" branch.
- **Justification:** amounts of $250.01 and $300 are not greater than 500, so the test cases take the
  small-deposit branch (lines 61-65), where they also meet DF-5.

### Boundary cases mapped to the faults above

- **DF-1** (line 39) is also exposed by DB-03 to DB-06 (amounts $50.01 and $100).
- **DF-2** (line 50) is also exposed by DB-01, DB-02, DB-07 and DB-08 (amount $50 or less, balance $5,000 or
  less).
- **DF-5** is also exposed by DB-10, DB-11, DB-16 and DB-17 (line 64: 0% instead of 0.1%) and by DB-18
  (line 62: 0.5% instead of 0%).

### Suspected faults masked or not reached by the test cases

No failing test exposes these yet, either because another fault sends the test case down a different branch,
or because no representative value falls between the wrong and the correct threshold. They would likely
surface once the faults above are fixed, or with Boundary Value Analysis on the thresholds.

- **DM-1:** line 41: `accountBalance > 1000.00` should be 500. Masked by DF-1 for D-01 and D-02.
- **DM-2:** line 42: rate 0.01 (1%) should be 0.005 (0.5%). Masked by DF-1.
- **DM-3:** line 44: rate 0.005 (0.5%) should be 0.0025 (0.25%). Masked by DF-1.
- **DM-4:** line 54: `amount > 500.00` should be 250. Not reached by D-01 to D-10, but now exposed by the boundary cases DB-12 to DB-15 and DB-20; see **DF-7**.
- **DM-5:** line 56: rate 0.01 (1%) should be 0.008 (0.8%). Masked by DF-3 for D-05.

## Transfer fee (`FeesCalculator.calculateTransferFee`)

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

### Boundary cases mapped to the faults above

- **TF-1** (line 80): TB-01, TB-04, TB-05, TB-06, TB-07, TB-08, TB-09 (student amounts $100 to $199.99).
- **TF-2** (line 95): TB-02, TB-03, TB-10 (student, amount >= $200, from balance $1,000 to $1,999.99).
- **TF-3** (line 103): TB-11, TB-12, TB-13. **TF-4** (line 105): TB-14, TB-15.
- **TF-5** (line 111): TB-16, TB-19, TB-22, TB-23, TB-24 (non-student, amount < $100, from balance $1,000 to
  $3,999.99).
- **TF-6** (line 118): TB-20, TB-21 (to balance exactly $1,000).
- **TF-8** (line 125): TB-25, TB-28, TB-29, TB-30. **TF-9** (line 133): TB-26, TB-27. **TF-10** (line 135):
  TB-17, TB-18.

### Suspected faults masked by the faults above

Because of TF-1, TF-2, TF-5 and TF-8, no test case reaches the student small-amount branch (lines 81-92),
the student low-from-balance branch (lines 96-100), or the non-student low-from-balance branches (lines
112-116 and 126-130). The statements below sit in those branches and do not match the specification. They
would likely surface once the threshold faults are fixed.

- **TM-1:** line 83: rate 0.01 (1%) should be 0.001 (0.1%).
- **TM-2:** line 85: rate 0.005 (0.5%) should be 0.0005 (0.05%).
- **TM-3:** line 81: `fromAccountBalance < 1000.00` should be 2,000.
- **TM-4:** line 89: rate 0.05 (5%) should be 0.0005 (0.05%).
- **TM-5:** line 91: rate 0.025 (2.5%) should be 0.00025 (0.025%).
- **TM-6:** line 97: rate 0.005 (0.5%) should be 0.0005 (0.05%).
- **TM-7:** line 99: rate 0.0025 (0.25%) should be 0.00025 (0.025%).
- **TM-8:** line 112: `toAccountBalance < 1000.00` should be 2,000 (same fault as TF-6, in the low-from-balance branch).
- **TM-9:** line 113: rate 0.02 (2%) should be 0.002 (0.2%).
- **TM-10:** line 115: rate 0.01 (1%) should be 0.001 (0.1%).
- **TM-11:** line 127: rate 0.01 (1%) should be 0.002 (0.2%).
- **TM-12:** line 129: rate 0.005 (0.5%) should be 0.001 (0.1%).
- **TM-13:** line 119: rate 0.1 (10%) should be 0.01 (1%). No test case reaches this branch (non-student,
  amount < $100, from balance of $1,000 or more, to balance below $1,000).

## Cross-reference to the team fee fault table

The team's fee fault table (originally kept as a Word document) groups each failing case under **every**
faulty statement on its path, rather than only the first one. It is kept here for reference, with the matching
IDs used above.

| Fault | Class and method / lines | Failed test IDs | One-sentence justification | Maps to |
|---|---|---|---|---|
| W1 | FeesCalculator.calculateWithdrawalFee(), lines 12-15 | W-01, W-02, W-03, W-04, W-11 | Weekend and weekday fee assignments are reversed: the code makes weekends free and charges students 0.1% on weekdays. | WF-1, WF-2 |
| W2 | FeesCalculator.calculateWithdrawalFee(), line 19 | W-05, W-10 | The < $1,000 non-student branch uses 0.002 (0.2%) instead of the required 0.003 (0.3%). | WF-3 |
| W3 | FeesCalculator.calculateWithdrawalFee(), line 20 | W-08, W-09 | The non-student free-withdrawal threshold is $10,000 rather than $5,000, so balances from $5,000 to below $10,000 are charged. | WF-4 |
| D1 | FeesCalculator.calculateDepositInterest(), line 39 | D-01, D-02, DB-03, DB-04, DB-05, DB-06 | Student deposits above $50 but no more than $100 follow the small-deposit branch because the code incorrectly checks amount > $100. | DF-1 |
| D2 | FeesCalculator.calculateDepositInterest(), line 50 | D-04, DB-01, DB-02, DB-07, DB-08 | For student deposits of $50 or less and balances at most $5,000, the code returns 0.2% instead of no fee. | DF-2 |
| D3 | FeesCalculator.calculateDepositInterest(), lines 54-58 | D-05, D-06, DB-12, DB-13, DB-14, DB-15, DB-20 | The non-student large-deposit cutoff is coded as > $500 (not > $250), while its balance cutoff and percentage constants also differ from the 0.4%/0.8% rules; some cases expose multiple errors. | DF-3, DF-4, DF-7 (and DM-5) |
| D4 | FeesCalculator.calculateDepositInterest(), line 62 | D-07, DB-18 | The non-student small-deposit / balance > $10,000 branch charges 0.5% rather than the required zero fee. | DF-5 (line 62) |
| D5 | FeesCalculator.calculateDepositInterest(), line 64 | D-08, DB-10, DB-11, DB-12, DB-13, DB-14, DB-15, DB-16, DB-17 | The non-student small-deposit / balance <= $10,000 branch returns zero instead of 0.1%; DB-12 to DB-15 also reach this branch due to the faulty amount cutoff. | DF-5 (line 64) |
| D6 | FeesCalculator.calculateDepositInterest(), lines 33-72 | D-09, D-10 | No validation rejects a negative deposit amount or balance, so the assumed IllegalArgumentException is not thrown; the supplied fee specification does not require this exception. | DF-6 |
| T1 | FeesCalculator.calculateTransferFee(), line 80 | T-01, T-02, T-03, T-04, TB-01, TB-04, TB-05, TB-06, TB-07, TB-08, TB-09 | The student amount cutoff is < $100 instead of < $200, misrouting student transfers between $100 and $199.99. | TF-1 |
| T2 | FeesCalculator.calculateTransferFee(), lines 81 and 95 | T-01, T-02, T-05, T-06, TB-01, TB-02, TB-03, TB-04, TB-07, TB-08, TB-09, TB-10 | Both student source-balance checks use < $1,000 instead of < $2,000, selecting the high-source branch for balances $1,000 to $1,999.99. | TF-2 (and TM-3) |
| T3 | FeesCalculator.calculateTransferFee(), lines 83-105 | T-01 to T-08, TB-01 to TB-15 | Student transfer percentage constants are higher than the stated 0.025%-0.25% rates, even when the code reaches the intended branch. | TF-3, TF-4 (and TM-1, TM-2, TM-4 to TM-7) |
| T4 | FeesCalculator.calculateTransferFee(), lines 111-121 | T-09, T-10, T-11, TB-16, TB-19, TB-20, TB-21, TB-22, TB-23, TB-24 | For non-student transfers below $100, source and destination cutoffs are both $1,000 rather than $4,000 and $2,000, causing wrong branch selection. | TF-5, TF-6 (and TM-8) |
| T5 | FeesCalculator.calculateTransferFee(), lines 113-121 | T-09, T-10, T-11, T-12, TB-16, TB-19, TB-20, TB-21, TB-22, TB-23, TB-24 | Non-student transfer rates for amounts below $100 are coded as 2%, 1%, 10%, and 5% rather than 0.2%, 0.1%, 1%, and 0.5%. | TF-7 (and TM-9, TM-10, TM-13) |
| T6 | FeesCalculator.calculateTransferFee(), line 125 | T-13, T-14, TB-25, TB-28, TB-29, TB-30 | For non-student transfers of at least $100, the source threshold is < $1,000 instead of < $2,000, wrongly routing 1,999.99 balances. | TF-8 |
| T7 | FeesCalculator.calculateTransferFee(), lines 127-135 | T-13, T-14, T-15, T-16, TB-17, TB-18, TB-25, TB-26, TB-27, TB-28, TB-29, TB-30 | The non-student transfer percentages for amounts of at least $100 are coded as 1%, 0.5%, 5%, and 5.5%, rather than 0.2%, 0.1%, 0.5%, and 0.25%. | TF-9, TF-10 (and TM-11, TM-12) |

---

# Appendix A - Executed withdrawal fee cases (`WithdrawalFeeTest`)

| ID | Student | Amount ($) | Balance ($) | Day | Expected fee | Observed fee | Result |
| --- | --- | ---: | ---: | --- | ---: | --- | --- |
| W-1 | Yes | 0.01 | 0.00 | Friday | $0.00000 | $0.00001 | Fail |
| W-2 | Yes | 0.01 | 0.00 | Saturday | $0.00001 | $0.00000 | Fail |
| W-3 | Yes | 0.01 | 0.00 | Sunday | $0.00001 | $0.00000 | Fail |
| W-4 | Yes | 0.01 | 0.00 | Monday | $0.00000 | $0.00001 | Fail |
| W-5 | Yes | 0.01 | 0.01 | Friday | $0.00000 | $0.00001 | Fail |
| W-6 | Yes | 0.01 | 0.01 | Saturday | $0.00001 | $0.00000 | Fail |
| W-7 | Yes | 0.01 | 0.01 | Sunday | $0.00001 | $0.00000 | Fail |
| W-8 | Yes | 0.01 | 0.01 | Monday | $0.00000 | $0.00001 | Fail |
| W-9 | Yes | 0.01 | 999.99 | Friday | $0.00000 | $0.00001 | Fail |
| W-10 | Yes | 0.01 | 999.99 | Saturday | $0.00001 | $0.00000 | Fail |
| W-11 | Yes | 0.01 | 999.99 | Sunday | $0.00001 | $0.00000 | Fail |
| W-12 | Yes | 0.01 | 999.99 | Monday | $0.00000 | $0.00001 | Fail |
| W-13 | Yes | 0.01 | 1000.00 | Friday | $0.00000 | $0.00001 | Fail |
| W-14 | Yes | 0.01 | 1000.00 | Saturday | $0.00001 | $0.00000 | Fail |
| W-15 | Yes | 0.01 | 1000.00 | Sunday | $0.00001 | $0.00000 | Fail |
| W-16 | Yes | 0.01 | 1000.00 | Monday | $0.00000 | $0.00001 | Fail |
| W-17 | Yes | 0.01 | 1000.01 | Friday | $0.00000 | $0.00001 | Fail |
| W-18 | Yes | 0.01 | 1000.01 | Saturday | $0.00001 | $0.00000 | Fail |
| W-19 | Yes | 0.01 | 1000.01 | Sunday | $0.00001 | $0.00000 | Fail |
| W-20 | Yes | 0.01 | 1000.01 | Monday | $0.00000 | $0.00001 | Fail |
| W-21 | Yes | 0.01 | 2500.00 | Friday | $0.00000 | $0.00001 | Fail |
| W-22 | Yes | 0.01 | 2500.00 | Saturday | $0.00001 | $0.00000 | Fail |
| W-23 | Yes | 0.01 | 2500.00 | Sunday | $0.00001 | $0.00000 | Fail |
| W-24 | Yes | 0.01 | 2500.00 | Monday | $0.00000 | $0.00001 | Fail |
| W-25 | Yes | 0.01 | 4999.99 | Friday | $0.00000 | $0.00001 | Fail |
| W-26 | Yes | 0.01 | 4999.99 | Saturday | $0.00001 | $0.00000 | Fail |
| W-27 | Yes | 0.01 | 4999.99 | Sunday | $0.00001 | $0.00000 | Fail |
| W-28 | Yes | 0.01 | 4999.99 | Monday | $0.00000 | $0.00001 | Fail |
| W-29 | Yes | 0.01 | 5000.00 | Friday | $0.00000 | $0.00001 | Fail |
| W-30 | Yes | 0.01 | 5000.00 | Saturday | $0.00001 | $0.00000 | Fail |
| W-31 | Yes | 0.01 | 5000.00 | Sunday | $0.00001 | $0.00000 | Fail |
| W-32 | Yes | 0.01 | 5000.00 | Monday | $0.00000 | $0.00001 | Fail |
| W-33 | Yes | 0.01 | 5000.01 | Friday | $0.00000 | $0.00001 | Fail |
| W-34 | Yes | 0.01 | 5000.01 | Saturday | $0.00001 | $0.00000 | Fail |
| W-35 | Yes | 0.01 | 5000.01 | Sunday | $0.00001 | $0.00000 | Fail |
| W-36 | Yes | 0.01 | 5000.01 | Monday | $0.00000 | $0.00001 | Fail |
| W-37 | Yes | 0.02 | 0.00 | Friday | $0.00000 | $0.00002 | Fail |
| W-38 | Yes | 0.02 | 0.00 | Saturday | $0.00002 | $0.00000 | Fail |
| W-39 | Yes | 0.02 | 0.00 | Sunday | $0.00002 | $0.00000 | Fail |
| W-40 | Yes | 0.02 | 0.00 | Monday | $0.00000 | $0.00002 | Fail |
| W-41 | Yes | 0.02 | 0.01 | Friday | $0.00000 | $0.00002 | Fail |
| W-42 | Yes | 0.02 | 0.01 | Saturday | $0.00002 | $0.00000 | Fail |
| W-43 | Yes | 0.02 | 0.01 | Sunday | $0.00002 | $0.00000 | Fail |
| W-44 | Yes | 0.02 | 0.01 | Monday | $0.00000 | $0.00002 | Fail |
| W-45 | Yes | 0.02 | 999.99 | Friday | $0.00000 | $0.00002 | Fail |
| W-46 | Yes | 0.02 | 999.99 | Saturday | $0.00002 | $0.00000 | Fail |
| W-47 | Yes | 0.02 | 999.99 | Sunday | $0.00002 | $0.00000 | Fail |
| W-48 | Yes | 0.02 | 999.99 | Monday | $0.00000 | $0.00002 | Fail |
| W-49 | Yes | 0.02 | 1000.00 | Friday | $0.00000 | $0.00002 | Fail |
| W-50 | Yes | 0.02 | 1000.00 | Saturday | $0.00002 | $0.00000 | Fail |
| W-51 | Yes | 0.02 | 1000.00 | Sunday | $0.00002 | $0.00000 | Fail |
| W-52 | Yes | 0.02 | 1000.00 | Monday | $0.00000 | $0.00002 | Fail |
| W-53 | Yes | 0.02 | 1000.01 | Friday | $0.00000 | $0.00002 | Fail |
| W-54 | Yes | 0.02 | 1000.01 | Saturday | $0.00002 | $0.00000 | Fail |
| W-55 | Yes | 0.02 | 1000.01 | Sunday | $0.00002 | $0.00000 | Fail |
| W-56 | Yes | 0.02 | 1000.01 | Monday | $0.00000 | $0.00002 | Fail |
| W-57 | Yes | 0.02 | 2500.00 | Friday | $0.00000 | $0.00002 | Fail |
| W-58 | Yes | 0.02 | 2500.00 | Saturday | $0.00002 | $0.00000 | Fail |
| W-59 | Yes | 0.02 | 2500.00 | Sunday | $0.00002 | $0.00000 | Fail |
| W-60 | Yes | 0.02 | 2500.00 | Monday | $0.00000 | $0.00002 | Fail |
| W-61 | Yes | 0.02 | 4999.99 | Friday | $0.00000 | $0.00002 | Fail |
| W-62 | Yes | 0.02 | 4999.99 | Saturday | $0.00002 | $0.00000 | Fail |
| W-63 | Yes | 0.02 | 4999.99 | Sunday | $0.00002 | $0.00000 | Fail |
| W-64 | Yes | 0.02 | 4999.99 | Monday | $0.00000 | $0.00002 | Fail |
| W-65 | Yes | 0.02 | 5000.00 | Friday | $0.00000 | $0.00002 | Fail |
| W-66 | Yes | 0.02 | 5000.00 | Saturday | $0.00002 | $0.00000 | Fail |
| W-67 | Yes | 0.02 | 5000.00 | Sunday | $0.00002 | $0.00000 | Fail |
| W-68 | Yes | 0.02 | 5000.00 | Monday | $0.00000 | $0.00002 | Fail |
| W-69 | Yes | 0.02 | 5000.01 | Friday | $0.00000 | $0.00002 | Fail |
| W-70 | Yes | 0.02 | 5000.01 | Saturday | $0.00002 | $0.00000 | Fail |
| W-71 | Yes | 0.02 | 5000.01 | Sunday | $0.00002 | $0.00000 | Fail |
| W-72 | Yes | 0.02 | 5000.01 | Monday | $0.00000 | $0.00002 | Fail |
| W-73 | Yes | 100.00 | 0.00 | Friday | $0.00000 | $0.10000 | Fail |
| W-74 | Yes | 100.00 | 0.00 | Saturday | $0.10000 | $0.00000 | Fail |
| W-75 | Yes | 100.00 | 0.00 | Sunday | $0.10000 | $0.00000 | Fail |
| W-76 | Yes | 100.00 | 0.00 | Monday | $0.00000 | $0.10000 | Fail |
| W-77 | Yes | 100.00 | 0.01 | Friday | $0.00000 | $0.10000 | Fail |
| W-78 | Yes | 100.00 | 0.01 | Saturday | $0.10000 | $0.00000 | Fail |
| W-79 | Yes | 100.00 | 0.01 | Sunday | $0.10000 | $0.00000 | Fail |
| W-80 | Yes | 100.00 | 0.01 | Monday | $0.00000 | $0.10000 | Fail |
| W-81 | Yes | 100.00 | 999.99 | Friday | $0.00000 | $0.10000 | Fail |
| W-82 | Yes | 100.00 | 999.99 | Saturday | $0.10000 | $0.00000 | Fail |
| W-83 | Yes | 100.00 | 999.99 | Sunday | $0.10000 | $0.00000 | Fail |
| W-84 | Yes | 100.00 | 999.99 | Monday | $0.00000 | $0.10000 | Fail |
| W-85 | Yes | 100.00 | 1000.00 | Friday | $0.00000 | $0.10000 | Fail |
| W-86 | Yes | 100.00 | 1000.00 | Saturday | $0.10000 | $0.00000 | Fail |
| W-87 | Yes | 100.00 | 1000.00 | Sunday | $0.10000 | $0.00000 | Fail |
| W-88 | Yes | 100.00 | 1000.00 | Monday | $0.00000 | $0.10000 | Fail |
| W-89 | Yes | 100.00 | 1000.01 | Friday | $0.00000 | $0.10000 | Fail |
| W-90 | Yes | 100.00 | 1000.01 | Saturday | $0.10000 | $0.00000 | Fail |
| W-91 | Yes | 100.00 | 1000.01 | Sunday | $0.10000 | $0.00000 | Fail |
| W-92 | Yes | 100.00 | 1000.01 | Monday | $0.00000 | $0.10000 | Fail |
| W-93 | Yes | 100.00 | 2500.00 | Friday | $0.00000 | $0.10000 | Fail |
| W-94 | Yes | 100.00 | 2500.00 | Saturday | $0.10000 | $0.00000 | Fail |
| W-95 | Yes | 100.00 | 2500.00 | Sunday | $0.10000 | $0.00000 | Fail |
| W-96 | Yes | 100.00 | 2500.00 | Monday | $0.00000 | $0.10000 | Fail |
| W-97 | Yes | 100.00 | 4999.99 | Friday | $0.00000 | $0.10000 | Fail |
| W-98 | Yes | 100.00 | 4999.99 | Saturday | $0.10000 | $0.00000 | Fail |
| W-99 | Yes | 100.00 | 4999.99 | Sunday | $0.10000 | $0.00000 | Fail |
| W-100 | Yes | 100.00 | 4999.99 | Monday | $0.00000 | $0.10000 | Fail |
| W-101 | Yes | 100.00 | 5000.00 | Friday | $0.00000 | $0.10000 | Fail |
| W-102 | Yes | 100.00 | 5000.00 | Saturday | $0.10000 | $0.00000 | Fail |
| W-103 | Yes | 100.00 | 5000.00 | Sunday | $0.10000 | $0.00000 | Fail |
| W-104 | Yes | 100.00 | 5000.00 | Monday | $0.00000 | $0.10000 | Fail |
| W-105 | Yes | 100.00 | 5000.01 | Friday | $0.00000 | $0.10000 | Fail |
| W-106 | Yes | 100.00 | 5000.01 | Saturday | $0.10000 | $0.00000 | Fail |
| W-107 | Yes | 100.00 | 5000.01 | Sunday | $0.10000 | $0.00000 | Fail |
| W-108 | Yes | 100.00 | 5000.01 | Monday | $0.00000 | $0.10000 | Fail |
| W-109 | No | 0.01 | 0.00 | Friday | $0.00003 | $0.00002 | Fail |
| W-110 | No | 0.01 | 0.00 | Saturday | $0.00003 | $0.00002 | Fail |
| W-111 | No | 0.01 | 0.00 | Sunday | $0.00003 | $0.00002 | Fail |
| W-112 | No | 0.01 | 0.00 | Monday | $0.00003 | $0.00002 | Fail |
| W-113 | No | 0.01 | 0.01 | Friday | $0.00003 | $0.00002 | Fail |
| W-114 | No | 0.01 | 0.01 | Saturday | $0.00003 | $0.00002 | Fail |
| W-115 | No | 0.01 | 0.01 | Sunday | $0.00003 | $0.00002 | Fail |
| W-116 | No | 0.01 | 0.01 | Monday | $0.00003 | $0.00002 | Fail |
| W-117 | No | 0.01 | 999.99 | Friday | $0.00003 | $0.00002 | Fail |
| W-118 | No | 0.01 | 999.99 | Saturday | $0.00003 | $0.00002 | Fail |
| W-119 | No | 0.01 | 999.99 | Sunday | $0.00003 | $0.00002 | Fail |
| W-120 | No | 0.01 | 999.99 | Monday | $0.00003 | $0.00002 | Fail |
| W-121 | No | 0.01 | 1000.00 | Friday | $0.00001 | Matched expected | Pass |
| W-122 | No | 0.01 | 1000.00 | Saturday | $0.00001 | Matched expected | Pass |
| W-123 | No | 0.01 | 1000.00 | Sunday | $0.00001 | Matched expected | Pass |
| W-124 | No | 0.01 | 1000.00 | Monday | $0.00001 | Matched expected | Pass |
| W-125 | No | 0.01 | 1000.01 | Friday | $0.00001 | Matched expected | Pass |
| W-126 | No | 0.01 | 1000.01 | Saturday | $0.00001 | Matched expected | Pass |
| W-127 | No | 0.01 | 1000.01 | Sunday | $0.00001 | Matched expected | Pass |
| W-128 | No | 0.01 | 1000.01 | Monday | $0.00001 | Matched expected | Pass |
| W-129 | No | 0.01 | 2500.00 | Friday | $0.00001 | Matched expected | Pass |
| W-130 | No | 0.01 | 2500.00 | Saturday | $0.00001 | Matched expected | Pass |
| W-131 | No | 0.01 | 2500.00 | Sunday | $0.00001 | Matched expected | Pass |
| W-132 | No | 0.01 | 2500.00 | Monday | $0.00001 | Matched expected | Pass |
| W-133 | No | 0.01 | 4999.99 | Friday | $0.00001 | Matched expected | Pass |
| W-134 | No | 0.01 | 4999.99 | Saturday | $0.00001 | Matched expected | Pass |
| W-135 | No | 0.01 | 4999.99 | Sunday | $0.00001 | Matched expected | Pass |
| W-136 | No | 0.01 | 4999.99 | Monday | $0.00001 | Matched expected | Pass |
| W-137 | No | 0.01 | 5000.00 | Friday | $0.00000 | $0.00001 | Fail |
| W-138 | No | 0.01 | 5000.00 | Saturday | $0.00000 | $0.00001 | Fail |
| W-139 | No | 0.01 | 5000.00 | Sunday | $0.00000 | $0.00001 | Fail |
| W-140 | No | 0.01 | 5000.00 | Monday | $0.00000 | $0.00001 | Fail |
| W-141 | No | 0.01 | 5000.01 | Friday | $0.00000 | $0.00001 | Fail |
| W-142 | No | 0.01 | 5000.01 | Saturday | $0.00000 | $0.00001 | Fail |
| W-143 | No | 0.01 | 5000.01 | Sunday | $0.00000 | $0.00001 | Fail |
| W-144 | No | 0.01 | 5000.01 | Monday | $0.00000 | $0.00001 | Fail |
| W-145 | No | 0.02 | 0.00 | Friday | $0.00006 | $0.00004 | Fail |
| W-146 | No | 0.02 | 0.00 | Saturday | $0.00006 | $0.00004 | Fail |
| W-147 | No | 0.02 | 0.00 | Sunday | $0.00006 | $0.00004 | Fail |
| W-148 | No | 0.02 | 0.00 | Monday | $0.00006 | $0.00004 | Fail |
| W-149 | No | 0.02 | 0.01 | Friday | $0.00006 | $0.00004 | Fail |
| W-150 | No | 0.02 | 0.01 | Saturday | $0.00006 | $0.00004 | Fail |
| W-151 | No | 0.02 | 0.01 | Sunday | $0.00006 | $0.00004 | Fail |
| W-152 | No | 0.02 | 0.01 | Monday | $0.00006 | $0.00004 | Fail |
| W-153 | No | 0.02 | 999.99 | Friday | $0.00006 | $0.00004 | Fail |
| W-154 | No | 0.02 | 999.99 | Saturday | $0.00006 | $0.00004 | Fail |
| W-155 | No | 0.02 | 999.99 | Sunday | $0.00006 | $0.00004 | Fail |
| W-156 | No | 0.02 | 999.99 | Monday | $0.00006 | $0.00004 | Fail |
| W-157 | No | 0.02 | 1000.00 | Friday | $0.00002 | Matched expected | Pass |
| W-158 | No | 0.02 | 1000.00 | Saturday | $0.00002 | Matched expected | Pass |
| W-159 | No | 0.02 | 1000.00 | Sunday | $0.00002 | Matched expected | Pass |
| W-160 | No | 0.02 | 1000.00 | Monday | $0.00002 | Matched expected | Pass |
| W-161 | No | 0.02 | 1000.01 | Friday | $0.00002 | Matched expected | Pass |
| W-162 | No | 0.02 | 1000.01 | Saturday | $0.00002 | Matched expected | Pass |
| W-163 | No | 0.02 | 1000.01 | Sunday | $0.00002 | Matched expected | Pass |
| W-164 | No | 0.02 | 1000.01 | Monday | $0.00002 | Matched expected | Pass |
| W-165 | No | 0.02 | 2500.00 | Friday | $0.00002 | Matched expected | Pass |
| W-166 | No | 0.02 | 2500.00 | Saturday | $0.00002 | Matched expected | Pass |
| W-167 | No | 0.02 | 2500.00 | Sunday | $0.00002 | Matched expected | Pass |
| W-168 | No | 0.02 | 2500.00 | Monday | $0.00002 | Matched expected | Pass |
| W-169 | No | 0.02 | 4999.99 | Friday | $0.00002 | Matched expected | Pass |
| W-170 | No | 0.02 | 4999.99 | Saturday | $0.00002 | Matched expected | Pass |
| W-171 | No | 0.02 | 4999.99 | Sunday | $0.00002 | Matched expected | Pass |
| W-172 | No | 0.02 | 4999.99 | Monday | $0.00002 | Matched expected | Pass |
| W-173 | No | 0.02 | 5000.00 | Friday | $0.00000 | $0.00002 | Fail |
| W-174 | No | 0.02 | 5000.00 | Saturday | $0.00000 | $0.00002 | Fail |
| W-175 | No | 0.02 | 5000.00 | Sunday | $0.00000 | $0.00002 | Fail |
| W-176 | No | 0.02 | 5000.00 | Monday | $0.00000 | $0.00002 | Fail |
| W-177 | No | 0.02 | 5000.01 | Friday | $0.00000 | $0.00002 | Fail |
| W-178 | No | 0.02 | 5000.01 | Saturday | $0.00000 | $0.00002 | Fail |
| W-179 | No | 0.02 | 5000.01 | Sunday | $0.00000 | $0.00002 | Fail |
| W-180 | No | 0.02 | 5000.01 | Monday | $0.00000 | $0.00002 | Fail |
| W-181 | No | 100.00 | 0.00 | Friday | $0.30000 | $0.20000 | Fail |
| W-182 | No | 100.00 | 0.00 | Saturday | $0.30000 | $0.20000 | Fail |
| W-183 | No | 100.00 | 0.00 | Sunday | $0.30000 | $0.20000 | Fail |
| W-184 | No | 100.00 | 0.00 | Monday | $0.30000 | $0.20000 | Fail |
| W-185 | No | 100.00 | 0.01 | Friday | $0.30000 | $0.20000 | Fail |
| W-186 | No | 100.00 | 0.01 | Saturday | $0.30000 | $0.20000 | Fail |
| W-187 | No | 100.00 | 0.01 | Sunday | $0.30000 | $0.20000 | Fail |
| W-188 | No | 100.00 | 0.01 | Monday | $0.30000 | $0.20000 | Fail |
| W-189 | No | 100.00 | 999.99 | Friday | $0.30000 | $0.20000 | Fail |
| W-190 | No | 100.00 | 999.99 | Saturday | $0.30000 | $0.20000 | Fail |
| W-191 | No | 100.00 | 999.99 | Sunday | $0.30000 | $0.20000 | Fail |
| W-192 | No | 100.00 | 999.99 | Monday | $0.30000 | $0.20000 | Fail |
| W-193 | No | 100.00 | 1000.00 | Friday | $0.10000 | Matched expected | Pass |
| W-194 | No | 100.00 | 1000.00 | Saturday | $0.10000 | Matched expected | Pass |
| W-195 | No | 100.00 | 1000.00 | Sunday | $0.10000 | Matched expected | Pass |
| W-196 | No | 100.00 | 1000.00 | Monday | $0.10000 | Matched expected | Pass |
| W-197 | No | 100.00 | 1000.01 | Friday | $0.10000 | Matched expected | Pass |
| W-198 | No | 100.00 | 1000.01 | Saturday | $0.10000 | Matched expected | Pass |
| W-199 | No | 100.00 | 1000.01 | Sunday | $0.10000 | Matched expected | Pass |
| W-200 | No | 100.00 | 1000.01 | Monday | $0.10000 | Matched expected | Pass |
| W-201 | No | 100.00 | 2500.00 | Friday | $0.10000 | Matched expected | Pass |
| W-202 | No | 100.00 | 2500.00 | Saturday | $0.10000 | Matched expected | Pass |
| W-203 | No | 100.00 | 2500.00 | Sunday | $0.10000 | Matched expected | Pass |
| W-204 | No | 100.00 | 2500.00 | Monday | $0.10000 | Matched expected | Pass |
| W-205 | No | 100.00 | 4999.99 | Friday | $0.10000 | Matched expected | Pass |
| W-206 | No | 100.00 | 4999.99 | Saturday | $0.10000 | Matched expected | Pass |
| W-207 | No | 100.00 | 4999.99 | Sunday | $0.10000 | Matched expected | Pass |
| W-208 | No | 100.00 | 4999.99 | Monday | $0.10000 | Matched expected | Pass |
| W-209 | No | 100.00 | 5000.00 | Friday | $0.00000 | $0.10000 | Fail |
| W-210 | No | 100.00 | 5000.00 | Saturday | $0.00000 | $0.10000 | Fail |
| W-211 | No | 100.00 | 5000.00 | Sunday | $0.00000 | $0.10000 | Fail |
| W-212 | No | 100.00 | 5000.00 | Monday | $0.00000 | $0.10000 | Fail |
| W-213 | No | 100.00 | 5000.01 | Friday | $0.00000 | $0.10000 | Fail |
| W-214 | No | 100.00 | 5000.01 | Saturday | $0.00000 | $0.10000 | Fail |
| W-215 | No | 100.00 | 5000.01 | Sunday | $0.00000 | $0.10000 | Fail |
| W-216 | No | 100.00 | 5000.01 | Monday | $0.00000 | $0.10000 | Fail |
