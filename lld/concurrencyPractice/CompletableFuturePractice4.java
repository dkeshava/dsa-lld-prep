
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFuturePractice4 {
    public static void main(String[] args) {
        ExecutorService executor=Executors.newFixedThreadPool(2);
        
        CompletableFuture<?> future=CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Stage 1: 10");
            System.out.println(Thread.currentThread().getName());
            return 10;
        },executor).thenApply(x->{
            System.out.println("Stage 2: "+(x+10));
            System.out.println(Thread.currentThread().getName());
            return x+10;
        })
        .thenApply(x->{
            System.out.println("Stage 3: "+(x*2));
            System.out.println(Thread.currentThread().getName());
            return x*2;})
        .thenApply(x->String.valueOf(x)+" string");

        System.out.println(Thread.currentThread().getName()+"...");
        System.out.println("Result = "+future.join());
        executor.shutdown();

        //Exercise 7
        // CompletableFuture<?> future=CompletableFuture.supplyAsync(()->{
        //     return 100;
        // })
        // .thenApply(x->x-20)
        // .thenApply(x->x*3)
        // .thenApply(x->String.valueOf(x));

        // System.out.println("Result: "+future.join());
    }
}
