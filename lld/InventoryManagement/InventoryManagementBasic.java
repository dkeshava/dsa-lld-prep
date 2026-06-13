import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum ProductCategory{
    ELECTRONICS,
    CLOTHING,
    GROCERY,
    FURNITURE,
    OTHER
}

abstract class Product{
    private String sku;
    private String name;
    private double price;
    private int threshold;
    private ProductCategory category;
    protected Product(String sku, String name, double price, int threshold, ProductCategory category){
        this.sku=sku;
        this.name=name;
        this.price=price;
        this.threshold=threshold;
        this.category=category;
    }

    public String getSku(){return sku;}
    public String getName(){return name;}
    public double getPrice(){return price;}
    public int getThreshold(){return threshold;}
    public ProductCategory getCategory(){return category;}
    public void setCategory(ProductCategory category){this.category=category;}
    public void setSku(String sku){this.sku=sku;}
    public void setPrice(double price){this.price=price;}
    public void setThreshold(int threshold){this.threshold=threshold;}
    public void setName(String name){this.name=name;}
}

class ElectronicsProduct extends Product{
    private String brand;
    private int warrantyPeriod;
    public ElectronicsProduct(String sku, String name, double  price , int threshold, String brand, int warrantyPeriod){
        super(sku, name, price, threshold, ProductCategory.ELECTRONICS);
        this.brand=brand;
        this.warrantyPeriod=warrantyPeriod;
    }
    public String getBrand(){return brand;}
    public int getWarrantyPeriod(){return warrantyPeriod;}
}

class GroceryProduct extends Product{
    private Date expiryDate;
    private boolean refrigerated;
    public GroceryProduct(String sku, String name, double  price ,int threshold,Date expiryDate, boolean refrigerated){
        super(sku, name, price, threshold, ProductCategory.GROCERY);
        this.expiryDate=expiryDate;
        this.refrigerated=refrigerated;
    }
    public Date getExpiryDate(){return expiryDate;}
    public void setExpiryDate(Date expiryDate){this.expiryDate=expiryDate;}
    public boolean isRefrigerated(){return refrigerated;}
}

class ClothingProduct extends Product{
    private String size;
    private String color;
    public ClothingProduct(String sku, String name, double  price , int threshold,String size, String color){
        super(sku, name, price, threshold, ProductCategory.CLOTHING);
        this.size=size;
        this.color=color;
    }
    public String getSize(){return size;}
    public String getColor(){return color;}
}
class InventoryItem{
    private Product product;
    private int quantity;
    public InventoryItem(Product product,int quantity){
        this.product=product;
        this.quantity=quantity;
    }
    public int getQuantity(){return quantity;}
    public Product getProduct(){return product;}
    public void reduceStock(int qty){
        if(this.quantity<qty){
            throw new IllegalArgumentException("error: trying to remove more than what we have!");
        }
        quantity-=qty;
    }
    public void addStock(int qty){
        quantity+=qty;
    }
}
class Warehouse{
    int id;
    String name;
    String locatiion;
    Map<String , InventoryItem> inventory;
    public Warehouse(int id,String name, String location){
        this.id=id;
        this.name=name;
        this.locatiion=location;
        inventory = new HashMap<>();
    }
    public void initializeWarehouse(List<Product> productsList){
        for(Product product: productsList){
            inventory.put(product.getSku(), new InventoryItem(product, 20));
        }
    }

    public void addProduct(Product product, int quantity){
        InventoryItem item = inventory.get(product.getSku());
        if(item==null){
            inventory.put(product.getSku(), new InventoryItem(product, quantity));
        }
        else item.addStock(quantity);
    }
    public void removeProduct(String sku, int quantity){
        InventoryItem item=inventory.get(sku);
        if(item==null) throw new IllegalArgumentException("no such item exists");
        if(item.getQuantity()<quantity){
            throw new IllegalArgumentException("Don't have enough stock to remove");
        }
        item.reduceStock(quantity);
        if(item.getQuantity() == 0){
            inventory.remove(sku);
        }
    }
    public int getAvailableQuantity(String sku){
        return inventory.get(sku).getQuantity();
    }
    public Map<String,InventoryItem> getInventory(){return inventory;}
}

interface ReplenishmentStrategy{
    void replenish(Product product);
}
class JustInTimeStrategy implements ReplenishmentStrategy{
    @Override
    public void replenish(Product product){
        System.out.println("Applying Just-In-Time replenishment for " + product.getName());
    }
}
class BulkOrderStrategy implements ReplenishmentStrategy{
    @Override
    public void replenish(Product product){
        System.out.println("Applying Bulk Order replenishment for " + product.getName());
    }
}
class InventoryManager{
    private List<Warehouse> warehouses;
    private static InventoryManager instance;
    private ReplenishmentStrategy replenishmentStrategy;
    private InventoryManager(){
        warehouses=new ArrayList<>();
    }
    public static synchronized InventoryManager getInstance(){
        if(instance==null){
            instance=new InventoryManager();
        }
        return instance;
    }
    public List<Product> getLowStockProducts(){
        List<Product> lowStockProducts=new ArrayList<>();
        for(Warehouse warehouse: warehouses){
            for(InventoryItem item: warehouse.getInventory().values()){
                if(item.getQuantity()<=item.getProduct().getThreshold()){
                    lowStockProducts.add(item.getProduct());
                }
            }
        }
        return lowStockProducts;
    }
    public void checkAndReplenish(Warehouse warehouse,String sku){
        InventoryItem item = warehouse.getInventory().get(sku);
        if(item == null){
            throw new IllegalArgumentException(
                "No such product in warehouse"
            );
        }
        if(item.getQuantity()<=item.getProduct().getThreshold()){
            replenishmentStrategy.replenish(item.getProduct());
        }
    }
    public void transferStock(Warehouse source, Warehouse destination, String sku, int quantity){
        InventoryItem sourceItem=source.getInventory().get(sku);
        if(sourceItem == null){
            throw new IllegalArgumentException(
                "No such product in source warehouse"
            );
        }
        Product sourceProduct=sourceItem.getProduct();
        if(sourceItem.getQuantity()<quantity) throw new IllegalArgumentException("Don't have enough stock to transfer");
        sourceItem.reduceStock(quantity);
        if(sourceItem.getQuantity() == 0){
            source.getInventory().remove(sku);
        }
        InventoryItem destinationItem=destination.getInventory().get(sku);
        if(destinationItem!=null){
            destinationItem.addStock(quantity);
        }
        else{
            destination.getInventory().put(sku, new InventoryItem(sourceProduct, quantity));
        }
    }
    public void setReplenishmentStrategy(ReplenishmentStrategy strategy) {
        this.replenishmentStrategy = strategy;
    }
    public void addWarehouse(Warehouse warehouse) {
        warehouses.add(warehouse);
    }

    public void removeWarehouse(Warehouse warehouse) {
        warehouses.remove(warehouse);
    }

}

public class InventoryManagementBasic {
    public static void main(String[] args) {
        ReplenishmentStrategy replenishmentStrategy=new JustInTimeStrategy();
        Product laptop =
            new ElectronicsProduct(
                "E101",
                "MacBook",
                120000,
                5,
                "Apple",
                24
            );
        Product desktop =
            new ElectronicsProduct(
                "E102",
                "lenovoAllInOne",
                80000,
                5,
                "Lenovo",
                24
            );

        Product milk =
            new GroceryProduct(
                "G101",
                "Milk",
                60,
                5,
                new Date(0),
                true
            );   
        List<Product> productsList=List.of(laptop,desktop,milk);
        Warehouse warehouse1=new Warehouse(1, "w1", "andheri");
        warehouse1.initializeWarehouse(productsList);
        Warehouse warehouse2=new Warehouse(2, "w2", "santacruz");
        warehouse2.initializeWarehouse(productsList);


        System.out.println(warehouse1.getAvailableQuantity("E101"));

        InventoryManager inventoryManager=InventoryManager.getInstance();
        inventoryManager.addWarehouse(warehouse1);
        inventoryManager.addWarehouse(warehouse2);
        inventoryManager.setReplenishmentStrategy(replenishmentStrategy);

        inventoryManager.transferStock(warehouse1, warehouse2, "E101", 5);
        System.out.println("Available quantity of E101 in warehouse1 after transferring : "+warehouse1.getAvailableQuantity("E101"));
        //inventoryManager.transferStock(warehouse1, warehouse2, "E101", 20);
        warehouse1.removeProduct("E101", 11);

        inventoryManager.checkAndReplenish(
            warehouse1,
            "E101"
        );
    }
}
