
import java.util.concurrent.CompletableFuture;

public class CompletableFuturePractice1{
    public static void main(String[] args) {
        CompletableFuture<Void> future=CompletableFuture.runAsync(()->{
            System.out.println("Task Started");
            System.out.println(Thread.currentThread().getName());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Task finished");
        });
        System.out.println(Thread.currentThread().getName()+ ": Main thread continues");

        future.join();
    }
}