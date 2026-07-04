
import java.util.concurrent.CompletableFuture;

public class CFuture7 {
    public static void main(String[] args) {
        CompletableFuture<String> future=CompletableFuture.supplyAsync(()->{
            if(true) throw new RuntimeException();
            return "success";
        })
        .exceptionally(ex->{
            System.out.println("Exception caught: " + ex.getMessage());
            return "Recovery value";
        });

        System.out.println(future.join());

        System.out.println();
        System.out.println("Using handle");
        CompletableFuture<Integer> future2=CompletableFuture.supplyAsync(()->{
            if(true) throw new RuntimeException();
            return 10;
        })
        .handle((result, ex)->{
            if(ex!=null){
                System.out.println("Exception caught: " + ex.getMessage());
                return 0;
            }
            System.out.println("No exception occured");
            return result*2;
        });

        System.out.println(future2.join());
    }
}
