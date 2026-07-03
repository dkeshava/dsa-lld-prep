
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CompletableFuturePractice3 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executor=Executors.newFixedThreadPool(2);
        Future<?> future=executor.submit(()->{
            return 50;
        });
        int x=(int) future.get();
        x+=10;
        x*=2;
        System.out.println("Result by future "+x);
        executor.shutdown();
        
        CompletableFuture<?> future1=CompletableFuture.supplyAsync(()->{
            return 50;
        })
        .thenApply(y->y+10)
        .thenApply(y->y*2)
        .thenApply(y->String.valueOf(y));
        System.out.println("Result by completable future: "+future1.join());
    }
}
