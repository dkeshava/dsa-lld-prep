import java.util.ArrayList;
import java.util.List;

enum ItemType{
    COKE,
    PEPSI,
    JUICE,
    SODA
}
enum Coin{
    ONE_RUPEE(1),
    TWO_RUPEES(2),
    FIVE_RUPEES(5),
    TEN_RUPEES(10);

    public int value;
    Coin(int value){
        this.value=value;
    } 
}

class Item{
    private ItemType type;
    private int price;
    public Item(ItemType type, int price){
        this.type=type;
        this.price=price;
    }
    public ItemType getType(){return type;}
    public int getPrice(){return price;}
}

class ItemShelf{
    private List<Item> items;
    private int code;
    private boolean isSoldOut;
    public ItemShelf(int code){
        this.code=code;
        this.items=new ArrayList<>();
        this.isSoldOut=false;
    }
    public int getCode(){return code;}
    public List<Item> getItems(){return items;}
    public void setItems(List<Item> items){
        this.items=items;
        setSoldOut(items.isEmpty());
    }
    public boolean checkIsSoldOut(){
        return isSoldOut;
    }
    public void setSoldOut(boolean isSoldOut){
        this.isSoldOut=isSoldOut;
    }
    public void addItem(Item item){items.add(item);
        if(checkIsSoldOut()) setSoldOut(false);
    }
    public void removeItem(Item item){items.remove(item);
        if(items.isEmpty()) setSoldOut(true);
    }
}

class Inventory{
    private ItemShelf[] inventory=null;
    public Inventory(int itemShelfCount){
        inventory=new ItemShelf[itemShelfCount];
        initializeEmptyInventory();
    }
    public ItemShelf[] getInventory(){
        return inventory;
    }
    public void setInventory(ItemShelf[] inventory){
        this.inventory=inventory;
    }
    private void initializeEmptyInventory(){
        int startCode=101;
        for(int i=0;i<inventory.length;i++){
            ItemShelf shelf=new ItemShelf(startCode);
            inventory[i]=shelf;
            startCode++;
        }
    }
    public void addItem(Item item, int code) throws Exception{
        for(ItemShelf itemShelf: inventory){
            if(itemShelf.getCode()==code){
                itemShelf.addItem(item);
                return;
            }
        }
        throw new Exception("Invalid code");
    }
    public Item getItem(int codeNumber) throws Exception{
        for(ItemShelf shelf: inventory){
            if(shelf.getCode()==codeNumber){
                if(shelf.checkIsSoldOut()){
                    throw new Exception("Item already sold out");
                }
                else{
                    Item item=shelf.getItems().get(0);
                    //shelf.removeItem(item);
                    return item;
                }
            }
        }
        throw new Exception("Invalid code");
    }
    public void removeItem(int codeNumber) throws Exception{
        for(ItemShelf shelf: inventory){
            if(shelf.getCode()==codeNumber){
                if(shelf.checkIsSoldOut()){
                    throw new Exception("Item already sold out");
                }
                else{
                    Item item=shelf.getItems().get(0);
                    shelf.removeItem(item);
                    return;
                    //return item;
                }
            }
        }
        throw new Exception("Invalid code");
    }
    // public void updateSoldItem(int codeNumber){
    //     for(ItemShelf shelf : inventory){
    //         if(shelf.getCode()==codeNumber){
    //             if(shelf.getItems().isEmpty()) shelf.setSoldOut(true);
    //         }
    //     }
    // }
    public ItemShelf getItemShelf(int shelfNumber){return inventory[shelfNumber];}
    public boolean isInventoryOutOfStock(){
        for(ItemShelf shelf : inventory){
            if(!shelf.getItems().isEmpty()) return false;
        }
        return true;
    }
}
interface VendingMachineState{
    void printPrompt();
    String getStateName();
    VendingMachineState next(VendingMachineContext context);
}
class IdleState implements VendingMachineState{
    @Override
    public void printPrompt(){
        System.out.println("Vending machine is idle. Please insert coins.");
    }
    @Override
    public String getStateName(){return "IdleState";}
    @Override
    public VendingMachineState next(VendingMachineContext context){
        if(context.getInventory().isInventoryOutOfStock())return new OutOfStockState();
        else if(context.getCoins().isEmpty()) return this;
        else return new HasCoinsState();
    }
}
class HasCoinsState implements VendingMachineState{
    @Override
    public void printPrompt(){
        System.out.println("Vending machine has coins.");
    }
    @Override
    public String getStateName(){return "HasCoinsState";}
    @Override
    public VendingMachineState next(VendingMachineContext context){
        if(context.getInventory().isInventoryOutOfStock())return new OutOfStockState();
        else if(context.getCoins().isEmpty()) return new IdleState();
        return this;
    }
}
class SelectionState implements VendingMachineState{
    @Override
    public void printPrompt(){
        System.out.println("Item selected.");
    }
    @Override
    public String getStateName(){return "SelectionState";}
    @Override
    public VendingMachineState next(VendingMachineContext context){
        if(context.getInventory().isInventoryOutOfStock())return new OutOfStockState();
        else if(context.getCoins().isEmpty()) return new IdleState();
        if(context.getPurchaseApproved()) return new DispenseState();
        else return this;
    }
}
class DispenseState implements VendingMachineState{
    @Override
    public void printPrompt(){
        //System.out.println("Dispensing Item");
    }
    @Override
    public String getStateName(){return "DispenseState";}
    @Override
    public VendingMachineState next(VendingMachineContext context){
        if(context.getInventory().isInventoryOutOfStock())return new OutOfStockState();
        else if (context.getChangeAmount()>0) return new ReturnChangeState();
        else return new IdleState();
    }
}
class ReturnChangeState implements VendingMachineState{
    @Override
    public void printPrompt(){
        //System.out.println("Returning Change");
    }
    @Override
    public String getStateName(){return "ReturnChangeState";}
    @Override
    public VendingMachineState next(VendingMachineContext context){
        if(context.getInventory().isInventoryOutOfStock())return new OutOfStockState();
        return new IdleState();
    }
}
class OutOfStockState implements VendingMachineState{
    @Override
    public void printPrompt(){
        System.out.println("OutOfStock");
    }
    @Override
    public String getStateName(){return "OutOfStockState";}
    @Override
    public VendingMachineState next(VendingMachineContext context){
        if(context.getInventory().isInventoryOutOfStock()){
            return this;
        }
        else{
            return new IdleState();
        }
    }
}
class VendingMachineContext{
    private VendingMachineState currentState;
    private Inventory inventory;
    private List<Coin> coinList;
    private int selectedItemCode=-1;
    private boolean purchaseApproved;
    private int changeAmount;
    public VendingMachineContext(int itemShelfCount){
        this.currentState=new IdleState();
        this.inventory=new Inventory(itemShelfCount);
        this.coinList=new ArrayList<>();
        this.purchaseApproved=false;
    }
    public VendingMachineState getCurrentState(){return currentState;}
    public Inventory getInventory(){return inventory;}
    public List<Coin> getCoins(){return coinList;}
    public void advanceState(){
        VendingMachineState nextState=currentState.next(this);
        currentState=nextState;
        System.out.println("current state: "+currentState.getStateName());
        currentState.printPrompt();
    }
    public void insertCoin(Coin coin){
        if(currentState instanceof IdleState || currentState instanceof HasCoinsState || currentState instanceof SelectionState){
            coinList.add(coin);
            advanceState();
        }
    }
    public void selectItem(int codeNumber){
        if(currentState instanceof HasCoinsState){
            selectedItemCode=codeNumber;
            currentState = new SelectionState();
            System.out.println(
                "current state: " + currentState.getStateName()
            );
            
            currentState.printPrompt();
        }
    }
    public Item dispenseItem(){
        if(currentState instanceof SelectionState){
            try {
                //if valid amount inserted, next state
                Item item=inventory.getItem(selectedItemCode);
                purchaseApproved= (item!=null) && (getTotalInsertedAmount()>=item.getPrice());
                advanceState();
                if(currentState instanceof DispenseState){
                    System.out.println("Dispensed item: " + item.getType());
                    inventory.removeItem(selectedItemCode);
                    changeAmount=getTotalInsertedAmount()-item.getPrice();
                    advanceState();
                    if(currentState instanceof ReturnChangeState){
                        returnChange();
                        advanceState();
                    }
                    resetMachine();
                    return item;
                }
                else{
                    System.out.println("Not enough Balance to dispense selected item!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        else System.out.println("Products can only be dispensed in dispensed state");
        return null;
    }
    private void resetMachine(){
        coinList.clear();
        selectedItemCode=-1;
        changeAmount=0;
        purchaseApproved=false;
    }
    private void returnChange(){
        System.out.println(
            "Returned change: Rs " + changeAmount
        );
    }
    public int getTotalInsertedAmount(){
        int total=0;
        for(Coin coin: coinList){
            total+=coin.value;
        }
        return total;
    }
    public int getSelectedItemCode(){return selectedItemCode;}
    public boolean getPurchaseApproved(){return purchaseApproved;}
    public int getChangeAmount(){return changeAmount;}
}

public class VendingMachine {
    public static void main(String[] args) {
        VendingMachineContext vendingMachine=new VendingMachineContext(10);
        List<Item> itemList1=new ArrayList<>(List.of(new Item(ItemType.COKE, 10),new Item(ItemType.COKE, 10),new Item(ItemType.COKE, 10))) ;
        List<Item> itemList2=new ArrayList<>(List.of(new Item(ItemType.JUICE, 15),new Item(ItemType.JUICE, 15),new Item(ItemType.JUICE, 15))) ;
        vendingMachine.getInventory().getItemShelf(0).setItems(itemList1);
        vendingMachine.getInventory().getItemShelf(1).setItems(itemList2);

        vendingMachine.insertCoin(Coin.FIVE_RUPEES);
        //vendingMachine.insertCoin(Coin.FIVE_RUPEES);
        //vendingMachine.insertCoin(Coin.TEN_RUPEES);
        vendingMachine.selectItem(101);
        vendingMachine.dispenseItem();
    }
}
