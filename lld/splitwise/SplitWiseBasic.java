
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class User{
    private String userId;
    private String name;
    private String email;
    public User(String userId, String name, String email){
        this.userId=userId;
        this.name=name;
        this.email=email;
    }
    public String getUserId(){
        return userId;
    }
    public String getName(){
        return name;
    }
    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof User)) return false;
        User user=(User) o;
        return userId.equals(user.userId);
    }
    @Override
    public int hashCode(){
        return Objects.hash(userId);
    }
}

class Expense{
    private String id;
    private String description;
    private double amount;
    private User payer; 
    private List<User> participants; //those who participated in the expense
    private Map<User, Double> shares; //share of each participant in the expense
    public Expense(String id , String description, double amount, User payer, List<User> participants, SplitStrategy splitStrategy,Map<User,Double> splitVaues){
        this.id=id;
        this.amount=amount;
        this.description=description;
        this.payer=payer;
        this.participants=participants;
        this.shares=splitStrategy.calculateShares(amount, participants,splitVaues);
    }
    public String getId(){
        return id;
    }
    public String getDescription(){
        return description;
    }
    public double getAmount(){
        return amount;
    }
    public User getPayer(){
        return payer;
    }
    public List<User> getParticipants(){
        return participants;
    }
    public Map<User, Double> getShares(){
        return shares;
    }
}

class Transaction{
    private static int counter=1;

    private String transactionId; 
    private User from ;
    private User to ;
    private double amount;
    public Transaction(User from , User to, double amount){
        this.transactionId=String.valueOf(counter++);
        this.from=from;
        this.to=to;
        this.amount=amount;
    }
    public String getTransactionId(){
        return transactionId;
    }
    public User getFrom(){
        return from;
    }
    public double getAmount(){
        return amount;
    }
    public User getTo(){
        return to;
    }
}

class Group{
    private static int counter=1;

    private String groupId;
    private String groupName; 
    private List<Expense> expenses;
    private List<User> users;
    private ExpenseManager localExpenseManager;
    private ExpenseManager globalExpenseManager;
    public Group(String groupName,List<User> grpParticipants,ExpenseManager globalExpenseManager){
        this.groupId=String.valueOf(counter++);
        this.groupName=groupName;
        this.expenses=new ArrayList<>();
        this.users=new ArrayList<>(grpParticipants);
        this.localExpenseManager=new ExpenseManager();
        this.globalExpenseManager=globalExpenseManager;
    }
    public void addUser(User user){
        users.add(user);
    }
    public void addExpense(Expense expense){
        expenses.add(expense);
        localExpenseManager.addExpense(expense);
        globalExpenseManager.addExpense(expense);

    }
    public void showGroupBalances(){
        localExpenseManager.showGroupBalances(users);
    }
}

interface SplitStrategy{
    Map<User, Double> calculateShares(double amount, List<User> participants,Map<User, Double> splitValues);
}

class EqualSplit implements SplitStrategy{
    @Override
    public Map<User, Double> calculateShares(double amount, List<User> participants,Map<User, Double> splitValues){
        double splitShare=amount/participants.size();
        Map<User, Double> shares=new HashMap<>();
        for(User participant: participants){
            shares.putIfAbsent(participant, splitShare);
        }
        return shares;
    }
}

class PercentageSplit implements SplitStrategy{
    @Override
    public Map<User, Double> calculateShares(double amount, List<User> participants, Map<User, Double> percentageSplitDetails){
        double totalPercentage = percentageSplitDetails.values()
        .stream()
        .mapToDouble(Double::doubleValue)
        .sum();
        if (Math.abs(totalPercentage - 100.0) > 0.001) {
            throw new IllegalArgumentException("Invalid split: total percentage must be 100");
        }
        Map<User, Double> shares = new HashMap<>();
        for(User participant: participants){
            double percentage=percentageSplitDetails.get(participant);
            shares.put(participant, amount*percentage/100.0);
        }
        return shares;
    }
}

class ExactSplit implements SplitStrategy{
    @Override
    public Map<User, Double> calculateShares(double amount, List<User> participants, Map<User, Double> exactShares){
        double total=0;
        for(double share: exactShares.values()){
            total+=share;
        }
        if(Math.abs(total - amount) > 0.001){
            throw new IllegalArgumentException("Invalid exact split");
        }
        return exactShares;
    }
}

class ExpenseManager{
    private List<Expense> expenses;
    private Map<User, Map<User, Double>> balances;
    private List<Transaction> transactions;
    public ExpenseManager(){
        this.expenses=new ArrayList<>();
        this.balances=new HashMap<>();
        this.transactions=new ArrayList<>();
    }

    public void addExpense(Expense expense){
        String expenseId=expense.getId();
        //check if expense id is already present , ideally this won't come in here but just for safety
        for(Expense expense1: expenses){
            if(expense1.getId().equals(expenseId)){
                updateExpense(expense);
                return;
            }
        }
        expenses.add(expense);
        User payer=expense.getPayer();
        Map<User, Double> shares=expense.getShares();
        List<User> participants=expense.getParticipants();
        for(User participant: participants){
            if(!participant.equals(payer)){
                double share=shares.get(participant);
                balances.putIfAbsent(participant, new HashMap<>());
                //balances.get(participant).put(payer, balances.get(participant).getOrDefault(payer, 0.0)+share);
                updateBalances(participant, payer, share);
            }
        }
    }
    public void reverseExpense(Expense expense){
        User payer=expense.getPayer();
        Map<User, Double> shares=expense.getShares();
        for(User participant: expense.getParticipants()){
            if(!participant.equals(payer)){
                double share=shares.get(participant);
                //reversing the balance
                updateBalances(payer, participant, share);
            }
        }
    }
    public void deleteExpense(String expenseId){
        Expense target=null; 
        for(Expense expense: expenses){
            if(expense.getId().equals(expenseId)){
                target=expense;
                break;
            }
        }
        if(target==null) throw new IllegalArgumentException("Expense not found");
        reverseExpense(target);
        expenses.remove(target);
    }
    public void updateExpense(Expense newExpense){
        deleteExpense(newExpense.getId());
        addExpense(newExpense);
    }
    private double getBalance(User debtor, User creditor){
        return balances
            .getOrDefault(debtor, new HashMap<>())
            .getOrDefault(creditor, 0.0);
    }
    private void setBalance(User debtor, User creditor, double amount){
        balances.computeIfAbsent(debtor, k->new HashMap<>()).put(creditor, amount);
    }
    private void removeBalance(User debtor, User creditor){
        if(balances.containsKey(debtor)){
            balances.get(debtor).remove(creditor);
        }
    }
    private void updateBalances(User debtor, User creditor, double amount){
        double reverseBalance =getBalance(creditor, debtor);
        if(reverseBalance!=0.0){
            double curBalance=amount-reverseBalance;
            if(curBalance<0){
                setBalance(creditor, debtor, Math.abs(curBalance));
            }
            else if(Math.abs(curBalance)<0.001){
                removeBalance(creditor, debtor);
            }
            else{
                removeBalance(creditor, debtor);
                setBalance(debtor, creditor, curBalance);
            }
        }
        else{
            double curBalance=getBalance(debtor, creditor);
            if(curBalance!=0.0) amount+=curBalance;
            setBalance(debtor, creditor, amount);
        }
    }
    public Map<User, Double> getNetBalances(){
        Map<User, Double> net=new HashMap<>();
        for(Map.Entry<User,Map<User,Double>> outer: balances.entrySet()){
            User debtor=outer.getKey();
            for(Map.Entry<User,Double> inner: outer.getValue().entrySet()){
                User creditor=inner.getKey();
                double amount=inner.getValue();
                net.put(debtor, net.getOrDefault(debtor, 0.0)-amount);
                net.put(creditor, net.getOrDefault(creditor, 0.0)+amount);
            }
        }
        return net;
    }
    public List<Transaction> getSimplifiedSettlements(){
        Map<User,Double> netBalances=getNetBalances();
        List<User> debtors=new ArrayList<>();
        List<User> creditors=new ArrayList<>();
        for(Map.Entry<User,Double> entry: netBalances.entrySet()){
            User user=entry.getKey();
            double amount=entry.getValue();
            if(amount<-0.001) debtors.add(user);
            else if(amount>0.001) creditors.add(user);
        }

        List<Transaction> transactions=new ArrayList<>();

        int debtorIndex=0,creditorIndex=0;
        while(debtorIndex<debtors.size() && creditorIndex<creditors.size()){
            User debtor=debtors.get(debtorIndex);
            User creditor=creditors.get(creditorIndex);
            double debtorAmount=netBalances.get(debtor);
            double creditorAmount=netBalances.get(creditor);
            double transactionAmount=Math.min(Math.abs(debtorAmount),creditorAmount);

            transactions.add(new Transaction(debtor, creditor, transactionAmount));

            netBalances.put(debtor, debtorAmount+transactionAmount);
            netBalances.put(creditor, creditorAmount-transactionAmount);

            if(Math.abs(netBalances.get(debtor))<0.001) debtorIndex++;
            if(Math.abs(netBalances.get(creditor))<0.001) creditorIndex++;
        }
        return transactions;
    }

    public void showSimplifiedSettlements(){
        List<Transaction> transactions=getSimplifiedSettlements();
        System.out.println("Showing simplified settlements: ");
        for(Transaction transaction: transactions){
            System.out.println(
                transaction.getFrom().getName()
                + " pays "
                + transaction.getAmount()
                + "rs to "
                + transaction.getTo().getName()
            );
        }
    }
    public int minTransactions(int currentIndex, List<Double> creditList, int n){
        while(currentIndex<n && Math.abs(creditList.get(currentIndex))<0.001){
            currentIndex++;
        }
        if(currentIndex==n) return 0;
        int minTxns=Integer.MAX_VALUE;
        for(int nextIndex=currentIndex+1;nextIndex<n;nextIndex++){
            if(creditList.get(currentIndex)*creditList.get(nextIndex)< -0.001){
                creditList.set(nextIndex, creditList.get(nextIndex)+creditList.get(currentIndex));
                minTxns=Math.min(minTxns, 1+minTransactions(currentIndex+1, creditList, n));
                creditList.set(nextIndex, creditList.get(nextIndex)-creditList.get(currentIndex));
            }
        }
        return minTxns;
    }
    public int getMinimumSettlements(){
        Map<User,Double> netBalances=getNetBalances();
        List<Double> crediList=new ArrayList<>();

        for(Map.Entry<User, Double> entry: netBalances.entrySet()){
            if(Math.abs(entry.getValue())>0.001) crediList.add(entry.getValue());
        }
        return minTransactions(0,crediList,crediList.size());
    }

    public void settlePayment(User from , User to , double amount){
        double owed=getBalance(from, to);
        if(Math.abs(owed)<0.001) {
            throw new IllegalArgumentException("No pending balance");
        }
        if(amount>owed){
            throw new IllegalArgumentException("Overpayment not allowed");
        }
        double remainingBalance=owed-amount;
        Transaction transaction=new Transaction(from, to, amount);
        if(Math.abs(remainingBalance)<0.001) removeBalance(from, to);
        else setBalance(from, to, remainingBalance);
        transactions.add(transaction);
    }
    public void showGroupBalances(List<User> users){
        for (User from : balances.keySet()) {
            if (!users.contains(from)) continue;
        
            Map<User, Double> innerMap = balances.get(from);
        
            for (User to : innerMap.keySet()) {
                if (!users.contains(to)) continue;
                System.out.println("User: "+from.getUserId()+" ("+from.getName()+") "+"owes "+innerMap.get(to)+" amount to user: "+to.getUserId()+" ("+to.getName()+")");
            }
        }
    }
    public void showBalances(){
        boolean noEntries = balances.values().stream().allMatch(Map::isEmpty);
        if (noEntries) {
            System.out.println("No Balances left: All payments are settled!!");
        }
        for(Map.Entry<User, Map<User, Double>> outerEntry: balances.entrySet()){
            User user1=outerEntry.getKey();
            Map<User, Double> innerMap=outerEntry.getValue();
            for(Map.Entry<User, Double> innerEntry: innerMap.entrySet()){
                User user2=innerEntry.getKey();
                double amountOwed=innerEntry.getValue();
                System.out.println("User: "+user1.getUserId()+" ("+user1.getName()+") "+"owes "+amountOwed+" amount to user: "+user2.getUserId()+" ("+user2.getName()+")");
            }
        }
    }
}


public class SplitWiseBasic{
    public static void main(String[] args) {
        User user1=new User("1", "dk", "dk@gmail.com");
        User user2=new User("2", "md", "md@gmail.com");
        User user3=new User("3", "js", "js@gmail.com");      
        ExpenseManager globalExpenseManager=new ExpenseManager();
        List<User> grp1Participants=List.of(user1,user2,user3);
        Group tripGroup=new Group("Goa trip", grp1Participants,globalExpenseManager);
        SplitStrategy equSplitStrategy=new EqualSplit();
        SplitStrategy percentageSplitStrategy=new PercentageSplit();
        Map<User, Double> percentages = new HashMap<>();
        percentages.put(user1, 50.0);
        percentages.put(user2, 25.0);
        percentages.put(user3, 25.0);
        Expense expense1=new Expense("1", "user1 paid 600rs", 600.0, user1, grp1Participants, equSplitStrategy,new HashMap<>());
        Expense expense2=new Expense("2", "user2 paid 600rs", 600.0, user2, grp1Participants, equSplitStrategy,new HashMap<>());
        Expense expense3=new Expense("3", "user1 paid 900rs", 900.0, user1, grp1Participants, equSplitStrategy,new HashMap<>());
        Expense expense4=new Expense("4", "User3 paid 600rs", 600.0, user3, grp1Participants, percentageSplitStrategy, percentages);
        Expense expense5=new Expense("5", "user3 paid 600rs", 600.0, user3, grp1Participants, equSplitStrategy,new HashMap<>());

        tripGroup.addExpense(expense1);
        tripGroup.addExpense(expense2);
        tripGroup.addExpense(expense3);
        tripGroup.addExpense(expense4);
        tripGroup.addExpense(expense5);
        globalExpenseManager.showBalances();

        globalExpenseManager.settlePayment(user2, user3, 150.0);
        globalExpenseManager.settlePayment(user2, user1, 200.0);
        System.out.println("Balances after 2 transactions: ");
        globalExpenseManager.showBalances();

        System.out.println("After creating user4");
        User user4= new User("4", "nh", "nh@gmail.com");
        Expense expense6=new Expense("6", "user4 paid 300rs", 300, user4, List.of(user2,user3,user4), equSplitStrategy, new HashMap<>());
        globalExpenseManager.addExpense(expense6);
        globalExpenseManager.showBalances();

        System.out.println("Showing tripGroup balances only: ");
        tripGroup.showGroupBalances();

        globalExpenseManager.showSimplifiedSettlements();
        System.out.println("Minimum transactions needed to settle all payments: "+globalExpenseManager.getMinimumSettlements());
    }
}