# Jared - Withdrawal Fees Black Box Tests (bank.utils.FeesCalculator)

**JUnit class** WithdrawalTestFee.java 
**Technique** Robust Worst Case Boundary Value Analysis

## My part

I tested the withdrawal fee returned by FeesCalculator.calculateWithdrawalFee. The assignment page sets student fees by day and non student fees by starting balance.

| Customer | Condition | Expected fee |
| --- | --- | --- |
| Student | Weekday | No fee |
| Student | Weekend | 0.1% of the withdrawal |
| Non-student | Balance below $1,000 | 0.3% of the withdrawal |
| Non-student | Balance at least $1,000 and below $5,000 | 0.1% of the withdrawal |
| Non-student | Balance at least $5,000 | No fee |

## Process and assumptions

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

### JUnit test design

The assignment asked for Robust Worst Case Boundary Value Analysis, so I used JUnit 5 Method Sourcing to generate the combinations from the selected boundary values. Each combination runs as a separate parameterized invocation with its own case ID. The appendix lists every executed case, its inputs, its expected fee, and its observed result. The handout leaves the choice of JUnit argument source open.

## Test cases

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

## Test results

I ran the revised Withdrawal Fee Test with Maven in the Java 11 Docker container. Every invocation was checked against the Maven Surefire report. The commands below reproduce that run.

~~~powershell
docker compose run --rm atm mvn -B checkstyle:check
docker compose run --rm atm mvn -B test '-Dtest=WithdrawalFeeTest'
~~~

For passing cases, JUnit confirms that the observed value matched the expected fee within tolerance. For failed cases, the appendix records the value from the assertion message.

## Fault localization

| Group | Failed cases | Location and justification |
| --- | ---: | --- |
| F-1 | 54 | Line 13 assigns no fee for a student on the weekend, where the handout specifies 0.1%. |
| F-2 | 54 | Line 15 assigns a 0.1% fee for a student on a weekday, where the handout specifies no fee. |
| F-3 | 36 | Line 19 assigns 0.2% for non-student balances below $1,000, where the handout specifies 0.3%. |
| F-4 | 24 | Line 20 uses $10,000 as the upper cutoff for the 0.1% rate, where the handout specifies $5,000. |

Line numbers refer to the Fees Calculator file, specifically the method calculateWithdrawalFee.
Note: I used Codex Work to record the results as shown below:

- **F-1** — W-2, W-3, W-6, W-7, W-10, W-11, W-14, W-15, W-18, W-19, W-22, W-23, W-26, W-27, W-30, W-31, W-34, W-35, W-38, W-39, W-42, W-43, W-46, W-47, W-50, W-51, W-54, W-55, W-58, W-59, W-62, W-63, W-66, W-67, W-70, W-71, W-74, W-75, W-78, W-79, W-82, W-83, W-86, W-87, W-90, W-91, W-94, W-95, W-98, W-99, W-102, W-103, W-106, W-107
- **F-2** — W-1, W-4, W-5, W-8, W-9, W-12, W-13, W-16, W-17, W-20, W-21, W-24, W-25, W-28, W-29, W-32, W-33, W-36, W-37, W-40, W-41, W-44, W-45, W-48, W-49, W-52, W-53, W-56, W-57, W-60, W-61, W-64, W-65, W-68, W-69, W-72, W-73, W-76, W-77, W-80, W-81, W-84, W-85, W-88, W-89, W-92, W-93, W-96, W-97, W-100, W-101, W-104, W-105, W-108
- **F-3** — W-109, W-110, W-111, W-112, W-113, W-114, W-115, W-116, W-117, W-118, W-119, W-120, W-145, W-146, W-147, W-148, W-149, W-150, W-151, W-152, W-153, W-154, W-155, W-156, W-181, W-182, W-183, W-184, W-185, W-186, W-187, W-188, W-189, W-190, W-191, W-192
- **F-4** — W-137, W-138, W-139, W-140, W-141, W-142, W-143, W-144, W-173, W-174, W-175, W-176, W-177, W-178, W-179, W-180, W-209, W-210, W-211, W-212, W-213, W-214, W-215, W-216

## Appendix - Executed fee cases

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

## Final test summary

| Tests run | Passed | Failed assertions | Errors | Skipped |
| ---: | ---: | ---: | ---: | ---: |
| 216 | 48 | 168 | 0 | 0 |

Maven reported build failure after the assertion failures. The test run completed, and the failures identify differences from the stated fee rules.

