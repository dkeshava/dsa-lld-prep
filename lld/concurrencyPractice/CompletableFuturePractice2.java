
import java.util.concurrent.CompletableFuture;

public class CompletableFuturePractice2 {
    public static void main(String[] args){
        // CompletableFuture<?> future=CompletableFuture.supplyAsync(()->{
        //     try {
        //         Thread.sleep(2000);
        //     } catch (InterruptedException e) {
        //         Thread.currentThread().interrupt();
        //     }
        //     System.out.println(Thread.currentThread().getName());
        //     return 50;
        // }).thenApply(x->x+10)
        // .thenApply(x->{return x*2;})
        // .thenApply(x->String.valueOf(x)+" string");

        CompletableFuture<?> future=CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Stage 1: 10");
            System.out.println(Thread.currentThread().getName());
            return 10;
        }).thenApply(x->{
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
    }
}
