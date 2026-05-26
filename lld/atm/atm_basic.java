
import java.util.HashMap;
import java.util.Map;


enum CashType{
    Rs100(100),
    Rs50(50),
    Rs20(20),
    Rs10(10),
    Rs5(5),
    Rs1(1);

    public final int value;
    CashType(int value){
        this.value=value;
    }
}

enum TransactionType{
    CheckBalance,
    WithDrawCash
}

class Card{
    private String cardNumber;
    private int pin;
    private String accountNumber;
    public Card(String cardNumber, int pin, String accountNumber){
        this.cardNumber=cardNumber;
        this.pin=pin;
        this.accountNumber=accountNumber;
    }
    public String getCardNumber() {return cardNumber;}
    public boolean validatePin(int eneteredPin){
        return pin==eneteredPin;
    }
    public String getAccountNumber(){return accountNumber;}
}

class Account{
    private String accountNumber;
    private double balance;
    public Account(String accountNumber, double initalBalance){
        this.accountNumber=accountNumber;
        this.balance=initalBalance;
    }
    public String getAccountNumber(){return accountNumber;}
    public double getBalance(){return balance;}
    public boolean withDraw(int amount){
        if(balance>=amount){
            balance-=amount;
            return true;
        }
        return false;
    }
    public void deposit(int amount){
        balance+=amount;
    }
}

class ATMInventory{
    private Map<CashType, Integer> cashInventory;
    public ATMInventory(){
        this.cashInventory=new HashMap<>();
        initializeInventory();
    }
    private void initializeInventory(){
        cashInventory.put(CashType.Rs100, 10);
        cashInventory.put(CashType.Rs50, 10);
        cashInventory.put(CashType.Rs20, 20);
        cashInventory.put(CashType.Rs10, 30);
        cashInventory.put(CashType.Rs5, 20);
        cashInventory.put(CashType.Rs1, 50);        
    }
    public int getTotalCash(){
        int totalCash=0;
        for(Map.Entry<CashType,Integer> entry: cashInventory.entrySet()){
            CashType cashType=entry.getKey();
            int count=entry.getValue();
            totalCash+=(cashType.value*count);
        }
        return totalCash;
    }
    public boolean hasSufficientBalance(int amount){
        return getTotalCash()>=amount;
    }
    public void addCash(CashType type,int count){
        cashInventory.put(type, cashInventory.get(type)+count);
    }
    public void deductCash(CashType type,int count){
        if(cashInventory.get(type)<count){
            throw new IllegalArgumentException("don't have sufficient count of cashtype to deduct");
        }
        cashInventory.put(type, cashInventory.get(type)-count);
    }
    public Map<CashType,Integer> dispenseCash(int amount){
        if(!hasSufficientBalance(amount)){
            return null;
        }
        Map<CashType,Integer> dispensedCash=new HashMap<>();
        int remainingAmount=amount;
        for(CashType cashType: CashType.values()){
            int count=Math.min(remainingAmount/cashType.value, cashInventory.get(cashType));
            if(count>0){
                dispensedCash.put(cashType, count);
                remainingAmount-=cashType.value*count;
                cashInventory.put(cashType, cashInventory.get(cashType)-count);
            }
        }
        if(remainingAmount>0){
            for(Map.Entry<CashType,Integer> entry: dispensedCash.entrySet()){
                cashInventory.put(entry.getKey(), cashInventory.get(entry.getKey())+entry.getValue());
            }
            return null;
        }
        return dispensedCash;
    }
}

interface ATMState{
    void insertCard(ATM atm, Card card);
    void authenticatPin(ATM atm, int pin);
    void selectOperation(ATM atm, TransactionType transactionType);
    void performTransaction(ATM atm, int amount);
    void ejectCard(ATM atm);
}

class IdleState implements ATMState{
    public void insertCard(ATM atm,Card card){
        atm.setInsertedCard(card);
        atm.setState(new HasCardState());
        System.out.println("Card Inserted");
    }
    public void authenticatPin(ATM atm, int pin){
        System.out.println("Insert card first");
    }
    public void selectOperation(ATM atm, TransactionType type){
        System.out.println("Insert card first");
    }

    public void performTransaction(ATM atm, int amount){
        System.out.println("Insert card first");
    }

    public void ejectCard(ATM atm){
        System.out.println("No card inserted");
    }
}

class HasCardState implements ATMState{
    public void insertCard(ATM atm,Card card){
        System.out.println("Card already inserted :(");
    }
    public void authenticatPin(ATM atm, int pin){
        Card card=atm.getInsertedCard();
        if(!card.validatePin(pin)){
            System.out.println("Entered pin is invalid. Please try again!");
            return;
        }
        System.out.println("Pin validated successfully");
        atm.setState(new SelectOperationState());
        System.out.println("Please select operation");
    }
    public void selectOperation(ATM atm, TransactionType type){
        System.out.println("validate pin first");
    }

    public void performTransaction(ATM atm, int amount){
        System.out.println("validate pin first");
    }

    public void ejectCard(ATM atm){
        atm.ejectCard();
    }
}

class SelectOperationState implements ATMState{
    public void insertCard(ATM atm,Card card){
        System.out.println("Card already inserted :(");
    }
    public void authenticatPin(ATM atm, int pin){
        System.out.println("Pin already validated. Please select operation");
    }
    public void selectOperation(ATM atm, TransactionType type){
        if(type==TransactionType.CheckBalance){
            atm.setState(new CheckBalanceState());
        }
        else{
            atm.setState(new WithDrawCashState());
        }
        System.out.println("Operation selected: " + type);
    }

    public void performTransaction(ATM atm, int amount){
        System.out.println("Select Operation first");
    }

    public void ejectCard(ATM atm){
        atm.ejectCard();
    }
}
class CheckBalanceState implements ATMState{
    public void insertCard(ATM atm,Card card){
        System.out.println("Card already inserted :(");
    }
    public void authenticatPin(ATM atm, int pin){
        System.out.println("Pin already validated");
    }
    public void selectOperation(ATM atm, TransactionType type){
        System.out.println("Transaction type already selected");
    }

    public void performTransaction(ATM atm, int amount){
        Card card=atm.getInsertedCard();
        Account account=atm.getBankService().getAccount(card.getAccountNumber());
        System.out.println("Your account balance is :"+account.getBalance()+"rs");
        atm.ejectCard();
    }

    public void ejectCard(ATM atm){
        atm.ejectCard();
    }
}
class WithDrawCashState implements ATMState{
    public void insertCard(ATM atm,Card card){
        System.out.println("Card already inserted :(");
    }
    public void authenticatPin(ATM atm, int pin){
        System.out.println("Pin already validated");
    }
    public void selectOperation(ATM atm, TransactionType type){
        System.out.println("Transaction type already selected");
    }

    public void performTransaction(ATM atm, int amount){
        Card card=atm.getInsertedCard();
        Account account=atm.getBankService().getAccount(card.getAccountNumber());
        if(account.getBalance()<amount){
            System.out.println("Insufficient account balance");
            return;
        }
        if(!atm.getInventory().hasSufficientBalance(amount)){
            System.out.println("ATM has insufficient cash");
            return;
        }
        Map<CashType,Integer> dispensedCash=atm.getInventory().dispenseCash(amount);
        if(dispensedCash==null){
            System.out.println("Cannot dispense exact amount");
            return;
        }
        account.withDraw(amount);
        System.out.println("Cash dispensed: " + dispensedCash);
        System.out.println("Remaining balance: " + account.getBalance());

        atm.ejectCard();
    }

    public void ejectCard(ATM atm){
        atm.ejectCard();
    }
}

class BankService{
    private Map<String, Account> accounts;
    public BankService(){
        this.accounts=new HashMap<>();
    }
    public void addAccount(Account account){
        accounts.put(account.getAccountNumber(), account);
    }
    public Account getAccount(String accountNumber){
        return accounts.get(accountNumber);
    }
}
class ATM{
    private ATMState currentState;
    private ATMInventory atmInventory;
    private Card insertedCard;
    private BankService bankService;
    public ATM(BankService bankService){
        this.currentState=new IdleState();
        this.atmInventory=new ATMInventory();
        this.bankService=bankService;
    }
    public void setState(ATMState state){
        this.currentState=state;
    }
    public ATMState getState(){
        return currentState;
    }
    public void setInsertedCard(Card card){
        this.insertedCard=card;
    }
    public Card getInsertedCard(){
        return insertedCard;
    }
    public void ejectCard(){
        this.insertedCard=null;
        setState(new IdleState());
        System.out.println("Card ejected: Please insert a card!");
    }
    public ATMInventory getInventory(){
        return atmInventory;
    }

    public BankService getBankService(){
        return bankService;
    }
    public void insertCard(Card card){
        currentState.insertCard(this, card);
    }
    public void authenticatPin(int pin){
        currentState.authenticatPin(this, pin);
    }
    public void selectOperation(TransactionType selectedTransaction){
        currentState.selectOperation(this, selectedTransaction);
    }
    public void performTransaction(int amount){
        currentState.performTransaction(this, amount);
    }
}

public class atm_basic {
    public static void main(String[] args) {
        BankService bank=new BankService();
        ATM atm=new ATM(bank);
        Account account1=new Account("1234", 300);
        Card card1=new Card("123456", 3378, "1234");
        bank.addAccount(account1);
        atm.insertCard(card1);
        atm.ejectCard();
        atm.authenticatPin(3378);
        atm.selectOperation(TransactionType.WithDrawCash);
        atm.performTransaction(290); 
    }
}
