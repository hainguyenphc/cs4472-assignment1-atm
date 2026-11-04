# Assignment 1 - ATM Session (`atm.session.Session`)

**Author:** Shane
**Testing target:** `atm.session.Session` - PIN format and withdrawal amount (transaction choice 1)
**Technique:** Robust Worst Case Boundary Value Analysis, single fault assumption
**JUnit class:** `src/test/java/atm/session/SessionTest.java`

## My part of the assignment

I performed black-box testing of the ATM session for the **withdrawal** transaction only. The specification
defines two kinds of invalid input for this part:

| Input | Valid when | On invalid input |
|---|---|---|
| PIN | it is exactly 5 digits | `InvalidPinFormatException` is thrown |
| Withdrawal amount | it can be dispensed in $20 and $50 bills, and does not exceed the $1,000 daily limit | `InvalidAmountException` is thrown |

The test account has a **daily limit of $1,000** and an **available balance of $5,000**. Since the balance is
larger than the daily limit, the daily limit ($1,000) is the upper boundary for a withdrawal.

## Section 1 - Process and assumptions

### Process

Robust Worst Case Boundary Value Analysis takes seven values for each input variable: **min-**, **min**,
**min+**, **nom**, **max-**, **max**, **max+**. *Robust* adds the two values just outside the valid range
(min- and max+). *Worst case* takes the Cartesian product of the values of all variables. Under the
**single fault assumption**, a test case may contain at most one invalid value. Every other input is held at
its nominal value, so any combination with two invalid values is dropped.

I identified the following input variables:

1. **Withdrawal amount** (integer dollars). Valid range: $20 (the smallest amount that can be dispensed, one
   $20 bill) to $1,000 (the daily limit). Because this is a single variable, worst case analysis gives the
   seven robust boundary values directly:

   | min- | min | min+ | nom | max- | max | max+ |
   |---|---|---|---|---|---|---|
   | 19 | 20 | 21 | 500 | 999 | 1000 | 1001 |

   The values 21 (min+) and 999 (max-) lie inside the numeric range but cannot be made from $20 and $50
   bills, so the specification makes them **invalid**. The nominal value (500) was chosen so that it is
   dispensable (10 x $50).

2. **PIN length.** The valid range is degenerate (min = max = nom = 5), so the robust values collapse to
   **4** (min-), **5** (valid) and **6** (max+).

3. **PIN digit value.** Each character must be a digit `'0'`-`'9'`. The robust values are the characters
   immediately outside that range in the character set: `'/'` (min-, just below `'0'`) and `':'` (max+, just
   above `'9'`). Together: `'/'`, `'0'`, `'1'`, `'5'` (nom), `'8'`, `'9'`, `':'`. I applied these values to
   the **boundary positions** of the PIN (position 1 and position 5) and held the interior positions at the
   nominal digit `'5'`.

The PIN cases are the worst case product of the valid length (5) with the seven digit values at each boundary
position, plus the two invalid lengths with nominal digits. A test with an invalid length *and* an invalid
digit would contain two faults and is excluded. Duplicate nominal cases were merged, giving **15 PIN cases**
and **7 amount cases** (22 in total).

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
  does not depend on the PIN value, so the two inputs are analysed separately. Amount test cases drive
  `Session` directly with the nominal PIN so that a PIN fault cannot mask the amount result.
- **A9 - Expected outcome for valid input.** For valid input the expected outcome is that no exception is
  thrown and the session moves on to the next step.
- **A10 - Credentials.** Credential checking (correct vs. incorrect PIN) is outside the scope of this part;
  the stub always accepts the credentials, so only the *format* of the PIN is tested.

## Section 2 - Test cases

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

## Test results

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

## Fault localization

Each entry below is one suspected fault. Failing test cases that expose the same fault are grouped under a
single entry.

### F-1: PIN length check expects 4 digits instead of 5

- **Failing test cases:** S-PIN-01, S-PIN-02, S-PIN-05, S-PIN-06, S-PIN-07, S-PIN-08, S-PIN-11, S-PIN-12,
  S-PIN-13, S-PIN-14
- **Location:** `atm.utils.FormatChecker`, method `checkPinFormat(char[])`, line 33: `if (pin.length == 4)`
- **Expected (specification):** A valid PIN is 5 digits, so every 5-digit PIN should be accepted and the 4-digit
  PIN `5555` should throw `InvalidPinFormatException`.
- **Observed:** The 4-digit PIN `5555` was accepted (no exception was thrown), and every valid 5-digit PIN was
  rejected with `InvalidPinFormatException`.
- **Justification:** The condition only accepts PINs of length 4, so a 4-digit PIN passes the digit check while
  every 5-digit PIN falls into the `else` branch (line 40) and throws.

Matching row for the fault-localization table in the PR description:

| Failing test | Suspected fault (file, method, line) | One-line justification |
  | --- | --- | --- |
  | S-PIN-01, 02, 05, 06, 07, 08, 11, 12, 13, 14 | FormatChecker.java, checkPinFormat, line 33 | The length check is `pin.length == 4` instead of 5, so a 4-digit PIN is accepted and every valid 5-digit PIN is rejected. |

### Suspected faults masked by F-1

  No failing test exposes these yet, because every 5-digit PIN is rejected at line 33 before they run.
  They are listed here because they would likely surface once F-1 is fixed.

- **M-1:** `FormatChecker.checkPinFormat`, line 35: `pin[i] >= '9'` rejects the digit `9`, although `9` is a valid digit. S-PIN-08 and S-PIN-14 would likely still fail after F-1 is fixed.
- **M-2:** `FormatChecker.checkPinFormat`, line 34: `for (int i = 0; i < 4; i++)` checks only the first four characters, so the fifth character is never validated. S-PIN-10 and S-PIN-15 would likely fail once 5-digit PINs reach the loop.
