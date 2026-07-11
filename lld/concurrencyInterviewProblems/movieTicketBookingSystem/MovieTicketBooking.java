package lld.concurrencyInterviewProblems.movieTicketBookingSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

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
    private boolean booked;
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

}
class BookingManager{
    private List<Booking> bookings;
    public boolean bookSeat(Show show, int seatNo){
        ShowSeat showSeat=show.getShowSeats().get(seatNo);
        if(showSeat==null){
            System.out.println("Error: Invalid seat");
            return false;
        }
        ReentrantLock lock=showSeat.getLock();
        lock.lock();
        try {
            if(showSeat.isBooked()){
                //System.out.println("Booking failed: Already booked by other users");
                return false;
            }
            showSeat.setBooked(true);
            System.out.println(Thread.currentThread().getName()+" Successfully booked the seat : "+showSeat.getSeat().getSeatNo());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            lock.unlock();
        }
    }
}
public class MovieTicketBooking {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        final Theatre theatre1=new Theatre("dk", 1);
        Screen screen1=new Screen(1, theatre1, 50);
        theatre1.addScreen(screen1);

        Movie m1=new Movie("mass", 1);
        Show show1=new Show(1, m1, screen1, LocalDateTime.of(2026, 8, 10, 17, 30));

        BookingManager bookingManager=new BookingManager();
        ExecutorService executor=Executors.newFixedThreadPool(5);

        List<Future<Boolean>> futures=new ArrayList<>();
        for(int i=0;i<10;i++){
            futures.add(executor.submit(()->{
                boolean success=bookingManager.bookSeat(show1, 5);
                System.out.println(Thread.currentThread().getName()+" booking status: "+success);
                return success;
            }));
        }
        for(Future<Boolean> future: futures) future.get();
        executor.shutdown();
    }
}
