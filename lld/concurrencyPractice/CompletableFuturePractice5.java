
import java.util.concurrent.CompletableFuture;

public class CompletableFuturePractice5 {
    public static void main(String[] args) {
        CompletableFuture<Integer> future=CompletableFuture.supplyAsync(()->{
            System.out.println("A");
            return 10;
        });
        future.thenApply(x->{
            System.out.println("B");
            return x*2;
        });
        System.out.println("Main");

        System.out.println(future.join());

        CompletableFuture<Integer> future1=CompletableFuture.supplyAsync(()->{
            System.out.println("C");
            return 10;
        });
        CompletableFuture<Integer> result=future1.thenApply(x->{
            System.out.println("D");
            return x*2;
        });
        System.out.println(result.join());
    }
}
