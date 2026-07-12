package lld.concurrencyInterviewProblems.movieTicketBookingSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

enum BookingStatus{
    CONFIRMED,
    CANCELLED
}

class Movie{
    private String movieName;
    private int movieId;
    public Movie(String movieName, int movieId){
        this.movieId=movieId;
        this.movieName=movieName;
    }
    public String getMovieName(){return movieName;}
    public int getMovieId(){return movieId;}
}

class Screen{
    private List<Seat> seats;
    private int screenNo;
    private Theatre theatre;
    private int totalSeats;
    public Screen(int screenNo, Theatre theatre, int totalSeats){
        this.screenNo=screenNo;
        this.totalSeats=totalSeats;
        this.theatre=theatre;
        this.seats=new ArrayList<>();

        for(int i=0;i<totalSeats;i++){
            seats.add(new Seat(i+1, this));
        }
    }
    public List<Seat> getSeats(){return seats;}
    public int getScreenNo(){return screenNo;}
    public int getTotalSeats(){return totalSeats;}
    public Theatre getTheatre(){return theatre;}
}

class Seat{
    private int seatNo;
    private Screen screen;
    public Seat(int seatNo, Screen screen){
        this.seatNo=seatNo;
        this.screen=screen;
    }
    public int getSeatNo(){return seatNo;}
    public Screen getScreen(){return screen;}
}

class Theatre{
    private String name;
    private int id;
    private List<Screen> screens;
    public Theatre(String name, int id){
        this.name=name;
        this.id=id;
        screens=new ArrayList<>();
    }
    public void addScreen(Screen screen){
        screens.add(screen);
    }
    public String geTheatreName(){return name;}
    public int geTheatreId(){return id;}
}
class Show{
    private int showId;
    private Movie movie;
    private Screen screen;
    private LocalDateTime startTime;
    private Map<Integer,ShowSeat> showSeats;
    public Show(int showId, Movie movie, Screen screen, LocalDateTime startTime){
        this.showId=showId;
        this.movie=movie;
        this.screen=screen;
        this.startTime=startTime;
        showSeats=new ConcurrentHashMap<>();

        for(Seat seat: screen.getSeats()){
            showSeats.putIfAbsent(seat.getSeatNo(), new ShowSeat(seat));
        }
    }
    public int getShowId(){return showId;}
    public Movie getMovie(){return movie;}
    public Screen getScreen(){return screen;}
    public Map<Integer,ShowSeat> getShowSeats(){return showSeats;}
    public LocalDateTime getStartTime(){return startTime;}
}
class ShowSeat{
    private Seat seat;
    private volatile boolean booked;
    private ReentrantLock lock = new ReentrantLock();
    public ShowSeat(Seat seat){
        this.seat=seat;
        booked=false;
    }
    public Seat getSeat(){return seat;}
    public ReentrantLock getLock(){return lock;}
    public boolean isBooked(){return booked;}
    public void setBooked(boolean b){booked=b;}
}
class Booking{
    private int bookingId;
    private User user;
    private Show show;
    private BookingStatus status;
    private List<ShowSeat> showSeats;
    public Booking(int bookingId, User user, Show show, BookingStatus status, List<ShowSeat> showSeats){
        this.bookingId=bookingId;
        this.user=user;
        this.show=show;
        this.showSeats=new ArrayList<>(showSeats);
        this.status=status;
    }
    public int getBookingId(){return bookingId;}
    public User getUser(){return user;}
    public Show getShow(){return show;}
    public List<ShowSeat> getShowSeats(){return Collections.unmodifiableList(showSeats);}
    public BookingStatus getStatus(){return status;}
    public void cancelBooking(){status=BookingStatus.CANCELLED;}
}
class User{
    private int userId;
    private String name;
    public User(int userId, String name){
        this.userId=userId;
        this.name=name;
    }
    public int getUserId(){return userId;}
    public String getUserName(){return name;}
}
class BookingManager{
    private AtomicInteger currentBookingId;
    private Map<Integer,Booking> bookings;
    public BookingManager(){
        this.currentBookingId=new AtomicInteger(1);
        this.bookings=new ConcurrentHashMap<>();
    }
    public Booking bookSeat(Show show, User user, int seatNo){
        return bookSeats(show, user, List.of(seatNo));
    }
    public Booking bookSeats(Show show, User user, List<Integer> seats){
        List<ShowSeat> showSeats=new ArrayList<>();
        if (new HashSet<>(seats).size() != seats.size()) {
            System.out.println("Duplicate seats requested");
            return null;
        }
        for(Integer i:seats){
            ShowSeat showSeat=show.getShowSeats().get(i);
            if(showSeat==null){
                System.out.println("Error: Invalid show seat!");
                return null;
            }
            showSeats.add(showSeat);
        }
        showSeats.sort(Comparator.comparingInt(showSeat -> showSeat.getSeat().getSeatNo()));
        try {
            for(ShowSeat showSeat: showSeats){
                ReentrantLock lock=showSeat.getLock();
                lock.lock();
            }
            for(ShowSeat showSeat: showSeats){
                if(showSeat.isBooked()){
                    //System.out.println("Error: seat "+showSeat.getSeat().getSeatNo()+" is already booked!");
                    return null;
                }
            }
            for(ShowSeat showSeat: showSeats){
                showSeat.setBooked(true);
            }
            int id=currentBookingId.getAndIncrement();
            Booking booking=new Booking(id, user, show, BookingStatus.CONFIRMED, showSeats);
            bookings.putIfAbsent(id, booking);
            return booking;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            for(ShowSeat showSeat: showSeats){
                showSeat.getLock().unlock();
            }
        }
    }
    public boolean cancelBooking(int bookingId,User user){
        Booking booking=bookings.get(bookingId);
        if(booking==null){
            System.out.println("No booking found");
            return false;
        }
        List<ShowSeat> showSeats = new ArrayList<>(booking.getShowSeats());
        showSeats.sort(Comparator.comparingInt(showSeat -> showSeat.getSeat().getSeatNo()));
        try {
            if (booking.getUser().getUserId() != user.getUserId()) {
                System.out.println("Error: Trying to cancel another user's booking");
                return false;
            }
            for(ShowSeat showSeat: showSeats){
                ReentrantLock lock=showSeat.getLock();
                lock.lock();
            }
            if (booking.getStatus() == BookingStatus.CANCELLED) return false;
            for(ShowSeat showSeat: showSeats){
                showSeat.setBooked(false);
            }
            booking.cancelBooking();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            for(ShowSeat showSeat: showSeats){
                showSeat.getLock().unlock();
            }
        }
    }
    public List<Seat> getAvailableSeats(Show show){
        List<Seat> availableSeats=new ArrayList<>();
        Map<Integer,ShowSeat> showSeats=show.getShowSeats();
        for(Map.Entry<Integer,ShowSeat> entry: showSeats.entrySet()){
            int seatNo = entry.getKey();
            ShowSeat showSeat = entry.getValue();

            if(!showSeat.isBooked()) availableSeats.add(showSeat.getSeat());
        }
        return availableSeats;
    }
    public List<Seat> getBookedSeats(Show show){
        List<Seat> bookedSeats=new ArrayList<>();
        Map<Integer,ShowSeat> showSeats=show.getShowSeats();
        for(Map.Entry<Integer,ShowSeat> entry: showSeats.entrySet()){
            int seatNo = entry.getKey();
            ShowSeat showSeat = entry.getValue();

            if(showSeat.isBooked()) bookedSeats.add(showSeat.getSeat());
        }
        return bookedSeats;
    }
    public List<Booking> getBookings(User user){
        List<Booking> userBookings=new ArrayList<>();
        for(Map.Entry<Integer,Booking> entry: bookings.entrySet()){
            int bookingId = entry.getKey();
            Booking booking=entry.getValue();

            if(booking.getUser().getUserId()==user.getUserId()) userBookings.add(booking);
        }
        return userBookings;
    }
    public void printSeatLayout(Show show) {
        Map<Integer, ShowSeat> showSeats = show.getShowSeats();
    
        int seatsPerRow = 10; // change if needed
        System.out.println("printing seat layout for show "+show.getShowId());
        for (int i = 1; i <= show.getScreen().getTotalSeats(); i++) {
            ShowSeat seat = showSeats.get(i);
    
            System.out.print(seat.isBooked() ? "X " : "O ");
    
            if (i % seatsPerRow == 0) {
                System.out.println();
            }
        }
    }
}
public class MovieTicketBooking_version2 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        final Theatre theatre1=new Theatre("dk", 1);
        Screen screen1=new Screen(1, theatre1, 50);
        theatre1.addScreen(screen1);

        Movie m1=new Movie("mass", 1);
        Show show1=new Show(1, m1, screen1, LocalDateTime.of(2026, 8, 10, 17, 30));

        BookingManager bookingManager=new BookingManager();
        ExecutorService executor=Executors.newFixedThreadPool(5);
        //User user1=new User(1, "dk");
        List<Future<Booking>> futures=new ArrayList<>();
        for(int i=0;i<10;i++){
            final int x=i;
            futures.add(executor.submit(()->{
                Booking booking=bookingManager.bookSeats(show1,new User(x+1, "user"+(x+1)) ,List.of(5));
                boolean bookingStatus=false;
                if(booking!=null) bookingStatus=true;
                System.out.println(Thread.currentThread().getName()+" booking status: "+bookingStatus);
                return booking;
            }));
        }
        for(Future<Booking> future: futures) future.get();

        System.out.println();
        System.out.println("Multiple seat booking: ");
        Future<Booking> booking1=executor.submit(()->{
            Booking booking= bookingManager.bookSeats(show1, new User(10, "dk"), List.of(12,13,14));
            boolean bookingStatus=false;
            if(booking!=null) bookingStatus=true;
            System.out.println(Thread.currentThread().getName()+" booking status: "+bookingStatus);
            return booking;
        });
        
        Future<Booking> booking2=executor.submit(()->{
            Booking booking= bookingManager.bookSeats(show1, new User(11, "md"), List.of(24,25,26));
            boolean bookingStatus=false;
            if(booking!=null) bookingStatus=true;
            System.out.println(Thread.currentThread().getName()+" booking status: "+bookingStatus);
            return booking;
        });
        booking1.get();
        booking2.get();

        executor.shutdown();

        System.out.println();
        bookingManager.printSeatLayout(show1);
    }
}
