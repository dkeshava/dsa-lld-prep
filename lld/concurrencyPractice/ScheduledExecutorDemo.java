import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorDemo {

    public static void main(String[] args) {

        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(2);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> System.out.println("Hello"),
                2,
                1,
                TimeUnit.SECONDS
        );

        scheduler.schedule(() -> {
            System.out.println("Stopping scheduler...");
            future.cancel(false);
            scheduler.shutdown();
        }, 10, TimeUnit.SECONDS);
    }
}