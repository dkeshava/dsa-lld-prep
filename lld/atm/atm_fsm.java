
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
    WithDraw
}

class Card{
    private String cardNumber;
    private int pin; 
    private String accountNumber;
    public Card(String cardNumber, int pin, String accountNumber){
        this.accountNumber=accountNumber;
        this.pin=pin;
        this.cardNumber=cardNumber;
    }
    public String getCardNumber(){
        return cardNumber;
    }
    public String getAccountNumber(){
        return accountNumber;
    }
    public boolean validatePin(int givenPin){
        return this.pin==givenPin;
    }
}
class Account{
    private String accountNumber;
    private int balance;
    public Account(String accountNumber,int initalBalance){
        this.accountNumber=accountNumber;
        this.balance=initalBalance;
    }
    public void withDraw(int amount){
        if(amount>balance){
            throw new IllegalArgumentException("amount requested is greater than balance, please try again with a smaller amount!");
        }
        else{
            balance-=amount;
        }
    }
    public void depositCash(int amount){
        balance+=amount;
    }
    public int getBalance(){return balance;}
    public String getAccountNumber(){return accountNumber;}
}
class ATMInventory{
    Map<CashType,Integer> cashInventory;
    public ATMInventory(){
        this.cashInventory=new HashMap<>();
        initializeInventory();
    }
    private void initializeInventory(){
        cashInventory.put(CashType.Rs100, 50);
        cashInventory.put(CashType.Rs50, 100);
        cashInventory.put(CashType.Rs20, 100);
        cashInventory.put(CashType.Rs10, 100);
        cashInventory.put(CashType.Rs5, 100);
        cashInventory.put(CashType.Rs1, 100);
    }
    public int getTotalCash(){
        int totalCash=0;
        for(Map.Entry<CashType,Integer> entry: cashInventory.entrySet()){
            totalCash+=(entry.getKey().value*entry.getValue());
        }
        return totalCash;
    }
    public boolean hasSufficientBalance(int amount){
        return getTotalCash()>=amount;
    }
    public Map<CashType,Integer> dispenseCash(int amount){
        if(!hasSufficientBalance(amount)) return null;
        Map<CashType,Integer> dispensedCash=new HashMap<>();
        int remainingAmount=amount;
        for(CashType cashType: CashType.values()){
            Integer count=Math.min(remainingAmount/cashType.value, cashInventory.get(cashType));
            if(count>0){
                dispensedCash.put(cashType, count);
                remainingAmount-=cashType.value*count;
                cashInventory.put(cashType, cashInventory.get(cashType)-count);
            }
        }
        //unable to dispense exact cash amount
        if(remainingAmount>0){
            for(Map.Entry<CashType,Integer> entry: dispensedCash.entrySet()){
                cashInventory.put(entry.getKey(), cashInventory.get(entry.getKey())+entry.getValue());
            }
            return null;
        }
        return dispensedCash;
    }
    public void addCash(Map<CashType,Integer> cash){
        for(Map.Entry<CashType,Integer> entry: cash.entrySet()){
            cashInventory.put(entry.getKey(), cashInventory.get(entry.getKey())+entry.getValue());
        }
    }
    public void addCash(CashType cashType,int count){
        cashInventory.put(cashType,cashInventory.get(cashType)+count);
    }
    public void deductCash(CashType cashType,int count){
        cashInventory.put(cashType,cashInventory.get(cashType)-count);
    }
}
interface ATMState{
    String getStateName();
    ATMState next(ATMContext context);
    void printPrompt();
}
class IdleState implements ATMState{
    // public IdleState() {
    //     System.out.println("ATM is in idle state: Please insert your card!");
    // }
    @Override 
    public void printPrompt(){
        System.out.println("ATM is idle. Please insert your card.");
    }
    @Override
    public String getStateName(){
        return "IdleState";
    }
    @Override
    public ATMState next(ATMContext atm){
        Card insertedCard=atm.getInsertedCard();
        if(insertedCard!=null){
            return atm.getAtmStateFactory().createHasCardState();
        }
        else return this;
    }
}

class HasCardState implements ATMState{
    @Override 
    public void printPrompt(){
        System.out.println("Card inserted. Please enter PIN.");
    }
    @Override
    public String getStateName(){
        return "HasCardState";
    }
    @Override
    public ATMState next(ATMContext atm){
        if(atm.getInsertedCard()!=null){
            //System.out.println("Please select an operation to perform");
            return atm.getAtmStateFactory().createSelectOperationState();
        }
        else return atm.getAtmStateFactory().createIdleState();
    }
}
class SelectOperationState implements ATMState{
    // public SelectOperationState(){
    //     System.out.println("Pin is validated successfully, please select the next operation");
    // }
    @Override 
    public void printPrompt(){
        System.out.println("Select operation:");
        System.out.println("1. Check Balance");
        System.out.println("2. Withdraw");
    }
    @Override
    public String getStateName(){return "SelectOperationState";}
    @Override
    public ATMState next(ATMContext atm){
        if(atm.getInsertedCard()==null) return atm.getAtmStateFactory().createIdleState();
        if(null==atm.getSelectedOperation())return this;
        else switch (atm.getSelectedOperation()) {
            case CheckBalance:
                Card card=atm.getInsertedCard();
                Account account=atm.getBankService().getAccount(card.getAccountNumber());
                System.out.println("Your account balance is :"+account.getBalance()+"rs");
                //printPrompt();
                return this;
            case WithDraw:
                return atm.getAtmStateFactory().createTransactionState();
            default:
                return this;
        }
    }
}
class TransactionState implements ATMState{
    @Override
    public void printPrompt() {
        System.out.println("Transaction state. Enter amount.");
    }
    @Override
    public String getStateName(){
       return "TransactionState";
    }
    @Override
    public ATMState next(ATMContext atm){
        if(atm.getInsertedCard()==null) return atm.getAtmStateFactory().createIdleState();
        //go back to select operation after transaction
        return atm.getAtmStateFactory().createSelectOperationState();
    }
}

class ATMStateFactory{
    private static ATMStateFactory instance=null;
    private ATMStateFactory(){}
    public static ATMStateFactory getInstance(){
        if(instance==null){
            instance=new ATMStateFactory();
        }
        return instance;
    }
    public ATMState createIdleState(){return new IdleState();}
    public ATMState createHasCardState() {return new HasCardState();}
    public ATMState createSelectOperationState() {return new SelectOperationState();}
    public ATMState createTransactionState() {return new TransactionState();}
}

class BankService{
    Map<String, Account> accounts;
    public BankService(){
        this.accounts=new HashMap<>();
    }
    public void addAccount(Account account){
        accounts.put(account.getAccountNumber(), account);
    }
    public Account getAccount(String accountNumber){return accounts.get(accountNumber);}
}

class ATMContext{
    private ATMState currentState;
    private Card insertedCard;
    private ATMInventory atmInventory;
    private ATMStateFactory atmStateFactory;
    private TransactionType selectedOperation;
    private BankService bank;
    public ATMContext(){
        this.atmStateFactory=ATMStateFactory.getInstance();
        this.currentState=atmStateFactory.createIdleState();
        this.insertedCard=null;
        this.atmInventory=new ATMInventory();
        this.bank=new BankService();
        currentState.printPrompt();
    }
    public void advanceState(){
        ATMState nextState=currentState.next(this);
        currentState=nextState;
        System.out.println("current state: "+currentState.getStateName());
        currentState.printPrompt();
    }
    public void insertCard(Card card){
        if(currentState instanceof IdleState){
            insertedCard=card;
            advanceState();
        }
        else{
            System.out.println("Can't insert card in: "+currentState.getStateName());
        }
    }
    public void enterPin(int pin){
        if(currentState instanceof HasCardState){
            boolean validPin=insertedCard.validatePin(pin);
            if(validPin){
                System.out.println("PIN authenticated successfully");
                advanceState();
            }
            else{
                System.out.println("please enter valid pin!");
            }
        }
        else{
            System.out.println("can't enter pin in "+currentState.getStateName());
        }
    }
    public void selectOperation(TransactionType transactionType){
        if(currentState instanceof SelectOperationState){
            this.selectedOperation=transactionType;
            advanceState();
        }
        else System.out.println("Can't select operation in: "+currentState.getStateName());
    }
    public void performTransaction(int amount){
        if(currentState instanceof TransactionState){
            Account account=bank.getAccount(insertedCard.getAccountNumber());
            if(account.getBalance()<amount){
                System.out.println("Insufficient account balance");
                return;
            }
            if(!atmInventory.hasSufficientBalance(amount)){
                System.out.println("ATM has insufficient cash");
                return;
            }
            Map<CashType,Integer> dispensedCash=atmInventory.dispenseCash(amount);
            if(dispensedCash==null){
                System.out.println("Cannot dispense exact amount");
                return;
            }
            account.withDraw(amount);
            System.out.println("Cash dispensed: " + dispensedCash);
            System.out.println("Remaining balance: " + account.getBalance());

            advanceState();
        }
        else{
            System.out.println("Can't withdraw amount in: "+currentState.getStateName());
        }
    }
    public void cancelTransaction(){
        if(currentState instanceof TransactionState){
            System.out.println("Cancelling transaction!");
            ejectCard();
        }
        else{
            System.out.println("No transaction to cancel in: "+currentState.getStateName());
        }
    }
    public void ejectCard(){
        System.out.println("Returning card to the customer!");
        resetAtm();
    }
    public void resetAtm(){
        this.insertedCard=null;
        this.selectedOperation=null;
        this.currentState=atmStateFactory.createIdleState();
    }
    //getters and setters
    public ATMState getCurrentState(){return currentState;}
    public void setState(ATMState state){this.currentState=state;}
    public ATMInventory getAtmInventory(){return this.atmInventory;}
    public Card getInsertedCard(){return this.insertedCard;}
    public ATMStateFactory getAtmStateFactory(){return atmStateFactory;}
    public TransactionType getSelectedOperation(){return selectedOperation;}
    public BankService getBankService(){return bank;}
}

public class atm_fsm {
    public static void main(String[] args) {
        ATMContext atm=new ATMContext();
        Card card=new Card("1234", 2278, "1010");
        Account account=new Account("1010", 7000);
        atm.getBankService().addAccount(account);
        atm.insertCard(card);
        atm.enterPin(2278);
        atm.selectOperation(TransactionType.CheckBalance);
        atm.selectOperation(TransactionType.WithDraw);
        atm.cancelTransaction();
        //atm.performTransaction(900);
        atm.performTransaction(6999);
    }
}
