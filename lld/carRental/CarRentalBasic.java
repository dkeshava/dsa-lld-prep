import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum VehicleType{
    ECONOMY, 
    LUXURY,
    SUV, 
    BIKE,
    AUTO
}

enum VehicleStatus{
    AVAILABLE,
    RESERVED,
    RENTED,
    MAINTENANCE,
    OUT_OF_SERVICE
}

enum ReservationStatus{
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

abstract class Vehicle{
    private String licenseNumber;
    private String model;
    private VehicleType vehicleType;
    private VehicleStatus vehicleStatus;
    private double baseRentalPrice;
    public Vehicle(String licenseNumber, String model, VehicleType type, double baseRentalPrice){
        this.licenseNumber=licenseNumber;
        this.model=model;
        this.vehicleType=type;
        this.vehicleStatus=VehicleStatus.AVAILABLE;
        this.baseRentalPrice=baseRentalPrice;
    }
    public abstract double calculateRentalFee(int days);
    //getters and setters
    public double getBaseRentalPrice(){
        return baseRentalPrice;
    }
    public VehicleType getVehicleType(){return vehicleType;}
    public VehicleStatus getVehicleStatus(){return vehicleStatus;}
    public void setVehicleStatus(VehicleStatus status){this.vehicleStatus=status;}
    public String getLicenseNumber(){return licenseNumber;}
    public String getModel(){return model;}
}
class EconomyVehicle extends Vehicle{
    private static final double rateMultiplier=1.0;
    public EconomyVehicle(String licenseNumber, String model, VehicleType type, double baseRentalPrice){
        super(licenseNumber, model, type, baseRentalPrice);
    }
    @Override
    public double calculateRentalFee(int days){
        return getBaseRentalPrice()*rateMultiplier*days;
    }
}
class LuxuryVehicle extends Vehicle{
    private static final double rateMultiplier=2.5;
    private static final double premiumFee=50;
    public LuxuryVehicle(String licenseNumber, String model, VehicleType type, double baseRentalPrice){
        super(licenseNumber, model, type, baseRentalPrice);
    }
    @Override
    public double calculateRentalFee(int days){
        return getBaseRentalPrice()*rateMultiplier*days+premiumFee;
    }
}
class SuvVehicle extends Vehicle{
    private static final double rateMultiplier=1.5;
    public SuvVehicle(String licenseNumber, String model, VehicleType type, double baseRentalPrice){
        super(licenseNumber, model, type, baseRentalPrice);
    }
    @Override
    public double calculateRentalFee(int days){
        return getBaseRentalPrice()*rateMultiplier*days;
    }
}
class BikeVehicle extends Vehicle{
    private static final double rateMultiplier=0.5;
    public BikeVehicle(String licenseNumber, String model, VehicleType type, double baseRentalPrice){
        super(licenseNumber, model, type, baseRentalPrice);
    }
    @Override
    public double calculateRentalFee(int days){
        return getBaseRentalPrice()*rateMultiplier*days;
    }
}
class AutoVehicle extends Vehicle{
    private static final double rateMultiplier=0.75;
    public AutoVehicle(String licenseNumber, String model, VehicleType type, double baseRentalPrice){
        super(licenseNumber, model, type, baseRentalPrice);
    }
    @Override
    public double calculateRentalFee(int days){
        return getBaseRentalPrice()*rateMultiplier*days;
    }
}
class VehicleFactory{
    public Vehicle createVehicle(String licenseNumber, String model, VehicleType type, double baseRentalPrice){
        switch(type){
            case ECONOMY: return new EconomyVehicle(licenseNumber, model, type, baseRentalPrice);
            case LUXURY: return new LuxuryVehicle(licenseNumber, model, type, baseRentalPrice);
            case SUV: return new SuvVehicle(licenseNumber, model, type, baseRentalPrice);
            case BIKE: return new BikeVehicle(licenseNumber, model, type, baseRentalPrice);
            case AUTO: return new AutoVehicle(licenseNumber, model, type, baseRentalPrice);
            default: throw new IllegalArgumentException("Unsupported vehicle type:  "+ type);
        }
    }
}

class Location{
    private String address;
    private String city;
    private String state;
    private String pinCode;
    public Location(String address,String city, String state, String pinCode){
        this.address=address;
        this.city=city;
        this.state=state;
        this.pinCode=pinCode;
    }
}
class VehicleInventory{
    private List<Vehicle> vehicles;
    public VehicleInventory(){
        this.vehicles=new ArrayList<>();
    }
    public List<Vehicle> searchVehicle(VehicleType type){
        return vehicles.stream().filter(v->v.getVehicleType()==type).filter(v->v.getVehicleStatus()==VehicleStatus.AVAILABLE).toList();
    }
    public void addVehicle(Vehicle vehicle){
        vehicles.add(vehicle);
    }
    public void removeVehicle(Vehicle vehicle){
        vehicles.remove(vehicle);
    }
}
class RentalStore{
    private int id;
    private String name;
    private Location location;
    private VehicleInventory vehicleInventory;
    public RentalStore(int id, String name, Location location){
        this.id=id;
        this.name=name;
        this.location=location;
        this.vehicleInventory=new VehicleInventory();
    }
    public List<Vehicle> searchVehicles(VehicleType type){
        return vehicleInventory.searchVehicle(type);
    }
    public VehicleInventory getVehicleInventory(){return vehicleInventory;}
}
class Reservation{
    private int id;
    private Date startDate;
    private Date endDate;
    private User user;
    private Vehicle vehicle;
    private RentalStore pickupStore;
    private RentalStore returnStore;
    private ReservationStatus reservationStatus;
    private double totalAmount;
    public Reservation(int id, Date startDate, Date endDate,User user, Vehicle vehicle, RentalStore pickupStore, RentalStore returnStore){
        this.id=id;
        this.user=user;
        this.vehicle=vehicle;
        this.endDate=endDate;
        this.startDate=startDate;
        this.pickupStore=pickupStore;
        this.returnStore=returnStore;
        this.reservationStatus=ReservationStatus.PENDING;
        long diffInMillis=endDate.getTime()-startDate.getTime();
        int days=(int) (diffInMillis/(1000*60*60*24))+1;
        totalAmount=vehicle.calculateRentalFee(days);
    }
    public int getId(){return id;}
    public User getUser(){return user;}
    public Vehicle getVehicle(){return vehicle;}
    public ReservationStatus getReservationStatus(){return reservationStatus;}
    public void setReservationStatus(ReservationStatus status){this.reservationStatus=status;}
    public double getTotalAmount(){return totalAmount;}
    public RentalStore getPickupStore(){return pickupStore;}
    public RentalStore getReturnStore(){return returnStore;}
    public Date getStartDate(){ return startDate; }
    public Date getEndDate(){ return endDate; }
}
class User{
    private int id;
    private String name;
    private String email;
    private List<Reservation> reservations;
    public User(int id, String name, String email){
        this.id=id;
        this.name=name;
        this.email=email;
        this.reservations=new ArrayList<>();
    }
    public void addReservation(Reservation reservation){
        reservations.add(reservation);
    }
    public void deleteReservation(Reservation resevation){
        reservations.remove(resevation);
    }
    public int getId(){return id;}
}

class Bill{
    private Reservation reservation;
    private double amount;
    public Bill(Reservation reservation){
        this.reservation=reservation;
        this.amount=reservation.getTotalAmount();
    }
    public Reservation getReservation(){return reservation;}
    public double getAmount(){return amount;}
}
interface PaymentStrategy{
    void pay(double amount);
}
class CashPaymentStrategy implements PaymentStrategy{
    @Override 
    public void pay(double amount){
        System.out.println("Successfully paid "+amount+"rs through cash");
    }
}
class UpiPaymentStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount){
        System.out.println("Paid " + amount + " using UPI");
    }
}
class CardPaymentStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount){
        System.out.println("Paid " + amount + " using Card");
    }
}
// class Payment{

// }
class ReservationManager{
    private Map<Integer,Reservation> reservations;
    private int reservationCounter;
    public ReservationManager(){
        this.reservations=new HashMap<>();
        this.reservationCounter=1;
    }
    public Reservation createReservation(Date startDate, Date endDate,User user, Vehicle vehicle, RentalStore pickupStore, RentalStore returnStore){
        for(Reservation reservation : reservations.values()){
            if(reservation.getVehicle().equals(vehicle)
                && reservation.getReservationStatus() != ReservationStatus.CANCELLED
                && reservation.getReservationStatus() != ReservationStatus.COMPLETED
                && isOverlapping(
                    startDate,
                    endDate,
                    reservation.getStartDate(),
                    reservation.getEndDate()
                )){
                throw new IllegalStateException(
                    "Vehicle already booked for overlapping dates"
                );
            }
        }
        Reservation newReservation=new Reservation(reservationCounter++, startDate, endDate, user, vehicle, pickupStore, returnStore);
        reservations.put(newReservation.getId(), newReservation);
        vehicle.setVehicleStatus(VehicleStatus.RESERVED);
        user.addReservation(newReservation);
        newReservation.setReservationStatus(ReservationStatus.CONFIRMED);
        System.out.println("Reservation created");
        return newReservation;
    } 
    public void pickup(int reservationId){
        Reservation reservation=reservations.get(reservationId);
        if(reservation.getReservationStatus() != ReservationStatus.CONFIRMED)
            throw new IllegalStateException("Can't pickup the vehicle");
        reservation.setReservationStatus(ReservationStatus.IN_PROGRESS);
        reservation.getVehicle().setVehicleStatus(VehicleStatus.RENTED);
        System.out.println("Vehicle picked up");
    }
    public Bill returnVehicle(int reservationId){
        Reservation reservation=reservations.get(reservationId);
        if(reservation.getReservationStatus() != ReservationStatus.IN_PROGRESS)
            throw new IllegalStateException("No vehicle to return!");
        Bill bill=new Bill(reservation);
        RentalStore pickupStore=reservation.getPickupStore();
        RentalStore returnStore=reservation.getReturnStore();
        pickupStore.getVehicleInventory().removeVehicle(reservation.getVehicle());
        returnStore.getVehicleInventory().addVehicle(reservation.getVehicle());
        reservation.getVehicle().setVehicleStatus(VehicleStatus.AVAILABLE);
        System.out.println("Vehicle returned");
        return bill;
    }
    public void cancelReservation(int reservationId){
        Reservation reservation=reservations.get(reservationId);
        if(reservation.getReservationStatus() != ReservationStatus.CONFIRMED)
            throw new IllegalStateException("There's no reservation to cancel!");
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservation.getVehicle().setVehicleStatus(VehicleStatus.AVAILABLE);
    }
    private boolean isOverlapping(Date start1, Date end1, Date start2, Date end2){
        return !(end1.before(start2) || start1.after(end2));
    }
}
class PaymentProcessor{
    public void processPayment(Bill bill,PaymentStrategy strategy){
        strategy.pay(bill.getAmount());
        bill.getReservation().setReservationStatus(ReservationStatus.COMPLETED);
        System.out.println("Payment completed");
    }
}
public class CarRentalBasic {
    public static void main(String[] args) {
        VehicleFactory vehicleFactory=new VehicleFactory();
        Vehicle v1=vehicleFactory.createVehicle("1234", "honda city", VehicleType.LUXURY, 80);
        Vehicle v2=vehicleFactory.createVehicle("5678", "Tata nexon", VehicleType.SUV, 70);
        Vehicle v3=vehicleFactory.createVehicle("1111", "toyota fortuner", VehicleType.SUV, 120);

        Location airportLocation=new Location("airport line", "mumbai", "maharashtra", "400098");
        Location andheriLocation=new Location("andheri midc", "mumbai", "maharashtra", "400079");
        RentalStore airportStore=new RentalStore(1, "mumbai airport store", airportLocation);
        RentalStore andheriStore=new RentalStore(2, "andheri store", andheriLocation);
        airportStore.getVehicleInventory().addVehicle(v1);
        airportStore.getVehicleInventory().addVehicle(v2);
        airportStore.getVehicleInventory().addVehicle(v3);

        User dk=new User(1, "keshava", "dk@gmail.com");
        List<Vehicle> availableVehicles= airportStore.searchVehicles(VehicleType.SUV);
        Vehicle selectedVehicle = availableVehicles.get(0);
        System.out.println("The available vehicles in airport store are:");
        for(Vehicle vehicle: availableVehicles){
            System.out.println(vehicle.getModel());
        }
        PaymentStrategy paymentStrategy=new CashPaymentStrategy();
        ReservationManager reservationManager=new ReservationManager();
        Reservation reservation1=reservationManager.createReservation(new Date(126, 4, 18), new Date(126, 4, 23), dk, selectedVehicle, airportStore, andheriStore);
        // availableVehicles= airportStore.searchVehicles(VehicleType.SUV);
        // System.out.println("The available vehicles are:");
        // for(Vehicle vehicle: availableVehicles){
        //     System.out.println(vehicle.getModel());
        // }
        reservationManager.pickup(reservation1.getId());
        Bill bill1=reservationManager.returnVehicle(reservation1.getId());
        PaymentProcessor paymentProcessor=new PaymentProcessor();
        paymentProcessor.processPayment(bill1, paymentStrategy);
        availableVehicles= andheriStore.searchVehicles(VehicleType.SUV);
        System.out.println("The available vehicles in Andheri store are:");
        for(Vehicle vehicle: availableVehicles){
            System.out.println(vehicle.getModel());
        }
        availableVehicles= airportStore.searchVehicles(VehicleType.SUV);
        System.out.println("The available vehicles in airport store are:");
        for(Vehicle vehicle: availableVehicles){
            System.out.println(vehicle.getModel());
        }
    }
}
