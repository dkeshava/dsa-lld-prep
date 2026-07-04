
import java.util.concurrent.CompletableFuture;

public class CFuture6{
    public static CompletableFuture<String> getUser(){
        return CompletableFuture.supplyAsync(()->{
            System.out.println("Fetching user...");
            return "DK";
        });
    }
    public static CompletableFuture<Integer> getAge(String user) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching age for " + user);
            return 23;
        });
    }
    public static CompletableFuture<String> getCity(){
        return CompletableFuture.supplyAsync(()->{
            System.out.println("Fetching city...");
            return "Mumbai";
        });
    }
    public static void main(String[] args) throws InterruptedException {
        CompletableFuture<Integer> future=getUser().thenCompose(x->getAge(x));
        //Thread.sleep(2000);
        System.out.println(future.join());

        CompletableFuture<CompletableFuture<Integer>> future2=getUser().thenApply(x->getAge(x));
        //Thread.sleep(2000);
        System.out.println(future2.join().join());

        CompletableFuture<String> user=getUser();
        CompletableFuture<String> city=getCity();

        CompletableFuture<String> combined=user.thenCombine(city, (result1, result2)->result1+" lives in "+result2);
        System.out.println(combined.join());
    }
}