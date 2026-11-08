package atm.session;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import atm.ATM;
import atm.dispatcher.MessageDispatcher;
import atm.exceptions.InvalidAmountException;
import atm.exceptions.InvalidPinFormatException;
import atm.session.transactions.ATMWithdrawal;
import atm.ui.panels.MainPanel;
import atm.utils.CredentialsCheck;
import atm.utils.FormatChecker;
import bank.transactions.utils.AccountType;
import bank.transactions.utils.TransactionResult;

// Black-box tests for the ATM session, withdrawal only.
// Spec: Assignment 1, "ATM Session" (PIN = 5 digits; amount in $20/$50 bills, max $1,000 daily limit).
// Technique: Robust Worst Case BVA, single fault assumption. Test-case IDs match docs/assignment1.md.
class SessionTest {
  private static final String CARD = "4000000000000000";
  private static final String NOMINAL_PIN = "55555";

  private MessageDispatcher dispatcher;
  private MainPanel panel;

  @BeforeEach
  void setUp() {
    // Fake bank: accepts any card/PIN, and every transaction succeeds on a $5,000
    // account (assumption A6).
    dispatcher = Mockito.mock(MessageDispatcher.class);
    panel = Mockito.mock(MainPanel.class);
    Mockito.when(dispatcher.checkCredentials(any(), any())).thenReturn(true);
    Mockito.when(dispatcher.performTransaction(any(), any(), any()))
        .thenReturn(new TransactionResult(true, null, 0, new double[] { 5000 }));
  }

  // An ATM with a card inserted, waiting for the PIN (assumption A2).
  private ATM atmAtPinScreen() throws Exception {
    ATM atm = new ATM(new FormatChecker(), new CredentialsCheck(dispatcher), dispatcher);
    atm.setMainPanel(panel);
    atm.createSession();
    atm.checkCardNumber(CARD);
    return atm;
  }

  // A session that has chosen Withdrawal from Chequing and is waiting for the
  // amount (assumption A8).
  private Session sessionAtAmountScreen() {
    Session session = new Session(panel, dispatcher);
    session.insertCard();
    session.addCard(CARD);
    session.addPin(NOMINAL_PIN.toCharArray());
    session.setTransaction(new ATMWithdrawal());
    session.setFromAccount(AccountType.Chequing);
    return session;
  }

  // Spec: "Invalid PIN format: throws InvalidPinFormatException. A valid PIN is 5
  // digits."
  @ParameterizedTest(name = "{0}: PIN \"{1}\" is rejected")
  @CsvSource({
      "S-PIN-01, 5555",
      "S-PIN-03, 555555",
      "S-PIN-04, /5555",
      "S-PIN-09, :5555",
      "S-PIN-10, 5555/",
      "S-PIN-15, 5555:",
  })
  void invalidPinFormat(String id, String pin) throws Exception {
    ATM atm = atmAtPinScreen();
    assertThrows(InvalidPinFormatException.class, () -> atm.checkPin(pin.toCharArray()));
  }

  // Spec: "A valid PIN is 5 digits." Valid input should not throw.
  @ParameterizedTest(name = "{0}: PIN \"{1}\" is accepted")
  @CsvSource({
      "S-PIN-02, 55555",
      "S-PIN-05, 05555",
      "S-PIN-06, 15555",
      "S-PIN-07, 85555",
      "S-PIN-08, 95555",
      "S-PIN-11, 55550",
      "S-PIN-12, 55551",
      "S-PIN-13, 55558",
      "S-PIN-14, 55559",
  })
  void validPinFormat(String id, String pin) throws Exception {
    ATM atm = atmAtPinScreen();
    assertDoesNotThrow(() -> atm.checkPin(pin.toCharArray()));
  }

  // Spec: "Invalid amount: throws InvalidAmountException", $20/$50 bills, $1,000
  // daily limit.
  @ParameterizedTest(name = "{0}: amount {1} is rejected")
  @CsvSource({
      "S-AMT-01, 19",
      "S-AMT-03, 21",
      "S-AMT-05, 999",
      "S-AMT-07, 1001",
  })
  void invalidWithdrawalAmount(String id, int amount) {
    Session session = sessionAtAmountScreen();
    assertThrows(InvalidAmountException.class, () -> session.setAmount(amount));
  }

  // Spec: "A valid amount for withdrawal is one that can be dispensed in $20 and
  // $50 bills", $20/$50 bills, $1,000 daily limit.
  @ParameterizedTest(name = "{0}: amount {1} is accepted")
  @CsvSource({
      "S-AMT-02, 20",
      "S-AMT-04, 500",
      "S-AMT-06, 1000",
  })
  void validWithdrawalAmount(String id, int amount) {
    Session session = sessionAtAmountScreen();
    assertDoesNotThrow(() -> session.setAmount(amount));
  }
}
