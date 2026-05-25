
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//parking lot flow:
// park vehicle: 
//     find a free slot
//     assign vehicle
//     create ticket

// unpark vehicle:
//     take ticket
//     find & free slot and invalidate ticket
enum VehicleType{
    BIKE, CAR, TRUCK
}
class Vehicle{
    String LicensePlate;
    VehicleType type;
    public Vehicle(String license, VehicleType type){
        this.LicensePlate=license;
        this.type=type;
    }
}
class ParkingSlot{
    int id;
    VehicleType type;
    boolean isOcuupied;
    Vehicle vehicle;
    int floorNumber;
    PricingStrategy pricingStrategy;
    public ParkingSlot(int id, VehicleType type,PricingStrategy pricingStrategy,int floorNumber){
        this.id=id;
        this.type=type;
        this.isOcuupied=false;
        this.pricingStrategy=pricingStrategy;
        this.floorNumber=floorNumber;
    }
}
class Floor{
    int floorNumber;
    List<ParkingSlot> slots;
    public Floor(int floorNumber,List<ParkingSlot> slots){
        this.floorNumber=floorNumber;
        this.slots=slots;
    }
}
class Ticket{
    int ticketId;
    ParkingSlot parkingSlot;
    long entryTime;
    boolean isValid;
    public Ticket(int ticketId,ParkingSlot parkingSlot, long entryTime){
        this.ticketId=ticketId;
        this.parkingSlot=parkingSlot;
        this.entryTime=entryTime;
        this.isValid=true;
    }
}
interface PricingStrategy{
    double calculateFee(long duration, VehicleType type); 
}
class NormalPricingStrategy implements PricingStrategy{
    public double calculateFee(long duration, VehicleType type){
        double fee;
        switch (type) {
            case BIKE:
                fee=duration*10;
                break;
            case CAR:
                fee=duration*20;
                break;
            case TRUCK:
                fee=duration*30;
                break;
            default:
                fee=0;
        }
        return fee;
    }
}
class PremiumPricingStrategy implements PricingStrategy{
    public double calculateFee(long duration, VehicleType type){
        double fee;
        switch (type) {
            case BIKE:
                fee=duration*5*10;
                break;
            case CAR:
                fee=duration*5*20;
                break;
            case TRUCK:
                fee=duration*5*30;
                break;
            default:
                fee=0;
        }
        return fee;
    }
}
interface PaymentStrategy{
    boolean pay(double amount);
}
class CashPayment implements PaymentStrategy{
    public boolean pay(double amount){
        System.out.println("Paying "+amount+" amount of cash");
        return true;
    }
}
class CreditCardPayment implements PaymentStrategy{
    public boolean pay(double amount){
        System.out.println("Paying "+amount+" amount through credit card");
        return true;
    }
}
interface SlotAllocationStrategy{
    ParkingSlot getParkingSlot(List<Floor> floors,VehicleType type);
}
class FirstAvailableStrategy implements SlotAllocationStrategy{
    public ParkingSlot getParkingSlot(List<Floor> floors,VehicleType type){
        for(Floor floor:floors){
            for(ParkingSlot parkingslot: floor.slots){
                if(!parkingslot.isOcuupied && parkingslot.type==type){
                    return parkingslot;
                }
            }
        }
        return null;
    }
}
class ParkingLot{
    List<Floor> floors;
    int currentTicketId;
    SlotAllocationStrategy slotStrategy;
    public ParkingLot(List<Floor> floors,int currentTicketId, SlotAllocationStrategy slotStrategy){
        this.floors=floors;
        this.currentTicketId=currentTicketId;
        this.slotStrategy=slotStrategy;
    }
    public Ticket parkVehicle(Vehicle vehicle){
        VehicleType vechicleToPark=vehicle.type;
        //find a free slot for that vehicle
        try {
            // for(ParkingSlot parkingslot: parkingslots){
            //     if(parkingslot.isOcuupied) continue;
            //     if(parkingslot.type==vechicleToPark){
                ParkingSlot parkingslot=slotStrategy.getParkingSlot(floors, vechicleToPark);
                if(parkingslot==null){
                    System.out.println("No parking slot available");
                    return null;
                }
                    currentTicketId+=1;
                    Ticket ticket= new Ticket(currentTicketId,parkingslot,System.currentTimeMillis());
                    System.out.println("Parking "+vehicle.LicensePlate+" at Floor: "+parkingslot.floorNumber+" & parking slot: "+parkingslot.id);
                    parkingslot.isOcuupied=true;
                    parkingslot.vehicle=vehicle;
                    return ticket;
            //     }
            // }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    public void unparkVehicle(Ticket ticket,PaymentStrategy paymentStrategy){
        try {
            if(ticket!=null && ticket.isValid){
                ParkingSlot parkingSlot=ticket.parkingSlot;
                //for(ParkingSlot parkingSlot: parkingslots){
                  //  if(tovacate!=parkingSlot) continue;
                    //calculate the time of parking
                    long timeParked=System.currentTimeMillis()-ticket.entryTime;
                    timeParked=TimeUnit.MILLISECONDS.toHours(timeParked)+1;
                    //calculate fee
                    double fee=parkingSlot.pricingStrategy.calculateFee(timeParked, parkingSlot.type);
                    //then collect payment
                    boolean ispaid=paymentStrategy.pay(fee);
                    if(!ispaid) {
                        System.out.println("Payment failed");
                        return;
                    }
                    //then vacate parkingslot
                    parkingSlot.isOcuupied=false;
                    parkingSlot.vehicle=null;
                    ticket.isValid=false;
                    System.out.println("Unparked successfully by paying ₹"+fee+"/-");
                //}
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
class ParkingSlotFactory{
    public static List<ParkingSlot> createSlots(int floorNumber,PricingStrategy normalPricingStrategy, PricingStrategy premiumPricingStrategy){
        return List.of(
            new ParkingSlot(1, VehicleType.CAR, normalPricingStrategy, floorNumber),
            new ParkingSlot(2, VehicleType.CAR, premiumPricingStrategy, floorNumber),
            new ParkingSlot(3, VehicleType.BIKE, normalPricingStrategy, floorNumber),
            new ParkingSlot(4, VehicleType.BIKE, normalPricingStrategy, floorNumber),
            new ParkingSlot(5, VehicleType.TRUCK, premiumPricingStrategy, floorNumber)
        );
    }
}
public class parkingLot_basic{
    public static void main(String[] args) {
        PricingStrategy normalPricingStrategy=new NormalPricingStrategy();
        PricingStrategy premiumPricingStrategy=new PremiumPricingStrategy();
        PaymentStrategy paymentStrategy=new CashPayment();
        List<Floor> floors=new ArrayList<>();
        Floor floor1=new Floor(1,ParkingSlotFactory.createSlots(1, normalPricingStrategy, premiumPricingStrategy));
        Floor floor2=new Floor(2,ParkingSlotFactory.createSlots(2, normalPricingStrategy, premiumPricingStrategy));
        Floor floor3=new Floor(3,ParkingSlotFactory.createSlots(3, normalPricingStrategy, premiumPricingStrategy));
        floors.add(floor1);
        floors.add(floor2);
        floors.add(floor3);
        //floors.addAll(List.of(floor1,floor2,floor3));


        //can also use vehicle factory to create vehicles
        Vehicle car1=new Vehicle("abc1",VehicleType.CAR);
        Vehicle car2=new Vehicle("abc2",VehicleType.CAR);
        Vehicle car3=new Vehicle("abc3",VehicleType.CAR);
        SlotAllocationStrategy slotStrategy=new FirstAvailableStrategy();

        ParkingLot parkingLot=new ParkingLot(floors, 0,slotStrategy);
        Ticket car3ticket=parkingLot.parkVehicle(car3);
        Ticket car2ticket=parkingLot.parkVehicle(car2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //parkingLot.unparkVehicle(car3ticket,paymentStrategy);
        Ticket car1ticket=parkingLot.parkVehicle(car1);        
    }
}


// import java.time.Duration;
// import java.util.List;

// abstract class Vehicle{
//     public String licensePlate;
//     public String vehicleType;
// }
// class CarVehicle extends Vehicle{

// }
// class BikeVehicle extends Vehicle{

// }
// enum DurationType{
//     HOURS,
//     DAYS
// }
// interface ParkingFeeStrategy{
//     void calculateFee(String vehicleType, DurationType duration);
// }
// class HourlyRateStrategy implements ParkingFeeStrategy{
//     public void calculateFee(String vehicleType, DurationType duration){
//         //switch case statement
//         //if car : if duration type =hours -> hours*15, if duration type =days -> days*24*15
//         // similarly for other vehicles
//     }
// }
// class PremiumRateStrategy implements ParkingFeeStrategy{
//     public void calculateFee(String vehicleType, DurationType duration){
//         //switch case statement
//         //if car : if duration type =hours -> hours*25, if duration type =days -> days*24*25
//         // similarly for other vehicles
//     }
// }
// class ParkingSlot{
//     String vehicleType;
//     boolean isOcuupied;

// }
// class ParkingLot{
//     List<ParkingSlot> parkingSlots;
//     //manages all slots; park, vacate, payment, ....
// }
// interface PaymentStrategy{
//     void processPayment(double amount);
// }
// class CashPayment implements PaymentStrategy{
//     public void processPayment(double amount){
//         System.out.println(amount+": paying with cash");
//     }
// }
// class CreditCardPayment implements PaymentStrategy{
//     public void processPayment(double amount){
//         System.out.println(amount+": paying with CC");
//     }
// }


// public class parkingLot_basic {
//     public static void main(String[] args) {
        
//     }
// }
