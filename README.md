
# **Bank Account Management System**

This project is a **Java-based Bank Account Management System** that models a banking application. It allows operations such as adding users, managing accounts and cards, handling payments, and converting between currencies. The system is modular and implements clean design principles to ensure flexibility and scalability.

---

## **Project Overview**

The system revolves around a **`BankInputHandler`** class that processes user commands and delegates tasks to specific classes and functions.

Key components of the system include:

1. **Bank** - Manages users and their accounts.
2. **User** - Represents a user with one or more accounts.
3. **Account** - Represents a bank account, managing cards and transactions.
4. **Card and OtpCard** - Represents payment cards (normal or OTP-based).
5. **Payment Strategies** - Allows different types of payments with validations.
6. **CurrencyExchanger** - Converts between different currencies.

---

## **Class Structure and Details**

### **1. Bank**

- **Description**: This class represents the bank and manages users.
- **Key Fields**:
    - `LinkedHashMap<String, User> users`
        - Users are stored using their **email** as a key.
        - The **LinkedHashMap** ensures O(1) operations for add, get, and remove while maintaining insertion order.
- **Responsibilities**:
    - Add, remove, and retrieve users efficiently.

---

### **2. User**

- **Description**: Represents a user in the bank system.
- **Key Fields**:
    - `LinkedHashMap<String, Account> accounts`
        - Accounts are stored using their **ID** or identifier as the key.
        - The **LinkedHashMap** ensures O(1) operations and preserves the order accounts were added.
- **Responsibilities**:
    - Manage user accounts.

---

### **3. Account**

- **Description**: Represents a bank account owned by a user.
- **Key Fields**:
    - `LinkedHashMap<String, Card> cards`
        - Stores cards belonging to the account.
    - `ArrayList<Transaction> transactions`
        - Stores all transactions made from the account, even if initiated via a card.
- **Responsibilities**:
    - Manage account-related operations like payments, card management, and transactions.

---

### **4. Card**

- **Description**: Represents a standard card.
- **Responsibilities**:
    - Execute payments.
    - Manage card-specific behaviors such as freezing.

#### **OtpCard (Extends Card)**

- **Description**: A one-time payment card that is destroyed after a single use.
- **Key Fields**:
    - `private final Account owner`
        - Holds a reference to the account that owns this OTP card.
- **Behavior**:
    - When `executePayment()` is called, the OTP card is destroyed, and a new one is added to the **Account**.

---

## **5. Payment Strategies**

The system implements the **Strategy Design Pattern** for handling different payment methods.

### **PaymentStrategy (Interface)**

- Defines the behavior for executing payments.

### **AccountPayment (Implements PaymentStrategy)**

- **Description**: Executes a payment directly from an account.
- **Behavior**:
    - First validates if the payment can be made.

### **SendMoneyAccountPayment (Extends AccountPayment)**

- **Description**: Executes a payment and overrides the `reportSuccessMethod()` to add custom behavior when the payment succeeds.

### **CardPayment (Extends AccountPayment)**

- **Description**: Executes a payment using a card.
- **Behavior**:
    - Performs all validations required for the account and verifies if the card is **frozen** before processing the payment.

---

## **6. CurrencyExchanger**

- **Description**: Converts between two currencies using exchange rates.
- **Implementation**:
    - Maintains a **Map** of currency rates.
    - Contains an internal **Graph** class to handle multi-step conversions if no direct rate exists between two currencies.

- **Behavior**:
    - Allows conversion between two currencies, even when no direct exchange rate is available.

---

## **Main Class: BankInputHandler**

- **Description**: Processes user commands and delegates tasks to the appropriate class and function.
- **Responsibilities**:
    - Acts as the central input handler.
    - Calls the appropriate methods for user management, payments, card management, and currency exchange.

---


## **Conclusion**

This Bank Account Management System is designed for flexibility and performance. By using `LinkedHashMap` for O(1) operations and the **Strategy Design Pattern** for payments, the system ensures clean and efficient implementation.

