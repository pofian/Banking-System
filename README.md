# **Bank Management System**

This project is a **Java-based Bank Management System** designed to simulate a banking application.
It supports a range of operations, including adding users, managing accounts and cards, processing payments, and currency conversion.
The system is modular and follows clean design principles, ensuring flexibility and scalability.

---

## **Project Overview**

The system centers around a **`BankInputHandler`** class, which processes user commands and delegates tasks to various classes and functions.

Key components of the system include:

1. **Bank** - Manages users, accounts, and transactions.
2. **Banker** - Facilitates user and account searches, and manages the state of pending payments.
3. **User** - Represents a user with one or more accounts, each linked to an **AccountSpendingHistory** for payment tracking.
4. **Account** - Represents a bank account, handling cards and transactions.
5. **Card and OtpCard** - Represents normal and OTP-based payment cards.
6. **Payment Strategies** - Implements various payment methods with validation.
7. **CurrencyExchanger** - Handles conversion between different currencies.

---

### **Key Interfaces**

#### **MoneyUser**

- **Description**: Ensures that all money-related entities have a valid IBAN and name, supporting transaction logging.

#### **MoneySender** (Extends `MoneyUser`)

- **Method**:
    - `payTo(MoneyReceiver receiver, MoneySum amount)`:
        - Sends money to a specified receiver.

#### **MoneyReceiver** (Extends `MoneyUser`)

- **Method**:
    - `addSum(MoneySum amount)`:
        - Adds the received amount to the balance.

---

## **Main Classes: Structure and Details**

### **Bank**

- **Description**: Represents the bank and manages user accounts.
- **Key Fields**:
    - `LinkedHashMap<String, User> users`
        - Users are stored using their **email** as a key.
        - **LinkedHashMap** ensures efficient operations (O(1)) and preserves insertion order.
- **Responsibilities**:
    - Add, remove, and retrieve users efficiently.
    - Retrieve commerciants based on their name on the IBAN of the account associated with them.

---

### **Banker**

- **Description**: Facilitates searching users, accounts, and cards in the bank by various identifiers.
- **Key Features**:
    - Perform efficient searches for users, accounts, commerciants and cards in the Bank.
    - Track and manage split payment statuses.

---

### **User**

- **Description**: Represents a user within the bank system.
- **Key Fields**:
    - `LinkedHashMap<String, Account> accounts`
        - Accounts are stored using their **IBAN** or identifier as the key.
        - **LinkedHashMap** ensures efficient operations and maintains account insertion order.
- **Responsibilities**:
    - Manage and operate user accounts.

---

### **Account**

- **Description**: Represents a bank account associated with a user. It implements both `MoneySender` and `MoneyReceiver`.
- **Key Features**:
    - Implements `payTo()` from `MoneySender` for transferring money to `MoneyReceiver` objects.
    - Handles commissions and cashback for transactions.
    - Provides access control methods for managers and employees in `BusinessAccount`.
    - Reports payments for business analysis.

#### **Account Types**:

1. **ClassicAccount**:
    - Standard account for personal banking.

2. **SavingsAccount**:
    - Account for saving funds with potential interest accumulation.

3. **BusinessAccount**:
    - Supports multiple users (e.g., managers and employees), allowing them to add funds, make payments, and create cards.
    - Overrides access control to allow non-owners (managers) to perform operations.
    - Provides business reporting for transaction analysis.

---

### **Payments**
- **Description**: Represents the invoker for executing payment operations.
- **Behavior**:
    - Validates and executes payments only if sufficient funds are available.

#### **Payment Methods**

The system includes several types of payment methods:

1. **SendMoneyPaymentMethod**:
    - Extends `AccountPaymentMethod`.

2. **CardPaymentMethod**:
    - An abstract class, extended by `CashWithdrawalPaymentMethod` and `PayOnlinePaymentMethod`.

3. **CustomSplitPaymentMethod** and **EqualSplitPaymentMethod**:
    - Extends the abstract `SplitPaymentMethod` to divide payments between multiple accounts.

#### **SplitPaymentMethod**

- **Description**: Handles splitting payments between multiple accounts.
- **Behavior**:
    - Validates and executes individual payments for each account.

---

### *MoneySum*
- **Description**: Represents a monetary sum with a `convert()` method for currency conversion.
- **Behavior**:
    - `convert()` avoids redundant calculations and operations when converting between the same currencies.

---

### **Commerciant**

- **Description**: Implements `MoneyReceiver` and represents merchants who receive payments.
- **Key Features**:
    - Maintains a static `Map<Account, AccountSpendingHistory>` to track spending history.
    - Offers cashback or rewards based on transaction history:
        1. **SpendingThresholdCommerciant**: Provides cashback based on total spending.
        2. **NrOfTransactionsCommerciant**: Rewards accounts based on transaction volume.

---

### **CurrencyExchanger**

- **Description**: Converts between currencies using stored exchange rates.
- **Implementation**:
    - Maintains a **Map** of currency exchange rates.
    - Uses an internal **Graph** class for multistep conversions when direct rates are unavailable.

---

## **Design Patterns**

Several design patterns have been employed to enhance flexibility, reuse code, enforce specific implementations, and ensure ease of modification:

- **Facade** - Used in the `Banker` class to simplify interactions with the `Bank`, serving as the only class that accesses methods in `Bank`.
- **Factory** - Found in `DatabaseFactory`, which creates instances of classes that extend abstract classes based on input.
- **Singleton** - Implemented in `OutputHandler`, `CurrencyExchanger`, `UnrecordedMoneyUser`, and the four plan types.
- **Proxy** - Applied in `CurrencyExchanger`, where the exchange graph is constructed only when necessary.
- **Flyweight** - In `MoneySum`, reusing the same object when the same currency conversion is requested to optimize memory usage.
- **Command Pattern** - In the `Payment` class, which invokes methods on a `PaymentMethod` interface, ensuring that payments are only executed after validation.
- **Observer** - In `BusinessAccount`, each `UserReport` object acts as an observer, notified when changes occur, such as adding balance or making payments.
- **Strategy** - Implements different behaviors for cashback and payment additions, with specific strategies for accounts and commerciants.
- **Visitor** - The `getCashback` method on `Commerciants` uses an `Account` parameter that acts as a visitor, applying specific behavior to different account types.

---

## **Conclusion**

This Bank Account Management System is designed for flexibility and efficiency. Its modular architecture, advanced account management features, and payment strategies offer a powerful platform for managing banking operations.
